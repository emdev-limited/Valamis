learningPaths.module('Entities', function (Entities, learningPaths, Backbone, Marionette, $, _) {

    Entities.GOAL_TYPES = {
        group: 'group',
        course: 'course',
        statement: 'statement',
        activity: 'activity',
        webContent: 'webContent',
        lesson: 'lesson',
        assignment: 'assignment',
        trainingEvent: 'trainingEvent'
    };

    Entities.STATUSES = {
        success: 'Success',
        inprogress: 'InProgress',
        failed: 'Failed',
        expired: 'Expired',
        expiring: 'Expiring'
    };

    var LearningPathsService = new Backbone.Service({
        url: path.root,
        targets: {
            'getUserProgress': {
                'path': function (model) {
                    return path.api.learningPaths + 'learning-paths/' + model.get('id')
                        + '/members/users/' + Utils.getUserId() + '/goals-progress'
                },
                'method': 'get'
            }
        }
    });

    Entities.LearningPathModel = Backbone.Model.extend({
        getFullLogoUrl: function() {
            var logoUrl = this.get('logoUrl');
            return (logoUrl) ? '/' + path.api.prefix + logoUrl : '';
        }
    }).extend(LearningPathsService);

    var LearningPathCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function () {
                    return path.api.learningPaths + 'users/current/learning-paths';
                },
                'data': function (collection, options) {
                    var result = {
                        status: 'InProgress',
                        skip: options.skip,
                        take: options.take
                    };

                    if (!learningPaths.showInstanceCertificates) {
                        _.extend(result, { courseId: Utils.getCourseId() });
                    }

                    return result;
                },
                'method': 'get'
            }
        }
    });

    Entities.LearningPathCollection = valamisApp.Entities.LazyCollection.extend({
        model: Entities.LearningPathModel,
        parse: function (response) {
            this.total = response.total;
            return response.items;
        }
    }).extend(LearningPathCollectionService);

    var GoalModelService = new Backbone.Service({
        url: path.root,
        targets: {
            'getCourseInfo': {
                'path': function (model) {
                    return path.api.learningPaths + 'courses/' + model.get('courseId');
                },
                'method': 'get'
            }
        }
    });

    Entities.GoalModel = Backbone.Model.extend({
        isGroup: function () {
            return this.get('goalType') == Entities.GOAL_TYPES.group;
        },
        isActivity: function () {
            return this.get('goalType') == Entities.GOAL_TYPES.activity;
        },
        isLesson: function() {
            return this.get('goalType') == Entities.GOAL_TYPES.lesson;
        },
        isCourse: function() {
            return this.get('goalType') == Entities.GOAL_TYPES.course;
        },
        isTrainingEvent: function() {
            return this.get('goalType') == Entities.GOAL_TYPES.trainingEvent;
        },
        isAssignment: function() {
            return this.get('goalType') === Entities.GOAL_TYPES.assignment;
        }
    }).extend(GoalModelService);

    var ActivityCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function () {
                    return path.api.learningPaths + 'lr-activity-types';
                },
                'method': 'get'
            }
        }
    });

    Entities.ActivitiesCollection = Backbone.Collection.extend({
        model: Backbone.Model
    }).extend(ActivityCollectionService);

    var GoalCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (collection, options) {
                    return path.api.learningPaths + 'learning-paths/' + options.learningPathId
                        + '/goals/tree'
                },
                'method': 'get'
            }
        }
    });

    Entities.GoalsCollection = Backbone.Collection.extend({
        model: Entities.GoalModel,
        parse: function (response, options) {
            var activities = {};
            _.each(options.activities, function (item) {
                activities[item.activityName] = item.title;
            });

            var setGoalData = function (goals) {
                _.each(goals, function (item) {
                    if (item.goalType == Entities.GOAL_TYPES.activity) {
                        item.title = activities[item.activityName];
                    }

                    if (item.goalType == Entities.GOAL_TYPES.statement) {
                        var langObject;
                        try {
                            langObject = JSON.parse(item.objectName);
                        } catch (e) {
                            langObject = item.objectName;
                        }

                        var obj = (typeof langObject === 'object')
                            ? Utils.getLangDictionaryTincanValue(langObject)
                            : langObject;

                        item.title = (Valamis.language[item.verbId] || item.verbId) + ' ' + obj;
                    }

                    if (item.goals) {
                        setGoalData(item.goals);
                    }
                });
            };

            setGoalData(response);

            return response;
        }
    }).extend(GoalCollectionService);
});