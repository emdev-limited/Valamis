var LessonViewerSettings = Marionette.Application.extend({
  channelName:'lessonViewerSettings',
  initialize: function() {
    this.addRegions({
      mainRegion: '#lessonViewerSettingsAppRegion'
    });
  },
  onStart: function(options){
    _.extend(this, options);
    this.lessons = new lessonViewerSettings.Entities.LessonsCollection();
    var layoutView = new lessonViewerSettings.Views.LessonsListView({ collection: this.lessons });
    this.mainRegion.show(layoutView);
    this.lessons.fetch();
  }
});

var lessonViewerSettings = new LessonViewerSettings();