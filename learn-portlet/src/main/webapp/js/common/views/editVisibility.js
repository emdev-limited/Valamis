valamisApp.module('Views.EditVisibility', function (EditVisibility, valamisApp, Backbone, Marionette, $, _) {

  var VIEWER_TYPE = {
    USER: 'user',
    ORGANIZATION: 'organization',
    GROUP: 'userGroup',
    ROLE: 'role'
  };

  EditVisibility.EditVisibilityToolbarView = valamisApp.Views.BaseLayout.ToolbarView.extend({
    template: '#visibilitySettingsToolbarViewTemplate',
    templateHelpers: function() {
      var isViewerUser = this.model.get('viewerType') === VIEWER_TYPE.USER;

      var params = {
        viewerTypeObject: VIEWER_TYPE,
        isViewerUser: isViewerUser,
        addButtonLabelText: Valamis.language[this.model.get('viewerType') + 'AddViewersLabel']
      };

      if (isViewerUser) _.extend(params, { organizations: this.organizations.toJSON() });

      return params;
    },
    ui: {
      addItems: '.js-add-items',
      viewerTypeItem: '.js-viewer-type .dropdown-menu > li',
      organizationItem: '.js-organizations-filter .dropdown-menu > li',
      deleteItems: '.js-delete-items'
    },
    events: {
      'click @ui.addItems': 'addItems',
      'click @ui.viewerTypeItem': 'changeViewerType',
      'click @ui.organizationItem': 'changeOrganization',
      'click @ui.deleteItems': 'deleteItems'
    },
    modelEvents: {
      'change:viewerType': 'render'
    },
    onValamisControlsInit: function () {
      this.$('.js-viewer-type').valamisDropDown('select', this.model.get('viewerType'));
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
    changeViewerType: function(e) {
      var newViewerType = $(e.target).data('value');
      this.model.set(_.extend({ viewerType: newViewerType }, this.model.defaults));
      this.triggerMethod('items:list:viewerType:changed', newViewerType);
    },
    deleteItems: function() {
      this.triggerMethod('items:list:action:delete:items');
    }
  });

  EditVisibility.AddViewersListView = valamisApp.Views.BaseLayout.ListView.extend({
    childView: valamisApp.Views.BaseLayout.SelectListItemView
  });

  EditVisibility.EditVisibilityListItemView = valamisApp.Views.BaseLayout.ListItemView.extend({
      templateHelpers: function () {
          return {
              isUser: this.options.viewerType === VIEWER_TYPE.USER
          };
      },
      deleteItem: function () {
          this.triggerMethod('items:list:delete:item', this.model);
      }
  });

  EditVisibility.EditVisibilityListView = valamisApp.Views.BaseLayout.ListView.extend({
      childView: EditVisibility.EditVisibilityListItemView,
      childViewOptions: function () {
          return {viewerType: this.options.viewerType}
      }
  });

  EditVisibility.EditVisibilityView = valamisApp.Views.BaseLayout.MainLayoutView.extend({
    templateHelpers: function() {
      return {
        hideFooter: !(this.available)
      }
    },
    childEvents: {
      'items:list:action:delete:items': function (childView) {
        var viewerIds = this.collection.filter(function (model) {
          return model.get('itemSelected');
        }).map(function(model) {
          return model.get('id')
        });

        this.deleteViewers(viewerIds);
      },
      'items:list:add': function(childView, viewerType) {
        var addViewersView = new EditVisibility.EditVisibilityView({
          available: true,
          viewerType: viewerType,
          packageId: this.packageId
        });

        var that = this;
        var addViewersModalView = new valamisApp.Views.ModalView({
          header: Valamis.language['addViewersLabel'],
          contentView: addViewersView,
          submit: function() {
            viewerType = addViewersView.viewerType;

            addViewersView.collection.addViewers({}, {
              viewerIds: addViewersView.getSelectedItems(),
              viewerType: viewerType,
              packageId: that.packageId
            }).then(function() {
              that.filter.set({ viewerType: viewerType });
              that.fetchCollection(true);
            });
          }
        });

        valamisApp.execute('modal:show', addViewersModalView);
      },
      'items:list:viewerType:changed': function(childView, viewerType) {
        this.viewerType = viewerType;
        this.cleanSelectedItems();
      },
      'items:list:delete:item': function(childView, model) {
        this.deleteViewers(model.get('id'));
      }
    },
    initialize: function() {
      this.childEvents = _.extend({}, this.constructor.__super__.childEvents, this.childEvents);

      this.available = !!(this.options.available);
      this.packageId = this.options.packageId;
      this.viewerType = this.options.viewerType || VIEWER_TYPE.USER;

      this.filter = new valamisApp.Entities.Filter({
        viewerType: this.viewerType,
        packageId: this.packageId,
        available: this.available
      });
      this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

      this.collection = new valamisApp.Entities.ViewersCollection();
      this.collection.on('viewerCollection:updated', function (details) {
        this.updatePagination(details);
      }, this);

      this.constructor.__super__.initialize.apply(this, arguments);
    },
    onRender: function() {
      this.itemsToolbarView = new EditVisibility.EditVisibilityToolbarView({
        model: this.filter,
        paginatorModel: this.paginatorModel
      });

      this.itemsListView = (this.available)
        ? new EditVisibility.AddViewersListView({ collection: this.collection })
        : new EditVisibility.EditVisibilityListView({
          collection: this.collection,
          packageId: this.packageId,
          viewerType: this.viewerType
        });

      this.constructor.__super__.onRender.apply(this, arguments);
    },
    deleteViewers: function(viewerIds) {
      var that = this;
      this.collection.deleteViewers({}, {
        viewerIds: viewerIds,
        viewerType: this.viewerType,
        packageId: this.packageId
      }).then(function() {
        that.fetchCollection();
      });
    }
  });

});
