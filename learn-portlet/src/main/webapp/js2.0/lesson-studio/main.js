var slidesApp = new Backbone.Marionette.Application({container: '#revealEditor', type: 'editor'});

function showSlideSet(slideSetModel) {
    var deferred = jQueryValamis.Deferred();
    if(!lessonStudio.googleClientApiReady)
        lessonStudio.googleClientAPILoadTryCount++;
    if(!lessonStudio.youtubeIframeAPIReady)
        lessonStudio.youtubeIframeAPILoadTryCount++;

    if(lessonStudio.googleClientApiReady && lessonStudio.youtubeIframeAPIReady) {
        if(lessonStudio.googleClientAPILoadTryCount > 10)
            lessonStudio.googleClientApiReady = false;
        if(lessonStudio.youtubeIframeAPILoadTryCount > 10)
            lessonStudio.youtubeIframeAPIReady = false;
        lessonStudio.googleAPIsLoadTryCount = 0;
        slidesApp.slideSetModel = slideSetModel;
        slidesApp.mode = 'edit';
        slidesApp.slideCollection = new lessonStudio.Entities.LessonPageCollection();
        slidesApp.tempSlideIds = [];
        slidesApp.slideTemplateCollection = new lessonStudio.Entities.LessonPageCollection(null, {isTemplates: true});
        window.collection = Backbone.Collection.extend({});
        slidesApp.slideElementCollection = new lessonStudio.Entities.LessonPageElementCollection();
        slidesApp.newSlideId = -1;
        slidesApp.newSlideElementId = -1;
        slidesApp.categories = [];
        slidesApp.questions = [];
        slidesApp.questionCollection = new window.collection();
        slidesApp.slideCollection.on('sync', function () {

            if (!slidesApp.isRunning && slidesApp.slideCollection && slidesApp.slideElementCollection) {
                jQueryValamis.when(revealModule.start()).then(function() {
                    deferred.resolve();
                });

                jQueryValamis('.js-lesson-title').html(
                    slideSetModel.get('title').length < 100
                        ? slideSetModel.get('title')
                        : slideSetModel.get('title').substr(0, 96) + ' ...'
                );

                if(slideSetModel.get('isTemplate')) {
                    slidesApp.switchMode('preview', false);
                    jQueryValamis('.js-mode-switcher').hide();
                    jQueryValamis('.js-undo').hide();
                    jQueryValamis('.js-editor-save-container').hide();
                    jQueryValamis('.js-lesson-title').append(' (' + Valamis.language['templatePreviewLabel'] + ')');
                } else {
                    jQueryValamis('.js-mode-switcher').show();
                    jQueryValamis('.js-undo').show();
                    jQueryValamis('.js-editor-save-container').show();
                }

                slidesApp.topbar.currentView.$childViewContainer.show();
            }
        });

        slidesApp.themeModel = new lessonStudio.Entities.LessonPageThemeModel;

        var themeId = slideSetModel.get('themeId');
        if (themeId) {
            slidesApp.themeModel.id = themeId;
            slidesApp.themeModel.fetch();
        }

        slidesApp.slideCollection.fetch({ slideSetId: slideSetModel.id });
        slidesApp.slideTemplateCollection.fetch({ model: new lessonStudio.Entities.LessonPageModel(slideSetModel).set('id', 0), isTemplate: true });
    }
    else {
        if(lessonStudio.youtubeIframeAPILoadTryCount <= 10 && lessonStudio.googleClientAPILoadTryCount <= 10)
            setTimeout(function() {
                showSlideSet(slideSetModel);
            }, 500);
        else {
            if(!lessonStudio.youtubeIframeAPIReady) {
                valamisApp.execute('notify', 'warning', Valamis.language['youtubeAPILoadingFailedLabel']);
                lessonStudio.youtubeIframeAPIReady = true;
            }
            if(!lessonStudio.googleClientApiReady) {
                valamisApp.execute('notify', 'warning', Valamis.language['googleClientAPILoadingFailedLabel']);
                lessonStudio.googleClientApiReady = true;
            }
            showSlideSet(slideSetModel);
        }
    }
    return deferred.promise();
}

slidesApp.restart = function(settings) {
    var deferred = jQueryValamis.Deferred();
    var slideSetModel = slidesApp.slideSetModel;
    slidesApp.execute('app:stop');
    showSlideSet(slideSetModel).then(function() {
        slidesApp.slideElementCollection.onSync();
        if(settings.indices)
            Reveal.slide(settings.indices.h, settings.indices.v);
        deferred.resolve(settings);
    });
    return deferred.promise();
};

slidesApp.saveSlideset = function(options) {
    options || (options = {});
    slidesApp.execute('item:blur', slidesApp.activeSlideModel.id);
    slidesApp.deferred = jQueryValamis.Deferred();
    if(!slidesApp.isSaved) {
        var totalSlideCount = 0;
        var totalSlideElementCount = 0;
        var i = 0;

        function saveElement(oldSlideModel, slideElementModel) {
            var deferred = jQueryValamis.Deferred();
            var slideModelId = oldSlideModel.get('id');
            var slideModelTempId = oldSlideModel.get('tempId');
            if(slidesApp.slideElementCollection.where({ slideId: slideModelTempId }).length +
                slidesApp.slideElementCollection.where({ slideId: slideModelId }).length == 0) {
                deferred.resolve();
            }
            if (slideElementModel) {
                if (slideElementModel.get('slideId') === slideModelTempId || slideElementModel.get('slideId') === slideModelId) {
                    if (slideElementModel.get('slideId') === slideModelTempId)
                        slideElementModel.set('slideId', slideModelId);

                    var formData = slideElementModel.get('formData');
                    var fileModel = slideElementModel.get('fileModel');
                    if(formData){
                        if (slideElementModel.get('mediaGalleryTitle'))
                            slideElementModel.set('content', slideElementModel.get('mediaGalleryTitle'));
                        else
                            slideElementModel.set('content', formData.itemModel.get('filename'));
                    }
                    else if(fileModel) {
                        var fileExt = getExtByMime(fileModel.get('mimeType'));
                        var fileName = fileExt ? fileModel.get('title') + '.' + fileExt : fileModel.get('title');
                        slideElementModel.set('content', fileName);
                    }
                    slideElementModel.setLayoutProperties();

                    var saveSubmit = function(){
                        slideElementModel.save().then(function (newSlideElementModel) {
                            slideElementModel.set('id', newSlideElementModel.id);
                            var registeredModelView =
                                Marionette.ItemView.Registry.getByModelId(slideElementModel.get('tempId')) ||
                                Marionette.ItemView.Registry.getByModelId(slideElementModel.get('id'));

                            if(formData) {
                                slidesApp
                                    .uploadImage(slideElementModel, registeredModelView.behaviors.ImageUpload.getFolderId, formData)
                                    .always(onElementSaved);
                            }
                            else if(fileModel)
                                registeredModelView.trigger('mediagallery:image:upload',
                                    fileModel,
                                    function() { onElementSaved(); }
                                );
                            else onElementSaved();

                            function onElementSaved() {
                                slideElementModel
                                    .unset('formData')
                                    .unset('fileModel')
                                    .unset('fileUrl')
                                    .unset('mediaGalleryTitle');

                                i++;
                                totalSlideElementCount++;
                                if (i == slidesApp.slideElementCollection.where({ slideId: slideModelTempId }).length +
                                    slidesApp.slideElementCollection.where({ slideId: slideModelId }).length)

                                    deferred.resolve();
                            }
                        });
                    };

                    //TODO: need refactoring
                    if(!slideElementModel.get('id') && slideElementModel.get('clonedId')){
                        slideElementModel.clone().then(function(newModelAttributes) {
                            slideElementModel.set('id', newModelAttributes.id);
                            saveSubmit();
                        });
                    } else {
                        saveSubmit();
                    }

                }
            }
            return deferred.promise();
        }

        function saveSlide(slideModel) {
            var deferred = jQueryValamis.Deferred();
            if(slideModel) {
                var slideModelTempId = slideModel.get('tempId');
                var index = slidesApp.tempSlideIds.indexOf(slideModel.id);
                if(index >= 0)
                    slidesApp.tempSlideIds.splice(index, 1);

                var slideDOMElement = jQueryValamis('section#slide_' + (slideModelTempId || slideModel.get('id')));

                if (!slideModel.get('title'))
                    slideModel.set('title', 'Page');

                var formData = slideModel.get('formData');
                var fileModel = slideModel.get('fileModel');
                if(formData && !slideModel.get('bgImageChange') && formData.itemModel)
                    slideModel.set('bgImage', formData.itemModel.get('filename') + ' ' + slideModel.getBackgroundSize());
                else if(fileModel) {
                    var fileExt = getExtByMime(fileModel.get('mimeType'));
                    var correctFileName = fileModel.get('title').replace(/\s/g,'_');
                    var fileName = fileExt ? correctFileName + '.' + fileExt : correctFileName;
                    slideModel.set('bgImage', fileName + ' ' + slideModel.getBackgroundSize());
                }

                slideModel.save({ isTemplate: false }).then(function (model) {
                    slideModel.set('id', model.id);
                    var slideModelId = slideModel.get('id');

                    if (slidesApp.slideSetModel.get('themeId') && slideModel.get('bgImageChange')){
                        var slideOptions = {
                            id: slideModelId,
                            themeId: slidesApp.slideSetModel.get('themeId')
                        };
                        jQueryValamis.when(slideModel.copyFileFromTheme({}, slideOptions)).always(onSlideModelSaved);
                    }
                    else onSlideModelSaved();

                    function onSlideModelSaved() {
                        if(slideModelTempId) {
                            _.each(slidesApp.slideCollection.where({leftSlideId: slideModelTempId}), function (slide) {
                                slide.set('leftSlideId', model.id);
                            });
                            _.each(slidesApp.slideCollection.where({topSlideId: slideModelTempId}), function (slide) {
                                slide.set('topSlideId', model.id);
                            });
                        }

                        var registeredSlideIndices = slidesApp.slideRegistry
                            .getBySlideId(slideModelTempId || slideModelId);
                        slidesApp.slideRegistry
                            .update(slideModelTempId, slideModelId, registeredSlideIndices);

                        i = 0;
                        if(slidesApp.addedSlides.indexOf(slideModelTempId) != -1) {
                            delete slidesApp.addedSlides[slideModelTempId];
                            if(slidesApp.addedSlides.indexOf(slideModelId) == -1) {
                                slidesApp.addedSlides.push(slideModelId);
                            }
                        }
                        var slideIsLeftFor = slidesApp.slideCollection.where({ leftSlideId: slideModelId });
                        var slideIsTopFor = slidesApp.slideCollection.where({ topSlideId: slideModelId });

                        slideDOMElement.attr('id', 'slide_' + slideModelId);

                        // Update linked slide ids
                        var elementsWithCorrectLinkedSlides =
                            slidesApp.slideElementCollection.where({ correctLinkedSlideId: slideModelId })
                                .concat(slidesApp.slideElementCollection.where({ correctLinkedSlideId: slideModelTempId }));

                        var elementsWithIncorrectLinkedSlides =
                            slidesApp.slideElementCollection.where({ incorrectLinkedSlideId: slideModelId })
                                .concat(slidesApp.slideElementCollection.where({ incorrectLinkedSlideId:slideModelTempId }));
                        _.each(elementsWithCorrectLinkedSlides, function(slideElementModel) {
                            slideElementModel.set('correctLinkedSlideId', slideModelId);
                            slideElementModel.save();
                        });
                        _.each(elementsWithIncorrectLinkedSlides, function(slideElementModel) {
                            slideElementModel.set('incorrectLinkedSlideId', slideModelId);
                            slideElementModel.save();
                        });

                        if(slidesApp.slideSetModel.get('themeId') && slideModel.get('bgImageChange')) {
                            saveSlideElements();
                        }
                        // Save background image if no theme is applied to the lesson
                        else {
                            if(formData) {
                                slidesApp
                                    .uploadImage(slideModel, revealControlsModule.view.behaviors.ImageUpload.getFolderId, formData)
                                    .always(saveSlideElements);
                            }
                            else {
                                var fileModel = slideModel.get('fileModel');
                                if(fileModel){
                                    if (fileModel.get('title').indexOf(' ') > -1){
                                        var correctFileName = fileModel.get('title').replace(/\s/g,'_');
                                        fileModel.set('title', correctFileName);
                                    }
                                    revealControlsModule.view.model.set('id', model.id);
                                    revealControlsModule.view.trigger('mediagallery:image:upload',
                                        fileModel,
                                        function() { saveSlideElements(); }
                                    );
                                }
                                else saveSlideElements();
                            }
                        }

                        function saveSlideElements() {
                            slideModel
                                .unset('slideId')
                                .unset('bgImageChange', { silent: true })
                                .unset('formData')
                                .unset('fileModel')
                                .unset('fileUrl');

                            var slideElements = _.filter(
                                slidesApp.slideElementCollection.where({ slideId: slideModelTempId })
                                    .concat(slidesApp.slideElementCollection.where({ slideId: slideModelId })), function(model) {
                                    return !model.get('toBeRemoved');
                                });
                            if(slideElements.length == 0)
                                jQueryValamis.when(saveRelatedSlides(slideModel, slideIsLeftFor, slideIsTopFor)).then(function() {
                                    totalSlideCount++;
                                    deferred.resolve();
                                });
                            else {
                                for (var j in slideElements) {
                                    jQueryValamis.when(saveElement(slideModel, slideElements[j])).then(function () {
                                        if (j == slideElements.length - 1) {
                                            jQueryValamis.when(saveRelatedSlides(slideModel, slideIsLeftFor, slideIsTopFor)).then(function () {
                                                deferred.resolve();
                                            });
                                        }
                                        if (totalSlideCount == slidesApp.slideCollection.size() && totalSlideElementCount == slidesApp.slideElementCollection.size()) {
                                            deferred.resolve(slidesApp.slideSetModel);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
            return deferred.promise();
        }

        function saveRelatedSlides(newSlideModel, slideIsLeftFor, slideIsTopFor) {
            var deferred = jQueryValamis.Deferred();
            if(slideIsLeftFor.length > 0) {
                for (var j in slideIsLeftFor) {
                    slideIsLeftFor[j].set('leftSlideId', newSlideModel.id);
                    jQueryValamis.when(saveSlide(slideIsLeftFor[j])).then(function () {
                        if(slideIsTopFor.length > 0) {
                            for (var k in slideIsTopFor) {
                                slideIsTopFor[k].set('topSlideId', newSlideModel.id);
                                jQueryValamis.when(saveSlide(slideIsTopFor[k])).then(function() {
                                    if(k == slideIsTopFor.length - 1)
                                        deferred.resolve();
                                });
                            }
                        }
                        else {
                            if(j == slideIsLeftFor.length - 1)
                                deferred.resolve();
                        }
                    });
                }
            }
            else if(slideIsTopFor.length > 0) {
                for (var j in slideIsTopFor) {
                    slideIsTopFor[j].set('topSlideId', newSlideModel.id);
                    jQueryValamis.when(saveSlide(slideIsTopFor[j])).then(function() {
                        if(j == slideIsTopFor.length - 1)
                            deferred.resolve();
                    });
                }
            }
            else
                deferred.resolve();

            return deferred.promise();
        }

        function destroyRemovedModels(type) {
            var deferred = jQueryValamis.Deferred();
            var collection = type === 'slide' ? slidesApp.slideCollection : slidesApp.slideElementCollection;
            var models = _.clone(collection.models);
            var collectionSize = collection.size();
            if(collectionSize > 0) {
                _.each(models, function (model, index) {
                    function resolveIfLastModel() {
                        if (index == collectionSize - 1) {
                            deferred.resolve();
                        }
                    }

                    if (model.get('toBeRemoved')) {
                        var registeredItemView =
                            Marionette.ItemView.Registry.getByModelId(model.get('tempId')) ||
                            Marionette.ItemView.Registry.getByModelId(model.get('id'));

                        if (registeredItemView)
                            Marionette.ItemView.Registry.remove(registeredItemView.cid);

                        if (model.isNew()) {
                            collection.remove(model);
                            resolveIfLastModel();
                        }
                        else {
                            var i = slidesApp.tempSlideIds.indexOf(model.id);
                            if(i > -1) slidesApp.tempSlideIds.splice(i, 1);
                            model.destroy().then(function () {
                                resolveIfLastModel();
                            });
                        }
                    }
                    else resolveIfLastModel();
                });
            } else
                deferred.resolve();

            return deferred.promise();
        }

        // Blur all CKEDITOR instances
        for (var i in CKEDITOR.instances) {
            CKEDITOR.instances[i].focusManager.blur();
        }

        var rootSlideModel = slidesApp.slideCollection.findWhere({ leftSlideId: undefined, topSlideId: undefined, toBeRemoved: false });
        rootSlideModel.unset('leftSlideId');
        rootSlideModel.unset('topSlideId');
        jQueryValamis.when.apply(jQueryValamis, [ destroyRemovedModels('slide'), destroyRemovedModels('slideElement') ]).then(function(slideModels, slideElementModels) {
            jQueryValamis.when(saveSlide(rootSlideModel)).then(
                function() {
                    slidesApp.slideSetModel.save().then( function (slideSetModel){
                            slidesApp.deferred.resolve(slideSetModel, options);
                    });
                },
                function() {
                    slidesApp.deferred.reject(options);
                }
            );
        });
    }
    else
        slidesApp.deferred.resolve(options);

    return slidesApp.deferred.promise();
};

slidesApp.toggleSavedState = function(set_saved) {
    if(!slidesApp.initializing && slidesApp.topbar.currentView) {
        if( typeof set_saved == 'undefined' ){
            set_saved = !slidesApp.isSaved;
        }
        var topBar = slidesApp.topbar.currentView,
            unsavedLabel = topBar.$('.js-presentation-unsaved');
        if (set_saved) {
            topBar.ui.button_save.hide();
            topBar.ui.button_disabled_saved.show();
            unsavedLabel.hide();
            slidesApp.isSaved = true;
        }
        else {
            topBar.ui.button_save.show();
            topBar.ui.button_disabled_saved.hide();
            unsavedLabel.show();
            slidesApp.isSaved = false;
        }
    }
};

slidesApp.togglePreviewMode = function(newMode) {
    var isToggleToPreview = newMode != 'edit';
    if( newMode == 'edit' && !slidesApp.isEditorReady ){
        _.each(Marionette.ItemView.Registry.items, function(item) {
            item.$el.removeClass('unactive');
            item.goToSlideActionDestroy();
            item.delegateEvents();
            if(item.model.get('slideEntityType') === 'webgl') {
                item.$el.unbind('mouseover mouseout');
            }
        });
        slidesApp.initDnD();
        slidesApp.getRegion('editorArea').$el.removeAttr('style');
        jQueryValamis('div[id^="slideEntity_"] .video-js').removeAttr('controls');
        slidesApp.isEditorReady = true;
    }
    else if( newMode == 'preview' && slidesApp.isEditorReady ) {
        _.each(Marionette.ItemView.Registry.items, function(item) {
            item.$el.addClass('unactive');
            item.undelegateEvents();
            item.goToSlideActionInit();
            if(item.model.get('slideEntityType') === 'webgl') {
                item.$el.unbind('mouseover').bind('mouseover', function() {
                    item.trackballControls.handleResize();
                    item.trackballControls.enabled = true;
                });
                item.$el.unbind('mouseout').bind('mouseout', function() {
                    item.trackballControls.enabled = false;
                });
            }
        });
        jQueryValamis(document).unbind('keydown');
        jQueryValamis('.reveal-wrapper').css('left', 0);
        jQueryValamis('div[id^="slideEntity_"] .video-js').attr('controls', 'controls');
        slidesApp.isEditorReady = false;
    }
    slidesApp.execute('item:blur');

    if( newMode != 'arrange' ){
        slidesApp.getRegion('sidebar').$el.toggle(!isToggleToPreview);
    }
    slidesApp.getRegion('revealControls').$el.toggle(!isToggleToPreview);
    jQueryValamis('div[id^="slideEntity_"] .video-js').toggleClass('no-pointer-events', !isToggleToPreview);
    jQueryValamis('div[id^="slideEntity_"] iframe').toggleClass('no-pointer-events', !isToggleToPreview);
    jQueryValamis('.js-slides-editor-topbar .js-editor-save-container').toggle(newMode!='preview');
    jQueryValamis('.js-slides-editor-topbar .js-undo').toggle(!isToggleToPreview);
    slidesApp.topbar.currentView.ui.button_change_theme.toggle(!isToggleToPreview);
    slidesApp.topbar.currentView.ui.button_change_settings.toggle(!isToggleToPreview);

    jQueryValamis('.slides-work-area-wrapper').attr('data-mode', newMode);

    placeSlideControls(jQueryValamis(window.parent).width(), jQueryValamis(window.parent).height());
};

slidesApp.switchMode = function(mode, visualOnly, slideId) {
    slidesApp.execute('item:blur');
    var btn = jQueryValamis('.js-editor-' + mode.replace(':select', '') + '-mode');
    btn.siblings().removeClass('slides-primary');
    btn.siblings().removeClass('active');
    btn.siblings().addClass('slides-dark');
    btn.addClass('active');
    btn.addClass('slides-primary');
    btn.removeClass('slides-dark');

    slidesApp.togglePreviewMode(mode);

    if(!visualOnly) {
        switch (mode) {
            case 'arrange':
                arrangeModule.start();
                slidesApp.topbar.currentView.$childViewContainer.hide();
                break;
            case 'preview':
                if (slidesApp.mode === 'arrange') {
                    valamisApp.execute('notify', 'info', Valamis.language['lessonModeSwitchingLabel'], { 'timeOut': '0', 'extendedTimeOut': '0' });
                    setTimeout(function() {
                        revealModule.stop();
                        slidesApp.isRunning = true;
                        revealModule.start();
                        arrangeModule.stop();
                        jQueryValamis('#arrangeContainer').empty();
                        if (slideId) {
                            var slideIndices = slidesApp.slideRegistry.getBySlideId(slideId);
                            Reveal.slide(slideIndices.h, slideIndices.v);
                        }
                    }, 0);
                    slidesApp.topbar.currentView.$childViewContainer.show();
                }
                else {
                    revealModule.configure({ backgroundTransition: 'none', transition: 'slide' });
                }
                break;
            case 'edit':
                var entitySlideId = slidesApp.selectedItemView
                    ? slidesApp.selectedItemView.model.get('slideId')
                    : null;
                if (slidesApp.mode != 'preview'){
                    valamisApp.execute('notify', 'info', Valamis.language['lessonModeSwitchingLabel'], { 'timeOut': '0', 'extendedTimeOut': '0' });
                    setTimeout(function() {
                        revealModule.stop();
                        slidesApp.isRunning = true;
                        revealModule.start();
                        arrangeModule.stop();
                        var nextSlideId = slideId ? slideId : entitySlideId;
                        if ( nextSlideId ) {
                            var slideIndices = slidesApp.slideRegistry.getBySlideId(nextSlideId);
                            Reveal.slide(slideIndices.h, slideIndices.v);
                            if( entitySlideId && nextSlideId == entitySlideId ){
                                var selectedEntityId = slidesApp.selectedItemView.model.id || slidesApp.selectedItemView.model.get('tempId'),
                                    currentEntity = slidesApp.getSlideElementModel(selectedEntityId),
                                    selectedView = Marionette.ItemView.Registry.getByModelId(currentEntity.get('id') || currentEntity.get('tempId'));
                                selectedView.selectEl();
                            }
                        }
                    }, 0);
                }
                revealModule.configure({ backgroundTransition: 'none', transition: 'none' });
                slidesApp.topbar.currentView.$childViewContainer.show();
                break;
        }
    }
    if(revealModule.view){
        revealModule.view.updateSlidesContainer();
    }
    setTimeout(function() {
        slidesApp.mode = mode;
    }, 200);
};

slidesApp.activeElement = {
    model: null,
    view: null,
    moduleName: '',
    offsetX: 0,
    offsetY: 0,
    startX: 0,
    startY: 0,
    isMoving: false,
    isResizing: false
};

Marionette.ItemView.Registry = {
    items: {},
    register: function (id, object) {
        this.items[id] = object;
    },
    getByViewId: function (id) {
        return _.first(_.filter(this.items, function(item) {
            return item.cid === id;
        }));
    },
    getByModelId: function (id) {
        var intId = parseInt(id);
        for(var view in this.items) {
            var model = this.items[view].model;
            var modelId = intId < 0 ? model.get('tempId') : model.get('id');
            if(modelId == intId) {
                return this.items[view];
                break;
            }
        }
    },
    remove: function (id) {
        delete this.items[id];
    },
    update: function (oldId, newId, object) {
        if(!object) object = this.items[oldId];
        if(object){
            this.remove(oldId);
            this.register(newId, object);
        }
    },
    size: function() {
        return Object.keys(this.items).length;
    }
};

slidesApp.slideRegistry = {
    items: {},
    register: function (id, indices) {
        this.items[id] = indices;
    },
    getBySlideId: function (id) {
        return this.items[id] || null;
    },
    getByModelId: function (id) {
        var intId = parseInt(id);
        for(var view in this.items) {
            var model = this.items[view].model;
            var modelId = intId < 0 ? model.get('tempId') : model.get('id');
            if(modelId == intId) {
                return this.items[view];
                break;
            }
        }
    },
    remove: function (id) {
        delete this.items[id];
    },
    update: function (oldId, newId, indices) {
        this.remove(oldId);
        this.register(newId, indices);
    },
    size: function() {
        return Object.keys(this.items).length;
    }
};

slidesApp.getSlideModel = function (id) {
    var intId = parseInt(id);
    return (intId > 0)
        ? slidesApp.slideCollection.get(intId)
        : slidesApp.slideCollection.findWhere({tempId: intId});
};

slidesApp.getSlideElementModel = function (id) {
    var intId = parseInt(id);
    return (intId > 0)
        ? slidesApp.slideElementCollection.get(intId)
        : slidesApp.slideElementCollection.findWhere({tempId: intId});
};

slidesApp.getFileUrl = function(model, filename) {
    var url = filename || '';
    if(url && url.indexOf('/') == -1) {
        if(model.get('slideSetId')){//is slide model
            var modelId = model.get('id') || model.get('slideId'),
                folderPrefix = 'slide_';
        } else {
            var modelId = model.get('id') || model.get('clonedId'),
                folderPrefix = model.get('isTheme')
                    ? 'slide_theme_'
                    : 'slide_item_';
        }
        url = model.get('slideEntityType') === 'pdf'
            ? path.root + path.portlet.prefix + 'preview-resources/pdf/web/viewer.html?file=/learn-portlet/SCORMData/files/slideData' +
        modelId + '/' + filename
            : path.root + path.api.files + 'images?folderId=' + folderPrefix + modelId + '&file=' + filename;
    }
    return url;
};

slidesApp.uploadImage = function(model, folderIdFunction, formData) {
    var deferred = jQueryValamis.Deferred();
    if(model && formData && typeof folderIdFunction === 'function') {
        var folderId = folderIdFunction(model);
        var mediaGallery = model.get('mediaGalleryTitle');
        var endpointparam = {
            action: 'ADD',
            courseId: Utils.getCourseId(),
            contentType: (mediaGallery) ? 'document-library' :'icon',
            folderId: folderId
        };
        var uploaderUrl = path.root + path.api.files + "?" + jQueryValamis.param(endpointparam);
        if(formData instanceof FormData){
            jQueryValamis.ajax({
                url: uploaderUrl,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                headers: {
                    'X-CSRF-Token': Liferay.authToken
                },
                success: function() { deferred.resolve(); },
                error: function() { deferred.reject(); }
            });
        }
        else {
            formData.url = uploaderUrl;
            if( formData.jqXHR && formData.jqXHR.state() === 'pending' ){
                delete formData.jqXHR;
            }
            formData.submit()
                .done(function() { deferred.resolve(); })
                .fail(function() { deferred.reject(); });
        }
    }
    else deferred.reject();

    return deferred.promise();
};

slidesApp.initDnD = function() {
    jQueryValamis(slidesApp.container).mousemove(function(e) {
        if( slidesApp.mode != 'edit' ) return;
        if (slidesApp.activeElement.isMoving) {
            e.preventDefault();
            // Prevent creating elements when the cursor is still over the sidebar
            if(e.clientX > slidesApp.sidebar.$el.width()) {
                if (!slidesApp.activeElement.view)
                    slidesApp.execute('item:create', true);

                var scrollTop = jQueryValamis(document).scrollTop(),
                    offset = jQueryValamis('.slides').offset(),
                    posSideTop = e.clientY - (offset.top - scrollTop) - slidesApp.activeElement.offsetY,
                    posSideLeft = e.clientX - offset.left - slidesApp.activeElement.offsetX;

                if(!slidesApp.activeElement.view.model.get('classHidden')){
                    posSideTop = slidesApp.GridSnapModule.getPosSideTop(posSideTop);
                    posSideLeft = slidesApp.GridSnapModule.getPosSideLeft(posSideLeft);
                }

                slidesApp.activeElement.view.model.set('top', posSideTop);
                slidesApp.activeElement.view.model.set('left', posSideLeft);

                slidesApp.activeElement.view.model.set(
                    'slideEntityType', slidesApp.activeElement.moduleName
                        .substr(0, slidesApp.activeElement.moduleName.toLowerCase().indexOf("element")).toLowerCase()
                );

                slidesApp.activeElement.view.updateControlsPosition();
            }
        }
    });

    jQueryValamis(slidesApp.container)
        .unbind('mousedown')
        .bind('mousedown',function(e) {
            if( slidesApp.mode != 'edit' ) return;
            //We click on button, not on icon sometimes
            if ((e.target.className.indexOf('val-icon-') == 0
                || (e.target.firstElementChild && e.target.firstElementChild.className.indexOf('val-icon-') == 0))) {

                e.preventDefault();
                var button = jQueryValamis(e.target).closest('button');
                if( !button.is('.js-change-slide-background') && revealControlsModule.pickerVisible ){
                    jQueryValamis('.js-change-slide-background').colpickHide();
                }
                return false;
            }

            if (jQueryValamis(e.target).closest('div.rj-element').length == 0
                && jQueryValamis(e.target).closest('div[id^="cke_editor"]').length == 0
                && jQueryValamis(e.target).closest('div[class*="val-modal"]').length == 0) {
                if (jQueryValamis(e.target).closest('.slide-popup-panel').length == 0) {
                    if (!slidesApp.activeElement.isMoving && !slidesApp.activeElement.isResizing && !revealControlsModule.pickerVisible) {
                        if (slidesApp.activeElement.view && slidesApp.activeElement.view.editor)
                            slidesApp.activeElement.view.destroyEditor();
                        slidesApp.execute('item:blur');
                    }
                }
                else
                    jQueryValamis('.js-change-slide-background').colpickHide();
            }
        });

    jQueryValamis(slidesApp.container).mouseup(function(e) {
        if(e.clientX <= slidesApp.sidebar.$el.width() && jQueryValamis(e.target).closest('.js-valamis-popup-panel').size() == 0)
            slidesApp.execute('item:blur');
        else {
            if (!slidesApp.activeElement.view || slidesApp.mode != 'edit') return;

            var scrollTop = jQueryValamis(document).scrollTop();

            if (!slidesApp.activeElement.isResizing) {
                e.stopPropagation();
                if (!(e.clientX > slidesApp.activeElement.view.$el.offset().left
                    && e.clientY > slidesApp.activeElement.view.$el.offset().top - scrollTop)
                    && jQueryValamis(e.target).closest('.item-controls').size() === 0
                    && jQueryValamis(e.target).closest('div[class*="val-modal"]').size() == 0
                    && !slidesApp.isEditing) {
                    slidesApp.activeElement.view = null;
                    slidesApp.activeElement.moduleName = null;
                }
                else {
                    if (slidesApp.oldValue)
                        slidesApp.newValue = {
                            'top': slidesApp.activeElement.view.model.get('top'),
                            'left': slidesApp.activeElement.view.model.get('left')
                        };
                    else {
                        slidesApp.viewId = slidesApp.activeElement.view.cid;
                        slidesApp.actionType = 'itemCreated';
                        slidesApp.oldValue = null;
                        slidesApp.newValue = {indices: Reveal.getIndices(), view: slidesApp.activeElement.view.cid};
                        var modelId = slidesApp.activeElement.view.model.get('id') || slidesApp.activeElement.view.model.get('tempId');
                        Marionette.ItemView.Registry.register(modelId, slidesApp.activeElement.view);
                    }
                }
            }

            if (slidesApp.activeElement.view && !slidesApp.isEditing) {
                if (slidesApp.oldValue && slidesApp.newValue && JSON.stringify(slidesApp.newValue) !== JSON.stringify(slidesApp.oldValue)) {
                    slidesApp.viewId = slidesApp.activeElement.view.cid;
                    if (slidesApp.viewId && slidesApp.newValue && slidesApp.actionType)
                        slidesApp.execute('action:push');
                }
            }

            if (slidesApp.activeElement && slidesApp.activeElement.isMoving) {
                slidesApp.activeElement.isMoving = false;

                slidesApp.getRegion('editorArea').$el
                    .css('overflow', '');//return default style
            }

            if( slidesApp.activeElement.view ){
                slidesApp.activeElement.view.updateControlsPosition();
            }

            slidesApp.GridSnapModule.removeLines();
        }
    });
};

slidesApp.onKeyPress = function(event){
    if(slidesApp.mode != 'edit' || slidesApp.isEditing) {
        return;
    }
    if(event.type == 'keydown'){
        if(event.keyCode == 17)//Ctrl
            slidesApp.ctrlPressed = true;
        if(event.keyCode == 16)//Shift
            slidesApp.shiftPressed = true;
    } else {
        if(event.keyCode == 17)//Ctrl
            slidesApp.ctrlPressed = false;
        if(event.keyCode == 16)//Shift
            slidesApp.shiftPressed = false;
        return;
    }
    if (slidesApp.activeElement.view) {
        // Del
        if (!slidesApp.ctrlPressed && !slidesApp.shiftPressed && event.keyCode == 46) {
            slidesApp.execute('item:delete', slidesApp.activeElement.view);
        }
        if (slidesApp.ctrlPressed) {
            // Ctrl+C
            if (event.keyCode == 67) {
                slidesApp.itemCopy = slidesApp.activeElement.view;
            }
            // Ctrl+V
            if (event.keyCode == 86) {
                slidesApp.execute('item:duplicate', slidesApp.itemCopy);
            }
        }
    }
    // Ctrl+Z
    if (event.keyCode == 90) {
        slidesApp.execute('action:undo');;
        slidesApp.execute('item:blur');
    }
    // Ctrl+S
    if (slidesApp.ctrlPressed && event.keyCode == 83) {
        event.preventDefault();
        lessonStudio.execute('save-slideset', {close: false});
    }
};

slidesApp.bindKeys = function() {
    jQueryValamis(document).bind('keydown keyup', slidesApp.onKeyPress);
};
slidesApp.unBindKeys = function() {
    jQueryValamis(document).unbind('keydown keyup', slidesApp.onKeyPress);
};

slidesApp.checkIsTemplate = function() {
    if(!slidesApp.activeSlideModel) return;
    var isLessonSummary = slidesApp.activeSlideModel.get('isLessonSummary');
    var slideId = slidesApp.activeSlideModel.get('id') || slidesApp.activeSlideModel.get('tempId');
    if (isLessonSummary){
        jQueryValamis('.js-hide-if-summary').hide();
        slidesApp.execute('item:blur', slideId);
    }
    else {
        jQueryValamis('.js-hide-if-summary').show();
    }
};

slidesApp.layoutResizeInit = function(){
    var workArea = jQueryValamis('.slides-work-area-wrapper');
    if( jQueryValamis('.layout-resizable-handle', workArea).size() > 0 ){
        return;
    }

    jQueryValamis('<div/>',{
        "class": "layout-resizable-handle"
    }).appendTo( workArea );

    var startAutoResize = function(e, start){
        if( typeof start == 'undefined' ){ start = true; }
        clearInterval(window.timer);
        if(!start || (e && e.type != 'mouseleave')) return;
        var wrapper = jQueryValamis('.slides-editor-main-wrapper');
        window.timer = setInterval(function(){
            wrapper.find('.slides-work-area-wrapper')
                .add(wrapper.find('.slides'))
                .css('height', '+=5');
            wrapper.find('.layout-resizable-handle')
                .css('top', '100%');
            wrapper
                .scrollTop(wrapper.get(0).scrollHeight);
        }, 10);
    };

    jQueryValamis('.layout-resizable-handle').draggable({
        axis: 'y',
        scroll: false,
        start: function(){
            var editorArea = slidesApp.getRegion('editorArea').$el,
                wrapper = editorArea.closest('.slides-editor-main-wrapper');
            revealControlsModule.view.ui.button_add_page_down.toggleClass('hidden', true);
            workArea.parent()
                .bind('mouseleave mouseenter', startAutoResize);
        },
        drag: function(event, ui){
            var minHeight = parseInt( workArea.css('min-height').replace(/\D/g,'') );
            if( ui.position.top < minHeight ){
                ui.position.top = minHeight;
            }
            workArea
                .add(workArea.find('.slides'))
                .css({
                    height: ui.position.top
                });
        },
        stop: function(event, ui){
            jQueryValamis( this ).css({top: '100%'});
            var oldHeight = slidesApp.activeSlideModel.get('height'),
                newHeight = Math.round(workArea.height());
            slidesApp.activeSlideModel.set('height', newHeight);
            revealControlsModule.view.ui.button_add_page_down.toggleClass('hidden', false);
            startAutoResize(null, false);
            workArea.parent()
                .unbind('mouseleave mouseenter', startAutoResize);
            slidesApp.toggleSavedState(false);

            slidesApp.viewId = null;
            slidesApp.actionType = 'changeModelAttribute';
            slidesApp.oldValue = {height: oldHeight};
            slidesApp.newValue = {height: newHeight};
            slidesApp.slideId = slidesApp.activeSlideModel.get('id') || slidesApp.activeSlideModel.get('tempId');
            slidesApp.execute('action:push');
        }
    });
};

slidesApp.copyImageFromGallery = function (model) {
    if (model.get('fileModel') && !model.get('formData')) {
        var data = model.get('fileModel');
        var fileExt = getExtByMime ? getExtByMime(data.get('mimeType')) : null;
        data.set({
            title: fileExt
                ? data.get('title') + '.' + fileExt
                : data.get('title')
        });
        var formData = new FormData();
        formData.append('contentType', 'document-library');
        formData.append('fileEntryID', data.get('id'));
        formData.append('file', data.get('title'));
        formData.append('fileVersion', data.get('version'));
        formData.append('p_auth', Liferay.authToken);
        model
            .unset('fileModel')
            .set('formData', formData)
            .set('mediaGalleryTitle', data.get('title'))
    }
};

slidesApp.on('start', function(options){

    CKEDITOR.disableAutoInline = true;

    slidesApp.addRegions({
        sidebar: '.sidebar',
        topbar: '.slides-editor-topbar',
        editorArea: '.reveal-wrapper',
        revealControls: '.reveal-controls',
        arrangeArea: '#arrangeContainer',
        modals: {
            selector: '#slides-modals-layout',
            regionClass: Backbone.Marionette.Modals
        }
    });
    sidebarModule.start();

    slidesApp.layoutResizeInit();

    var topbarView = new TopbarView({
        collection: slidesApp.devicesCollection
    });
    slidesApp.topbar.show(topbarView);

    slidesApp.actionStack = [];
    slidesApp.isSaved = true;
    slidesApp.savedIndex = 0;
    slidesApp.initialized = true;
    slidesApp.isEditorReady = true;
    slidesApp.initializing = true;
    initGAPISettings();

    slidesApp.getRegion('editorArea').$el
        .closest('.slides-editor-main-wrapper')
        .bind('scroll', function(){
            if( slidesApp.activeElement && slidesApp.activeElement.view ){
                slidesApp.activeElement.view.updateControlsPosition();
            } else {
                slidesApp.execute('item:blur');
            }
        });

});

function isEditorEnabled() {
    for (var i in CKEDITOR.instances) {
        if (jQueryValamis('.cke_editor_' + i).is(':visible')) {
            return true;
        }
    }
    return false;
}

var TopbarView = Marionette.CompositeView.extend({
    template: '#topbarTemplate',
    childView: lessonStudio.Views.DeviceItemView.extend({
        template: '#deviceItemButtonTemplate'
    }),
    childViewContainer: '.js-buttons-select-layout',
    className: 'val-row',
    templateHelpers: function(){
        return {
            contextPath: path.root + path.portlet.prefix
        }
    },
    ui: {
        'button_change_theme': '.js-change-theme',
        'button_change_settings': '.js-change-settings',
        'buttons_select_layout': '.js-buttons-select-layout .button',
        'button_save': '.js-slides-editor-save',
        'button_disabled_saved': '.js-slides-editor-changes-saved'
    },
    events: {
        'click .js-mode-switcher > .button-group > .slides-dark': 'switchEditorMode',
        'click .js-undo': 'triggerUndoAction',
        'click @ui.button_save': 'saveSlidesetWithoutClosing',
        'click .js-close-slideset': 'returnToOverview',
        'click @ui.buttons_select_layout': 'switchLayout',
        'click @ui.button_change_theme': function(e) {
            if (!this.ui.button_change_theme.hasClass('highlight')) {
                e.preventDefault();
                this.ui.button_change_theme.addClass('highlight');
                slidesApp.execute('controls:theme:change');
            }
        },
        'click @ui.button_change_settings':  function(e) {
            if (!this.ui.button_change_settings.hasClass('highlight')) {
                e.preventDefault();
                this.ui.button_change_settings.addClass('highlight');
                slidesApp.execute('controls:settings:change');
            }
        }
    },
    onRender: function() {
        var view = this;

        jQueryValamis('.js-slides-editor-topbar').tooltip(
            {
                selector: '.valamis-tooltip',
                container: '.js-slides-editor-topbar',
                placement: 'bottom',
                trigger: 'hover'
            }
        );

        jQueryValamis(window).on('unload', function () {
            view.closeEditor()
        })
    },
    onRenderCollection: function(){
        this.bindUIElements();
    },
    onShow: function() {
        slidesApp.toggleSavedState(true);
    },
    switchEditorMode: function (e) {
        var btn = jQueryValamis(e.target).closest('button');
        if(btn.hasClass('js-editor-edit-mode'))
            window.editorMode = 'edit';
        else if(btn.hasClass('js-editor-arrange-mode'))
            window.editorMode = 'arrange';
        else if(btn.hasClass('js-editor-preview-mode'))
            window.editorMode = 'preview';
        slidesApp.switchMode(window.editorMode);
    },
    returnToOverview: function() {
        if(!slidesApp.isSaved) {
            var that = this;
            valamisApp.execute('save:confirm', { title: Valamis.language['changesDetectedLabel'], message: Valamis.language['saveConfirmationTitle'] }, function(){
                that.triggerSaveSlideset({ close: true });
            },
            function(){
                that.closeEditor();
            });
        }
        else
            this.closeEditor();
    },
    closeEditor: function() {
        slidesApp.execute('temp:delete');
        slidesApp.execute('app:stop');
        lessonStudio.execute('editor:close');
        lessonStudio.execute('lessons:reload');
    },
    triggerUndoAction: function() {
        slidesApp.execute('action:undo');
    },
    saveSlidesetWithoutClosing: function() {
        this.triggerSaveSlideset({ close: false });
    },
    triggerSaveSlideset: function(options) {
        var view = this;
        valamisApp.execute('notify', 'info', Valamis.language['lessonIsSavingLabel']); //, { 'timeOut': '0', 'extendedTimeOut': '0' });
        view.ui.button_save.html(Valamis.language['lessonIsSavingButtonLabel']);
        jQueryValamis('body').css('pointer-events', 'none');
        jQueryValamis(slidesApp.container)
            .unbind('mousedown')
            .unbind('mouseup')
            .unbind('mousemove');

        slidesApp.saveSlideset(options).then(
            function() {
                valamisApp.execute('notify', 'clear');
                valamisApp.execute('notify', 'success', Valamis.language['lessonWasSavedSuccessfullyLabel']);

                view.ui.button_save.html(Valamis.language['saveLabel']);
                jQueryValamis('body').css('pointer-events', 'all');
                slidesApp.initDnD();

                slidesApp.slideCollection.each(function(slideModel) {
                    slideModel.unset('tempId');
                });
                slidesApp.slideElementCollection.each(function(slideElementModel) {
                    slideElementModel.unset('tempId');
                });

                slidesApp.savedIndex = slidesApp.actionStack.length;
                slidesApp.toggleSavedState();
                if (options.close) {
                    slidesApp.execute('temp:delete');
                    slidesApp.execute('app:stop');
                    lessonStudio.execute('editor:close');
                    lessonStudio.execute('lessons:reload');
                }

            },
            function() {
                valamisApp.execute('notify', 'error', Valamis.language['lessonFailedToSaveLabel']);
                jQueryValamis('body').css('pointer-events', 'all');
                slidesApp.initDnD();
            }
        );
    },
    switchLayout: function(e){
        e.preventDefault();
        var target = e.currentTarget || e.target,
            layoutId = jQueryValamis(target).data('value');
        jQueryValamis(target).tooltip('hide');
        this.setLayout(layoutId);
    },
    setLayout: function(layoutId){
        var deviceLayout = slidesApp.devicesCollection.findWhere({ id: layoutId }),
            deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
            layoutIdOld = deviceLayoutCurrent ? deviceLayoutCurrent.get('id') : null;

        if( !deviceLayout || layoutIdOld == layoutId ){
            return;
        }

        this.ui.buttons_select_layout
            .removeClass('active')
            .filter('[data-value="' + layoutId + '"]')
            .addClass('active');

        deviceLayout.set('active', true);
        if(layoutIdOld){
            this.updateElementsProperties(layoutIdOld);
        }
    },
    updateElementsProperties: function(layoutIdOld){
        var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
            slideElements = slidesApp.slideElementCollection.where( { toBeRemoved: false } );

        if( !deviceLayoutCurrent || slideElements.length === 0 ){
            return;
        }

        var layoutSizeRatio = slidesApp.devicesCollection.getSizeRatio(layoutIdOld);

        slideElements.forEach(function(model){
            var modelView = Marionette.ItemView.Registry
                .getByModelId(model.get('tempId') || model.get('id'));

            if( modelView ){
                model.setLayoutProperties(layoutIdOld);//save attributes for previous layout
                model.applyLayoutProperties(deviceLayoutCurrent.get('id'), layoutSizeRatio);

                modelView.wrapperUpdate();
                modelView.updateControlsPosition();
                modelView.trigger('resize:stop');
            }
        });

        window.placeSlideControls();

    },
    updateDevicesView: function(){
        this._renderChildren();
    },
    showDeviceSelectModal: function(){
        var modalView = Marionette.CompositeView.extend({
            template: '#selectDeviceModalTemplate',
            childView: lessonStudio.Views.DeviceItemView,
            childViewContainer: '.devices-list',
            ui: {
                'button_continue': '.js-button-continue'
            },
            events: {
                'click @ui.button_continue': 'selectSubmit'
            },
            collectionEvents: {
                'change': 'collectionChanged'
            },
            onShow: function(){
                this.$el.closest('.bbm-modal')
                    .css({ maxWidth: 640 })
                    .position({
                        my: 'center',
                        at: 'center',
                        of: window
                    });
            },
            collectionChanged: function(){
                var selected = this.collection.where({active: true});
                this.ui.button_continue.toggleClass( 'primary', selected.length > 0 );
            },
            selectSubmit: function(e){
                e.preventDefault();
                slidesApp.slideSetModel.updateDevices();
                view.destroy();
            }
        });

        var view = new valamisApp.Views.ModalView({
            contentView: new modalView({
                collection: slidesApp.devicesCollection
            }),
            beforeCancel: function(){
                slidesApp.devicesCollection.updateSelected();
            },
            className: 'lesson-studio-modal light-val-modal',
            header: Valamis.language.valSelectDeviceSettingsLabel
        });
        valamisApp.execute('modal:show', view);

    }
});

slidesApp.devicesCollection = new lessonStudioCollections.LessonDeviceCollection;
slidesApp.devicesCollection.fetch();
