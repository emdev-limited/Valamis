var MyCertificates = Marionette.Application.extend({
  channelName:'myCertificates',
  initialize: function() {
    this.addRegions({
      mainRegion: '#myCertificatesAppRegion'
    });
  },
  onStart: function(){
    var layoutView = new myCertificates.Views.AppLayoutView();
    this.mainRegion.show(layoutView);
  }
});

var myCertificates = new MyCertificates();
