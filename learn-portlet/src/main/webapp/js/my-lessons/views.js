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

      var requiredReview = this.model.get('lesson').requiredReview;
      var teacherGradeObject = this.model.get('teacherGrade');
      var teacherGrade = (teacherGradeObject) ? teacherGradeObject.grade : undefined;
      var autoGrade = this.model.get('autoGrade');
      var state = this.model.get('state');

      var grade = (requiredReview)
        ? teacherGrade
        : (!!teacherGrade) ? teacherGrade : autoGrade;

      var gradeInt = parseInt(grade * 100) || 0;

      if (gradeInt < 25)
        colorClass = 'failed';
      else if (gradeInt >= 25 && gradeInt < 50)
        colorClass = 'inprogress';
      else
        colorClass = 'success';

      var isSuccess = (gradeInt === 100);

      var lessonGrade = gradeInt + '%';

      var lessonItemStatusLabel = (state)
          ? Valamis.language[state.name + 'Label']
          : Valamis.language['notStartedLabel'];

      var statusClass = (state && state.name == 'inReview') ? 'inprogress' : '';

      return {
        lessonItemStatusLabel: lessonItemStatusLabel,
        colorClass: colorClass,
        lessonGrade: lessonGrade,
        completedLessons: this.options.completedLessons,
        isSuccess: isSuccess,
        title: this.model.get('lesson').title,
        description: this.model.get('lesson').description,
        statusClass: statusClass
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