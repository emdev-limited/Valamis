achievedCertificates.module('Entities', function(Entities, achievedCertificates, Backbone, Marionette, $, _) {

  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.certificateStates,
        'data': function (collection) {
          var params = {
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid(),
            statuses: 'Success'
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function(response) {
      return _.map(response, function(model) {
        model['url'] = Utils.getCertificateUrl(model.id);
        return model;
      })
    }
  }).extend(CertificateCollectionService);

});