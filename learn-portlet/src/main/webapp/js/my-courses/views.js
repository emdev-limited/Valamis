myCourses.module('Views', function (Views, myCourses, Backbone, Marionette, $, _) {

  var ROW_TYPE = {
    DETAILS: 'details',
    COURSE: 'course'
  };

  var USERS_COUNT = 10;

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
      if (this.model.get('tpe') === ROW_TYPE.COURSE)
        this.template = '#myCoursesRowViewTemplate';
      else {
        this.template = '#myCoursesDetailsViewTemplate';
        this.$el.addClass('hidden');

        this.collection = new myCourses.Entities.UsersCollection();
      }
    },
    onRender: function () {
      if (this.model.get('tpe') === ROW_TYPE.DETAILS) {
        var fetchedCollection = new myCourses.Entities.UsersCollection();

        fetchedCollection.on('sync', function () {
          this.collection.add(fetchedCollection.toJSON());
        }, this);

        this.$('.js-scroll-div').valamisInfiniteScroll(fetchedCollection, {
          count: USERS_COUNT,
          groupId: this.model.get('courseId')
        });
      }
    }
  });

  Views.AppLayoutView = Marionette.CompositeView.extend({
    template: '#myCoursesLayoutTemplate',
    childView: Views.RowItemView,
    childViewContainer: '#coursesTable',
    events: {
      'click .js-show-more': 'takeCourses',
      'click .js-toggle-details': 'toggleDetails'
    },
    initialize: function() {
      this.page = 0;

      this.collection = new myCourses.Entities.CourseCollection();
      this.fetchedCollection = new myCourses.Entities.CourseCollection();

      this.fetchedCollection.on('courseCollection:updated', function(details) {
        this.$('.js-courses-table').toggleClass('hidden', details.total == 0);
        this.$('.js-no-items').toggleClass('hidden', details.total > 0);
        this.$('.js-show-more').toggleClass('hidden', this.page * details.count >= details.total);
      }, this);
    },
    onRender: function() {
      this.$('.valamis-tooltip').tooltip();
      this.takeCourses();
    },
    checkTableWidth: function() {  // todo: make it on resize?
      var tableWidth = this.$('.js-courses-table').width();
      var layoutWidth = this.$el.width();
      var diff = tableWidth - layoutWidth;

      var progressColWidth = this.$('.js-courses-table .js-progress-col').width();

      if (diff > 0)
        this.$('.js-courses-table').addClass((diff < progressColWidth) ? 'hide-progress' : 'hide-status');
    },
    takeCourses: function() {
      this.page++;

      var that = this;
      this.fetchedCollection.fetch({
        page: this.page,
        success: function() {
          that.fetchedCollection.each(function(item) {
            that.collection.add(_.extend({tpe: ROW_TYPE.COURSE}, item.toJSON()));
            that.collection.add({tpe: ROW_TYPE.DETAILS, courseId: item.get('id')});
          });
          that.checkTableWidth();
        }
      });
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