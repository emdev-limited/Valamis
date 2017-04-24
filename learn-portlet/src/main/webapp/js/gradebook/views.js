gradebook.module('Views', function (Views, gradebook, Backbone, Marionette, $, _) {

  var SEARCH_TIMEOUT = 800;

  var NAVIGATION_TYPE = {
    COURSE: 'course',
    LESSON: 'lesson',
    USER: 'user',
    ASSIGNMENT: 'assignment',
    QUEUE: 'queue'
  };

  Views.BreadcrumbItemView = Marionette.ItemView.extend({
    template: '#gradebookBreadcrumbItemTemplate',
    className: 'display-inline',
    ui: {
      section: '.js-section',
      separator: '.js-separator'
    },
    modelEvents: {
      'change': 'render'
    },
    events: {
      'click @ui.section': 'navigateTo'
    },
    onRender: function() {
      var isFirstModel = this.model.collection.indexOf(this.model) === 0;
      this.$(this.ui.separator).toggleClass('hidden', isFirstModel);
    },
    navigateTo: function () {
      this.triggerMethod('breadcrumb:navigate', this.model.get('section'));
    }
  });

  Views.BreadcrumbsView = Marionette.CompositeView.extend({
    template: '#gradebookBreadcrumbsTemplate',
    templateHelpers: function() {
      return { filterText: this.collection.filterText }
    },
    className: 'div-table',
    ui: {
      search: '.js-search'
    },
    childView: Views.BreadcrumbItemView,
    childViewContainer: '.js-breadcrumbs-container',
    behaviors: {
      ValamisUIControls: {}
    },
    events: {
      'keyup @ui.search': 'changeSearchText'
    },
    changeSearchText: function(e) {
      var that = this;
      clearTimeout(this.inputTimeout);
      this.inputTimeout = setTimeout(function(){
        var filterText = $(e.target).val();
        that.triggerMethod('breadcrumb:search', filterText);
        that.collection.last().set({ filter: filterText });
      }, SEARCH_TIMEOUT);
    }
  });

  Views.CollectionLayoutView = Marionette.LayoutView.extend({
    template: '#gradebookCollectionLayoutTemplate',
    regions: {
      'breadcrumbsRegion': '.js-breadcrumbs',
      'collectionRegion': '.js-collection'
    },
    childEvents: {
      'breadcrumb:search': function(childView, filterText) {
        this.fetchCollection(filterText);
      },
      'breadcrumb:navigate': function(childView, section) {
        this.triggerMethod('breadcrumb:navigate', section);
      },
      'user:show:lessons': function (childView, userModel) {
        this.triggerMethod('user:show:lessons', userModel);
      },
      'lesson:show:users': function (childView, lessonModel) {
        this.triggerMethod('lesson:show:users', lessonModel);
      },
      'assignments:show:users': function (childView, assignmentModel) {
        this.triggerMethod('assignments:show:users', assignmentModel);
      }
    },
    initialize: function() {
      if (this.options.contentView) {
        this.contentView = this.options.contentView;
        this.contentCollection = this.contentView.collection;
      }
    },
    onRender: function() {
      var breadcrumbsView = new Views.BreadcrumbsView({
        collection: gradebook.navigationCollection
      });
      this.breadcrumbsRegion.show(breadcrumbsView);

      this.collectionRegion.show(this.contentView);
      this.fetchCollection(gradebook.navigationCollection.filterText || '');
    },
    fetchCollection: function(filterText) {
      this.contentCollection.reset();
      var params = {
        firstPage: true,
        currentCourseId: gradebook.courseId,
        filter: filterText || ''
      };
      this.contentCollection.fetchMore(params);
    }
  });

  Views.CoursesListView = Marionette.ItemView.extend({
    template: '#gradebookCoursesCollectionViewTemplate',
    className: 'tab-side',
    events: {
      'click li': 'selectCourse'
    },
    templateHelpers: function() {
      return {
        courses: this.options.courses
      }
    },
    behaviors: {
      ValamisUIControls: {}
    },
    onValamisControlsInit: function() {
      this.$('.js-courses-list').valamisDropDown('select', gradebook.courseId);
    },
    selectCourse: function(e) {
      gradebook.execute('gradebook:course:changed', $(e.target).data('value'));
    }
  });

  Views.PopoverButtonView = Marionette.ItemView.extend({
    template: '#gradebookSetGradePopoverButtonTemplate',
    className: 'popover-region',
    events: {
      'keyup .js-grade-comment': 'toggleSend',
      'click .js-cancel-button': 'closePopover',
      'click .js-submit-button': 'submitGrade',
      'click .js-popover-button': 'openPopover'
    },
    templateHelpers: function() {
      return {
        buttonText: this.options.buttonText
      }
    },
    onRender: function () {
      var that = this;
      this.$('.js-popover-button').popover({
        placement: 'bottom',
        content: 'content',
        template: Mustache.to_html($('#gradebookSetGradePopoverTemplate').html(), {
          comment: this.options.comment,
          scoreLimit: this.options.scoreLimit,
          placeholderText: this.options.placeholderText,
          submitButtonText: Valamis.language['sendButtonLabel'],
          cancelButtonText: Valamis.language['cancelButtonLabel'],
          passingScoreLabel: Valamis.language['passingScoreLabel']
        })
      }).on('shown.bs.popover', function () {
        var elem = that.$('.js-grade-input');
        elem.valamisDigitsOnly({
          callback_func: function() { that.toggleSend() }
        });

        elem.val(that.options.grade);  // for cursor after last character
        elem.focus();
      }).on('hidden.bs.popover', function (e) {
        // fix for bootstrap popover bug: need click twice after hide a shown popover
        $(e.target).data('bs.popover').inState = { click: false, hover: false, focus: false }
      });
    },
    toggleSend: function () {
      var value = this.$('.js-grade-input').val();
      var grade = parseFloat(value);
      var comment = this.$('.js-grade-comment').val();

      var checkGrade = function() {
        return !isNaN(grade) && grade >= 0 && grade <= 100;
      };

      var isValid = false;
      var prevGrade = this.options.grade;

      // do not allow to unset grade
      // so if previous grade exists, new grade must be set and be valid
      // if not - check new grade only if user set smth into grade field, otherwise check comment
      if (!isNaN(prevGrade) && prevGrade !== '') {
        isValid = checkGrade();
      }
      else {
        isValid = (grade !== '') ? checkGrade() : comment != '' && comment != this.options.comment;
      }
      this.$('.js-submit-button').attr('disabled', !isValid);
    },
    openPopover: function(e) {
      this.trigger('popover:open', e.target);
    },
    closePopover: function () {
      this.$('.js-popover-button').popover('hide');
    },
    submitGrade: function () {
      this.$('.js-popover-button').popover('hide');
      var grade = parseFloat(this.$('.js-grade-input').val());
      this.trigger('popover:submit', {
        grade: !isNaN(grade) ? grade / 100 : NaN,
        comment: this.$('.js-grade-comment').val()
      });
    }
  });

  Views.ItemsCollectionView = Marionette.CompositeView.extend({
    ui: {
      loadingContainer: '> .js-loading-container',
      itemsListTable: '> .js-items-list-table',
      showMore: '> .js-show-more',
      itemsTotal: '.js-items-total'
    },
    events: {
      'click @ui.showMore': 'showMore'
    },
    initialize: function () {
      this.itemsTotal = -1;
      this.collection.on('fetchingMore', function() {
        this.$(this.ui.loadingContainer).removeClass('hidden');
      }, this);
      this.collection.on('sync', function () {
        this.$(this.ui.loadingContainer).addClass('hidden');
        this.$(this.ui.itemsListTable).toggleClass('hidden', this.collection.total == 0);
        this.$(this.ui.showMore).toggleClass('hidden', !this.collection.hasMore());
        if (this.collection.total !== this.itemsTotal) {
          this.itemsTotal = this.collection.total;
          this.$(this.ui.itemsTotal).text(this.itemsTotal);
        }
      }, this);
      this.collection.on('update:total', function() {
        this.$(this.ui.itemsTotal).text(this.collection.total);
      }, this);
    },
    showMore: function () {
      this.$(this.ui.showMore).addClass('hidden');
      this.collection.fetchMore({ currentCourseId: gradebook.courseId });
    }
  });

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#gradebookMainLayoutViewTemplate',
    templateHelpers: function () {
      return {
        assignmentDeployed: gradebook.assignmentDeployed
      }
    },
    className: 'val-tabs',
    regions: {
      'coursesListRegion': '.js-courses-dropdown',
      'overviewRegion': '#courseOverview',
      'usersListRegion': '#courseUsers',
      'lessonsListRegion': '#courseLessons',
      'assignmentsListRegion': '#courseAssignments',
      'gradingQueueRegion': '#courseGrading'
    },
    ui: {
      overviewTab: '#gradebookTabs a[href="#courseOverview"]',
      usersTab: '#gradebookTabs a[href="#courseUsers"]',
      lessonsTab: '#gradebookTabs a[href="#courseLessons"]',
      assignmentsTab: '#gradebookTabs a[href="#courseAssignments"]',
      gradingTab: '#gradebookTabs a[href="#courseGrading"]'
    },
    events: {
      'click @ui.overviewTab': function() { this.showOverview() },
      'click @ui.usersTab': function() { this.showUsers() },
      'click @ui.lessonsTab': function() { this.showLessons() },
      'click @ui.assignmentsTab': function() {this. showAssignments() },
      'click @ui.gradingTab': function() {this.showGradingQueue() }
    },
    childEvents: {
      'user:show:lessons': function (childView, userModel) {
        this.showUserLessons(userModel);
      },
      'lesson:show:users': function (childView, lessonModel) {
        this.showLessonUsers(lessonModel);
      },
      'assignments:show:users': function (childView, assignmentModel) {
        this.showAssignmentUsers(assignmentModel);
      },
      'breadcrumb:navigate': function(childView, section) {
        switch (section) {
          case NAVIGATION_TYPE.COURSE:
            this.showOverview();
            break;
          case NAVIGATION_TYPE.USER:
            this.showUsers(true);
            break;
          case NAVIGATION_TYPE.LESSON:
            this.showLessons(true);
            break;
          case NAVIGATION_TYPE.ASSIGNMENT:
            this.showAssignments(true);
            break;
          case NAVIGATION_TYPE.QUEUE:
            this.showGradingQueue();
            break;
        }
      }
    },
    onRender: function() {
      // stay on the same tab when change course
      gradebook.courseModel.on('change:id', function() {
        gradebook.navigationCollection.changeLevel(0, { title: gradebook.courseModel.get('title')});
        this.$('#gradebookTabs li.active a').click();
      }, this);

      var coursesListView = new Views.CoursesListView({
        courses: gradebook.coursesCollection.toJSON()
      });
      this.coursesListRegion.show(coursesListView);
    },
    showOverview: function() {
      gradebook.navigationCollection.updateLevels(0, {
        title: gradebook.courseModel.get('title'),
        section: NAVIGATION_TYPE.COURSE
      });

      this.overviewRegion.empty();
      var that = this;

      gradebook.courseModel.getCoursesStatistic({}, { currentCourseId: gradebook.courseId })
        .then(function (response) {
          if (gradebook.courseId) {
            gradebook.courseModel.set('statistic', response.statistic);
          }
          else {
            var statisticCollection = new gradebook.Entities.CoursesCollection(response.records);
            gradebook.courseModel.set('statistic', statisticCollection);
          }

          var overviewView = (gradebook.courseId)
            ? new Views.Overview.CourseOverviewView({ model: gradebook.courseModel })
            : new Views.Overview.AllCoursesOverviewView({ model: gradebook.courseModel });

          that.overviewRegion.show(overviewView);
        });
    },
    showCollectionLayoutView: function(region, contentView) {
      var layoutView = new Views.CollectionLayoutView({
        contentView: contentView
      });
      region.show(layoutView);
    },
    showUsers: function(keepFilter) {
      var navItem = { title: Valamis.language['usersLabel'], section: NAVIGATION_TYPE.USER };
      if (!keepFilter) _.extend(navItem, { filter: '' });
      gradebook.navigationCollection.updateLevels(1, navItem);

      var usersCollection = new gradebook.Entities.UsersCollection();

      var usersListView = (gradebook.courseId)
        ? new Views.UsersList.UsersCollectionView({ collection: usersCollection })
        : new Views.UsersList.AllCoursesUsersCollectionView({ collection: usersCollection });

      this.showCollectionLayoutView(this.usersListRegion, usersListView);
    },
    showUserLessons: function(userModel) {
      gradebook.navigationCollection.updateLevels(2, {
        title: userModel.get('user').name,
        section: ''
      });

      var userLessonsCollection = new gradebook.Entities.UserLessonsCollection();
      userLessonsCollection.userId = userModel.get('user').id;

      var userLessonsListView = new Views.LessonsList.UserLessonsCollectionView({
        model: userModel,
        collection: userLessonsCollection
      });

      this.showCollectionLayoutView(this.usersListRegion, userLessonsListView);
    },
    showLessons: function(keepFilter) {
      var navItem = { title: Valamis.language['lessonsLabel'], section: NAVIGATION_TYPE.LESSON };
      if (!keepFilter) _.extend(navItem, { filter: '' });
      gradebook.navigationCollection.updateLevels(1, navItem);

      var lessonsCollection = new gradebook.Entities.LessonsCollection();

      var lessonsListView = new Views.LessonsList.LessonsCollectionView({
        collection: lessonsCollection
      });

      this.showCollectionLayoutView(this.lessonsListRegion, lessonsListView);
    },
    showLessonUsers: function(lessonModel) {
      gradebook.navigationCollection.updateLevels(2, {
        title: lessonModel.get('lesson').title,
        section: ''
      });

      var lessonUsersCollection = new gradebook.Entities.LessonUsersCollection();
      lessonUsersCollection.lessonId = lessonModel.get('lesson').id;

      var lessonUsersListView = new Views.UsersList.LessonUsersCollectionView({
        model: lessonModel,
        collection: lessonUsersCollection
      });

      this.showCollectionLayoutView(this.lessonsListRegion, lessonUsersListView);
    },
    showAssignments: function(keepFilter) {
      var navItem = { title: Valamis.language['assignmentsLabel'], section: NAVIGATION_TYPE.ASSIGNMENT };
      if (!keepFilter) _.extend(navItem, { filter: '' });
      gradebook.navigationCollection.updateLevels(1, navItem);

      var assignmentsCollection = new gradebook.Entities.AssignmentCollection();

      var assignmentsListView = new Views.AssignmentsList.AssignmentCollectionView({
        collection: assignmentsCollection
      });

      this.showCollectionLayoutView(this.assignmentsListRegion, assignmentsListView);
    },
    showAssignmentUsers: function(assignmentModel) {
      gradebook.navigationCollection.updateLevels(2, {
        title: assignmentModel.get('title'),
        section: ''
      });

      var assignmentUsersCollection = new gradebook.Entities.AssignmentUsersCollection();
      assignmentUsersCollection.assignmentId = assignmentModel.get('id');

      var assignmentUsersListView = new Views.AssignmentsList.AssignmentUsersCollectionView({
        model: assignmentModel,
        collection: assignmentUsersCollection
      });

      this.showCollectionLayoutView(this.assignmentsListRegion, assignmentUsersListView);
    },
    showGradingQueue: function() {
      gradebook.navigationCollection.updateLevels(1, {
        title: Valamis.language['gradingQueueLabel'],
        section: NAVIGATION_TYPE.QUEUE
      });

      var gradingCollection = new gradebook.Entities.GradingCollection();

      var gradingCollectionView = new Views.GradingQueue.GradingCollectionView({
        collection: gradingCollection
      });

      this.showCollectionLayoutView(this.gradingQueueRegion, gradingCollectionView);
    }
  });

//  user views

  Views.UserAppLayoutView = Marionette.LayoutView.extend({
    template: '#gradebookUserLayoutViewTemplate',
    templateHelpers: function() {
      return {
        assignmentDeployed: gradebook.assignmentDeployed
      }
    },
    className: 'val-tabs',
    regions: {
      'coursesListRegion': '.js-courses-dropdown',
      'overviewRegion': '#courseOverview',
      'lessonsListRegion': '#courseLessons',
      'assignmentsListRegion': '#courseAssignments'
    },
    ui: {
      overviewTab: '#gradebookTabs a[href="#courseOverview"]',
      lessonsTab: '#gradebookTabs a[href="#courseLessons"]',
      assignmentsTab: '#gradebookTabs a[href="#courseAssignments"]'
    },
    events: {
      'click @ui.overviewTab': 'showOverview',
      'click @ui.lessonsTab': 'showLessons',
      'click @ui.assignmentsTab': 'showAssignments'
    },
    childEvents: {
      'breadcrumb:navigate': function(childView, section) {
        switch (section) {
          case NAVIGATION_TYPE.COURSE:
            this.showOverview();
            break;
        }
      }
    },
    onRender: function() {
      // stay on the same tab when change course
      gradebook.courseModel.on('change:id', function() {
        gradebook.navigationCollection.changeLevel(0, { title: gradebook.courseModel.get('title')});
        this.$('#gradebookTabs li.active a').click();
      }, this);

      var coursesListView = new Views.CoursesListView({
        courses: gradebook.coursesCollection.toJSON()
      });
      this.coursesListRegion.show(coursesListView);
    },
    showOverview: function() {
      gradebook.navigationCollection.updateLevels(0, {
        title: gradebook.courseModel.get('title'),
        section: NAVIGATION_TYPE.COURSE
      });

      this.overviewRegion.empty();
      var that = this;

      gradebook.courseModel.getUserCoursesStatistic({}, { currentCourseId: gradebook.courseId })
        .then(function (response) {
          if (gradebook.courseId) {
            gradebook.courseModel.set('lessons', response.lessons);
          }
          else {
            var statisticCollection = new gradebook.Entities.CoursesCollection(response.records);
            gradebook.courseModel.set('statistic', statisticCollection);
          }

          var overviewView = (gradebook.courseId)
            ? new Views.Overview.UserCourseOverviewView({ model: gradebook.courseModel })
            : new Views.Overview.UserAllCoursesOverviewView({ model: gradebook.courseModel });
          that.overviewRegion.show(overviewView);
        });
    },
    showCollectionLayoutView: function(region, contentView) {
      var layoutView = new Views.CollectionLayoutView({
        contentView: contentView
      });
      region.show(layoutView);
    },
    showLessons: function () {
      gradebook.navigationCollection.updateLevels(1, {
        title: Valamis.language['lessonsLabel'],
        section: ''
      });

      var userLessonsCollection = new gradebook.Entities.UserLessonsCollection();
      userLessonsCollection.userId = Utils.getUserId();

      var userLessonsListView = new Views.LessonsList.CurrentUserLessonsCollectionView({
        model: gradebook.courseModel,
        collection: userLessonsCollection
      });

      this.showCollectionLayoutView(this.lessonsListRegion, userLessonsListView);
    },
    showAssignments: function() {
      gradebook.navigationCollection.updateLevels(1, {
        title: Valamis.language['assignmentsLabel'],
        section: ''
      });

      var userAssignmentsCollection = new gradebook.Entities.UserAssignmentsCollection();
      userAssignmentsCollection.userId = Utils.getUserId();

      var userAssignmentsListView = new Views.AssignmentsList.CurrentUserAssignmentsCollectionView({
        model: gradebook.courseModel,
        collection: userAssignmentsCollection
      });

      this.showCollectionLayoutView(this.assignmentsListRegion, userAssignmentsListView);
    }
  });

});