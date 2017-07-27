myCourses.module('Entities', function(Entities, myCourses, Backbone, Marionette, $, _) {

  Entities.ROW_TYPE = {
    DETAILS: 'details',
    COURSE: 'course'
  };

  var CourseCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.courses + 'my/';
        },
        'data': function (collection, options) {
          var params = {
            sortAscDirection: true,
            page: options.page,
            count: options.count
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.CourseCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function (response) {
      this.total = 2 * response.total;
      var res = [];
      _.each(response.records, function (item) {
        res.push(_.extend({tpe: Entities.ROW_TYPE.COURSE}, item));
        res.push(_.extend({tpe: Entities.ROW_TYPE.DETAILS, courseId: item.id}));
      });
      return res;
    }
  }).extend(CourseCollectionService);


  var UsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.lessonResults + 'course/'+ options.groupId + '/users/';
        },
        'data': function (collection, options) {
          var params = {
            sortBy: 'name',
            sortAscDirection: true,
            page: options.page,
            count: options.count
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.UsersCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model
  }).extend(UsersCollectionService);
  
});