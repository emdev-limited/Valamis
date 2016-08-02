var AllCourses = Marionette.Application.extend({
    channelName: 'allCourses',
    onStart: function (options) {
        var isEditor = !!options.editor;
        this.addRegions({
            mainAppRegion: (isEditor) ? '#coursesManagerAppRegion' : '#coursesBrowserAppRegion'
        });

        var mainLayoutView = new allCourses.Views.MainAppLayoutView({ isEditor: isEditor });
        this.mainAppRegion.show(mainLayoutView);
    }
});

var allCourses = new AllCourses();