allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.DISPLAY_TYPE = {
        LIST: 'list',
        TILES: 'tiles'
    };

    Views.ToolbarView = Marionette.ItemView.extend({
        template: '#allCoursesToolbarTemplate',
        behaviors: {
            ValamisUIControls: {}
        },
        templateHelpers: function() {
            return {
                tilesModeOption: Views.DISPLAY_TYPE.TILES,
                listModeOption: Views.DISPLAY_TYPE.LIST,
                isEditor: this.options.isEditor
            }
        },
        events: {
            'keyup .js-search': 'changeSearchText',
            'click .js-new-course' : 'createNewCourse',
            'paginatorShowing' : 'allCoursesToolbarShowing',
            'click .dropdown-menu > li.js-sort': 'changeSort',
            'click .js-display-option': 'changeDisplayMode'
        },
        initialize:function(){
            this.inputTimeout = {};
        },
        onRender: function(){
            this.$('.js-sort-filter').valamisDropDown('select', this.model.get('sort'));
            this.$('.js-search').val(this.model.get('searchtext'));

            var displayMode = this.options.settings.get('displayMode') || Views.DISPLAY_TYPE.LIST;
            this.$('.js-display-option[data-value="'+ displayMode +'"]').addClass('active');
        },
        changeSort: function(e){
            this.model.set('sort', $(e.target).attr("data-value"));
        },
        changeSearchText: function(e){
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.model.set('searchtext', $(e.target).val());
            }, 800);
        },
        createNewCourse: function(){
            var newCourse = new allCourses.Entities.Course();
            this.triggerMethod('courseList:edit', newCourse, 'CourseDetails');
        },
        changeDisplayMode: function(e) {
            this.$('.js-display-option').removeClass('active');
            var elem = $(e.target).closest('.js-display-option');
            elem.addClass('active');
            this.triggerMethod('toolbar:displaymode:change', elem.attr('data-value'));
        }
    });

    Views.CourseTypes = {
        OPEN: 'OPEN',
        ON_REQUEST: 'ON_REQUEST',
        CLOSED: 'CLOSED'
    };

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#allCoursesAppLayoutTemplate',
        regions: {
            'courseList': '#allCourseCourseList',
            'toolbar': '#allCourseToolbar',
            'paginator': '#allCoursePaginator',
            'paginatorShowing': '#allCoursesToolbarShowing'
        },
        childEvents: {
            'courseList:delete:course': function(childView, model) {
                var that = this;
                model.deleteCourse().then(
                    function() {
                        valamisApp.execute('notify', 'success', Valamis.language['courseSuccessfullyDeletedLabel']);
                        that.fetchCollection(false);
                    },
                    function() {
                        valamisApp.execute('notify', 'error', Valamis.language['courseFailedToDeleteLabel']);
                    }
                );
            },
            'courseList:edit': function(childView, model, tabSelector) {
                this.editCourse(model, tabSelector);
            },
            'courseList:join:course': function(childView, model) {
                var that = this;
                model.joinCourse().then(
                    function() {
                        valamisApp.execute('notify', 'success', Valamis.language['courseSuccessfullyJoinedLabel']);
                        that.fetchCollection(false);
                    },
                    function() {
                        valamisApp.execute('notify', 'error', Valamis.language['courseFailedToJoinLabel']);
                    }
                );
            },
            'courseList:leave:course': function(childView, model) {
                var that = this;
                model.leaveCourse().then(
                    function() {
                        valamisApp.execute('notify', 'success', Valamis.language['courseSuccessfullyLeftLabel']);
                        that.fetchCollection(false);
                    },
                    function() {
                        valamisApp.execute('notify', 'error', Valamis.language['courseFailedToLeaveLabel']);
                    }
                );
            },
            'courseList:requestJoin:course': function(childView, model) {
                var that = this;
                model.requestJoinCourse().then(
                    function() {
                        valamisApp.execute('notify', 'success', Valamis.language['courseSuccessfullyRequestedLabel']);
                        that.fetchCollection(false);
                    },
                    function() {
                        valamisApp.execute('notify', 'error', Valamis.language['courseFailedToRequestLabel']);
                    }
                );
            },
            'toolbar:displaymode:change': function(childView, displayMode) {
                this.settings.set('displayMode', displayMode);
                this.settings.save();
            }
        },
        initialize: function(options){

            var that = this;
            that.paginatorModel = new PageModel();

            this.settings = new SettingsHelper({url: window.location.href, portlet: options.portletName});
            this.settings.fetch();

            that.filter = new allCourses.Entities.Filter(this.settings.get('searchParams'));
            that.filter.on('change', function(model){
                that.fetchCollection(true);
                that.settings.set('searchParams', model.toJSON());
                that.settings.save();
            });

            that.courses = options.courses;
            that.courses.on('fetchCollection', function() {
                that.fetchCollection(true);
            });

            that.courses.on('courseCollection:updated', function (details) {
                that.updatePagination(details);
            });
        },
        onRender: function () {
            var courseListView = new Views.CourseListView({
                collection: this.courses,
                paginatorModel: this.paginatorModel,
                settings: this.settings,
                isEditor: this.options.isEditor
            });
            courseListView.on('render:collection', function(view) {
                valamisApp.execute('update:tile:sizes', view.$el);
            });
            var toolbarView = new Views.ToolbarView({
                model: this.filter,
                settings: this.settings,
                isEditor: this.options.isEditor
            });
            this.toolbar.show(toolbarView);

            this.paginatorView = new ValamisPaginator({
                language: Valamis.language,
                model : this.paginatorModel
            });
            this.paginator.show(this.paginatorView);

            var paginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: this.paginatorModel
            });
            this.paginatorShowing.show(paginatorShowingView);

            this.paginatorView.on('pageChanged', function () {
                this.fetchCollection(false);
            }, this);

            this.courseList.show(courseListView);
        },

        updatePagination: function (details, context) {
            this.paginatorView.updateItems(details.total);
        },
        fetchCollection: function(filterChanged) {
            if(filterChanged) {
                this.paginatorModel.set('currentPage', 1);
            }

            this.courses.fetchMore({
                reset: true,
                filter: this.filter.toJSON(),
                currentPage: this.paginatorModel.get('currentPage'),
                itemsOnPage: this.paginatorModel.get('itemsOnPage')
            });
        },
        editCourse: function(course, activeTab) {
            var editCourseView = new Views.MainEditView({
                model: course,
                activeTab: activeTab
            });

            var that = this;
            var modalView = new valamisApp.Views.ModalView({
                template: '#allCoursesEditModalTemplate',
                contentView: editCourseView,
                beforeSubmit: function() {
                    return editCourseView.updateModel();
                },
                submit: function(){
                    course.save().then(
                      function(){
                          editCourseView.saveImage(function(){
                              that.fetchCollection(true);
                          });
                      },
                      function(xr, er){
                          that.displayCourseError(xr, er);
                          that.editCourse(course, activeTab);
                      });
                }
            });

            valamisApp.execute('modal:show', modalView);
        },
        displayCourseError: function(xr, er) {
            var details = er.responseJSON;
            var messageLabel = 'createCourseError';
            if (details) {
                if (details.field == "friendlyUrl") {
                    if (details.reason == "duplicate") {
                        messageLabel = 'friendlyUrlIsDuplicatedError';
                    } else if (details.reason == "invalid-size") {
                        messageLabel = 'friendlyUrlHasInvalidLengthError';
                    } else {
                        messageLabel = 'friendlyUrlIsWrongError';
                    }
                } else if (details.field == "name") {
                    if (details.reason == "duplicate") {
                        messageLabel = 'courseNameIsDuplicatedError';
                    } else {
                        messageLabel = 'courseNameIsWrongError';
                    }
                }

            }
            valamisApp.execute('notify', 'warning', Valamis.language[messageLabel]);
        }
    });

    Views.MainAppLayoutView = Marionette.LayoutView.extend({
        regions: {
            allCoursesRegion: '#allCoursesRegion',
            myCoursesRegion: '#studentViewMyCourses',
            availableCoursesRegion: '#studentViewAvailableCourses'
        },
        initialize: function() {
            this.template = (!!this.options.isEditor)
              ? '#allCoursesMainAppLayoutTemplate'
              : '#coursesBrowserMainAppLayoutTemplate';
        },
        onRender: function() {
            var isEditor = !!this.options.isEditor;
            if (isEditor) {
                var allCoursesCollection = new allCourses.Entities.CourseCollection();
                var layoutView = new allCourses.Views.AppLayoutView({
                    courses: allCoursesCollection,
                    portletName: 'coursesManager',
                    isEditor: isEditor
                });
                this.allCoursesRegion.show(layoutView);
                allCoursesCollection.trigger('fetchCollection');
            } else {
                this.$el.addClass('val-tabs');

                var myCoursesCollection = new allCourses.Entities.UserCourseCollection();
                var myCoursesLayoutView = new allCourses.Views.AppLayoutView({
                    courses: myCoursesCollection,
                    portletName: 'coursesBrowser',
                    isEditor: isEditor
                });
                this.myCoursesRegion.show(myCoursesLayoutView);

                var availableCoursesCollection = new allCourses.Entities.NotMemberCourseCollection();
                var availableCoursesLayoutView = new allCourses.Views.AppLayoutView({
                    courses: availableCoursesCollection,
                    portletName: 'coursesBrowserAvailable',
                    isEditor: isEditor
                });
                this.availableCoursesRegion.show(availableCoursesLayoutView);

                this.$('#studentViewTabs a[href="#studentViewMyCourses"]').on('shown.bs.tab', function () {
                    myCoursesCollection.trigger('fetchCollection');
                });
                this.$('#studentViewTabs a[href="#studentViewAvailableCourses"]').on('shown.bs.tab', function () {
                    availableCoursesCollection.trigger('fetchCollection');
                });
            }
        },
        onShow: function () {
            var activeTabSelector = '#studentViewMyCourses';
            this.$('#studentViewTabs').find('a[href="' + activeTabSelector + '"]').tab('show');
        }
    });
});