learningPaths.module('Views', function (Views, learningPaths, Backbone, Marionette, $, _) {

  Views.GoalItemView = Marionette.CompositeView.extend({
    tagName: 'tr',
    className: function () {
      return this.model.get('isGroup') ? 'goal-group' : '';
    },
    childViewContainer: '.js-items',
    templateHelpers: function () {
      var status = this.model.get('status');
      var statuses = valamisApp.Entities.STATUS;
      var goalType = valamisApp.Entities.GOAL_TYPE;

      return {
        isSuccess: status == statuses.SUCCESS,
        isFailed: status == statuses.FAILED,
        wasStarted: this.model.get('doneCount') > 0,
        groupInProgress: status == statuses.INPROGRESS && this.model.get('isGroup'),
        goalInProgress: status == statuses.INPROGRESS && !this.model.get('isGroup'),
        statusClass: status.toLowerCase(),
        lessonCourse: this.model.get('type') == goalType.PACKAGE && !!this.model.get('packageCourse'),
        isCourse: this.model.get('type') == goalType.COURSE,
        isEvent: this.model.get('type') == goalType.EVENT,
        eventInfo: this.model.get('eventInfo') || ''
      };
    },
    initialize: function() {
      this.template = (this.model.get('isGroup'))
          ? '#learningPathsGroupItemViewTemplate'
          : '#learningPathsGoalItemViewTemplate';

      this.collection = this.model.get('collection');
    }
  });

  Views.GoalCollectionView = Marionette.CompositeView.extend({
    template: '#learningPathsGoalsCollectionViewTemplate',
    childView: Views.GoalItemView,
    childViewContainer: '.js-certificate-goals',
    templateHelpers: function() {
      var progressPercent = Utils.gradeToPercent(this.collection.progress);
      return {
        progressPercent: progressPercent + '%'
      }
    },
    onRender: function() {
      this.$('.js-no-goals-label').toggleClass('hidden', this.collection.length > 0);
      this.$('.js-certificate-goals-header').toggleClass('hidden', this.collection.length == 0);
    }
  });

  Views.CertificateItemView = Marionette.LayoutView.extend({
    template: '#learningPathsItemViewTemplate',
    className: 'learning-path-item clearfix',
    templateHelpers: function() {
      return {
        isFailed : this.model.get('status') === 'Failed'
      }
    },
    regions: {
      'goalsRegion' : '.js-certificate-goals'
    },
    events: {
      'click .js-toggle-goals': 'clickToggleGoals'
    },
    initialize: function(options) {
      this.isGoalsExpanded = options.isSingleCertificate;
    },
    onRender: function() {
      var that = this;

      this.goals = new learningPaths.Entities.GoalsCollection([], {
        certificateId: this.model.get('id')
      });
      this.goals.on('sync', function() {
          that.model.getUserGoalsStatuses().then(function(statuses) {
            that.goals.setUserStatuses(statuses);
            that.showGoals();
          });
      });
      this.goals.fetch();
    },
    clickToggleGoals: function() {
      this.isGoalsExpanded = !this.isGoalsExpanded;
      this.toggleGoals();
    },
    toggleGoals: function() {
      this.$('.js-arrow-icon').toggleClass('val-icon-arrow-up', this.isGoalsExpanded);
      this.$('.js-show-label').toggleClass('hidden', this.isGoalsExpanded);

      this.$('.js-arrow-icon').toggleClass('val-icon-arrow-down', !this.isGoalsExpanded);
      this.$('.js-hide-label').toggleClass('hidden', !this.isGoalsExpanded);
      this.$('.js-certificate-goals table').toggleClass('hidden', !this.isGoalsExpanded);
    },
    showGoals: function() {
      var goalsView = new learningPaths.Views.GoalCollectionView({ collection: this.goals });
      this.goalsRegion.show(goalsView);
      this.toggleGoals();
    }
  });

  Views.AppLayoutView = Marionette.CompositeView.extend({
    template: '#learningPathsLayoutTemplate',
    className: 'val-learning-paths',
    childView: Views.CertificateItemView,
    childViewContainer: '.js-list-view',
    childViewOptions: function() {
      return {
        isSingleCertificate: this.collection.length === 1
      }
    },
    onRender: function() {
      if(this.collection.length === 0)
        this.$('.js-no-certificates').removeClass('hidden');
    }
  });

});