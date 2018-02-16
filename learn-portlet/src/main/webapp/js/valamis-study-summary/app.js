var ValamisStudySummary = Marionette.Application.extend({

  channelName: 'valamisStudySummary',
  initialize: function () {
    this.addRegions({
      mainRegion: '#valamisStudySummaryAppRegion'
    });
  },
  onStart: function (options) {
    var layoutView = new valamisStudySummary.Views.AppLayoutView({
      userModel: options.userModel,
      hideStatistic: options.hideStatistic
    });
    this.mainRegion.show(layoutView);
  }

});

var valamisStudySummary = new ValamisStudySummary();

