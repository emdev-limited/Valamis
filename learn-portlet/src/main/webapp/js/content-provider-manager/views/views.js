contentProviderManager.module('Views', function (Views, contentProviderManager, Backbone, Marionette, $, _) {

    Views.DISPLAY_TYPE = {
        LIST: 'list',
        TILES: 'tiles'
    };

    Views.ToolbarView = Marionette.ItemView.extend({
        template: '#contentProviderManagerToolbarTemplate',
        behaviors: {
            ValamisUIControls: {}
        },
        templateHelpers: function() {
            return {
                tilesModeOption: Views.DISPLAY_TYPE.TILES,
                listModeOption: Views.DISPLAY_TYPE.LIST
            }
        },
        events: {
            'keyup .js-search': 'changeSearchText',
            'click .js-new-provider' : 'createNewProvider',
            'paginatorShowing' : 'contentProviderManagerToolbarShowing',
            'click .dropdown-menu > li.js-sort': 'changeSort',
            'click .js-display-option': 'changeDisplayMode'
        },
        initialize:function(){
            this.inputTimeout = {};
        },
        onRender: function(){
            this.$('.js-sort-filter').valamisDropDown('select', this.model.get('sort'));
            this.$('.js-search').val(this.model.get('searchtext'));

            var displayMode = this.options.settings.get('displayMode') || Views.DISPLAY_TYPE.LIST;
            this.$('.js-display-option[data-value="'+ displayMode +'"]').addClass('active');
        },
        changeSort: function(e){
            this.model.set('sort', $(e.target).attr("data-value"));
        },
        changeSearchText: function(e){
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.model.set('searchtext', $(e.target).val());
            }, 800);
        },
        createNewProvider: function(){
            var newProvider = new contentProviderManager.Entities.Provider();
            this.triggerMethod('providerList:edit', newProvider);
        },
        changeDisplayMode: function(e) {
            this.$('.js-display-option').removeClass('active');
            var elem = $(e.target).closest('.js-display-option');
            elem.addClass('active');
            this.triggerMethod('toolbar:displaymode:change', elem.attr('data-value'));
        }
    });

    Views.CourseTypes = {
        OPEN: 'OPEN',
        ON_REQUEST: 'ON_REQUEST',
        CLOSED: 'CLOSED'
    };

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#contentProviderManagerAppLayoutTemplate',
        regions: {
            'providerList': '#contentProvidersList',
            'toolbar': '#contentProvidersToolbar',
            'paginator': '#contentProvidersPaginator',
            'paginatorShowing': '#contentProvidersToolbarShowing'
        },
        childEvents: {
            'providerList:delete:course': function(childView, model) {
                var that = this;
                model.deleteProvider().then(
                    function() {
                        valamisApp.execute('notify', 'success', Valamis.language['providerSuccessfullyDeletedLabel']);
                        that.fetchCollection(false);
                    },
                    function() {
                        valamisApp.execute('notify', 'error', Valamis.language['providerFailedToDeleteLabel']);
                    }
                );
            },
            'providerList:edit': function(childView, model) {
                this.editProvider(model);
            },
            'toolbar:displaymode:change': function(childView, displayMode) {
                this.settings.set('displayMode', displayMode);
                this.settings.save();
            }
        },
        initialize: function(options){

            var that = this;
            that.paginatorModel = new PageModel();

            this.settings = new SettingsHelper({url: window.location.href, portlet: options.portletName});
            this.settings.fetch();

            that.filter = new contentProviderManager.Entities.Filter(this.settings.get('searchParams'));
            that.filter.on('change', function(model){
                that.fetchCollection(true);
                that.settings.set('searchParams', model.toJSON());
                that.settings.save();
            });

            that.providers = options.providers;
            that.providers.on('fetchCollection', function() {
                that.fetchCollection(true);
            });

            that.providers.on('providerCollection:updated', function (details) {
                that.updatePagination(details);
            });
        },
        onRender: function () {
            var providerListView = new Views.ProviderListView({
                collection: this.providers,
                paginatorModel: this.paginatorModel,
                settings: this.settings
            });
            providerListView.on('render:collection', function(view) {
                valamisApp.execute('update:tile:sizes', view.$el);
            });
            var toolbarView = new Views.ToolbarView({
                model: this.filter,
                settings: this.settings
            });
            this.toolbar.show(toolbarView);

            this.paginatorView = new ValamisPaginator({
                language: Valamis.language,
                model : this.paginatorModel
            });
            this.paginator.show(this.paginatorView);

            var paginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: this.paginatorModel
            });
            this.paginatorShowing.show(paginatorShowingView);

            this.paginatorView.on('pageChanged', function () {
                this.fetchCollection(false);
            }, this);

            this.providerList.show(providerListView);
        },

        updatePagination: function (details) {
            this.paginatorView.updateItems(details.total);
        },
        fetchCollection: function(filterChanged) {
            if(filterChanged) {
                this.paginatorModel.set('currentPage', 1);
            }

            this.providers.fetchMore({
                reset: true,
                filter: this.filter.toJSON(),
                currentPage: this.paginatorModel.get('currentPage'),
                itemsOnPage: this.paginatorModel.get('itemsOnPage')
            });
        },
        editProvider: function(provider) {
            var editProviderView = new Views.MainEditView({
                model: provider
            });

            var that = this;
            var modalView = new valamisApp.Views.ModalView({
                template: '#contentProvidersEditModalTemplate',
                contentView: editProviderView,
                beforeSubmit: function() {
                    return editProviderView.updateModel();
                },
                submit: function(){
                    provider.save().then(
                        function(){
                            that.fetchCollection(true);
                        },
                        function(xr, er) {
                            if (er != null) {
                                that.displayProviderError(xr, er);
                                that.editProvider(provider);
                            }
                        });
                }
            });

            valamisApp.execute('modal:show', modalView);
        },
        displayProviderError: function(xr, er) {
            var details = er.responseJSON;
            var messageLabel = 'createProviderError';
            if (details) {
                if (details.field == "friendlyUrl") {
                    if (details.reason == "duplicate") {
                        messageLabel = 'friendlyUrlIsDuplicatedError';
                    } else if (details.reason == "invalid-size") {
                        messageLabel = 'friendlyUrlHasInvalidLengthError';
                    } else {
                        messageLabel = 'friendlyUrlIsWrongError';
                    }
                } else if (details.field == "name") {
                    if (details.reason == "duplicate") {
                        messageLabel = 'providerTitleIsDuplicatedError';
                    } else {
                        messageLabel = 'providerTitleIsWrongError';
                    }
                }
            }
            valamisApp.execute('notify', 'warning', Valamis.language[messageLabel]);
        }
    });

    Views.MainAppLayoutView = Marionette.LayoutView.extend({
        regions: {
            contentProvidersRegion: '#contentProvidersRegion'
        },
        initialize: function() {
            this.template = '#contentProviderManagerMainAppLayoutTemplate';
        },
        showTab: function(e) {
            e.preventDefault();
            $(e.target).tab('show');
        },
        onRender: function() {
            var providerCollection = new contentProviderManager.Entities.ProviderCollection();
            var layoutView = new contentProviderManager.Views.AppLayoutView({
                providers: providerCollection,
                portletName: 'contentProviderManager'
            });
            this.contentProvidersRegion.show(layoutView);
            providerCollection.trigger('fetchCollection');
        },
        onShow: function () {
        }
    });
});