lessonViewer.module('Views.Content', function (Content, lessonViewer, Backbone, Marionette, $, _) {

  Content.PlayerTreeItem = Marionette.CompositeView.extend({
    events: {
      'click': 'onClick'
    },
    templateHelpers: function() {
      return { hasChildren: this.hasChildren }
    },
    childViewContainer: '.js-lesson-items',
    modelEvents: {
      'change:active': 'render',
      'change:collapsed': 'toggleCollapsed'
    },
    initialize: function() {
      var children = this.model.get('childElements');

      this.template = (children != undefined)
        ? '#navigationCategoryItemTemplate'
        : '#navigationItemTemplate';

      this.hasChildren = (this.model.get('elementType') == 'directory')
        && (this.model.internalCollection.length > 1);

      if (this.hasChildren) {
        this.$el.addClass('directory-content val-icon-arrow-down');
      }

      this.collection = this.model.internalCollection;
    },
    onClick: function(e){
      e.stopPropagation();
      if (this.hasChildren) {
        this.model.set('collapsed', !this.model.get('collapsed'));
      }
      else {
        this.model.navigateToSlide();
      }
    },
    toggleCollapsed: function() {
      var collapsed = this.model.get('collapsed');
      var elem = this.$el;
      elem.toggleClass('collapsed', collapsed);
      elem.toggleClass('val-icon-arrow-right', collapsed);
      elem.toggleClass('val-icon-arrow-down', !collapsed);
      var arrowText = collapsed ? '508ShowListLabel' : '508HideListLabel';
      this.$('.arrow-text').text(Valamis.language[arrowText]);
    }
  });

  Content.PlayerTree = Marionette.CollectionView.extend({
    childView: Content.PlayerTreeItem
  });

  Content.PlayerLayoutView = Marionette.LayoutView.extend({
    regions: {
      'playerTreeRegion' : '#playerTree'
    },
    className: 'portlet-wrapper sidebar-hidden',
    events: {
      'click .js-player-navigation-exit': 'doExit',
      'click .js-player-navigation-suspend': 'doSuspend',
      'click #playerNavigationBackward': 'doPrevious',
      'click #playerNavigationForward': 'doContinue',
      'click .js-player-launch-fullscreen': 'fullScreen',
      'click .js-player-exit-fullscreen': 'cancelFullscreen'
    },
    initialize: function() {
      this.packageId = this.options.packageId;
      this.packageType = this.options.packageType;
      this.packageTitle = this.options.packageTitle;
      this.isSuspended = this.options.isSuspended;
      this.tincanActor = this.options.tincanActor;
      this.isSuspended = this.options.isSuspended;

      var that = this;
      jQueryValamis(window).resize(function(){
        if(that.iPadHack) {
          var element$ = that.getFullscreenElement();
          that.iPadSetElement2FullScreen(element$);
        }
        that.resizeIFrame();
      });
    },
    onRender: function () {
      this.loadingIframeToggle(false);
      var that = this;
      this.$('#playerDataOutput').on('load', function() {
        that.loadingIframeToggle(true);
        that.resizeIFrame();
        that.$('#playerDataOutput').focus();
      });
      this.$('.js-toggle-sidebar').valamisSidebar();

      this.startLesson();
      this.$('#currentPackageName').html(decodeURIComponent(this.packageTitle));
    },

    loadingIframeToggle: function(hidden){
      this.$('.content-container .spinner-container').toggleClass('hidden', hidden);
    },
    buildLessonTree: function(collection) {
      var playerTreeView = new lessonViewer.Views.Content.PlayerTree({
        collection: collection
      });
      this.playerTreeRegion.show(playerTreeView);
    },

    doExit: function () {
      this.loadingIframeToggle(false);
      this.finishLesson();

      this.cancelFullscreen();
      window.frames.top.ValamisTick = null; //Destroy valamis tick variable, so that opening package doesn't continue countdown
      navigationProxy.destroyNavigation();
    },
    resizeIFrame: function() {
      var windowHeight = window.innerHeight;
      var headerHeight = this.getHeaderHeight();
      var footerHeight = this.getFooterHeight();

      var iframeHeight = windowHeight - headerHeight - footerHeight;
      this.$('#playerDataOutput').height(iframeHeight);
    },


    iPadExitFullscreen: function() {
      this.iPadHack = false;
      var element$ = this.getFullscreenElement();
      $('.portlet-dockbar').show();  // todo is it necessary???
      this.iPadUnsetElement(element$);
    },
    toggleFullscreenButtons: function(isFullscreen) {
      this.$('.js-player-launch-fullscreen').toggleClass('hidden', isFullscreen);
      this.$('.js-player-exit-fullscreen').toggleClass('hidden', !isFullscreen);
    },
    fullScreen: function () {
      var isFullscreen = true;
      this.toggleFullscreenButtons(isFullscreen);

      var element$ = this.getFullscreenElement();
      var element = element$.get(0);

      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      } else {
        //iPad doesn't support any sort of fullscreen api .
        this.iPadHack = true;

        this.iPadSetElement2FullScreen(element$);
        $('.portlet-dockbar').hide();  // todo is it necessary???
      }
      this.resizeIFrame();
    },
    cancelFullscreenHelper: function () {
      var isFullscreen = false;
      this.toggleFullscreenButtons(isFullscreen);
      this.resizeIFrame();
    },
    cancelFullscreen: function () {
      this.cancelFullscreenHelper();

      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      } else {
        this.iPadExitFullscreen();
      }
    },
    getFullscreenElement: function(){
      return this.$el;
    },
    iPadSetElement2FullScreen: function(element$){
      element$.height($(window).height());
      element$.width($(window).width());
      element$.css('position', 'absolute').css('top', '0').css('left', '0');
      $(element$).parents('.val-portlet').css('position', 'initial');
    },
    iPadUnsetElement: function(element$) {
      element$.css('height', '').css('width', '').css('position', '').css('top', '').css('left', '');
      $(element$).parents('.val-portlet').css('position', 'relative');
    }
  });

});