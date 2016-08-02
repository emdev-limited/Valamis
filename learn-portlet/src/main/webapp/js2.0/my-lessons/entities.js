myLessons.module('Entities', function(Entities, myLessons, Backbone, Marionette, $, _) {

  var COUNT = 5;

  Entities.LessonModel = Backbone.Model.extend({
  });

  var LessonCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.lessongrades + 'my/',
        'data': function (model, options) {
          return {
            completed: options.completed,
            page: options.page,
            count: COUNT,
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid()
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
        model['url'] = Utils.getPackageUrl(model.lesson.id);
        return model;
      });
    }
  }).extend(LessonCollectionService);

});
