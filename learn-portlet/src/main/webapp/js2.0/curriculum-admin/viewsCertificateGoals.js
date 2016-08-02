curriculumManager.module('Views.CertificateGoals', function (CertificateGoals, CurriculumManager, Backbone, Marionette, $, _) {

  CertificateGoals.GoalsTreeView = Marionette.CompositeView.extend({
    tagName: 'li',
    childViewContainer: '.js-items',
    templateHelpers: function() {
      return {
        canModify: Valamis.permissions.CurriculumManager.MODIFY && !this.options.isPublished
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
      ungroupGoal: '> .val-row .js-goal-ungroup'
    },
    events: {
      'click @ui.deleteGoal': 'deleteGoal',
      'click @ui.selectGoal': 'selectGoal',
      'keyup @ui.periodValue': 'updateGoal',
      'change @ui.periodType': 'updateGoal',
      'change @ui.isOptional': 'updateOptional',
      'keyup @ui.activityCount': 'updateActivityCount',
      'keyup @ui.groupCount': 'updateGroupCount',
      'click @ui.ungroupGoal': 'ungroupGoal'
    },
    modelEvents: {
      'change:selected': 'selectedChanged',
      'change:hidden': 'hiddenChanged'
    },
    childEvents: {
      'toggle:action:buttons': function(childView, isSelected) {
        var unselected = this.model.get('collection')
          .filter(function(model) {return !model.isSelected()}).length;

        if (!isSelected && this.model.isSelected())
          this.model.set('selected', false);

        if (isSelected && unselected == 0)
          this.model.set('selected', true);

        var selected = this.model.get('collection').length - unselected;
        this.triggerMethod('toggle:action:buttons', selected);
      },
      'action:delete:goal': function(childView, goalModel) {
        var deletedAmount = 1;
        if (this.model.get('collection').length == 1)  // if group contains one goal, delete it
          this.model.destroy({ deletedContent: true });
        else
          goalModel.destroy();

        this.triggerMethod('update:goals:count', deletedAmount);
      }
    },
    childViewOptions: function() {
      return {
        isPublished: this.options.isPublished
      }
    },
    initialize: function() {
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
      if (this.model.get('groupId'))
        this.triggerMethod('action:delete:goal', this.model);
      else
        this.model.destroy({ deletedContent: true });
    },
    selectGoal: function() {
      var isSelected = this.ui.selectGoal.is(':checked');
      this.model.set('selected',  isSelected);

      if (this.model.get('isGroup'))
        this.model.get('collection').each(function(model) {model.set('selected', isSelected)});

      this.triggerMethod('toggle:action:buttons', isSelected);
    },
    updateGoal: function() {
      var periodValue = this.ui.periodValue.val() || 0;
      var periodType = (periodValue) ? (this.ui.periodType.val() || 'DAYS') : 'UNLIMITED';

      this.model.set({
        periodValue: periodValue,
        periodType: periodType
      });

      this.model.save();
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
    }
  });

  CertificateGoals.EditGoalsView = Marionette.CompositeView.extend({
    template: '#curriculumManagerEditGoalsTemplate',
    childView: CertificateGoals.GoalsTreeView,
    childViewContainer: 'ul.js-certificate-goals',
    childViewOptions: function() {
      return {
        isPublished: this.options.certificateModel.get('isPublished')
      }
    },
    templateHelpers: function() {
      return {
        isPublished: this.options.certificateModel.get('isPublished'),
        assignmentDeployed: curriculumManager.assignmentDeployed
      }
    },
    behaviors: {
      ValamisUIControls: {}
    },
    events: {
      'keyup .js-search': 'filterGoals',
      'click #selectAllGoals': 'onSelectAllClick',
      'click .js-goal-group': 'groupGoals',
      'click .js-delete-selected': 'deleteSelected',
      'click .js-add-course': 'addCourse',
      'click .js-add-statement': 'addStatement',
      'click .js-add-activity': 'addActivity',
      'click .js-add-lesson': 'addLesson',
      'click .js-add-assignment': 'addAssignment'
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
        this.toggleSelectAll(selectedAmount === this.collection.length);
      },
      'toggle:select:all': function(childView, selectAllValue) {
        this.toggleSelectAll(selectAllValue);
      },
      'action:ungroup:goal': function(childView, groupModel) {
        valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
        var that = this;
        groupModel.destroy({ deletedContent: false }).then(function () {
          that.fetchCollection();
        });
      },
      'update:goals:count': function(childView, diff) {
        this.setGoalsCount(diff, true);
      }
    },
    initialize: function() {
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
    },
    onRender: function () {
      this.setGoalsCount(this.certificateModel.get('goalsCount'));
      if (!this.certificateModel.get('isPublished'))
        this.initSortable();
    },
    initSortable: function() {
      var that = this;
      this.$('ul.js-certificate-goals').nestedSortable({
        handle: '.handle',
        items: 'li',
        toleranceElement: '> div.list-table-row',
        listType: 'ul',
        relocate: function (e, ui) {
          // regexp, nestedSortable waits attribute in second group
          var sortArray = $(this).nestedSortable('toArray', { expression: /(?=(.+))(.+)/ });

          // one goal is left inside group
          var goalsInGroup = sortArray.filter(function (item) { return item.parent_id != null });
          var arrayValues = _.values(_.groupBy(goalsInGroup, 'parent_id'));
          var oneGoalInGroup = arrayValues.filter(function (item) {
              return item.length == 1
            }).length > 0;

          // group into group
          var groupIntoGroup = sortArray.filter(function (item) {
              return item.parent_id != null && _.contains(sortArray.map(function (item) {
                  return item.parent_id
                }), item.item_id)
            }).length > 0;

          if (oneGoalInGroup || groupIntoGroup) {
            $(this).nestedSortable('cancel');
            return;
          }

          var goalIndexes = {};
          sortArray.forEach(function (item, index) {
            if (item.item_id) goalIndexes[item.item_id] = index
          });

          that.collection.updateGoalIndexes({}, { goalIndexes: goalIndexes });

          var attr = ui.item.attr('id');
          var itemId = parseInt(attr.split('_')[1]);
          var itemArray = sortArray.filter(function (item) { return item.item_id == attr })[0];
          var parentId = itemArray.parent_id;

          var model = that.collection.getGoalById(itemId);
          var prevGroupId = model.get('groupId');
          var currGroupId = (parentId) ? parseInt(parentId.split('_')[1]) : undefined;
          var coll, goalIds;
          if (!model.get('isGroup') && prevGroupId != currGroupId) { // goal was moved in/out group

            var deferredFunc = function () {
              var deferreds = [];
              if (prevGroupId != undefined) {  // moved out group
                var d = $.Deferred();
                deferreds.push(d);

                coll = that.collection.findWhere({isGroup: true, id: prevGroupId}).get('collection');
                goalIds = coll.filter(function (model) { return model.get('id') != itemId }).
                  map(function (model) { return model.get('id') });
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
                goalIds = coll.map(function (model) { return model.get('id') });
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
      this.$('.js-no-goals-label').toggleClass('hidden', total !== 0);
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
        this.$('#selectAllGoals').prop('checked', selectAllValue);
      }
    },
    toggleActionButtons: function(hideGroupButton, hideRemoveButton) {
      this.$('.js-goal-group').toggleClass('hidden', hideGroupButton);
      this.$('.js-delete-selected').toggleClass('hidden', hideRemoveButton);
      this.$('.js-divider').toggleClass('hidden', hideGroupButton && hideRemoveButton);
    },
    filterGoals: function(e) {
      var filter = $(e.target).val();
      if (!this.certificateModel.get('isPublished'))
        this.$('ul.js-certificate-goals').nestedSortable( 'option', 'disabled', !!(filter) );
      this.collection.filterCollection(filter);
    },
    groupGoals: function() {
      valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
      this.$('.js-goal-group').attr('disabled', true);
      var selectedItems = this.collection.filter(function(model) { return model.isSelected(); });
      var selectedGroups = selectedItems.filter(function(model) {return model.get('isGroup')});

      var goalIds = [];
      selectedItems.forEach(function(model) {
        if (model.get('isGroup')) {
          model.get('collection').each(function(model) {
            goalIds.push(model.get('id'));
          });
        } else {
          goalIds.push(model.get('id'));
        }
      });

      var that = this;
      if (selectedGroups.length > 0) { // group selected, move all goals to it
        that.collection.updateGroupGoals({}, {
          groupId: selectedGroups[0].get('id'),
          goalIds: goalIds
        }).then(function () {
          that.fetchCollection();
          that.$('.js-goal-group').attr('disabled', false);
        });
      } else {
        this.collection.groupGoals({}, {
          certificateId: this.certificateModel.get('id'),
          goalIds: goalIds
        }).then(function () {
          that.fetchCollection();
          that.$('.js-goal-group').attr('disabled', false);
        });
      }

      this.toggleActionButtons(true, true);
    },
    deleteSelected: function() {
      this.collection.each(function(model) {
        if (!model.isSelected() && model.get('isGroup')) {
          model.get('collection').filter(function(model) {return model.isSelected()}).
            forEach(function(item) { item.destroy(); });
        }
      });

      this.collection.filter(function(model) {return model.isSelected()}).
        forEach(function(model) { model.destroy({ deletedContent: true });});

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
        scope: 'instance',
        packageType: 'tincan'
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
    saveGoalsToCertificate: function(params) {
      var that = this;
      valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
      this.collection.saveToCertificate({}, params).then(function() {
        that.fetchCollection();
      }, function (err, res) {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    }
  });

});