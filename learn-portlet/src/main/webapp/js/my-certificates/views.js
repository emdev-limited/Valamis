myCertificates.module('Views', function (Views, myCertificates, Backbone, Marionette, $, _) {

  var ROW_TYPE = myCertificates.Entities.ROW_TYPE;

  var STATUSES = {
    undef: 'Undefined',
    inprogress: 'InProgress',
    success: 'Success',
    failed: 'Failed',
    // path status
    expired: 'Expired'
  };

  Views.UsersItemView = Marionette.CompositeView.extend({
    tagName: 'li',
    template: '#myCertificatesUsersItemViewTemplate',
    templateHelpers: function () {
      var totalGoals = 0;

      var successProgress = 0, inProgress = 0, failedProgress = 0;
      if (this.model.get('status') == STATUSES.success) {
        successProgress = 100;
        inProgress = failedProgress = 0;
      } else {
        var goalStatuses = this.model.get('statusToCount') || {};

        var inProgressCount = goalStatuses[STATUSES.inprogress] || 0 + goalStatuses[STATUSES.undef];
        var successCount = goalStatuses[STATUSES.success] || 0;
        var failedCount = goalStatuses[STATUSES.failed] || 0;
        $.each(goalStatuses, function (k, v) {
          totalGoals += v;
        });

        successProgress = (totalGoals) ? Math.floor(successCount * 100 / totalGoals) : 0;
        failedProgress = (totalGoals) ? Math.floor(failedCount * 100 / totalGoals) : 0;
        inProgress = (totalGoals) ? Math.floor(inProgressCount * 100 / totalGoals) : 0;
      }

      return {
        isExpired: this.model.get('status') == STATUSES.expired,
        successProgress: successProgress + '%',
        failedProgress: failedProgress + '%',
        inProgress: inProgress + '%',
        successCount: successCount,
        totalGoals: totalGoals
      }
    }
  });

  Views.RowItemView = Marionette.CompositeView.extend({
    tagName: 'tr',
    childView: Views.UsersItemView,
    childViewContainer: '.js-users-list',
    initialize: function () {
      this.isDetails = this.model.get('tpe') === ROW_TYPE.DETAILS;

      if (!this.isDetails)
        this.template = '#myCertificatesRowViewTemplate';
      else {
        this.template = '#myCertificatesDetailsViewTemplate';
        this.$el.addClass('hidden details');
        this.collection = new myCertificates.Entities.UsersCollection([], {useSkipTake: true});
      }
    },
    templateHelpers: function () {
      var values = {};

      if (!this.isDetails) {
        var userStatuses = this.model.get('statusToCount') || {};

        var successCount = userStatuses[STATUSES.success] || 0;
        var expiredCount = userStatuses[STATUSES.expired] || 0;
        var totalUsers = 0;
        $.each(userStatuses, function (k, v) {
          totalUsers += v;
        });

        values.totalUsers = totalUsers;
        values.successUsers = successCount;
        values.overdueUsers = expiredCount;
        values.url = Utils.getCertificateUrl(this.model.get('id'));
      }

      return values;
    },
    onRender: function () {
      if (this.isDetails) {
        var that = this;
        this.$('.js-scroll-div').valamisInfiniteScroll(this.collection, function () {
          that.collection.fetchMore({pathId: that.model.get('pathId')});
        });
      }
    }
  });

  Views.AppLayoutView = Marionette.CompositeView.extend({
    template: '#myCertificatesLayoutTemplate',
    childView: Views.RowItemView,
    childViewContainer: '#certificatesTable',
    ui: {
      pathsTable: '.js-items-table',
      showMore: '.js-show-more',
      toggleDetails: '.js-toggle-details',
      noItems: '.js-no-items'
    },
    events: {
      'click @ui.showMore': 'fetchMore',
      'click @ui.toggleDetails': 'toggleDetails'
    },
    onRender: function () {
      this.$('.valamis-tooltip').tooltip();

      this.collection = new myCertificates.Entities.PathsCollection([], {
        useSkipTake: true,
        itemsPerPage: 5
      });
      this.collection.on('sync', this.checkUi, this);
      this.collection.fetchMore();
    },
    fetchMore: function () {
      this.collection.fetchMore();
    },
    checkUi: function () {
      this.ui.pathsTable.toggleClass('hidden', !this.collection.hasItems());
      this.ui.noItems.toggleClass('hidden', this.collection.hasItems());
      this.ui.showMore.toggleClass('hidden', !this.collection.hasMore());
    },
    toggleDetails: function (e) {
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