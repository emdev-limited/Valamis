contentProviderManager.module('Views', function (Views, contentProviderManager, Backbone, Marionette, $, _) {

    Views.RowItemView = Marionette.ItemView.extend({
        tagName: 'div',
        className: 'tile s-12 m-4 l-2',
        template: '#contentProviderManagerRowViewTemplate',
        events: {
            'click .dropdown-menu > li.js-provider-info-edit': 'editProviderInfo',
            'click .dropdown-menu > li.js-provider-delete': 'deleteProvider',
            'click .js-open-provider-edit': 'editProviderInfo'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        modelEvents: {
            'change': 'render'
        },
        templateHelpers: function() {
            return {
                timestamp: Date.now()
            }
        },
        editProviderInfo: function(){
            this.triggerMethod('providerList:edit', this.model);
        },
        deleteProvider: function(){
            var that = this;
            valamisApp.execute('valamis:confirm', { message: Valamis.language['warningDeleteProviderMessageLabel'] }, function(){
                that.deleteProviderTrigger();
            });
        },
        deleteProviderTrigger: function() {
            this.triggerMethod('providerList:delete:course', this.model);
        }
    });

    Views.ProviderListView = Marionette.CompositeView.extend({
        template: '#contentProviderManagerLayoutTemplate',
        childViewContainer: '.js-providers-list',
        childView: Views.RowItemView,
        initialize: function () {
            var that = this;
            that.collection.on('sync', function() {
                that.$('.no-content-table').toggleClass('hidden', that.collection.total == 0);
                that.$('.js-no-items').toggleClass('hidden', that.collection.hasItems());
            });
            that.options.settings.on('change:displayMode', this.setDisplayMode, this);
        },
        onRender: function () {
            this.$('.valamis-tooltip').tooltip();
            this.setDisplayMode();
        },
        setDisplayMode: function() {
            var displayMode = this.options.settings.get('displayMode')|| Views.DISPLAY_TYPE.LIST;
            this.$('.js-providers-list').removeClass('list');
            this.$('.js-providers-list').removeClass('tiles');
            this.$('.js-providers-list').addClass(displayMode);
            valamisApp.execute('update:tile:sizes', this.$el);
        }
    });
});