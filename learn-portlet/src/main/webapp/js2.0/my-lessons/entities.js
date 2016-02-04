myLessons.module('Entities', function(Entities, myLessons, Backbone, Marionette, $, _) {

  var COUNT = 5;

  Entities.LessonModel = Backbone.Model.extend({
  });

  var LessonCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.gradebooks,
        'data': function (model, options) {
          return {
            action: 'GRADED_PACKAGE',
            completed: options.completed,
            page: options.page,
            count: COUNT,
            courseId: Utils.getCourseId()
          };
        },
        'method': 'get'
      }
    }
  });

  Entities.LessonCollection = Backbone.Collection.extend({
    model: Entities.LessonModel,
    parse: function (response) {
      this.trigger('lessonCollection:updated', {total: response.total, count: COUNT});
      return _.map(response.records, function(model) {
        model['url'] = Utils.getPackageUrl(model.id);
        return model;
      });
    }
  }).extend(LessonCollectionService);

});