valamisApp.module('Views.SelectLesson', function (SelectLesson, valamisApp, Backbone, Marionette, $, _) {

  var SEARCH_TIMEOUT = 800;

  SelectLesson.LessonsToolbarView = Marionette.ItemView.extend({
    template: '#lessonSelectToolbarViewTemplate',
    templateHelpers: function() {
      return {
        categories: this.options.categories
      }
    },
    behaviors: {
      ValamisUIControls: {}
    },
    events: {
      'keyup .js-search': 'changeSearchText',
      'click .js-categories-filter .dropdown-menu > li': 'changeCategory',
      'click .js-sort-filter .dropdown-menu > li': 'changeSort'
    },
    changeSearchText:function(e){
      var that = this;
      clearTimeout(this.inputTimeout);
      this.inputTimeout = setTimeout(function(){
        that.model.set('searchtext', $(e.target).val());
      }, SEARCH_TIMEOUT);
    },
    changeCategory: function(e){
      this.model.set('tagId', $(e.target).attr('data-value'));
    },
    changeSort: function(e){
      this.model.set('sort', $(e.target).attr('data-value'));
    }
  });

  SelectLesson.LessonsListItemView = Marionette.ItemView.extend({
    template: '#lessonSelectListItemViewTemplate',
    tagName: 'tr',
    events: {
      'click .js-select-lesson': 'selectGoal'
    },
    selectGoal: function() {
      this.model.toggle();
      this.$('.js-select-lesson').toggleClass('primary', this.model.get('selected'));
      this.$('.js-select-lesson').toggleClass('neutral', !this.model.get('selected'));
    }
  });

  SelectLesson.LessonsListView = Marionette.CompositeView.extend({
    template: '#lessonSelectListViewTemplate',
    childView: SelectLesson.LessonsListItemView,
    childViewContainer: '.js-lessons-list',
    initialize: function() {
      var that = this;
      this.collection.on('sync', function() {
        that.$('.js-no-lessons-label').toggleClass('hidden', that.collection.length !== 0);
        that.$('.js-lessons-list').toggleClass('hidden', that.collection.length === 0);
      })
    }
  });

  SelectLesson.LessonsSelectLayoutView = Marionette.LayoutView.extend({
    template: '#lessonSelectLayoutViewTemplate',
    regions: {
      'lessonsToolbar': '#lessonsListToolbar',
      'lessonsList': '#lessonsList',
      'lessonsPaginator': '#lessonsListPaginator',
      'lessonsPaginatorShowing': '#lessonsListPaginatorShowing'
    },
    initialize: function(options) {
      var that = this;
      this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

      this.lessonsCollection = new valamisApp.Entities.LessonCollection();
      this.lessonsCollection.on('lessonCollection:updated', function (details) {
        that.updatePagination(details);
      });

      this.lessonsFilter = new valamisApp.Entities.Filter({
        'packageType': options.packageType,
        'scope': options.scope,
        'playerId': options.playerId,
        'action': options.action
      });

      this.lessonsFilter.on('change', function() {
        that.fetchLessonsCollection(true);
      });
    },
    onRender: function() {
      this.categories =  new Valamis.TagCollection();
      this.categories.on('sync', this.showToolbar, this);
      this.categories.fetch();

      var lessonsListView = new SelectLesson.LessonsListView({

        collection: this.lessonsCollection,
        paginatorModel: this.paginatorModel
      });
      this.lessonsList.show(lessonsListView);

      this.lessonsPaginatorView = new ValamisPaginator({
        language: Valamis.language,
        model : this.paginatorModel
      });
      this.lessonsPaginatorView.on('pageChanged', function () {
        this.fetchLessonsCollection()
      }, this);
      this.lessonsPaginator.show(this.lessonsPaginatorView);

      this.fetchLessonsCollection(true);
    },
    showToolbar: function() {
      var lessonsToolbarView = new SelectLesson.LessonsToolbarView({
        model: this.lessonsFilter,
        categories: this.categories.toJSON()
      });
      this.lessonsToolbar.show(lessonsToolbarView);

      var lessonsPaginatorShowingView = new ValamisPaginatorShowing({
        language: Valamis.language,
        model: this.paginatorModel
      });
      this.lessonsPaginatorShowing.show(lessonsPaginatorShowingView);
    },
    updatePagination: function (details, context) {
      this.lessonsPaginatorView.updateItems(details.total);
    },
    fetchLessonsCollection: function(firstPage) {
      if(firstPage) {
        this.paginatorModel.set('currentPage', 1);
      }

      this.lessonsCollection.fetch({
        reset: true,
        filter: this.lessonsFilter.toJSON(),
        currentPage: this.paginatorModel.get('currentPage'),
        itemsOnPage: this.paginatorModel.get('itemsOnPage')
      });
    },
    getSelectedLessons: function() {
      return this.lessonsCollection.filter(function (item) {
        return item.get('selected');
      }).map(function (item) {
        return item.get('id');
      });
    }
  });

});