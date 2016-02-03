/**
 * Created by igorborisov on 16.04.15.
 */
valamisApp.module("Views", function (Views, valamisApp, Backbone, Marionette, $, _) {

    Views.MainLayout = Marionette.LayoutView.extend({
        tagName: 'div',
        className: 'portlet',
        template: '#valamisAppLayoutTemplate',
        regions:{
            modals: {
                selector: '#valamisAppModalRegion',
                regionClass: Backbone.Marionette.Modals
            }
        },
        onRender: function() {}
    });

    Views.LiferaySiteSelectToolbarView = Marionette.ItemView.extend({
        template: '#liferaySiteDialogToolbar',
        events: {
            'keyup .js-site-search': 'filterCourses',
            'click .dropdown-menu > li': 'filterCourses',
            'click .js-addCourses': 'addCourses'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        onValamisControlsInit: function(){
        },
        addCourses: function () {
            this.trigger('addSelectedLfSite', this.collection);
        },
        filterCourses: function () {
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(this.applyFilter.bind(this), 800);
        },
        applyFilter: function () {
            var that = this;
            clearTimeout(that.inputTimeout);

            that.triggerMethod('toolbar:filter', {
                filter: that.$('.js-site-search').val(),
                sort: that.$('.js-site-sort').data('value')
            });
        }
    });

    Views.LiferaySiteSelectItemView = Marionette.ItemView.extend({
        template: '#liferaySiteElementView',
        tagName: 'tr',
        events: {
            'click .js-toggle-site-button': 'toggleThis'
        },
        toggleThis: function () {
            this.triggerMethod('site:selected');

            this.model.set('selected', !this.model.get('selected') );

            if(this.model.get('selected')){
                this.$('.js-toggle-site-button').removeClass('neutral');
                this.$('.js-toggle-site-button').addClass('primary');
            }else{
                this.$('.js-toggle-site-button').addClass('neutral');
                this.$('.js-toggle-site-button').removeClass('primary');
            }
        }
    });

    Views.LiferaySiteSelectCollectionView = Marionette.CollectionView.extend({
        tagName: 'tbody',
        childView: Views.LiferaySiteSelectItemView,
        childEvents: {
            'site:selected': 'itemSelected'
        },
        initialize: function(options){
            this.singleSelect = options.singleSelect;
         },
        itemSelected: function(childView){
            //if(this.singleSelect){
            //    this.collection.each(function(item){
            //        item.set('selected', false);
            //    });
            //    this.$('.js-toggle-site-button').removeClass('primary');
            //    this.$('.js-toggle-site-button').addClass('neutral');
            //}
            this.triggerMethod('sitelist:select', childView.model);
        }
    });

    Views.LiferaySiteSelectLayout = Marionette.LayoutView.extend({
        template: '#liferaySiteDialogLayout',
        regions: {
            'toolbar': '#liferaySiteSelectToolbarRegion',
            'sites': '#liferaySiteSelectListRegion',
            'paginator': '#siteListPaginator',
            'paginatorShowing': '#siteListPagingShowing'
        },
        event: {
            'click .js-addCourses': 'addCourses'
        },
        initialize: function(options){
            var that = this;
            this.singleSelect = options.singleSelect;
            this.collection = new valamisApp.Entities.LiferaySiteCollection();
            this.model = new Backbone.Model({
               singleSelect: options.singleSelect
            });

            this.paginatorModel = new PageModel();
            this.paginatorModel.set({'itemsOnPage': 10});

            this.collection.on("siteCollection:updated", function (details) {
                that.updatePagination(details, that);
            });
        },
        childEvents: {
            'toolbar:filter': function(child, filterdata){
                this.fetchSites(filterdata);
            },
            'sitelist:select': function(child, site){
                if(this.singleSelect){
                    this.trigger('liferay:site:selected', site)
                }
            }
        },
        onRender: function(){
            var that = this;
            var toolbarView = new Views.LiferaySiteSelectToolbarView();
            this.toolbar.show(toolbarView);

            var siteListView = new Views.LiferaySiteSelectCollectionView({
                collection: this.collection,
                singleSelect: this.singleSelect
            });

            this.paginatorView = new ValamisPaginator({
                language: Valamis.language,
                model: this.paginatorModel
            });

            this.paginatorView.on('pageChanged', function () {
                that.fetchSites();
            });

            this.paginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: this.paginatorModel
            });

            this.sites.show(siteListView);
            this.fetchSites();

            this.paginator.show(this.paginatorView);
            this.paginatorShowing.show(this.paginatorShowingView);
        },
        fetchSites: function (filterData) {
            var that = this;
            var filter = {
                filter: '',
                sort: true
            };

            if(filterData) {
                filter.filter = filterData.filter || '';
                filter.sort = filterData.sort;

                that.paginatorModel.set('currentPage', 1);
            }

            this.collection.fetch({
                reset: true,
                currentPage: that.paginatorModel.get('currentPage'),
                itemsOnPage: that.paginatorModel.get('itemsOnPage'),
                filter: filter.filter ,
                sort: filter.sort
            });
        },
        updatePagination: function (details, context) {
            this.paginatorView.updateItems(details.total);
        }
    });

    Views.DeleteConfirmationView = Marionette.ItemView.extend({
        template: '#valamisDeleteConfirmationTemplate',
        events: {
            'click .js-confirmation': 'confirmDelete'
        },
        initialize: function (options) {
            this.options.title = options.title || Valamis.language['deleteConfirmationTitle'];
        },
        templateHelpers: function(){
            return {
               message:  this.options.message || Valamis.language['deleteConfirmationMessage']
            }
        },
        confirmDelete: function () {
            this.trigger('deleteConfirmed', this);
        },
        onRender: function(){
            valamisApp.execute('notify', 'info', this.$el,
                {
                    'positionClass': 'toast-center',
                    'timeOut': '0',
                    'showDuration': '0',
                    'hideDuration': '0',
                    'extendedTimeOut': '0'
                }, this.options.title);
        }
    });

    Views.SaveConfirmationView = Marionette.ItemView.extend({
        template: '#valamisSaveConfirmationTemplate',
        events: {
            'click .js-confirmation': 'confirmSave',
            'click .js-decline': 'declineSave'
        },
        initialize: function (options) {
            this.options.title = options.title || Valamis.language['saveConfirmationTitle'];
        },
        templateHelpers: function(){
            return {
                message:  this.options.message || Valamis.language['saveConfirmationMessage']
            }
        },
        confirmSave: function () {
            this.trigger('saveConfirmed', this);
        },
        declineSave: function () {
            this.trigger('saveDeclined', this);
        },
        onRender: function(){
            valamisApp.execute('notify', 'info', this.$el,
                {
                    'positionClass': 'toast-center',
                    'timeOut': '0',
                    'showDuration': '0',
                    'hideDuration': '0',
                    'extendedTimeOut': '0'
                }, this.options.title);
        }
    });
});