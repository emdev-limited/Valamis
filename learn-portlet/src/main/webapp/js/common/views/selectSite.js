valamisApp.module('Views.SelectSite', function (SelectSite, valamisApp, Backbone, Marionette, $, _) {

  SelectSite.LiferaySiteSelectToolbarView = Marionette.ItemView.extend({
    template: '#liferaySiteDialogToolbar',
    events: {
      'keyup .js-site-search': 'filterCourses',
      'click .dropdown-menu > li': 'filterCourses'
    },
    behaviors: {
      ValamisUIControls: {}
    },
    onValamisControlsInit: function(){
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

  SelectSite.LiferaySiteSelectItemView = Marionette.ItemView.extend({
    template: '#liferaySiteElementView',
    tagName: 'tr',
    events: {
      'click .js-select-item': 'toggleThis'
    },
    toggleThis: function () {
      this.triggerMethod('site:selected');
      this.model.set('selected', !this.model.get('selected') );
    }
  });

  SelectSite.LiferaySiteSelectCollectionView = Marionette.CollectionView.extend({
    tagName: 'tbody',
    childView: SelectSite.LiferaySiteSelectItemView,
    childEvents: {
      'site:selected': 'itemSelected'
    },
    initialize: function(options){
      this.singleSelect = options.singleSelect;
    },
    itemSelected: function(childView){
      this.triggerMethod('sitelist:select', childView.model);
    }
  });

  SelectSite.LiferaySiteSelectLayout = Marionette.LayoutView.extend({
    template: '#liferaySiteDialogLayout',
    regions: {
      'toolbar': '#liferaySiteSelectToolbarRegion',
      'sites': '#liferaySiteSelectListRegion',
      'paginator': '#siteListPaginator',
      'paginatorShowing': '#siteListPagingShowing'
    },
    events: {
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
      this.paginatorModel.set({ 'itemsOnPage': 10 });

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
      var toolbarView = new SelectSite.LiferaySiteSelectToolbarView();
      this.toolbar.show(toolbarView);

      var siteListView = new SelectSite.LiferaySiteSelectCollectionView({
        collection: this.collection,
        singleSelect: this.singleSelect
      });

      this.paginatorView = new ValamisPaginator({
        language: Valamis.language,
        model: this.paginatorModel,
        topEdgeParentView: this,
        topEdgeSelector: this.regions.toolbar,
        topEdgeOffset: 0
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
    },
    addCourses: function () {
      var selectedSiteIds = this.collection.filter(function (item) {
        return item.get('selected');
      }).map(function (item) {
        return item.get('id');
      });
      this.trigger('addSelectedLfSite', selectedSiteIds);
    }
  });

});