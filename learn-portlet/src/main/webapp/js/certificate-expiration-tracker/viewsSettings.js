certificateExpirationTrackerSettings.module('Views', function (Views, certificateExpirationTrackerSettings, Backbone, Marionette, $, _) {

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#certificateExpirationTrackerSettingsTemplate',
        className: 'valamis-certificate-expiration-tracker-container',
        regions: {
            'settingsRegion' : '#certificateExpirationTrackerSettingsRegion'
        },
        events: {
            'click .js-current-selection': 'dropdownToggle'
        },
        childEvents: {
            'settings:changed': function(trackerScope) {
                certificateExpirationTracker.trackerScope = trackerScope;
                this.render();
            }
        },
        onRender: function() {
            var that = this;
            this.settingsModel = new certificateExpirationTrackerSettings.Entities.SettingsModel();
            this.settingsModel.fetch().then(function() {
                var settingsView = new Views.SettingsView({model: that.settingsModel});
                that.settingsRegion.show(settingsView);
            });
        },
        dropdownToggle: function() {
            var dropdownWrapper = this.$('.dropdown-wrapper');
            dropdownWrapper.toggleClass('hidden');
        }
    });

    Views.SettingsView = Marionette.LayoutView.extend({
        template: '#expirationSettingsTemplate',
        events: {
            'click .js-dropdown-options li': 'updateSettings'
        },
        templateHelpers: function() {
            var options = this.model.get('options');
            _.each(options, function(i) {
                i.label = Valamis.language[i.id + 'SettingsLabel'];
            });

            return {
                extOptions: options,
                trackerActiveScopeLabel: _.findWhere(options, { active: true }).label
            }
        },
        updateSettings: function(e) {
            var trackerScope = $(e.target).parent().attr('data-value');

            this.model.updateSettings({
                trackerScope: trackerScope
            });

            certificateExpirationTrackerSettings.trackerScope = trackerScope;

            this.triggerMethod('settings:changed', trackerScope);
        }
    });

});
