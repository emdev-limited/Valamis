learningReport.module('Entities', function (Entities, learningReport, Backbone, Marionette, $, _) {

    Entities.REPORT_TYPES = {
        LESSONS: 'lessonsReport',
        PATHS: 'pathsReport'
    };

    // Legend entities

    Entities.LESSON_LEGEND = [
        {
            id: 0,
            name: 'not-available',
            labelKey: 'NotAvailable'
        },
        {
            id: 1,
            name: 'finished',
            labelKey: 'Finished'
        },
        {
            id: 2,
            name: 'in-progress',
            labelKey: 'InProgress'
        },
        {
            id: 3,
            name: 'failed',
            labelKey: 'Failed'
        },
        {
            id: 4,
            name: 'attempted',
            labelKey: 'Attempted'
        },
        {
            id: 5,
            name: 'not-started',
            labelKey: 'NotStarted'
        }
    ];

    Entities.LESSON_LEGEND_SLIDES = _.filter(learningReport.Entities.LESSON_LEGEND, function (item) {
        return $.inArray(item.id, [1, 3, 5]) != -1;
    });

    Entities.PATHS_LEGEND = [
        {
            id: 0,
            name: 'not-available',
            labelKey: 'NotAvailable'
        },
        {
            id: 1,
            name: 'achieved',
            labelKey: 'Achieved'
        },
        {
            id: 2,
            name: 'failed',
            labelKey: 'Failed'
        },
        {
            id: 3,
            name: 'in-progress',
            labelKey: 'InProgress'
        },
        {
            id: 4,
            name: 'expiring',
            labelKey: 'Expiring'
        },
        {
            id: 5,
            name: 'expired',
            labelKey: 'Expired'
        }
    ];

    Entities.PATHS_LEGEND_GOALS = _.filter(learningReport.Entities.PATHS_LEGEND, function (item) {
        return $.inArray(item.id, [1, 2, 3]) != -1;
    });

    Entities.LegendModel = Backbone.Model.extend({
        defaults: {
            isActive: false
        }
    });

    Entities.LegendCollection = Backbone.Collection.extend({
        model: Entities.LegendModel
    });

    // Course selector entities

    Entities.CourseModel = Backbone.Model.extend();

    var CoursesCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    return path.api.courses + 'list/mySites';
                },
                'data': {
                    courseId: Utils.getCourseId()
                },
                'method': 'get'
            }
        }
    });

    Entities.CoursesCollection = Backbone.Collection.extend({
        model: Entities.CourseModel,
        parse: function (response) {
            return response.records;
        }
    }).extend(CoursesCollectionService);

    // Report grid data entities

    Entities.ReportItemModel = Backbone.Model.extend({
        defaults: {
            isDetailsShown: false,
            isDetailsFetched: false,
            detailsFetchedUsers: []
        }
    });

    Entities.UserCellsCollection = Backbone.Collection.extend({
        model: Entities.ReportItemModel
    });

    Entities.ReportUserModel = Backbone.Model.extend({
        initialize: function () {
            var userSummary = {};
            var userLessons = this.get('lessons');

            _.each(Entities.LESSON_LEGEND, function (status) {
                if (status.id > 0) {
                    userSummary['summary' + status.labelKey] = _.filter(userLessons, function (lesson) {
                        return lesson.status == status.id;
                    }).length;
                }
            });

            this.set('userSummary', userSummary);
        }
    });

    var ReportUsersCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    var pathPrefix =
                        (options.reportType == Entities.REPORT_TYPES.PATHS) ? path.api.patternReport.paths : path.api.patternReport.lessons;

                    var courseId = options.courseId || 'all';
                    return pathPrefix + 'course/' + courseId + '/users';
                },
                'data': function (collection, options) {
                    return {
                        filter: options.userFilter,
                        take: options.fetchUsersCount || 20,
                        skip: options.skipUsersCount || 0
                    }
                },
                'method': 'get'
            }
        }
    });

    Entities.ReportUsersCollection = Backbone.Collection.extend({
        model: Entities.ReportUserModel
    }).extend(ReportUsersCollectionService);

    Entities.ReportPathsUserModel = Backbone.Model.extend({
        initialize: function () {
            var userSummary = {};
            var userPaths = this.get('certificates');

            _.each(Entities.PATHS_LEGEND, function (status) {
                if (status.id > 0) {
                    userSummary['summary' + status.labelKey] = _.filter(userPaths, function (path) {
                        return path.status == status.id;
                    }).length;
                }
            });

            this.set('userSummary', userSummary);
        }
    });

    Entities.ReportPathsUsersCollection = Backbone.Collection.extend({
        model: Entities.ReportPathsUserModel
    }).extend(ReportUsersCollectionService);


    var ReportHeaderCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    var requestPath = '';
                    if (options.reportType == Entities.REPORT_TYPES.PATHS) {
                        requestPath = path.api.patternReport.paths + 'course/' + options.courseId + '/certificate';
                    } else {
                        requestPath = path.api.patternReport.lessons + 'course/' + options.courseId + '/lessons';
                    }
                    return requestPath;
                },
                'method': 'get'
            }
        }
    });

    Entities.ReportHeaderCollection = Backbone.Collection.extend({
        model: Entities.ReportItemModel
    }).extend(ReportHeaderCollectionService);

    var ReportTotalCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    var requestPath = '',
                        detailSuffix = '';

                    if (options.reportType == Entities.REPORT_TYPES.PATHS) {
                        detailSuffix = (!!options.detailId) ? '/certificate/' + options.detailId : '';
                        requestPath = path.api.patternReport.paths + 'course/' + options.courseId
                            + detailSuffix + '/total';
                    } else {
                        detailSuffix = (!!options.detailId) ? '/' + options.detailId : 's';
                        requestPath = path.api.patternReport.lessons + 'course/' + options.courseId
                            + '/lesson' + detailSuffix + '/total';
                    }

                    return requestPath;
                },
                'method': 'get'
            }
        }
    });

    Entities.ReportTotalCollection = Backbone.Collection.extend({
        model: Backbone.Model
    }).extend(ReportTotalCollectionService);

    var ReportUsersDetailsService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    var requestPath = '';
                    if (options.reportType == Entities.REPORT_TYPES.PATHS) {
                        requestPath = path.api.patternReport.paths + 'course/' + Utils.getCourseId()
                            + '/certificate/' + options.itemId + '/goals'
                    } else {
                        requestPath = path.api.patternReport.lessons + 'course/' + Utils.getCourseId()
                            + '/lesson/' + options.itemId + '/slides'
                    }
                    return requestPath;
                },
                'data': function (collection, options) {
                    return {
                        userIds: options.userIds
                    }
                },
                'method': 'get'
            }
        }
    });

    Entities.ReportUserPagesModel = Backbone.Model.extend({
        initialize: function () {
            this.get('pages').forEach(function (page) {
                var legendState = _.find(Entities.LESSON_LEGEND, function (status) {
                    return status.id === page.status
                });
                page.state = legendState.name;
                page.stateDescr = Valamis.language['lessonPage' + legendState.labelKey + 'StateDescrLabel'];
            });
        }
    });

    Entities.ReportUserGoalsModel = Backbone.Model.extend({
        initialize: function () {
            this.get('goals').forEach(function (goal) {
                var legendState = _.find(Entities.PATHS_LEGEND, function (status) {
                    return status.id === goal.status
                });
                goal.state = legendState.name;
                goal.stateDescr = Valamis.language['pathGoal' + legendState.labelKey + 'StateDescrLabel'];
            });
        }
    });

    Entities.ReportUsersWithPagesCollection = Backbone.Collection.extend({
        model: Entities.ReportUserPagesModel,
        initialize: function () {
            this.primaryId = 'lessonId';
            this.primaryItems = 'lessons';
            this.detailItems = 'pages';
        }
    }).extend(ReportUsersDetailsService);

    Entities.ReportUsersWithGoalsCollection = Backbone.Collection.extend({
        model: Entities.ReportUserGoalsModel,
        initialize: function () {
            this.primaryId = 'certificateId';
            this.primaryItems = 'certificates';
            this.detailItems = 'goals';
        }
    }).extend(ReportUsersDetailsService);

    //Detail layout entities

    Entities.ReportDetailDataCollection = Backbone.Collection.extend({
        model: Backbone.Model
    });

    Entities.ReportUserDetailDataCollection = Backbone.Collection.extend({
        model: Backbone.Model
    });

    Entities.ReportDetailLayoutHeaderModel = Backbone.Model.extend();

    // Filter entities

    Entities.ReportSelectorModel = Backbone.Collection.extend({
        model: Backbone.Model
    });

    var ReportSettingsService = new Backbone.Service({
        sync: {
            'read': {
                'path': function (model, options) {
                    return Utils.getContextPath() + 'js/learning-pattern-report/defaultSettings.json'
                },
                'data': function (collection, options) {
                },
                'method': 'get'
            }
        },
        targets: {
            'updateSettings': {
                'path': function (collection, options) {
                    var settingsType = (options.reportType == Entities.REPORT_TYPES.PATHS)
                        ? 'paths'
                        : 'lessons';
                    return path.root + path.api.patternReport.settings + settingsType + '/' + options.settingsId
                },
                'data': function (collection, options) {
                    var params = {
                        courseId: Utils.getCourseId(),
                        reportCourseId: options.courseId,
                        userIds: [],
                        hasSummary: options.isSummaryVisible,
                        filter: options.textSearch
                    };

                    if (options.reportType == Entities.REPORT_TYPES.PATHS) {
                        params.status = 0;
                        params.goalType = options.pathGoalType;
                        params.certificateIds = [];
                    } else {
                        params.status = 0;
                        params.questionOnly = options.onlyLessonsWithQuestions;
                        params.lessonIds = [];
                    }

                    return params;
                },
                'method': 'post'
            }
        }
    });

    Entities.ReportSettingsCollection = Backbone.Collection.extend({
        model: Backbone.Model,
        parse: function (response, options) {
            if (response === undefined) return response;

            var settings = response[this.reportType];
            _.each(settings, function (setting) {
                setting.currentValue = setting.defaultValue;
            });
            return settings;
        }
    }).extend(ReportSettingsService);


    var usersCountModelService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    var pathPrefix =
                            (options.reportType == Entities.REPORT_TYPES.PATHS) ? path.api.patternReport.paths : path.api.patternReport.lessons,
                        courseId = options.currentCourseId || 'all';
                    return pathPrefix + 'course/' + courseId + '/usersCount';
                },
                'data': function (collection, options) {
                    return {
                        filter: options.filterString
                    }
                },
                'method': 'get'
            }
        }
    });

    Entities.usersCountModel = Backbone.Model.extend(usersCountModelService);

});