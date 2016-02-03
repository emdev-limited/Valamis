myCertificates.module('Views', function (Views, myCertificates, Backbone, Marionette, $, _) {

  var ROW_TYPE = {
    DETAILS: 'details',
    CERTIFICATE: 'certificate'
  };

  var USERS_COUNT = 10;

  Views.UsersItemView = Marionette.CompositeView.extend({
    tagName: 'li',
    template: '#myCertificatesUsersItemViewTemplate',
    templateHelpers: function() {
      var totalGoals = this.model.get('statistic').total;
      var successProgress = (totalGoals) ? Math.floor(this.model.get('statistic').success * 100 / totalGoals) : 0;
      var failedProgress = (totalGoals) ? Math.floor(this.model.get('statistic').failed * 100 / totalGoals) : 0;
      var inProgress = (totalGoals) ? Math.floor(this.model.get('statistic').inProgress * 100 / totalGoals) : 0;

      return {
        isExpired: this.model.get('status') == 'Overdue',
        successProgress: successProgress + '%',
        failedProgress: failedProgress + '%',
        inProgress: inProgress + '%'
      }
    }
  });

  Views.RowItemView = Marionette.CompositeView.extend({
    tagName: 'tr',
    childView: Views.UsersItemView,
    childViewContainer: '.js-users-list',
    initialize: function () {
      if (this.model.get('tpe') === ROW_TYPE.CERTIFICATE)
        this.template = '#myCertificatesRowViewTemplate';
      else {
        this.template = '#myCertificatesDetailsViewTemplate';
        this.$el.addClass('hidden');
        this.collection = new myCertificates.Entities.UsersCollection();
      }
    },
    onRender: function () {
      if (this.model.get('tpe') === ROW_TYPE.DETAILS) {
        var fetchedCollection = new myCertificates.Entities.UsersCollection();

        fetchedCollection.on('sync', function () {
          this.collection.add(fetchedCollection.toJSON());
        }, this);

        this.$('.js-scroll-div').valamisInfiniteScroll(fetchedCollection, {
          count: USERS_COUNT,
          certificateId: this.model.get('certificateId')
        });
      }
    }
  });

  Views.AppLayoutView = Marionette.CompositeView.extend({
    template: '#myCertificatesLayoutTemplate',
    childView: Views.RowItemView,
    childViewContainer: '#certificatesTable',
    events: {
      'click .js-show-more': 'takeCertificates',
      'click .js-toggle-details': 'toggleDetails'
    },
    initialize: function() {
      this.page = 0;

      this.collection = new myCertificates.Entities.CertificateCollection();
      this.fetchedCollection = new myCertificates.Entities.CertificateCollection();

      this.fetchedCollection.on('certificateCollection:updated', function(details) {
        this.$('.js-certificates-table').toggleClass('hidden', details.total == 0);
        this.$('.js-no-items').toggleClass('hidden', details.total > 0);
        this.$('.js-show-more').toggleClass('hidden', this.page * details.count >= details.total);
      }, this);
    },
    onRender: function() {
      this.$('.valamis-tooltip').tooltip();
      this.takeCertificates();
    },
    takeCertificates: function() {
      this.page++;

      var that = this;
      this.fetchedCollection.fetch({
        page: this.page,
        success: function() {
          that.fetchedCollection.each(function(item) {
            that.collection.add(_.extend({tpe: ROW_TYPE.CERTIFICATE}, item.toJSON()));
            that.collection.add({tpe: ROW_TYPE.DETAILS, certificateId: item.get('id')});
          });

        }
      });
    },
    toggleDetails: function(e) {
      var targetTr = $(e.target).parents('tr');
      targetTr.toggleClass('open');
      var detailsTr = $(e.target).parents('tr').next('tr');
      detailsTr.toggleClass('hidden');
      this.setCanvas(detailsTr);
    },
    setCanvas: function (detailsTr) {
      var printCanvas = !detailsTr.hasClass('hidden') && detailsTr.find('#canvas-labels span').length == 0
        && detailsTr.find('ul.user-list > li').length > 0;

      if (printCanvas) {

        detailsTr.valamisCanvasBackground(
          detailsTr.find('ul.user-list > li').width(),
          detailsTr.find('.js-scroll-bounded').height()
        );

      }
    }
  });

});