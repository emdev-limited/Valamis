certificateExpirationTracker.module('Entities', function (Entities, certificateExpirationTracker, Backbone, Marionette, $, _) {

    var ToolbarService = new Backbone.Service({
        targets: {
            'sendNotifications': {
                'path': function () {
                    return path.root + path.api.certificatesTracker + 'send-notifications/'
                },
                'data': function (model) {
                        var result = {};

                    if (model.get("startDate")) _.extend(result, { startDate: model.get("startDate")});
                    if (model.get("endDate")) _.extend(result, { endDate: model.get("endDate")});
                    if (model.get("scopeId")) _.extend(result, { scopeId: model.get("scopeId")});
                    if (model.get("userIds")) _.extend(result, { userIds: model.get("userIds")});
                    if (model.get("skipTake")) _.extend(result, { skipTake: model.get("skipTake")});
                    if (model.get("certificateIds")) _.extend(result, { certificateIds: model.get("certificateIds")});


                    _.extend(result, { courseId: Utils.getCourseId});

                    if (certificateExpirationTracker.trackerScope
                        && certificateExpirationTracker.trackerScope == certificateExpirationTracker.availableScopes.currentCourse) {
                        _.extend(result, { scopeId: Utils.getCourseId});
                    }

                    return result;
                },
                'method': 'post'
            }
        },
        sync: {
            'read': {
                'path': function () {
                    return Utils.getContextPath() + 'js/certificate-expiration-tracker/defaultSettings.json'
                },
                'method': 'get'
            }
        }
    });

    Entities.ToolbarModel = Backbone.Model.extend({
        parse: function (response) {
            if (response === undefined) return response;

            var periodSettings = '';

            var settings = response.certificateExpirationTracker;
            _.each(settings, function (item) {
                if (item.id == 'expirationPeriod') {
                    periodSettings = item;

                    _.each(periodSettings.options, function (option) {
                        option.timePeriodLabel = Valamis.language[option.id + 'Label'];
                    });
                }
            });
            return periodSettings;
        }
    }).extend(ToolbarService);

    var ExpirationItemsService = new Backbone.Service({
        url: path.root + path.api.certificatesTracker,
        sync: {
            'read': {
                'path': '',
                'data': function (collection, options) {
                    var result = {};

                    if (options.startDate) _.extend(result, { startDate: options.startDate});
                    if (options.endDate) _.extend(result, { endDate: options.endDate});
                    if (options.scopeId) _.extend(result, { scopeId: options.scopeId});
                    if (options.userIds) _.extend(result, { userIds: options.userIds});
                    if (options.skipTake) _.extend(result, { skipTake: options.skipTake});
                    if (options.certificateIds) _.extend(result, { certificateIds: options.certificateIds});
                    if (options.courseId) _.extend(result, { courseId: options.courseId});
                    if (certificateExpirationTracker.trackerScope
                        && certificateExpirationTracker.trackerScope == certificateExpirationTracker.availableScopes.currentCourse) {
                        _.extend(result, { scopeId: Liferay.ThemeDisplay.getSiteGroupId()});
                    }

                    return result;
                },
                'method': 'get'
            }
        }
    });

    var ExpirationItemService = new Backbone.Service({
        url: path.root + path.api.certificatesTracker,
        targets: {
            'sendNotification': {
                'path': 'send-notification/',
                'method': 'post'
            }
        }
    });

    Entities.ExpirationItemModel = Backbone.Model.extend(ExpirationItemService);

    Entities.CertificatesModel = Backbone.Model.extend(ExpirationItemsService);

    Entities.ExpirationItemsCollection = Backbone.Collection.extend({
        model: Entities.ExpirationItemModel
    });
});

certificateExpirationTrackerSettings.module('Entities', function (Entities, certificateExpirationTrackerSettings, Backbone, Marionette, $, _) {

    var SettingsService = new Backbone.Service({
        sync: {
            'read': {
                'path': function () {
                    return Utils.getContextPath() + 'js/certificate-expiration-tracker/defaultSettings.json'
                },
                'method': 'get'
            }
        },
        targets: {
            'updateSettings': {
                'path': function () {
                    return certificateExpirationTrackerSettings.actionURL
                },
                'data': function (options) {
                    return {
                        action: 'SaveAll',
                        trackerScope: options.get('trackerScope')
                    };
                },
                'method': 'post'
            }
        }
    });

    Entities.SettingsModel = Backbone.Model.extend({
        parse: function (response) {
            if (response === undefined) return response;

            var settings = response.certificateExpirationTracker;

            var trackerScopeSettings = _.extend({}, _.findWhere( settings, { id: 'trackerScope'}));

            var defaultScope = _.findWhere(trackerScopeSettings.options,
                { id: trackerScopeSettings['default']});

            var currentScopeId = certificateExpirationTrackerSettings.trackerScope;

            var currentScope = _.findWhere(trackerScopeSettings.options, { id: currentScopeId});

            currentScope = currentScope || defaultScope;

            if (!!currentScope) {
                currentScope.active = true;
            }

            return trackerScopeSettings;
        }
    }).extend(SettingsService);
});
