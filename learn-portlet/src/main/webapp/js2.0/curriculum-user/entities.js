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
          return path.api.certificates + model.id + "/user"
        },
        'data': function (model) {
          return {
            userId: Utils.getUserId(),
            courseId: Utils.getCourseId()
          }
        },
        method: 'post'
      },
      'leave' : {
        'path': function (model) {
          return path.api.certificates + model.id + "/members"
        },
        'data': function (model) {
          var membersId = Utils.getUserId();
          return {
            courseId: Utils.getCourseId(),
            memberIds: membersId,
            memberType: MEMBER_TYPE.USER
          }
        },
        method: 'delete'
      },
      'getUserGoalsStatuses': {
        'path': function (model, options) {
          var userId = Utils.getUserId();
          return path.api.users + userId + '/certificates/' + model.get('id') + '/goals';
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
      isPublished: false,
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
          response.assignmentCount;
      return response;
    }
  }).extend(CertificateService);


  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          var userId = Utils.getUserId();
          return path.api.users + userId + '/certificates'
        },
        'data': function (collection, options) {
          var filter = options.filter;
          var sort = filter.sort;
          var sortBy = sort.split(':')[0];
          var asc = sort.split(':')[1];

          var parameters = {
            available: collection.available, // true for certificates available for joining
            page: options.currentPage,
            count: options.itemsOnPage,
            filter: filter.searchtext,
            scopeId: filter.scopeId,
            sortBy: sortBy,
            sortAscDirection: asc,
            resultAs: 'short',
            isOnlyPublished: true,
            courseId: Utils.getCourseId()
          };

          return parameters;
        },
        method: 'get'
      }
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    initialize: function(models, options) {
      this.available = options.available;
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
          return path.api.certificates + collection.certificateId + '/goals'
        },
        'data': function (collection, options) {
          var params = {
            courseId: Utils.getCourseId()
          };

          return params;
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