learningReport.module('Views', function (Views, learningReport, Backbone, Marionette, $, _) {

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#learningReportLayoutTemplate',
        className: 'valamis-learning-report-container',
        regions: {
            'learningReportLessonsTab': '#learningReportLessonsTab',
            'learningReportPathsTab': '#learningReportPathsTab'
        },
        ui: {
            tabLink: '.nav-tabs a'
        },
        events: {
            'click @ui.tabLink': 'toggleTabs'
        },
        onRender: function () {
            this.learningReportLessonsTabView = new Views.LearningReportTabView(this.options.reportFilter.lessonsReport);
        },
        showTabContent: function (tabView) {
            var tabRegion = (tabView.options.reportType == learningReport.Entities.REPORT_TYPES.LESSONS)
                ? this.learningReportLessonsTab
                : this.learningReportPathsTab;

            tabRegion.show(tabView);
            tabRegion.$el.addClass('active');
        },
        toggleTabs: function (e) {
            e.stopPropagation();
            e.preventDefault();

            var thisEl = $(e.target),
                changeMode = thisEl.attr('href') == '#learningReportPathsTab' ? 'switchToLP' : 'switchToLessons',
                tabSwitchEl = thisEl.parent('li');

            if (!tabSwitchEl.hasClass('active')) {

                tabSwitchEl.addClass('active');
                tabSwitchEl.siblings().removeClass('active');

                if (changeMode == 'switchToLP') {
                    this.showLearningPathsTab();
                } else {
                    this.showLessonsTab();
                }
            }
        },
        showLearningPathsTab: function () {
            learningReport.activeReportType = learningReport.Entities.REPORT_TYPES.PATHS;

            this.learningReportLessonsTab.$el.removeClass('active');
//            this.learningReportLessonsTab.empty({ preventDestroy: true });

            if (this.learningReportPathsTabView === undefined) {

                var currentCourseId = learningReport.reportFilter.pathsReport.courseId;
                var currentCourseModel = learningReport.coursesCollection.findWhere({ id: currentCourseId });

                if (!!currentCourseModel) {
                    learningReport.courseModel.set(currentCourseModel.toJSON());
                } else {
                    learningReport.execute('report:course:changed', learningReport.coursesCollection.first().get('id'));
                }

                this.learningReportPathsTabView = new Views.LearningReportTabView(this.options.reportFilter.pathsReport);
            } else {
                this.updateCourseSelector();
//                this.learningReportPathsTab.show(this.learningReportPathsTabView, { forceShow: true });
                this.learningReportPathsTab.$el.addClass('active');
            }
        },
        showLessonsTab: function () {
            learningReport.activeReportType = learningReport.Entities.REPORT_TYPES.LESSONS;
            this.updateCourseSelector();

            this.learningReportPathsTab.$el.removeClass('active');

//            this.learningReportPathsTab.empty({ preventDestroy: true });
//            this.learningReportLessonsTab.show(this.learningReportLessonsTabView, { forceShow: true });

            this.learningReportLessonsTab.$el.addClass('active');
        },
        updateCourseSelector: function () {
            if (learningReport.coursesCollection.length > 1) {
                learningReport.execute('report:course:changed',
                    learningReport.reportFilter[learningReport.activeReportType].courseId
                );
            }
        }
    });

    Views.LearningReportTabView = Marionette.LayoutView.extend({
        template: '#learningReportTabTemplate',
        regions:{
            'reportFilter' : '.js-learning-report-filter',
            'reportCourseSelector' : '.js-learning-report-course-selector',
            'reportLegend' : '.js-learning-report-legend',
            'reportDetailedViewLegend' : '.js-learning-report-detailed-view-legend',
            'reportDetailedViewHeader' : '.js-learning-report-detailed-view-header',
            'reportDataRenderer' : '.js-learning-report-data-renderer'
        },
        initialize: function() {
            var that = this;

            this.usersCountModel = new learningReport.Entities.usersCountModel();
            this.usersCountModel.fetch({
                currentCourseId: learningReport.reportFilter[this.options.reportType].courseId,
                reportType: this.options.reportType,
                success: function(model, response, options) {
                    learningReport.layoutView.showTabContent(that);
                }
            });
        },
        onRender: function () {

            var options = learningReport.reportFilter[this.options.reportType],
                coursesCollection = learningReport.coursesCollection.clone(),
                hideAllCoursesOption = true;

            if (learningReport.activeReportType == learningReport.Entities.REPORT_TYPES.PATHS) {
                if (coursesCollection.length > 2 || options.courseId == 0) {
                    hideAllCoursesOption = false;
                }
            }

            if (hideAllCoursesOption) {
                coursesCollection.remove(0);
            }

            if (coursesCollection.length > 1) {
                var courseSelectorView = new Views.CourseSelectorView({
                    courses: coursesCollection.toJSON()
                });
                this.reportCourseSelector.show(courseSelectorView);
            } else {
                var courseTitleView = new Views.CourseTitleView({
                    title: coursesCollection.models[0].get('title')
                });
                this.reportCourseSelector.show(courseTitleView);
            }

            var legendCollection = new learningReport.Entities.LegendCollection(
                (options.reportType == learningReport.Entities.REPORT_TYPES.LESSONS)
                    ? learningReport.Entities.LESSON_LEGEND
                    : learningReport.Entities.PATHS_LEGEND
            );

            var legendView = new Views.ReportLegendView({ collection: legendCollection });
            this.reportLegend.show(legendView);

            var detailedViewLegend = new learningReport.Entities.LegendCollection(
                (options.reportType == learningReport.Entities.REPORT_TYPES.LESSONS)
                    ? learningReport.Entities.LESSON_LEGEND_SLIDES
                    : learningReport.Entities.PATHS_LEGEND_GOALS
            );

            var legendDetailedView = new Views.ReportLegendView({ collection: detailedViewLegend, detailedViewMode: true});
            this.reportDetailedViewLegend.show(legendDetailedView);

            options.totalUsersCount = this.usersCountModel.get('result');

            this.filterView = new learningReport.Views.FilterView(options);
            this.reportFilter.show(this.filterView);

            this.datatableView = new learningReport.Views.DatatableLayoutView(options);
            this.reportDataRenderer.show(this.datatableView);
        },
        childEvents: {
            'legend:updated': function (childView) {
                this.datatableView.toggleStates(childView.model);
            },
            'filter:disable': function() {
                this.filterView.$('.js-dropdown-filter').addClass('disabled');
            },
            'filter:enable': function() {
                this.filterView.$('.js-dropdown-filter').removeClass('disabled');
            },
            'filter:update': function (childView, childModel) {
                this.datatableView.setFilter(
                    childView.reportSettingsCollection,
                    childModel.id,
                    childModel.get('currentValue')
                );
            },
            'filter:user:update': function (childView, childModel) {
                var that = this,
                    filterSettings = learningReport.reportFilter[learningReport.activeReportType];

                filterSettings.userFilter = childModel.get('currentValue');

                this.datatableView.showLoading();

                that.usersCountModel.fetch({
                    currentCourseId: filterSettings.courseId,
                    reportType: filterSettings.reportType,
                    filterString: filterSettings.userFilter,
                    success: function(model, response, options) {
                        that.datatableView.options.totalUsersCount = response.result;
                        that.datatableView.fetchUsersCollection(filterSettings);
                    }
                });
            },
            'filter:text:clear': function (childView) {
                this.filterView.reportSettingsCollection.each(function (model) {
                    if (model.get('type') == 'textSearch') {
                        model.set('currentValue', model.get('defaultValue'));
                    }
                });
            },

            'layout:change': function (childView, options) {
                var isDetailedMode = options.mode == 'detail';
                var dataRenderedView = this.datatableView;

                if (isDetailedMode) {
                    var headerModel = new learningReport.Entities.ReportDetailLayoutHeaderModel({
                        gridTitle: options.gridTitle,
                        courseTitle: learningReport.courseModel.get('title')
                    });
                    var headerView = new Views.ReportDetailLayoutHeaderView({model: headerModel});
                    this.reportDetailedViewHeader.show(headerView);

                    dataRenderedView.detailedPrimaryId = options.primaryId;
                } else {

                    dataRenderedView.reportDataHeaders.$el.removeClass('hidden');
                    dataRenderedView.reportPrimaryDatatable.$el.removeClass('hidden');
                    dataRenderedView.reportUsersSummary.$el.removeClass('hidden');
                    dataRenderedView.reportTotalBar.$el.removeClass('hidden');

                    dataRenderedView.reportDetailHeaders.reset();
                    dataRenderedView.reportDetailDatatable.reset();
                    dataRenderedView.reportUsersDetailSummary.reset();

                    dataRenderedView.reportDetailTotalBar.reset();
                    dataRenderedView.detailedPrimaryId = 0;
                }

                this.reportCourseSelector.$el.toggleClass('hidden', isDetailedMode);
                this.reportDetailedViewHeader.$el.toggleClass('hidden', !isDetailedMode);

                this.filterView.$('.js-dropdown-filter').toggleClass('disabled', isDetailedMode);
                this.filterView.activeFiltersRegion.$el.toggleClass('hidden', isDetailedMode);

                this.reportLegend.$el.toggleClass('hidden', isDetailedMode);
                this.reportDetailedViewLegend.$el.toggleClass('hidden', !isDetailedMode);

                dataRenderedView.$el.toggleClass('detail-layout', isDetailedMode);
            }
        }
    });

    Views.ReportDetailLayoutHeaderView = Marionette.ItemView.extend({
        template: '#learningReportDetailLayoutHeaderViewTemplate',
        ui: {
            backLink: '.js-back-link'
        },
        events: {
            'click @ui.backLink': 'toggleLayoutView'
        },
        toggleLayoutView: function () {
            this.triggerMethod('layout:change', {mode: 'course'});
        }
    });

    Views.ReportLegendItemView = Marionette.ItemView.extend({
        template: '#learningReportLegendItemTemplate',
        tagName: 'li',
        defaults: {
            detailedViewItem: false
        },
        className: function () { return 'legend-item-' + this.model.get('name') },
        templateHelpers: function () {
            var labelPrefix = '';

            if (learningReport.activeReportType == learningReport.Entities.REPORT_TYPES.LESSONS) {
                labelPrefix = this.model.get('detailedViewItem') ? 'slide' : 'lesson';
            } else {
                labelPrefix = this.model.get('detailedViewItem') ? 'goal' : 'path';
            }

            return {
                title: Valamis.language[labelPrefix + 'State' + this.model.get('labelKey') + 'Label']
            }
        },
        ui: {
            checkbox: 'input'
        },
        events: {
            'change @ui.checkbox': 'markOutStates'
        },
        markOutStates: function () {
            this.model.set({'isActive': this.ui.checkbox.prop('checked')});
            this.triggerMethod('legend:updated');
        }
    });

    Views.ReportLessonsLegendItemView = Marionette.ItemView.extend({
        template: '#learningReportLessonsLegendItemTemplate',
        tagName: 'li',
        className: function () {
            return 'legend-item-' + this.model.get('name');
        },
        templateHelpers: function () {
            var svgSymbol = '';

            switch (this.model.id) {
                case 4:
                    svgSymbol = 'half-book';
                    break;
                case 5:
                    svgSymbol = 'outline-book';
                    break;
                default:
                    svgSymbol = 'full-book';
            }

            return {
                title: Valamis.language['lessonState' + this.model.get('labelKey') + 'Label'],
                bookIconSrc: Utils.getContextPath() + 'img/book-icon.svg#' + svgSymbol
            }
        },
        ui: {
            label: 'label'
        },
        events: {
            'click @ui.label': 'markOutStates'
        },
        markOutStates: function () {
            this.ui.label.toggleClass('checked');
            this.model.set({'isActive': this.ui.label.hasClass('checked')});
            this.triggerMethod('legend:updated');
        }
    });

    Views.ReportLegendView = Marionette.CollectionView.extend({
        tagName: 'ul',
        getChildView: function(model) {
            return model.get('lessonLegendViewItem')
                ? Views.ReportLessonsLegendItemView
                : Views.ReportLegendItemView;
        },
        className: function () {
            return (learningReport.activeReportType == learningReport.Entities.REPORT_TYPES.LESSONS)
                ? 'learning-report-lessons-legend'
                : 'learning-report-paths-legend';
        },
        initialize: function (options) {
            if (options.detailedViewMode) {
                this.collection.each( function (model) {
                    model.set('detailedViewItem', true);
                });
            } else if (learningReport.activeReportType == learningReport.Entities.REPORT_TYPES.LESSONS) {
                this.collection.each( function (model) {
                    model.set('lessonLegendViewItem', true);
                });
            }
        }
    });

});