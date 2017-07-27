learningTranscript.module('Entities', function(Entities, learningTranscript, Backbone, Marionette, $, _) {

  var UserModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.users + options.userId;
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    }
  });

  Entities.UserModel = Backbone.Model
      .extend({
        getPrintUrl: function () {
          var params = {
            courseId: Utils.getCourseId()
          }

          return themeDisplay.getPortalURL() + '/' + path.api.transcript + 'course/' + Utils.getCourseId() + '/user/'
              + this.id + '/printTranscriptAll' + '?' + $.param(params);
        }
      })
      .extend(UserModelService);

  var CoursesCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.transcript + 'user/' + options.userId + '/courses';
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    }
  });

  Entities.CoursesCollection = Backbone.Collection.extend({
    model: Backbone.Model,
    parse: function(response) {
      return response;
    }
  }).extend(CoursesCollectionService);

  // todo should return only finished lessons for userId and courseId
  var LessonsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
            return path.api.transcript + 'course/' + options.courseId + '/user/' + options.userId + '/lessons';
        },
        'data': {
            courseId: Utils.getCourseId(),
            sortBy: 'name'
        },
        'method': 'get'
      }
    }
  });

  Entities.LessonsCollection = Backbone.Collection.extend({
    model: Backbone.Model,
    parse: function(response) {
      return response.records;
    }
  }).extend(LessonsCollectionService);

  // todo should return only graded assignments for userId and courseId (without using skipTake)
  var AssignmentsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          return path.api.transcript + 'course/' + options.courseId + '/user/' + options.userId + '/assignments';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            groupId: options.courseId,
            sortBy: 'title',
            status: 'Published',
            page: 1,  // todo do not use these parameters
            count: 10
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.AssignmentsCollection = Backbone.Collection.extend({
    model: Backbone.Model,
    parse: function(response) {
      var assignments = [];

      _.forEach(response, function(record) {

        var a = _.extend({
          submission: record.users[0].submission
        }, _.omit(record, 'users'));
        assignments.push(a);

      });

      return assignments;
    }
  }).extend(AssignmentsCollectionService);

  // certificates

  var CertificatesCollectionService = new Backbone.Service({
      url: path.root,
      sync: {
          read: {
              path: function (collection, options) {
                  return path.api.learningPaths + 'users/' + options.userId + '/learning-paths/'
              },
              data: function () {
                  var params = {
                      sort: 'title',
                      status: 'Success',
                      skip: 0,
                      take: 9999,
                      layoutId: Utils.getPlid()
                  };
                  return params;
              }
          }
      },
      targets: {
          getOpenBadges: {
              path: function (collection, options) {
                  return path.api.transcript + 'user/' + options.userId + '/open-badges';
              },
              data: function () {
                  return {courseId: Utils.getCourseId()}
              },
              method: 'get'
          }
      }
  });

  Entities.Certificate = Backbone.Model.extend({
    defaults: {
      isOpenBadges: false
    },
    getFullLogoUrl: function() {
      var logoUrl = this.get('logoUrl');
      return (logoUrl) ? '/' + path.api.prefix + logoUrl : '';
    },
    getPrintUrl: function (model, userId) {
      var params = {
        courseId: Utils.getCourseId()
      }
      return themeDisplay.getPortalURL() + '/' + path.api.transcript + 'user/' + userId
          + '/certificate/' + model.get('id') + '/printCertificate' + '?' + $.param(params);
    }
  });

  Entities.CertificatesCollection = Backbone.Collection.extend({
    model: Entities.Certificate,
    parse: function(response) {
      return response.items;
    }
  }).extend(CertificatesCollectionService);

});

