gradebook.module('Views.Overview', function (Overview, gradebook, Backbone, Marionette, $, _) {

  Overview.LastGradingItemView = Marionette.ItemView.extend({   // todo create a base view?
    tagName: 'tr',
    template: '#gradebookLastGradingItemViewTemplate',
    templateHelpers: function() {
      return {
        autoGradePercent: Utils.gradeToPercent(this.model.get('autoGrade'))
      }
    },
    onRender: function() {
      var popoverView = new gradebook.Views.PopoverButtonView({
        buttonText: Valamis.language['gradeLabel'],
        placeholderText: Valamis.language['lessonFeedbackPlaceholderLabel'],
        scoreLimit: Utils.gradeToPercent(this.model.get('lesson').scoreLimit)
      });
      popoverView.on('popover:submit', this.sendGrade, this);
      popoverView.on('popover:open', function (button) {
        this.triggerMethod('popover:button:click', button);
      }, this);
      this.$('.js-grade-button').html(popoverView.render().$el);
    },
    sendGrade: function(data) {
      var lessonGrade = {};
      lessonGrade.grade = data.grade;
      lessonGrade.comment = data.comment;
      this.model.set('teacherGrade', lessonGrade);

      var that = this;
      this.model.setLessonGrade().then(function() {
        that.model.destroy();
      });
    }
  });

  Overview.LastGradingCollectionView = Marionette.CompositeView.extend({
    template: '#gradebookLastGradingCollectionViewTemplate',
    childView: Overview.LastGradingItemView,
    childViewContainer: '.js-items-list',
    onRender: function() {
      this.collection.on('sync', function() {
        this.$('.js-items-list-table').toggleClass('hidden', this.collection.length == 0);
      }, this);
    }
  });

  Overview.CourseStatisticItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookCourseStatisticItemViewViewTemplate'
  });

  Overview.CourseStatisticView = Marionette.CompositeView.extend({
    template: '#gradebookCourseStatisticViewTemplate',
    childView: Overview.CourseStatisticItemView,
    childViewContainer: '.js-items-list'
  });

  Overview.StatisticView = Marionette.ItemView.extend({
    template: '#gradebookOverviewStatisticViewTemplate',
    modelEvents: {
      'change': 'render'
    }
  });

  Overview.CourseOverviewView = Marionette.LayoutView.extend({
    template: '#gradebookOverviewViewTemplate',
    className: 'course-overview',
    regions: {
      'lastGradingRegion': '.js-last-grading',
      'coursesRegion': '.js-courses-list',
      'statisticRegion': '.js-statistic'
    },
    modelEvents: {
      'change:id': 'render'
    },
    onRender: function() {

      if (gradebook.courseId) {
        var statisticView = new Overview.StatisticView({
          model: this.model
        });
        this.statisticRegion.show(statisticView);
      }
      else {
        var courseStatisticView = new Overview.CourseStatisticView({
          collection: this.model.get('statistic')
        });
        this.statisticRegion.show(courseStatisticView);
      }

      var lastGradingCollection = new gradebook.Entities.LastGradingCollection();
      lastGradingCollection.fetch({
        page: 1,
        count: 5
      });

      var lastGradingQueue = new Overview.LastGradingCollectionView({
        collection: lastGradingCollection
      });
      this.lastGradingRegion.show(lastGradingQueue);

    }
  });

  // overview for user

  Overview.UserStatisticView = Marionette.ItemView.extend({
    template: '#gradebookOverviewUserStatisticViewTemplate',
    templateHelpers: function() {
      var teacherGrade = this.model.get('teacherGrade');
      var teacherGradePercent= Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);

      return {
        teacherGradePercent: (teacherGradePercent) ? teacherGradePercent + '%' : '',
        courseFeedback: (teacherGrade) ? teacherGrade.comment : ''
      }
    },
    modelEvents: {
      'change': 'render'
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
      'coursesRegion': '.js-courses-list',
      'statisticRegion': '.js-statistic'
    },
    modelEvents: {
      'change:id': 'render'
    },
    onRender: function() {

      if (gradebook.courseId) {
        var statisticView = new Overview.UserStatisticView({
          model: this.model
        });
        this.statisticRegion.show(statisticView);

        var LastActivityCollection = new gradebook.Entities.LastActivityCollection();
        LastActivityCollection.fetch({
          page: 1,
          count: 5
        });

        var lastActivityView = new Overview.lastActivityCollectionView({
          collection: LastActivityCollection
        });
        this.lastActivityRegion.show(lastActivityView);
      }
      else {
        var courseStatisticView = new Overview.UserCourseStatisticView({
          collection: this.model.get('statistic')
        });
        this.statisticRegion.show(courseStatisticView);
      }
    }
  });

});