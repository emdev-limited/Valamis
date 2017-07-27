var CertificateExpirationTracker = Marionette.Application.extend({
    channelName: 'certificateExpirationTracker',
    initialize: function() {
        this.addRegions({
            mainRegion: '#certificateExpirationTrackerAppRegion'
        });
    },
    onStart: function(options) {
        _.extend(this, options);

        var that = this;
        var toolbarModel = new certificateExpirationTracker.Entities.ToolbarModel();
        toolbarModel.fetch().then( function() {
            that.layoutView = new certificateExpirationTracker.Views.AppLayoutView(_.extend(options, {toolbarModel: toolbarModel}));
            that.mainRegion.show(that.layoutView);
        });
    }
});

var certificateExpirationTracker = new CertificateExpirationTracker();

var CertificateExpirationTrackerSettings = Marionette.Application.extend({
    channelName:'certificateExpirationTrackerSettings',
    initialize: function() {
        this.addRegions({
            mainRegion: '#certificateExpirationTrackerAppRegion'
        });
    },
    onStart: function(options) {
        _.extend(this, options);
        this.layoutView = new certificateExpirationTrackerSettings.Views.AppLayoutView(options);
        this.mainRegion.show(this.layoutView);
    }
});

var certificateExpirationTrackerSettings = new CertificateExpirationTrackerSettings();
