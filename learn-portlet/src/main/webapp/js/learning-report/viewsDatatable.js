learningReport.module('Views', function (Views, learningReport, Backbone, Marionette, $, _) {

    var CELL_MIN_WIDTH = 120,
        PAGE_BOX_WIDTH = 20,
        ORGANIZATION_TOOLTIP_WIDTH = 500;

    BOOK_ICON_SIZE = 26;

    if ($.tooltipster) {
        $.tooltipster.setDefaults({
            maxWidth: 220,
            theme: 'tooltipster-valamis',
            contentCloning: true,
            side: 'bottom',
            animationDuration: [350,150]
        });
    }

    Views.LoadingView = Marionette.ItemView.extend({
        template: '#loadingTemplate',
        className: 'loading-message-block',
        onDestroy: function() {
            this.triggerMethod('loading:finished');
        }
    });

    Views.DatatableLayoutView = Marionette.LayoutView.extend({
        template: '#learningReportDatatableLayoutTemplate',
        regions: {
            'reportUsersData': '#learningReportUserlist',
            'reportDataHeaders': '#learningReportDataHeaders',
            'reportPrimaryDatatable': '#learningReportDatatable',
            'reportDetailHeaders': '#learningReportDetailHeaders',
            'reportDetailDatatable': '#learningReportDetailGrid',
            'reportUsersSummary': '#learningReportUserSummary',
            'reportUsersDetailSummary': '#learningReportUserDetailSummary',
            'reportTotalBar': '#learningReportTotalBar',
            'reportDetailTotalBar': '#learningReportDetailTotalBar',
            'reportLoading' : '#learningReportLoading'
        },

        initialize: function () {
            this.isTotalBarSummaryFetched = false;
        },

        ui: {
            showMore: '.js-show-more'
        },

        triggers: {
            'click @ui.showMore': 'users:showMore'
        },

        showLoading: function () {
            this.loading = true;
            this.loadingView = new Views.LoadingView();
            this.reportLoading.show(this.loadingView);
            this.$el.addClass('loading');

            this.$('.js-learning-report-table').addClass('hidden');
            this.$('.js-no-users-message').addClass('hidden');
            this.$('.js-no-lessons-message').addClass('hidden');
            this.$('.js-no-paths-message').addClass('hidden');
            this.$('.js-empty-data-message').addClass('hidden');
            this.toggleShowMoreLink(false);
        },

        hideLoading: function () {
            this.loading = false;
            if (this.reportLoading) {
                this.reportLoading.empty();
            }
            this.$el.removeClass('loading');
        },

        toggleOverlay: function (show) {
            this.$el.toggleClass('show-overlay', show);
        },

        toggleStates: function(model) {
            var classPrefix = model.get('detailedViewItem') ? 'hilite-detailed-' : 'hilite-';
            this.$el.toggleClass(classPrefix + model.get('name'), model.get('isActive'));
        },

        onRender: function () {
            var that = this,
                currentReportFilter = learningReport.reportFilter[learningReport.activeReportType],
                courseId = currentReportFilter.courseId || 'all';

            this.lessonSlidesFetch = [];
            this.pathGoalsFetch = [];
            this.detailedPrimaryId = 0;
            this.showLoading();

            this.headerCollection = new learningReport.Entities.ReportHeaderCollection();
            this.headerCollection.fetch({
                courseId: courseId,
                reportType: learningReport.activeReportType,
                success: function(headerCollection, response, options) {
                    if (response.length > 0) {

                        that.fetchUsersCollection(currentReportFilter);
                        that.triggerMethod('filter:enable');

                        if (currentReportFilter.isSummaryVisible) {
                            that.$el.addClass('show-user-summary');
                            that.$('.js-learning-report-table').removeClass('total-bar-hidden');
                            that.fetchTotalBarCollection(currentReportFilter);
                        }

                    } else {
                        that.hideLoading();

                        var messageBlockClass =
                            learningReport.activeReportType == learningReport.Entities.REPORT_TYPES.LESSONS
                                ? '.js-no-lessons-message'
                                : '.js-no-paths-message';

                        that.$(messageBlockClass).removeClass('hidden');
                        that.$('.js-learning-report-table').addClass('hidden');
                        that.triggerMethod('filter:disable');
                    }
                }
            });
        },

        fetchTotalBarCollection: function (currentReportFilter) {
            var that = this;

            this.totalBarCollection = new learningReport.Entities.ReportTotalCollection();
            this.totalBarCollection.fetch({
                courseId: currentReportFilter.courseId || 'all',
                reportType: currentReportFilter.reportType,
                success : function(totalCollection, response, options) {

                    that.isTotalBarSummaryFetched = true;

                    that.headerCollection.each( function (headerModel) {
                        var totalResponseModel = totalCollection.findWhere({ id: headerModel.id });
                        if (totalResponseModel.get('total')) {
                            headerModel.set('total', totalResponseModel.get('total'));
                        }
                    });
                }
            });
        },

        fetchUsersCollection: function (currentReportFilter) {
            var that = this;

            if (this.usersCollection === undefined ) {
                this.usersCollection = this.isPathsReport()
                    ? new learningReport.Entities.ReportPathsUsersCollection()
                    : new learningReport.Entities.ReportUsersCollection();
            }

            this.usersCollection.fetch({
                courseId: currentReportFilter.courseId || 'all',
                userFilter: currentReportFilter.userFilter,
                reportType: learningReport.activeReportType,
                success : function(usersCollection, response, options) {
                    that.hideLoading();

                    that.headerCollection.each(function (headerModel) {
                        headerModel.set('detailsFetchedUsers', []);
                    });

                    if (response.length > 0) {

                        that.$('.js-learning-report-table').removeClass('hidden');

                        that.updateFilteredGrid(that.getFilterOptions(currentReportFilter));

                        that.off('users:showMore').on('users:showMore', function() {
                            that.fetchMoreUsers(currentReportFilter);
                        });

                    } else {
                        if (that.headerCollection.length > 0) {
                            that.$('.js-empty-data-message').removeClass('hidden');
                        }

                    }
                }
            });
        },

        isPathsReport: function() {
            return learningReport.activeReportType == learningReport.Entities.REPORT_TYPES.PATHS;
        },

        getFilterOptions: function(currentReportFilter) {
            var filterOptions = {
                'showSummary': currentReportFilter.isSummaryVisible,
                'headerFilter': currentReportFilter.headerFilter || ''
            };

            if (this.isPathsReport()) {
                filterOptions.pathGoalTypesFilter = currentReportFilter.pathGoalType;
            } else {
                filterOptions.questionTypeFilter = currentReportFilter.onlyLessonsWithQuestions;
            }

            return filterOptions;
        },

        fetchMoreUsers: function (currentReportFilter) {
            if (!this.loading) {

                var that = this,
                    fetchedUsersCount = this.usersCollection.models.length;

                if (this.isPathsReport()) {
                    this.moreUsersCollection = new learningReport.Entities.ReportPathsUsersCollection();
                } else {
                    this.moreUsersCollection = new learningReport.Entities.ReportUsersCollection();
                }

                this.showMoreLoading(true);

                this.moreUsersCollection.fetch({
                    courseId: currentReportFilter.courseId,
                    reportType: currentReportFilter.reportType,
                    skipUsersCount: fetchedUsersCount,
                    userFilter: currentReportFilter.userFilter,
                    success: function (usersCollection, response, options) {
                        that.showMoreLoading(false);
                        if (usersCollection.length > 0) {
                            if (that.detailedPrimaryId > 0) {
                                that.updateDetailedView(
                                    that.detailedPrimaryId,
                                    usersCollection,
                                    currentReportFilter
                                );
                            } else {
                                that.hideDetailsInPrimaryView();
                                that.usersCollection.add(usersCollection.toJSON());
                            }
                        }
                    },
                    error: function (collection, response) {
                        console.log(response);
                        that.showMoreLoading(false);
                        valamisApp.execute('notify', 'error', Valamis.language['loadReportFailed']);
                    }
                });
            }
        },

        showMoreLoading: function (isLoading) {
            this.loading = isLoading;
            this.ui.showMore.toggleClass('loading',isLoading);
        },

        hideDetailsInPrimaryView: function () {
            var triggerEventName = this.isPathsReport() ? 'goals:hide:inline' : 'pages:hide:inline';

            this.reportHeadersView.children.each(function(headerItemView){
                headerItemView.$el.removeClass('js-details-rendered');
                if (headerItemView.model.get('isDetailsShown')) {
                    headerItemView.triggerMethod(triggerEventName, headerItemView.model.id);
                }
            });
        },

        updateDetailedView: function (primaryId, usersCollection, currentReportFilter) {

            var that = this;

            this.hideDetailsInPrimaryView();

            var detailsCollection = this.isPathsReport()
                ? new learningReport.Entities.ReportUsersWithGoalsCollection()
                : new learningReport.Entities.ReportUsersWithPagesCollection();

            var detailsFetched = this.isPathsReport()
                ? this.pathGoalsFetch[primaryId] = $.Deferred()
                : this.lessonSlidesFetch[primaryId] = $.Deferred();

            this.toggleOverlay(true);

            $.when(detailsFetched).then(
                function() {

                    that.usersCollection.add(usersCollection.toJSON());

                    var usersCollectionWithDetails = that.isPathsReport()
                        ? that.getUsersCollectionWithPathGoals( primaryId, true )
                        : that.getUsersCollectionWithLessonPages( primaryId, true );

                    that.toggleOverlay(false);

                    that.reportDetailDatatable.currentView.collection.reset(usersCollectionWithDetails);

                    that.headerCollection.findWhere({'id': primaryId}).set({
                        'detailsFetchedUsers': _.map(usersCollectionWithDetails, 'userId')
                    });

                },
                function() {
                    that.toggleOverlay(false);
                }
            );

            this.fetchDetails(primaryId, detailsCollection, usersCollection.map('id'), usersCollection);
        },

        setFilter: function(filter, filterId, currentValue) {
            var filterOptions = {};

            if (filterId == 'lessonSearch' || filterId == 'pathSearch') {
                learningReport.reportFilter[learningReport.activeReportType].headerFilter = currentValue;
            }

            if (filterId == 'isSummaryVisible') {

                this.$el.toggleClass('show-user-summary', currentValue);
                this.$('.js-learning-report-table').toggleClass('total-bar-hidden', !currentValue);

                this.renderTotalBar(this.filteredHeaderCollection);

                if (!this.isTotalBarSummaryFetched) {
                    this.fetchTotalBarCollection(
                        learningReport.reportFilter[learningReport.activeReportType]
                    );
                }

            } else if (filter.length) {

                if (filter.reportType == learningReport.Entities.REPORT_TYPES.PATHS) {
                    filterOptions.pathGoalTypesFilter =
                        filter.findWhere({ id: 'pathGoalType'}).get('currentValue');
                    filterOptions.headerFilter =
                        filter.findWhere({ id: 'pathSearch'}).get('currentValue');
                } else {
                    filterOptions.questionTypeFilter =
                        Boolean(filter.findWhere({ id: 'lessonType'}).get('currentValue'));
                    filterOptions.headerFilter =
                        filter.findWhere({ id: 'lessonSearch'}).get('currentValue');
                }

                this.updateFilteredGrid(filterOptions);
            }
        },

        updateFilteredGrid: function (filterOptions) {

            if (this.headerCollection.length > 0 && this.usersCollection.length > 0) {

                this.filteredHeaderCollection = this.headerCollection.clone();

                // Filtering header collection
                if (Boolean(filterOptions.questionTypeFilter)) {
                    var lessonsWithQuestions = this.filteredHeaderCollection.filter(function(lessonModel) {
                        return lessonModel.get('hasQuestion');
                    });
                    this.filteredHeaderCollection.reset(lessonsWithQuestions);
                }

                if (Boolean(filterOptions.pathGoalTypesFilter)) {
                    var pathsWithSelectedGoalTypes = this.filteredHeaderCollection.filter(function(model) {
                        var selectedGoalType = filterOptions.pathGoalTypesFilter;
                        return _.indexOf(_.map(model.get('goals'), 'goalType'), selectedGoalType) > -1;
                    });
                    this.filteredHeaderCollection.reset(pathsWithSelectedGoalTypes);
                }

                if (Boolean(filterOptions.headerFilter)) {
                    var itemsFilteredByTitle = this.filteredHeaderCollection.filter(function(model) {
                        var pattern = new RegExp(filterOptions.headerFilter,'gi');
                        return pattern.test(model.get('title'));
                    });
                    this.filteredHeaderCollection.reset(itemsFilteredByTitle);
                }

                if (this.usersCollection.length > 0 && this.filteredHeaderCollection.length > 0) {

                    this.$('.js-empty-data-message').addClass('hidden');
                    this.$('.js-learning-report-table').removeClass('hidden');

                    // Rendering filtered collections
                    this.renderUsersColumn(this.usersCollection, learningReport.activeReportType);
                    this.renderUsersSummary(this.usersCollection);

                    this.renderHeader(this.filteredHeaderCollection);
                    this.renderReportBody(this.usersCollection, this.filteredHeaderCollection);

                    this.renderTotalBar(this.filteredHeaderCollection);

                    this.toggleShowMoreLink();

                } else {

                    this.$('.js-learning-report-table').addClass('hidden');
                    this.$('.js-empty-data-message').removeClass('hidden');
                    this.toggleShowMoreLink(false);

                }
            }
        },

        toggleShowMoreLink: function (showLink) {
            var hideLink =
                (showLink == false) || (this.usersCollection.length == this.options.totalUsersCount);
            this.ui.showMore.toggleClass('hidden', hideLink);
        },

        renderTotalBar: function (headerCollection) {
            this.reportTotalBarView = new Views.ReportTotalBarView({
                collection: headerCollection,
                reportType: learningReport.activeReportType,
                detailView: false
            });
            this.reportTotalBar.show(this.reportTotalBarView);
        },

        renderUsersColumn: function (usersCollection, reportType) {
            this.reportUserlistView = new Views.ReportUserListView({
                collection: usersCollection,
                reportType: reportType,
                totalUsersCount: this.options.totalUsersCount
            });
            this.reportUsersData.show(this.reportUserlistView);
        },

        renderUsersSummary: function (usersCollection) {
            this.reportUsersSummaryView = new Views.ReportUsersSummaryView({
                collection: usersCollection
            });
            this.reportUsersSummary.show(this.reportUsersSummaryView);
        },

        renderHeader: function (headers) {
            if (this.isPathsReport()) {
                this.reportHeadersView = new Views.ReportPathHeadersView({ collection: headers });
            } else {
                this.reportHeadersView = new Views.ReportLessonsHeadersView({ collection: headers });
            }
            this.reportDataHeaders.show(this.reportHeadersView);
        },

        renderReportBody: function (usersCollection, columnsCollection) {
            var that = this;

            if (this.isPathsReport()) {
                this.mainGridCollectionView = new Views.ReportPathsCollectionView({
                    collection: usersCollection,
                    certificatesCollection: columnsCollection
                });
            } else {
                this.mainGridCollectionView = new Views.ReportCollectionView({
                    collection: usersCollection,
                    headerCollection: columnsCollection
                });
            }

            that.reportPrimaryDatatable.show(this.mainGridCollectionView);
        },

        toggleLayoutView: function (options) {
            if (options.mode == 'detail') {
                options.gridTitle = this.headerCollection.findWhere({'id': options.primaryId})
                    .get('title');
            }
            this.triggerMethod('layout:change', options);
        },

        childEvents: {
            'users:toggle_information': function (childView, isVisible) {
                this.$el.find('.learning-report-table')
                    .toggleClass('show-users-information', isVisible);
            },

            'user:toggle_highlight': function (childView, userId, isActive) {
                var userRows = '[data-user-id="' + userId + '"]';
                this.$el.find(userRows).toggleClass('hover', isActive);
            },

            'pages:show:inline': function (childView, lessonId) {

                var that = this;
                var headerLessonView = childView;
                var lessonModel = childView.model;

                var fetchDiff =
                    this.usersCollection.length - lessonModel.get('detailsFetchedUsers').length;

                var isDataRendered = headerLessonView.$el.hasClass('js-details-rendered');

                if (isDataRendered && fetchDiff == 0) {

                    headerLessonView.$el.width(lessonModel.get('columnWidth'));

                    this.mainGridCollectionView.children.each(function(userRow){
                        userRow.children.each(function (lessonView) {
                            if (lessonView.model.get('lessonId') == lessonId) {
                                var lessonCellEl = lessonView.$el;

                                lessonCellEl.width(lessonModel.get('columnWidth'));

                                if (lessonCellEl.hasClass('js-pages-cached')) {
                                    lessonCellEl.find('.js-lesson-state').hide();
                                    lessonCellEl.find('.js-pages-list').show();
                                }
                            }
                        });
                    });

                    childView.model.set('isDetailsShown', true);

                } else {

                    if (fetchDiff == 0) {

                        this.renderLessonPagesBrief(
                            headerLessonView,
                            this.getUsersCollectionWithLessonPages(
                                lessonId,
                                false
                            ),
                            lessonModel
                        );

                        childView.model.set('isDetailsShown', true);

                    } else {

                        if (!lessonModel.get('isLoading')) {

                            var headerItemFetchedUsers
                                = lessonModel.get('detailsFetchedUsers');

                            lessonModel.set('isLoading', true);

                            var usersToFetch = _.difference(
                                this.usersCollection.map('id'),
                                headerItemFetchedUsers
                            );

                            this.lessonSlidesFetch[lessonId] = $.Deferred();

                            $.when(this.lessonSlidesFetch[lessonId]).then(
                                function() {

                                    lessonModel.set('isLoading', false);

                                    if (lessonModel.isDetailsFetched) {

                                        var usersCollectionWithPages =
                                            that.getUsersCollectionWithLessonPages(
                                                lessonId,
                                                false
                                            );

                                        lessonModel.set('detailsFetchedUsers',
                                            _.union(
                                                headerItemFetchedUsers,
                                                _.pluck(usersCollectionWithPages, 'userId')
                                            )
                                        );

                                        that.renderLessonPagesBrief(
                                            headerLessonView,
                                            usersCollectionWithPages,
                                            lessonModel);

                                        childView.model.set({isDetailsShown: true, isLoading: false});

                                    } else {
                                        childView.model.set({isDetailsShown: false, isLoading: false});
                                    }
                                },
                                function() {
                                    childView.model.set({isDetailsShown: false, isLoading: false});
                                }
                            );

                            var lessonPages = new learningReport.Entities.ReportUsersWithPagesCollection();
                            this.fetchDetails(lessonId, lessonPages, usersToFetch);
                        }
                    }
                }
            },

            'pages:hide:inline': function (childView, lessonId) {

                this.mainGridCollectionView.children.each(function(userRow){
                    userRow.children.each(function (lessonView) {
                        if (lessonView.model.get('lessonId') == lessonId) {
                            var lessonCellEl = lessonView.$el;

                            childView.$el.css({'width': ''});
                            lessonCellEl.css({'width': ''});

                            lessonCellEl.find('.js-pages-list').hide();
                            lessonCellEl.find('.js-lesson-state').show();
                        }
                    });
                });

                childView.model.set('isDetailsShown', false);
            },

            'pages:show:layout': function (childView, lessonId) {

                var that = this,
                    reportType = learningReport.Entities.REPORT_TYPES.LESSONS,
                    lessonModel = this.headerCollection.findWhere({id: lessonId}),
                    fetchDiff =
                        this.usersCollection.length - lessonModel.get('detailsFetchedUsers').length;

                if (fetchDiff == 0) {

                    var usersCollectionWithPages = this.getUsersCollectionWithLessonPages(
                        lessonId,
                        true
                    );
                    this.renderDetailsLayout(usersCollectionWithPages, lessonId, reportType);

                } else {

                    var headerItemFetchedUsers
                        = lessonModel.get('detailsFetchedUsers');

                    var usersToFetch = _.difference(
                        this.usersCollection.map('id'),
                        headerItemFetchedUsers
                    );

                    this.toggleOverlay(true);
                    this.lessonSlidesFetch[lessonId] = $.Deferred();

                    $.when(this.lessonSlidesFetch[lessonId]).then(
                        function() {

                            if (lessonModel.isDetailsFetched) {

                                usersCollectionWithPages =
                                    that.getUsersCollectionWithLessonPages( lessonId, true );

                                lessonModel.set('detailsFetchedUsers',
                                    _.union(
                                        headerItemFetchedUsers,
                                        _.pluck(usersCollectionWithPages, 'userId')
                                    )
                                );

                                that.renderDetailsLayout(
                                    usersCollectionWithPages,
                                    lessonId,
                                    reportType
                                );
                            }

                            that.toggleOverlay(false);

                        },
                        function() {
                            that.toggleOverlay(false);
                        });

                    var lessonPages = new learningReport.Entities.ReportUsersWithPagesCollection();
                    this.fetchDetails(lessonId, lessonPages, usersToFetch);
                }
            },

            'goals:show:inline': function (childView) {

                var that = this,
                    headerItemView = childView,
                    pathModel = childView.model,
                    certificateId = pathModel.id;

                var fetchDiff =
                    this.usersCollection.length - pathModel.get('detailsFetchedUsers').length;

                var isDataRendered = headerItemView.$el.hasClass('js-details-rendered');

                if (isDataRendered && fetchDiff == 0) {

                    headerItemView.$el.width(pathModel.get('columnWidth'));

                    this.mainGridCollectionView.children.each(function(userRow){
                        userRow.children.each(function (cellView) {
                            if (cellView.model.get('certificateId') == certificateId) {
                                var cellEl = cellView.$el;

                                cellEl.width(pathModel.get('columnWidth'));

                                if (cellEl.hasClass('js-goals-cached')) {
                                    cellEl.find('.js-cert-state').hide();
                                    cellEl.find('.js-goals-list').show();
                                }
                            }
                        });
                    });

                    childView.model.set('isDetailsShown', true);

                } else {

                    if (fetchDiff == 0) {

                        this.renderPathGoalsBrief(
                            headerItemView,
                            this.getUsersCollectionWithPathGoals(certificateId),
                            pathModel
                        );

                        childView.model.set('isDetailsShown', true);

                    } else {

                        if (!pathModel.get('isLoading')) {

                            pathModel.set('isLoading', true);

                            var headerItemFetchedUsers
                                = pathModel.get('detailsFetchedUsers');

                            var usersToFetch = _.difference(
                                this.usersCollection.map('id'),
                                headerItemFetchedUsers
                            );

                            this.pathGoalsFetch[certificateId] = $.Deferred();

                            $.when(this.pathGoalsFetch[certificateId]).then(
                                function() {

                                    pathModel.set('isLoading', false);

                                    var usersCollectionWithGoals =
                                        that.getUsersCollectionWithPathGoals(certificateId);

                                    pathModel.set('detailsFetchedUsers',
                                        _.union(
                                            headerItemFetchedUsers,
                                            _.pluck(usersCollectionWithGoals, 'userId')
                                        )
                                    );

                                    if (pathModel.isDetailsFetched) {

                                        that.renderPathGoalsBrief(
                                            headerItemView,
                                            usersCollectionWithGoals,
                                            pathModel
                                        );

                                        childView.model.set({isDetailsShown: true, isLoading: false});
                                    } else {
                                        childView.model.set({isDetailsShown: false, isLoading: false});
                                    }

                                },
                                function() {
                                    childView.model.set({isDetailsShown: false, isLoading: false});
                                });

                            var goalPaths = new learningReport.Entities.ReportUsersWithGoalsCollection();
                            this.fetchDetails(certificateId, goalPaths, usersToFetch);
                        }
                    }
                }
            },

            'goals:hide:inline': function (childView) {

                var certificateId = childView.model.id;

                this.mainGridCollectionView.children.each(function(userRow){
                    userRow.children.each(function (cellView) {
                        if (cellView.model.get('certificateId') == certificateId) {
                            var cellEl = cellView.$el;

                            childView.$el.css({'width': ''});
                            cellEl.css({'width': ''});

                            cellEl.find('.js-goals-list').hide();
                            cellEl.find('.js-cert-state').show();
                        }
                    });
                });

                childView.model.set('isDetailsShown', false);
            },

            'goals:show:layout': function (childView, certificateId) {

                var that = this;
                var reportType = learningReport.Entities.REPORT_TYPES.PATHS;

                var pathModel = this.headerCollection.findWhere({id: certificateId}),
                    fetchDiff =
                        this.usersCollection.length - pathModel.get('detailsFetchedUsers').length;

                if (fetchDiff == 0) {

                    var usersCollectionWithGoals = this.getUsersCollectionWithPathGoals(certificateId);
                    this.renderDetailsLayout(usersCollectionWithGoals, certificateId, reportType);

                } else {

                    var headerItemFetchedUsers = pathModel.get('detailsFetchedUsers');

                    var usersToFetch = _.difference(
                        this.usersCollection.map('id'),
                        headerItemFetchedUsers
                    );

                    this.toggleOverlay(true);
                    this.pathGoalsFetch[certificateId] = $.Deferred();

                    $.when(this.pathGoalsFetch[certificateId]).then(
                        function() {
                            usersCollectionWithGoals =
                                that.getUsersCollectionWithPathGoals(certificateId);

                            pathModel.set('detailsFetchedUsers',
                                _.union(
                                    headerItemFetchedUsers,
                                    _.pluck(usersCollectionWithGoals, 'userId')
                                )
                            );

                            that.toggleOverlay(false);

                            if (usersCollectionWithGoals[0].userGoals.length) {
                                that.renderDetailsLayout(
                                    usersCollectionWithGoals,
                                    certificateId,
                                    reportType
                                );
                            }
                        },
                        function() {
                            that.toggleOverlay(false);
                        });

                    var goalPaths = new learningReport.Entities.ReportUsersWithGoalsCollection();
                    this.fetchDetails(certificateId, goalPaths, usersToFetch);
                }
            },

            'hide:show_more': function () {
                this.toggleShowMoreLink(false);
            }
        },

        getUsersCollectionWithLessonPages: function (lessonId, summarize) {

            return this.usersCollection.map( function (userModel) {

                var userLesson = _.find(userModel.get('lessons'), function (lesson) {
                    return lesson.lessonId == lessonId;
                });

                var userLessonSlides = userLesson.pages || [];
                var userSummary = {};

                if (!!(userLessonSlides.length) && !!(userLesson.status) && summarize) {
                    _.each(learningReport.Entities.LESSON_LEGEND_SLIDES, function (status) {
                        if (status.id > 0) {
                            userSummary['summary' + status.labelKey] = _.filter(
                                userLessonSlides,
                                function (lesson) {
                                    return lesson.status == status.id;
                                }
                            ).length;
                        }
                    });
                }

                return {
                    'userId': userModel.id,
                    'userName': userModel.get('user').name,
                    'lessonState': userLesson.status,
                    'lessonVersion': userLesson.version ? userLesson.version : '',
                    'userPages': userLessonSlides,
                    'userSummary': userSummary
                };

            });
        },

        getUsersCollectionWithPathGoals: function (certificateId, summarize) {

            var pathGoals = this.headerCollection.findWhere({ id: certificateId }).get('goals');

            return _.map(this.usersCollection.models, function (userModel) {

                var userPath = _.find(userModel.get('certificates'), function (certificate) {
                    return certificate.certificateId == certificateId;
                });

                var fetchedPathGoals = userPath.goals || [];
                var userSummary = {};
                var userPathGoals = [];

                _.each(pathGoals, function (goal) {
                    var userGoalValues = _.find(fetchedPathGoals, { 'goalId': goal.id }),
                        userGoal = {};

                    if (userGoalValues) {
                        userGoal.goalId = goal.id;
                        userGoal.title = goal.title;
                        userGoal.status = userGoalValues.status;
                        userGoal.state = userGoalValues.state;
                        userGoal.stateDescr = userGoalValues.stateDescr;
                        userGoal.date = userGoalValues.date;
                    } else {
                        userGoal.status = 0;
                    }

                    userPathGoals.push(userGoal);
                });

                if (!!(fetchedPathGoals.length) && !!(userPath.status) && summarize) {
                    _.each(learningReport.Entities.PATHS_LEGEND_GOALS, function (status) {
                        if (status.id > 0) {
                            userSummary['summary' + status.labelKey] = _.filter(
                                fetchedPathGoals,
                                function (certificate) {
                                    return certificate.status == status.id;
                                }
                            ).length;
                        }
                    });
                }

                return {
                    'userId': userModel.id,
                    'userName': userModel.get('user').name,
                    'userGoals': userPathGoals,
                    'userSummary': userSummary
                };
            });
        },

        fetchDetails: function(itemId, detailsCollection, usersToFetch, bufferCollection) {
            var that = this,
                deferredObj = detailsCollection.primaryItems == 'certificates'
                    ? that.pathGoalsFetch[itemId]
                    : that.lessonSlidesFetch[itemId],
                usersToFetch = usersToFetch || this.usersCollection.map('id');

            bufferCollection = bufferCollection || this.usersCollection;

            detailsCollection.fetch({
                itemId: itemId,
                userIds: usersToFetch,
                reportType: learningReport.activeReportType,
                success: function (userCollectionWithDetails, response, options) {

                    if (userCollectionWithDetails.length > 0) {

                        var countDetails = that.isPathsReport()
                            ? 0
                            : userCollectionWithDetails.models[0].get(detailsCollection.detailItems).length;

                        if (!(that.isPathsReport()) && countDetails == 0) {

                            that.headerCollection.findWhere({'id': itemId}).isDetailsFetched = false;

                            valamisApp.execute('notify', 'warning',
                                Valamis.language[detailsCollection.detailItems + 'ListEmptyLabel']);

                        } else {

                            that.headerCollection.findWhere({'id': itemId}).isDetailsFetched = true;

                            userCollectionWithDetails.each( function (user) {

                                var matchedUserModel =
                                    _.find(bufferCollection.models, function (userModel) {
                                        return userModel.id == user.get('userId');
                                    });

                                if (matchedUserModel) {

                                    var userDetailsPrimaryItem =
                                        _.find(matchedUserModel.get(detailsCollection.primaryItems),
                                            function(item) {
                                                return item[detailsCollection.primaryId] == itemId;
                                            }
                                        );

                                    userDetailsPrimaryItem[detailsCollection.detailItems] =
                                        user.get(
                                            detailsCollection.detailItems
                                        );

                                    if (user.get('version')) {
                                        userDetailsPrimaryItem.version =
                                            matchedUserModel.get('version');
                                    }
                                }
                            });
                        }
                    } else {
                        if (that.isPathsReport()) {
                            that.headerCollection.findWhere({'id': itemId}).isDetailsFetched = true;
                        }
                    }
                },
                error: function (model, response) {
                    console.log(response);
                    valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                }
            }).then(function() {
                deferredObj.resolve();
              },function() {
                deferredObj.reject();
            });

        },

        renderDetailsLayout: function (usersCollectionWithDetails, primaryId, reportType) {

            var that = this;

            this.reportDataHeaders.$el.addClass('hidden');
            this.reportPrimaryDatatable.$el.addClass('hidden');
            this.reportUsersSummary.$el.addClass('hidden');
            this.reportTotalBar.$el.addClass('hidden');

            var isPathsReportType = reportType == learningReport.Entities.REPORT_TYPES.PATHS;
            var headerData = isPathsReportType
                ? this.headerCollection.findWhere({ id: primaryId }).get('goals')
                : usersCollectionWithDetails[0].userPages;

            var reportDetailsHeaderCollection =
                new learningReport.Entities.ReportDetailDataCollection(headerData);
            var reportDetailHeaderView =
                new Views.ReportDetailHeadersView({ collection: reportDetailsHeaderCollection });

            var detailGridCollection =
                new learningReport.Entities.ReportUserDetailDataCollection(usersCollectionWithDetails);

            var usersDetailGrid = isPathsReportType
                ? new Views.ReportUserGoalsCollectionView({ collection: detailGridCollection })
                : new Views.ReportUserSlidesCollectionView({ collection: detailGridCollection });

            var usersSummaryView =
                new Views.ReportUsersSummaryView({
                    collection: detailGridCollection,
                    detailSummaryView: true
                });

            this.reportDetailHeaders.show(reportDetailHeaderView);
            this.reportDetailDatatable.show(usersDetailGrid);

            if (learningReport.reportFilter[reportType].isSummaryVisible) {
                this.reportUsersDetailSummary.show(usersSummaryView);
                this.reportUsersDetailSummary.$el.removeClass('hidden');
                this.renderTotalBarDetailView(reportDetailsHeaderCollection, primaryId, reportType);
            }

            this.toggleLayoutView({
                mode: 'detail',
                primaryId: primaryId
            });
        },

        renderTotalBarDetailView: function(reportDetailsHeaderCollection, primaryId, reportType) {
            var that = this,
                reportDetailsTotalCollection = new learningReport.Entities.ReportTotalCollection();

            that.reportDetailTotalBarView = new Views.ReportTotalBarView({
                collection: reportDetailsHeaderCollection,
                reportType: reportType,
                detailView: true
            });

            that.reportDetailTotalBar.show(that.reportDetailTotalBarView);

            reportDetailsTotalCollection.fetch({
                courseId: learningReport.reportFilter[reportType].courseId,
                reportType: reportType,
                detailId: primaryId,
                success: function(totalCollection, response, options) {

                    var legend = reportType == learningReport.Entities.REPORT_TYPES.LESSONS
                        ? learningReport.Entities.LESSON_LEGEND_SLIDES
                        : learningReport.Entities.PATHS_LEGEND_GOALS;

                    reportDetailsHeaderCollection.each( function (headerItemModel) {

                        var totalResponseModel = totalCollection.findWhere({ id: headerItemModel.id }),
                            totalArr = [];

                        _.each(legend, function(legendItem) {
                            totalArr[legendItem.id] = totalResponseModel.get('total')[legendItem.id] || 0;
                        });

                        headerItemModel.set('total', totalArr);
                    });

                    that.reportDetailTotalBarView = new Views.ReportTotalBarView({
                        collection: reportDetailsHeaderCollection,
                        reportType: reportType,
                        detailView: true
                    });

                    that.reportDetailTotalBar.show(that.reportDetailTotalBarView);
                },
                error: function (model, response) {
                    console.log(response);
                }
            });
        },

        renderLessonPagesBrief: function (headerLessonView, usersCollectionWithPages, lessonModel) {

            var countLessonPages = usersCollectionWithPages[0].userPages.length;
            var lessonCellWidth = Math.max(CELL_MIN_WIDTH, PAGE_BOX_WIDTH * countLessonPages);

            lessonModel.set('columnWidth',lessonCellWidth);

            headerLessonView.$el.width(lessonCellWidth).addClass('js-details-rendered');

            this.reportPrimaryDatatable.currentView.children.each(function(userRow){

                var currentUserId = userRow.model.get('id');

                var currentUserData = _.find(usersCollectionWithPages, function (user) {
                    return user.userId == currentUserId;
                });

                userRow.children.each(function (lessonView) {

                    if (lessonView.model.get('lessonId') == lessonModel.id) {

                        var lessonCellEl = lessonView.$el;
                        lessonCellEl.width(lessonCellWidth);

                        if (currentUserData.userId > 0) {

                            var pagesForCurrentUser = currentUserData.userPages;

                            _.each(pagesForCurrentUser, function (page) {
                                _.extend(page, {
                                    username: userRow.model.get('user').name,
                                    lesson: lessonModel.get('title'),
                                    lessonVersion: currentUserData.lessonVersion || false,
                                    slideOpenDate: page.date
                                        ? new Date(page.date).toLocaleDateString()
                                        : false
                                });
                            });

                            var template = Mustache.to_html(
                                $('#learningReportUserLessonDetailTemplate').html(),
                                {
                                    pagesList: pagesForCurrentUser,
                                    ofLabel: Valamis.language['ofLabel']
                                }
                            );

                            lessonCellEl.addClass('js-pages-cached');
                            lessonCellEl.find('.js-lesson-state').hide();
                            lessonCellEl.find('.js-pages-list').html(template).show();

                            $('li',lessonCellEl).tooltipster({
                                functionInit: function(instance, helper){
                                    var content =
                                        $(helper.origin).find('.element-tooltip-content').detach();
                                    instance.content(content);
                                }
                            });
                        }
                    }
                });
            });
        },

        renderPathGoalsBrief: function (headerItemView, usersCollectionWithGoals) {

            var certificateModel = headerItemView.model;

            var columnWidth =
                Math.max(CELL_MIN_WIDTH, PAGE_BOX_WIDTH * certificateModel.get('goals').length);
            certificateModel.set('columnWidth', columnWidth);
            headerItemView.$el.width(columnWidth).addClass('js-details-rendered');

            this.reportPrimaryDatatable.currentView.children.each(function(userRow){

                var currentUserId = userRow.model.get('id');

                var currentUserData = _.find(usersCollectionWithGoals, function (user) {
                    return user.userId == currentUserId;
                });

                userRow.children.each(function (cellView) {

                    if (cellView.model.get('certificateId') == certificateModel.id) {

                        var cellEl = cellView.$el;
                        cellEl.width(columnWidth);

                        if (currentUserData.userId > 0) {

                            var goalsForCurrentUser = currentUserData.userGoals;

                            if (goalsForCurrentUser[0].status) {

                                _.each(goalsForCurrentUser, function (goal) {
                                    _.extend(goal, {
                                        userName: userRow.model.get('user').name,
                                        pathTitle: certificateModel.get('title'),
                                        lastDate: goal.date
                                            ? new Date(goal.date).toLocaleDateString()
                                            : false
                                    });
                                });

                                var template = Mustache.to_html(
                                    $('#learningReportUserPathDetailTemplate').html(),
                                    {
                                        goalsList: goalsForCurrentUser,
                                        ofLabel: Valamis.language['ofLabel']
                                    }
                                );

                                cellEl.addClass('js-goals-cached');
                                cellEl.find('.js-cert-state').hide();
                                cellEl.find('.js-goals-list').html(template).show();

                                $('li',cellEl).tooltipster({
                                    functionInit: function(instance, helper){
                                        var content =
                                            $(helper.origin).find('.element-tooltip-content').detach();
                                        instance.content(content);
                                    }
                                });
                            }
                        }
                    }
                });
            });
        }
    });

    Views.ReportDetailHeaderThView = Marionette.ItemView.extend({
        template: '#learningReportDetailThView',
        className: 'learning-report-th'
    });

    Views.ReportDetailHeadersView = Marionette.CompositeView.extend({
        template: '#learningReportTheadView',
        className: 'learning-report-tr learning-report-headers',
        childView: Views.ReportDetailHeaderThView
    });

// Userlist Views

    Views.ReportUserListItemView = Marionette.ItemView.extend({
        template: '#learningReportUserListItemViewTemplate',
        className: 'learning-report-tr',
        events: {
            'mouseover': 'mouseOverRow',
            'mouseout': 'mouseOutRow'
        },
        templateHelpers: function() {
            var organizations = this.model.get('organizations');
            var grade = (!!this.model.get('grade')) ? Math.round(this.model.get('grade') * 100) : 0;
            return {
                organization: organizations.join(', '),
                showGrade: this.options.showGrade,
                gradePercent: grade
            };
        },
        onRender: function () {
            this.$el.attr('data-user-id', this.model.get('id'));
            this.$('.js-tooltipster').tooltipster({ maxWidth: ORGANIZATION_TOOLTIP_WIDTH });
        },
        mouseOverRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('id'), true);
        },
        mouseOutRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('id'), false);
        }
    });

    Views.ReportUserListView = Marionette.CompositeView.extend({
        template: '#learningReportUserListViewTemplate',
        className: 'userlist-data',
        initialize: function () {
            if (this.options.reportType == learningReport.Entities.REPORT_TYPES.LESSONS) {
                this.$el.addClass('show-grades');
                this.showGrade = true;
            }
        },
        childView: Views.ReportUserListItemView,
        childViewContainer: '.js-left-column-user-list',
        childViewOptions:  function () {
            return {
                showGrade: !!this.showGrade
            }
        },
        ui: {
            listedUsersInfo: '.js-listed-users-info',
            showUserInformation: '.js-show-user-information',
            hideUserInformation: '.js-hide-user-information'
        },
        events: {
            'click @ui.showUserInformation': 'showUserInformation',
            'click @ui.hideUserInformation': 'hideUserInformation'
        },
        templateHelpers: function() {
            return {
                showGrade: !!this.showGrade
            }
        },
        collectionEvents: {
            'add': function () {
                this.updateShownUsersInfo(this.collection.length);
            }
        },
        onShow:  function () {
            this.updateShownUsersInfo(this.collection.length);
        },
        updateShownUsersInfo: function (fetchedUsersCount) {
            var output = Valamis.language['showingLabel'] + ' ' + fetchedUsersCount
                + ' ' + Valamis.language['ofLabel'] + ' ' + this.options.totalUsersCount
                + ' ' + Valamis.language['usersLabel'];
            this.ui.listedUsersInfo.html(output);

            if (fetchedUsersCount == this.options.totalUsersCount) {
                this.triggerMethod('hide:show_more');
            }
        },
        showUserInformation: function () {
            this.triggerMethod('users:toggle_information', true);
        },
        hideUserInformation: function () {
            this.triggerMethod('users:toggle_information', false);
        }
    });

// Datatable Summary Views

    Views.ReportUsersSummaryItemView = Marionette.ItemView.extend({
        getTemplate: function () {
            switch (learningReport.activeReportType) {
                case learningReport.Entities.REPORT_TYPES.PATHS:
                    return '#learningReportPathsSummaryItemViewTemplate';
                default:
                    return '#learningReportUserSummaryItemViewTemplate';
            }
        },
        className: 'summary-user-row',
        initialize: function () {
            this.userId = this.model.get(this.options.detailSummaryView ? 'userId' : 'id');
        },
        templateHelpers: function() {
            return {
                detailSummaryView: this.options.detailSummaryView,
                summary: _.mapValues(this.model.get('userSummary'), function(value) {
                    return value ? String(value) : '-';
                })
            }
        },
        events: {
            'mouseover': 'mouseOverRow',
            'mouseout': 'mouseOutRow'
        },
        onRender: function () {
            this.$el.attr('data-user-id', this.userId);
        },
        mouseOverRow: function () {
            this.triggerMethod('user:toggle_highlight', this.userId, true);
        },
        mouseOutRow: function () {
            this.triggerMethod('user:toggle_highlight', this.userId, false);
        }

    });

    Views.ReportUsersSummaryView = Marionette.CompositeView.extend({
        getTemplate: function () {
            switch (learningReport.activeReportType) {
                case learningReport.Entities.REPORT_TYPES.PATHS:
                    return '#learningReportPathsSummaryViewTemplate';
                default:
                    return '#learningReportUserSummaryViewTemplate';
            }
        },
        className:  function () {
            return 'userlist-summary' +
                (this.options.detailSummaryView ? ' userlist-slides-summary' : '');
        },
        childView: Views.ReportUsersSummaryItemView,
        childViewOptions: function () {
            return {
                detailSummaryView: Boolean(this.options.detailSummaryView)
            }
        },
        childViewContainer: '.js-users-summary',
        templateHelpers: function () {
            return {
                detailSummaryView: Boolean(this.options.detailSummaryView)
            }
        }
    });

// Total Bar Views

    Views.ReportTotalBarItemView = Marionette.ItemView.extend({
        getTemplate: function () {
            return this.model.get('total')
                ? '#learningReportTotalBarItemView'
                : '#learningReportTotalBarLoadingView';
        },
        tagName: 'li',
        className: 'js-total-bar-item',
        ui: {
            totalList: '.js-statictics',
            tooltipTotalList: '.js-tooltip-statictics'
        },
        events: {
            'mouseover @ui.totalList': 'showTooltip'
        },
        modelEvents: {
            'change:total': function() {
                this.render();
            },
            'change:isDetailsShown': function () {
                if (this.model.get('isDetailsShown')) {
                    this.$el.width(this.model.get('columnWidth'));
                } else {
                    this.$el.width(CELL_MIN_WIDTH);
                }
            }
        },
        onRender: function () {
            if (this.model.get('total')) {
                var that = this,
                    total = [],
                    activeLegend = [],
                    isPathsReport =
                        (this.options.reportType == learningReport.Entities.REPORT_TYPES.PATHS);

                var namePrefix = isPathsReport ? 'certificate' : 'lesson';

                if (this.options.detailView) {
                    activeLegend = isPathsReport
                        ? learningReport.Entities.PATHS_LEGEND_GOALS
                        : learningReport.Entities.LESSON_LEGEND_SLIDES;
                } else {
                    activeLegend = isPathsReport
                        ? learningReport.Entities.PATHS_LEGEND
                        : learningReport.Entities.LESSON_LEGEND;
                }

                total = _.union(total, activeLegend);

                _.remove(total, function(state) { return state.id == 0; });

                _.each(total, function (state) {
                    var value = that.model.get('total')[state.id];

                    _.extend(state, {
                        'value': value ? value : 0,
                        'label': Valamis.language[namePrefix + 'State' + state.labelKey + 'Label']
                    });
                });

                var valuesHtml = Mustache.to_html(
                    $('#learningReportTotalBarValueTemplate').html(), {
                        valuesList: total,
                        classPrefix: namePrefix
                    }
                );

                var valuesTooltipHtml = Mustache.to_html(
                    $('#learningReportTotalBarTooltipValueTemplate').html(), {
                        valuesList: total,
                        classPrefix: namePrefix
                    }
                );

                this.ui.totalList.html(valuesHtml);
                this.ui.tooltipTotalList.html(valuesTooltipHtml);
            }
        },
        showTooltip: function(e) {
            var lessonIconEl = $(e.target).closest('.js-total-bar-item');

            if (!lessonIconEl.hasClass('tooltipstered')) {
                lessonIconEl.tooltipster({
                    minWidth: 170,
                    functionInit: function(instance, helper){
                        var content = $(helper.origin).children('.element-tooltip-content').detach();
                        instance.content(content);
                    }
                });
            }
            lessonIconEl.tooltipster('open');
        }
    });

    Views.ReportTotalBarView = Marionette.CompositeView.extend({
        template: '#learningReportTotalBarView',
        childView: Views.ReportTotalBarItemView,
        childViewContainer: '.js-bar-content',
        childViewOptions: function () {
            return this.options;
        }
    });
});