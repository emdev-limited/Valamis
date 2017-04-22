learningReport.module('Views.SelectUsers', function (SelectUsers, learningReport, Backbone, Marionette, $, _) {

  var USER_TYPE = {
    USER: 'user',
    ORGANIZATION: 'organization',
    GROUP: 'userGroup',
    ROLE: 'role'
  };

  SelectUsers.EditUsersToolbarView = valamisApp.Views.BaseLayout.ToolbarView.extend({
    template: '#learningReportSettingsUsersToolbarViewTemplate',
    templateHelpers: function() {
      var isUser = this.model.get('userType') === USER_TYPE.USER;

      var params = {
        userTypeObject: USER_TYPE,
        isUser: isUser,
        showUserTypes: false,
        addButtonLabelText: Valamis.language[this.model.get('userType') + 'AddUsersLabel'],
        showAvailable: this.options.showAvailable
      };

      if (isUser) _.extend(params, { organizations: this.organizations.toJSON() });

      return params;
    },
    ui: {
      addItems: '.js-add-items',
      userTypeItem: '.js-user-type .dropdown-menu > li',
      organizationItem: '.js-organizations-filter .dropdown-menu > li',
      deleteItems: '.js-delete-items'
    },
    events: {
      'click @ui.addItems': 'addItems',
      'click @ui.userTypeItem': 'changeUserType',
      'click @ui.organizationItem': 'changeOrganization',
      'click @ui.deleteItems': 'deleteItems'
    },
    modelEvents: {
      'change:userType': 'render'
    },
    onValamisControlsInit: function () {
      this.$('.js-user-type').valamisDropDown('select', this.model.get('userType'));
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
      this.triggerMethod('items:list:add', type);
    },
    changeOrganization: function(e) {
      this.model.set({ orgId: $(e.target).data('value') });
    },
    changeUserType: function(e) {
      var newUserType = $(e.target).data('value');
      this.model.set(_.extend({ userType: newUserType }, this.model.defaults));
      this.triggerMethod('items:list:userType:changed', newUserType);
    },
    deleteItems: function() {
      this.triggerMethod('items:list:action:delete:items');
    }
  });

  SelectUsers.AddUsersListView = valamisApp.Views.BaseLayout.ListView.extend({
    childView: valamisApp.Views.BaseLayout.SelectListItemView
  });

  SelectUsers.EditUsersListItemView = valamisApp.Views.BaseLayout.ListItemView.extend({
    template: '#learningReportSettingsEditUsersItemViewTemplate',
    initialize: function() {
      this.events = _.extend({}, this.constructor.__super__.events, this.events);
    },
    deleteItem: function() {
      this.destroy();
      this.triggerMethod('items:list:delete:item', this.model);
    }
  });

  SelectUsers.EditUsersListView = valamisApp.Views.BaseLayout.ListView.extend({
    childView: SelectUsers.EditUsersListItemView
  });

  SelectUsers.UsersView = valamisApp.Views.BaseLayout.MainLayoutView.extend({
    templateHelpers: function() {
      return {
        hideFooter: !(this.options.showAvailable)
      }
    },
    childEvents: {
      'items:list:action:delete:items': function (childView) {
        var userIds = this.collection.filter(function (model) {
          return model.get('itemSelected');
        }).map(function(model) {
          return model.get('id');
        });

        this.deleteUsers(userIds);
      },
      'items:list:add': function(childView, userType) {
        var addUsersView = new SelectUsers.UsersView({
          showAvailable: true,
          userType: userType,
          reportModel: this.options.reportModel,
          isUserJoined: false
        });

        var that = this;
        var addUsersModalView = new valamisApp.Views.ModalView({
          header: Valamis.language['addUsersLabel'],
          contentView: addUsersView,
          submit: function() {
            that.trigger('users:selected', that, addUsersView.getSelectedItems());
          }
        });

        valamisApp.execute('modal:show', addUsersModalView);
      },
      'items:list:userType:changed': function(childView, userType) {
        this.userType = userType;
        this.cleanSelectedItems();
      },
      'items:list:delete:item': function(childView, model) {
        this.deleteUsers([model.get('id')]);
      }
    },
    initialize: function(options) {
      this.childEvents = _.extend({}, this.constructor.__super__.childEvents, this.childEvents);

      this.userType = this.options.userType || USER_TYPE.USER;
      this.filter = new valamisApp.Entities.Filter({
        userType: this.userType,
        available: options.showAvailable
      });
      this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

      this.collection = new learningReport.Entities.UsersCollection();
      this.collection.userIds = _.pluck(this.options.reportModel.get('users'), 'id');
      this.collection.isUserJoined = this.options.isUserJoined;
      this.collection.withUserIdFilter = !this.options.showAvailable;
      this.collection.on('userCollection:updated', function (details) {
        this.updatePagination(details);
        this.trigger('users:list:update:count', details.total);
      }, this);

      this.constructor.__super__.initialize.apply(this, arguments);
    },
    onRender: function() {
      this.itemsToolbarView = new SelectUsers.EditUsersToolbarView({
        model: this.filter,
        paginatorModel: this.paginatorModel,
        showAvailable: this.options.showAvailable,
        reportModel: this.options.reportModel
      });

      this.itemsListView = (this.options.showAvailable)
        ? new SelectUsers.AddUsersListView({ collection: this.collection })
        : new SelectUsers.EditUsersListView({
          userType: this.userType,
          collection: this.collection
        });

      this.constructor.__super__.onRender.apply(this, arguments);
    },
    deleteUsers: function(userIds) {
      var that = this;
      var currentUserIds = _.pluck(that.options.reportModel.get('users'), 'id');
      var newUserIds = _.difference(currentUserIds, userIds);
      window.LearnAjax.post(learningReport.actionURL, {
        action: 'SaveUsers',
        userIds: newUserIds.join(',')
      }).done(function () {
        valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        that.options.reportModel.set('users', _.map(newUserIds, function (id) { return { id: id }; }));
      }).error(function () {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      }).always(function () {
        that.fetchCollection(true, { userIds: newUserIds });
      });
    }
  });

});