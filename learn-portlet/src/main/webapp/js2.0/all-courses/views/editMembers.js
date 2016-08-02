allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    var MEMBER_TYPE = {
        USER: 'user',
        ORGANIZATION: 'organization',
        GROUP: 'userGroup'
    };

    Views.EditMembersToolbarView = valamisApp.Views.BaseLayout.ToolbarView.extend({
        template: '#allCoursesMembersToolbarViewTemplate',
        templateHelpers: function() {
            var isMemberUser = this.model.get('memberType') === MEMBER_TYPE.USER;

            var params = {
                isAddView: !this.model.get('available'),
                memberTypeObject: MEMBER_TYPE,
                isMemberUser: isMemberUser,
                addButtonLabelText: Valamis.language[this.model.get('memberType') + 'AddMembersLabel']
            };

            if (isMemberUser) _.extend(params, { organizations: this.organizations.toJSON() });

            return params;
        },
        ui: {
            addItems: '.js-add-items',
            memberTypeItem: '.js-member-type .dropdown-menu > li',
            organizationItem: '.js-organizations-filter .dropdown-menu > li',
            deleteItems: '.js-delete-items',
            selectAllItem: '.js-selectAll-item',
            globalControlArea: '.display-on-selection',
            globalAssignRoles: '.js-global-assign-role',
            globalDeleteItem: '.js-global-delete-item',
            deselectAllLabel: '.deselect-all-label'
        },
        events: {
            'click @ui.addItems': 'addItems',
            'click @ui.memberTypeItem': 'changeMemberType',
            'click @ui.organizationItem': 'changeOrganization',
            'click @ui.selectAllItem': 'toggleSelectAll',
            'click @ui.globalAssignRoles': 'toggleGlobalRoleAssignment',
            'click @ui.globalDeleteItem': 'toggleSGlobalItemDeletion',
            'click @ui.deselectAllLabel': 'toggleDeSelectAll'
        },
        modelEvents: {
            'change:memberType': 'render'
        },
        onValamisControlsInit: function () {
            this.$('.js-member-type').valamisDropDown('select', this.model.get('memberType'));
            this.ui.globalControlArea.hide();
        },
        initialize: function() {
            this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
            this.events = _.extend({}, this.constructor.__super__.events, this.events);

            this.organizations = new Valamis.OrganizationCollection();
            this.organizations.on('sync', this.render, this);
            this.organizations.fetch();
        },
        addItems: function(e) {
            var type = $(e.target).closest(this.ui.addItems).attr('data-value');
            this.triggerMethod('items:list:add', type)
        },
        changeOrganization: function(e) {
            this.model.set({ orgId: $(e.target).data('value') });
        },
        changeMemberType: function(e) {
            var newMemberType = $(e.target).data('value');
            this.model.set(_.extend({ memberType: newMemberType }, this.model.defaults));
            this.triggerMethod('items:list:memberType:changed', newMemberType);
        },
        toggleSelectAll: function() {
            this.triggerMethod('items:list:action:selectAll:items', this.ui.selectAllItem.is(':checked'));
        },
        toggleDeSelectAll: function() {
            this.triggerMethod('items:list:action:selectAll:items', false);
        },
        toggleGlobalRoleAssignment: function() {
            this.triggerMethod('items:list:assign:role');
        },
        toggleSGlobalItemDeletion: function() {
            this.triggerMethod('items:list:action:delete:items');
        }
    });

    Views.AddMembersListItemView = valamisApp.Views.BaseLayout.SelectListItemView.extend({
        template: '#allCoursesAddMembersItemViewTemplate'
    });

    Views.AddMembersListView = valamisApp.Views.BaseLayout.ListView.extend({
        childView:  Views.AddMembersListItemView
    });

    Views.EditMembersListItemView = valamisApp.Views.BaseLayout.ListItemView.extend({
        template: '#allCoursesEditMembersItemViewTemplate',
        ui: {
            checkBox: '.val-checkbox',
            editRoles: '.js-assign-role'
        },
        events: {
            'click @ui.checkBox': 'displayGlobalControl',
            'click @ui.editRoles': 'editRoles'
        },
        templateHelpers: function() {
            var status = this.model.get('status');
            return {
                statusItemLabel: (status) ? Valamis.language[status.toLowerCase() + 'StatusLabel'] : ''
            };
        },
        initialize: function() {
            this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
            this.events = _.extend({}, this.constructor.__super__.events, this.events);
        },
        onRender: function() {
            this.$(".role-col").text(this.model.get("roles").join(", "));
        },
        deleteItem: function() {
            this.destroy();
            this.triggerMethod('items:list:delete:item', this.model);
        },
        editRoles: function() {
            this.triggerMethod('items:list:assign:role', this.model);
        },
        displayGlobalControl: function() {
            this.triggerMethod('items:list:selectionChanged');
        }
    });

    Views.EditMembersListView = valamisApp.Views.BaseLayout.ListView.extend({
        childView: Views.EditMembersListItemView
    });

    Views.MembersView = valamisApp.Views.BaseLayout.MainLayoutView.extend({
        templateHelpers: function() {
            return {
                hideFooter: !(this.available)
            }
        },
        childEvents: {
            'items:list:action:selectAll:items': function (childView, selected) {
                var filterValue = selected ? ":not(:checked)" : ":checked";
                this.$(".js-select-item").filter(filterValue).trigger("click")
            },
            'items:list:action:delete:items': function (childView) {
                this.deleteMembers(this.getSelectedItems());
            },
            'items:list:assign:role': function(childView, model) {
                var addSiteRolesView = new Views.AddSiteRoles({
                    model: model
                });

                //model should be defined, if selectedUsers is empty
                var userIds = model ? [model.id] : this.getSelectedItems();

                var that = this;
                var addSiteRolesModalView = new valamisApp.Views.ModalView({
                    header: Valamis.language['assignRolesLabel'],
                    customClassName: "add-site-roles-modal",
                    contentView: addSiteRolesView,
                    submit: function() {
                        memberType = addSiteRolesView.memberType;

                        addSiteRolesView.collection.setSiteRoles({}, {
                            memberIds: userIds,
                            courseId: that.courseId,
                            roleIds: addSiteRolesView.getSelectedItems()
                        }).then(function() {
                            valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
                            that.fetchCollection(true);
                        }, function (err, res) {
                            valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                        });
                    }
                });

                valamisApp.execute('modal:show', addSiteRolesModalView);
            },
            'items:list:add': function(childView, memberType) {
                var addMembersView = new Views.MembersView({
                    available: true,
                    memberType: memberType,
                    courseId: this.courseId
                });

                var that = this;
                var addMembersModalView = new valamisApp.Views.ModalView({
                    header: Valamis.language['addMembersLabel'],
                    customClassName: "add-site-members-modal",
                    contentView: addMembersView,
                    submit: function() {
                        memberType = addMembersView.memberType;

                        addMembersView.collection.saveToCourse({}, {
                            memberIds: addMembersView.getSelectedItems(),
                            memberType: memberType,
                            courseId: that.courseId
                        }).then(function() {
                            that.filter.set({ memberType: memberType });
                            valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
                            that.fetchCollection(true);
                        }, function (err, res) {
                            valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                        });
                    }
                });

                valamisApp.execute('modal:show', addMembersModalView);
            },
            'items:list:memberType:changed': function(childView, memberType) {
                this.memberType = memberType;
                this.cleanSelectedItems();
            },
            'items:list:delete:item': function(childView, model) {
                this.deleteMembers(model.get('id'));
            },
            'items:list:selectionChanged': function(childView, model) {
                this.configureOnSelectionDisplay();
            }
        },
        initialize: function(options) {
            this.childEvents = _.extend({}, this.constructor.__super__.childEvents, this.childEvents);

            this.available = !!(this.options.available);
            this.courseId = this.options.courseId;
            this.memberType = this.options.memberType || MEMBER_TYPE.USER;

            this.filter = new valamisApp.Entities.Filter({
                memberType: this.memberType,
                courseId: this.courseId,
                available: this.available
            });
            this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

            var that = this;
            this.collection = new allCourses.Entities.MembersCollection();
            this.collection.on('userCollection:updated', function (details) {
                that.$('.display-on-selection').hide();
                this.updatePagination(details);
                this.trigger('members:list:update:count', details.total);
            }, this);

            this.constructor.__super__.initialize.apply(this, arguments);
        },
        onRender: function() {
            this.itemsToolbarView = new Views.EditMembersToolbarView({
                model: this.filter,
                paginatorModel: this.paginatorModel,
            });

            this.itemsListView = (this.available)
                ? new Views.AddMembersListView({ collection: this.collection })
                : new Views.EditMembersListView({
                memberType: this.memberType,
                collection: this.collection,
                courseId: this.courseId
            });

            this.constructor.__super__.onRender.apply(this, arguments);
        },
        deleteMembers: function(memberIds) {
            var that = this;
            this.collection.deleteFromCourse({}, {
                memberIds: memberIds,
                courseId: this.courseId,
                memberType: this.memberType
            }).then(function (res) {
                that.fetchCollection();
                valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
            }, function (err, res) {
                valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
            });
        },
        configureOnSelectionDisplay: function() {
            var checkedUserCount = this.$('.js-select-item').filter(':checked').length;
            var controlArea = this.$('.display-on-selection');
            if(checkedUserCount > 0) {
                controlArea.slideDown(400);
                this.$('.display-on-selection .user-selection-count-label')
                    .text(checkedUserCount + " " + Valamis.language['membersSelectedLabel']);
            } else {
                controlArea.slideUp(400);
                this.$('.display-on-selection .user-selection-count-label')
                    .text("0 " + Valamis.language['membersSelectedLabel']);
            }
        },
        getSelectedItems: function() {
            return this.collection.filter(function (model) {
                return model.get('itemSelected');
            }).map(function(model) {
                return model.get('id')
            });
        }
    });
});