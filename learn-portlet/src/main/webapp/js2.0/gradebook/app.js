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

    this.coursesCollection = new gradebook.Entities.CoursesCollection();
    this.coursesCollection.showAllCourses = true;
    this.userModel = new gradebook.Entities.UserModel(); // needed for attempts commenting

    var d1 = jQueryValamis.Deferred();
    var d2 = jQueryValamis.Deferred();
    this.coursesCollection.fetch().then(function() { d1.resolve(); });
    this.userModel.fetch().then(function() { d2.resolve(); });

    jQueryValamis.when(d1, d2).then(function() {
      var currentCourseId = parseInt(Utils.getCourseId());

      var currentCourseModel = gradebook.coursesCollection.findWhere({ id: currentCourseId });
      // if course with current courseId doesn't exist in collection, show all courses
      gradebook.courseId = (currentCourseModel != undefined) ? currentCourseId : '';

      var layoutView = (gradebook.viewAll) ? new gradebook.Views.AppLayoutView()
        : new gradebook.Views.UserAppLayoutView();
      that.mainRegion.show(layoutView);
      that.execute('gradebook:course:changed', gradebook.courseId);
    });
  },
  formatDate: function(date) {
    return $.datepicker.formatDate("dd MM yy", date);
  },
  formatTime: function(date) {
    return date.toLocaleTimeString();
  }
});

var gradebook = new Gradebook();

// handlers

gradebook.commands.setHandler('gradebook:course:changed', function(courseId){
  gradebook.courseId = courseId;
  gradebook.courseModel = new gradebook.Entities.CourseModel(
    gradebook.coursesCollection.findWhere({ id: courseId }).toJSON()
  );

  if (gradebook.courseModel)
    gradebook.mainRegion.currentView.showOverview();  // todo avoid this kind of call

  // creating new collections to avoid collision when course was changed
  gradebook.usersCollection = new gradebook.Entities.UsersCollection();
  gradebook.lessonsCollection = new gradebook.Entities.LessonsCollection();
  gradebook.gradingCollection = new gradebook.Entities.GradingCollection();
  gradebook.assignmentCollection = new gradebook.Entities.AssignmentCollection();

});