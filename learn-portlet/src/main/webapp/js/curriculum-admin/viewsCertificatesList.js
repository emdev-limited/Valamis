curriculumManager.module('Views.CertificatesList', function (CertificatesList, CurriculumManager, Backbone, Marionette, $, _) {

  var DISPLAY_TYPE = {
    LIST: 'list',
    TILES: 'tiles'
  };
  var EDIT_TABS = {
    DETAILS: 'Details',
    GOALS: 'Goals',
    MEMBERS: 'Members'
  };

  CertificatesList.ToolbarView = Marionette.ItemView.extend({
    template: '#curriculumManagerToolbarTemplate',
    behaviors: {
      ValamisUIControls: {}
    },
    templateHelpers: function() {
      return {
        currentCourseId: Utils.getCourseId(),
        tilesModeOption: DISPLAY_TYPE.TILES,
        listModeOption: DISPLAY_TYPE.LIST
      }
    },
    events: {
      'click .js-scope-filter .dropdown-menu > li': 'changeScope',
      'click .js-sort-filter .dropdown-menu > li': 'changeSort',
      'keyup .js-search': 'changeSearchText',
      'click .js-display-option': 'changeDisplayMode',
      'click .js-create-certificate': 'createCertificate'
    },
    onValamisControlsInit: function(){
      this.$('.js-scope-filter').valamisDropDown('select', this.model.get('scopeId'));
      this.$('.js-sort-filter').valamisDropDown('select', this.model.get('sort'));
      this.$('.js-search').val(this.model.get('searchtext'));

      var displayMode = curriculumManager.settings.get('displayMode') || DISPLAY_TYPE.LIST;
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
      }, curriculumManager.Entities.SEARCH_TIMEOUT);
    },
    changeDisplayMode: function(e) {
      this.$('.js-display-option').removeClass('active');
      var elem = $(e.target).closest('.js-display-option');
      elem.addClass('active');
      this.triggerMethod('toolbar:displaymode:change', elem.attr('data-value'));
    },
    createCertificate: function() {
      this.triggerMethod('toolbar:certificate:create');
    }
  });

  CertificatesList.CertificatesItemView = Marionette.ItemView.extend({
    template: '#curriculumManagerCertificatesItemTemplate',
    className: 'tile s-12 m-4 l-2',
    templateHelpers: function() {
      return {
        courseId: Utils.getCourseId(),
        timestamp: Date.now()
      }
    },
    events: {
      'click .dropdown-menu > li.js-edit-certificate': 'editDetails',
      'click .dropdown-menu > li.js-edit-goals': 'editGoals',
      'click .dropdown-menu > li.js-edit-members': 'editMembers',
      'click .dropdown-menu > li.js-clone-certificate': 'cloneCertificate',
      'click .dropdown-menu > li.js-activate-certificate': 'activateCertificate',
      'click .dropdown-menu > li.js-deactivate-certificate': 'confirmDeactivate',
      'click .dropdown-menu > li.js-delete-certificate': 'confirmDelete',
      'click .dropdown-menu > li.js-export-certificate': 'exportCertificate'
    },
    modelEvents: {
      'change:isActive': 'render',
      'change:goalsCount': 'render',
      'change:actionsDisabled': 'onChangeActionsDisabled',
      'itemSaved': 'render'
    },
    behaviors: {
      ValamisUIControls: {}
    },
    onRender: function() {
      this.$el.toggleClass('unpublished', !(this.model.get('isActive')));
    },
    onChangeActionsDisabled: function() {
      var isDisabled = !!this.model.get('actionsDisabled');

      if (isDisabled) {
        valamisApp.execute('notify', 'info', Valamis.language['overlayInprogressMessageLabel']);
        this.$('.dropdown').valamisDropDown('disable');
      }
      else {
        valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        this.$('.dropdown').valamisDropDown('enable');
      }
    },
    editCertificate: function(activeTab) {
      var that = this;

      this.model.fetch().then(function() {
        var editView = new curriculumManager.Views.CertificateDetails.EditCertificateView({
          model: that.model,
          activeTab: activeTab
        });
        var editModalView = new valamisApp.Views.ModalView({
          template: '#curriculumManagerEditModalTemplate',
          contentView: editView,
          submit: function () {
            editView.saveModel(false);
          },
          beforeCancel: function() {
            that.model.fetch().then(function() {
              that.model.unset('logoSrc');
              that.model.trigger('itemSaved');
            });
          },
          onDestroy: function() {
            that.model.trigger('updateGoalsCount');
            editView.editGoals.currentView.setGoalsCount(that.model.get('goalsCount'));
            valamisApp.execute('portlet:unset:onbeforeunload');
          }
        });
        valamisApp.execute('modal:show', editModalView);
        valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
      });
    },
    editDetails: function() {
      this.editCertificate(EDIT_TABS.DETAILS);
    },
    editGoals: function() {
      this.editCertificate(EDIT_TABS.GOALS);
    },
    editMembers: function() {
      this.editCertificate(EDIT_TABS.MEMBERS);
    },
    cloneCertificate: function() {
      this.model.clone({}).then(function (res) {
        curriculumManager.execute('certificates:reload');
      }, function (err, res) {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    activateCertificate: function() {
      var that = this;
      this.model.set('actionsDisabled', true);

      this.model.activate({}, {
        certificateId: that.model.get('id')
      }).then(function() {
        that.model.set('actionsDisabled', false);
        that.model.set('isActive', true);
        that.model.trigger('updateGoalsCount');
      }, function() {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    confirmDeactivate: function() {
      var that = this;
      valamisApp.execute('valamis:confirm', { message: Valamis.language['warningDeactivateCertificateMessageLabel'] }, function(){
        that.deactivateCertificate();
      });
    },
    deactivateCertificate: function() {
      var that = this;
      this.model.set('actionsDisabled', true);

      this.model.deactivate({}).then(function (res) {
        that.model.set('actionsDisabled', false);
        that.model.set('isActive', false);
      }, function (err, res) {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    confirmDelete: function() {
      var that = this;
      valamisApp.execute('valamis:confirm', { message: Valamis.language['warningDeleteCertificateMessageLabel'] }, function(){
        that.deleteCertificate();
      });
    },
    deleteCertificate: function() {
      this.model.destroy({
        success: function (model, response) {
          valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
          curriculumManager.execute('certificates:reload');
        },
        error: function (model, response) {
          toastr.error(that.language['overlayFailedMessageLabel']);
        }
      });
    },
    exportCertificate: function() {
      window.location = path.root + path.api.files + 'export/?action=EXPORT&contentType=certificate'
        + '&companyID=' + curriculumManager.curriculumCompanyId + '&id=' + this.model.get('id');
    }
  });

  CertificatesList.CertificatesList = Marionette.CollectionView.extend({
    className: 'js-certificate-items val-row',
    childView: CertificatesList.CertificatesItemView,
    initialize: function (options) {
      this.paginatorModel = options.paginatorModel;
      curriculumManager.settings.on('change:displayMode', this.setDisplayMode, this);
    },
    setDisplayMode: function() {
      var displayMode = curriculumManager.settings.get('displayMode')|| DISPLAY_TYPE.LIST;
      this.$el.removeClass('list');
      this.$el.removeClass('tiles');
      this.$el.addClass(displayMode);
    },
    onRender: function() {
      this.setDisplayMode();
    }
  });

  CertificatesList.AppLayoutView = Marionette.LayoutView.extend({
    template: '#curriculumManagerLayoutTemplate',
    regions: {
      'toolbar': '#curriculumManagerToolbar',
      'certificatesList': '#curriculumManagerCertificates',
      'paginator': '#curriculumManagerPaginator',
      'paginatorShowing': '#curriculumManagerPaginatorShowing'
    },
    childEvents: {
      'toolbar:displaymode:change': function(childView, displayMode) {
        curriculumManager.settings.set('displayMode', displayMode);
        curriculumManager.settings.save();
        valamisApp.execute('update:tile:sizes', this.certificatesList.currentView.$el);
      },
      'toolbar:certificate:create': function() {
        var newCertificate = new curriculumManager.Entities.CertificateModel();
        var createView = new curriculumManager.Views.CertificateDetails.EditCertificateView({
          model: newCertificate,
          activeTab: EDIT_TABS.DETAILS
        });
        var createModalView = new valamisApp.Views.ModalView({
          template: '#curriculumManagerEditModalTemplate',
          contentView: createView,
          submit: function(){
            createView.saveModel(false);
          },
          onDestroy: function() {
            valamisApp.execute('portlet:unset:onbeforeunload');
          }
        });

        valamisApp.execute('modal:show', createModalView);
        valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
      }
    },
    initialize: function() {
      var that = this;
      that.paginatorModel = curriculumManager.paginatorModel;
      that.certificates = curriculumManager.certificates;

      that.certificates.on('certificateCollection:updated', function (details) {
        that.updatePagination(details);
      });
    },
    onRender: function() {

      var toolbarView = new CertificatesList.ToolbarView({
        model: curriculumManager.filter
      });
      this.toolbar.show(toolbarView);

      var certificatesListView = new CertificatesList.CertificatesList({
        collection: this.certificates,
        paginatorModel: this.paginatorModel
      });
      certificatesListView.on('render:collection', function(view) {
        valamisApp.execute('update:tile:sizes', view.$el);
      });
      this.certificatesList.show(certificatesListView);

      this.paginatorView = new ValamisPaginator({
        language: Valamis.language,
        model : this.paginatorModel,
        topEdgeParentView: this,
        topEdgeSelector: this.regions.paginatorShowing
      });
      this.paginatorView.on('pageChanged', function () {
        curriculumManager.execute('certificates:reload');
      }, this);

      var paginatorShowingView = new ValamisPaginatorShowing({
        language: Valamis.language,
        model: this.paginatorModel
      });
      this.paginator.show(this.paginatorView);
      this.paginatorShowing.show(paginatorShowingView);

      curriculumManager.execute('certificates:reload');

    },
    updatePagination: function (details, context) {
      this.paginatorView.updateItems(details.total);
    }
  });


});