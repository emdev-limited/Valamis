gradebook.module('Views.LessonsList', function (LessonsList, gradebook, Backbone, Marionette, $, _) {

  LessonsList.LessonItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookLessonItemViewTemplate',
    templateHelpers: function() {
      var averageGrade = Utils.gradeToPercent(this.model.get('averageGrade'));

      return {
        averageGradePercent: (!isNaN(averageGrade)) ? (averageGrade + '%') : '',
        courseId: Utils.getCourseId(),
        imageApi: path.api.packages,
        isAllCourses: !(gradebook.courseId)
      }
    },
    events: {
      'click .js-lesson-name': 'showLesson'
    },
    showLesson: function() {
      this.triggerMethod('lesson:show:users', this.model);
    }
  });

  LessonsList.LessonsCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookLessonsCollectionViewTemplate',
    childView: LessonsList.LessonItemView,
    childViewContainer: '.js-items-list',
    templateHelpers: function() {
      return {
        courseName: gradebook.courseModel.get('title')
      }
    }
  });

  LessonsList.UserLessonItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookUserLessonItemViewTemplate',
    className: 'js-results',
    templateHelpers: function() {
      var state = this.model.get('state');
      var lessonStatus = (state) ? state.name : 'none';
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));
      return {
        courseId: Utils.getCourseId(),
        imageApi: path.api.packages,
        lessonStatus: Valamis.language[lessonStatus + 'StatusLabel'],
        autoGradePercent: (!isNaN(autoGrade)) ? autoGrade + '%' : '',
        isAllCourses: !(gradebook.courseId)
      }
    },
    events: {
      'click .js-expand-results': 'expandResults'
    },
    modelEvents: {
      'change:teacherGrade': 'render',
      'change:state': 'render'
    },
    onRender: function() {
      var teacherGrade = this.model.get('teacherGrade');
      var grade = Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);
      var popoverView = new gradebook.Views.PopoverButtonView({
        buttonText: (!isNaN(grade)) ? grade + '%' : Valamis.language['gradeLabel'],
        grade: (!isNaN(grade)) ? grade : '',
        comment: (teacherGrade) ? teacherGrade.comment : '',
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
      var lessonGrade = _.clone(this.model.get('teacherGrade') || {});
      lessonGrade.grade = data.grade;
      lessonGrade.comment = data.comment;
      this.model.set('teacherGrade', lessonGrade);

      var that = this;
      this.model.setLessonGrade().then(function(result) {
        if (result) {
          that.model.set('state', result.state);
        }
      });
    },
    expandResults: function() {
      this.triggerMethod('results:expand', this.model.get('user').id, this.model.get('lesson').id);
    }
  });

  LessonsList.UserLessonsCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookUserLessonsCollectionViewTemplate',
    getChildView: function(model) {
      if (model.get('type') == 'result')
        return LessonsList.UserLessonItemView;
      else
        return gradebook.Views.AttemptsList.AttemptsCollectionView;
    },
    childViewOptions: {
      isLesson: true
    },
    childViewContainer: '.js-items-list',
    childEvents: {
      // hide already opened popover buttons
      'popover:button:click': function(childView, button) {
        this.closePopovers(button);
      },
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
    templateHelpers: function() {
      return {
        organizationList: this.model.get('user').organizations.join(', ')
      }
    },
    modelEvents: {
      'change:teacherGrade': 'renderPopover'
    },
    onRender: function() {
      if (gradebook.courseModel.get('id')) {
        this.renderPopover();
      }
    },
    renderPopover: function() {
      var teacherGrade = this.model.get('teacherGrade');
      var grade = Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);
      var popoverView = new gradebook.Views.PopoverButtonView({
        buttonText: Valamis.language['totalGradeLabel'] + ' ' + ((!isNaN(grade)) ? grade + '%' : ''),
        grade: (!isNaN(grade)) ? grade : '',
        comment: (teacherGrade) ? teacherGrade.comment : '',
        placeholderText: Valamis.language['courseFeedbackPlaceholderLabel']
      });
      popoverView.on('popover:submit', this.sendGrade, this);
      popoverView.on('popover:open', function (button) {
        this.closePopovers(button)
      }, this);
      this.$('.js-items-header .js-grade-button').html(popoverView.render().$el);
    },
    closePopovers: function(button) {
      this.$('.js-popover-button').not(button).popover('hide');
    },
    sendGrade: function(data) {
      var teacherGrade = _.clone(this.model.get('teacherGrade') || {});
      teacherGrade.grade = data.grade;
      teacherGrade.comment = data.comment;
      this.model.set('teacherGrade', teacherGrade);
      this.model.setCourseGrade({}, { currentCourseId: gradebook.courseId });
    }
  });

  // student views

  LessonsList.CurrentUserLessonItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookCurrentUserLessonItemViewTemplate',
    className: 'js-results',
    templateHelpers: function() {
      var state = this.model.get('state');
      var lessonStatus = (state) ? state.name : 'none';
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));
      var gradeModel = this.model.get('teacherGrade');
      var teacherGrade = Utils.gradeToPercent((gradeModel) ? gradeModel.grade : undefined);
      return {
        courseId: Utils.getCourseId(),
        imageApi: path.api.packages,
        lessonStatus: Valamis.language[lessonStatus + 'StatusLabel'],
        autoGradePercent: (!isNaN(autoGrade)) ? autoGrade + '%' : '',
        teacherGradePercent: (!isNaN(teacherGrade)) ? teacherGrade + '%' : '',
        isAllCourses: !(gradebook.courseId)
      }
    },
    events: {
      'click .js-expand-results': 'expandResults'
    },
    expandResults: function() {
      this.triggerMethod('results:expand', this.model.get('user').id, this.model.get('lesson').id);
    }
  });

  LessonsList.CurrentUserLessonsCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookCurrentUserLessonsCollectionViewTemplate',
    getChildView: function(model) {
      if (model.get('type') == 'result')
        return LessonsList.CurrentUserLessonItemView;
      else
        return gradebook.Views.AttemptsList.AttemptsCollectionView;
    },
    childViewOptions: {
      isLesson: true
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
    }
  });
});