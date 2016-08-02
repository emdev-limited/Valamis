userPortfolio.module('Entities', function(Entities, UserPortfolio, Backbone, Marionette, $, _) {

  var UserModelService = new Backbone.Service({
    url: path.root,
    sync:{
      'read':{
        path: function (model, options) {
          return path.api.users + Utils.getUserId();
        },
        'data': function () {
          return {
            courseId: Utils.getCourseId()
          }
        },
        'method': 'get'
      }
    }
  });

  Entities.UserModel = Backbone.Model.extend(UserModelService);

  var CertificateModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model) {
          return path.api.certificates + model.id
        },
        'data': {courseId: Utils.getCourseId()},
        'method': 'get'
      }
    },
    targets: {
      'getUserGoalsStatuses': {
        path: function (model, options) {
          var userId = Utils.getUserId();
          return path.api.users + userId + '/certificates/' + model.get('id') + '/goals';
        },
        'method': 'get'
      }
    }
  });

  Entities.CertificateModel = Backbone.Model.extend(CertificateModelService);

  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (e, options) {
          return path.api.users + Utils.getUserId() + '/certificates'
        },
        'data': function () {
          var params = {
            withOpenBadges: true,
            courseId: Utils.getCourseId()
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    model: Entities.CertificateModel,
    parse: function (response) {
      return response.records;
    }
  }).extend(CertificateCollectionService);

  Entities.GoalModel = Backbone.Model.extend({
    idAttribute: 'uniqueId'
  });

  var GoalCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection) {
          return path.api.certificates + collection.certificateId + '/goals'
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    }
  });

  Entities.GoalsCollection = valamisApp.Entities.BaseGoalsCollection.extend({
    model: Entities.GoalModel,
    initialize: function(models, options) {
      this.certificateId = options.certificateId;
    },
    parse: function(response) {
      var singleGoals = _.filter(response.goals, function(goal){
        return goal.goalData.groupId == undefined
      });

      var that = this;
      _.each(response.groups, function(item) {
        var filteredGoals = _.filter(response.goals, function (goal) {
          return goal.goalData.groupId == item.id
        });
        item.collection = new Entities.GoalsCollection(
          that.toGroupResponse(filteredGoals),
          { certificateId: that.certificateId }
        );
        item.isGroup = true;
        item.uniqueId = 'group_' + item.id;
      });

      return [].concat(this.toGroupResponse(singleGoals), response.groups);
    }
  }).extend(GoalCollectionService);

});