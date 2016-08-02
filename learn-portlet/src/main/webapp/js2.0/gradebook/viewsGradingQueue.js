gradebook.module('Views.GradingQueue', function (GradingQueue, gradebook, Backbone, Marionette, $, _) {

  GradingQueue.GradingItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookGradingItemViewTemplate',
    className: 'js-results',
    templateHelpers: function() {
      var attemptDate = new Date(this.model.get('lastAttemptedDate'));
      return {
        autoGradePercent: Utils.gradeToPercent(this.model.get('autoGrade')),
        formattedDate: gradebook.formatDate(attemptDate),
        formattedTime: gradebook.formatTime(attemptDate),
        isAllCourses: !(gradebook.courseId)
      }
    },
    events: {
      'click .js-expand-results': 'expandResults'
    },
    onShow: function() {
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
        that.triggerMethod('update:collection', that.model.get('user').id, that.model.get('lesson').id);
      });
    },
    expandResults: function() {
      this.triggerMethod('results:expand', this.model.get('user').id, this.model.get('lesson').id);
    }
  });

  GradingQueue.GradingCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookGradingCollectionViewTemplate',
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
      },
      'update:collection': function(childView, userId, lessonId) {
        this.collection.findWhere({'uniqueId': 'attempt_' + userId + '_' + lessonId}).destroy();
        this.collection.findWhere({'uniqueId': 'result_' + userId + '_' + lessonId}).destroy();
        var total = this.collection.total;
        this.collection.total = total - 1;
        this.collection.trigger('update:total');
      }
    }
  });

});