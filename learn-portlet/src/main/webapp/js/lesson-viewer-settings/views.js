lessonViewerSettings.module('Views', function (Views, lessonViewerSettings, Backbone, Marionette, $, _) {

  Views.LessonsListItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#lessonViewerSettingsListItemTemplate',
    events: {
      'change .js-is-default': 'changeDefault',
      'change .js-is-hidden': 'changeVisibility',
      'click .js-delete-item': 'deleteItem'
    },
    modelEvents: {
      'change:isDefault': 'onIsDefaultChange'
    },
    changeDefault: function(e) {
      var checked = $(e.target).is(':checked');
      this.model.set('isDefault', checked);

      this.triggerMethod('lesson:default:changed', this.model.get('id'));
      this.model.setDefault();
    },
    changeVisibility: function(e) {
      var hidden = $(e.target).is(':checked');
      this.model.set('isHidden', hidden);
      this.model.setVisibility();
    },
    deleteItem: function() {
      this.model.destroy();
    },
    onIsDefaultChange: function() {
      this.$('input.js-is-default').attr('checked', this.model.get('isDefault'));
    }
  });

  Views.LessonsListView = Marionette.CompositeView.extend({
    template: '#lessonViewerSettingsListTemplate',
    childView: Views.LessonsListItemView,
    childViewContainer: '.js-list-view',
    events: {
      'click .js-add-lesson': 'addLesson'
    },
    childEvents: {
      'lesson:default:changed': function(childView, modelId){
        this.collection.each(function(item) {
          if (item.get('id') != modelId)
            item.set({ isDefault: false });
        })
      }
    },
    addLesson: function() {
      var selectLessonView = new valamisApp.Views.SelectLesson.LessonsSelectLayoutView({
        scope: 'instance',
        playerId: lessonViewerSettings.playerId,
        action: 'ALL_AVAILABLE_FOR_PLAYER'
      });

      var that = this;
      var selectLessonModalView = new valamisApp.Views.ModalView({
        header: Valamis.language['selectLessonsLabel'],
        contentView: selectLessonView,
        submit: function() {
          var selected = selectLessonView.getSelectedLessons();
          that.collection.addToPlayer({}, { lessonsIds: selected }).then(function() {
            that.collection.fetch();
          });
        }
      });

      valamisApp.execute('modal:show', selectLessonModalView);
    }
  });

});