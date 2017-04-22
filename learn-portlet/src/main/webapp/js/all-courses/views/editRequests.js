allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.EditRequestsListItemView = valamisApp.Views.BaseLayout.ListItemView.extend({
        templateHelpers: function() {
            return {
                isMyId: this.model.id == Utils.getUserId()
            }
        },
        template: '#allCoursesMemberRequestsItemViewTemplate',
        ui: {
            acceptRequest: '.js-button-accept',
            denyRequest: '.js-button-deny'
        },
        events: {
            'click @ui.acceptRequest': 'acceptRequest',
            'click @ui.denyRequest': 'denyRequest'
        },
        initialize: function() {
            this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
            this.events = _.extend({}, this.constructor.__super__.events, this.events);
        },
        acceptRequest: function(){this.triggerMethod('items:list:accept:request', this.model);},
        denyRequest: function(){this.triggerMethod('items:list:deny:request', this.model);}
    });

    Views.EditRequestsListView = valamisApp.Views.BaseLayout.ListView.extend({
        childView: Views.EditRequestsListItemView
    });

    Views.RequestsView = valamisApp.Views.BaseLayout.MainLayoutView.extend({
        templateHelpers: function() {
            return {
                hideFooter: true,
                hideToolbar: true
            }
        },
        childEvents: {
            'items:list:accept:request': function(childView, model) {
                var userId = model.get('id');

                var that = this;
                this.collection.acceptRequest({}, {
                    userId: userId,
                    courseId: this.courseId
                }).then(function (res) {
                    that.fetchCollection();
                    if (that.options.courseModel.hasFreePlaces()) {
                        valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
                    }
                    else {
                        valamisApp.execute('notify', 'warning', Valamis.language['notEnoughPlacesError'] 
                            + ' ' + Valamis.language['addUsertoQueueLabel']);
                    }
                }, function (err, res) {
                    valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                });
            },

            'items:list:deny:request': function(childView, model) {
                var userId = model.get('id');

                var that = this;
                this.collection.denyRequest({}, {
                    userId: userId,
                    courseId: this.courseId
                }).then(function (res) {
                    that.fetchCollection();
                    valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
                }, function (err, res) {
                    valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                });
            }
        },

        initialize: function(options) {
            this.courseId = this.options.courseModel.get('id');

            this.filter = new valamisApp.Entities.Filter({
                courseId: this.courseId
            });
            this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

            this.collection = new allCourses.Entities.RequestCollection();
            this.collection.on('userCollection:updated', function (details) {
                this.updatePagination(details);
                this.trigger('requests:list:update:count', details.total);
            }, this);

            this.constructor.__super__.initialize.apply(this, arguments);
        },
        onRender: function() {

            this.itemsListView = new Views.EditRequestsListView({
                collection: this.collection,
                courseId: this.courseId
            });

            this.constructor.__super__.onRender.apply(this, arguments);
            this.$('#valamisBaseToolbar').hide();
        }
    });
});
