allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.RowItemView = Marionette.ItemView.extend({
        tagName: 'div',
        className: 'tile s-12 m-4 l-2',
        template: '#allCoursesRowViewTemplate',
        events: {
            'click .dropdown-menu > li.js-course-info-edit': 'editCourseInfo',
            'click .dropdown-menu > li.js-course-members-edit': 'editCourseMembers',
            'click .dropdown-menu > li.js-course-requests-edit': 'editCourseRequests',
            'click .dropdown-menu > li.js-course-delete': 'deleteCourse',
            'click .joinButton': 'changeCourseRelation'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        modelEvents: {
            'change': 'render'
        },
        templateHelpers: function() {
            return {
                isRestrictedCourse: this.model.get('membershipType') === Views.CourseTypes.ON_REQUEST,
                timestamp: Date.now(),
                isEditor: this.options.isEditor
            }
        },
        onValamisControlsInit: function () {
            var courseRating = this.model.get('rating');
            var that = this;
            this.$('.js-valamis-rating').
                valamisRating({
                    score: courseRating.score,
                    average: courseRating.average
                }).on('valamisRating:changed', function(e, score) {
                    that.setCourseRating(score)
                }).on('valamisRating:deleted', function(e) {
                    that.deleteCourseRating()
                });
        },
        onRender: function () {
            this.$el.toggleClass('unpublished', !(this.model.get('isActive')));

            var memberShipType = this.model.get('membershipType');
            var isMember = this.model.get('isMember');
            var hasRequested = this.model.get('hasRequestedMembership');
            this.configureJoinButton(memberShipType,isMember, hasRequested);
            this.setMembershipType(memberShipType, isMember, hasRequested);
            this.setTags(this.model.get('tags'));
        },
        setCourseRating: function(score) {
            this.model.setRating({}, {ratingScore: score});
        },
        deleteCourseRating: function() {
            var that = this;
            this.model.unsetRating({}, {
                success: function(response) {
                    that.$('.js-valamis-rating').valamisRating('average', response.average);
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
        configureJoinButton: function(type, isMember, hasRequested) {
            var button = this.$('.joinButton');

            if(isMember){
                button.text(Valamis.language['buttonLabelLeave']);
                button.removeClass("primary neutral slides-gray").addClass("danger");
            } else if (hasRequested) {
                button.text(Valamis.language['buttonLabelRequested']);
                button.removeClass("primary danger joinButton").attr('disabled', 'disabled');
            } else if (type === Views.CourseTypes.OPEN) {
                button.text(Valamis.language['buttonLabelJoin']);
                button.removeClass("neutral danger slides-gray").addClass("primary");
            } else if (type === Views.CourseTypes.ON_REQUEST) {
                button.text(Valamis.language['buttonLabelRequestJoin']);
                button.removeClass("primary danger slides-gray").addClass("neutral");
            }
        },
        editCourseInfo: function(){
            this.triggerMethod('courseList:edit', this.model, 'CourseDetails');
        },
        editCourseMembers: function(){
            this.triggerMethod('courseList:edit', this.model, 'CourseMembers');
        },
        editCourseRequests: function(){
            this.triggerMethod('courseList:edit', this.model, 'Requests');
        },
        confirmLeave: function() {
            var that = this;
            valamisApp.execute('delete:confirm', { message: Valamis.language['warningLeaveCourseMessageLabel'] }, function(){
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
            valamisApp.execute('delete:confirm', { message: Valamis.language['warningDeleteCourseMessageLabel'] }, function(){
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
            this.$('.js-courses-list').removeClass('list');
            this.$('.js-courses-list').removeClass('tiles');
            this.$('.js-courses-list').addClass(displayMode);
            valamisApp.execute('update:tile:sizes', this.$el);
        }
    });
});