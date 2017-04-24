var LRS2ActivityMapperApp = new Marionette.Application();

LRS2ActivityMapperApp.addInitializer(function(options){
  var controls = new LRS2ActivityMapperApp.ViewModule.ControlsView({
    el: jQuery('.js-lrs-to-activity-controls'),
    language: options.language
  });
  controls.render();

  var collectionView = new LRS2ActivityMapperApp.ViewModule.ActivityMapperRowCollectionView({
    collection: LRS2ActivityMapperApp.request('event:list'), el: jQuery('.js-social-activities-mapper-data')
  });
  collectionView.render();
});