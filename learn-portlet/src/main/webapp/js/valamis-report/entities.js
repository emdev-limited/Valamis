valamisReport.module('Entities', function(Entities, valamisReport, Backbone, Marionette, $, _) {

    var reportModelService = new Backbone.Service({
        targets: {
            'getData': {
                'path': function (model, options) {
                    return model.get('url');
                },
                'data': function (model, options) {
                    return options.data
                },
                'method': 'get'
            }
        }
    });

    Entities.reportModel  = Backbone.Model.extend({
        defaults: {
            reportType: 'reportTypeCertificates'
        },
        initialize: function () {
            if (this.get('reportType') == 'reportTypeCertificates') {
                this.set({'url': path.root + path.api.report + 'certificate'});
            }
            else if (this.get('reportType') == 'reportTypeTopLessons') {
                this.set({'url': path.root + path.api.report + 'lesson'});
            }
            else if (this.get('reportType') == 'reportTypeMostActiveUsers') {
                this.set({'url': path.root + path.api.report + 'most-active-users'});
            }
        }
    }).extend(reportModelService);
});

valamisReportSettings.module('Entities', function(Entities, valamisReportSettings, Backbone, Marionette, $, _) {

    Entities.ReportOptionModel = Backbone.Model.extend({});

    Entities.ReportSelectorModel = Backbone.Collection.extend({
        model: Entities.ReportOptionModel
    });

    Entities.ReportSettingModel = Backbone.Model.extend({
        defaults: {
            primary: false
        }
    });

    var ReportSettingsService = new Backbone.Service({
        url: valamisReportSettings.actionURL,
        sync: {
            'read': {
                'path': function (model, options) {
                    return Utils.getContextPath() + 'js/valamis-report/defaultSettings.json'
                },
                'data': function (collection, options) {},
                'method': 'get'
            }
        },
        targets: {
            'updateSettings': {
                'path': valamisReportSettings.actionURL,
                'data': function (collection, options) {
                    return {
                        reportType: options.currentTypeId,
                        reportsScope: options.reportsScope,
                        userIds: options.userIds,
                        periodType: options.periodType,
                        startDate: options.periodStartDate,
                        endDate: options.periodEndDate
                    };
                },
                'method': 'post'
            }
        }
    });

    Entities.ReportSettingsModel = Backbone.Collection.extend({
        model: Entities.ReportSettingModel,
        parse : function(response, options) {
            if(response === undefined) return response;
            return response.report_settings;
        }

    }).extend(ReportSettingsService);


    // certificate users

    Entities.UserModel = Backbone.Model.extend({
        defaults: {
            selected: false
        }
    });

    var UsersCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.users,
                'data': function (collection, options) {

                    var order = options.filter.sort;
                    var sortBy = order.split(':')[0];
                    var asc = order.split(':')[1];

                    var params = {
                        courseId: Utils.getCourseId(),
                        isUserJoined: collection.isUserJoined,
                        withUserIdFilter: true,
                        sortBy: sortBy,
                        sortAscDirection: asc,
                        filter: options.filter.searchtext,
                        page: options.currentPage,
                        count: options.itemsOnPage
                    };

                    var organizationId = options.filter.orgId;
                    if (organizationId) {
                        _.extend(params, { orgId: organizationId });
                    }
                    var userIds = options.userIds;
                    _.extend(params, { userIds: (userIds) ? options.userIds : collection.userIds });

                    return params;
                },
                'method': 'get'
            }
        }
    });

    Entities.UsersCollection = Backbone.Collection.extend({
        model: Entities.UserModel,
        parse: function (response) {
            this.trigger('userCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        },
        getSelected: function() {
            return this.filter(function(item) { return item.get('selected') });
        }
    }).extend(UsersCollectionService);

});