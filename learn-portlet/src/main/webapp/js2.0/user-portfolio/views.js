userPortfolio.module('Views', function(Views, UserPortfolio, Backbone, Marionette, $, _) {

  Views.UserInfoView = Marionette.ItemView.extend({
    tagName: 'table',
    className: 'val-table big list no-border no-out-margin',
    template: '#userPortfolioUserInfoViewTemplate',
    modelEvents: {
      'change': 'render'
    },
    initialize: function() {
      this.model.fetch();
    }
  });

  Views.GoalsItemView = Marionette.ItemView.extend({
    tagName: 'li',
    childViewContainer: '.js-items',
    initialize: function() {
      this.collection = this.model.get('collection');

      this.template = (this.model.get('isGroup'))
        ? '#userPortfolioGroupGoalsItemViewTemplate'
        : '#userPortfolioGoalsItemViewTemplate';
    }
  });

  Views.GoalsCollectionView = Marionette.CompositeView.extend({
    template: '#userPortfolioGoalsCollectionViewTemplate',
    childView: Views.GoalsItemView,
    childViewContainer: '.js-certificate-goals',
    onRender: function() {
      this.$('.js-no-goals-label').toggleClass('hidden', this.collection.length > 0);
      this.$('.js-certificate-goals-header').toggleClass('hidden', this.collection.length == 0);
    }
  });

  Views.CertificatesItemView = Marionette.ItemView.extend({
    className: 'tile s-12 m-4 l-2',
    template: '#userPortfolioCertificatesItemViewTemplate',
    templateHelpers: function() {
      return {
        isLocal: !(this.model.get('id') < 0)
      }
    },
    events: {
      'click': 'openDetailsModal'
    },
    openDetailsModal: function() {
      if (this.model.get('id') > 0) {  // certificates from Mozilla OpenBadges have negative ids
        var that = this;
        this.goalsCollection = new userPortfolio.Entities.GoalsCollection(
          [],
          {certificateId: this.model.get('id')}
        );
        this.goalsCollection.on('sync', function () {
          this.model.getUserGoalsStatuses().then(function (statuses) {
            that.goalsCollection.setUserStatuses(statuses);
            that.showGoalsCollection();
          });
        }, this);
        this.goalsCollection.fetch();
      }
    },
    showGoalsCollection: function() {
      var contentView = new Views.GoalsCollectionView({ collection: this.goalsCollection });
      var modalView = new valamisApp.Views.ModalView({
        header: Valamis.language['goalsLabel'],
        contentView: contentView
      });
      valamisApp.execute('modal:show', modalView);
    }
  });

  Views.CertificatesCollectionView = Marionette.CollectionView.extend({
    childView: Views.CertificatesItemView,
    className: 'val-row tiles'
  });

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#userPortfolioLayoutTemplate',
    regions: {
      'userInfo': '#userInfo',
      'userCertificates': '#userCertificates'
    },
    onRender: function() {
      var userModel = new userPortfolio.Entities.UserModel();
      var userInfoView = new Views.UserInfoView({ model: userModel });
      this.userInfo.show(userInfoView);

      var certificatesCollection = new userPortfolio.Entities.CertificateCollection();
      var certificatesView = new Views.CertificatesCollectionView({ collection: certificatesCollection });
      certificatesView.on('render:collection', function(view) {
        valamisApp.execute('update:tile:sizes', view.$el);
      });
      this.userCertificates.show(certificatesView);
      certificatesCollection.fetch({ reset: true });
    }
  });

});