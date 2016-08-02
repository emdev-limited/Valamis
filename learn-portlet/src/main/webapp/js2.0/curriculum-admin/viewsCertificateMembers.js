curriculumManager.module('Views.CertificateMembers', function (CertificateMembers, CurriculumManager, Backbone, Marionette, $, _) {

  // user results view

  CertificateMembers.UserGoalResultsItemView = Marionette.CompositeView.extend({
    tagName: 'li',
    childViewContainer: '.js-items',
    templateHelpers: function () {
      var status = this.model.get('status');
      return {
        statusItemLabelText: (status) ? Valamis.language[status.toLowerCase() + 'StatusLabel'] : ''
      };
    },
    initialize: function() {
      this.collection = this.model.get('collection');

      this.template = (this.model.get('isGroup'))
        ? '#curriculumManagerUserGroupGoalResultsItemViewTemplate'
        : '#curriculumManagerUserGoalResultsItemViewTemplate';
    },
    onRender: function () {
      this.$('.js-period-type').val(this.model.get('periodType'));
    }
  });

  CertificateMembers.UserGoalResultsView = Marionette.CompositeView.extend({
    template: '#curriculumManagerUserGoalResultsViewTemplate',
    childView: CertificateMembers.UserGoalResultsItemView,
    childViewContainer: '.js-certificate-goals',
    onRender: function() {
      this.$('.js-no-goals-label').toggleClass('hidden', this.collection.length > 0);
      this.$('.js-certificate-goals-header').toggleClass('hidden', this.collection.length == 0);
    }
  });

  // edit members view

  var MEMBER_TYPE = {
    USER: 'user',
    ORGANIZATION: 'organization',
    GROUP: 'userGroup',
    ROLE: 'role'
  };

  CertificateMembers.EditMembersToolbarView = valamisApp.Views.BaseLayout.ToolbarView.extend({
    template: '#curriculumManagerMembersToolbarViewTemplate',
    templateHelpers: function() {
      var isMemberUser = this.model.get('memberType') === MEMBER_TYPE.USER;

      var params = {
        memberTypeObject: MEMBER_TYPE,
        isMemberUser: isMemberUser,
        addButtonLabelText: Valamis.language[this.model.get('memberType') + 'AddMembersLabel']
      };

      if (isMemberUser) _.extend(params, { organizations: this.organizations.toJSON() });

      return params;
    },
    ui: {
      addItems: '.js-add-items',
      memberTypeItem: '.js-member-type .dropdown-menu > li',
      organizationItem: '.js-organizations-filter .dropdown-menu > li',
      deleteItems: '.js-delete-items'
    },
    events: {
      'click @ui.addItems': 'addItems',
      'click @ui.memberTypeItem': 'changeMemberType',
      'click @ui.organizationItem': 'changeOrganization',
      'click @ui.deleteItems': 'deleteItems'
    },
    modelEvents: {
      'change:memberType': 'render'
    },
    onValamisControlsInit: function () {
      this.$('.js-member-type').valamisDropDown('select', this.model.get('memberType'));
    },
    initialize: function() {
      this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
      this.events = _.extend({}, this.constructor.__super__.events, this.events);

      this.organizations = new Valamis.OrganizationCollection();
      this.organizations.on('sync', this.render, this);
      this.organizations.fetch();
    },
    onRender: function() {
      this.constructor.__super__.onRender.apply(this, arguments);
    },
    addItems: function(e) {
      var type = $(e.target).closest(this.ui.addItems).attr('data-value');
      this.triggerMethod('items:list:add', type)
    },
    changeOrganization: function(e) {
      this.model.set({ orgId: $(e.target).data('value') });
    },
    changeMemberType: function(e) {
      var newMemberType = $(e.target).data('value');
      this.model.set(_.extend({ memberType: newMemberType }, this.model.defaults));
      this.triggerMethod('items:list:memberType:changed', newMemberType);
    },
    deleteItems: function() {
      this.triggerMethod('items:list:action:delete:items');
    }
  });

  CertificateMembers.AddMembersListView = valamisApp.Views.BaseLayout.ListView.extend({
    childView: valamisApp.Views.BaseLayout.SelectListItemView
  });

  CertificateMembers.EditMembersListItemView = valamisApp.Views.BaseLayout.ListItemView.extend({
    template: '#curriculumManagerEditMembersItemViewTemplate',
    templateHelpers: function() {
      var status = this.model.get('status');
      return {
        statusItemLabel: (status) ? Valamis.language[status.toLowerCase() + 'StatusLabel'] : ''
      };
    },
    ui: {
      showDetails: '.js-show-details'
    },
    events: {
      'click @ui.showDetails': 'showDetails'
    },
    initialize: function() {
      this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
      this.events = _.extend({}, this.constructor.__super__.events, this.events);
    },
    deleteItem: function() {
      this.destroy();
      this.triggerMethod('items:list:delete:item', this.model);
    },
    showDetails: function() {
      this.triggerMethod('items:list:show:details', this.model);
    }
  });

  CertificateMembers.EditMembersListView = valamisApp.Views.BaseLayout.ListView.extend({
    childView: CertificateMembers.EditMembersListItemView
  });

  CertificateMembers.MembersView = valamisApp.Views.BaseLayout.MainLayoutView.extend({
    templateHelpers: function() {
      return {
        hideFooter: !(this.available)
      }
    },
    childEvents: {
      'items:list:action:delete:items': function (childView) {
        var memberIds = this.collection.filter(function (model) {
          return model.get('itemSelected');
        }).map(function(model) {
          return model.get('id')
        });

        this.deleteMembers(memberIds);
      },
      'items:list:add': function(childView, memberType) {
        var addMembersView = new CertificateMembers.MembersView({
          available: true,
          memberType: memberType,
          certificateId: this.certificateId
        });

        var that = this;
        var addMembersModalView = new valamisApp.Views.ModalView({
          header: Valamis.language['addMembersLabel'],
          contentView: addMembersView,
          submit: function() {
            memberType = addMembersView.memberType;

            addMembersView.collection.saveToCertificate({}, {
              memberIds: addMembersView.getSelectedItems(),
              memberType: memberType,
              certificateId: that.certificateId
            }).then(function() {
              that.filter.set({ memberType: memberType });
              valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
              that.fetchCollection(true);
            }, function (err, res) {
              valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
            });
          }
        });

        valamisApp.execute('modal:show', addMembersModalView);
      },
      'items:list:memberType:changed': function(childView, memberType) {
        this.memberType = memberType;
        this.cleanSelectedItems();
      },
      'items:list:delete:item': function(childView, model) {
        this.deleteMembers(model.get('id'));
      },
      'items:list:show:details': function(childView, model) {
        var that = this;
        model.getGoalsStatuses({}, {
          certificateId: this.certificateId
        }).then(function(result) {
          that.showUserDetailsView(result);
        });
      }
    },
    initialize: function(options) {
      this.childEvents = _.extend({}, this.constructor.__super__.childEvents, this.childEvents);

      this.available = !!(this.options.available);
      this.certificateId = this.options.certificateId;
      this.memberType = this.options.memberType || MEMBER_TYPE.USER;

      this.filter = new valamisApp.Entities.Filter({
        memberType: this.memberType,
        certificateId: this.certificateId,
        available: this.available
      });
      this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });
      this.goalsCollection = options.goalsCollection;

      this.collection = new curriculumManager.Entities.MembersCollection();
      this.collection.on('userCollection:updated', function (details) {
        this.updatePagination(details);
        this.trigger('members:list:update:count', details.total);
      }, this);

      this.constructor.__super__.initialize.apply(this, arguments);
    },
    onRender: function() {
      this.itemsToolbarView = new CertificateMembers.EditMembersToolbarView({
        model: this.filter,
        paginatorModel: this.paginatorModel
      });

      this.itemsListView = (this.available)
        ? new CertificateMembers.AddMembersListView({ collection: this.collection })
        : new CertificateMembers.EditMembersListView({
          memberType: this.memberType,
          collection: this.collection,
          certificateId: this.certificateId
      });

      this.constructor.__super__.onRender.apply(this, arguments);
    },
    deleteMembers: function(memberIds) {
      var that = this;
      this.collection.deleteFromCertificate({}, {
        memberIds: memberIds,
        certificateId: this.certificateId,
        memberType: this.memberType
      }).then(function (res) {
        that.fetchCollection();
        valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
      }, function (err, res) {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    showUserDetailsView: function(goalsStatuses) {
      var userGoalsCollection = new curriculumManager.Entities.GoalsCollection(
        this.goalsCollection.toJSON(),
        { certificateId: this.certificateId }
      );

      userGoalsCollection.setUserStatuses(goalsStatuses);

      var userDetailsView = new CertificateMembers.UserGoalResultsView({
        collection: userGoalsCollection
      });

      var userDetailsModalView = new valamisApp.Views.ModalView({
        header: Valamis.language['userResultsLabel'],
        contentView: userDetailsView
      });

      valamisApp.execute('modal:show', userDetailsModalView);
    }
  });

});