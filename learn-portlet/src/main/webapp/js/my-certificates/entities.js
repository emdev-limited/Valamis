myCertificates.module('Entities', function(Entities, myCertificates, Backbone, Marionette, $, _) {

  var CERTIFICATES_COUNT = 5;

  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.certificates;
        },
        'data': function (collection, options) {
          return {
            courseId: Utils.getCourseId(),
            page: options.page,
            count: CERTIFICATES_COUNT,
            sortBy: 'creationDate',
            sortAscDirection: true,
            isActive: true,
            additionalData: 'usersStatistics'
          };
        },
        'method': 'get'
      }
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function (response) {
      this.trigger('certificateCollection:updated', {total: response.total, count: CERTIFICATES_COUNT});
      return _.map(response.records, function(model) {
        model['url'] = Utils.getCertificateUrl(model.id);
        return model;
      });
    }
  }).extend(CertificateCollectionService);

  var UsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.users,
        'data': function (collection, options) {
          var params = {
            certificateId: options.certificateId,
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            sortAscDirection: true,
            page: options.page,
            count: options.count,
            isUserJoined: true,
            withStat: true
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.UsersCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function (response) {
      return response.records;
    }
  }).extend(UsersCollectionService);


});