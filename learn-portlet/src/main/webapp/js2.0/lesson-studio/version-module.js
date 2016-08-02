var versionModule = slidesApp.module('VersionModule', function(VersionModule, slidesApp, Backbone, Marionette, $, _){
    VersionModule.startWithParent = false;

    VersionModule.VersionItemView = Marionette.ItemView.extend({
        className: 'version-item js-show-version',
        template: '#versionItemTemplate',
        templateHelpers: function() {
            return {
                isDraft: this.isDraft,
                modifiedDate: this.customDate
            }
        },
        events: {
            'click .version-title': 'showVersion',
            'click .js-delete-version': 'deleteVersion',
            'click .js-clone-version': 'cloneVersion'
        },
        modelEvents: {
            'change:isActive': 'makeActive'
        },
        initialize: function() {
            this.isDraft = this.model.get('status') === 'draft';
            var customDateArr = new Date(Date.parse(this.model.get('modifiedDate')));
            customDateArr = customDateArr.toString().split(' ');
            customDateArr.splice(0, 1);
            customDateArr.splice(2, 1, 'to');
            customDateArr.splice(4, 2);
            customDateArr[3] = customDateArr[3].split('', 5).join('');
            this.customDate = customDateArr.join(' ');
        },
        deleteVersion: function(){
            var that = this;
            valamisApp.execute('delete:confirm', { message: Valamis.language['warningDeleteSlidesetMessageLabel'] }, function(){
                that.model.destroy().then(
                    function() {
                        valamisApp.execute('notify', 'success', Valamis.language['lessonSuccessfullyDeletedLabel']);
                    },
                    function() {
                        valamisApp.execute('notify', 'error', Valamis.language['lessonFailedToDeleteLabel']);
                    }
                );
            });
        },
        cloneVersion: function() {
            this.model.clone().then(function(responce){
                valamisApp.execute('notify', 'success', responce.title + ' ' + Valamis.language['lessonClonedVersionLabel']);
            });
        },
        showVersion: function(e) {
            this.triggerMethod('change:active:version', this.model.get('id'));
        },

        makeActive: function() {
            var isActive = !!this.model.get('isActive');
            this.$el.toggleClass('active', isActive);

            if (isActive) {
                var that = this;
                var slidesCollection = new lessonStudio.Entities.LessonPageCollection();
                slidesApp.initializing = true;
                slidesCollection.fetch({
                    slideSetId: this.model.get('id'),
                    success: function () {
                        versionModule.view.activeVersionModel = that.model;
                        versionModule.view.activeVersionCollection = slidesCollection;
                        versionModule.view.activeVersionElementsCollection = new lessonStudio.Entities.LessonPageElementCollection();
                        versionModule.view.activeVersionModel.set({'slides': slidesCollection});

                        if (slidesApp.replaceModel) {
                            slidesApp.slideSetModel = that.model;
                            slidesApp.slideCollection = slidesCollection;
                            slidesApp.replaceModel = false;
                        }

                        versionModule.view.activeVersionCollection.each(function(model){
                            if(model.get('slideElements').length > 0){
                                versionModule.view.activeVersionElementsCollection.add(model.get('slideElements'));
                            }
                        });

                        slidesApp.RevealModule.renderSlideset({
                            slideSetModel: versionModule.view.activeVersionModel,
                            slideCollection: versionModule.view.activeVersionCollection,
                            slideElementCollection: versionModule.view.activeVersionElementsCollection
                        });
                        _.defer(function(){
                            slidesApp.initializing = false;
                        });

                    },
                    error: function () {
                        _.defer(function(){
                            slidesApp.initializing = false;
                        });
                    }
                });
            }
        }

    });

    VersionModule.VersionView = Marionette.CollectionView.extend({
        childView: VersionModule.VersionItemView,
        childEvents: {
            'change:active:version': function(childView, modelId) {
                this.changeActive(modelId);
            }
        },
        initialize: function() {
            var that = this;
            that.collection.reset();//???
            that.collection.add(slidesApp.slideSetModel);//???

            slidesApp.slideSetModel.getAllVersions().then(function (data) {
                that.collection.add(data);
                that.collection.comparator = function(model) {
                    return -model.get('version');
                };
                that.collection.sort();
                that.collection.trigger('versionsCollection:updated');
            });

            this.collection.on('remove', function() {
                slidesApp.replaceModel = true;
                this.changeActive();
            }, this);
        },
        changeActive: function(modelId) {
            var activeModelId = modelId || this.collection.at(0).get('id');
            this.collection.each(function(model) {
                model.set('isActive', model.get('id') == activeModelId);
            });
        },
        cleanActive: function() {
            this.collection.each(function(model) {
                model.set('isActive', false);
            });
        }
    });
});

versionModule.on('start', function() {
    versionModule.view = new versionModule.VersionView({
        collection: slidesApp.versionsCollection
    });
    slidesApp.versionArea.show(versionModule.view);

});