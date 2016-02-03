learningPaths.module('Entities', function(Entities, learningPaths, Backbone, Marionette, $, _) {

  var CertificateService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model) {
          return path.api.certificates + model.id + '?courseId=' + Utils.getCourseId();
        }
      }
    }
  });

  Entities.CertificateModel = Backbone.Model.extend({
    parse: function(response) {
      response['url'] = Utils.getCertificateUrl(response.id);
      response['packages'] = _.map(response['packages'], function(pkg) {
        pkg['url'] = Utils.getPackageUrl(pkg.packageId);
        return pkg;
      });
      return response;
    }
  }).extend(CertificateService);

  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.certificateStates,
        'data': {
            courseId: Utils.getCourseId(),
            statuses: ['InProgress', 'Failed']
        },
        'method': 'get'
      }
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    model: Entities.CertificateModel,
    parse: function(response) {
      return _.map(response, function(model) {
        model['url'] = Utils.getCertificateUrl(model.id);
        return model;
      });
    }
  }).extend(CertificateCollectionService);


  var UserGoalModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.users + options.userId + '/certificates/'+ options.certificateId + '/goals';
        },
        method: 'get'
      }
    }
  });

  Entities.UserGoalModel = Backbone.Model.extend({
  }).extend(UserGoalModelService);

  Entities.UserGoalCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({})
  });

});