var LearningReport = Marionette.Application.extend({
    channelName:'learningReport',
    initialize: function() {
        this.addRegions({
            mainRegion: '#learningReportAppRegion'
        });
    },
    onStart: function(options){

        _.extend(this, options);
        this.activeReportType = learningReport.Entities.REPORT_TYPES.LESSONS;

        var that = this;

        this.courseModel = new learningReport.Entities.CourseModel;
        this.coursesCollection = new learningReport.Entities.CoursesCollection();

        var d1 = jQueryValamis.Deferred();
        this.coursesCollection.fetch().then(function() { d1.resolve(); });

        this.commands.setHandler('report:course:changed', function(courseId){
            that.reportFilter[that.activeReportType].courseId = courseId;
            that.courseModel.set(
                that.coursesCollection.findWhere({ id: courseId }).toJSON()
            );
        });

        jQueryValamis.when(d1).then(function() {

            that.coursesCollection.add({
                'id': 0,
                'title' : Valamis.language['allCoursesLabel']
            });

            var currentCourseId = that.reportFilter[that.activeReportType].courseId;
            var currentCourseModel = that.coursesCollection.findWhere({ id: currentCourseId });

            if (!!currentCourseModel) {
                that.courseModel.set(currentCourseModel.toJSON());
            } else {
                that.execute('report:course:changed', that.coursesCollection.first().get('id'));
            }

            that.layoutView = new learningReport.Views.AppLayoutView(options);
            that.mainRegion.show(that.layoutView);
        });
    }
});

var learningReport = new LearningReport();