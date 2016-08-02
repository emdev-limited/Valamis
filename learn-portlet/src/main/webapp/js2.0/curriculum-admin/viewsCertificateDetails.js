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
      'change input[name="validPeriod"]': 'updateValidPeriod'
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

      var data = {
        title: title,
        description: description,
        scopeId: scopeId,
        validPeriod: validPeriod,
        validPeriodType: validPeriodType,
        publishBadge: publishBadge,
        shortDescription: shortDescription
      };

      return data;
    },
    onModelLogoChanged: function () {
      this.$('.js-logo').attr('src', this.model.get('logoSrc'));
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
    events: {
      'click .js-save-continue': 'saveContinue'
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

      var isNew = (this.model.get('id') == undefined);
      if (!isNew) {
        this.model.trigger('resetGoals');
        this.showTabs();
      }
    },
    onShow: function() {
      var that = this;
      this.$('#editCertificateTabs a[href="#editDetails"]').on('shown.bs.tab', function () {
        that.$('.js-submit-button').show();
        that.$('.js-save-continue').show();
        that.editDetailsView.focusTitle();
      });
      this.$('#editCertificateTabs a[href="#editGoals"]').on('shown.bs.tab', function () {
        that.$('.js-submit-button').hide();
        that.$('.js-save-continue').show();
      });
      this.$('#editCertificateTabs a[href="#editMembers"]').on('shown.bs.tab', function () {
        that.$('.js-submit-button').show();
        that.$('.js-save-continue').hide();
      });

      var activeTabSelector = '#edit' + this.options.activeTab;
      this.$('#editCertificateTabs a[href="'+ activeTabSelector +'"]').tab('show');
    },
    showTabs: function() {
      this.$('#editCertificateTabs a[href="#editGoals"]').removeClass('hidden');
      this.$('#editCertificateTabs a[href="#editMembers"]').removeClass('hidden');

      var editGoalsView = new curriculumManager.Views.CertificateGoals.EditGoalsView({
        collection: this.goalsCollection,
        certificateModel: this.model,
        isPublished: this.model.get('isPublished')
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
      that.editDetailsView.trigger('view:submit:image', function (name) {
          that.model.set(data);
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
          }, function(err, res) {
            valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
          })
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
            that.saveModelData(data, true, isContinue)
          },
          function() {
            valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
          }
        );
      else
        this.saveModelData(data, false, isContinue);
    },
    saveContinue: function() {
      this.saveModel(true);
    }
  });

});