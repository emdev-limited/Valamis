lessonViewer.module('Views', function (Views, lessonViewer, Backbone, Marionette, $, _) {

    var DISPLAY_TYPE = {
        LIST: 'list',
        TILES: 'tiles'
    };

    var DESKTOP_LANDSCAPE = 1024;

    Views.SharePackageView = Marionette.ItemView.extend({
        template: '#lessonViewerSharePackageViewTemplate',
        getShareComment: function() {
            return this.$('.js-package-comment').val();
        }
    });

    Views.ToolbarView = Marionette.ItemView.extend({
        template: '#lessonViewerToolbarTemplate',
        events: {
            'click .dropdown-menu > li.js-category': 'changeCategory',
            'click .js-sort-filter .dropdown-menu > li': 'changeSort',
            'keyup .js-search': 'changeSearchText',
            'click .js-list-view': 'listDisplayMode',
            'click .js-tile-view': 'tilesDisplayMode'
        },
        modelEvents: {
            'change:categories': 'render'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        onValamisControlsInit: function(){
            this.$('.js-category-filter').valamisDropDown('select', this.model.get('selectedCategories')[0]);
            this.$('.js-sort-filter').valamisDropDown('select', this.model.get('sort'));
            this.$('.js-search').val(this.model.get('searchtext'));

            var displayMode = lessonViewer.settings.get('displayMode');
            if (displayMode === DISPLAY_TYPE.TILES)
                this.$('.js-tile-view').addClass('active');
            else
                this.$('.js-list-view').addClass('active');
        },
        onRender: function(){
            var paginatorModel = this.options.paginatorModel;

            var paginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: paginatorModel,
                el: this.$('#lessonViewerToolbarShowing')
            });

            paginatorModel.on('showAll', function (value) {
                this.model.set({'isShowingAll': value});
            }, this);

            paginatorShowingView.render();
        },
        listDisplayMode: function(){
            this.changeDisplayMode('list');
            this.$('.js-list-view').addClass('active');
        },
        tilesDisplayMode: function(){
            this.changeDisplayMode('tiles');
            this.$('.js-tile-view').addClass('active');
        },
        changeDisplayMode: function(displayMode){
            this.triggerMethod('toolbar:displaymode:change', displayMode);
            this.$('.js-display-option').removeClass('active');
            lessonViewer.settings.set('displayMode', displayMode);
            lessonViewer.settings.save();
        },
        changeSearchText:function(e){
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.model.set('searchtext', $(e.target).val());
            }, 800);
        },
        changeCategory: function(e){
            this.model.set('selectedCategories', [ $(e.target).attr('data-value') ]);
        },
        changeSort: function(e){
            this.model.set('sort', $(e.target).attr('data-value'));
        }
    });

    Views.PackageItemView = Marionette.ItemView.extend({
        template: '#playerTileItemView',
        templateHelpers: function () {
            var isSuspended = !!(this.model.get('suspendedId'));

            var link = lessonViewer.getLessonLink(
              this.model.get('id'),
              this.model.get('packageType'),
              this.model.get('title'),
              isSuspended
            );

            var categories = this.model.get('tags').map(function(i){ return i.text }).join(' • ');

            var packageStatusLabel = (isSuspended)
              ? 'suspendedPackageStatusLabel'
              : (this.model.get('status') + 'PackageStatusLabel');

            var packageRating = this.model.get('rating');

            return {
                courseId: Utils.getCourseId,
                timestamp: Date.now(),
                link: link,
                dateString: new Date(this.model.get('creationDate')).toLocaleDateString(),
                packageAuthor: this.model.get('owner') || Valamis.language['removedUserLabel'],
                categories: categories,
                packageStatusLabel: Valamis.language[packageStatusLabel],
                canOrder: !lessonViewer.isSortDisabled,
                ratingAverage: Math.round(packageRating.average * 10) / 10,
                ratingScore: packageRating.score,
                noAverage: (packageRating.total == 0),
                notRated: (packageRating.score == 0)
            }
        },
        className: 'tile s-12 m-4 l-2',
        events: {
            'click .js-share-package': 'sharePackage'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        sortHelper: function() {
            if($(window).width() <= DESKTOP_LANDSCAPE)
                this.$('.js-sortable-element').addClass('js-handle');
            else
                this.$el.addClass('js-handle');

            this.$el.attr('data-orderIndex', this.model.id);
        },
        onRender: function () {
            this.$el.attr('id', this.model.id);

            var that = this;
            if (Utils.isSignedIn()) {
                this.$('.js-valamis-rating')
                    .on('valamisRating:changed', function (e, score) {
                        that.setPackageRating(score)
                    })
                    .on('valamisRating:deleted', function (e) {
                        that.deletePackageRating()
                    });
            } else {
                this.$('.js-valamis-rating').valamisRating('disable');
            }

            this.sortHelper();

            return this;
        },
        setPackageRating: function(score) {
            var that = this;
            this.model.ratePackage({}, {
                ratingScore: score,
                success: function (response) {
                    that.$('.js-valamis-rating').valamisRating('score', response.average, score);
                }
            });
        },
        deletePackageRating: function() {
            var that = this;
            this.model.deletePackageRating({}, {
                success: function (response) {
                    that.$('.js-valamis-rating').valamisRating('score', response.average, 0);
                }
            });
        },
        sharePackage: function() {
            var that = this;
            var shareView = new lessonViewer.Views.SharePackageView();
            var shareLessonModalView = new valamisApp.Views.ModalView({
                contentView: shareView,
                header: Valamis.language['packageCommentLabel'],
                submit: function () {
                    var comment = shareView.getShareComment();
                    that.model.sharePackage({}, {comment: comment}).then(function (result) {
                        valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
                    }, function (err, res) {
                        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                    });
                }
            });
            valamisApp.execute('modal:show', shareLessonModalView);
        }
    });

    Views.Packages = Marionette.CompositeView.extend({
        template: '#packageSortableListTemplate',
        className: 'js-package-items val-row',
        childViewContainer: '.js-package-items-sortable',
        childView: Views.PackageItemView,
        initialize: function (options) {
            var that = this;
            this.paginatorModel = options.paginatorModel;
            this.collection.on('reset', function(){
                that.loadingToggle(false);
            });
            this.collection.on('sync', function(){
                that.loadingToggle(true);
            });
        },
        onRender: function() {
            var displayMode = lessonViewer.settings.get('displayMode')|| DISPLAY_TYPE.LIST;
            this.$el.addClass(displayMode);
        },
        loadingToggle:function(hidden) {
            this.$('.loading-container').toggleClass('hidden', hidden);
        }
    });

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#lessonViewerLayoutTemplate',
        regions:{
            'toolbar' : '#lessonViewerToolbar',
            'packageList' : '#lessonViewerPackages',
            'paginator': '#lessonViewerPaginator'
        },
        childEvents: {
            'toolbar:displaymode:change': function (childView, displayMode) {
                this.packageList.currentView.$el.removeClass('list');
                this.packageList.currentView.$el.removeClass('tiles');
                this.packageList.currentView.$el.addClass(displayMode);

                valamisApp.execute('update:tile:sizes', this.packageList.currentView.$el);
            }
        },
        initialize: function() {
            var that = this;
            that.paginatorModel = lessonViewer.paginatorModel;
            that.packages = lessonViewer.packages;

            that.packages.on('packageCollection:updated', function (details) {
                that.updatePagination(details);
            });
        },
        onRender: function() {
            var toolbarView = new Views.ToolbarView({
                model: lessonViewer.filter,
                paginatorModel: this.paginatorModel
            });

            this.paginatorView = new ValamisPaginator({
                language: Valamis.language,
                model : this.paginatorModel,
                topEdgeParentView: this,
                topEdgeSelector: '#lessonViewerToolbarShowing'
            });

            lessonViewer.packageListView = new Views.Packages({
                collection: lessonViewer.packages,
                paginatorModel: this.paginatorModel
            });

            lessonViewer.packageListView.on('render:collection', function(view){
                valamisApp.execute('update:tile:sizes', view.$el);
            });

            this.paginatorView.on('pageChanged', function () {
                lessonViewer.execute('packages:reload');
            }, this);

            this.toolbar.show(toolbarView);
            this.paginator.show(this.paginatorView);

            this.packageList.show(lessonViewer.packageListView);

            lessonViewer.execute('packages:reload');
        },
        updatePagination: function (details, context) {
            this.paginatorView.updateItems(details.total);
        },
        onShow: function() {
            this.doSortable();
        },
        doSortable: function() {
            var that = this;
            this.lessonsSortable = Sortable.create(sortable, {
                dataIdAttr: 'data-orderIndex',
                handle: '.js-handle',
                animation: 200,
                disabled: !!(lessonViewer.isSortDisabled),
                store: {
                    get: function (sortable) {
                        var order = localStorage.getItem(sortable.options.group);
                        return order ? order.split('|') : [];
                    },
                    set: function (sortable) {
                        that.order = sortable.toArray();
                        that.saveOrder(that.order);
                    }
                }
            });
        },
        toggleSortable: function () {
            this.lessonsSortable.option('disabled', lessonViewer.isSortDisabled);
        },
        saveOrder: function(order) {
            if (order && order.length > 0) {
                var options = {
                    playerId: lessonViewer.playerId,
                    index: order
                };
                lessonViewer.packageListView.collection.updateIndex({}, options);
            }
        }
    });

});