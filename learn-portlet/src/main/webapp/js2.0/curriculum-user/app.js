var CurriculumUser = Marionette.Application.extend({
  channelName:'curriculumUser',
  initialize: function() {
    this.addRegions({
      mainRegion: '#curriculumUserAppRegion'
    });
  },
  onStart: function(options){
    _.extend(this, options);

    var layoutView = new curriculumUser.Views.CertificateList.AppLayoutView();
    this.mainRegion.show(layoutView);

    // router for opening certificate details from liferay search
    var Router = Backbone.Router.extend({
      routes: {
        '': 'index',
        'certificate/:id': 'certificateOpen'
      },

      index: function () {},

      certificateOpen: function (id) {
        curriculumUser.execute('certificates:show:details', id);
      }
    });
    this.router = new Router();

    if (!Backbone.History.started) Backbone.history.start();
  }
});

var curriculumUser = new CurriculumUser();

// handlers

curriculumUser.commands.setHandler('certificates:show:details', function(modelId, activeTab, callback) {
  var contentView = new curriculumUser.Views.CertificateDetails.MainLayoutView({
    modelId: modelId,
    activeTab: activeTab
  });
  var modalView = new valamisApp.Views.ModalView({
    template: '#curriculumUserModalTemplate',
    contentView: contentView,
    onDestroy: function() {
      curriculumUser.router.navigate('/');
    }
  });
  contentView.on('userjoint:changed', function() {
    if (callback != undefined &&  _.isFunction(callback))
      callback();
    valamisApp.execute('modal:close', modalView);
  });

  valamisApp.execute('modal:show', modalView);
});