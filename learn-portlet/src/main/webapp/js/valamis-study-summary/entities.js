valamisStudySummary.module('Entities', function (Entities, valamisStudySummary, Backbone, Marionette, $, _) {

    var LessonSummaryService = new Backbone.Service({
        url: path.root,
        targets: {
            getData: {
                path: function () {
                    return path.api.dashboard + 'summary'
                },
                data: function() {
                    return {
                        courseId: Utils.getCourseId(),
                        plid: Utils.getPlid()
                    }
                },
                method: 'get'
            }
        }
    });

    Entities.LessonSummary = Backbone.Model.extend(LessonSummaryService);


    var PathSummaryService = new Backbone.Service({
        url: path.root,
        targets: {
            getPathsByStatus: {
                path: function () {
                    return path.api.learningPaths + 'users/current/learning-paths';
                },
                data: function (model, options) {
                    return {status: options.status}
                },
                method: 'get'
            },
            getGoalsCompleted: {
                path: function () {
                    return path.api.learningPaths + 'users/current/success-goals-count';
                },
                method: 'get'
            }
        }
    });

    Entities.PathSummary = Backbone.Model.extend(PathSummaryService);

});