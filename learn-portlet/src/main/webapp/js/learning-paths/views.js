learningPaths.module('Views', function (Views, learningPaths, Backbone, Marionette, $, _) {

    Views.PathGoalItem = Marionette.LayoutView.extend({
        tagName: 'li',
        template: '#learningPathsGoalItemViewTemplate',
        templateHelpers: function () {
            var statuses = learningPaths.Entities.STATUSES;

            var userResult = _.find(this.options.userProgress, {goalId: this.model.get('id')});
            var userGoalStatus = (userResult) ? userResult.status : '';
            var isSucceed = userGoalStatus === statuses.success;
            var isFailed = userGoalStatus === statuses.failed;
            var isInProgress = !isSucceed && !isFailed;

            return {
                isSucceed: isSucceed,
                isFailed: isFailed,
                isInProgress: isInProgress,
                isGroup: this.model.isGroup(),
                isActivity: this.model.isActivity()
            };
        },
        ui: {
            title: '.js-title'
        },
        regions: {
            'treeRegion': '.js-group-children'
        },
        onRender: function () {
            var goals = this.model.get('goals');
            if (goals && goals.length) {
                var groupView = new Views.PathGoalsGroup({
                    collection: new learningPaths.Entities.GoalsCollection(goals),
                    userProgress: this.options.userProgress
                });
                this.treeRegion.show(groupView);
            }

            if (this.model.isLesson()) {
                this.addLink(Utils.getPackageUrl(this.model.get('lessonId')));
            } else if (this.model.isAssignment()) {
                this.addLink(Utils.getAssignmentUrl(this.model.get('assignmentId')));
            }

            if (this.model.isCourse()) {
                var that = this;
                this.model.getCourseInfo().then(
                    function (response) {
                        that.addLink(response.friendlyUrl)
                    }
                );
            }

            if (this.model.isTrainingEvent()) {
                this.addLink(Utils.getEventUrl(this.model.get('trainingEventId')));
            }
        },
        addLink: function(url) {
            if (url) {
                this.ui.title.wrap('<a href="' + url + '"></a>');
            }
        }
    });

    Views.PathGoalsGroup = Marionette.CollectionView.extend({
        tagName: 'ul',
        childView: Views.PathGoalItem,
        childViewOptions: function () {
            return {
                userProgress: this.options.userProgress
            }
        }
    });

    Views.LearningPathItemView = Marionette.LayoutView.extend({
        template: '#learningPathItemViewTemplate',
        className: 'learning-path-item clearfix',
        templateHelpers: function () {
            var progress = Math.round((this.model.get('progress') || 0) * 100);
            return {
                fullLogoUrl: this.model.getFullLogoUrl(),
                isFailed: this.model.get('status') === 'Failed',
                progressPercent: progress + '%',
                url: Utils.getCertificateUrl(this.model.get('id'))
            }
        },
        regions: {
            'goalsRegion': '.js-goals-list'
        },
        events: {
            'click .js-toggle-goals': 'toggleGoals'
        },
        initialize: function () {
            this.isGoalsExpanded = false;
        },
        onRender: function () {
            var that = this;

            this.goals = new learningPaths.Entities.GoalsCollection();

            this.goals.fetch({
                activities: this.options.activities.toJSON(),
                learningPathId: this.model.get('id')
            }).then(function () {
                that.model.getUserProgress().then(function (statuses) {
                    that.showGoals(statuses);
                })
            });

            if (this.model.collection.length == 1) {
                this.toggleGoals();
            }
        },
        toggleGoals: function () {
            this.isGoalsExpanded = !this.isGoalsExpanded;

            this.$('.js-arrow-icon').toggleClass('val-icon-arrow-up', this.isGoalsExpanded);
            this.$('.js-show-label').toggleClass('hidden', this.isGoalsExpanded);

            this.$('.js-arrow-icon').toggleClass('val-icon-arrow-down', !this.isGoalsExpanded);
            this.$('.js-hide-label').toggleClass('hidden', !this.isGoalsExpanded);
            this.$('.js-goals-list').toggleClass('hidden', !this.isGoalsExpanded);
        },
        showGoals: function (userProgress) {

            var goalsView = new Views.PathGoalsGroup({
                collection: this.goals,
                userProgress: userProgress
            });
            this.goalsRegion.show(goalsView);
        }
    });

    Views.AppLayoutView = Marionette.CompositeView.extend({
        template: '#learningPathsLayoutTemplate',
        className: 'val-learning-paths',
        childView: Views.LearningPathItemView,
        childViewContainer: '.js-list-view',
        childViewOptions: function () {
            return {
                activities: this.options.activities
            }
        },
        ui: {
            showMore: '.js-show-more'
        },
        events: {
            'click @ui.showMore': 'fetchMore'
        },
        onRender: function () {
            this.checkUi();
            this.collection.on('sync', this.checkUi, this);

            if (this.collection.length === 0) {
                this.$('.js-no-learning-path').removeClass('hidden');
            }
        },
        fetchMore: function() {
            this.collection.fetchMore();
        },
        checkUi: function () {
            this.ui.showMore.toggleClass('hidden', !this.collection.hasMore());
        }
    });

});