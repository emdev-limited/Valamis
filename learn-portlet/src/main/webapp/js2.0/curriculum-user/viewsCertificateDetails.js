curriculumUser.module('Views.CertificateDetails', function (CertificateDetails, CurriculumUser, Backbone, Marionette, $, _) {

  CertificateDetails.DetailsView = Marionette.ItemView.extend({
    template: '#curriculumUserCertificateDetailsViewTemplate',
    templateHelpers: function() {
      var labelText = (this.model.get('userStatus'))
        ? Valamis.language[this.model.get('userStatus').toLowerCase() + 'StatusLabel']
        : '';
      var isPermanent = this.model.get('periodType') === 'UNLIMITED';
      var validityPeriodText = (!isPermanent)
        ? Valamis.language['validForLabel'] + ' '
          + this.model.get('periodValue') + ' ' + this.model.get('periodType')
        : '';

      return {
        courseId: Utils.getCourseId(),
        labelText : labelText,
        isPermanent: isPermanent,
        isSuccess: this.model.get('status') === 'Success',
        validityPeriodText: validityPeriodText
      }
    },
    events: {
      'click .js-issue-badge': 'issueBadge',
      'click .js-join-certificate': 'joinCertificate',
      'click .js-leave-certificate': 'leaveCertificate'
    },
    issueBadge: function() {
      OpenBadges.issue(
        [
          'http://' + curriculumUser.rootUrl + path.root + path.api.certificates + this.model.id
          + '/issue_badge?userId=' + Utils.getUserId()
          + '&rootUrl=' + curriculumUser.rootUrl
        ],
        function (error, success) {}
      );
    },
    joinCertificate: function () {
      var that = this;
      this.model.join().then(
        function (res) {
          that.triggerMethod('details:userjoint:changed');
        },
        function (err, res) {
          valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
        }
      );
    },
    leaveCertificate: function() {
      var that = this;
      this.model.leave().then(
        function (res) {
          that.triggerMethod('details:userjoint:changed');
        },
        function (err, res) {
          valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
        }
      );
    }
  });

  CertificateDetails.GoalsItemView = Marionette.CompositeView.extend({
    tagName: 'li',
    childViewContainer: '.js-items',
    childViewOptions: function() {
      return {
        isUserJoined: this.options.isUserJoined
      }
    },
    templateHelpers: function () {
      var status = this.model.get('status');
      return {
        isUserJoined: this.options.isUserJoined,
        statusItemLabelText: (status) ? Valamis.language[status.toLowerCase() + 'StatusLabel'] : ''
      };
    },
    initialize: function() {
      this.collection = this.model.get('collection');

      this.template = (this.model.get('isGroup'))
        ? '#curriculumUserCertificateGroupGoalItemViewTemplate'
        : '#curriculumUserCertificateGoalItemViewTemplate';
    },
    onRender: function () {
      this.$('.js-period-type').val(this.model.get('periodType'));
    }
  });

  CertificateDetails.GoalsCollectionView = Marionette.CompositeView.extend({
    template: '#curriculumUserCertificateGoalsCollectionViewTemplate',
    childView: CertificateDetails.GoalsItemView,
    childViewContainer: '.js-certificate-goals',
    childViewOptions: function() {
      return {
        isUserJoined: this.options.isUserJoined
      }
    },
    templateHelpers: function() {
      return {
        isUserJoined: this.options.isUserJoined
      }
    },
    onRender: function() {
      this.$('.js-no-goals-label').toggleClass('hidden', this.collection.length > 0);
      this.$('.js-certificate-goals-header').toggleClass('hidden', this.collection.length == 0);
    }
  });

  CertificateDetails.MainLayoutView = Marionette.LayoutView.extend({
    template: '#curriculumUserModalMainLayoutViewTemplate',
    regions: {
      'certificateDetails': '#certificateDetails',
      'certificateGoals': '#certificateGoals'
    },
    childEvents: {
      'details:userjoint:changed': function (childView, displayMode) {
        this.trigger('userjoint:changed');
      }
    },
    initialize: function() {
      this.model = new curriculumUser.Entities.CertificateModel({ id: this.options.modelId });
    },
    onRender: function() {
      var that = this;
      this.model.fetch().then(function () {
        that.showDetails();
      });
    },
    showDetails: function() {
      var detailsView = new CertificateDetails.DetailsView({
        model: this.model
      });
      this.certificateDetails.show(detailsView);

      var that = this;
      this.goalsCollection = new curriculumUser.Entities.GoalsCollection(
        [],
        { certificateId: this.model.get('id') }
      );
      this.goalsCollection.on('sync', function() {
        if (this.model.get('isJoined')) {
          this.model.getUserGoalsStatuses().then(function(statuses) {
            that.goalsCollection.setUserStatuses(statuses);
            that.showGoalsCollection();
          });
        }
        else
          this.showGoalsCollection();
      }, this);
      this.goalsCollection.fetch();
    },
    onShow: function() {
      var activeTab = this.options.activeTab;
      if (activeTab) {
        var activeTabSelector = '#certificate' + activeTab;
        this.$('#certificateDatailsTabs a[href="' + activeTabSelector + '"]').tab('show');
      }
    },
    showGoalsCollection: function() {
      var goalsView = new CertificateDetails.GoalsCollectionView({
        collection: this.goalsCollection,
        isUserJoined: this.model.get('isJoined')
      });
      this.certificateGoals.show(goalsView);
    }
  });

});