gradebook.module('Views.Overview', function (Overview, gradebook, Backbone, Marionette, $, _) {

  Overview.UserStatisticView = Marionette.ItemView.extend({
    template: '#gradebookOverviewUserStatisticViewTemplate',
    templateHelpers: function() {
      var teacherGrade = this.model.get('teacherGrade');
      var teacherGradePercent= Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);

      return {
        teacherGradePercent: (teacherGradePercent) ? teacherGradePercent + '%' : '',
        courseFeedback: (teacherGrade) ? teacherGrade.comment : ''
      }
    }
  });

  Overview.LastActivityItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookLastActivityItemViewTemplate',
    templateHelpers: function() {
      var autoGradePercent = Utils.gradeToPercent(this.model.get('autoGrade'));
      var teacherGrade = this.model.get('teacherGrade');
      var teacherGradePercent= Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);
      var state = this.model.get('state');
      var lessonStatus = (state) ? state.name : 'none';
      return {
        imageApi: path.api.packages,
        autoGradePercent: (autoGradePercent) ? autoGradePercent + '%' : '',
        teacherGradePercent: (teacherGradePercent) ? teacherGradePercent + '%' : '',
        lessonStatus: Valamis.language[lessonStatus + 'StatusLabel'],
        courseId: Utils.getCourseId()
      }
    }
  });

  Overview.lastActivityCollectionView = Marionette.CompositeView.extend({
    template: '#gradebookLastActivityCollectionViewTemplate',
    childView: Overview.LastActivityItemView,
    childViewContainer: '.js-items-list',
    onRender: function() {
      this.collection.on('sync', function() {
        this.$('.js-items-list-table').toggleClass('hidden', this.collection.length == 0);
      }, this);
    }
  });

  Overview.UserCourseOverviewView = Marionette.LayoutView.extend({
    template: '#gradebookUserOverviewViewTemplate',
    className: 'course-overview',
    regions: {
      'lastActivityRegion': '.js-last-activity',
      'statisticRegion': '.js-statistic'
    },
    onRender: function() {
      var statisticView = new Overview.UserStatisticView({
        model: this.model
      });
      this.statisticRegion.show(statisticView);

      var LastActivityCollection = new gradebook.Entities.LastActivityCollection();
      LastActivityCollection.fetch({
        page: 1,
        count: 5,
        currentCourseId: gradebook.courseId
      });

      var lastActivityView = new Overview.lastActivityCollectionView({
        collection: LastActivityCollection
      });
      this.lastActivityRegion.show(lastActivityView);
    }
  });

  Overview.UserCourseStatisticItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookUserCourseStatisticItemViewViewTemplate',
    templateHelpers: function() {
      var courseStatus = Valamis.language['notStartedLabel'];
      var lessons = this.model.get('lessons');
      var isCourseCompleted = lessons.success === lessons.total;
      var isCourseInProgress = lessons.inProgress > 0;

      if (isCourseCompleted)
        courseStatus = Valamis.language['completedLabel'];
      else if (isCourseInProgress)
        courseStatus = Valamis.language['inProgressLabel'];

      var teacherGrade = this.model.get('teacherGrade');
      var teacherGradePercent= Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);

      return {
        courseStatus: courseStatus,
        teacherGradePercent: (teacherGradePercent) ? teacherGradePercent + '%' : ''
      }
    }
  });

  Overview.UserCourseStatisticView = Marionette.CompositeView.extend({
    template: '#gradebookUserCourseStatisticViewTemplate',
    childView: Overview.UserCourseStatisticItemView,
    childViewContainer: '.js-items-list'
  });

  Overview.UserAllCoursesOverviewView = Marionette.LayoutView.extend({
    template: '#gradebookUserOverviewViewTemplate',
    className: 'course-overview',
    regions: {
      'lastActivityRegion': '.js-last-activity',
      'statisticRegion': '.js-statistic'
    },
    onRender: function () {
      var courseStatisticView = new Overview.UserCourseStatisticView({
        collection: this.model.get('statistic')
      });
      this.statisticRegion.show(courseStatisticView);
    }
  });

});