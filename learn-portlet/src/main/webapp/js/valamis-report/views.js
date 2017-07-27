valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var localeDateFormat = 'DD/MM/YYYY',
        fetchDateFormat = 'YYYY/MM/DD';

    var DROPDOWN = {
        WIDTH: 195,
        SHIFT_TOP: 25,
        SHIFT_LEFT: 19,
        SHIFT_RIGHT: 30
    };

    var availableReportTypes = {
        certificates: 'reportTypeCertificates',
        topLessons: 'reportTypeTopLessons',
        mostActiveUsers: 'reportTypeMostActiveUsers',
        averagePassingGrade: 'reportTypeAveragePassingGrade',
        numberOfLessonsAttempted: 'reportTypeNumberOfLessonsAttempted'
    };

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#ValamisReportLayoutTemplate',
        className: 'valamis-report-container',
        regions: {
            'periodSelector': '#valamisReportPeriodSelector',
            'exportSelector': '#valamisReportExportSelector',
            'loadingBlock': '#valamisReportLoading',
            'reportRenderer': '#valamisReportRenderer'
        },
        initialize: function () {
            this.reportType = this.options['reportType'] || availableReportTypes.certificates;
            this.reportsScope = this.options['reportsScope'] || 'currentCourse';
            this.userIds = this.options['userIds'] || [];

            this.model = new valamisReport.Entities.reportModel({
                'reportType': this.reportType,
                'reportsScope': this.reportsScope,
                'userIds': this.userIds
            });
        },
        childEvents: {
            'loading:finished': function () {
                this.loading = false;
                this.hideLoading();
            },
            'settings:changed': function (event, datesModel) {
                this.renderReport(datesModel);
            }
        },

        onRender: function () {
            this.buildPeriodSelectorMenu();
            this.buildExportSelectorMenu(this.periodView.collection.models[0]);
            this.renderReport(this.periodView.collection.models[0]);
        },

        onShow: function () {
            if (this.options.reportType != "reportTypeAveragePassingGrade"
                && this.options.reportType != "reportTypeNumberOfLessonsAttempted") {
                this.periodSelector.show(this.periodView);
            }
            this.exportSelector.show(this.exportView);
        },

        modelEvents: {
            'change': 'render'
        },

        buildPeriodSelectorMenu: function () {
            var periodSelectorData = {
                "id": "reportPeriod",
                "type": "dropdown",
                "visibility": true,
                "hotSelector": true,
                "data": "datePeriod",
                "currentOptionId": this.options['reportPeriodType'] || 'periodLastWeek',
                "options": [
                    {
                        "id": "periodLastWeek",
                        "days": 7
                    },
                    {
                        "id": "periodLastMonth",
                        "days": 30
                    },
                    {
                        "id": "periodLastYear",
                        "days": 365
                    },
                    {
                        "id": "periodCustom",
                        "groupFirstItem": true
                    }
                ],
                "startDate": this.options['reportPeriodStart'],
                "endDate": this.options['reportPeriodEnd']
            };

            if (periodSelectorData.currentOptionId != 'periodCustom') {
                var periodOption = _.find(periodSelectorData.options, function (e) {
                    return e.id === periodSelectorData.currentOptionId;
                });
                periodSelectorData.startDate = moment().subtract(periodOption.days, 'days').format(fetchDateFormat);
                periodSelectorData.endDate = moment().format(fetchDateFormat);
            }

            var periodSelectorCollection = new valamisReportSettings.Entities.ReportSettingsModel([periodSelectorData]);

            this.periodView = new Views.PeriodSelectorView({
                collection: periodSelectorCollection
            });
        },

        buildExportSelectorMenu: function (model) {
            var reportModel = new valamisReport.Entities.reportModel({
                reportType: this.reportType,
                reportsScope: this.reportsScope,
                userIds: this.userIds,
                startDate: model.get('startDate'),
                endDate: model.get('endDate'),
                availableReportTypes: availableReportTypes
            });
            this.exportView = new Views.ExportSelectorView({model: reportModel});
        },

        renderReport: function (datesModel) {

            if (this.loading) return;

            var that = this,
                reportView;

            var options = {
                model: this.model,
                startDate: datesModel.get('startDate').split('/').join('-'),
                endDate: datesModel.get('endDate').split('/').join('-')
            };

            if (this.reportType == availableReportTypes.certificates) {
                reportView = new Views.CertificateGraphView(options);
            }
            else if (this.reportType == availableReportTypes.topLessons) {
                reportView = new Views.topLessonsView(options);
            }
            else if (this.reportType == availableReportTypes.mostActiveUsers) {
                reportView = new Views.mostActiveUsersView(options);
            }
            else if (this.reportType == availableReportTypes.averagePassingGrade) {
                reportView = new Views.averagePassingGradeView(options);
            }
            else if (this.reportType == availableReportTypes.numberOfLessonsAttempted) {
                reportView = new Views.numberOfLessonsAttemptedView(options);
            }
            this.showLoading();
            that.reportRenderer.show(reportView);
        },

        showLoading: function () {
            this.loading = true;
            this.loadingView = new Views.LoadingView();
            this.loadingBlock.show(this.loadingView);
        },

        hideLoading: function () {
            this.loadingBlock.empty();
        }
    });

    Views.LoadingView = Marionette.ItemView.extend({
        template: '#loadingTemplate',
        className: 'loading-message-block',
        onDestroy: function () {
            this.triggerMethod('loading:finished');
        }
    });

    Views.PeriodSelectorView = Marionette.CollectionView.extend({
        className: 'report-selector',
        childView: valamisReportSettings.Views.ReportSettingFieldsetView,
        childEvents: {
            'dropdown:toggle': function (childView, model) {
                var activeStateClass = 'show-dropdown-menu';

                var dropdownElClicked = $('.js-dropdown-selector', childView.$el);
                var dropdownElClickedNewState = !(dropdownElClicked.hasClass(activeStateClass));

                this.$('.js-dropdown-selector').removeClass(activeStateClass);

                if (dropdownElClickedNewState) {
                    var dropdownPosition = {
                        left: $('.js-label', dropdownElClicked).width() - Math.round(DROPDOWN.WIDTH / 2) + DROPDOWN.SHIFT_LEFT,
                        top: DROPDOWN.SHIFT_TOP
                    };
                    $('.dropdown-wrapper', dropdownElClicked).css(dropdownPosition);

                    if (dropdownElClickedNewState) {

                        var dateSelectors = {
                            startSelector: childView.$('#period-selector-start'),
                            endSelector: childView.$('#period-selector-end')
                        };

                        $.each(dateSelectors, function (key, selector) {
                            selector.data('DateTimePicker').hide();
                        });
                    }
                }

                dropdownElClicked.toggleClass(activeStateClass, dropdownElClickedNewState);
            }
        }
    });

    Views.ExportSelectorView = Marionette.CompositeView.extend({
        className: 'report-selector',
        template: '#reportExportFieldset',
        childView: valamisReportSettings.Views.ReportExportFieldsetView,
        events: {
            'click .js-current-selection': 'toggleDropdown',
            'submit form': 'downloadReportExport'
        },
        templateHelpers: function () {
            return {
                isLimit: this.model.get("reportType") != availableReportTypes.certificates
            }
        },
        onRender: function () {
            this.$('.js-digits-only').valamisDigitsOnly();
        },
        downloadReportExport: function () {
            var limit = this.$('input[name="limit"]').val();
            if (!limit) {
                limit = 0;
            }
            var startDate = this.formatDate(this.model.get('startDate'));
            var endDate = this.formatDate(this.model.get('endDate'));
            var format = this.$('input[type=radio]:checked').val();

            this.model.set({
                'startDate': startDate,
                'endDate': endDate,
                'top': limit,
                'format': format
            });

            var _this = this;
            var $submitButton = this.$('.submit-button button');
            var $loadingMessage = this.$('.loading-message');
            $submitButton.fadeOut().promise().done(function () {
                $loadingMessage.show();
                _this.model.exportReport().then(function (data) {
                    $loadingMessage.hide();
                    $submitButton.show();
                    window.location.href = data.path;
                }, function (err, res) {
                    valamisApp.execute('notify', 'error', Valamis.language['requestFailedError']);
                    $loadingMessage.hide();
                    $submitButton.show();
                });
            });
            return false;
        },
        toggleDropdown: function () {
            var activeStateClass = 'show-dropdown-menu';

            var dropdownElClicked = $('.js-dropdown-selector', this.$el);
            var dropdownElClickedNewState = !(dropdownElClicked.hasClass(activeStateClass));

            dropdownElClicked.removeClass(activeStateClass);

            if (dropdownElClickedNewState) {
                var dropdownPosition = {
                    left: $('.js-label', dropdownElClicked).width() - Math.round(DROPDOWN.WIDTH) + DROPDOWN.SHIFT_RIGHT
                };
                $('.dropdown-wrapper', dropdownElClicked).css(dropdownPosition);
            }
            dropdownElClicked.toggleClass(activeStateClass, dropdownElClickedNewState);
        },
        formatDate: function (date) {
            var outputDateFormat = 'YYYY-MM-DD';
            return moment(date, fetchDateFormat).format(outputDateFormat);
        }
    });
});
