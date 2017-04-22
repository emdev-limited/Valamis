curriculumManager.module('Views.CertificateGoals.AddStatements', function (AddStatements, CurriculumManager, Backbone, Marionette, $, _) {

  // select statements

  AddStatements.StatementsToolbarView = Marionette.ItemView.extend({
    template: '#curriculumManagerStatementsToolbarViewTemplate',
    behaviors: {
      ValamisUIControls: {}
    },
    events: {
      'keyup .js-search': 'changeSearchText',
      'click .js-sort-filter .dropdown-menu > li': 'changeSort',
      'click .js-select-all': 'selectAll'
    },
    initialize: function() {
      this.selectAllValue = false;
    },
    changeSearchText:function(e){
      var that = this;
      clearTimeout(this.inputTimeout);
      this.inputTimeout = setTimeout(function(){
        that.model.set('searchtext', $(e.target).val());
      }, curriculumManager.Entities.SEARCH_TIMEOUT);
    },
    changeSort: function(e){
      this.model.set('sort', $(e.target).attr('data-value'));
    },
    selectAll: function() {
      this.selectAllValue = !this.selectAllValue;
      this.triggerMethod('statements:select:all', this.selectAllValue);
    }
  });

  AddStatements.StatementsListItemView = Marionette.ItemView.extend({
    template: '#curriculumManagerStatementsListItemViewTemplate',
    tagName: 'tr',
    events: {
      'click .js-select-statement': 'selectGoal'
    },
    modelEvents: {
      'change:selected': 'toggleButton'
    },
    selectGoal: function() {
      this.model.toggle();
    },
    toggleButton: function() {
      this.$('.js-select-statement').toggleClass('primary', this.model.get('selected'));
      this.$('.js-select-statement').toggleClass('neutral', !this.model.get('selected'));
    }
  });

  AddStatements.StatementsListView = Marionette.CompositeView.extend({
    template: '#curriculumManagerStatementsListViewTemplate',
    childView: AddStatements.StatementsListItemView,
    childViewContainer: '.js-statements-list tbody',
    initialize: function() {
      var that = this;
      this.collection.on('sync', function() {
        that.$('.js-no-statements-label').toggleClass('hidden', that.collection.length !== 0);
        that.$('.js-statements-list').toggleClass('hidden', that.collection.length === 0);
      })
    }
  });

  AddStatements.StatementsSelectView = Marionette.LayoutView.extend({
    template: '#curriculumManagerStatementsSelectViewTemplate',
    regions: {
      'statementsToolbar': '#statementsListToolbar',
      'statementsList': '#statementsList',
      'statementsPaginator': '#statementsListPaginator',
      'statementsPaginatorShowing': '#statementsListPaginatorShowing'
    },
    childEvents: {
      'statements:select:all': function (childView, selectAllValue) {
        this.statementsCollection.each(function (item) {
          item.set('selected', selectAllValue);
        })
      }
    },
    initialize: function(options) {
      var that = this;
      this.paginatorModel = new PageModel({ 'itemsOnPage': 10 });

      this.statementsCollection = new curriculumManager.Entities.StatementsCollection();
      this.statementsCollection.on('statementsCollection:updated', function (details) {
        that.updatePagination(details);
      });

      this.statementsFilter = new valamisApp.Entities.Filter({
        'sort': 'creationDate:false'
      });
      this.statementsFilter.on('change', function() {
        that.fetchStatementsCollection(true);
      });

      this.fetchStatementsCollection(true);
    },
    onRender: function() {
      var statementsToolbarView = new AddStatements.StatementsToolbarView({
        model: this.statementsFilter
      });
      this.statementsToolbar.show(statementsToolbarView);

      var statementsListView = new AddStatements.StatementsListView({
        collection: this.statementsCollection,
        paginatorModel: this.paginatorModel
      });
      this.statementsList.show(statementsListView);

      this.statementsPaginatorView = new ValamisPaginator({
        language: Valamis.language,
        model : this.paginatorModel,
        topEdgeParentView: this,
        topEdgeSelector: this.regions.statementsToolbar,
        topEdgeOffset: 0
      });
      this.statementsPaginatorView.on('pageChanged', function () {
        this.fetchStatementsCollection()
      }, this);

      var statementsPaginatorShowingView = new ValamisPaginatorShowing({
        language: Valamis.language,
        model: this.paginatorModel
      });
      this.statementsPaginator.show(this.statementsPaginatorView);
      this.statementsPaginatorShowing.show(statementsPaginatorShowingView);
    },
    updatePagination: function (details, context) {
      this.statementsPaginatorView.updateItems(details.total);
    },
    fetchStatementsCollection: function(firstPage) {
      if(firstPage) {
        this.paginatorModel.set('currentPage', 1);
      }

      this.statementsCollection.fetch({
        reset: true,
        filter: this.statementsFilter.toJSON(),
        currentPage: this.paginatorModel.get('currentPage'),
        itemsOnPage: this.paginatorModel.get('itemsOnPage')
      });
    },
    getSelectedStatements: function() {
      return this.statementsCollection.filter(function (item) {
        return item.get('selected');
      });
    }
  });

  AddStatements.StatementAddItemView = Marionette.ItemView.extend({
    template: '#curriculumManagerStatementAddItemViewTemplate',
    tagName: 'tr',
    events: {
      'click .js-delete-statement': 'deleteStatement',
      'change .js-statement-verb': 'changeVerb',
      'keyup .js-statement-object': 'changeObject'
    },
    onRender: function() {
      this.$('.js-statement-verb').val(this.model.get('verb'));

      var that = this;
      var plugins = new curriculumManager.Entities.StatementPluginCollection();

      new AutoCompleteView({
        input: this.$('.js-statement-object'),
        model: plugins,
        queryParameter: 'activity',
        onSelect: function (model) {
          that.model.set({ obj: model.id });
          that.$('.js-statement-object').val(model.id);
        }
      }).render();
    },
    deleteStatement: function() {
      this.model.destroy();
    },
    changeVerb: function() {
      this.model.set({ verb: this.$('.js-statement-verb').val() });
    },
    changeObject: function() {
      var obj = this.$('.js-statement-object').val();
      this.model.set({ obj: obj });
    }
  });

  AddStatements.StatementsAddView = Marionette.CompositeView.extend({
    template: '#curriculumManagerStatementsAddViewTemplate',
    childView: AddStatements.StatementAddItemView,
    childViewContainer: '.js-statements-list',
    events: {
      'click .js-new-statement': 'newStatement',
      'click .js-select-statements': 'selectStatements'
    },
    initialize: function() {
      this.collection = new curriculumManager.Entities.StatementsCollection({});
    },
    onShow: function() {
      this.$('.js-statement-object').focus();
    },
    newStatement: function() {
      var newModel = new curriculumManager.Entities.StatementModel();
      this.collection.add(newModel);
    },
    selectStatements: function() {
      var that = this;
      var selectStatementsView = new AddStatements.StatementsSelectView();

      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['selectTincanStatementsLabel'],
        contentView: selectStatementsView,
        submit: function() {
          var selectedStatements = selectStatementsView.getSelectedStatements();
          that.collection.reset();
          that.collection.add(selectedStatements);
        }
      });

      valamisApp.execute('modal:show', modalView);
    },
    validate: function() {
      var isValid = true;
      this.collection.each(function(item) {
        if (item.get('obj') == '') {
          valamisApp.execute('notify', 'warning', Valamis.language['emptyObjectErrorLabel']);
          isValid = false;
        }
      });
      return isValid;
    }
  });

});