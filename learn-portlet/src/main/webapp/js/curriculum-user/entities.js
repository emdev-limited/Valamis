curriculumUser.module('Entities', function(Entities, CurriculumUser, Backbone, Marionette, $, _) {

  Entities.SEARCH_TIMEOUT = 800;

  Entities.Filter = Backbone.Model.extend({
    defaults: {
      scopeId: '',
      searchtext: '',
      sort: 'name:true'
    }
  });

  var MEMBER_TYPE = {
    USER: 'user',
    ORGANIZATION: 'organization',
    GROUP: 'userGroup',
    ROLE: 'role'
  };

  var CertificateService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model) {
          return path.api.certificates + model.id;
        },
        'data': {courseId: Utils.getCourseId()}
      }
    },
    targets: {
      'join' : {
        'path': function (model) {
          return path.api.certificates + model.id + "/current-user"
        },
        'data': function () {
          return {
            courseId: Utils.getCourseId()
          }
        },
        method: 'post'
      },
      'leave' : {
        'path': function (model) {
          return path.api.certificates + model.id + "/current-user"
        },
        'data': function () {
          return {
            courseId: Utils.getCourseId()
          }
        },
        method: 'delete'
      },
      'getUserGoalsStatuses': {
        'path': function (model, options) {
          return path.api.certificateGoals + 'certificates/' + model.get('id') + '/users/' + Utils.getUserId();
        },
        method: 'get'
      }
    }
  });

  Entities.CertificateModel = Backbone.Model.extend({
    defaults: {
      scope: {id: ''},
      title: '',
      description: '',
      isActive: false,
      isPermanent: true,
      periodValue: 0,
      periodType: 'UNLIMITED',
      isOpenBadgesIntegration: false
    },
    parse: function(response) {
      response.goalsCount =
        response.activityCount +
        response.courseCount +
        response.packageCount +
        response.statementCount +
        response.assignmentCount -
        response.deletedCount;
      return response;
    }
  }).extend(CertificateService);


  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function () {
          return  path.api.certificates + 'users/' + Utils.getUserId()
        },
        'data': function (collection, options) {
          var filter = options.filter;
          var sort = filter.sort;
          var sortBy = sort.split(':')[0];
          var asc = sort.split(':')[1];

          return {
            available: collection.available, // true for certificates available for joining
            page: options.currentPage,
            count: options.itemsOnPage,
            filter: filter.searchtext,
            scopeId: filter.scopeId,
            sortBy: sortBy,
            sortAscDirection: asc,
            resultAs: 'short',
            isActive: collection.isActive,
            isAchieved: collection.isAchieved,
            courseId: Utils.getCourseId()
          };
        },
        method: 'get'
      }
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    initialize: function(models, options) {
      this.available = options.available;
      this.isActive = options.isActive;
      this.isAchieved = options.isAchieved;
    },
    model: Entities.CertificateModel,
    parse: function (response) {
      this.trigger('certificateCollection:updated', {
        total: response.total, currentPage: response.currentPage
      });
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
          return path.api.certificateGoals + 'certificates/' + collection.certificateId
        },
        'data': function (collection, options) {
          return {
            courseId: Utils.getCourseId()
          };
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
        return !goal.goalData.isDeleted && !goal.goalData.groupId;
      });

      var that = this;
      var notDeletedGroups = _.filter(response.groups, function (group) {
        return !group.group.isDeleted;
      });
      _.each(notDeletedGroups, function(item) {
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

      return [].concat(this.toGoalResponse(singleGoals), this.toGroupResponse(notDeletedGroups));
    }
  }).extend(GoalCollectionService);
});