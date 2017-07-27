myCourses.module('Views', function (Views, myCourses, Backbone, Marionette, $, _) {

  var ROW_TYPE = myCourses.Entities.ROW_TYPE;

  Views.UsersItemView = Marionette.CompositeView.extend({
    tagName: 'li',
    template: '#myCoursesUsersItemViewTemplate',
    templateHelpers: function () {
      var lessons = this.model.get('lessons');
      var totalLessons = lessons.total;
      var successProgress = (totalLessons) ? Math.floor(lessons.success * 100 / totalLessons) : 0;
      var inProgress = (totalLessons) ? Math.floor(lessons.inProgress * 100 / totalLessons) : 0;
      var notStarted = (totalLessons) ? Math.floor(lessons.notStarted * 100 / totalLessons) : 0;

      return {
        isCompleted: (successProgress === 100),
        successProgress: successProgress + '%',
        inProgress: inProgress + '%',
        notStarted: notStarted + '%'
      }
    }
  });

  Views.RowItemView = Marionette.CompositeView.extend({
    tagName: 'tr',
    childView: Views.UsersItemView,
    childViewContainer: '.js-users-list',
    templateHelpers: function() {
      var templateData = {};

      if (this.model.get('tpe') === ROW_TYPE.COURSE) {
        var progress = Math.floor((this.model.get('completed') / this.model.get('users')) * 100);
        var isSuccess = (progress === 100);

        var colorClass;
        if (progress < 25)
          colorClass = 'failed';
        else if (progress >= 25 && progress < 50)
          colorClass = 'inprogress';
        else
          colorClass = 'success';

        _.extend(templateData, {
          progress: progress + '%',
          isSuccess: isSuccess,
          colorClass: colorClass
        });
      }

      return templateData
    },
    initialize: function() {
      this.isDetails = this.model.get('tpe') === ROW_TYPE.DETAILS;

      if (!this.isDetails)
        this.template = '#myCoursesRowViewTemplate';
      else {
        this.template = '#myCoursesDetailsViewTemplate';
        this.$el.addClass('hidden details');

        this.collection = new myCourses.Entities.UsersCollection();
      }
    },
    onRender: function () {
      if (this.isDetails) {
        var that = this;
        this.$('.js-scroll-div').valamisInfiniteScroll(this.collection, function () {
          that.collection.fetchMore({groupId: that.model.get('courseId')});
        });

      }
    }
  });

  Views.AppLayoutView = Marionette.CompositeView.extend({
    template: '#myCoursesLayoutTemplate',
    childView: Views.RowItemView,
    childViewContainer: '#coursesTable',
    ui: {
      coursesTable: '.js-courses-table',
      showMore: '.js-show-more',
      toggleDetails: '.js-toggle-details',
      noItems: '.js-no-items'
    },
    events: {
      'click @ui.showMore': 'fetchMore',
      'click @ui.toggleDetails': 'toggleDetails'
    },
    onRender: function() {
      this.$('.valamis-tooltip').tooltip();

      this.collection = new myCourses.Entities.CourseCollection([], {itemsPerPage: 5});
      this.collection.on('sync', this.checkUi, this);
      this.collection.fetchMore();
    },
    fetchMore: function () {
      this.collection.fetchMore();
    },
    checkUi: function () {
      this.ui.coursesTable.toggleClass('hidden', !this.collection.hasItems());
      this.ui.noItems.toggleClass('hidden', this.collection.hasItems());
      this.ui.showMore.toggleClass('hidden', !this.collection.hasMore());

      this.checkTableWidth();
    },
    checkTableWidth: function() {  // todo: make it on resize?
      var tableWidth = this.$('.js-courses-table').width();
      var layoutWidth = this.$el.width();
      var diff = tableWidth - layoutWidth;

      var progressColWidth = this.$('.js-courses-table .js-progress-col').width();

      if (diff > 0)
        this.$('.js-courses-table').addClass((diff < progressColWidth) ? 'hide-progress' : 'hide-status');
    },
    toggleDetails: function(e) {
      var targetTr = $(e.target).parents('tr');
      targetTr.toggleClass('open');
      var detailsTr = $(e.target).parents('tr').next('tr');
      detailsTr.toggleClass('hidden');
      this.setCanvas(detailsTr);
    },
    setCanvas: function (detailsTr) {
      var printCanvas = !detailsTr.hasClass('hidden') && detailsTr.find('#canvas-labels span').length == 0
        && detailsTr.find('ul.user-list > li').length > 0;

      if (printCanvas) {

        detailsTr.valamisCanvasBackground(
          detailsTr.find('ul.user-list > li').width(),
          detailsTr.find('.js-scroll-bounded').height()
        );

      }
    }
  });

});