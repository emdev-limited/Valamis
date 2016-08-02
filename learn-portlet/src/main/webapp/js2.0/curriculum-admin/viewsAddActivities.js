curriculumManager.module('Views.CertificateGoals.AddActivities', function (AddActivities, CurriculumManager, Backbone, Marionette, $, _) {

  // select activities

  AddActivities.ActivitySelectItemView = Marionette.ItemView.extend({
    template: '#curriculumManagerActivitySelectItemViewTemplate',
    tagName: 'tr',
    templateHelpers: function() {
      return {
        title: Valamis.language[this.model.get('activityId')]
      }
    },
    events: {
      'click .js-select-activity': 'selectActivity'
    },
    selectActivity: function() {
      this.model.toggle();
      this.$('.js-select-activity').toggleClass('primary', this.model.get('selected'));
      this.$('.js-select-activity').toggleClass('neutral', !this.model.get('selected'));
    }
  });

  AddActivities.ActivitySelectView = Marionette.CompositeView.extend({
    template: '#curriculumManagerActivitySelectViewTemplate',
    childView: AddActivities.ActivitySelectItemView,
    childViewContainer: '.js-activity-list',
    initialize: function() {
      this.collection = new curriculumManager.Entities.LiferayActivityCollection([
        { activityId: 'com.liferay.portlet.blogs.model.BlogsEntry' },
        { activityId: 'com.liferay.portlet.documentlibrary.model.DLFileEntry' },
        { activityId: 'com.liferay.portlet.wiki.model.WikiPage' },
        { activityId: 'com.liferay.portlet.messageboards.model.MBMessage' },
        { activityId: 'com.liferay.calendar.model.CalendarBooking' },
        { activityId: 'com.liferay.portlet.bookmarks.model.BookmarksEntry' },
        { activityId: 'participation' },
        { activityId: 'contribution' }
      ]);
    },
    getSelectedActivities: function() {
      return this.collection.filter(function (item) {
        return item.get('selected');
      }).map(function (item) {
        return item.get('activityId');
      });
    }
  });

});