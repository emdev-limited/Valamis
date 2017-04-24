curriculumUser.module('Views.CertificateList', function (CertificatesList, CurriculumUser, Backbone, Marionette, $, _) {

  var DISPLAY_TYPE = {
    LIST: 'list',
    TILES: 'tiles'
  };

  CertificatesList.ToolbarView = Marionette.ItemView.extend({
    template: '#curriculumUserToolbarTemplate',
    behaviors: {
      ValamisUIControls: {}
    },
    templateHelpers: function() {
      return {
        currentCourseId: Utils.getCourseId(),
        tilesModeOption: DISPLAY_TYPE.TILES,
        listModeOption: DISPLAY_TYPE.LIST,
        isAvailableCollection: this.options.isAvailableCollection
      }
    },
    events: {
      'click .js-scope-filter .dropdown-menu > li': 'changeScope',
      'click .js-sort-filter .dropdown-menu > li': 'changeSort',
      'keyup .js-search': 'changeSearchText',
      'click .js-display-option': 'changeDisplayMode'
    },
    initialize: function() {
      this.settings = this.options.settings;
    },
    onValamisControlsInit: function(){
      this.$('.js-scope-filter').valamisDropDown('select', this.model.get('scopeId'));
      this.$('.js-sort-filter').valamisDropDown('select', this.model.get('sort'));
      this.$('.js-search').val(this.model.get('searchtext'));

      var displayMode = this.settings.get('displayMode') || DISPLAY_TYPE.LIST;
      this.$('.js-display-option[data-value="'+ displayMode +'"]').addClass('active');
    },
    changeScope: function(e){
      this.model.set('scopeId', $(e.target).attr('data-value'));
    },
    changeSort: function(e){
      this.model.set('sort', $(e.target).attr('data-value'));
    },
    changeSearchText:function(e){
      var that = this;
      clearTimeout(this.inputTimeout);
      this.inputTimeout = setTimeout(function(){
        that.model.set('searchtext', $(e.target).val());
      }, curriculumUser.Entities.SEARCH_TIMEOUT);
    },
    changeDisplayMode: function(e) {
      this.$('.js-display-option').removeClass('active');
      var elem = $(e.target).closest('.js-display-option');
      elem.addClass('active');
      this.triggerMethod('toolbar:displaymode:change', elem.attr('data-value'));
    }
  });

  CertificatesList.CertificatesItemView = Marionette.ItemView.extend({
    template: '#curriculumUserCertificatesItemTemplate',
    className: 'tile s-12 m-4 l-2',
    templateHelpers: function() {
      var status = this.model.get('status');
      var certificateStatus;

      if (status)
        certificateStatus = {
          cssClass : status.toLowerCase(),
          label : Valamis.language[status.toLowerCase() + 'StatusLabel']
        };

      return {
        isAvailableCollection: this.options.isAvailableCollection,
        courseId: Utils.getCourseId(),
        timestamp: Date.now(),
        certificateStatus: certificateStatus
      }
    },
    events: {
      'click .dropdown-menu > li.js-view-details': 'viewDetails',
      'click .dropdown-menu > li.js-view-goals': 'viewGoals',
      'click .dropdown-menu > li.js-join-certificate': 'joinCertificate',
      'click .dropdown-menu > li.js-leave-certificate': 'confirmLeave'
    },
    behaviors: {
      ValamisUIControls: {}
    },
    initialize: function() {
      this.isAvailableCollection = this.options.isAvailableCollection;
    },
    openModal: function(activeTab) {
      var that = this;
      curriculumUser.execute('certificates:show:details', this.model.get('id'), activeTab, function() {
        that.destroy();
      });
    },
    viewDetails: function() {
      this.openModal('Details');
    },
    viewGoals: function() {
      this.openModal('Goals');
    },
    joinCertificate: function() {
      var that = this;
      this.model.join().then(
        function() {
          that.destroy();
          valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        },
        function() {
          valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
        }
      );
    },
    confirmLeave: function() {
      var that = this;
      valamisApp.execute('valamis:confirm', { message: Valamis.language['warningLeaveCertificateMessageLabel'] }, function(){
        that.leaveCertificate();
      });
    },
    leaveCertificate: function() {
      var that = this;
      this.model.leave().then(
        function() {
          that.destroy();
          valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        },
        function() {
          valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
        }
      );
    }
  });

  CertificatesList.CertificatesList = Marionette.CollectionView.extend({
    className: 'js-certificate-items val-row',
    childView: CertificatesList.CertificatesItemView,
    childViewOptions: function() {
      return {
        isAvailableCollection: this.collection.available
      }
    },
    initialize: function () {
      this.settings = this.options.settings;
      this.settings.on('change:displayMode', this.setDisplayMode, this);
    },
    setDisplayMode: function() {
      var displayMode = this.settings.get('displayMode')|| DISPLAY_TYPE.LIST;
      this.$el.removeClass('list');
      this.$el.removeClass('tiles');
      this.$el.addClass(displayMode);
    },
    onRender: function() {
      this.setDisplayMode();
    }
  });

  CertificatesList.MainLayoutView = Marionette.LayoutView.extend({
    template: '#curriculumUserMainLayoutTemplate',
    regions: {
      'toolbar': '#certificateListToolbar',
      'certificatesList': '#certificateList',
      'paginator': '#certificateListPaginator',
      'paginatorShowing': '#certificateListPaginatorShowing'
    },
    childEvents: {
      'toolbar:displaymode:change': function(childView, displayMode) {
        this.settings.set('displayMode', displayMode);
        this.settings.save();
        valamisApp.execute('update:tile:sizes', this.certificatesList.currentView.$el);
      }
    },
    initialize: function() {
      var that = this;
      this.certificates = this.options.certificates;
      this.certificates.on('certificateCollection:updated', function (details) {
        that.updatePagination(details);
      });

      this.isAvailableCollection = this.certificates.available;

      var portletName = (this.isAvailableCollection) ? 'curriculumUserAvailable' : 'curriculumUser';
      this.settings = new SettingsHelper({ url: window.location.href, portlet: portletName });
      this.settings.fetch();

      this.paginatorModel = new PageModel();

      this.filter = new curriculumUser.Entities.Filter(this.settings.get('searchParams'));
      this.filter.on('change', function(){
        that.fetchCollection(true);
        that.settings.set('searchParams', this.filter.toJSON());
        that.settings.save();
      }, this);
    },
    onRender: function() {
      var toolbarView = new CertificatesList.ToolbarView({
        model: this.filter,
        settings: this.settings,
        isAvailableCollection: this.isAvailableCollection
      });
      this.toolbar.show(toolbarView);

      var certificatesListView = new CertificatesList.CertificatesList({
        collection: this.certificates,
        paginatorModel: this.paginatorModel,
        settings: this.settings
      });
      certificatesListView.on('render:collection', function(view) {
        valamisApp.execute('update:tile:sizes', view.$el);
      });
      this.certificatesList.show(certificatesListView);

      this.paginatorView = new ValamisPaginator({
        language: Valamis.language,
        model : this.paginatorModel
      });
      this.paginatorView.on('pageChanged', function () {
        this.fetchCollection(false);
      }, this);

      var paginatorShowingView = new ValamisPaginatorShowing({
        language: Valamis.language,
        model: this.paginatorModel
      });
      this.paginator.show(this.paginatorView);
      this.paginatorShowing.show(paginatorShowingView);
    },
    updatePagination: function (details, context) {
      this.paginatorView.updateItems(details.total);
    },
    fetchCollection: function(filterChanged) {
      if(filterChanged) {
        this.paginatorModel.set('currentPage', 1);
      }

      this.certificates.fetch({
        reset: true,
        filter: this.filter.toJSON(),
        currentPage: this.paginatorModel.get('currentPage'),
        itemsOnPage: this.paginatorModel.get('itemsOnPage')
      });
    }
  });

  CertificatesList.AppLayoutView = Marionette.LayoutView.extend({
    template: '#curriculumUserAppLayoutTemplate',
    className: 'val-tabs',
    regions: {
      'myCertificates': '#myCertificates',
      'availableCertificates': '#availableCertificates'
    },
    ui: {
      myCertificatesTab: '#certificateCollectionTabs a[href="#myCertificates"]',
      availableCertificatesTab: '#certificateCollectionTabs a[href="#availableCertificates"]',
      tabs: '#certificateCollectionTabs a'
    },
    events: {
      'click @ui.tabs': 'showTab'
    },
    onRender: function() {
      var myCertificatesCollection = new curriculumUser.Entities.CertificateCollection([], {
        available: false,
        isActive: true,
        isAchieved: true
      });
      var myCertificatesView = new CertificatesList.MainLayoutView({
        certificates: myCertificatesCollection
      });
      this.myCertificates.show(myCertificatesView);

      var availableCollection = new curriculumUser.Entities.CertificateCollection([], {
        available: true,
        isActive: true,
        isAchieved: false
      });
      var availableCertificatesView = new CertificatesList.MainLayoutView({
        certificates: availableCollection
      });
      this.availableCertificates.show(availableCertificatesView);

      this.ui.myCertificatesTab.on('shown.bs.tab', function () {
        myCertificatesView.fetchCollection(true);
      });
      this.ui.availableCertificatesTab.on('shown.bs.tab', function () {
        availableCertificatesView.fetchCollection(true);
      });
    },
    showTab: function(e) {
      e.preventDefault();
      $(e.target).tab('show');
    },
    onShow: function() {
      this.ui.myCertificatesTab.tab('show');
    }
  });

});