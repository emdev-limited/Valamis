lessonViewer.module('Views.ScormContent', function (ScormContent, lessonViewer, Backbone, Marionette, $, _) {

  ScormContent.PlayerLayoutView = lessonViewer.Views.Content.PlayerLayoutView.extend({
    template: '#scormPlayerLayoutTemplate',

    initialize: function() {
      this.constructor.__super__.initialize.apply(this, arguments);

      window.API_1484_11 = new SCORM2004_4API();
      window.API = new SCORM12API();
    },

    getHeaderHeight: function() {
      return this.$('.content-wrapper .content-header').outerHeight(true);
    },
    getFooterHeight: function() {
      return this.$('.content-wrapper .content-footer').outerHeight(true);
    },

    startLesson: function() {
      this.initView();
      if (this.isSuspended == 'true') {
        this.loadAndResume();
      }
      else {
        this.load();
      }
    },

    load: function () {
      StartPackageAttempt(this.packageId, this.packageTitle, this.packageTitle);
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('start'));
      this.trigger('load');
    },
    loadAndResume: function () {
      StartPackageAttempt(this.packageId, this.packageTitle, this.packageTitle);
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('resumeAll'));
    },
    initView: function () {
      window.LearnAjax.setHeader('currentScormPackageID', this.packageId.toString());

      var organizationsData = window.LearnAjax.syncRequest(path.root + path.scormorganizations + 'package/' + this.packageId);
      this.organizationId = organizationsData[0].id;

      var activitiesData = window.LearnAjax.syncRequest(path.root + path.api.manifestactivities
        + '?packageID=' + this.packageId
        + '&organizationID=' + this.organizationId
      );
      this.buildScormTree(activitiesData);
    },
    buildScormTree: function (data) {
      var parsedData = ScormNodeNavigationCollection.prototype.parseTree(data);
      this.navigationNodeCollection = new ScormNodeNavigationCollection(parsedData);
      this.navigationNodeCollection.afterInitialization();

      this.buildLessonTree(this.navigationNodeCollection);

      var that = this;
      this.listenTo(this.navigationNodeCollection,'navigate',function(model){
        that.doChoice(model.get('id'));
      });
    },

    loadView: function (data) {
      var that = this;
      function hideNavigationControls(control) {
        switch (control) {
          case 'continue':
            that.$('#playerNavigationForward').hide();
            break;
          case 'previous':
            that.$('#playerNavigationBackward').hide();
            break;
          case 'exitAll':
            that.$('.js-player-navigation-exit').hide();
            break;
          case 'suspendAll':
            that.$('.js-player-navigation-suspend').hide();
            break;
          default:
            break;
        }
      }

      function showNavigationControls() {
        that.$('#playerNavigationForward').show();
        that.$('#playerNavigationBackward').show();
        that.$('.js-player-navigation-exit').show();
      }

      if (data.endSession) {
        lessonViewer.execute('player:session:end');

        FinishPackageAttempt(true);
        this.cancelFullscreen();
      }

      if (data.currentActivity && !data.endSession) {
        this.selectNode(data.currentActivity);

        // Bug in firefox. OpticalIllusions are not shown.
        if (navigator.userAgent.indexOf('Firefox') == -1)
          this.$('#playerDataOutput').attr('src', data.activityURL);

        API_1484_11.setActivity(this.packageId, this.organizationId, data.currentActivity);
        API.setActivity(this.packageId, this.organizationId, data.currentActivity);
        SetActivity(data.activityURL, data.activityTitle, data.activityDesc);

        showNavigationControls();
        if (data.hiddenUI) {
          for (var i = 0; i < data.hiddenUI.length; i++) {
            hideNavigationControls(data.hiddenUI[i]);
          }
        }
        if(navigator.userAgent.indexOf('Firefox') != -1)
          this.$('#playerDataOutput').attr('src', data.activityURL);
      } else {
        if (this.onSuspend) {
          this.onSuspend = false;
          lessonViewer.execute('player:session:end');
        }
        this.$('#playerDataOutput').attr('src', '');
      }
    },
    selectNode: function (id) {
      this.navigationNodeCollection.toggle(id);
    },
    getNavigationRequestURL: function (requestType) {
      return path.root + path.sequencing +  'NavigationRequest/' + this.packageId + '/'
        + this.organizationId + '/' + requestType;
    },
    doSuspend: function () {
      // SCORM 1.2, ignore auto doContinue on LMSFinish
      window.API.silenceFinish();
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('suspendAll'));
      this.onSuspend = true;
      FinishPackageAttempt(false);
      this.cancelFullscreen();
      window.frames.top.ValamisTick = null; //Destroy valamis tick variable, so that opening package doesn't continue countdown
      navigationProxy.destroyNavigation();  // todo prepare exit
    },
    doPrevious: function () {
      // SCORM 1.2, ignore auto doContinue on LMSFinish
      window.API.silenceFinish();
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('previous'));
    },
    doContinue: function () {
      // SCORM 1.2, ignore auto doContinue on LMSFinish
      window.API.silenceFinish();
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('continue'));
    },
    doChoice: function (id) {
      // SCORM 1.2, ignore auto doContinue on LMSFinish
      window.API.silenceFinish();
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('choice{' + id + '}'));
    },
    doJump: function (id) {
      // SCORM 1.2, ignore auto doContinue on LMSFinish
      window.API.silenceFinish();
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('jump{' + id + '}'));
    },
    finishLesson: function() {
      //  // SCORM 1.2, ignore auto doContinue on LMSFinish
      window.API.silenceFinish();
      this.$('#playerDataOutput').attr('src', this.getNavigationRequestURL('exitAll'));
      FinishPackageAttempt(true);

      delete window.API_1484_11;
      delete window.API;
    }
  });

});