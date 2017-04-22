/***** Chart page *****/

var ValamisReport = Marionette.Application.extend({
  channelName:'valamisReport',
  initialize: function() {
    this.addRegions({
      mainRegion: '#valamisReportAppRegion'
    });
  },
  onStart: function(options){
    _.extend(this, options);
    this.layoutView = new valamisReport.Views.AppLayoutView(options);
    this.mainRegion.show(this.layoutView);
  }
});

var valamisReport = new ValamisReport();

/***** Settings Page *****/

var ValamisReportSettings = Marionette.Application.extend({
    channelName:'valamisReportSettings',
    initialize: function() {
      this.addRegions({
          mainRegion: '#valamisReportAppRegion'
      });
    },
    onStart: function(options){
      _.extend(this, options);
      this.layoutView = new valamisReportSettings.Views.AppLayoutView(options);
      this.mainRegion.show(this.layoutView);
    }
});

var valamisReportSettings = new ValamisReportSettings();