gradebook.module('Views', function (Views, gradebook, Backbone, Marionette, $, _) {

  Views.BreadcrumbsView = Marionette.ItemView.extend({
    template: '#gradebookBreadcrumbsViewTemplate',
    updateLevels: function(level, text) {
      var labels = this.$('span:not(.js-separator)');
      _.each(labels, function(item, index) {
        $(item).toggleClass('hidden', index > level);
      });
      $(labels[level]).text(text);

      var icons = this.$('span.js-separator');
      _.each(icons, function(item, index) {
        $(item).toggleClass('hidden', index > level - 1);
      });
    }
  });

  Views.PopoverButtonView = Marionette.ItemView.extend({
    template: '#gradebookSetGradePopoverButtonTemplate',
    className: 'popover-region',
    events: {
      'keyup .js-grade-input': 'toggleSend',
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
    initialize: function() {
      this.grade = 0;
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
          cancelButtonText: Valamis.language['cancelButtonLabel']
        })
      }).on('shown.bs.popover', function () {
        var elem = that.$('.js-grade-input');
        elem.on('focus', function () { elem.parent('.grade-region').addClass('focus'); })
          .on('blur', function () { elem.parent('.grade-region').removeClass('focus'); });

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
      var isValid = !isNaN(value) && grade >= 0 && grade <= 100;
      this.$('.js-submit-button').attr('disabled', !isValid);
      if (isValid)
        this.grade = grade;
    },
    openPopover: function(e) {
      this.trigger('popover:open', e.target);
    },
    closePopover: function () {
      this.$('.js-popover-button').popover('hide');
    },
    submitGrade: function () {
      this.$('.js-popover-button').popover('hide');
      this.trigger('popover:submit', {
        grade: this.grade / 100,
        comment: this.$('.js-grade-comment').val()
      });
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
      this.itemsTotal = 0;
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
      this.$(this.ui.loadingContainer).removeClass('hidden');
      this.$(this.ui.showMore).addClass('hidden');
      this.collection.fetchMore();
    }
  });

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#gradebookMainLayoutViewTemplate',
    templateHelpers: function() {
      return {
        assignmentDeployed: gradebook.assignmentDeployed
      }
    },
    className: 'val-tabs',
    regions: {
      'coursesListRegion': '.js-courses-dropdown',
      'breadcrumbsRegion': '.js-breadcrumbs',
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
      'click @ui.overviewTab': 'showOverview',
      'click @ui.usersTab': 'showUsers',
      'click @ui.lessonsTab': 'showLessons',
      'click @ui.assignmentsTab': 'showAssignments',
      'click @ui.gradingTab': 'showGradingQueue'
    },
    childEvents: {
      'user:show:lessons': function(childView, userModel) {
        this.showUserLessons(userModel);
      },
      'lesson:show:users': function(childView, lessonModel) {
        this.showLessonUsers(lessonModel);
      },
      'assignments:show:users': function(childView, assignmentModel) {
        this.showAssignmentUsers(assignmentModel);
      }
    },
    onRender: function() {
      this.breadcrumbsView = new Views.BreadcrumbsView();
      this.breadcrumbsRegion.show(this.breadcrumbsView);
    },
    onShow: function() {

      var coursesListView = new Views.CoursesListView({
        courses: gradebook.coursesCollection.toJSON()
      });
      this.coursesListRegion.show(coursesListView);
    },
    showOverview: function() {
      this.breadcrumbsView.updateLevels(0, gradebook.courseModel.get('title'));
      this.breadcrumbsView.updateLevels(1, Valamis.language['overviewLabel']);
      this.ui.overviewTab.tab('show');
      this.overviewRegion.empty();

      var that = this;
      if (gradebook.courseId)
        gradebook.courseModel.fetch().then(function () {
          that.showOverviewView();
        });
      else
        gradebook.courseModel.getCoursesStatistic().then(function (response) {
          var statisticCollection = new gradebook.Entities.CoursesCollection(response.records);
          gradebook.courseModel.set('statistic', statisticCollection);
          that.showOverviewView();
        });
    },
    showOverviewView: function() {
      var overviewView = new Views.Overview.CourseOverviewView({ model: gradebook.courseModel });
      this.overviewRegion.show(overviewView);
    },
    showUsers: function() {
      this.breadcrumbsView.updateLevels(1, Valamis.language['usersLabel']);
      gradebook.usersCollection.fetchMore({ firstPage: true, reset: true });
      var usersListView = (gradebook.courseId)
        ? new Views.UsersList.UsersCollectionView({collection: gradebook.usersCollection})
        : new Views.UsersList.AllCoursesUsersCollectionView({collection: gradebook.usersCollection});
      this.usersListRegion.show(usersListView);
    },
    showLessons: function() {
      this.breadcrumbsView.updateLevels(1, Valamis.language['lessonsLabel']);
      gradebook.lessonsCollection.fetchMore({ firstPage: true, reset: true });
      var lessonsListView = new Views.LessonsList.LessonsCollectionView({
        collection: gradebook.lessonsCollection
      });
      this.lessonsListRegion.show(lessonsListView);
    },
    showUserLessons: function(userModel) {
      this.breadcrumbsView.updateLevels(2, userModel.get('user').name);
      var userLessonsCollection = new gradebook.Entities.UserLessonsCollection();
      userLessonsCollection.userId = userModel.get('user').id;
      userLessonsCollection.fetchMore({ firstPage: true });

      var userLessonsListView = new Views.LessonsList.UserLessonsCollectionView({
        model: userModel,
        collection: userLessonsCollection
      });
      this.usersListRegion.show(userLessonsListView);
    },
    showLessonUsers: function(lessonModel) {
      this.breadcrumbsView.updateLevels(2, lessonModel.get('lesson').title);

      var lessonUsersCollection = new gradebook.Entities.LessonUsersCollection();
      lessonUsersCollection.lessonId = lessonModel.get('lesson').id;
      lessonUsersCollection.fetchMore({ firstPage: true });

      var lessonUsersListView = new Views.UsersList.LessonUsersCollectionView({
        model: lessonModel,
        collection: lessonUsersCollection
      });
      this.lessonsListRegion.show(lessonUsersListView);
    },
    showGradingQueue: function() {
      this.breadcrumbsView.updateLevels(1, Valamis.language['gradingQueueLabel']);
      gradebook.gradingCollection.fetchMore({ firstPage: true, reset: true });
      var gradingCollectionView = new Views.GradingQueue.GradingCollectionView({
        collection: gradebook.gradingCollection
      });
      this.gradingQueueRegion.show(gradingCollectionView);
    },
    showAssignments: function() {
      this.breadcrumbsView.updateLevels(1, Valamis.language['assignmentsLabel']);
      gradebook.assignmentCollection.fetchMore({ firstPage: true, reset: true });
      var assignmentCollectionView = new Views.AssignmentsList.AssignmentCollectionView({
        collection: gradebook.assignmentCollection
      });
      this.assignmentsListRegion.show(assignmentCollectionView);
    },
    showAssignmentUsers: function(assignmentModel) {
      this.breadcrumbsView.updateLevels(2, assignmentModel.get('title'));

      var assignmentUsersCollection = new gradebook.Entities.AssignmentUsersCollection();
      assignmentUsersCollection.assignmentId = assignmentModel.get('id');
      assignmentUsersCollection.fetchMore({ firstPage: true });

      var assignmentUsersListView = new Views.AssignmentsList.AssignmentUsersCollectionView({
        model: assignmentModel,
        collection: assignmentUsersCollection
      });
      this.assignmentsListRegion.show(assignmentUsersListView);
    }
  });

  //  student views

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
      'breadcrumbsRegion': '.js-breadcrumbs',
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
    onRender: function() {
      this.breadcrumbsView = new Views.BreadcrumbsView();
      this.breadcrumbsRegion.show(this.breadcrumbsView);
    },
    onShow: function() {

      var coursesListView = new Views.CoursesListView({
        courses: gradebook.coursesCollection.toJSON()
      });
      this.coursesListRegion.show(coursesListView);
    },
    showOverview: function() {
      this.breadcrumbsView.updateLevels(0, gradebook.courseModel.get('title'));
      this.breadcrumbsView.updateLevels(1, Valamis.language['overviewLabel']);
      this.ui.overviewTab.tab('show');
      this.overviewRegion.empty();

      var that = this;
      if (gradebook.courseId)
        gradebook.courseModel.fetch().then(function () {
          that.showOverviewView();
        });
      else
        gradebook.courseModel.getCoursesStatistic().then(function (response) {
          var statisticCollection = new gradebook.Entities.CoursesCollection(response.records);
          gradebook.courseModel.set('statistic', statisticCollection);
          that.showOverviewView();
        });
    },
    showOverviewView: function () {
      var overviewView = new Views.Overview.UserCourseOverviewView({model: gradebook.courseModel});
      this.overviewRegion.show(overviewView);
    },
    showLessons: function () {
      this.breadcrumbsView.updateLevels(1, Valamis.language['lessonsLabel']);

      var userLessonsCollection = new gradebook.Entities.UserLessonsCollection();
      userLessonsCollection.userId = Utils.getUserId();
      userLessonsCollection.fetchMore({ firstPage: true });

      var userLessonsListView = new Views.LessonsList.CurrentUserLessonsCollectionView({
        model: gradebook.courseModel,
        collection: userLessonsCollection
      });

      this.lessonsListRegion.show(userLessonsListView);
    },
    showAssignments: function() {
      this.breadcrumbsView.updateLevels(1, Valamis.language['assignmentsLabel']);

      var userAssignmentsCollection = new gradebook.Entities.UserAssignmentsCollection();
      userAssignmentsCollection.userId = Utils.getUserId();
      userAssignmentsCollection.fetchMore({ firstPage: true });

      var userAssignmentsListView = new Views.AssignmentsList.CurrentUserAssignmentsCollectionView({
        model: gradebook.courseModel,
        collection: userAssignmentsCollection
      });
      this.assignmentsListRegion.show(userAssignmentsListView);
    }
  });


});