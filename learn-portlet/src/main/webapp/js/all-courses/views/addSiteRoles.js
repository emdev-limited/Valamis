allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.AddSiteRolesToolbarView = valamisApp.Views.BaseLayout.ToolbarView.extend({
        template: '#allCoursesAddSiteRolesToolbarViewTemplate',
        ui: {
            selectAllItem: '.js-selectAll-item'
        },
        events: {
            'click @ui.selectAllItem': 'toggleSelectAll'
        },
        initialize: function() {
            this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
            this.events = _.extend({}, this.constructor.__super__.events, this.events);
        },
        onRender: function() {},
        toggleSelectAll: function() {
            this.triggerMethod('items:list:action:selectAll:items', this.ui.selectAllItem.is(':checked'));
        }
    });

    Views.AddSiteRolesListItemView = valamisApp.Views.BaseLayout.SelectListItemView.extend({
        template: '#allCoursesAddSiteRolesItemViewTemplate',
        initialize: function() {
            this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
            this.events = _.extend({}, this.constructor.__super__.events, this.events);
        },
        onRender: function() {
            if(this.model.get("selected")) {
                this.$(".js-select-item").trigger("click");
            }
        }
    });

    Views.AddSiteRolesListView = valamisApp.Views.BaseLayout.ListView.extend({
        childView: Views.AddSiteRolesListItemView
    });

    Views.AddSiteRoles = valamisApp.Views.BaseLayout.MainLayoutView.extend({
        childEvents: {
            'items:list:action:selectAll:items': function (childView, selected) {
                var filterValue = selected ? ":not(:checked)" : ":checked";
                this.$(".js-select-item").filter(filterValue).trigger("click")
            }
        },
        initialize: function(options) {
            this.userRoles = this.model ? this.model.get("roles") : [];
            this.childEvents = _.extend({}, this.constructor.__super__.childEvents, this.childEvents);
            this.courseId = this.options.courseId;
            this.collection = new allCourses.Entities.SiteRoleCollection([],{roles: this.userRoles});
            this.constructor.__super__.initialize.apply(this, arguments);
        },
        onRender: function() {
            this.itemsToolbarView = new Views.AddSiteRolesToolbarView();
            this.itemsListView = new Views.AddSiteRolesListView({ collection: this.collection });
            this.constructor.__super__.onRender.apply(this, arguments);
        }
    });
});