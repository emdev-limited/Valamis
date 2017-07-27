valamisActivities.module('Entities', function(Entities, valamisActivities, Backbone, Marionette, $, _) {

  var ActivitiesModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'delete': {
        'path': function(model){
          return path.api.activities + model.get('id')
        },
        'data': {},
        'method': 'delete'
      }
    },
    targets: {
      'likeActivity': {
        'path': path.api.valamisActivityLike,
        'data': function (model) {
          return {
            userId: Valamis.currentUserId,
            activityId: model.get('id'),
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid()
          };
        },
        'method': 'post'
      },
      'unlikeActivity': {
        'path': path.api.valamisActivityLike,
        'data': function (model) {
          return {
            userId: Valamis.currentUserId,
            activityId: model.get('id'),
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid()
          };
        },
        'method': 'delete'
      },
      'commentActivity': {
        'path': path.api.valamisActivityComment,
        'data': function (model, options) {
          return {
            action: 'CREATE',
            userId: Valamis.currentUserId,
            activityId: model.get('id'),
            content: options.content,
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid()
          };
        },
        'method': 'post'
      },
      'deleteComment': {
        'path': path.api.valamisActivityComment,
        'data': function (model, options) {
          return {
            action: 'DELETE',
            id: model.get('id'),
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid()
          };
        },
        'method': 'post'
      },
      shareActivity: {
        'path': path.api.activities,
        'data': function(model){
          var params =  {
            action: 'SHARELESSON' ,
            packageId: model.get('obj')['id'],
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid()
          };
          return params;
        },
        'method': 'post'
      }
    }
  });

  Entities.ActivitiesModel = Backbone.Model.extend({
    defaults: {
      comments: [],
      userLiked: []
    },
    parse: function (response) {
      var currentUserLike = false;
      _.forEach(response['userLiked'], function(item) {
        if (item['id'] === Valamis.currentUserId)
          currentUserLike = true;
      });
      response['currentUserLike'] = currentUserLike;
      return response;
    }
  }).extend(ActivitiesModelService);

  var ActivitiesCollectionService = new Backbone.Service({
    url: '',
    sync: {
      'read': {
        'path': function(collection, options) {
          return options.resPath
        },
        'data':  function (collection, options) {          
          return {
            action: 'getActivities',
            courseId: Utils.getCourseId,
            page: options.page,
            count: options.count,
            getMyActivities: options.getMyActivities,
            plid: Utils.getPlid()
          };
        },
        'method': 'get'
      }
    }
  });

  Entities.ActivitiesCollection = Backbone.Collection.extend({
    model: Entities.ActivitiesModel,
    parse: function(response) {
      var singleActivities = _.cloneDeep(response),
        activityGroups = [];

      // Convert plain activity response to a nested object,
      // grouping activities by date, type, id and verb (in that exact order).
      // Omit objects (at all levels) that have only 1 activity and user status activities,
      // leaving them as plain ones.
      var nested = Utils.nest(response,
        [
          function(item) {
            return moment(item.date).format('L');
          },
          function(item) {
            return item.obj.tpe;
          },
          function(item) {
            return item.obj.id;
          },
          function(item) {
            return item.verb;
          }
        ], function(collection) {
          return (collection.length > 1 && _.first(collection).obj.tpe !== 'UserStatus') ? collection : [];
        });

      var groups = Utils.omitEmptyObjects(nested);

      _.each(singleActivities, function(activity) {
        activity.userLiked = _.toArray(activity.userLiked);
      });

      // Iterate over the resulting nested object,
      // converting it to a set of group models with corresponding activities as their collections.
      _.each(groups, function(activitiesByDate, date) {
        _.each(activitiesByDate, function(activitiesByType, tpe) {
          _.each(activitiesByType, function(activitiesByObjectId, id) {
            _.each(activitiesByObjectId, function(objectActivitiesByVerb, verb) {
              var group = {
                isGroup: true,
                collection: new Entities.ActivitiesCollection()
              };

              var byVerbIdArray = _.toArray(objectActivitiesByVerb);
              var lastGroupActivity = _.first(byVerbIdArray);

              _.extend(group, _.clone(lastGroupActivity));
              delete group.id;
              group.comments = [];
              group.userLiked = [];
              _.each(byVerbIdArray, function (activity) {
                activity.isInGroup = true;
                activity.userLiked = _.toArray(activity.userLiked);
                activity.comments = _.toArray(activity.comments);
                activity.currentUserLike = _(activity.userLiked)
                  .map(function(user) { return user.id; })
                  .contains(Valamis.currentUserId);
                group.collection.add(activity);
              });

              activityGroups.push(group);

              // Delete plain activities that have been copied to groups from the response.
              _(singleActivities)
                .filter(function (a) {
                  return _.contains(_.pluck(byVerbIdArray, 'id'), a.id);
                })
                .each(function (a) {
                  _.pull(singleActivities, a);
                });
            });
          });
        });
      });

      return [].concat(singleActivities, activityGroups).sort(function(a,b) {
        return new Date(b.date) - new Date(a.date);
      });
    }
  }).extend(ActivitiesCollectionService);

  var LiferayUserModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.users + options.userId;
        },
        'data': {
          courseId: Utils.getCourseId(),
          plid: Utils.getPlid()
        },
        'method': 'get'
      }
    },
    targets: {
      'postStatus': {
        'path': path.api.activities,
        'data': function (model, options) {
          return {
            action: 'CREATEUSERSTATUS',
            courseId: Utils.getCourseId(),
            content: options.content,
            plid: Utils.getPlid()
          };
        },
        'method': 'post'
      }
    }
  });

  Entities.LiferayUserModel = Backbone.Model.extend({
  }).extend(LiferayUserModelService);

  Entities.LiferayUserCollection = Backbone.Collection.extend({
    model: Entities.LiferayUserModel
  });

});