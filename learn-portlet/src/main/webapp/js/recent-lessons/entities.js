recentLessons.module('Entities', function(Entities, recentLessons, Backbone, Marionette, $, _) {

  var RecentCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.lessonResults + 'recent-lessons/',
        'data': function (collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid(),
            count: 3
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.RecentCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function(response) {
      return _.map(response, function(e) {
        e.lessonUrl = Utils.getPackageUrl(e.lessonId);
        return e;
      });
    }
  }).extend(RecentCollectionService);

});