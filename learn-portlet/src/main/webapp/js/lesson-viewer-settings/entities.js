lessonViewerSettings.module('Entities', function (Entities, lessonViewerSettings, Backbone, Marionette, $, _) {

    var LessonModelService = new Backbone.Service({
        url: path.root,
        sync: {
            'delete': {
                'path': path.api.packages,
                'data': function (model) {
                    return {
                        action: 'DELETE_LESSON_FROM_PLAYER',
                        courseId: Utils.getCourseId(),
                        id: model.get('id'),
                        playerId: lessonViewerSettings.playerId
                    }
                },
                'method': 'post'
            }
        },
        targets: {
            setDefault: {
                'path': path.api.packages,
                'data': function (model, options) {
                    var params = {
                        action: 'SET_PLAYER_DEFAULT',
                        courseId: Utils.getCourseId(),
                        playerId: lessonViewerSettings.playerId
                    };

                    if (model.get('isDefault')) params.id = model.get('id');

                    return params;
                },
                'method': 'post'
            },
            setVisibility: {
                'path': path.api.packages,
                'data': function (model, options) {
                    return {
                        action: 'SET_LESSON_VISIBILITY',
                        courseId: Utils.getCourseId(),
                        playerId: lessonViewerSettings.playerId,
                        id: model.get('id'),
                        isHidden: model.get('isHidden')
                    };
                },
                'method': 'post'
            }
        }
    });

    Entities.LessonModel = Backbone.Model.extend({
        defaults: {}
    }).extend(LessonModelService);

    var LessonsCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.packages,
                'data': function (e, options) {
                    return {
                        action: 'ALL_FOR_PLAYER',
                        courseId: Utils.getCourseId(),
                        playerId: lessonViewerSettings.playerId
                    };
                },
                'method': 'get'
            },
            'delete': {
                'path': path.api.packages,
                'data': function (model) {
                    return {
                        action: 'DELETE_FROM_PLAYER',
                        courseId: Utils.getCourseId(),
                        playerId: lessonViewerSettings.playerId,
                        id: model.get('id')
                    };
                },
                'method': 'post'
            }
        },
        targets: {
            addToPlayer: {
                'path': path.api.packages,
                'data': function (collection, options) {
                    return {
                        action: 'ADD_LESSONS_TO_PLAYER',
                        courseId: Utils.getCourseId(),
                        playerId: lessonViewerSettings.playerId,
                        ids: options.lessonsIds
                    }
                },
                'method': 'post'
            },
            updateCategories: {
                'path': path.api.lessonsSettings + 'categories',
                'data': function (collection, options) {
                    var params = {
                        courseId: Utils.getCourseId(),
                        playerId: lessonViewerSettings.playerId
                    };
                    if (options.categoriesIds.length > 0) {
                        _.extend(params, {categoriesIds: options.categoriesIds})
                    }

                    return params;
                },
                'method': 'post'
            }
        }
    });

    Entities.LessonsCollection = Backbone.Collection.extend({
        model: Entities.LessonModel,
        parse: function (lessons) {
            _.each(lessons, function (lesson) {
                lesson.isExternal = lesson.courseId != Utils.getCourseId();
                lesson.isDefault = lesson.id == lessonViewerSettings.defaultLessonId;
            });

            return lessons;
        }
    }).extend(LessonsCollectionService);

});