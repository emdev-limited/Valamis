var RecentLessons = Marionette.Application.extend({
  channelName:'recentLessons',
  initialize: function() {
    this.addRegions({
      mainRegion: '#recentLessonsAppRegion'
    });
  },
  onStart: function(options){
    this.recent = new recentLessons.Entities.RecentCollection();
    this.recent.on('sync', this.showContent, this);
    this.recent.fetch();
  },
  showContent: function() {
    this.recent.each(function (model) {
      if (model.get('throughDate') != '' && model.get('throughDate') != 0) {
        var lang = Utils.getUserLocale();
        model.set('throughDate', moment(model.get('throughDate')).locale(lang).fromNow());
      }
    });

    var layoutView = new recentLessons.Views.AppLayoutView({collection: this.recent});
    this.mainRegion.show(layoutView);
  }
});

var recentLessons = new RecentLessons();