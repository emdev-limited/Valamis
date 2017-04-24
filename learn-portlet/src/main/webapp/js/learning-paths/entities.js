learningPaths.module('Entities', function(Entities, learningPaths, Backbone, Marionette, $, _) {

  var CertificateService = new Backbone.Service({
    url: path.root,
    targets: {
      'getUserGoalsStatuses': {
        'path': function (model, options) {
          return path.api.certificateGoals + 'certificates/' + model.get('id') + '/users/' + Utils.getUserId();
        },
        method: 'get'
      }
    }
  });

  Entities.CertificateModel = Backbone.Model.extend(CertificateService);

  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': path.api.certificateStates,
        'data': function() {
          var params= {
            courseId: Utils.getCourseId(),
            plid: Utils.getPlid(),
            statuses: ['InProgress', 'Failed']
          };

          if (!learningPaths.showInstanceCertificates)
            params.scopeId = Utils.getCourseId();

          return params;
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

  Entities.GoalModel = Backbone.Model.extend({
    idAttribute: 'uniqueId'
  });

  var GoalCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection) {
          return path.api.certificateGoals + 'certificates/' + collection.certificateId
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
      var singleGoals = _.filter(response.goals, function(goal) {
        return !goal.goalData.isDeleted && !goal.goalData.groupId;
      });

      var that = this;
      var groups = _.filter(response.groups, function(item) {
        return !item.group.isDeleted;
      });
      _.each(groups, function(item) {
        var filteredGoals = _.filter(response.goals, function (goal) {
          return !goal.goalData.isDeleted && goal.goalData.groupId == item.group.id;
        });
        item.group.collection = new Entities.GoalsCollection(
          that.toGoalResponse(filteredGoals),
          { certificateId: that.certificateId }
        );
        item.isGroup = true;
        item.uniqueId = 'group_' + item.group.id;
      });

      return [].concat(this.toGoalResponse(singleGoals), this.toGroupResponse(groups));
    }
  }).extend(GoalCollectionService);
});