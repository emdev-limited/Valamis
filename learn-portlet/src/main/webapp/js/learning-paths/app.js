var LearningPaths = Marionette.Application.extend({
    channelName: 'learningPaths',
    initialize: function () {
        this.addRegions({
            mainRegion: '#learningPathsAppRegion'
        });
    },
    onStart: function (options) {
        _.extend(this, options);

        this.activities = new learningPaths.Entities.ActivitiesCollection();
        this.learningPathsCollection = new learningPaths.Entities.LearningPathCollection([], {
            useSkipTake: true
        });

        var that = this;
        this.activities.fetch().then(function () {
            that.learningPathsCollection.fetchMore().then(function () {
                that.showContent();
            })
        });
    },
    showContent: function () {
        var layoutView = new learningPaths.Views.AppLayoutView({
            activities: this.activities,
            collection: this.learningPathsCollection
        });
        this.mainRegion.show(layoutView);
    }
});

var learningPaths = new LearningPaths();
