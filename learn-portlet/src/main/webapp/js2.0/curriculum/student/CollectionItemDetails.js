var CollectionItemDetailsView = Backbone.View.extend({
  events: {
    "click #issueBadge": "issueBadge",
    'click .js-leaveCertificateCommand': 'leaveCertificate',
    'click .js-joinCertificateCommand': 'joinCertificate'
  },
  initialize: function (options) {
    this.options = options;
    this.model = new CertificateModel();
    this.model.on('change', this.render, this);
  },

  setCertificateID: function (certificateID, status, isJoint) {
    this.model.set({ id: certificateID, status: status, success: status == 'Success', isJoint: isJoint});
    this.model.fetch();
  },

  render: function () {
    var that = this;
    var description = jQuery1816Curriculum('<i>').html(this.model.get('description')).text();
    var renderedTemplate = _.template(
      Mustache.to_html(
        jQuery('#userCertificateItemEditDetailsTemplate').html(),
        _.extend(this.model.toJSON(), that.options.language, {
          contextPath: Utils.getContextPath,
          description: description,
          courseId: Utils.getCourseId})));
    this.$el.html(renderedTemplate);

    this.setValidPeriod();

    return this;
  },

  setValidPeriod: function () {
    var validPeriod = this.model.get('validPeriod');

    if (validPeriod != undefined) {
      if (validPeriod.valueType == 'UNLIMITED') {
        this.$('#nonPermanentLabel').hide();
      }
      else {
        this.$('#permanentLabel').hide();
        this.$('#nonPermanentLabel').text(this.options.language['validForLabel'] + ' ' + validPeriod.value + ' ' + validPeriod.valueType);
      }
    }

  },
  issueBadge: function () {
    OpenBadges.issue(
        [
          'http://' + jQuery('#rootUrl').val() + path.root + path.api.certificates
            + this.model.id + '/issue_badge?userID='
            + jQuery('#curriculumStudentID').val() + '&rootUrl=' + jQuery('#rootUrl').val()
        ],
        function (error, success) {}
    );
  },
  joinCertificate: function () {
    var that = this;
    this.model.join({}).then(function (res) {
      that.trigger('reloadWithMessage');
    }, function (err, res) {
      toastr.error(that.language['overlayFailedMessageLabel']);
    });
  },
  leaveCertificate: function () {
    var that = this;
    this.model.leave({}).then(function (res) {
      that.trigger('reloadWithMessage');
    }, function (err, res) {
      toastr.error(that.language['overlayFailedMessageLabel']);
    });
  }

});