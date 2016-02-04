SelectStatementCollectionService = new Backbone.Service({ url: path.root,
    sync: {
        'read': {
            'path': path.api.statements,
            'data': function (e, options) {
                var sortBy = options.order.split(':')[0];
                var asc = options.order.split(':')[1];
                return {
                    courseId: Utils.getCourseId(),
                    page: options.currentPage,
                    count: options.itemsOnPage,
                    filter: options.filter,
                    sortAscDirection: asc,
                    sortBy: sortBy
                }
            },
            'method': 'get'
        }
    }
});

SelectStatementCollection = Backbone.Collection.extend({
    model: StatementModel,
    parse: function (response) {
        this.trigger('statementCollection:updated', { total: response.total, currentPage: response.currentPage});
        return response.records;
    }
}).extend(SelectStatementCollectionService);

var CertificateSelectStatementsRowView = Backbone.View.extend({
    events: {
        "click .js-toggle-button": "toggleThis"
    },
    tagName: 'tr',
    initialize: function (options) {
        this.options = options;
        this.model.on('change', this.render, this);
        this.model.set('verbName',Utils.getLangDictionaryTincanValue(this.model.get('verbName')));
        this.model.set('objName',Utils.getLangDictionaryTincanValue(this.model.get('objName')));
    },
    render: function () {
        var template = Mustache.to_html(jQuery('#selectStatementsRowView').html(), this.model.toJSON());
        this.$el.html(template);
        return this;
    },
    toggleThis: function () {
        this.model.toggle();
        this.trigger('unsetIsSelectedAll');
    }
});

var CertificateSelectStatementsDialogView = Backbone.View.extend({
    SEARCH_TIMEOUT: 800,
    events: {
        'click #addStatementsButton': 'addStatements',
        'keyup #statementsSearch': 'filterStatements',
        'click #sortStatements li': 'filterStatements',
        'click #selectAllStatements': 'selectAllStatements'
    },
    initialize: function (options) {
        this.options = options;
        this.language = options.language;
        this.isSelectedAll = false;
        this.collection = new SelectStatementCollection();
        var that = this;
        this.collection.on('statementCollection:updated', function (details) {
            that.updatePagination(details, that);
        });
        this.collection.on('reset', this.renderElements, this);
        this.paginatorModel = new PageModel();
        this.paginatorModel.set({'itemsOnPage': 10});
    },
    filterStatements: function () {
        clearTimeout(this.inputTimeout);
        this.inputTimeout = setTimeout(this.applyFilter.bind(this), this.SEARCH_TIMEOUT);
    },
    applyFilter: function () {
        clearTimeout(this.inputTimeout);
        this.reloadFirstPage();
    },

    render: function () {
        var template = Mustache.to_html(jQuery('#selectStatementsDialogView').html(), this.language);
        this.$el.html(template);
        this.$('.dropdown').valamisDropDown();
        this.$('.js-search')
            .on('focus', function() {
                jQuery(this).parent('.val-search').addClass('focus');
            })
            .on('blur', function() {
                jQuery(this).parent('.val-search').removeClass('focus');
            });

        var that = this;
        this.paginator = new ValamisPaginator({
          el: this.$el.find("#statementListPaginator"),
          language: this.language,
          model: this.paginatorModel
        });
        this.paginator.on('pageChanged', function () {
          that.reload();
        });

        this.paginatorShowing = new ValamisPaginatorShowing({
          el: this.$el.find("#statementListPagingShowing"),
          language: this.language,
          model: this.paginator.model
        });

        this.reloadFirstPage();
        return this;
    },
    selectAllStatements: function () {
        this.isSelectedAll = !this.isSelectedAll;
        var that = this;
        this.collection.each(function (item) {
          if (item.get('selected') != that.isSelectedAll)
            item.toggle();
        });
    },
    unsetIsSelectedAll: function () {
      this.isSelectedAll = false;
    },
    renderElements: function () {
        if (this.collection.length > 0) {
          this.$('#noStatementsLabel').hide();
          this.collection.each(function (item) {
            var row = new CertificateSelectStatementsRowView({model: item});
            row.on('unsetIsSelectedAll', this.unsetIsSelectedAll, this);
            this.$('#statementsList').append(row.render().$el);
          }, this);
        } else {
          this.$('#noStatementsLabel').show();
        }
    },
    updatePagination: function (details, context) {
       this.paginator.updateItems(details.total);
    },

    fetchCollection: function () {
        this.$('#statementsList').empty();
        this.collection.fetch({
          reset: true,
          currentPage: this.paginator.currentPage(),
          itemsOnPage: this.paginator.itemsOnPage(),
          filter: this.$('#statementsSearch').val(),
          order: this.$('#sortStatements').data('value')
        });
    },
    reloadFirstPage: function () {
        this.paginatorModel.set({'currentPage': 1});
        this.fetchCollection();
    },
    reload: function () {
        this.fetchCollection();
    },
    addStatements: function () {
      var selectedStatements = this.collection.filter(function (item) {
        return item.get('selected');
      });
      this.options.parentWindow.trigger('addStatements', selectedStatements);
      this.trigger('closeModal', this);
    }
});