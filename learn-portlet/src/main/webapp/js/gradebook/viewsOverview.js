gradebook.module('Views.Overview', function (Overview, gradebook, Backbone, Marionette, $, _) {

  Overview.LastGradingItemView = Marionette.ItemView.extend({   // todo create a base view?
    tagName: 'tr',
    template: '#gradebookLastGradingItemViewTemplate',
    templateHelpers: function() {
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));
      return {
        autoGradePercent: (!isNaN(autoGrade)) ? autoGrade + '%' : ''
      }
    },
    onRender: function() {
      var teacherGrade = this.model.get('teacherGrade');
      var popoverView = new gradebook.Views.PopoverButtonView({
        comment: (teacherGrade) ? teacherGrade.comment : '',
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
        if (!!lessonGrade.grade) that.model.destroy();
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

  Overview.StatisticView = Marionette.ItemView.extend({
    template: '#gradebookOverviewStatisticViewTemplate'
  });

  Overview.CourseOverviewView = Marionette.LayoutView.extend({
    template: '#gradebookOverviewViewTemplate',
    className: 'course-overview',
    regions: {
      'lastGradingRegion': '.js-last-grading',
      'statisticRegion': '.js-statistic'
    },
    onRender: function() {
      var statisticView = new Overview.StatisticView({
        model: this.model
      });
      this.statisticRegion.show(statisticView);

      var lastGradingCollection = new gradebook.Entities.LastGradingCollection();
      lastGradingCollection.fetch({
        page: 1,
        count: 5,
        currentCourseId: gradebook.courseId
      });

      var lastGradingQueue = new Overview.LastGradingCollectionView({
        collection: lastGradingCollection
      });
      this.lastGradingRegion.show(lastGradingQueue);
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

  Overview.AllCoursesOverviewView = Marionette.LayoutView.extend({
    template: '#gradebookOverviewViewTemplate',
    className: 'course-overview',
    regions: {
      'lastGradingRegion': '.js-last-grading',
      'statisticRegion': '.js-statistic'
    },
    onRender: function() {
      var courseStatisticView = new Overview.CourseStatisticView({
        collection: this.model.get('statistic')
      });
      this.statisticRegion.show(courseStatisticView);

      var lastGradingCollection = new gradebook.Entities.LastGradingCollection();
      lastGradingCollection.fetch({
        page: 1,
        count: 5,
        currentCourseId: gradebook.courseId
      });

      var lastGradingQueue = new Overview.LastGradingCollectionView({
        collection: lastGradingCollection
      });
      this.lastGradingRegion.show(lastGradingQueue);
    }
  });

});