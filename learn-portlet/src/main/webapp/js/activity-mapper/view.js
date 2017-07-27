ActivityMapperApp.module("ViewModule", function(ViewModule, ActivityMapperApp, Backbone, Marionette, $, _){
  var language = {};
  var siteID = 0;

  ViewModule.ActivityMapperRowView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#activitySocialMapperRow',
    templateHelpers: function() {
      return _.extend(language, {activityName: this.model.get('title')});
    },
    events: {
      'change .js-mapped-activity-verb': 'updateEntry'
    },
    updateEntry: function() {
      this.model.set('mappedVerb', this.$('.js-mapped-activity-verb').val());
      this.model.persist(siteID);
    },
    onRender: function() {
      this.$('.js-mapped-activity-verb').val(this.model.get('mappedVerb'))
    }
  });

  ViewModule.ActivityMapperRowCollectionView = Marionette.CollectionView.extend({
    tagName: 'tbody',
    childView: ViewModule.ActivityMapperRowView
  });

  ViewModule.addInitializer(function(options) {
    if (options) {
      language = options.language;
      siteID = options.siteID;
    }
  })
});
