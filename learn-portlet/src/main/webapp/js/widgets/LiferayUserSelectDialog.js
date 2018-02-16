LiferayOrganizationModel = Backbone.Model.extend({
  defaults: {
    'id': '',
    'name': ''
  }
});

LiferayOrganizationCollectionService = new Backbone.Service({
  url: path.root,
  sync: {
    'read': function () {
      return  path.api.organizations;
    }
  }
});

LiferayOrganizationCollection = Backbone.Collection.extend({
  model: LiferayOrganizationModel
})
.extend(LiferayOrganizationCollectionService);

LiferayUserModel = Backbone.Model.extend({
  defaults: {
    userID: '',
    name: '',
    selected: false
  },
  toggle: function(){
    if(this.get('selected')) {
      this.set('selected', false);
      this.trigger('setUnselected', this);
    }
    else {
      this.set('selected', true);
      this.trigger('setSelected', this);
    }
  }
});

LiferayUserCollectionService = new Backbone.Service({
  url: path.root,
  sync: {
     'read': {
       'path': path.api.users,
       'data': function (collection, options) {
         var order = options.order;
         var sortBy = order.split(':')[0];
         var asc = order.split(':')[1];
         var params = {
             orgId: options.orgId,
             courseId: Utils.getCourseId(),
             filter: options.filter,
             sortBy: sortBy,
             sortAscDirection: asc,
             page: options.currentPage,
             count: options.itemsOnPage
         };
         if (options.certificateId) _.extend(params, {certificateId: options.certificateId, isUserJoined: false});
         return params;
       },
       'method': 'get'
     }
  }
});

LiferayUserCollection = Backbone.Collection.extend({
  model: LiferayUserModel,
  initialize: function(){
    this.modelsToSelect = [];

    var that = this;
    this.on('sync', function(collection){
      collection.each(function(model){
        model.on('setSelected', that.addModelToSelectedModels, that);
        model.on('setUnselected', that.removeModelFromSelectedModels, that);
      });
    });
  },
  addModelToSelectedModels: function(model){
    if(!_.contains(this.modelsToSelect,model)) this.modelsToSelect.push(model.id);
  },
  removeModelFromSelectedModels: function(model){
    var index = this.modelsToSelect.indexOf(model.id);
    if(index > -1) this.modelsToSelect.splice(index, 1);
  },
  parse: function (response) {
    this.trigger('userCollection:updated', { total: response.total, currentPage: response.currentPage, listed: response.records.length });

    var that = this;
    _.forEach(response.records, function(record){
      if(_.contains(that.modelsToSelect, record.id)) record.selected = true;
    });

    return response.records;
  }
}).extend(LiferayUserCollectionService);

LiferayUserListElement = Backbone.View.extend({
  tagName: 'tr',
  className: 'cursor-pointer',
  events: {
    'click': 'toggleThis'
  },
  initialize: function (options) {
    this.singleSelect = options.singleSelect;
  },
  render: function () {
    var template = Mustache.to_html(jQuery('#liferayUserElementView').html(), this.model.toJSON());
    this.$el.html(template);
    return this.$el;
  },

  toggleThis: function () {
    if (this.singleSelect)
      this.trigger('lfUserSelected', this.model);
    else {
      this.model.trigger('unsetIsSelectedAll', this.model);
      this.model.toggle();
    }
  }
});

LiferayUserSelectDialog = Backbone.View.extend({
  SEARCH_TIMEOUT: 800,
  events: {
    'click .js-addUsers': 'addUsers',
    'keyup #searchUsers': 'filterUsers',
    'click .dropdown-menu > li': 'filterUsers',
    'click #selectAllUsers': 'selectAll'
  },
  callback: function (userID, name) {
  },
  initialize: function (options) {
    this.language = options.language;
    this.singleSelect = options.singleSelect || false;
    this.certificateId = options.certificateId || undefined;

    this.organizations = new LiferayOrganizationCollection();
    this.organizations.on('reset', this.appendOrganizations, this);

    this.paginatorModel = new PageModel();
    this.paginatorModel.set({'itemsOnPage': 10});

    this.inputTimeout = null;

    this.collection = new LiferayUserCollection();

    this.collection.on('reset', this.showAll, this);
    this.collection.on('unsetIsSelectedAll', this.unsetIsSelectedAll, this);

    var that = this;
    this.collection.on('userCollection:updated', function (details) {
      that.updatePagination(details, that);
    });

    this.isSelectedAll = false;
  },
  render: function () {
    var renderedTemplate = Mustache.to_html(jQuery('#liferayUserDialogView').html(), _.extend({singleSelect: this.singleSelect}, this.language));
    this.$el.html(renderedTemplate);
    this.$('.js-search')
      .on('focus', function() {
        jQuery(this).parent('.val-search').addClass('focus');
      })
      .on('blur', function() {
        jQuery(this).parent('.val-search').removeClass('focus');
      });

    this.organizations.fetch({reset: true});

    var that = this;
    this.paginator = new ValamisPaginator({
      el: this.$el.find('#userListPaginator'),
      language: this.language,
      model: this.paginatorModel,
      topEdgeParentView: this,
      topEdgeSelector: '#userListPagingShowing'
    });
    this.paginator.on('pageChanged', function () {
      that.reload();
    });
    this.paginatorShowing = new ValamisPaginatorShowing({
      el: this.$el.find('#userListPagingShowing'),
      language: this.language,
      model: this.paginator.model
    });

    this.reloadFirstPage();

    return this;
  },

  appendOrganizations: function () {
    this.organizations.each(function(item) {
      this.$('#userOrganization .dropdown-menu').append('<li data-value="' + item.id + '"> ' + item.get('name') + ' </li>');
    }, this);
    this.$('.dropdown').valamisDropDown();
  },

  filterUsers: function () {
    clearTimeout(this.inputTimeout);
    this.inputTimeout = setTimeout(this.applyFilter.bind(this), this.SEARCH_TIMEOUT);
  },
  applyFilter: function () {
    clearTimeout(this.inputTimeout);
    this.reloadFirstPage();
  },

  updatePagination: function (details, context) {
    this.paginator.updateItems(details.total);
  },

  showAll: function () {
    this.$('#userList').empty();
    this.collection.each(this.showUser, this);
    if (this.collection.length > 0) {
      this.$('.js-addUsers').show();
    } else {
      this.$('#noUsersLabel').show();
    }
  },
  showUser: function (user) {
    var view = new LiferayUserListElement({model: user, singleSelect: this.singleSelect});
    var viewDOM = view.render();
    this.$('#userList').append(viewDOM);
    view.on('lfUserSelected', function (item) {
      this.trigger('lfUserSelected', item);
    }, this);
  },

  addUsers: function () {
    var selectedUserIds = this.collection.modelsToSelect;

    this.trigger('addUsers', selectedUserIds)
  },

  reloadFirstPage: function () {
    jQuery('.js-addUsers').hide();
    jQuery('#noUsersLabel').hide();
    this.paginatorModel.set({'currentPage': 1});
    this.fetchCollection();
  },
  reload: function () {
    this.fetchCollection();
  },
  fetchCollection: function () {
    this.collection.fetch({
      reset: true,
      currentPage: this.paginator.currentPage(),
      certificateId: this.certificateId,
      itemsOnPage: this.paginator.itemsOnPage(),
      filter: this.$('#searchUsers').val(),
      orgId: this.$('#userOrganization').data('value'),
      order: this.$('#sortUsers').data('value')
    });
  },

  selectAll: function () {
    this.isSelectedAll = (this.collection.filter(function(item) {return item.get('selected')}).length !== this.collection.length);
    this.collection.each(this.setSelectAll, this);
  },
  setSelectAll: function (model) {
    var alreadySelected = model.get('selected');

    if (alreadySelected != this.isSelectedAll) {
      model.toggle();
      this.$('#toggleUser_' + model.id).toggleClass('neutral', !model.get('selected'));
      this.$('#toggleUser_' + model.id).toggleClass('primary', model.get('selected'));
    }
  },
  unsetIsSelectedAll: function () {
    this.isSelectedAll = false;
  }
});