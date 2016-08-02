allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.MainEditView =  Marionette.LayoutView.extend({
        template: '#allCoursesMainEditView',
        ui: {
            requestCount: '.request-count-tab-label'
        },
        regions: {
            'editCourseDetails': '#editCourseDetails',
            'editCourseMembers': '#editCourseMembers',
            'editRequests': '#editRequests'
        },
        onRender: function() {
            this.editCourseView = new Views.EditCourseView({ model: this.model });
            this.editCourseDetails.show(this.editCourseView);

            this.$('.request-count-tab-label').hide();

            if(this.model.get('id')) {
                this.showTabs();
            }

            var that = this;
            this.$('#editCourseTabs a[href="#editCourseDetails"]').on('shown.bs.tab', function () {
                that.editCourseView.focusTitle();
            });
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

            if (this.model.get('membershipType') === Views.CourseTypes.ON_REQUEST) {
                this.showRequestsTab();
            }
        },

        showMembersTab: function() {
            this.$('#editCourseTabs a[href="#editCourseMembers"]').removeClass('hidden');
            this.editMembersView = new Views.MembersView({
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
                courseId: this.model.get('id')
            });

            this.editRequestsView.on('requests:list:update:count', function(total) {
               this.updateRequestCountLabel(total);
            }, this);

            this.editRequests.show(this.editRequestsView);
        },
        saveImage: function(doAfter){
            this.editCourseView.trigger('view:submit:image',doAfter);
        },
        updateRequestCountLabel: function(total) {
            if(total) {
                this.ui.requestCount.show();
                this.ui.requestCount.text(total);
            } else {
                this.ui.requestCount.hide();
            }
        }
    });

});
