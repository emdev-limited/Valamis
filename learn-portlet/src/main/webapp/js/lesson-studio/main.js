var slidesApp = new Backbone.Marionette.Application({
    container: '#revealEditor',
    type: 'editor'
});

function getServletContextPath(){
    return jQueryValamis('#ServletContextPath').val();
}

function showSlideSet(slideSetModel) {
    var deferred = jQueryValamis.Deferred();
    if(!lessonStudio.googleClientApiReady)
        lessonStudio.googleClientAPILoadTryCount++;
    if(!lessonStudio.youtubeIframeApiReady)
        lessonStudio.youtubeIframeAPILoadTryCount++;

    if(lessonStudio.googleClientApiReady && lessonStudio.youtubeIframeApiReady && lessonStudio.googleClientApiConfigured) {
        if(lessonStudio.googleClientAPILoadTryCount > 10)
            lessonStudio.googleClientApiReady = false;
        if(lessonStudio.youtubeIframeAPILoadTryCount > 10)
            lessonStudio.youtubeIframeApiReady = false;

        lessonStudio.googleAPIsLoadTryCount = 0;
        slidesApp.slideSetModel = slideSetModel;
        slidesApp.mode = 'edit';
        slidesApp.tempBlobUrls = [];
        slidesApp.slideTemplateCollection = new lessonStudio.Entities.LessonPageCollection(null, {
            model: lessonStudio.Entities.LessonPageTemplateModel,
            isTemplates: true
        });

        slidesApp.slideCollection = new lessonStudio.Entities.LessonPageCollection();
        slidesApp.slideCollection
            .on('add', function(model, collection, options){
                slidesApp.vent.trigger('slideAdd', model, collection, options);
            })
            .on('remove', function(model, collection, options){
                slidesApp.vent.trigger('slideRemove', model, collection, options);
            });

        slidesApp.slideElementCollection = new lessonStudio.Entities.LessonPageElementCollection();
        slidesApp.slideElementCollection.on('add', function(model, collection, options){
            if(!slidesApp.initializing){
                if(options.isUndoAction){
                    slidesApp.execute('item:create', model);
                }
                _.defer(function(){
                    slidesApp.vent.trigger('elementsUpdated');
                });
            }
        });

        slidesApp.newSlideId = -1;
        slidesApp.newSlideElementId = -1;
        slidesApp.categories = [];
        slidesApp.questions = [];
        slidesApp.questionCollection = new Backbone.Collection();
        slidesApp.slideCollection.on('sync', function () {

            if (!slidesApp.isRunning && slidesApp.slideCollection && slidesApp.slideElementCollection) {
                jQueryValamis.when(revealModule.start()).then(function() {
                    deferred.resolve();
                });

                jQueryValamis(lessonStudio.slidesWrapper + ' .js-lesson-title').html(
                    (slideSetModel.get('title').length < 100)
                        ? slideSetModel.get('title')
                        : slideSetModel.get('title').substr(0, 96) + ' ...'
                );

                if (slideSetModel.get("lockUserId")) {
                    var lockDate  = new Date(slideSetModel.get('lockDate'));
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-lock-user').html(
                        Valamis.language['lockedByLabel'] + ' ' +
                        slideSetModel.get("lockUserName") + ', ' +
                        jQueryValamis.datepicker.formatDate("dd MM yy", lockDate)+ " " +
                        lockDate.toLocaleTimeString()
                    );
                }
                jQueryValamis(lessonStudio.slidesWrapper + ' .js-presentation-unpublished')
                    .toggleClass('hidden', (slideSetModel.get('status') != 'draft'));

                if(slideSetModel.get('isTemplate')) {
                    slidesApp.switchMode('preview', false);
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-mode-switcher').hide();
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-undo').hide();
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-editor-save-container').hide();
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-lesson-title')
                        .append(' (' + Valamis.language['templatePreviewLabel'] + ')');
                } else {
                    if (slideSetModel.get("lockDate") && Utils.getUserId() != slidesApp.slideSetModel.get("lockUserId")) {
                        slidesApp.switchMode('versions');

                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-presentation-unsaved').hide();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-delete-version').hide();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-close-version').hide();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .layout-resizable-handle').hide();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-close-slideset').show();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-lock-user').show();
                    }
                    else {
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-mode-switcher').show();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-mode-switcher > .button-group').show()
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-undo').show();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-editor-save-container').show();
                        jQueryValamis(lessonStudio.slidesWrapper + ' .js-lock-user').hide();
                    }
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
        slidesApp.slideTemplateCollection.fetch({
            model: new lessonStudio.Entities.LessonPageTemplateModel(slideSetModel).set('id', 0),
            isTemplate: true
        });

        slidesApp.hasVersions = !!(slidesApp.slideSetModel.get('version'));

        slidesApp.versionsCollection = new lessonStudio.Entities.LessonCollection(slidesApp.slideSetModel.toJSON());

        slidesApp.versionsCollection.on('versionsCollection:updated', function() {
            jQueryValamis(lessonStudio.slidesWrapper + ' .js-versions-history')
                .toggleClass('hidden', (slidesApp.versionsCollection.length <= 1));
        });

        versionModule.start();
    }
    else {
        if(!lessonStudio.googleApiConfigured)
            lessonStudio.googleClientApiConfigured = true;

        if(lessonStudio.youtubeIframeAPILoadTryCount <= 10 && lessonStudio.googleClientAPILoadTryCount <= 10)
            setTimeout(function() {
                showSlideSet(slideSetModel);
            }, 500);
        else {
            if(!lessonStudio.youtubeIframeApiReady) {
                valamisApp.execute('notify', 'warning', Valamis.language['youtubeApiLoadingFailedLabel']);
                lessonStudio.youtubeIframeApiReady = true;
            }
            if(!lessonStudio.googleClientApiReady) {
                valamisApp.execute('notify', 'warning', Valamis.language['googleClientApiLoadingFailedLabel']);
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

slidesApp.save = function(options) {
    options || (options = {});
    if (slidesApp.activeSlideModel) {
        slidesApp.execute('item:blur', slidesApp.activeSlideModel.id);
    }
    slidesApp.deferred = jQueryValamis.Deferred();
    if(!slidesApp.isSaved) {
        var totalSlideCount = 0;
        var totalSlideElementCount = 0;
        var i = 0;

        // Blur all CKEDITOR instances
        for (var i in CKEDITOR.instances) {
            CKEDITOR.instances[i].focusManager.blur();
        }

        jQueryValamis.when(slidesApp.clonePublishedSlideSet()).then(
            function () {
                var rootSlideModel = slidesApp.slideCollection.getRootSlide();
                rootSlideModel.unset('leftSlideId');
                rootSlideModel.unset('topSlideId');
                jQueryValamis.when.apply(jQueryValamis, [destroyRemovedModels('slide'), destroyRemovedModels('slideElement')])
                    .then(function () {
                        jQueryValamis.when(saveSlide(rootSlideModel)).then(
                            function () {
                                slidesApp.slideSetModel.save().then(function (slideSetModel) {
                                    slidesApp.versionsCollection.add(slideSetModel);

                                    slidesApp.deferred.resolve(slideSetModel, options);
                                    if (!options.close) { //todo
                                        versionModule.stop();
                                        versionModule.start();
                                    }
                                });
                            },
                            function () {
                                slidesApp.deferred.reject(options);
                            }
                        );
                    });
            },
            function () {
                slidesApp.deferred.reject(options);
            }
        );
    }
    else {
        slidesApp.deferred.resolve(options);
    }


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
                        var fileExt = Utils.getExtByMime(fileModel.get('mimeType'));
                        var correctFileName = fileModel.get('title').replace(/\s/g,'_');
                        var fileName = fileExt ? correctFileName + '.' + fileExt : correctFileName;
                        slideElementModel.set('content', fileName);
                    }

                    function onElementSaved(){
                        slideElementModel
                            .unset('formData')
                            .unset('fileModel')
                            .unset('fileUrl')
                            .unset('mediaGalleryTitle')
                            .unset('file')
                            .unset('changed')
                            .unset('clonedId');

                        i++;
                        totalSlideElementCount++;
                        if (i == slidesApp.slideElementCollection.where({ slideId: slideModelTempId }).length +
                            slidesApp.slideElementCollection.where({ slideId: slideModelId }).length)

                            deferred.resolve();
                    }

                    function saveSubmit(){
                        slideElementModel.save().then(function (newSlideElementModel) {
                            slideElementModel.set('id', newSlideElementModel.id);
                            var registeredModelView =
                                Marionette.ItemView.Registry.getByModelId(slideElementModel.get('tempId')) ||
                                Marionette.ItemView.Registry.getByModelId(slideElementModel.get('id'));

                            if(formData) {
                                slidesApp
                                    .uploadFile(slideElementModel, registeredModelView.behaviors.ImageUpload.getFolderId, formData)
                                    .always(onElementSaved);
                            }
                            else if(fileModel) {
                                if (fileModel.get('title').indexOf(' ') > -1) {
                                    var correctFileName = fileModel.get('title').replace(/\s/g, '_');
                                    fileModel.set('title', correctFileName);
                                }
                                registeredModelView.trigger('mediagallery:image:upload',
                                    fileModel,
                                    function() { onElementSaved(); }
                                );
                            }
                            else onElementSaved();
                        });
                    }

                    if (!slideElementModel.get('id') || slideElementModel.get('changed')){
                        saveSubmit();
                    }
                    else {
                        onElementSaved();
                    }
                }
            }
            return deferred.promise();
        }

    function saveSlide(slideModel) {
            var deferred = jQueryValamis.Deferred();
            if(slideModel) {
                var slideModelTempId = slideModel.get('tempId');
                var slidesElementsTemp = slideModel.get('slideElements');

                var slideDOMElement = jQueryValamis('section#slide_' + slideModel.getId());

                if (!slideModel.get('title'))
                    slideModel.set('title', 'Page');

                var formData = slideModel.get('formData');
                var fileModel = slideModel.get('fileModel');
                var bgSize = 'cover';
                if(formData && formData.itemModel){
                    bgSize = slideModel.getBackgroundSize();
                    slideModel.set('bgImage', formData.itemModel.get('filename') + ' ' + bgSize, {silent: true});
                }
                else if(fileModel) {
                    var fileExt = Utils.getExtByMime(fileModel.get('mimeType'));
                    var correctFileName = fileModel.get('title').replace(/\s/g,'_');
                    var fileName = fileExt ? correctFileName + '.' + fileExt : correctFileName;
                    bgSize = slideModel.getBackgroundSize();
                    slideModel.set('bgImage', fileName + ' ' + bgSize, {silent: true});
                }  else if (slideModel.get('originalBgImageName')) {//this property was set after ppt/pdf uploading
                    bgSize = 'contain';
                    slideModel.set('bgImage', slideModel.get('originalBgImageName'), {silent: true});
                    slideModel.unset('originalBgImageName');
                }

                jQueryValamis.when(slidesApp.saveSlideModel(slideModel)).then(function (model) {
                    var slideModelId = slideModel.get('id');
                    if (slideModel.get('bgImageSizeChange') && !slideModel.get('bgImageChange')){
                        jQueryValamis.when(slideModel.updateBgImage()).always(onSlideModelSaved);
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
                        });
                        _.each(elementsWithIncorrectLinkedSlides, function(slideElementModel) {
                            slideElementModel.set('incorrectLinkedSlideId', slideModelId);
                        });

                        if (formData) {
                            slidesApp
                                .uploadFile(slideModel, revealControlsModule.view.behaviors.ImageUpload.getFileUploaderUrl, formData, bgSize)
                                .then(function () {
                                    slideModel.trigger('change:bgImage', slideModel, slideModel.get('bgImage'));
                                })
                                .always(saveSlideElements);
                        }
                        else if (fileModel) {
                            if (fileModel.get('title').indexOf(' ') > -1) {
                                var correctFileName = fileModel.get('title').replace(/\s/g, '_');
                                fileModel.set('title', correctFileName);
                            }
                            revealControlsModule.view.model.set('id', model.id);
                            revealControlsModule.view.trigger('mediagallery:image:upload',
                                fileModel,
                                function () {
                                    slideModel.trigger('change:bgImage', slideModel, slideModel.get('bgImage'));
                                    saveSlideElements();
                                },
                                bgSize
                            );
                        }
                        //if bgImage was deleted
                        else if (typeof formData == 'undefined' && typeof fileModel == 'undefined' && slideModel.get('bgImageChange')) {
                            slidesApp.deleteFile(slideModel, revealControlsModule.view.behaviors.ImageUpload.getFileUploaderUrl)
                                .always(saveSlideElements());
                        }
                        else saveSlideElements();

                        function saveSlideElements() {
                            slideModel
                                .unset('slideId')
                                .unset('bgImageChange', { silent: true })
                                .unset('bgImageSizeChange', { silent: true })
                                .unset('formData')
                                .unset('fileModel')
                                .unset('fileUrl')
                                .unset('changed')
                                .set('slideElements', slidesElementsTemp );

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
        //TODO when slide will be removed
        // slidesApp.slideElementsCollectionelements will keep slides elements -
        // need to just remove them from collection
        var deferred = jQueryValamis.Deferred();
        var collection = type === 'slide' ? slidesApp.slideCollection : slidesApp.slideElementCollection;

        var toRemove = collection.where({'toBeRemoved': true});

        var toRemoveAmount = toRemove.length;

        var resolveIfLastModel = function (index) {
            if (index >= toRemoveAmount - 1) {
                deferred.resolve();
            }
        };

        if (toRemoveAmount > 0) {
            _.each(toRemove, function (model, index) {

                if (type === 'slide') {
                    slidesApp.slideElementCollection.remove(model.get('slideElements'));
                }

                model.destroy({
                    success: function () {
                        resolveIfLastModel(index);
                    }
                });
            });
        } else {
            deferred.resolve();
        }

        return deferred.promise();
    }

    return slidesApp.deferred.promise();
};

slidesApp.saveSlideModel = function (model) {
    var deferred = jQueryValamis.Deferred();
    if (!model.get('id') || model.get('changed')) {
        model.save({ isTemplate: false })
            .then(
                function (newModel) {
                    model.set('id', newModel.id);
                    deferred.resolve(model);

                },
                function () {
                    deferred.reject();
                })
    }
    else deferred.resolve(model);
    return deferred.promise();
};

slidesApp.toggleSavedState = function(set_saved) {
    if(!slidesApp.initializing && slidesApp.topbar.currentView) {
        if( typeof set_saved == 'undefined' ){
            set_saved = !slidesApp.isSaved;
        }
        var topBar = slidesApp.topbar.currentView;
        if (set_saved) {
            topBar.ui.button_save.hide();
            topBar.ui.button_disabled_saved.show();
            topBar.ui.label_unsaved.hide();
            slidesApp.isSaved = true;
        }
        else {
            topBar.ui.button_save.show();
            topBar.ui.button_disabled_saved.hide();
            var lockDate = slidesApp.slideSetModel.get("lockDate")
            if (!lockDate || lockDate && Utils.getUserId() === slidesApp.slideSetModel.get("lockUserId")){
                topBar.ui.label_unsaved.show();
            }
            slidesApp.isSaved = false;
        }
    }
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
            if(modelId == intId) return this.items[view];
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
    var folderPrefix, modelId;
    if(url && url.indexOf('/') == -1) {
        if (model.get('slideSetId')) {//is slide model
            modelId = model.get('id') || model.get('slideId');
            folderPrefix = 'slide_';
        } else {
            modelId = model.get('id') || model.get('clonedId');
            folderPrefix = model.get('isTheme') ? 'slide_theme_' : 'slide_item_';
        }
        var elementType = model.get('slideEntityType');
        if (elementType === 'pdf')
            url = Utils.getContextPath() + 'preview-resources/pdf/web/viewer.html?file=' +
                getServletContextPath() + '/SCORMData/files/slideData' + modelId + '/' + filename;
        else if (elementType === 'audio')
            url = path.root + path.api.files + 'audio?folderId=' + folderPrefix + modelId + '&file=' + filename;
        else
            url = path.root + path.api.files + 'images?folderId=' + folderPrefix + modelId + '&file=' + filename;
    }
    return url;
};


slidesApp.getPdfUrl = function(model, pdfName) {
    var modelId = model.get('id') || model.get('clonedId');
    return getServletContextPath() + '/SCORMData/files/slideData'+ modelId +'/'+ pdfName
};

slidesApp.uploadFile = function(model, getUrlFunction, formData, bgSize) {
    var deferred = jQueryValamis.Deferred();
    if(model && formData && typeof getUrlFunction === 'function') {
        var uploaderUrl = slidesApp.getUrlForUploadFile(model, getUrlFunction, bgSize);
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

slidesApp.getUrlForUploadFile = function(model, getUrlFunction, bgSize) {
    var mediaGallery = model.get('mediaGalleryTitle');
    var contentType = 'icon';
    if (_.contains(['webgl', 'pdf', 'audio'], model.get('slideEntityType')))
        contentType = model.get('slideEntityType');
    else if (mediaGallery)
        contentType = 'document-library';

    var endPointParam = (bgSize)
        ? {
        courseId: Utils.getCourseId(),
        contentType: contentType,
        bgSize: bgSize
    } : {
        action: 'ADD',
        courseId: Utils.getCourseId(),
        contentType: contentType,
        folderId: getUrlFunction(model)
    };

    return (bgSize)
        ? getUrlFunction(model)+ '?' + jQueryValamis.param(endPointParam)
        : path.root + path.api.files + "?" + jQueryValamis.param(endPointParam)
};

slidesApp.deleteFile = function(model, deleteUrlFunction) {
    var deferred = jQueryValamis.Deferred();
    if(model && typeof deleteUrlFunction === 'function') {
        jQueryValamis.ajax({
            url: deleteUrlFunction(model),
            type: 'DELETE',
            data: {courseId: Utils.getCourseId()},
            headers: {
                'X-CSRF-Token': Liferay.authToken
            },
            success: function() { deferred.resolve(); },
            error: function() { deferred.reject(); }
        });
    }
    else deferred.reject();
    return deferred.promise();
};

//TODO: rename function
slidesApp.initDnD = function() {
    jQueryValamis(slidesApp.container)
        .unbind('mousedown')
        .bind('mousedown',function(e) {
            var $target = jQueryValamis(e.target);
            if( slidesApp.mode != 'edit' ) return;
            //We click on button, not on icon sometimes
            if ((e.target.className.indexOf('val-icon-') == 0
                || (e.target.firstElementChild && e.target.firstElementChild.className.indexOf('val-icon-') == 0))) {

                e.preventDefault();
                var button = $target.closest('button');
                if( !button.is('.js-change-slide-background') && revealControlsModule.pickerVisible ){
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-change-slide-background').colpickHide();
                }
                return false;
            }
            if ($target.closest('div.rj-element').length == 0
                && $target.closest('div[id^="cke_editor"]').length == 0
                && $target.closest('div[class*="val-modal"]').length == 0) {
                if ($target.closest('.slide-popup-panel').length == 0) {
                    if (!slidesApp.activeElement.isMoving && !slidesApp.activeElement.isResizing && !revealControlsModule.pickerVisible) {
                        if (slidesApp.activeElement.view && slidesApp.activeElement.view.editor) {
                            slidesApp.activeElement.view.destroyEditor();
                        }
                        slidesApp.execute('item:blur');
                    }
                }
                else {
                    jQueryValamis(lessonStudio.slidesWrapper + ' .js-change-slide-background').colpickHide();
                }

            }
        });
};

slidesApp.checkIsTemplate = function() {
    if(!slidesApp.activeSlideModel) return;
    var isLessonSummary = slidesApp.activeSlideModel.get('isLessonSummary');
    var slideId = slidesApp.activeSlideModel.getId();
    if (isLessonSummary){
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-hide-if-summary').hide();
        slidesApp.execute('item:blur', slideId);
    }
    else {
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-hide-if-summary').show();
    }
};

slidesApp.layoutResizeInit = function(){
    var workArea = jQueryValamis(lessonStudio.slidesWrapper + ' .slides-work-area-wrapper');
    var versionSidebar = jQueryValamis(lessonStudio.slidesWrapper + ' .version-sidebar');
    var workAreaMarginBottom = parseInt(workArea.css('margin-top')) + parseInt(workArea.css('margin-bottom'));

    versionSidebar.css('height', parseInt(workArea.css('height')) + workAreaMarginBottom);
    if( jQueryValamis(lessonStudio.slidesWrapper + ' .layout-resizable-handle', workArea).size() > 0 ){
        return;
    }

    jQueryValamis('<div/>',{
        "class": "layout-resizable-handle"
    }).appendTo( workArea );

    var startAutoResize = function(e, start){
        if( typeof start == 'undefined' ){ start = true; }
        clearInterval(window.timer);
        if(!start || (e && e.type != 'mouseleave')) return;
        var wrapper = jQueryValamis(lessonStudio.slidesWrapper + ' .slides-editor-main-wrapper');
        window.timer = setInterval(function(){
            wrapper.find('.slides-work-area-wrapper')
                .add(wrapper.find('.slides'))
                .css('height', '+=5');
            wrapper.find('.version-sidebar')
                .css('height', '+=5');
            wrapper.find('.layout-resizable-handle')
                .css('top', '100%');
            wrapper
                .scrollTop(wrapper.get(0).scrollHeight);
        }, 10);
    };

    jQueryValamis(lessonStudio.slidesWrapper + ' .layout-resizable-handle').draggable({
        axis: 'y',
        scroll: false,
        start: function(){
            var editorArea = slidesApp.getRegion('editorArea').$el,
                wrapper = editorArea.closest('.slides-editor-main-wrapper');
            revealControlsModule.view.updateTopDownNavigation(false);
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
            versionSidebar
                .css({
                    height: ui.position.top + workAreaMarginBottom
                });
        },
        stop: function(event, ui){
            jQueryValamis( this ).css({top: '100%', left: ''});
            var oldHeight = slidesApp.activeSlideModel.get('height'),
                newHeight = Math.round(workArea.height());
            slidesApp.activeSlideModel.updateProperties({height: newHeight});
            var isTopDownEnabled = !!(slidesApp.slideSetModel.get('topDownNavigation'));
            revealControlsModule.view.updateTopDownNavigation(isTopDownEnabled);
            startAutoResize(null, false);
            workArea.parent()
                .unbind('mouseleave mouseenter', startAutoResize);
        }
    });
};

slidesApp.copyFileContent = function (model) {
    var deferred = jQueryValamis.Deferred();
    if (!model.get('fileModel') && !model.get('formData')) {
        var fileName = model.get('content');
        var src = (model.get('slideEntityType') == 'pdf')
            ? slidesApp.getPdfUrl(model, fileName)
            : slidesApp.getFileUrl(model, fileName);
        var getBlob = (model.get('slideEntityType') == 'pdf')
            ? pdfSrcToBlob
            : imgSrcToBlob;

        getBlob.call(this, src).then(function (blob) {
            var formData = new FormData();
            formData.append('p_auth', Liferay.authToken);
            formData.append('files[]', blob, fileName);
            formData.itemModel = new FileUploaderItemModel({
                filename: fileName
            });
            model
                .set('formData', formData)
                .set('content', createObjectURL(blob))
                .unset('fileModel');
            deferred.resolve()
        });
    }
    else {
        deferred.resolve();
    }
    return deferred.promise();
};

slidesApp.clonePublishedSlideSet = function () {
    var deferred = jQueryValamis.Deferred();
    if (_.contains(['published', 'archived'], slidesApp.slideSetModel.get('status'))) {
        slidesApp.slideSetModel.set('newVersion', true);
        slidesApp.slideSetModel.clone()
            .then(
            function (response) {
                deferred.resolve();
                jQueryValamis(lessonStudio.slidesWrapper + ' .js-versions-history').show();
            },
            function () {
                deferred.reject();
            })
    }
    else deferred.resolve();
    return deferred.promise();
};

slidesApp.getModuleName = function(slideEntityType){
    var moduleNamePrefix = _.contains(['question','plaintext','randomquestion'], slideEntityType)
        ? 'content'
        : slideEntityType;
    return moduleNamePrefix.charAt(0).toUpperCase() + moduleNamePrefix.slice(1) + 'ElementModule';
};

slidesApp.setFileContent = function(view, file, data) {
    var fileUrl = (typeof file === 'string')
        ? file
        : createObjectURL(file);
    view.model.set('file', file);
    view.model.set('fileUrl', fileUrl);
    view.model.set('fileModel', data);

    if(typeof file === 'object')
        view.model.set('formData', data);

    var oldContent = view.model.get('content');
    view.model.set('content', fileUrl);

    var undf;//TODO refactor to get rid of this awful-looking method's call
    view.updateUrl(fileUrl, oldContent, undf, undf, undf, true);
};

slidesApp.on('start', function(options){

    CKEDITOR.disableAutoInline = true;

    slidesApp.addRegions({
        sidebar: lessonStudio.slidesWrapper + ' .sidebar',
        topbar: lessonStudio.slidesWrapper + ' .slides-editor-topbar',
        editorArea: lessonStudio.slidesWrapper + ' .reveal-wrapper',
        revealControls: lessonStudio.slidesWrapper + ' .reveal-controls',
        arrangeArea: lessonStudio.slidesWrapper + ' #arrangeContainer',
        versionArea: lessonStudio.slidesWrapper + ' .version-sidebar',
        modals: {
            selector: '#slides-modals-layout',
            regionClass: Backbone.Marionette.Modals
        }
    });
    sidebarModule.start();
    slidesApp.module('gridSnapModule', ValamisGridSnapModule);
    slidesApp.module('historyManager', HistoryManager);
    slidesApp.module('keyboardModule', KeyboardModule);

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
});

var TopbarView = Marionette.CompositeView.extend({
    template: '#topbarTemplate',
    childView: lessonStudio.Views.DeviceItemView.extend({
        template: '#deviceItemButtonTemplate'
    }),
    childViewContainer: '.js-buttons-select-layout',
    className: 'val-row',
    templateHelpers: function(){
        return {
            hasVersions: slidesApp.hasVersions
        }
    },
    ui: {
        'button_change_theme': '.js-change-theme',
        'button_change_settings': '.js-change-settings',
        'button_display_grid': '.js-display-grid',
        'buttons_select_layout': '.js-buttons-select-layout .button',
        'button_save': '.js-slides-editor-save',
        'button_disabled_saved': '.js-slides-editor-changes-saved',
        'label_unsaved': '.js-presentation-unsaved',
        'label_unpublished': '.js-presentation-unpublished',
        'button_undo': '.js-undo',
        'button_redo': '.js-redo'
    },
    events: {
        'click .js-mode-switcher > .button-group > .slides-dark': 'switchEditorMode',
        'click .js-versions-history': 'showVersions',
        'click @ui.button_undo': 'triggerUndoAction',
        'click @ui.button_redo': 'triggerRedoAction',
        'click @ui.button_save': 'saveSlidesetWithoutClosing',
        'click .js-close-slideset': 'returnToOverview',
        'click .js-close-version': 'closeVersion',
        'click @ui.buttons_select_layout': 'activateLayout',
        'click @ui.button_display_grid': 'displayGridToggle',
        'click @ui.button_change_theme': function(e) {
            if (!this.ui.button_change_theme.hasClass('highlight')) {
                e.preventDefault();
                this.ui.button_change_theme.addClass('highlight');
                slidesApp.execute('controls:theme:change');
            }
        },
        'click @ui.button_change_settings':  function(e) {
            slidesApp.isEditing = true;
            if (!this.ui.button_change_settings.hasClass('highlight')) {
                e.preventDefault();
                this.ui.button_change_settings.addClass('highlight');
                slidesApp.execute('controls:settings:change');
            }
        }
    },
    collectionEvents: {
        'change:active': function(model, value){
            var that = this;
            if(value === true){
                slidesApp.historyManager.groupOpen();
                _.defer(function(){
                    that.setLayout( model.get('id') );
                    slidesApp.historyManager.groupClose();
                });
            }
        },
        'change:selected': 'updateDevicesView'
    },
    onRender: function() {
        var view = this;

        jQueryValamis(lessonStudio.slidesWrapper + ' .js-slides-editor-topbar')
            .tooltip({
                selector: '.valamis-tooltip',
                container: '.js-slides-editor-topbar',
                placement: 'bottom',
                trigger: 'hover'
            });
        view.$('.topbar-buttons .button.slides-dark')
            .tooltip({
                container: view.el,
                placement: 'bottom',
                trigger: 'hover'
            })
            .on('inserted.bs.tooltip', function () {
                jQueryValamis(this).data('bs.tooltip').$tip
                    .css({
                        'margin-top': '3px'
                    });
            });

        jQueryValamis(window).on('unload', function () {
            view.closeEditor(false);
        });

        slidesApp.historyManager.on('change:after', function(){
            view.ui.button_undo.toggleClass('disabled', !slidesApp.historyManager.isUndoAvailable());
            view.ui.button_redo.toggleClass('disabled', !slidesApp.historyManager.isRedoAvailable());
        });
        slidesApp.historyManager.on('change:save', function(){
            slidesApp.toggleSavedState(!slidesApp.historyManager.isUnsavedAvailable());
        });

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
    showVersions: function (e) {
        slidesApp.switchMode('versions');
    },
    returnToOverview: function () {
        jQueryValamis(lessonStudio.slidesWrapper + ' .slides-work-area-wrapper').css('margin-left', 'auto');
        if (!slidesApp.isSaved && (slidesApp.slideSetModel.get('lockUserId') == Utils.getUserId())) {
            var that = this;
            valamisApp.execute('valamis:confirm', {
                    title: Valamis.language['changesDetectedLabel'],
                    message: Valamis.language['saveConfirmationTitle'],
                    showDontSaveButton: true
                },
                function () {
                    that.triggerSaveSlideset({close: true});
                },
                function () {
                    that.closeEditor(true);
                }
            );
        } else {
            this.closeEditor(true);
        }
    },
    closeVersion: function() {
        jQueryValamis(lessonStudio.slidesWrapper + ' #revealEditor .slides-work-area-wrapper').css('margin-left', 'auto');
        versionModule.view.cleanActive();
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-editor-edit-mode').trigger('click');
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-mode-switcher .button-group').show();
        revealModule.renderSlideset();

    },
    closeEditor: function(isCloseByUser) {
        slidesApp.execute('temp:delete');

        if (isCloseByUser) {
            if (slidesApp.slideSetModel && slidesApp.slideSetModel.get('lockUserId') == Utils.getUserId()) {
                slidesApp.slideSetModel.changeLockStatus().then(
                    function() { lessonStudio.execute('lessons:reload'); },
                    function() { valamisApp.execute('notify', 'error', Valamis.language['lessonFailedToSaveLabel']); }
                );
            }
            else {
                lessonStudio.execute('lessons:reload');
            }
        }

        slidesApp.execute('app:stop');
        lessonStudio.execute('editor:close');
        valamisApp.execute('portlet:unset:onbeforeunload');
    },
    triggerUndoAction: function() {
        slidesApp.execute('item:blur');
        slidesApp.historyManager.apply( 'undo' );
    },
    triggerRedoAction: function() {
        slidesApp.execute('item:blur');
        slidesApp.historyManager.apply( 'redo' );
    },
    saveSlidesetWithoutClosing: function() {
        this.triggerSaveSlideset({ close: false });
    },
    triggerSaveSlideset: function(options) {
        var view = this;
        valamisApp.execute('notify', 'info', Valamis.language['lessonIsSavingLabel']); //, { 'timeOut': '0', 'extendedTimeOut': '0' });
        jQueryValamis(lessonStudio.slidesWrapper +' .slideset-editor .js-loading-spinner').toggleClass('hidden', false);

        view.ui.button_save.html(Valamis.language['lessonIsSavingButtonLabel']);
        jQueryValamis('body').css('pointer-events', 'none');

        slidesApp.historyManager.skipActions(true);

        slidesApp.save(options).then(
            function () {
                slidesApp.historyManager.skipActions(false);

                jQueryValamis(lessonStudio.slidesWrapper +' .slideset-editor .js-loading-spinner').toggleClass('hidden', true);
                valamisApp.execute('notify', 'clear');
                valamisApp.execute('notify', 'success', Valamis.language['lessonWasSavedSuccessfullyLabel']);

                view.ui.button_save.html(Valamis.language['saveLabel']);
                jQueryValamis('body').css('pointer-events', 'all');

                slidesApp.slideCollection.each(function (slideModel) {
                    slideModel.unset('tempId');
                });
                slidesApp.slideElementCollection.each(function (slideElementModel) {
                    slideElementModel.unset('tempId');
                });

                slidesApp.savedIndex = slidesApp.actionStack.length;
                slidesApp.historyManager.setSaved();
                if (options.close) {
                    view.closeEditor(true);
                }
            },
            function () {
                slidesApp.historyManager.skipActions(false);
                valamisApp.execute('notify', 'error', Valamis.language['lessonFailedToSaveLabel']);
                jQueryValamis('body').css('pointer-events', 'all');
            });
    },
    activateLayout: function(e){
        e.preventDefault();
        if (slidesApp.activeElement.view && slidesApp.activeElement.view.editor){
            slidesApp.activeElement.view.destroyEditor();
        }
        var target = e.currentTarget || e.target,
            layoutId = jQueryValamis(target).data('value');
        var deviceLayout = slidesApp.devicesCollection.findWhere({ id: layoutId });
        deviceLayout.set('active', true);
    },
    setLayout: function(layoutId){
        var deviceLayout = layoutId
                ? slidesApp.devicesCollection.findWhere({ id: layoutId })
                : slidesApp.devicesCollection.getCurrent(),
            layoutIdOld = slidesApp.devicesCollection.previousActiveId;

        if( !deviceLayout || layoutIdOld == layoutId ){
            return;
        }

        this.ui.buttons_select_layout
            .removeClass('active')
            .filter('[data-value="' + deviceLayout.get('id') + '"]')
            .addClass('active');

        if(slidesApp.editorArea){
            var wrapper = slidesApp.editorArea.$el.closest('.slides-work-area-wrapper');
            $('.reveal-wrapper').removeClass(wrapper.attr('data-layout'));
            $('.reveal-wrapper').addClass(deviceLayout.get('name'));
            wrapper.attr('data-layout', deviceLayout.get('name'));
        }
        slidesApp.RevealModule.configure({
            width: deviceLayout.get('minWidth') || deviceLayout.get('maxWidth')
        });
        if(slidesApp.RevealModule.view){
            slidesApp.RevealModule.view.updateSlideHeight();
        }

        //Apply new properties for all elements
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

        slideElements.forEach(function (model) {
            var modelView = Marionette.ItemView.Registry
                .getByModelId(model.get('tempId') || model.get('id'));

            if (modelView) {
                model.applyLayoutProperties(deviceLayoutCurrent.get('id'), layoutSizeRatio);

                modelView.wrapperUpdate();
                modelView.trigger('resize:stop');
            }
            if (_.contains(['question', 'plaintext'], model.get('slideEntityType')) && model.get('fontSize')) {
                slidesApp.questionFontSize.setValue(model.get('fontSize'));
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
                this.saveSelectedDevices();
                valamisApp.execute('modal:close', view);
            },
            saveSelectedDevices: function(){
                var selectedDevicesIds = this.$childViewContainer.find('li.active')
                    .map(function(){
                        return jQueryValamis(this).data('id');
                    });
                this.collection.updateSelectedModels(selectedDevicesIds);
            }
        });

        var view = new valamisApp.Views.ModalView({
            contentView: new modalView({
                collection: slidesApp.devicesCollection
            }),
            className: 'lesson-studio-modal light-val-modal',
            header: Valamis.language.valSelectDeviceSettingsLabel
        });
        valamisApp.execute('modal:show', view);

    },
    displayGridToggle: function(e){
        e.preventDefault();
        slidesApp.gridSnapModule.displayGridToggle();
    }
});

slidesApp.devicesCollection = new lessonStudioCollections.LessonDeviceCollection;
slidesApp.devicesCollection.fetch();