valamisReport.module('Views', function (Views, valamisReport, Backbone, Marionette, $, _) {

    var localeDateFormat = 'DD/MM/YYYY',
        fetchDateFormat = 'YYYY/MM/DD';

    var DROPDOWN = {
        WIDTH: 195,
        SHIFT_TOP: 25,
        SHIFT_LEFT: 19
    };

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#ValamisReportLayoutTemplate',
        className: 'valamis-report-container',
        regions:{
            'periodSelector' : '#valamisReportPeriodSelector',
            'loadingBlock' : '#valamisReportLoading',
            'reportRenderer' : '#valamisReportRenderer'
        },
        initialize: function() {
            this.reportType = this.options['reportType'] || 'reportTypeCertificates';

            this.model = new valamisReport.Entities.reportModel(
                {
                    'reportType': this.reportType,
                    'reportsScope': this.options['reportsScope'] || 'currentCourse',
                    'userIds': this.options['userIds'] || '[]'
                }
            );
        },

        childEvents: {
            'loading:finished': function () {
                this.loading = false;
                this.hideLoading();
            },
            'settings:changed': function(event, datesModel) {
                this.renderReport(datesModel);
            }
        },

        onRender: function () {
            this.buildPeriodSelectorMenu();
            this.renderReport(this.periodView.collection.models[0]);
        },

        onShow: function() {
            this.periodSelector.show(this.periodView);
        },

        modelEvents: {
            'change': 'render'
        },

        buildPeriodSelectorMenu: function() {
            var periodSelectorData = {
                "id" : "reportPeriod",
                "type" : "dropdown",
                "visibility" : true,
                "hotSelector" : true,
                "data" : "datePeriod",
                "currentOptionId" : this.options['reportPeriodType'] || 'periodLastWeek',
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
                "startDate" : this.options['reportPeriodStart'],
                "endDate" : this.options['reportPeriodEnd']
            };

            if (periodSelectorData.currentOptionId != 'periodCustom') {
                var periodOption = _.find(periodSelectorData.options, function(e){ return e.id === periodSelectorData.currentOptionId; });
                periodSelectorData.startDate = moment().subtract(periodOption.days,'days').format(fetchDateFormat);
                periodSelectorData.endDate = moment().format(fetchDateFormat);
            };

            var periodSelectorCollection = new valamisReportSettings.Entities.ReportSettingsModel([periodSelectorData]);

            this.periodView = new Views.PeriodSelectorView({
                collection: periodSelectorCollection
            });
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

            if (this.reportType == 'reportTypeCertificates') {
                reportView = new Views.CertificateGraphView(options);
            }
            else if (this.reportType == 'reportTypeTopLessons') {
                reportView = new Views.topLessonsView(options);
            }
            else if (this.reportType == 'reportTypeMostActiveUsers') {
                reportView = new Views.mostActiveUsersView(options);
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
        onDestroy: function() {
            this.triggerMethod('loading:finished');
        }
    });

    Views.PeriodSelectorView = Marionette.CollectionView.extend({
        className: 'report-period-selector',
        childView: valamisReportSettings.Views.ReportSettingFieldsetView,
        childEvents: {
            'dropdown:toggle': function (childView, model) {
                var activeStateClass = 'show-dropdown-menu';

                var dropdownElClicked = $('.js-dropdown-selector', childView.$el);
                var dropdownElClickedNewState = !(dropdownElClicked.hasClass(activeStateClass));

                this.$('.js-dropdown-selector').removeClass(activeStateClass);

                if (dropdownElClickedNewState) {
                    var dropdownPosition = {
                        left: $('.js-label', dropdownElClicked).width() - Math.round(DROPDOWN.WIDTH/2) + DROPDOWN.SHIFT_LEFT,
                        top: DROPDOWN.SHIFT_TOP
                    };
                    $('.dropdown-wrapper', dropdownElClicked).css(dropdownPosition);

                    if (dropdownElClickedNewState) {

                        var dateSelectors = {
                            startSelector: childView.$('#period-selector-start'),
                            endSelector: childView.$('#period-selector-end')
                        };

                        $.each(dateSelectors, function(key, selector) {
                            selector.data('DateTimePicker').hide();
                        });
                    }
                }

                dropdownElClicked.toggleClass(activeStateClass, dropdownElClickedNewState);
            }
        }
    });


});