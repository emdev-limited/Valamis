allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.RowItemView = Marionette.ItemView.extend({
        tagName: 'div',
        className: 'tile s-12 m-4 l-2',
        template: '#allCoursesRowViewTemplate',
        events: {
            'click .dropdown-menu > li.js-course-info-edit': 'editCourseInfo',
            'click .dropdown-menu > li.js-course-info-view': 'viewCourseInfo',
            'click .dropdown-menu > li.js-course-members-edit': 'editCourseMembers',
            'click .dropdown-menu > li.js-course-requests-edit': 'editCourseRequests',
            'click .dropdown-menu > li.js-course-queue-edit': 'editCourseQueue',
            'click .dropdown-menu > li.js-course-delete': 'deleteCourse',
            'click .js-course-join': 'changeCourseRelation'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        modelEvents: {
            'change:userCount': 'render'
        },
        templateHelpers: function() {
            var packageRating = this.model.get('rating');

            var beginDate = this.model.get('beginDate');
            var beginDateFormatted = beginDate ? Utils.formatDate(beginDate, 'lll') : '';
            var endDate = this.model.get('endDate');
            var endDateFormatted = endDate ? Utils.formatDate(endDate, 'lll') : '';
            var courseDateInterval;

            if (beginDate || endDate) {
                courseDateInterval = beginDateFormatted + '—' + endDateFormatted;
            }
            return { 
                isRestrictedCourse: this.model.get('membershipType') === Views.CourseTypes.ON_REQUEST,
                isQueue: this.model.get('userLimit') > 0,
                timestamp: Date.now(),
                isEditor: this.options.isEditor,
                isMemberEditor: !!this.model.get('canEditMembers'),
                ratingAverage: Math.round(packageRating.average * 10) / 10,
                ratingScore: packageRating.score,
                noAverage: (packageRating.total == 0),
                notRated: (packageRating.score == 0),
                beginDateFormatted: beginDateFormatted,
                endDateFormatted: endDateFormatted,
                courseDateInterval: courseDateInterval
            }
        },
        onRender: function () {
            this.$el.toggleClass('unpublished', !(this.model.get('isActive')));

            var memberShipType = this.model.get('membershipType');
            var isMember = this.model.get('isMember');
            var hasRequested = this.model.get('hasRequestedMembership');
            var inQueue = this.model.get('isQueueMember');
            this.configureJoinButton(memberShipType,isMember, hasRequested, inQueue);
            this.setMembershipType(memberShipType, isMember, hasRequested);
            this.setTags(this.model.get('tags'));

            if (Utils.isSignedIn()) {
                var that = this;
                this.$('.js-valamis-rating').
                    on('valamisRating:changed', function(e, score) {
                        that.setCourseRating(score)
                    }).
                    on('valamisRating:deleted', function(e) {
                        that.deleteCourseRating()
                    });
            } else {
                this.$('.js-valamis-rating').valamisRating('disable');
            }
        },
        setCourseRating: function(score) {
            var that = this;
            this.model.setRating({}, {
                ratingScore: score,
                success: function (response) {
                    that.$('.js-valamis-rating').valamisRating('score', response.average, score);
                }
            });
        },
        deleteCourseRating: function() {
            var that = this;
            this.model.deleteRating({}, {
                success: function (response) {
                    that.$('.js-valamis-rating').valamisRating('score', response.average, 0);
                }
            });
        },
        setMembershipType: function(type, isMember, hasRequested) {
            var courseTypeField = this.$('.course-type');

            var languageKey;
            switch(type) {
                case Views.CourseTypes.OPEN: languageKey = 'membershipTypeOptionOpenLabel'; break;
                case Views.CourseTypes.ON_REQUEST: languageKey = 'membershipTypeOptionOnRequestLabel'; break;
                case Views.CourseTypes.CLOSED: languageKey = 'membershipTypeOptionClosedLabel'; break;
            }

            if(languageKey) {
                courseTypeField.text(Valamis.language[languageKey]);
            }
        },
        setTags: function(tags) {
            this.$('.course-tags').text(tags.map(function(tag){return tag.text;}).join(" • "));
        },
        configureJoinButton: function(type, isMember, hasRequested, inQueue) {
            var button = this.$('.js-course-join');

            if(isMember){
                button.text(Valamis.language['buttonLabelLeave']);
            } else if (hasRequested || inQueue) {
                button.remove();
            } else if (type === Views.CourseTypes.OPEN) {
                button.text(Valamis.language['buttonLabelJoin']);
            } else if (type === Views.CourseTypes.ON_REQUEST) {
                button.text(Valamis.language['buttonLabelRequestJoin']);
            }
        },
        editCourseInfo: function(){
            this.triggerMethod('courseList:edit', this.model, 'CourseDetails');
        },
        viewCourseInfo: function(){
            this.triggerMethod('courseList:view', this.model, 'CourseDetails');
        },
        editCourseMembers: function(){
            this.triggerMethod('courseList:edit', this.model, 'CourseMembers');
        },
        editCourseRequests: function(){
            this.triggerMethod('courseList:edit', this.model, 'Requests');
        },
        editCourseQueue: function(){
            this.triggerMethod('courseList:edit', this.model, 'Queues');
        },
        confirmLeave: function() {
            var that = this;
            valamisApp.execute('valamis:confirm', { message: Valamis.language['warningLeaveCourseMessageLabel'] }, function(){
                that.triggerMethod('courseList:leave:course', that.model);
            });
        },
        changeCourseRelation: function(){
            if(this.model.get('isMember')) {
                this.confirmLeave();
            } else {
                var targetMethod;
                switch(this.model.get('membershipType')) {
                    case Views.CourseTypes.OPEN:
                        targetMethod = 'courseList:join:course';
                        break;
                    case Views.CourseTypes.ON_REQUEST:
                        targetMethod = 'courseList:requestJoin:course';
                        break;
                }
                if(targetMethod) {
                    this.triggerMethod(targetMethod, this.model);
                }
            }
        },
        deleteCourse: function(){
            var that = this;
            valamisApp.execute('valamis:confirm', { message: Valamis.language['warningDeleteCourseMessageLabel'] }, function(){
                that.deleteCourseTrigger();
            });
        },
        deleteCourseTrigger: function() {
            this.triggerMethod('courseList:delete:course', this.model);
        }
    });

    Views.CourseListView = Marionette.CompositeView.extend({
        template: '#allCoursesLayoutTemplate',
        childViewContainer: '.js-courses-list',
        childView: Views.RowItemView,
        ui: {
            coursesList: '.js-courses-list'
        },
        childViewOptions: function() {
            return {
                isEditor: this.options.isEditor
            }
        },
        initialize: function () {
            var that = this;
            that.collection.on('sync', function() {
                that.$('.js-courses-table').toggleClass('hidden', that.collection.total == 0);
                that.$('.js-no-items').toggleClass('hidden', that.collection.hasItems());
            });
            that.options.settings.on('change:displayMode', this.setDisplayMode, this);
        },
        onRender: function () {
            this.$('.valamis-tooltip').tooltip();
            this.setDisplayMode();
        },
        setDisplayMode: function() {
            var displayMode = this.options.settings.get('displayMode')|| Views.DISPLAY_TYPE.LIST;
            this.ui.coursesList.removeClass('list');
            this.ui.coursesList.removeClass('tiles');
            this.ui.coursesList.addClass(displayMode);
            valamisApp.execute('update:tile:sizes', this.$el);
        }
    });
});