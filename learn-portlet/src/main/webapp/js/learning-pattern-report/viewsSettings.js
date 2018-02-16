learningReport.module('Views', function (Views, learningReport, Backbone, Marionette, $, _) {

    var SEARCH_TIMEOUT = 800;

    Views.CourseSelectorView = Marionette.ItemView.extend({
        template: '#learningReportCoursesTemplate',
        events: {
            'click li': 'selectCourse'
        },
        templateHelpers: function() {
            return {
                courses: this.options.courses
            }
        },
        behaviors: {
            ValamisUIControls: {}
        },
        onValamisControlsInit: function() {
            this.$('.js-courses-list').valamisDropDown('select', learningReport.courseModel.get('id'));
        },
        selectCourse: function(e) {
            var courseId = $(e.target).data('value');

            this.triggerMethod('filter:text:clear');

            learningReport.execute('report:course:changed', courseId);

            var filterSettings = learningReport.reportFilter[learningReport.activeReportType];

            var activeTabView = (filterSettings.reportType == learningReport.Entities.REPORT_TYPES.LESSONS)
                ? learningReport.layoutView.learningReportLessonsTabView
                : learningReport.layoutView.learningReportPathsTabView;

            activeTabView.filterView.reportSettingsCollection.updateSettings( {}, filterSettings).then(
                function (response, options) {
                    activeTabView.usersCountModel.fetch({
                        currentCourseId: courseId,
                        reportType: filterSettings.reportType,
                        success: function(model, response, options) {
                            activeTabView.render();
                        }
                    });
                },
                function (response) {
                    valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                    console.log(response);
                }
            );
        }
    });

    Views.CourseTitleView = Marionette.ItemView.extend({
        template: '#learningReportCourseTitleTemplate',
        templateHelpers: function() {
            return {
                title: this.options.title
            }
        }
    });

    Views.FilterView = Marionette.LayoutView.extend({
        template: '#learningReportSettingsFieldset',
        regions: {
            'reportSettingsRegion': '#filterDropdown',
            'activeFiltersRegion': '#activeFilters'
        },
        ui: {
            toggleButton: '.js-filter-toggle-button'
        },
        events: {
            'click @ui.toggleButton': 'toggleDropdown'
        },
        childEvents: {
            'summary:toggle': function (childView) {
                this.toggleDropdown();
            },
            'filter:update': function (childView, childModel) {
                this.triggerMethod('filter:update', childModel);
            },
            'filter:user:update': function (childView, childModel) {
                this.triggerMethod('filter:user:update', childModel);
            }
        },
        toggleDropdown: function () {
            if (!this.$('.js-dropdown-filter').hasClass('disabled')) {
                this.$('.js-dropdown-filter').toggleClass('show-dropdown-filter');
            }
        },
        onRender: function () {
            this.reportSettingsCollection = new learningReport.Entities.ReportSettingsCollection();
            this.reportSettingsCollection.reportType = this.options.reportType;
            this.reportSettingsCollection.on('sync', this.renderSettings, this);
            this.reportSettingsCollection.fetch();
        },
        renderSettings: function (data) {

            var loadedOptions = this.options,
                filterSettings = this.reportSettingsCollection;

            if (data.length > 0) {

                if (filterSettings.reportType == learningReport.Entities.REPORT_TYPES.LESSONS) {

                    if (loadedOptions.onlyLessonsWithQuestions) {
                        filterSettings.get('lessonType').set('currentValue', 1);
                    }

                } else {

                    if (loadedOptions.pathGoalType) {
                        filterSettings.get('pathGoalType').set('currentValue', loadedOptions.pathGoalType);
                    }

                }

                if (loadedOptions.userFilter) {
                    filterSettings.get('userSearch').set('currentValue', loadedOptions.userFilter);
                }

                if (loadedOptions.isSummaryVisible != '') {
                    filterSettings.get('isSummaryVisible').set('currentValue', true);
                }

                this.reportSettingsView = new Views.ReportSettingsView({
                    collection: filterSettings
                });

                this.reportSettingsRegion.show(this.reportSettingsView);

                this.activeFiltersView = new Views.ActiveFiltersView({
                    collection: filterSettings
                });

                this.activeFiltersRegion.show(this.activeFiltersView);
            }
        }
    });

    Views.SelectorView = Marionette.ItemView.extend({
        tagName: 'li',
        template: '#reportListOption',
        templateHelpers: function () {
            return {
                optionTitle: Valamis.language[this.model.get('id') + 'Label']
            }
        },
        events: {
            'click span': 'updateMenu'
        },
        updateMenu: function () {
            this.triggerMethod('item:set_active', this.model);
        },
        onRender: function () {
            this.triggerMethod('item:check_active', this.model);
        }
    });

    Views.ReportSettingFieldsetView = Marionette.CompositeView.extend({
        defaults: {
            type: 'optionList'
        },
        getTemplate: function () {
            switch (this.model.get('type')) {
                case 'toggleSwitch':
                    return '#reportFilterToggleSwitchTemplate';
                case 'textSearch':
                    return '#reportFilterTextSearchTemplate';
                default:
                    return '#reportFilterOptionListTemplate';
            }
        },
        className: 'filter-section',
        childView: Views.SelectorView,
        childViewContainer: 'ul',
        childEvents: {
            'item:check_active': function (childView, childModel) {
                childModel.set('isActive', childModel.get('value') == this.model.get('currentValue'));
                childView.$el.toggleClass('active', childModel.get('isActive'));
            },
            'item:set_active': function (childView, childModel) {
                var childSelectorValue = childModel.get('value');

                if (this.model.get('currentValue') != childSelectorValue) {
                    this.model.set('currentValue', childSelectorValue);
                }
            }
        },
        initialize: function () {
            this.$el.prop('id', this.model.get('id') + 'Fieldset');

            switch (this.model.get('type')) {
                case 'toggleSwitch':
                    this.$el.addClass('filter-toggle-switch');
                    break;
                case 'textSearch':
                    this.$el.addClass('filter-text-search');
                    break;
                default:
                    var options = this.model.get('options');
                    this.$el.addClass('filter-option-list');
                    this.collection = new learningReport.Entities.ReportSelectorModel(options);
            }

            if (this.model.get('isHidden')) {
                this.$el.addClass('hidden');
            }
        },
        onRender: function () {
            this.$('.js-search').valamisSearch();
        },
        templateHelpers: function () {
            return {
                title: Valamis.language[this.model.get('id') + 'FilterLabel'],
                isModal: (this.model.get('type') == 'modal'),
                currentValue: this.model.get('currentValue')
            }
        },
        events: {
            'change .js-val-switch': 'changeCurrentSwitch',
            'keyup .js-search': 'changeSearchText'
        },
        modelEvents: {
            'change:currentValue': 'changeCurrent',
            'change:triggerRender': 'triggerRender'
        },
        changeCurrentSwitch: function (e) {
            var that = this;
            this.model.set('currentValue', $(e.target).prop('checked'));
            setTimeout(function() {
                that.triggerMethod('summary:toggle');
            }, 200);
        },
        changeCurrent: function (model) {
            if (model.get('type') == 'optionList') {
                this.render();
            }
            this.triggerMethod('settings:changed');
        },
        changeSearchText: function(e){
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.model.set('currentValue', $(e.target).val());
            }, SEARCH_TIMEOUT);
        },
        triggerRender: function(){
            if (this.model.get('triggerRender')) {
                this.render();
                this.model.set('triggerRender', false);
            }
        }

    });

    Views.ReportSettingsView = Marionette.CollectionView.extend({
        className: 'dropdown-filter-container js-dropdown-filter-container',
        childView: Views.ReportSettingFieldsetView,
        saveSettings: function (childView) {
            var filterSettings = learningReport.reportFilter[learningReport.activeReportType],
                settingsCollection = this.collection,
                fieldName = childView.model.get('id');

            switch (fieldName)
                {
                case 'lessonType':
                    filterSettings.onlyLessonsWithQuestions = Boolean(childView.model.get('currentValue'));
                    break;
                default:
                    filterSettings[fieldName] = childView.model.get('currentValue');
                    break;
                }

            settingsCollection.updateSettings( {}, filterSettings).then(
                function (response, options) {
                    valamisApp.execute('notify', 'success', Valamis.language['settingsSavedMessageLabel']);
                },
                function (response) {
                    valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                    console.log(response);
                }
            );
        },
        childEvents: {
            'settings:changed': function (childView) {
                var childModel = childView.model;

                if (childModel.id == 'userSearch') {
                    this.triggerMethod('filter:user:update', childModel);
                } else {
                    if (childModel.id != 'lessonSearch' && childModel.id != 'pathSearch') {
                        this.saveSettings(childView);
                    }
                    this.triggerMethod('filter:update', childModel);
                }
            }
        }
    });

    Views.ActiveFilterItemView = Marionette.ItemView.extend({
        tagName: 'li',
        className: 'hidden',
        template: '#reportActiveFilterItemTemplate',
        templateHelpers: function () {
            var optionType = this.model.get('type'),
                currentValue = this.model.get('currentValue'),
                displayValue = '';

            if (optionType == 'textSearch') {
                displayValue = '\'' + currentValue + '\'';
            } else if (optionType == 'optionList') {
                var optionValue = _.find(this.model.get('options'), function(option) {
                    return option.value == currentValue;
                });
                if (optionValue) {
                    displayValue = Valamis.language[optionValue.id + 'Label'];
                }
            }
            return {
                optionTitle: Valamis.language[this.model.get('id') + 'ActiveLabel'],
                displayValue: displayValue
            }
        },
        ui: {
            removeIcon: '.js-remove'
        },
        events: {
            'mouseover @ui.removeIcon': 'hiliteOption',
            'mouseleave @ui.removeIcon': 'cancelOption',
            'click @ui.removeIcon': 'removeOption'
        },
        modelEvents: {
            'change:currentValue': 'updateActiveFilter'
        },
        onRender: function () {
            this.cancelOption();
            this.$el.toggleClass('hidden', this.model.get('currentValue') === this.model.get('defaultValue'));
        },
        hiliteOption: function () {
            this.$el.addClass('hilited');
        },
        cancelOption: function () {
            this.$el.removeClass('hilited');
        },
        updateActiveFilter: function () {
            this.render();
            this.triggerMethod('filter:active:changed');
        },
        removeOption: function () {
            this.model.set('currentValue', this.model.get('defaultValue'));
            this.model.set('triggerRender', true);
        }
    });

    Views.ActiveFiltersView = Marionette.CompositeView.extend({
        template: '#reportActiveFiltersTemplate',
        className: 'active-filters',
        childView: Views.ActiveFilterItemView,
        childViewContainer: 'ul',
        filter: function (child, index, collection) {
            return child.id != 'isSummaryVisible';
        },
        onShow: function () {
            this.toggleBlockVisibility();
        },
        childEvents: {
            'filter:active:changed': function () {
                this.toggleBlockVisibility();
            }
        },
        toggleBlockVisibility: function () {
            var firstActiveFilter = _.find(this.collection.models, function(model) {
                return model.get('currentValue') != model.get('defaultValue');
            });

            if (Boolean(firstActiveFilter)) {
                this.$el.toggleClass('hidden', firstActiveFilter.id == 'isSummaryVisible');
            } else {
                this.$el.addClass('hidden');
            }
        }
    });

});