learningPaths.module('Entities', function(Entities, learningPaths, Backbone, Marionette, $, _) {

  var CertificateService = new Backbone.Service({
    url: path.root,
    targets: {
      'getUserGoalsStatuses': {
        'path': function (model, options) {
          var userId = Utils.getUserId();
          return path.api.users + userId + '/certificates/' + model.get('id') + '/goals';
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