gradebook.module('Views.AttemptsList', function (AttemptsList, gradebook, Backbone, Marionette, $, _) {

  AttemptsList.CommentsItemView = Marionette.ItemView.extend({
    template: '#gradebookCommentsItemViewTemplate',
    className: 'item-info',
    templateHelpers: function() {
      var stmntDate = new Date(this.model.get('date'));
      return {
        formattedDate: gradebook.formatDate(stmntDate),
        formattedTime: gradebook.formatTime(stmntDate)
      }
    }
  });

  AttemptsList.StatementItemView = Marionette.CompositeView.extend({
    tagName: 'tr',
    template: '#gradebookStatementItemViewTemplate',
    templateHelpers: function() {
      var stmntDate = new Date(this.model.get('date'));
      var comments = this.collection;
      var isAnswer = _.contains(this.model.get('verb'), 'answered');
      var statementTitle = (isAnswer) ? this.model.get('description')
        : Utils.getLangDictionaryTincanValue(this.model.get('verbName'));

      var attempts = this.model.collection.where({ isAttempt: true });
      var attemptIndex = this.model.collection.total - attempts.indexOf(this.model);

      var score = this.model.get('score');

      return {
        statementTitle: statementTitle,
        isAnswer: isAnswer,
        isEssay: isAnswer && _.contains(this.model.get('questionType'), 'long-fill-in'),   // todo do not use constant here
        attemptIndex: attemptIndex,
        formattedDate: gradebook.formatDate(stmntDate),
        formattedTime: gradebook.formatTime(stmntDate),
        commentsCount: (comments) ? comments.length : '',
        attemptScore: (score != undefined && !isAnswer) ? (Utils.gradeToPercent(score) + '%') : '',
        attemptDuration: this.model.get('duration') || '',
        currentUser: gradebook.userModel.toJSON()
      }
    },
    childView: AttemptsList.CommentsItemView,
    childViewContainer: '.js-statement-comments',
    events: {
      'click .js-show-comments': 'toggleComments',
      'click .js-hide-comments': 'toggleComments',
      'focus .js-my-comment-field': function() {this.$('.js-send-comment').show();},
      'blur .js-my-comment-field': function() {this.$('.js-send-comment').hide();},
      'keypress .js-my-comment-field': 'keyAction'
    },
    initialize: function() {
      this.collection = new gradebook.Entities.StatementsCollection(this.model.get('comments'));
    },
    onRender: function() {
      if (this.model.get('isAttempt')) this.$el.addClass('attempt');

      var that = this;
      this.$('.js-send-comment').on('mousedown', function(event) {
        event.preventDefault();
      }).on('click', function() { that.sendComment(); });
    },
    toggleComments: function() {
      this.$('.js-statement-comments').toggleClass('collapsed');
      this.$('.js-new-comment').toggleClass('hidden');
      this.$('.js-show-comments').toggleClass('hidden');
      this.$('.js-hide-comments').toggleClass('hidden');
    },
    keyAction: function(e) {
      if(e.keyCode === 13) {
        this.sendComment();
      }
    },
    sendComment: function() {
      var comment =  this.$('.js-my-comment-field').val();

      var stmt = new TinCan.Statement({
        actor: new TinCan.Agent(JSON.parse(gradebook.tincanActor)),
        verb: TincanHelper.createVerb('commented'),
        target: new TinCan.StatementRef({ objectType:'StatementRef', id: this.model.get('id') }),
        context: new TinCan.Context({ contextActivities: { grouping: [{
          id: this.model.get('contextActivity'),
          objectType: 'Activity'
        }]}}),
        result: new TinCan.Result({ response: comment })
      });

      TincanHelper.sendStatement(stmt);

      this.model.sendNotification({}, {
        userId: this.options.userId,
        lessonTitle: this.options.lessonTitle
      });

      this.collection.add([{
        verb: TincanHelper.createVerb('commented').id,
        user: gradebook.userModel.toJSON(),
        userResponse: comment,
        date: new Date()
      }]);

      this.render();
      this.toggleComments();
    }
  });

  AttemptsList.AttemptsCollectionView = gradebook.Views.ItemsCollectionView.extend({
    ui: {
      loadingContainer: '> td .js-loading-container',
      itemsListTable: '> td .js-items-list-table',
      showMore: '> td .js-show-more',
      itemsTotal: '> td .js-items-total'
    },
    tagName: 'tr',
    template: '#gradebookAttemptsCollectionViewTemplate',
    className: 'hidden lesson-attempts js-attempts',
    templateHelpers: function() {
      var state = this.model.get('state');
      var lessonStatus = (state) ? state.name : 'none';
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));

      var params = {
        courseId: Utils.getCourseId(),
        imageApi: path.api.packages,
        lessonStatus: Valamis.language[lessonStatus + 'StatusLabel'],
        autoGradePercent: (!isNaN(autoGrade)) ? autoGrade + '%' : '',
        isLesson: this.options.isLesson,
        isUser: this.options.isUser,
        canViewAll: gradebook.canViewAll
      };

      // for user view
      if (!gradebook.canViewAll) {
        var gradeModel = this.model.get('teacherGrade');
        var teacherGrade = (gradeModel) ? gradeModel.grade : undefined;
        _.extend(params, {
          teacherGradePercentForUser: Utils.gradeToPercent(teacherGrade),
          lessonFeedbackForUser: (gradeModel) ? gradeModel.comment : ''
        });
      }

      return params;
    },
    childView: AttemptsList.StatementItemView,
    childViewOptions: function() {
      return {
        userId: this.model.get('user').id,
        lessonTitle: this.model.get('lesson').title
      }
    },
    childViewContainer: '.js-attempts-list',
    modelEvents: {
      'change:teacherGrade': 'render',
      'change:state': 'render',
      'getStatements': 'getStatements'
    },
    events: {
      'click .js-collapse-results': function() { this.triggerMethod('results:collapse'); }
    },
    initialize: function() {
      this.collection = new gradebook.Entities.StatementsCollection([], { itemsPerPage: 3 });

      this.events = _.extend({}, this.constructor.__super__.events, this.events);
      this.constructor.__super__.initialize.apply(this, arguments);
    },
    onRender: function() {
      var teacherGrade = this.model.get('teacherGrade');
      var grade = Utils.gradeToPercent((teacherGrade) ? teacherGrade.grade : undefined);
      var popoverView = new gradebook.Views.PopoverButtonView({
        buttonText: Valamis.language['gradeLabel'] + ' ' + ((!isNaN(grade)) ? grade + '%' : ''),
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

      if (this.collection.length > 0) {
        this.$('.js-loading-container').addClass('hidden');
        this.$('.js-items-total').text(this.collection.total);
      }
    },
    sendGrade: function(data) {
      var lessonGrade = _.clone(this.model.get('teacherGrade') || {});
      lessonGrade.grade = data.grade;
      lessonGrade.comment = data.comment;
      this.model.set('teacherGrade', lessonGrade);

      var that = this;
      this.model.setLessonGrade({}, { userId: this.options.userId }).then(function(result) {
        if (result) {
          that.model.set('state', result.state);
        }
      });
    },
    getStatements: function() {
      if (this.collection.length == 0) {
        this.collection.lessonId = this.model.get('lesson').id;
        this.collection.userId = this.model.get('user').id;
        this.collection.fetchMore({ currentCourseId: gradebook.courseId });
      }
    }
  });

});