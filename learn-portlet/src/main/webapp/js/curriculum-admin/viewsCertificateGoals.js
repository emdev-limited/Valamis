curriculumManager.module('Views.CertificateGoals', function (CertificateGoals, CurriculumManager, Backbone, Marionette, $, _) {

  CertificateGoals.GoalsTreeView = Marionette.CompositeView.extend({
    tagName: 'li',
    className: function() {
      return this.model.get('isDeleted') ? 'hidden' : '';
    },
    childViewContainer: '.js-items',
    templateHelpers: function() {
      var groupId = this.model.get('groupId');
      var parentModel = this.options.parentModel;
      var isAvailable = !this.model.get('isDeleted') &&
        (!groupId || (!!groupId && !!parentModel && parentModel.get('isGroup') && !parentModel.get('isDeleted')));

      return {
        canModify: Valamis.permissions.CurriculumManager.MODIFY &&
          !this.model.get('showChanges') && !this.model.get('isDeleted'),
        isAvailable: isAvailable
      }
    },
    ui: {
      deleteGoal: '> .val-row .js-goal-delete',
      selectGoal: '> .val-row .js-select-goal',
      periodValue: '> .val-row .js-period-value',
      periodType: '> .val-row .js-period-type',
      isOptional: '> .val-row .js-is-optional',
      activityCount: '> .val-row .js-activity-count',
      groupCount: '> .val-row .js-group-count',
      ungroupGoal: '> .val-row .js-goal-ungroup',
      undoDeleteGoal: '> .val-row .js-goal-undo',
      itemList: '> .js-items'
    },
    events: {
      'click @ui.deleteGoal': 'deleteGoal',
      'click @ui.selectGoal': 'selectGoal',
      'keyup @ui.periodValue': 'updateGoal',
      'change @ui.periodType': 'updateGoal',
      'change @ui.isOptional': 'updateOptional',
      'keyup @ui.activityCount': 'updateActivityCount',
      'keyup @ui.groupCount': 'updateGroupCount',
      'click @ui.ungroupGoal': 'ungroupGoal',
      'click @ui.undoDeleteGoal': 'undoDeleteGoal'
    },
    modelEvents: {
      'change:selected': 'selectedChanged',
      'change:hidden': 'hiddenChanged',
      'change:showChanges': 'toggleChanges',
      'change:groupId': 'render',
      'change:isDeleted': 'toggleChanges'
    },
    childEvents: {
      'toggle:action:buttons': function(childView, isSelected) {
        var unselected = this.model.get('collection')
          .filter(function(model) { return !model.get('isDeleted') && !model.isSelected(); }).length;

        if (!isSelected && this.model.isSelected())
          this.model.set('selected', false);

        if (isSelected && unselected == 0)
          this.model.set('selected', true);

        var selected = this.model.get('collection')
            .filter(function(model) { return !model.get('isDeleted'); }).length - unselected;
        this.triggerMethod('toggle:action:buttons', selected);
      },
      'action:delete:goal': function(childView, goalModel) {
        var that = this;
        var deletedAmount = 1;
        // if group contains one [not deleted] goal, delete the group
        if (this.model.get('collection').filter(function(goal) { return !goal.get('isDeleted'); }).length == 1) {
          this.model.set('isDeleted', true);
          this.model.save({}, { deleteContent: true }).then(function() {
            that.model.get('collection').each(function (goal) {
              goal.set('isDeleted', true);
            });
          });
        }
        else {
          goalModel.set('isDeleted', true);

          goalModel.save().then(function (res) {
            if(goalModel.get('groupId')) {
              that.model.set({
                modifiedDate: Utils.formatDate(res.goalData.modifiedDate, 'HH:mm, DD.MM.YYYY'),
                user: res.user
              });
            }
            that.certificateModel.set('hasGoalsChangedAfterActivation', true);
          });
        }

        this.triggerMethod('update:goals:count', deletedAmount);
      }
    },
    childViewOptions: function() {
      return {
        isActive: this.options.isActive,
        parentModel: this.model,
        certificateModel: this.certificateModel
      }
    },
    initialize: function() {
      this.certificateModel = this.options.certificateModel;
      this.collection = this.model.get('collection');

      this.template = (this.model.get('isGroup'))
        ? '#curriculumManagerGoalsGroupGoalTemplate'
        : '#curriculumManagerGoalsSingleGoalTemplate';

      var itemId = this.model.get('uniqueId');

      this.$el.attr('id', itemId);
    },
    onRender: function() {
      this.ui.periodType.val(this.model.get('periodType'));
    },
    selectedChanged: function() {
      this.ui.selectGoal.prop('checked', this.model.get('selected'));
    },
    hiddenChanged: function() {
      this.$el.toggleClass('hidden', this.model.get('hidden'));
    },
    deleteGoal: function() {
      var that = this;
      if (this.model.get('groupId'))
        this.triggerMethod('action:delete:goal', this.model);
      else {
        this.model.set('isDeleted', true);
        this.model.save({}, { deleteContent: true }).then(function() {
          var currentDeletedGoalCount = that.certificateModel.get('deletedCount');
          var currentRestoreGoalIds = that.certificateModel.get('restoreGoalIds');
          that.certificateModel.set('hasGoalsChangedAfterActivation', true);
          that.certificateModel.set('deletedCount', currentDeletedGoalCount + 1);
          that.certificateModel.set('restoreGoalIds', _.difference(currentRestoreGoalIds, [that.model.get('uniqueId')]));
          that.triggerMethod('update:goals:count', 1);
        });
      }
    },
    selectGoal: function() {
      var isSelected = this.ui.selectGoal.is(':checked');
      this.model.set('selected',  isSelected);

      if (this.model.get('isGroup')) {
        _(this.model.get('collection').filter(function (model) {
          return !model.get('isDeleted');
        })).each(function (model) {
          model.set('selected', isSelected);
        });
      }

      this.triggerMethod('toggle:action:buttons', isSelected);
    },
    updateGoal: function() {
      var that = this;
      var periodValue = this.ui.periodValue.val() || 0;
      var periodType = (periodValue) ? (this.ui.periodType.val() || 'DAYS') : 'UNLIMITED';

      this.model.set({
        periodValue: periodValue,
        periodType: periodType
      });

      this.model.save().then(function () {
        that.certificateModel.set('hasGoalChanges', true);
      });
    },
    updateOptional: function() {
      this.model.set('isOptional', this.ui.isOptional.is(':checked'));
      this.model.save();
    },
    updateActivityCount: function() {
      this.model.set('count', this.ui.activityCount.val() || 0);
      this.model.save();
    },
    ungroupGoal: function() {
      this.triggerMethod('action:ungroup:goal', this.model);
    },
    updateGroupCount: function() {
      this.model.set('goalCount', this.ui.groupCount.val() || 1);
      this.model.save();
    },
    undoDeleteGoal: function () {
      this.model.set('isDeleted', false);
      var currentRestoreGoalIds = this.certificateModel.get('restoreGoalIds');
      var currentDeletedGoalCount = this.certificateModel.get('deletedCount');
      if(!_(currentRestoreGoalIds).contains(this.model.get('uniqueId'))) {
        this.certificateModel.set('restoreGoalIds', [].concat(currentRestoreGoalIds, [this.model.get('uniqueId')]));
      }
      this.certificateModel.set('deletedCount', currentDeletedGoalCount - 1);
    },
    toggleChanges: function () {
      this.$el.toggleClass('hidden', !!this.model.get('isDeleted') && !this.model.get('showChanges'));
      this.render();
    }
  });

  CertificateGoals.EditGoalsView = Marionette.CompositeView.extend({
    template: '#curriculumManagerEditGoalsTemplate',
    childView: CertificateGoals.GoalsTreeView,
    childViewContainer: 'ul.js-certificate-goals',
    childViewOptions: function() {
      return {
        isActive: this.options.certificateModel.get('isActive'),
        showChanges: !this.ui.showChanges.prop('checked'),
        certificateModel: this.options.certificateModel
      }
    },
    templateHelpers: function() {
      var hasGoalChanges = this.options.certificateModel.get('hasGoalChanges') ||
      this.options.certificateModel.get('hasGoalsChangedAfterActivation') ||
      this.options.certificateModel.get('deletedCount') > 0;

      return {
        isActive: this.options.certificateModel.get('isActive'),
        hasGoalChanges: hasGoalChanges,
        assignmentDeployed: curriculumManager.assignmentDeployed,
        eventsDeployed: curriculumManager.eventsDeployed
      }
    },
    behaviors: {
      ValamisUIControls: {}
    },
    ui: {
      'search': '.js-search',
      'selectAllGoals': '#selectAllGoals',
      'noGoalsLabel': '.js-no-goals-label',
      'groupGoals': '.js-goal-group',
      'deleteSelected': '.js-delete-selected',
      'addCourse': '.js-add-course',
      'addStatement': '.js-add-statement',
      'addActivity': '.js-add-activity',
      'addLesson': '.js-add-lesson',
      'addAssignment': '.js-add-assignment',
      'addEvent': '.js-add-event',
      'showChanges': '#showChanges'
    },
    events: {
      'keyup @ui.search': 'filterGoals',
      'click @ui.selectAllGoals': 'onSelectAllClick',
      'click @ui.groupGoals': 'groupGoals',
      'click @ui.deleteSelected': 'deleteSelected',
      'click @ui.addCourse': 'addCourse',
      'click @ui.addStatement': 'addStatement',
      'click @ui.addActivity': 'addActivity',
      'click @ui.addLesson': 'addLesson',
      'click @ui.addAssignment': 'addAssignment',
      'click @ui.addEvent': 'addEvent',
      'change @ui.showChanges': 'toggleShowChanges'
    },
    childEvents: {
      'toggle:action:buttons': function(childView, selected) {
        var selectedItems = this.collection.filter(function(model) { return model.isSelected() });
        var selectedAmount = selectedItems.length;
        var selectedGroups = selectedItems.filter(function(model) { return model.get('isGroup') });

        // todo not allowed to combine several groups now
        var hideGroupButton = selectedAmount < 2 || selectedGroups.length > 1;
        var hideRemoveButton = selectedAmount == 0 && selected == 0;

        this.toggleActionButtons(hideGroupButton, hideRemoveButton);
        this.toggleSelectAll(selectedAmount === this.collection
            .filter(function(model) { return !model.get('isDeleted'); })
            .length
        );
      },
      'toggle:select:all': function(childView, selectAllValue) {
        this.toggleSelectAll(selectAllValue);
      },
      'action:ungroup:goal': function(childView, groupModel) {
        valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
        var that = this;
        groupModel.set('isDeleted', true);
        groupModel.save({}, { deleteContent: false }).then(function () {
          that.fetchCollection();
        });
      },
      'update:goals:count': function(childView, diff) {
        this.setGoalsCount(diff, true);
      }
    },
    initialize: function() {
      var that = this;
      this.selectAllValue = false;
      this.goalType = curriculumManager.Entities.GOAL_TYPE;
      this.certificateModel = this.options.certificateModel;
      this.certificateId = this.certificateModel.get('id');

      this.collection.on('goalsCollection:updated', function(total) {
        this.setGoalsCount(total);
      }, this);

      this.collection.on('remove', function(model) {
        var coll = model.get('collection');
        this.setGoalsCount( (coll) ? coll.length : 1, true)
      }, this);

      this.collection.on('toggleShowChanges', function (isShowChangesOn) {
        this.each(function(model) {
          model.set('showChanges', !!isShowChangesOn);
          if(model.get('isGroup')) {
            model.get('collection').each(function (goal) {
              goal.set('showChanges', !!isShowChangesOn);
            });
          }
        });
      });

      this.certificateModel.on('updateGoalsCount', function () {
        // if an "undo delete goal" action happened,
        // revert its 'isDeleted' attribute for the sake of counting goals properly
        var allGoals = that.collection.getAllGoalsAsArray();
        var goalsCount = _(allGoals).map(function (goal) {
          if(goal.changed.hasOwnProperty('isDeleted') && goal.previousAttributes().isDeleted) {
            goal.set('isDeleted', goal.previousAttributes().isDeleted);
          }
          return goal;
        }).filter(function (goal) { return !goal.get('isDeleted'); }).value().length;

        that.setGoalsCount(goalsCount);
      });
    },
    onRender: function () {
      this.setGoalsCount(this.certificateModel.get('goalsCount'));
      this.initSortable();
    },
    initSortable: function() {
      var that = this;
      this.$('ul.js-certificate-goals').nestedSortable({
        handle: '.handle',
        items: 'li:not(.hidden)',
        toleranceElement: '> div.list-table-row',
        listType: 'ul',
        relocate: function (e, ui) {
          // regexp, nestedSortable waits attribute in second group
          var sortArray = $(this).nestedSortable('toArray', { expression: /(?=(.+))(.+)/ });

          // one goal is left inside group
          var goalsInGroup = sortArray.filter(function (item) { return item.parent_id != null; });
          var arrayValues = _.values(_.groupBy(goalsInGroup, 'parent_id'));
          var oneGoalInGroup = arrayValues.filter(function (item) {
              return item.length == 1;
            }).length > 0;

          // group into group
          var groupIntoGroup = sortArray.filter(function (item) {
              return item.parent_id != null && _.contains(sortArray.map(function (item) {
                  return item.parent_id;
                }), item.item_id);
            }).length > 0;

          if (oneGoalInGroup || groupIntoGroup) {
            $(this).nestedSortable('cancel');
            return;
          }

          var goalIndexes = {};
          sortArray.forEach(function (item, index) {
            if (item.item_id) {
              goalIndexes[item.item_id] = index;
            }
          });

          that.collection.updateGoalIndexes({}, { goalIndexes: goalIndexes });

          var attr = ui.item.attr('id');
          var itemId = parseInt(_.last(attr.split('_')));
          var itemArray = sortArray.filter(function (item) { return item.item_id == attr; })[0];
          var parentId = itemArray.parent_id;

          var model = that.collection.getByUniqueId(attr);
          var prevGroupId = model.get('groupId');
          var currGroupId = (parentId) ? parseInt(parentId.split('_')[1]) : undefined;
          var coll, goalIds;
          if (!model.get('isGroup') && prevGroupId != currGroupId) { // goal was moved in/out of group

            var deferredFunc = function () {
              var deferreds = [];
              if (prevGroupId != undefined) {  // moved out of group
                var d = $.Deferred();
                deferreds.push(d);

                coll = that.collection.findWhere({isGroup: true, id: prevGroupId}).get('collection');
                goalIds = coll
                  .filter(function (model) { return model.get('id') != itemId; })
                  .map(function (model) { return model.get('id'); });
                that.collection.updateGroupGoals({}, {
                  groupId: prevGroupId,
                  goalIds: goalIds,
                  success: function () { d.resolve(); },
                  error: function () { d.reject(); }
                });
              }
              if (currGroupId != undefined) {  // moved to group
                var d1 = $.Deferred();
                deferreds.push(d1);

                coll = that.collection.findWhere({isGroup: true, id: currGroupId}).get('collection');
                goalIds = coll.map(function (model) { return model.get('id'); });
                goalIds.push(itemId);
                that.collection.updateGroupGoals({}, {
                  groupId: currGroupId,
                  goalIds: goalIds,
                  success: function () { d1.resolve(); },
                  error: function () { d1.reject(); }
                });
              }
              return deferreds;
            };

            $.when.apply($, deferredFunc()).then(
              function () { that.fetchCollection(); },
              function () {
                valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
              }
            );
          }
        }

      });
    },
    setGoalsCount: function(value, update) {
      var total = (update) ? (this.certificateModel.get('goalsCount') - value) : value;

      this.certificateModel.set({ goalsCount: total });
      this.$('.js-goals-amount').text(total);
      this.$(this.ui.noGoalsLabel).toggleClass('hidden', total !== 0);
      this.$('.js-certificate-goals-header').toggleClass('hidden', total === 0);

      this.selectAllValue = true;
      this.onSelectAllClick();
    },
    onSelectAllClick: function() {
      this.selectAllValue = !this.selectAllValue;
      this.collection.toggleSelectAll(this.selectAllValue);
      this.toggleActionButtons(!this.selectAllValue, !this.selectAllValue);
    },
    toggleSelectAll: function(selectAllValue) {
      if (this.selectAllValue != selectAllValue) {
        this.selectAllValue = selectAllValue;
        this.$(this.ui.selectAllGoals).prop('checked', selectAllValue);
      }
    },
    toggleActionButtons: function(hideGroupButton, hideRemoveButton) {
      this.$('.js-goal-group').toggleClass('hidden', hideGroupButton);
      this.$('.js-delete-selected').toggleClass('hidden', hideRemoveButton);
      this.$('.js-divider').toggleClass('hidden', hideGroupButton && hideRemoveButton);
    },
    filterGoals: function(e) {
      var filter = $(e.target).val();
      this.collection.filterCollection(filter);
    },
    groupGoals: function() {
      valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
      this.$('.js-goal-group').attr('disabled', true);
      var selectedItems = this.collection.filter(function(model) {
        return !model.get('isDeleted') && model.isSelected();
      });
      var selectedGroups = selectedItems.filter(function(model) { return model.get('isGroup'); });

      var goalIds = [];
      selectedItems.forEach(function(model) {
        if (model.get('isGroup')) {
          model.get('collection').each(function(goalModel) {
            goalIds.push(goalModel.get('id'));
          });
        } else {
          goalIds.push(model.get('id'));
        }
      });

      var that = this;
      if (selectedGroups.length > 0) { // group selected, move all goals to it
        this.collection.updateGroupGoals({}, {
          groupId: selectedGroups[0].get('id'),
          goalIds: goalIds
        }).then(onUpdateSuccess);
      } else {
        this.collection.groupGoals({}, {
          certificateId: this.certificateModel.get('id'),
          goalIds: goalIds
        }).then(onUpdateSuccess);
      }

      function onUpdateSuccess() {
        that.fetchCollection();
        that.$('.js-goal-group').attr('disabled', false);
        that.$(that.ui.selectAllGoals).prop('checked', false);
        that.certificateModel.set('hasGoalsChangedAfterActivation', true);
      }

      this.toggleActionButtons(true, true);
    },
    deleteSelected: function() {
      var that = this;
      this.collection.each(function(model) {
        if (!model.isSelected() && model.get('isGroup')) {
          model.get('collection')
            .filter(function(model) { return model.isSelected(); })
            .forEach(function(item) {
              item.set('isDeleted', true);
              item.save().then(function() {
                that.certificateModel.trigger('updateGoalsCount');
              });
            });
        }
      });

      this.collection
        .filter(function(model) { return model.isSelected(); })
        .forEach(function(model) {
          if(model.get('isGroup')) {
            model.get('collection').each(function(item) { item.set('isDeleted', true); });
          }
          model.set('isDeleted', true);
          model.save({}, { deleteContent: true }).then(function () {
            that.certificateModel.trigger('updateGoalsCount');
          });
        });

      this.toggleActionButtons(true, true);
    },
    fetchCollection: function() {
      this.collection.fetch({
        reset: true,
        success: function() {
          valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        },
        error: function() {
          valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
        }
      });
    },
    addCourse: function() {
      var that = this;

      var courseSelectView = new valamisApp.Views.SelectSite.LiferaySiteSelectLayout({ singleSelect: false });
      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['addCoursesLabel'],
        contentView: courseSelectView
      });

      courseSelectView.on('addSelectedLfSite', function(selectedSiteIds) {
        that.saveGoalsToCertificate({
          certificateId: that.certificateId,
          type: that.goalType.COURSE,
          key: 'courseIds',
          value: selectedSiteIds
        });
        valamisApp.execute('modal:close', modalView);
      });

      valamisApp.execute('modal:show', modalView);
    },
    addStatement: function() {
      var that = this;
      var addStatementView = new curriculumManager.Views.CertificateGoals.AddStatements.StatementsAddView();

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['addStatement'],
        contentView: addStatementView,
        beforeSubmit: function() {
          return addStatementView.validate();
        },
        submit: function() {
          var statements = addStatementView.collection;
          var stmnts = JSON.stringify( statements.map(function (item) {
            return {
              verb: item.get('verb'),
              obj: item.get('obj')
            };
          } ));
          that.saveGoalsToCertificate({
            certificateId: that.certificateId,
            type: that.goalType.STATEMENT,
            key: 'tincanStmnts',
            value: stmnts
          });
        }
      });

      valamisApp.execute('modal:show', modalView);

    },
    addActivity: function() {
      var that = this;
      var activitySelectView = new curriculumManager.Views.CertificateGoals.AddActivities.ActivitySelectView();

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['addActivitiesLabel'],
        contentView: activitySelectView,
        submit: function() {
          var selectedActivitiesIds = activitySelectView.getSelectedActivities();
          that.saveGoalsToCertificate({
            certificateId: that.certificateId,
            type: that.goalType.ACTIVITY,
            key: 'activityNames',
            value: selectedActivitiesIds
          });
        }
      });

      valamisApp.execute('modal:show', modalView);
    },
    addLesson: function() {
      var that = this;
      var lessonSelectView = new valamisApp.Views.SelectLesson.LessonsSelectLayoutView({
        scope: 'instance'
      });

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['selectLessonGoalsLabel'],
        contentView: lessonSelectView,
        submit: function() {
          var selectedLessonsIds = lessonSelectView.getSelectedLessons();
          that.saveGoalsToCertificate({
            type: that.goalType.PACKAGE,
            key: 'packageIds',
            value: selectedLessonsIds
          });
        }
      });

      valamisApp.execute('modal:show', modalView);
    },
    addAssignment: function() {
      var that = this;
      var assignmentSelectView = new valamisApp.Views.SelectAssignment.AssignmentsSelectLayoutView();

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['selectAssignmentGoalsLabel'],
        contentView: assignmentSelectView,
        submit: function() {
          var selectedAssignmentsIds = assignmentSelectView.getSelectedAssignments();
          that.saveGoalsToCertificate({
            type: that.goalType.ASSIGNMENT,
            key: 'assignmentIds',
            value: selectedAssignmentsIds
          });
        }
      });

      valamisApp.execute('modal:show', modalView);

    },
    addEvent: function() {
      var that = this;
      var eventsAddView = new valamisApp.Views.AddEvents.EventsSelectLayoutView();

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['selectEventGoalsLabel'],
        contentView: eventsAddView,
        submit: function() {
          var selectedEventsIds = eventsAddView.getSelectedEvents();
          that.saveGoalsToCertificate({
            type: that.goalType.EVENT,
            key: 'trainingEventIds',
            value: selectedEventsIds
          });
        }
      });

      valamisApp.execute('modal:show', modalView);
    },
    saveGoalsToCertificate: function(params) {
      var that = this;
      valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
      this.ui.showChanges.prop('checked', false);
      this.toggleShowChanges();
      this.collection.saveToCertificate({}, params).then(function() {
        that.fetchCollection();
      }, function (err, res) {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    toggleShowChanges: function() {
      var isShowChangesOn = this.ui.showChanges.prop('checked');
      var GRID_SIZE_WITH_CHANGES_OFF = 5,
          GRID_SIZE_WITH_CHANGES_ON = 4;

      this.collection.trigger('toggleShowChanges', isShowChangesOn);

      var titleHeader = this.$('.js-certificate-goal-title-header');
      var goalTitle = this.$('ul.js-certificate-goals .title');

      if(isShowChangesOn) {
        titleHeader.changeSizeInGrid({s: GRID_SIZE_WITH_CHANGES_ON});
        goalTitle.changeSizeInGrid({s: GRID_SIZE_WITH_CHANGES_ON});
      } else {
        titleHeader.changeSizeInGrid({s: GRID_SIZE_WITH_CHANGES_OFF});
        goalTitle.changeSizeInGrid({s: GRID_SIZE_WITH_CHANGES_OFF});
      }
      titleHeader.find('.handle, .checkbox-label').toggleClass('hidden');
      this.$('.js-certificate-goal-changes-header').toggleClass('hidden');
      this.$('ul.js-certificate-goals')
        .find('.js-goal-delete, .js-goal-ungroup')
        .parent()
        .toggleClass('hidden', isShowChangesOn);
      this.$('.goal-changes').toggleClass('hidden', !isShowChangesOn);

      var allGoals = this.collection.getAllGoalsAsArray();
      var goalsByRemovedState = (isShowChangesOn)
        ? allGoals
        : _.filter(allGoals, function(goal) { return !goal.get('isDeleted'); });
      this.setGoalsCount(goalsByRemovedState.length);
    }
  });

});