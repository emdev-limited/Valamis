var MyLessons = Marionette.Application.extend({
  channelName:'myLessons',
  initialize: function() {
    this.addRegions({
      mainRegion: '#myLessonsAppRegion'
    });
  },
  onStart: function(){
    var layoutView = new myLessons.Views.AppLayoutView();
    this.mainRegion.show(layoutView);
  }
});

var myLessons = new MyLessons();
