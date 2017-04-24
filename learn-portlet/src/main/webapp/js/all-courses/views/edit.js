allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.MainEditView =  Marionette.LayoutView.extend({
        template: '#allCoursesMainEditView',
        templateHelpers: function () {
            return {
                isEditor: this.options.isEditor,
                isMemberEditor: !!this.model.get('canEditMembers')
            }
        },
        ui: {
            requestCount: '.request-count-tab-label',
            queueCount: '.queue-count-tab-label',
            tabs: '#editCourseTabs a'
        },
        regions: {
            'editCourseDetails': '#editCourseDetails',
            'editCourseMembers': '#editCourseMembers',
            'editQueues': '#editQueues',
            'editRequests': '#editRequests'
        },
        events: {
            'click @ui.tabs': 'showTab'
        },
        showTab: function(e) {
            e.preventDefault();
            $(e.target).tab('show');
        },
        onRender: function() {
            var canEditCourse = this.model.has('canEditCourse') ? this.model.get('canEditCourse') : true;
            if (this.options.isEditor && canEditCourse) { 
                this.editCourseView = new Views.EditCourseView({ model: this.model });
            }
            else {
                this.editCourseView = new Views.ViewCourseView({model: this.model});
                var membershipType = this.model.get('membershipType');
                var button = this.$('.js-submit-button');
                if (this.options.isEditor) {
                    button.remove();
                } else {
                    if (this.model.get('isMember')) {
                        button.text(Valamis.language['buttonLabelLeave']);
                        button.removeClass("primary neutral slides-gray").addClass("danger");
                    } else if (this.model.get('hasRequestedMembership')) {
                        button.text(Valamis.language['buttonLabelRequested']);
                        button.removeClass("primary danger joinButton").attr('disabled', 'disabled');
                    } else if (this.model.get('isQueueMember')) {
                        button.text(Valamis.language['buttonLabelRequestedQueue']);
                        button.removeClass("primary danger joinButton").attr('disabled', 'disabled');
                    } else if (membershipType === Views.CourseTypes.OPEN) {
                        button.text(Valamis.language['buttonLabelJoin']);
                        button.removeClass("neutral danger slides-gray").addClass("primary");
                    } else if (membershipType === Views.CourseTypes.ON_REQUEST) {
                        button.text(Valamis.language['buttonLabelRequestJoin']);
                        button.removeClass("primary danger slides-gray").addClass("neutral");
                    }
                }
            }
            this.editCourseDetails.show(this.editCourseView);

            this.$('.request-count-tab-label').hide();

            if(this.model.get('id')) {
                this.showTabs();
            }

            var that = this;
            if (this.options.isEditor) {
                this.$('#editCourseTabs a[href="#editCourseDetails"]').on('shown.bs.tab', function () {
                    that.editCourseView.focusTitle();
                });
            }
        },
        changeCourseRelation: function() {
            if(this.model.get('isMember')) {
                this.triggerMethod('courseList:leave:course', this.model);
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
        onShow: function() {
            var activeTabSelector = '#edit' + this.options.activeTab;
            this.$('#editCourseTabs a[href="'+ activeTabSelector +'"]').tab('show');
            this.$(activeTabSelector).addClass('active');
        },
        updateModel: function() {
            return this.editCourseView.updateModel();
        },
        showTabs: function() {
            this.showMembersTab();
            if (this.options.isEditor) {
                if (this.model.get('membershipType') === Views.CourseTypes.ON_REQUEST) {
                    this.showRequestsTab();
                }
                if (this.model.get('userLimit') > 0) {
                    this.showQueueTab();
                }
            }
        },

        showMembersTab: function() {
            this.$('#editCourseTabs a[href="#editCourseMembers"]').removeClass('hidden');
            this.editMembersView = new Views.MembersView({
                courseModel: this.model,
                courseId: this.model.get('id')
            });
            this.editMembersView.on('members:list:update:count', function(total) {
                this.model.set('userCount', total)
            }, this);
            this.editCourseMembers.show(this.editMembersView);
        },

        showRequestsTab: function() {
            this.$('#editCourseTabs a[href="#editRequests"]').removeClass('hidden');
            this.editRequestsView = new Views.RequestsView({
                courseModel: this.model
            });

            this.editRequestsView.on('requests:list:update:count', function(total) {
                this.updateRequestCountLabel(total);
                this.editMembersView.fetchCollection();
                if (this.editQueueView && this.model.get('userLimit') <= this.model.get('userCount')) {
                    this.editQueueView.fetchCollection();
                }
            }, this);

            this.editRequests.show(this.editRequestsView);
        },

        showQueueTab: function() {
            this.$('#editCourseTabs a[href="#editQueues"]').removeClass('hidden');
            this.editQueueView = new allCourses.Views.QueueView({
                courseModel: this.model
            });

            this.editQueueView.on('queues:list:update:count', function(total) {
                this.updateQueueCountLabel(total);
                this.editMembersView.fetchCollection();
            }, this);

            this.editQueues.show(this.editQueueView);
        },
        updateLogo: function(doAfter){
            var course = this.editCourseView.model;
            var hasLogo = !!course.has('logo') || course.get('hasLogo');
            if (hasLogo) {
                this.editCourseView.trigger('view:submit:image', doAfter, !course.get('logo'));
            }
            else {
                course.deleteLogo().then( doAfter );
            }
        },
        updateTheme: function() {
            if (this.model.get('templateId')) this.model.setTheme();
        },
        updateRequestCountLabel: function(total) {
            if(total) {
                this.ui.requestCount.show();
                this.ui.requestCount.text(total);
            } else {
                this.ui.requestCount.hide();
            }
        },
        updateQueueCountLabel: function(total) {
            if(total) {
                this.ui.queueCount.show();
                this.ui.queueCount.text(total);
                this.model.set('queueCount', total);
            } else {
                this.ui.queueCount.hide();
            }
        }
    });

});
