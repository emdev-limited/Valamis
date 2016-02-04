myCourses.module('Entities', function(Entities, myCourses, Backbone, Marionette, $, _) {

  var COURSES_COUNT = 5;

  var CourseCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.courses + "my/";
        },
        'data': function (collection, options) {
          var params = {
            sortAscDirection: true,
            page: options.page,
            count: COURSES_COUNT
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.CourseCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function (response) {
      this.trigger('courseCollection:updated', {total: response.total, count: COURSES_COUNT});
      return response.records;
    }
  }).extend(CourseCollectionService);


  var UsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.users;
        },
        'data': function (collection, options) {
          var params = {
            groupId: options.groupId,
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            sortAscDirection: true,
            page: options.page,
            count: options.count,
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