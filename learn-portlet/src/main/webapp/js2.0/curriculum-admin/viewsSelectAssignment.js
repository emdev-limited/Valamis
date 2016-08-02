valamisApp.module('Views.SelectAssignment', function (SelectAssignment, valamisApp, Backbone, Marionette, $, _) {

    var SEARCH_TIMEOUT = 800;

    SelectAssignment.AssignmentsToolbarView = Marionette.ItemView.extend({
        template: '#assignmentSelectToolbarViewTemplate',
        behaviors: {
            ValamisUIControls: {}
        },
        events: {
            'keyup .js-search': 'changeSearchText',
            'click .js-sort-filter .dropdown-menu > li': 'changeSort'
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
        }
    });

    SelectAssignment.AssignmentsListItemView = Marionette.ItemView.extend({
        template: '#assignmentSelectListItemViewTemplate',
        tagName: 'tr',
        events: {
            'click .js-select-assignment': 'selectGoal'
        },
        selectGoal: function() {
            this.model.toggle();
            this.$('.js-select-assignment').toggleClass('primary', this.model.get('selected'));
            this.$('.js-select-assignment').toggleClass('neutral', !this.model.get('selected'));
        }
    });

    SelectAssignment.AssignmentsListView = Marionette.CompositeView.extend({
        template: '#assignmentSelectListViewTemplate',
        childView: SelectAssignment.AssignmentsListItemView,
        childViewContainer: '.js-assignments-list',
        initialize: function() {
            var that = this;
            this.collection.on('sync', function() {
                that.$('.js-no-assignments-label').toggleClass('hidden', that.collection.length !== 0);
                that.$('.js-assignments-list').toggleClass('hidden', that.collection.length === 0);
            })
        }
    });

    SelectAssignment.AssignmentsSelectLayoutView = Marionette.LayoutView.extend({
        template: '#assignmentSelectLayoutViewTemplate',
        regions: {
            'assignmentsToolbar': '#assignmentsListToolbar',
            'assignmentsList': '#assignmentsList',
            'assignmentsPaginator': '#assignmentsListPaginator',
            'assignmentsPaginatorShowing': '#assignmentsListPaginatorShowing'
        },
        initialize: function(options) {
            var that = this;
            this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

            this.assignmentsCollection = new valamisApp.Entities.AssignmentCollection();
            this.assignmentsCollection.on('assignmentCollection:updated', function (details) {
                that.updatePagination(details);
            });

            this.assignmentsFilter = new valamisApp.Entities.Filter({
                'sort': 'title:true'
            });

            this.assignmentsFilter.on('change', function() {
                that.fetchAssignmentsCollection(true);
            });
        },
        onRender: function() {
            var assignmentsListView = new SelectAssignment.AssignmentsListView({
                collection: this.assignmentsCollection,
                paginatorModel: this.paginatorModel
            });
            this.assignmentsList.show(assignmentsListView);

            this.assignmentsPaginatorView = new ValamisPaginator({
                language: Valamis.language,
                model : this.paginatorModel
            });
            this.assignmentsPaginatorView.on('pageChanged', function () {
                this.fetchAssignmentsCollection()
            }, this);
            this.assignmentsPaginator.show(this.assignmentsPaginatorView);

            this.fetchAssignmentsCollection(true);

            this.showToolbar();
        },
        showToolbar: function() {
            var assignmentsToolbarView = new SelectAssignment.AssignmentsToolbarView({
                model: this.assignmentsFilter
            });
            this.assignmentsToolbar.show(assignmentsToolbarView);

            var assignmentsPaginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: this.paginatorModel
            });
            this.assignmentsPaginatorShowing.show(assignmentsPaginatorShowingView);
        },
        updatePagination: function (details, context) {
            this.assignmentsPaginatorView.updateItems(details.total);
        },
        fetchAssignmentsCollection: function(firstPage) {
            if(firstPage) {
                this.paginatorModel.set('currentPage', 1);
            }

            this.assignmentsCollection.fetch({
                reset: true,
                filter: this.assignmentsFilter.toJSON(),
                currentPage: this.paginatorModel.get('currentPage'),
                itemsOnPage: this.paginatorModel.get('itemsOnPage'),
                status: 'Published'
            });
        },
        getSelectedAssignments: function() {
            return this.assignmentsCollection.filter(function (item) {
                return item.get('selected');
            }).map(function (item) {
                return item.get('id');
            });
        }
    });

});