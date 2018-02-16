valamisApp.module('Views.BaseLayout', function (BaseLayout, valamisApp, Backbone, Marionette, $, _) {

  var SEARCH_TIMEOUT = 800;

  BaseLayout.IdsToSelect = [];

  BaseLayout.ToolbarView = Marionette.ItemView.extend({
    template: '#valamisBaseToolbarViewTemplate',
    behaviors: {
      ValamisUIControls: {}
    },
    ui: {
      searchInput: '.js-search',
      sortFilterItem: '.js-sort-filter .dropdown-menu > li',
      selectAllButton: '.js-select-all',
      paginatorShowingField: '.js-paginator-showing'
    },
    events: {
      'keyup @ui.searchInput': 'changeSearchText',
      'click @ui.sortFilterItem': 'changeSort',
      'click @ui.selectAllButton': 'selectAll'
    },
    onRender: function() {
      this.selectAllValue = false;

      var paginatorShowingView = new ValamisPaginatorShowing({
        language: Valamis.language,
        model: this.options.paginatorModel,
        el: this.$(this.ui.paginatorShowingField)
      });
      paginatorShowingView.render();
    },
    changeSearchText:function(e){
      var that = this;
      clearTimeout(this.inputTimeout);
      this.inputTimeout = setTimeout(function(){
        that.model.set('searchtext', $(e.target).val());
      }, SEARCH_TIMEOUT);
    },
    changeSort: function(e){
      this.model.set('sort', $(e.target).attr('data-value'));
    },
    selectAll: function() {
      this.selectAllValue = !this.selectAllValue;
      this.triggerMethod('items:list:select:all', this.selectAllValue);
    }
  });

  BaseLayout.SelectListItemView = Marionette.ItemView.extend({
    template: '#valamisBaseSelectListItemViewTemplate',
    tagName: 'tr',
    ui: {
      selectItem: '.js-select-item'
    },
    events: {
      'click @ui.selectItem': 'toggleSelect'
    },
    modelEvents: {
      'change:itemSelected': 'onItemSelectedChange'
    },
    initialize: function() {
      if(_.contains(BaseLayout.IdsToSelect, this.model.get(this.model.idAttribute)))
        this.model.set('itemSelected', true);
    },
    toggleSelect: function() {
      var wasSelected = this.model.get('itemSelected');
      this.model.set('itemSelected', !wasSelected);
    },
    onItemSelectedChange: function() {
      var isSelected = this.model.get('itemSelected');
      this.ui.selectItem.prop('checked', isSelected);

      var idIndex;
      var modelId = this.model.get(this.model.idAttribute);
      if (isSelected)
        BaseLayout.IdsToSelect.push(modelId);
      else {
        idIndex = BaseLayout.IdsToSelect.indexOf(modelId);
        if (idIndex > -1) BaseLayout.IdsToSelect.splice(idIndex, 1);
      }
    }
  });

  BaseLayout.ListItemView = Marionette.ItemView.extend({
    template: '#valamisBaseListItemViewTemplate',
    tagName: 'tr',
    ui: {
      selectItem: '.js-select-item',
      deleteItem: '.js-delete-item'
    },
    events: {
      'click @ui.selectItem': 'toggleSelect',
      'click @ui.deleteItem': 'deleteItem'
    },
    modelEvents: {
      'change:itemSelected': 'onItemSelectedChange'
    },
    onItemSelectedChange: function() {
      this.ui.selectItem.prop('checked', this.model.get('itemSelected'));
    },
    toggleSelect: function() {
      this.model.set('itemSelected', this.$('.js-select-item').is(':checked'), { silent: true });
    },
    deleteItem: function() {
      this.model.destroy();
    }
  });

  BaseLayout.ListView = Marionette.CompositeView.extend({
    template: '#valamisBaseListViewTemplate',
    className: 'val-table-wrapper',
    ui: {
      noItemsLabel: '.js-no-items-label'
    },
    childView: BaseLayout.ListItemView,
    childViewContainer: '.js-items-list',
    onRender: function() {
      this.collection.on('sync', function() {
        this.ui.noItemsLabel.toggleClass('hidden', this.collection.length !== 0);
      }, this);
    }
  });

  BaseLayout.MainLayoutView = Marionette.LayoutView.extend({
    template: '#valamisBaseLayoutViewTemplate',
    regions: {
      'toolbar': '#valamisBaseToolbar',
      'itemsList': '#valamisBaseList',
      'listPaginator': '#valamisBaseListPaginator'
    },
    childEvents: {
      'items:list:select:all': function (childView, selectAllValue) {
        this.collection.each(function(model) {
          model.set('itemSelected', selectAllValue);
        })
      }
    },
    initialize: function() {
      BaseLayout.IdsToSelect = [];
      this.filter = this.filter || new valamisApp.Entities.Filter();
      this.filter.on('change', function () {
        this.fetchCollection(true);
      }, this);

      this.paginatorModel = this.paginatorModel || new PageModel({itemsOnPage: 10});
    },
    onRender: function () {
      var itemsToolbarView = this.itemsToolbarView || new BaseLayout.ToolbarView({
          model: this.filter,
          paginatorModel: this.paginatorModel
        });
      this.toolbar.show(itemsToolbarView);

      var itemsListView = this.itemsListView || new BaseLayout.ListView({
          collection: this.collection
        });
      this.itemsList.show(itemsListView);

      this.listPaginatorView = new ValamisPaginator({
        language: Valamis.language,
        model: this.paginatorModel,
        topEdgeParentView: this,
        topEdgeSelector: this.regions.toolbar,
        topEdgeOffset: 0
      });
      this.listPaginatorView.on('pageChanged', function () {
        this.fetchCollection()
      }, this);
      this.listPaginator.show(this.listPaginatorView);

      this.fetchCollection(true);
    },
    updatePagination: function (details, context) {
      this.listPaginatorView.updateItems(details.total);
    },
    fetchCollection: function(firstPage, options) {
      options || (options = {});
      if(firstPage)
        this.paginatorModel.set({ currentPage: 1 });

      this.collection.fetch(_.extend({
        reset: true,
        filter: this.filter.toJSON(),
        currentPage: this.paginatorModel.get('currentPage'),
        itemsOnPage: this.paginatorModel.get('itemsOnPage')
      }, options));
    },
    getSelectedItems: function() {
      return BaseLayout.IdsToSelect;
    },
    cleanSelectedItems: function() {
      BaseLayout.IdsToSelect = [];
    }
  });

});