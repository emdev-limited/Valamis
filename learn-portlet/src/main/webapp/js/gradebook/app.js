var Gradebook = Marionette.Application.extend({
  channelName:'gradebook',
  initialize: function() {
    this.addRegions({
      mainRegion: '#gradebookAppRegion'
    });
  },
  onStart: function(options){
    _.extend(this, options);

    TincanHelper.SetActor(JSON.parse(this.tincanActor));
    TincanHelper.SetLRS(JSON.parse(this.endpointData));

    var that = this;

    this.courseModel = new gradebook.Entities.CourseModel;
    this.coursesCollection = new gradebook.Entities.CoursesCollection();
    this.coursesCollection.showAllCoursesOption = true;

    this.userModel = new gradebook.Entities.UserModel(); // needed for attempts commenting

    this.navigationCollection = new gradebook.Entities.NavigationCollection();

    var d1 = jQueryValamis.Deferred();
    var d2 = jQueryValamis.Deferred();
    this.coursesCollection.fetch().then(function() { d1.resolve(); });
    this.userModel.fetch().then(function() { d2.resolve(); });

    jQueryValamis.when(d1, d2).then(function() {
      var currentCourseId = parseInt(Utils.getCourseId());

      var currentCourseModel = gradebook.coursesCollection.findWhere({ id: currentCourseId });
      // if course with current courseId doesn't exist in collection, show all courses
      gradebook.courseId = (!!currentCourseModel) ? currentCourseId : '';

      var layoutView = (gradebook.canViewAll)
        ? new gradebook.Views.AppLayoutView()
        : new gradebook.Views.UserAppLayoutView();

      that.mainRegion.show(layoutView);
      that.execute('gradebook:course:changed', gradebook.courseId);
    });
  },
  formatDate: function(date) {
    return jQueryValamis.datepicker.formatDate("dd MM yy", date);
  },
  formatTime: function(date) {
    return date.toLocaleTimeString();
  }
});

var gradebook = new Gradebook();

// handlers

gradebook.commands.setHandler('gradebook:course:changed', function(courseId){
  gradebook.courseId = courseId;
  gradebook.courseModel.set(
    gradebook.coursesCollection.findWhere({ id: courseId }).toJSON()
  );
});