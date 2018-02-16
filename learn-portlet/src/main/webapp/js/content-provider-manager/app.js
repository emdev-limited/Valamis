var ContentProviderManager = Marionette.Application.extend({
    channelName: 'contentProviderManager',
    onStart: function (options) {
        var mainLayoutView = new contentProviderManager.Views.MainAppLayoutView();

        this.addRegions({
            mainAppRegion: '#contentProviderManagerAppRegion'
        });

        this.mainAppRegion.show(mainLayoutView);
    }
});

var contentProviderManager = new ContentProviderManager();