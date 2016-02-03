recentLessons.module('Entities', function(Entities, recentLessons, Backbone, Marionette, $, _) {

  var RecentCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.gradebooks,
        'data': function (collection, options) {
          var params = {
            action: 'LAST_OPEN',
            courseId: Utils.getCourseId(),
            packagesCount: 3
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
      return _.map(response, function(pkg) {
        pkg['packageUrl'] = Utils.getPackageUrl(pkg.packageId);
        return pkg;
      });
    }
  }).extend(RecentCollectionService);

});