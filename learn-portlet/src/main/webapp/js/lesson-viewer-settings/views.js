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
        changeDefault: function (e) {
            var checked = $(e.target).is(':checked');
            this.model.set('isDefault', checked);

            this.triggerMethod('lesson:default:changed', this.model.get('id'));
            this.model.setDefault();
        },
        changeVisibility: function (e) {
            var hidden = $(e.target).is(':checked');
            this.model.set('isHidden', hidden);
            this.model.setVisibility();
        },
        deleteItem: function () {
            this.model.destroy();
        },
        onIsDefaultChange: function () {
            this.$('input.js-is-default').attr('checked', this.model.get('isDefault'));
        }
    });

    Views.LessonsListView = Marionette.CompositeView.extend({
        template: '#lessonViewerSettingsListTemplate',
        childView: Views.LessonsListItemView,
        childViewContainer: '.js-list-view',
        ui: {
            'categories': '.js-player-categories'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        events: {
            'click .js-add-lesson': 'addLesson'
        },
        childEvents: {
            'lesson:default:changed': function (childView, modelId) {
                this.collection.each(function (item) {
                    if (item.get('id') != modelId)
                        item.set({isDefault: false});
                })
            }
        },
        addLesson: function () {
            var selectLessonView = new valamisApp.Views.SelectLesson.LessonsSelectLayoutView({
                scope: 'instance',
                playerId: lessonViewerSettings.playerId,
                action: 'ALL_AVAILABLE_FOR_PLAYER'
            });

            var that = this;
            var selectLessonModalView = new valamisApp.Views.ModalView({
                header: Valamis.language['selectLessonsLabel'],
                contentView: selectLessonView,
                submit: function () {
                    var selected = selectLessonView.getSelectedLessons();
                    that.collection.addToPlayer({}, {lessonsIds: selected}).then(function () {
                        that.collection.fetch();
                    });
                }
            });

            valamisApp.execute('modal:show', selectLessonModalView);
        },
        onValamisControlsInit: function () {
            var that = this;
            this.categories = new Valamis.TagCollection();
            this.categories.fetch({reset: true}).then(function (allCategories) {
                that.categories.getLessonsSettingsCategories({}, {playerId: lessonViewerSettings.playerId})
                    .then(function (categories) {
                            that.fillCategories(allCategories, categories);
                        }
                    );
            });
        },
        fillCategories: function (allCategories, categories) {
            var that = this;

            var cIds = categories.map(function(model) {
                return model.id
            });

            var selectize = this.ui.categories.selectize({
                delimiter: ';',
                persist: false,
                valueField: 'id',
                options: allCategories,
                create: false,
                onChange: function(value) {
                    var categoriesIds = [];
                    if (!!value) {
                        categoriesIds =  value.split(';');
                    }
                    that.collection.updateCategories({}, {categoriesIds: categoriesIds})
                }
            });

            selectize[0].selectize.setValue(cIds, true);
        }
    });

});