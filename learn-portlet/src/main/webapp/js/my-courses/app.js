var MyCourses = Marionette.Application.extend({
  channelName:'myCourses',
  initialize: function() {
    this.addRegions({
      mainRegion: '#myCoursesAppRegion'
    });
  },
  onStart: function(){
    var layoutView = new myCourses.Views.AppLayoutView();
    this.mainRegion.show(layoutView);
  }
});

var myCourses = new MyCourses();
