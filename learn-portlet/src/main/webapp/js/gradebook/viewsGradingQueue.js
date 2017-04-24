gradebook.module('Views.GradingQueue', function (GradingQueue, gradebook, Backbone, Marionette, $, _) {

  GradingQueue.GradingItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookGradingItemViewTemplate',
    className: 'js-results',
    templateHelpers: function() {
      var attemptDate = new Date(this.model.get('lastAttemptedDate'));
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));
      return {
        autoGradePercent: (!isNaN(autoGrade)) ? autoGrade + '%' : '',
        formattedDate: gradebook.formatDate(attemptDate),
        formattedTime: gradebook.formatTime(attemptDate),
        isAllCourses: !(gradebook.courseId)
      }
    },
    events: {
      'click .js-expand-results': 'expandResults'
    },
    onShow: function() {
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
        that.triggerMethod('update:collection', that.model.get('user').id, that.model.get('lesson').id);
      });
    },
    expandResults: function() {
      this.triggerMethod('results:expand', this.model.get('user').id, this.model.get('lesson').id);
    }
  });

  GradingQueue.GradingCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookGradingCollectionViewTemplate',
    templateHelpers: function() {
      return { courseName: gradebook.courseModel.get('title') }
    },
    getChildView: function(model) {
      if (model.get('type') == 'result')
        return GradingQueue.GradingItemView;
      else
        return gradebook.Views.AttemptsList.AttemptsCollectionView;
    },
    childViewOptions: {
      isUser: true
    },
    childViewContainer: '.js-items-list',
    childEvents: {
      'results:expand': function(childView, userId, lessonId) {
        this.collection.findWhere({'uniqueId': 'attempt_' + userId + '_' + lessonId}).trigger('getStatements');
        childView.$el.addClass('hidden');
        childView.$el.next('.js-attempts').removeClass('hidden');
      },
      'results:collapse': function(childView) {
        childView.$el.addClass('hidden');
        childView.$el.prev('.js-results').removeClass('hidden');
      }
    },
    initialize: function() {
      this.constructor.__super__.initialize.apply(this, arguments);

      // delete models from grading collection when teacher grade was set
      this.collection.on('change:teacherGrade', function(model) {
        if (!!model.get('teacherGrade').grade) this.collection.remove(model);
      }, this);
    }
  });

});