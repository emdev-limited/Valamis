valamisApp.module('Views.AddEvents', function (AddEvents, valamisApp, Backbone, Marionette, $, _) {

    var SEARCH_TIMEOUT = 800;

    AddEvents.EventsToolbarView = Marionette.ItemView.extend({
        template: '#eventsSelectToolbarViewTemplate',
        templateHelpers: function() {
            return { calendars: this.options.calendars.toJSON() }
        },
        behaviors: {
            ValamisUIControls: {}
        },
        events: {
            'keyup .js-search': 'changeSearchText',
            'click .js-sort-filter .dropdown-menu > li': 'changeSort',
            'click .js-calendar-filter .dropdown-menu > li': 'changeCalendar'
        },
        changeSearchText:function(e){
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.model.set('searchtext', $(e.target).val());
            }, SEARCH_TIMEOUT);
        },
        changeSort: function(e){
            this.model.set('sort', $(e.target).attr('data-value'));
        },
        changeCalendar: function(e) {
            this.model.set('calendarId', $(e.target).attr('data-value'));
        }
    });

    AddEvents.EventsListItemView = Marionette.ItemView.extend({
        template: '#eventsSelectListItemViewTemplate',
        templateHelpers: function() {
            return {
                startTimeFormatted: Utils.formatDate(this.model.get('startTime'), 'lll'),
                endTimeFormatted: Utils.formatDate(this.model.get('endTime'), 'lll')
            }
        },
        tagName: 'tr',
        ui: {
            select: '.js-select-event'
        },
        events: {
            'click @ui.select': 'selectGoal'
        },
        selectGoal: function() {
            this.model.toggle();
            this.$('.js-select-event').toggleClass('primary', this.model.get('selected'));
            this.$('.js-select-event').toggleClass('neutral', !this.model.get('selected'));
        }
    });

    AddEvents.EventsListView = Marionette.CompositeView.extend({
        template: '#eventsSelectListViewTemplate',
        childView: AddEvents.EventsListItemView,
        childViewContainer: '.js-events-list',
        initialize: function() {
            var that = this;
            this.collection.on('sync', function() {
                that.$('.js-no-events-label').toggleClass('hidden', that.collection.length !== 0);
                that.$('.js-events-list').toggleClass('hidden', that.collection.length === 0);
            })
        }
    });

    AddEvents.EventsSelectLayoutView = Marionette.LayoutView.extend({
        template: '#eventsSelectLayoutViewTemplate',
        regions: {
            'toolbar': '#eventsListToolbar',
            'list': '#eventsList',
            'paginator': '#eventsListPaginator',
            'paginatorShowing': '#eventsListPaginatorShowing'
        },
        initialize: function(options) {
            var that = this;
            this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

            this.collection = new curriculumManager.Entities.EventsCollection();
            this.collection.on('eventsCollection:updated', function (details) {
                that.updatePagination(details);
            });

            this.calendars = new curriculumManager.Entities.CalendarsCollection();

            this.filter = new valamisApp.Entities.Filter({
                'sort': 'starttime:true'
            });

            this.filter.on('change', function() {
                that.fetchCollection(true);
            });
        },
        onRender: function() {
            var listView = new AddEvents.EventsListView({
                collection: this.collection,
                paginatorModel: this.paginatorModel
            });
            this.list.show(listView);

            this.paginatorView = new ValamisPaginator({
                language: Valamis.language,
                model : this.paginatorModel,
                topEdgeParentView: this,
                topEdgeSelector: this.regions.toolbar,
                topEdgeOffset: 0
            });
            this.paginatorView.on('pageChanged', function () {
                this.fetchCollection()
            }, this);
            this.paginator.show(this.paginatorView);

            this.fetchCollection(true);

            var that = this;
            this.calendars.fetch({
                success: function() {
                    that.showToolbar();
                }
            });
        },
        showToolbar: function() {
            var toolbarView = new AddEvents.EventsToolbarView({
                model: this.filter,
                calendars: this.calendars
            });
            this.toolbar.show(toolbarView);

            var paginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: this.paginatorModel
            });
            this.paginatorShowing.show(paginatorShowingView);
        },
        updatePagination: function (details, context) {
            this.paginatorView.updateItems(details.total);
        },
        fetchCollection: function(firstPage) {
            if(firstPage) {
                this.paginatorModel.set('currentPage', 1);
            }

            this.collection.fetch({
                reset: true,
                filter: this.filter.toJSON(),
                currentPage: this.paginatorModel.get('currentPage'),
                itemsOnPage: this.paginatorModel.get('itemsOnPage')
            });
        },
        getSelectedEvents: function() {
            return this.collection.filter(function (item) {
                return item.get('selected');
            }).map(function (item) {
                return item.get('id');
            });
        }
    });

});