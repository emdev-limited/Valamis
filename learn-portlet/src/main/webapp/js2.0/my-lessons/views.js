myLessons.module('Views', function (Views, myLessons, Backbone, Marionette, $, _) {

  var COLLECTION_TYPE = {
    COMPLETED: 'completed',
    UNFINISHED: 'unfinished'
  };

  Views.LessonItemView = Marionette.ItemView.extend({
    className: 'tile',
    template: '#lessonItemViewTemplate',
    templateHelpers: function() {
      var colorClass = '';

      var gradeInt = parseInt(this.model.get('sortGrade')) || 0;

      if (gradeInt < 25)
        colorClass = 'red';
      else if (gradeInt >= 25 && gradeInt < 50)
        colorClass = 'yellow';
      else
        colorClass = 'green';

      var lessonGrade = gradeInt + '%';

      var lessonItemStatusLabel = (gradeInt) ? Valamis.language['inProgressLabel'] : Valamis.language['notStartedLabel'];

      return {
        lessonItemStatusLabel: lessonItemStatusLabel,
        colorClass: colorClass,
        lessonGrade: lessonGrade,
        completedLessons: this.options.completedLessons
      }
    }
  });

  Views.LessonsCollectionView = Marionette.CompositeView.extend({
    template: '#lessonsCollectionViewTemplate',
    childView: Views.LessonItemView,
    childViewContainer: '.js-list-view',
    events: {
      'click .js-show-more': 'takeLessons'
    },
    templateHelpers: function() {
      return {
        completedLessons: this.options.collectionType === COLLECTION_TYPE.COMPLETED
      }
    },
    childViewOptions: function() {
      return {
        completedLessons: this.options.collectionType === COLLECTION_TYPE.COMPLETED
      }
    },
    initialize: function() {
      this.page = 0;
      this.collectionType = this.options.collectionType;

      this.collection = new myLessons.Entities.LessonCollection();
      this.fetchedCollection = new myLessons.Entities.LessonCollection();

      this.fetchedCollection.on('lessonCollection:updated', function(details) {
        this.$('.js-no-lessons').toggleClass('hidden', details.total > 0);
        this.$('.js-show-more').toggleClass('hidden', this.page * details.count >= details.total);
      }, this);

      this.takeLessons();
    },
    takeLessons: function() {
      this.page++;

      var that = this;
      this.fetchedCollection.fetch({
        completed: this.collectionType === COLLECTION_TYPE.COMPLETED,
        page: this.page,
        success: function() {
          that.collection.add(that.fetchedCollection.toJSON());
        }
      });
    }
  });

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#lessonsLayoutTemplate',
    className: 'my-lessons',
    regions: {
      'unfinishedLessonsRegion': '#unfinishedLessons',
      'completedLessonsRegion': '#completedLessons'
    },
    onRender: function () {

      var unfinishedView = new Views.LessonsCollectionView({
        collectionType: COLLECTION_TYPE.UNFINISHED
      });
      this.unfinishedLessonsRegion.show(unfinishedView);

      var completedView = new Views.LessonsCollectionView({
        collectionType: COLLECTION_TYPE.COMPLETED
      });
      this.completedLessonsRegion.show(completedView);
    }
  });

});