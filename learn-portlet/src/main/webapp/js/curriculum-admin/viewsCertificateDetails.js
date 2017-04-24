curriculumManager.module('Views.CertificateDetails', function (CertificateDetails, CurriculumManager, Backbone, Marionette, $, _) {

  // edit details view
  CertificateDetails.EditDetailsView = Marionette.ItemView.extend({
    template: '#curriculumManagerEditDetailsTemplate',
    className: 'div-table val-info-table',
    templateHelpers: function() {
      return {
        courseId: Utils.getCourseId()
      }
    },
    behaviors: {
      ValamisUIControls: {},
      ImageUpload: {
        'postponeLoading': true,
        'getFileUploaderUrl': function(model) {
          return path.root + path.api.files + 'certificate/' + model.get('id') + '/logo';
        },
        'uploadLogoMessage': function() { return Valamis.language['uploadLogoMessage'];},
        'fileUploadModalHeader' : function() { return Valamis.language['uploadLogoLabel']; },
        'selectImageModalHeader' : function() { return Valamis.language['galleryLabel']; },
        'rootUrl': function() {return curriculumManager.rootUrl;}
      }
    },
    events: {
      'click .js-design-badge': 'designBadge',
      'click .js-edit-scope': 'editScope',
      'click .js-delete-scope': 'deleteScope',
      'change #openBadgeIntegration': 'updateOpenBadge',
      'change input[name="validPeriod"]': 'updateValidPeriod',
      'click .js-delete-logo': 'deleteLogo'
    },
    modelEvents: {
      'change:scope': 'setScopeTitle',
      'change:logoSrc': 'onModelLogoChanged'
    },
    onValamisControlsInit: function () {
      var hasValidPeriod = (this.model.get('periodType') !== 'UNLIMITED');

      if (hasValidPeriod) {
        this.$('#nonPermanentPeriod').prop('checked', true);
        this.$('.js-plus-minus').valamisPlusMinus('value', this.model.get('periodValue'));
        this.$('.js-valid-period-type').val(this.model.get('periodType'));
      }
      else {
        this.$('#permanentPeriod').prop('checked', true);
        this.disableValidPeriod();
      }
    },
    onRender: function() {
      this.setScopeTitle();
      this.updateDeleteLogoButton();
    },
    focusTitle: function() {
      this.$('.js-certificate-title').val(this.model.get('title')); // for cursor after last character
      this.$('.js-certificate-title').focus();
    },
    editScope: function() {
      var scopeSelectView = new valamisApp.Views.SelectSite.LiferaySiteSelectLayout({ singleSelect: true });

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['scopeLabel'],
        contentView: scopeSelectView
      });

      var that = this;
      scopeSelectView.on('liferay:site:selected', function(scope) {
        that.model.set('scope', scope.toJSON());
        valamisApp.execute('modal:close', modalView);
      });

      valamisApp.execute('modal:show', modalView);
    },
    deleteScope: function() {
      this.model.set({ 'scope': this.model.defaults.scope });
    },
    setScopeTitle: function() {
      var scopeName = (this.model.get('scope').id) ? this.model.get('scope').title : Valamis.language['instanceScopeLabel'];
      this.$('.js-scope-name').val(scopeName);
    },
    updateOpenBadge: function() {
      var isChecked = this.$('#openBadgeIntegration').is(':checked');
      this.$('.js-openBadgesDescription').toggleClass('hidden', !isChecked);
    },
    updateValidPeriod: function() {
      if (this.$('#nonPermanentPeriod').is(':checked'))
        this.enableValidPeriod();
      else if (this.$('#permanentPeriod').is(':checked'))
        this.disableValidPeriod();
    },
    disableValidPeriod: function () {
      this.$('.js-plus-minus').valamisPlusMinus('disable');
      this.$('.js-valid-period-type').attr('disabled', 'disabled');
    },
    enableValidPeriod: function () {
      this.$('.js-plus-minus').valamisPlusMinus('enable');
      this.$('.js-valid-period-type').attr('disabled', false);
    },
    saveModelValues: function() {
      var title = this.$('.js-certificate-title').val() || Valamis.language['newCertificatePlaceholderLabel'];
      var description = this.$('.js-certificate-description').val() || '';
      var scopeId = this.model.get('scope').id;

      var isPermanent = this.$('#permanentPeriod').is(':checked');
      var validPeriod = 0;
      var validPeriodType = 'UNLIMITED';
      if (!isPermanent) {
        validPeriod = this.$('.js-plus-minus').valamisPlusMinus('value');
        validPeriodType = this.$('.js-valid-period-type').val();
      }

      var publishBadge = this.$('#openBadgeIntegration').is(':checked');
      var shortDescription = this.$('.js-openbadges-description').val();

      return {
        title: title,
        description: description,
        scopeId: scopeId,
        validPeriod: validPeriod,
        validPeriodType: validPeriodType,
        publishBadge: publishBadge,
        shortDescription: shortDescription,
        logo: this.model.get('logo') || ''
      };
    },
    onModelLogoChanged: function () {
      this.$('.js-logo').attr('src', this.model.get('logoSrc'));
      this.updateDeleteLogoButton();
    },
    deleteLogo: function () {
      this.model.unset('logo');
      this.model.set('logoSrc', '');
    },
    updateDeleteLogoButton: function() {
      this.$('.js-delete-logo').toggleClass('hidden', !this.model.get('logo'));
    }
  });

  // edit certificate
  CertificateDetails.EditCertificateView = Marionette.LayoutView.extend({
    template: '#curriculumManagerEditCertificateTemplate',
    regions: {
      'editDetails': '#editDetails',
      'editGoals': '#editGoals',
      'editMembers': '#editMembers'
    },
    ui: {
      submit: '.js-submit-button',
      saveContinue: '.js-save-continue',
      tabs: '#editCertificateTabs a'
    },
    events: {
      'click @ui.saveContinue': 'saveContinue',
      'click @ui.tabs': 'showTab'
    },
    modelEvents: {
      'change:isActive': 'render',
      'change:deletedCount': 'onDeletedCountChanged',
      'change:hasGoalChanges': function() {
        this.render();
        this.$('#editCertificateTabs a[href="#editGoals"]').tab('show');
      }
    },
    initialize: function() {
      this.goalsCollection = new curriculumManager.Entities.GoalsCollection([], {
        certificateId: this.model.get('id')
      });

      if (!this.model.isNew())
        this.goalsCollection.fetch();
    },
    onRender: function() {
      this.editDetailsView = new CertificateDetails.EditDetailsView({ model: this.model });
      this.editDetails.show(this.editDetailsView);

      var isNew = !this.model.get('id');
      if (!isNew) {
        this.model.trigger('resetGoals');
        this.showTabs();
      }
    },
    showTab: function(e) {
      e.preventDefault();
      $(e.target).tab('show');
    },
    onShow: function() {
      var that = this;
      this.$('#editCertificateTabs a[href="#editDetails"]').on('shown.bs.tab', function () {
        that.ui.submit.show();
        that.$('.js-save-continue').show();
        that.editDetailsView.focusTitle();
      });
      this.$('#editCertificateTabs a[href="#editGoals"]').on('shown.bs.tab', function () {
        that.$('.js-save-continue').show();
      });
      this.$('#editCertificateTabs a[href="#editMembers"]').on('shown.bs.tab', function () {
        that.ui.submit.show();
        that.$('.js-save-continue').hide();
      });

      var activeTabSelector = '#edit' + this.options.activeTab;
      this.$('#editCertificateTabs a[href="'+ activeTabSelector +'"]').tab('show');
    },
    onDeletedCountChanged: function () {
      var editGoalsView = this.editGoals.currentView;
      if(editGoalsView) {
        var showChangesCheckbox = editGoalsView.ui.showChanges;
        if(this.model.get('deletedCount') > 0) {
          showChangesCheckbox.parent().removeClass('hidden');
        } else {
          var hasGoalChanges = editGoalsView.collection.filter(function (goal) {
              return !!goal.get('user');
            }).length > 0;
          if(hasGoalChanges) {
            showChangesCheckbox.parent().addClass('hidden');
            showChangesCheckbox.prop('checked', false);
            editGoalsView.toggleShowChanges();
          }
        }
      }
    },
    showTabs: function() {
      this.$('#editCertificateTabs a[href="#editGoals"]').removeClass('hidden');
      this.$('#editCertificateTabs a[href="#editMembers"]').removeClass('hidden');

      var editGoalsView = new curriculumManager.Views.CertificateGoals.EditGoalsView({
        collection: this.goalsCollection,
        certificateModel: this.model,
        isActive: this.model.get('isActive')
      });
      this.editGoals.show(editGoalsView);

      var editMembersView = new curriculumManager.Views.CertificateMembers.MembersView({
        certificateId: this.model.get('id'),
        goalsCollection: this.goalsCollection
      });
      editMembersView.on('members:list:update:count', function(total) {
        this.model.set('userCount', total)
      }, this);
      this.editMembers.show(editMembersView);
    },
    saveModelData: function(data, isNew, isContinue) {
      var that = this;
      that.model.save().then(function(res){
        if (isContinue) {
          if (isNew) that.showTabs();

          var activeTab = that.$('#editCertificateTabs li.active');
          activeTab.next('li').children('a').tab('show');
        }
        else {
          valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
        }

        if (!(that.model.collection)) {
          curriculumManager.execute('certificates:reload', true);
        }
        else {
          that.model.trigger('itemSaved');
        }

        that.model.trigger('updateGoalsCount');
      }, function(err, res) {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    saveModelLogo: function(data, isNew, isContinue) {
      var that = this;
      that.model.set(data);

      var d1 = $.Deferred();
      if (!that.model.get('logo')) {
        that.model.deleteLogo().then(
          function () { d1.resolve(); },
          function () { d1.reject(); }
        );
      }
      else {
        d1.resolve();
      }

      $.when(d1).then(function () {
        that.editDetailsView.trigger('view:submit:image', function () {
          that.saveModelData(data, isNew, isContinue)
        }, !that.model.get('logo'))
      }, function () {
        valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
      });
    },
    saveModel: function(isContinue) {
      var data = this.editDetails.currentView.saveModelValues();
      var isNew = this.model.isNew();
      var that = this;

      if (isNew)  // just save certificate, the fields will be updated after success
        that.model.save().then(
          function() {
            that.goalsCollection.certificateId = that.model.get('id');
            that.saveModelLogo(data, true, isContinue);
          },
          function() {
            valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
          }
        );
      else
        this.saveModelLogo(data, false, isContinue);
    },
    saveContinue: function() {
      this.saveModel(true);
    }
  });

  CertificateDetails.ActivateView = Marionette.ItemView.extend({
    template: '#curriculumManagerActivateGoalChangesTemplate',
    behaviors: {
      ValamisUIControls: {}
    }
  });

});