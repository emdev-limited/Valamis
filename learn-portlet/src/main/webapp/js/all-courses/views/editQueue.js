allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.EditQueueListItemView = valamisApp.Views.BaseLayout.ListItemView.extend({
        templateHelpers: function() {
            return {
                isMyId: this.model.get('id') == Utils.getUserId()
            }
        },
        template: '#allCoursesMemberQueueItemViewTemplate',
        ui: {
            acceptQueue: '.js-button-accept',
            denyQueue: '.js-button-deny'
        },
        triggers: {
            'click @ui.acceptQueue': 'items:list:join:queue',
            'click @ui.denyQueue': 'items:list:deny:queue'
        },
        initialize: function() {
            this.ui = _.extend({}, this.constructor.__super__.ui, this.ui);
            this.events = _.extend({}, this.constructor.__super__.events, this.events);
        },
        onRender: function () {
            if (!this.options.hasFreePlace) {
                this.ui.acceptQueue.attr('disabled', 'disabled');
            }
        }
    });

    Views.EditQueueListView = valamisApp.Views.BaseLayout.ListView.extend({
        childView: Views.EditQueueListItemView,
        childViewOptions: function() {
            return {
                hasFreePlace: this.options.courseModel.hasFreePlaces()
            }
        },
        onRender: function() {
            this.collection.on('sync', function() {
                this.ui.noItemsLabel.removeClass('shifted no-items');
                this.ui.noItemsLabel.toggleClass('hidden', this.collection.length !== 0);
                this.ui.noItemsLabel.text(Valamis.language['noItemsLabel']);
                if (!this.options.courseModel.hasFreePlaces() && this.collection.length !== 0) {
                    this.ui.noItemsLabel.toggleClass('hidden', false);
                    this.ui.noItemsLabel.removeClass('no-items');
                    this.ui.noItemsLabel.addClass('shifted');
                    this.ui.noItemsLabel.text(Valamis.language['notEnoughPlacesError']);
                }
            }, this);
        }
    });

    Views.QueueView = valamisApp.Views.BaseLayout.MainLayoutView.extend({
        templateHelpers: {
            hideFooter: true,
            hideToolbar: true
        },
        childEvents: {
            'items:list:join:queue': function(childView, model) {
                var userId = model.model.get('id');

                var that = this;
                this.collection.joinToQueue({}, {
                    userId: userId,
                    memberType: 'user',
                    courseId: this.courseId
                }).then(function (res) {
                    that.fetchCollection();
                    valamisApp.execute('notify', 'success', Valamis.language['overlayCompleteMessageLabel']);
                }, function (err, res) {
                    valamisApp.execute('notify', 'error', Valamis.language['overlayFailedMessageLabel']);
                });
            },
            'items:list:deny:queue': function(childView, model) {
                var userId = model.model.get('id');

                var that = this;
                this.collection.denyFromQueue({}, {
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

            this.collection = new allCourses.Entities.QueueCollection();
            this.collection.on('userCollection:updated', function (details) {
                this.updatePagination(details);
                this.trigger('queues:list:update:count', details.total);
            }, this);

            this.constructor.__super__.initialize.apply(this, arguments);
        },
        onRender: function() {
            this.itemsListView = new Views.EditQueueListView({
                collection: this.collection,
                courseModel: this.options.courseModel
            });

            this.constructor.__super__.onRender.apply(this, arguments);
            this.$('#valamisBaseToolbar').hide();
        }
    });
});
