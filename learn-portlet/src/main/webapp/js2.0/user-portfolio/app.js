var UserPortfolio = Marionette.Application.extend({
  channelName: 'userPortfolio',
  initialize: function (options) {
    this.addRegions({
      mainRegion: '#userPortfolioAppRegion'
    });
  },
  onStart: function (options) {
    var layoutView = new userPortfolio.Views.AppLayoutView();
    this.mainRegion.show(layoutView);
  }
});

var userPortfolio = new UserPortfolio();