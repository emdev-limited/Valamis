var ValamisActivities = Marionette.Application.extend({
  channelName:'valamisActivities',
  initialize: function() {
    this.addRegions({
      mainRegion: '#valamisActivitiesAppRegion'
    });
  },
  onStart: function(options){
    this.userInfoModel = new valamisActivities.Entities.LiferayUserModel;

    var that = this;
    that.resourceURL = options.resourceURL;
    that.userInfoModel.fetch({
      'userId': Valamis.currentUserId,
      success: function() {
        var layoutView = new valamisActivities.Views.AppLayoutView({
          currentUserModel: that.userInfoModel,
          resourceURL: options.resourceURL,
          activitiesCount: options.activitiesCount
        });
        that.mainRegion.show(layoutView);
      }
    });
  }
});

var valamisActivities = new ValamisActivities();
