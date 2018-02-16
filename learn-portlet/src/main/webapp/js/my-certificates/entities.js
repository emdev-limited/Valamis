myCertificates.module('Entities', function (Entities, myCertificates, Backbone, Marionette, $, _) {

  Entities.ROW_TYPE = {
    DETAILS: 'details',
    CERTIFICATE: 'certificate'
  };

  var PathsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function () {
          return path.api.learningPathsStatistic;
        },
        'data': function (collection, options) {
          return {
            skip: options.skip,
            take: options.take
          };
        }
      }
    }
  });

  Entities.PathsCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function (response) {
      this.total = 2 * response.total;
      var res = [];
      _.each(response.items, function (item) {
        res.push(_.extend({tpe: Entities.ROW_TYPE.CERTIFICATE}, item));
        res.push(_.extend({tpe: Entities.ROW_TYPE.DETAILS, pathId: item.id}));
      });
      return res;
    }
  }).extend(PathsCollectionService);

  var UsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          return path.api.learningPathsStatistic + options.pathId + '/users';
        },
        'data': function (collection, options) {
          return {
            skip: options.skip,
            take: options.take
          };
        },
        'method': 'get'
      }
    }
  });

  Entities.UsersCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function (response) {
      this.total = response.total;
      return response.items;
    }
  }).extend(UsersCollectionService);


});