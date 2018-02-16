gradebook.module('Views.UsersList', function (UsersList, gradebook, Backbone, Marionette, $, _) {

  UsersList.AllCoursesUserItemView = Marionette.ItemView.extend({
    template: '#gradebookAllCoursesUserItemViewTemplate',
    tagName: 'tr',
    templateHelpers: function() {
      var activityDate = this.model.get('lastActivityDate');
      var lessons = this.model.get('lessons');
      var courseStatus = Valamis.language['notStartedLabel'];

      var isCourseCompleted = lessons.success === lessons.total;
      var isCourseInProgress = lessons.inProgress > 0;

      if (isCourseCompleted)
        courseStatus = Valamis.language['completedLabel'];
      else if (isCourseInProgress)
        courseStatus = Valamis.language['inProgressLabel'];

      return {
        dateFormatted: (activityDate) ? gradebook.formatDate(new Date(activityDate)) : '',
        courseStatus: courseStatus,
        organizationList: this.model.get('user').organizations.join(', '),
        isCourseCompleted: isCourseCompleted
      }
    },
    events: {
      'click .js-user-name': 'showLessons'
    },
    showLessons: function() {
      this.triggerMethod('user:show:lessons', this.model);
    }
  });

  UsersList.UserItemView = UsersList.AllCoursesUserItemView.extend({
    template: '#gradebookUserItemViewTemplate',
    modelEvents: {
      'change:teacherGrade': 'render'
    },
    onRender: function () {
      var teacherGrade = this.model.get('teacherGrade');
      var grade = Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);
      var popoverView = new gradebook.Views.PopoverButtonView({
        buttonText: (!isNaN(grade)) ? grade + '%' : Valamis.language['gradeLabel'],
        grade: (!isNaN(grade)) ? grade : '',
        comment: (teacherGrade) ? teacherGrade.comment : '',
        placeholderText: Valamis.language['courseFeedbackPlaceholderLabel']
      });
      popoverView.on('popover:submit', this.sendGrade, this);
      popoverView.on('popover:open', function (button) {
        this.triggerMethod('popover:button:click', button);
      }, this);
      this.$('.js-grade-button').html(popoverView.render().$el);
    },
    sendGrade: function(data) {
      var courseGrade = _.clone(this.model.get('teacherGrade') || {});
      courseGrade.grade = data.grade;
      courseGrade.comment = data.comment;
      this.model.set('teacherGrade', courseGrade);
      this.model.setCourseGrade({}, { currentCourseId: gradebook.courseId });
    }
  });

  UsersList.AllCoursesUsersCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookAllCoursesUsersCollectionViewTemplate',
    childView: UsersList.AllCoursesUserItemView,
    childViewContainer: '.js-items-list',
    templateHelpers: function() {
      return {
        courseName: gradebook.courseModel.get('title')
      }
    },
    childEvents: {
      // hide already opened popovers
      'popover:button:click': function(childView, button) {
        this.$('.js-popover-button').not(button).popover('hide');
      }
    }
  });

  UsersList.UsersCollectionView = UsersList.AllCoursesUsersCollectionView.extend({
    template: '#gradebookUsersCollectionViewTemplate',
    childView: UsersList.UserItemView
  });

  UsersList.LessonUserItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookLessonUserItemViewTemplate',
    className: 'js-results',
    templateHelpers: function() {
      var state = this.model.get('state');
      var lessonStatus = (state) ? state.name : 'none';
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));
      return {
        lessonStatus: Valamis.language[lessonStatus + 'StatusLabel'],
        autoGradePercent: (!isNaN(autoGrade)) ? autoGrade + '%' : '',
        organizationList: this.model.get('user').organizations.join(', ')
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

  UsersList.LessonUsersCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookLessonUsersCollectionViewTemplate',
    getChildView: function(model) {
      if (model.get('type') == 'result')
        return UsersList.LessonUserItemView;
      else
        return gradebook.Views.AttemptsList.AttemptsCollectionView;
    },
    childViewOptions: {
      isUser: true
    },
    childViewContainer: '.js-items-list',
    templateHelpers: function() {
      var averageGrade = Utils.gradeToPercent(this.model.get('averageGrade'));

      return {
        averageGradePercent: (!isNaN(averageGrade)) ? (averageGrade + '%') : '',
        courseId: Utils.getCourseId(),
        imageApi: path.api.packages,
        isAllCourses: !(gradebook.courseId)
      }
    },
    childEvents: {
      // hide already opened popover buttons
      'popover:button:click': function(childView, button) {
          this.$('.js-popover-button').not(button).popover('hide');
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
    }
  });

});