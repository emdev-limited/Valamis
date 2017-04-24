lessonViewer.module('Views.TincanContent', function (TincanContent, lessonViewer, Backbone, Marionette, $, _) {

  TincanContent.PlayerLayoutView = lessonViewer.Views.Content.PlayerLayoutView.extend({
    template: '#tincanPlayerLayoutTemplate',

    getHeaderHeight: function() {
      return this.$('.content-wrapper .content-header').outerHeight(true);
    },
    getFooterHeight: function() {
      return 0;
    },
    startLesson: function() {
      this.loadTincanPackage();
    },
    loadTincanPackage: function () {
      var player = this;
      $.ajax({
        type: 'POST',
        dataType: 'json',
        data: {'p_auth': Liferay.authToken},
        url: path.root + path.sequencing + 'Tincan/' + this.packageId,
        headers: {
          'X-CSRF-Token': Liferay.authToken
        },
        success: function (data) {
          player.openTincanPackage(data.launchURL, data.versionNumber, data.activityId);
        },
        error: function (jqXHR, status, err) {
          var errorMessage = (_.contains(jqXHR.responseText, 'unavailablePackageException'))
            ? Valamis.language['unavailablePackageMessageLabel']
            : Valamis.language['failedMessageLabel'];
          toastr.error(errorMessage);
          player.doExit()
        }
      });
    },
    openTincanPackage: function (launchUrl, version, activityId) {
      this.loadingIframeToggle(false);
      var src = '{0}SCORMData/{1}?locale={2}&{3}'
        .replace('{0}', lessonViewer.servletContextPath)
        .replace('{1}', launchUrl)
        .replace('{2}', Utils.getUserLocale())
        .replace('{3}', TincanHelper.getLaunchArguments(version, activityId));

      this.$('#playerDataOutput').attr('src', src);
    },
    buildTincanTree: function(collection) {
      this.buildLessonTree(collection)
    },
    finishLesson: function() {
        this.$('#playerDataOutput').attr('src', '');
        FinishPackageAttempt(false);
        lessonViewer.execute('player:session:end');
    }
  });

});