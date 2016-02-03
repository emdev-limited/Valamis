var GOAL_TYPE = {
  COURSE: 1,
  STATEMENT: 2,
  ACTIVITY: 3,
  PACKAGE: 4
};

var CertificateGoalStatusService = new Backbone.Service({ url: path.root,
  sync: {
    'read': function (e, options) {
      return path.api.users + options.userID + '/certificates/' + options.certificateID + '/goals';
    }
  }
});

var CertificateGoalStatus = Backbone.Model.extend({
  defaults: {
    id: 0,
    isActivity: false,
    selected: false,
    title: ""
  }
}).extend(CertificateGoalStatusService);


var GoalStatusView = Backbone.View.extend({
  initialize: function (options) {
    this.$el = jQuery('<tr>');
    this.language = options.language;
    this.permissions = options.permissions;

    this.model.set({ type: options.type, certificateID: options.certificateID, status: options.status});

    if (options.type == GOAL_TYPE.STATEMENT) {
      var objectName = this.model.get('tincanStmntObjName');
      var objectTitle = objectName ? Utils.getLangDictionaryTincanValue(objectName) : this.model.get('tincanStmntObj');
      this.model.set({title: this.language[this.model.get('tincanStmntVerb')] + ' ' + objectTitle});
    }
    else if (options.type == GOAL_TYPE.ACTIVITY) {
      var name = this.model.get('activityId');
      var noDate = (name == 'participation' || name == 'contribution');

      this.model.set({
        isActivity: true,
        title: this.language[this.model.get('activityId')],
        noDate: noDate
      });
    }
  },
  render: function () {
    var template = Mustache.to_html(jQuery('#goalRowTemplate').html(), _.extend(this.model.toJSON(), {
      adminView: false,
      status: this.language[this.model.get('status')]}, this.language, this.permissions));
    this.$el.html(template);
    return this;
  },
  setPeriod: function () {
    this.$('#duringPeriodType option[value=' + this.model.get('periodType') + ']').prop('selected', true);
  }
});


var ViewUserDetailsDialog = Backbone.View.extend({
  initialize: function (options) {
    this.options = options;
    this.model = new CertificateModel();
    this.statuses = new CertificateGoalStatus();
    this.statuses.on('change', this.addAll, this);
    this.render();
  },
  render: function () {
    var language = this.options.language;

    var goalsListTemplate = Mustache.to_html(jQuery('#certificateGoalsListTemplate').html(), language);
    this.$el.html(goalsListTemplate);

    return this;
  },
  setCertificateID: function (certificateID, userID, isUserMember) {
    this.userID = userID;
    this.certificateID = certificateID;
    this.isUserMember = isUserMember;
    this.model.set({id: certificateID});
    this.model.fetch({success: jQuery.proxy(function (){ this.resetStatuses(); }, this) });
  },
  resetStatuses: function () {
    if (this.isUserMember)
      this.statuses.fetch({reset: true, certificateID: this.certificateID, userID: this.userID});
    else
      this.addAll();
  },
  addAll: function () {
    this.renderGoals(this.model.get('courses'), this.statuses.get('courses'), GOAL_TYPE.COURSE);
    this.renderGoals(this.model.get('statements'), this.statuses.get('statements'), GOAL_TYPE.STATEMENT);
    this.renderGoals(this.model.get('activities'), this.statuses.get('activities'), GOAL_TYPE.ACTIVITY);
    this.renderGoals(this.model.get('packages'), this.statuses.get('packages'), GOAL_TYPE.PACKAGE);
  },
  renderGoals: function (goals, statuses, type) {
    if (goals != undefined) {
      var isFirstNotPassed = true;
      for (var i = 0; i < goals.length; i++) {
        var goalStatus = Array();
        var item = new CertificateGoalStatus(goals[i]);

        if (type == GOAL_TYPE.ACTIVITY) {
          item.set({isActivity: true });
        }

        if (statuses != undefined) {
          if (type == GOAL_TYPE.ACTIVITY) {
            goalStatus = statuses.filter(function (i) {
              return i.activityId == item.get('activityId')
            }).map(function (i) {
                return i.status;
              });
          }
          else if (type == GOAL_TYPE.STATEMENT) {
            goalStatus = statuses.filter(function (i) {
              return i.tincanStmntObj == item.get('tincanStmntObj') && i.tincanStmntVerb == item.get('tincanStmntVerb')
            }).map(function (i) {
                return i.status;
              });
          }
          else if (type == GOAL_TYPE.COURSE) {
            goalStatus = statuses.filter(function (i) {
              return i.courseGoalId == item.get('courseGoalId')
            }).map(function (i) {
                return i.status;
              });
          }
          else if (type == GOAL_TYPE.PACKAGE) {
            goalStatus = statuses.filter(function (i) {
              return i.packageId == item.get('packageId')
            }).map(function (i) {
              return i.status;
            });
          }
        }

        var status = (goalStatus.length) ? goalStatus[0] : '';
        if (type == GOAL_TYPE.COURSE){
          if (status == '' || !isFirstNotPassed) item.set({url: ''});
          if (status != 'Success') isFirstNotPassed = false;
        }
        var view = new GoalStatusView({
          model: item,
          status: status,
          certificateID: this.model.id,
          type: type,
          language: this.options.language,
          permissions: this.options.permissions
        });

        var template = view.render().$el;
        view.setPeriod();
        if (type == GOAL_TYPE.COURSE) {
          this.$('#certificateCoursesTable').append(template);
        }
        else if (type == GOAL_TYPE.STATEMENT) {
          this.$('#certificateStatementsTable').append(template);
        }
        else if (type == GOAL_TYPE.ACTIVITY) {
          this.$('#certificateActivitiesTable').append(template);
        }
        else if (type == GOAL_TYPE.PACKAGE) {
          this.$('#lessonGoalsTable').append(template);
        }
      }
    }
  }

});
