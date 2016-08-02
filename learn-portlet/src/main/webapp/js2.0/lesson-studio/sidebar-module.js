var sidebarModule = slidesApp.module('SideBarModule', function(SideBarModule, slidesApp, Backbone, Marionette, $, _){
    SideBarModule.startWithParent = false;
    SideBarModule.ToolbarItemView = Marionette.ItemView.extend({
        template: '#toolbarItemTemplate',
        className: 'toolbar-item',
        events: {
            'mousedown': 'onMouseDown',
            'click': 'onClick'
        },
        templateHelpers: function() {
            return {
                titleLower: (this.model.get('title').toLowerCase() === 'webgl' ? '3d' : this.model.get('title')).toLowerCase(),
                classList: (this.model.get('title').toLowerCase() === 'pdf') ? 'js-upload-image' : ''
            }
        },
        onMouseDown: function(e) {
            if (!_.contains(['pptx', 'pdf', 'question', 'imported'], this.model.get('slideEntityType'))){
                slidesApp.activeElement.isMoving = true;
                slidesApp.activeElement.moduleName = slidesApp.getModuleName(this.model.get('slideEntityType'));
                this.$el.one('mouseout', function(event){
                    //Create and drag new element
                    if(slidesApp.activeElement.isMoving){
                        slidesApp.activeElement.startX = event.clientX;
                        slidesApp.activeElement.startY = event.clientY;
                        slidesApp.execute('item:create', null, true);
                        e.type = "mousedown.draggable";
                        e.target = slidesApp.activeElement.view.$el.get(0);
                        slidesApp.activeElement.view.$el.trigger(e);
                    }
                })
            }
        },
        onClick: function(e) {
            if (_.contains(['pdf', 'pptx', 'imported'], this.model.get('slideEntityType'))) {
                this.showDisplayFormatSelectionView();
            } else if (_.indexOf(['question'], this.model.get('slideEntityType')) > -1) {
                slidesApp.execute('contentmanager:show:modal', this.model);
            }
            else {
                slidesApp.activeElement.isMoving = false;
                slidesApp.execute('item:create', null, true);
            }
        },
        showDisplayFormatSelectionView: function() {
            var fileSelectView = new sidebarModule.fileSelectView({ model: this.model });
            var modalView = new valamisApp.Views.ModalView({
                contentView: fileSelectView,
                className: 'lesson-studio-modal select-display-format-modal',
                header: Valamis.language['AddPdfPptFileLabel'],
                beforeCancel: function() {
                    fileSelectView.isCanceled = true;
                }
            });
            valamisApp.execute('modal:show', modalView);
        }
    });

    SideBarModule.fileSelectView = Marionette.ItemView.extend({
        template: '#fileSelectMethodViewTemplate',
        events: {
            'click .js-select-google-file': 'loadGooglePicker'
        },
        behaviors: {
            ImageUpload: {
                'postponeLoading': false,
                'fileUploaderLayout': '.js-file-uploader',
                'autoUpload': function(model) { return model.get('slideEntityType') !== 'imported'; },
                'getFileUploaderUrl': function (model) {
                    var contentType = model.get('contentType') || 'import-from-pptx';
                    var slideSetId = slidesApp.slideSetModel.get('id');
                    var uploaderUrl = path.root + path.api.files +
                        '?action=ADD&contentType=' + contentType +
                        '&courseId=' + Utils.getCourseId();
                    if(contentType === 'pdf')
                        uploaderUrl += '&entityId=' + model.get('entityId');
                    // If a document (PDF or PPTX) needs to be split in separate pages
                    else
                        uploaderUrl += '&slideSetId=' + slideSetId;

                    return uploaderUrl;
                },
                'uploadLogoMessage' : function() {
                    return Valamis.language['uploadFileMessage'];
                },
                'fileUploadModalHeader' : function() { return Valamis.language['displaySettingsLabel']; },
                'selectDisplayFormatView': function(parentModalView) {
                    if(parentModalView.model.get('slideEntityType') === 'imported')
                        return new sidebarModule.selectDisplayFormatView({ fileExt: parentModalView.model.get('fileExt') });
                },
                'onBeforeInit': function (model, context, callback) {
                    slidesApp.fileTypeGroup = model.get('slideEntityType');
                    if (typeof callback === 'function') {
                        callback(context);
                    }
                },
                'acceptFileTypes': function(model) {
                    var types = _.has(Utils.mimeToExt, model.get('slideEntityType'))
                        ? '(' +
                            _.reduce(Utils.getMimeTypeGroupValues(model.get('slideEntityType')), function(a, b) {
                                return a + ')|(' + b;
                            }) + ')'
                        : '';
                    return types;
                },
                'fileRejectCallback': function(context) {
                    if(context.selectDisplayFormatView) {
                        valamisApp.execute('modal:close');
                    }
                    context.uploadImage(context);
                }
            }
        },
        templateHelpers: function() {
            return {
                googleClientApiAvailable: lessonStudio.googleApiConfigured
            };
        },
        initialize: function() {
            this.isShown = false;
        },
        onShow: function () {
            this.isCanceled = false;
            if(!this.isShown) this.triggerMethod('InitStart');
        },
        loadGooglePicker: function(e) {
            loadPicker();
            valamisApp.execute('modal:close', this);
        },
        onFileAdded: function (file, data, orientationFormat) {
            var that = this;
            if (that.isCanceled) {
                return;
            }
            var fileData = _.clone(data);
            if (this.model.get('contentType') === 'pdf') {
                slidesApp.activeElement.view.updateUrl(fileData.name, slidesApp.activeElement.view.model.get('content'));
                valamisApp.execute('modal:close', this);
                slidesApp.execute('item:blur');
            } else {
                var results = [];
                var initial = jQueryValamis.Deferred().resolve();
                var res = fileData.reduce(function(prev, src) {
                    return prev.then(function(){ return dataUriToBlobUrl("data:image/png;base64," + src,results); });
                }, initial);

                res.then(function() {
                    if (that.isCanceled) {
                        return;
                    }
                    for (var i = 0; i < results.length; i++) {
                        var formData = new FormData();
                        formData.append('p_auth', Liferay.authToken);
                        formData.append('files[]', results[i].blob, results[i].fileName + ".png");
                        slidesApp.activeSlideModel.set("formData", formData);
                        slidesApp.activeSlideModel.set("fileUrl", results[i].blobUrl);
                        slidesApp.activeSlideModel.set("originalBgImageName", results[i].fileName + ".png contain");
                        slidesApp.activeSlideModel.unset("slideId");

                        slidesApp.execute('reveal:page:changeBackgroundImage', results[i].blobUrl + " contain");

                        if (i != results.length - 1) {
                            var model = new lessonStudio.Entities.LessonPageModel({
                                tempId: slidesApp.newSlideId--,
                                slideSetId: slidesApp.slideSetModel.id
                            });
                            slidesApp.execute('reveal:page:add', orientationFormat, model, 'pdf');
                            slidesApp.execute('reveal:page:applyTheme', model, true);
                        }
                    }

                    slidesApp.viewId = undefined;
                    slidesApp.actionType = 'documentImported';
                    slidesApp.oldValue = undefined;
                    slidesApp.newValue = { slideCount: fileData.length };
                    slidesApp.execute('action:push');

                    valamisApp.execute('modal:close', that);
                    slidesApp.execute('item:blur');
                });
            }
        }
    });

    SideBarModule.selectDisplayFormatView = Marionette.ItemView.extend({
        template: '#selectDisplayFormatViewTemplate',
        events: {
            'click .js-import-file': 'importFile',
            'change input[name="displayFormat"]': 'changeDisplayFormat',
            'click .js-select-orientation': 'changeOrientationFormat'
        },
        templateHelpers: function() {
            return {
                isPdf: (this.fileExt === 'pdf'),
                isTopDownEnabled: this.topDownEnabled
            };
        },
        initialize: function() {
            this.fileExt = this.options.fileExt;
            this.topDownEnabled = slidesApp.slideSetModel.get('topDownNavigation');
            this.orientationFormat = 'right';
        },
        onRender: function() {
            this.$('.valamis-tooltip').tooltip();
            if (_.contains('pptx', this.fileExt)) {  // for ppt and pptx
                this.$('.js-orientation-options').removeClass('hidden');
                this.toggleDisableButton(!!this.topDownEnabled);
            }
        },
        changeOrientationFormat: function(e) {
            this.$('.js-select-orientation').removeClass('selected');
            var elem = $(e.target).closest('.js-select-orientation');
            elem.addClass('selected');
            this.orientationFormat = elem.data('value');
            this.toggleDisableButton(false);
        },
        changeDisplayFormat: function(e) {
            var value = $(e.target).attr('value');

            if (value === 'single') {
                this.$('.js-orientation-options').addClass('hidden');
                this.toggleDisableButton(false);
            }

            if (value === 'split') {
                this.$('.js-orientation-options').removeClass('hidden');
                this.toggleDisableButton(this.topDownEnabled);
            }
        },
        toggleDisableButton: function(isDisable) {
            this.$('.js-import-file')
              .prop('disabled', isDisable)
              .toggleClass('primary', !isDisable)
              .toggleClass('neutral', isDisable);
        },
        importFile: function(e) {
            var that = this;
            var isSingle = this.$('input#singleFileFormat').is(':checked');

            if (this.fileExt === 'pdf')
                this.contentType = (isSingle) ? 'pdf' : 'import-from-pdf';
            else if (_.contains('pptx', this.fileExt)) // for ppt and pptx
                this.contentType = 'import-from-pptx';

            if (isSingle) {
                var toolbarItemModel = new Backbone.Model();
                toolbarItemModel.set({
                    title: 'PDF',
                    slideEntityType: 'pdf'
                });

                slidesApp.execute('prepare:new', toolbarItemModel);
                slidesApp.execute('item:create');
                slidesApp.activeElement.isMoving = false;
                var slideId = slidesApp.activeElement.view.model.get('slideId');
                if (slideId < 0) {
                    var slide = slidesApp.getSlideModel(slideId);
                    slide.save().then(function (newSlideModel) {
                        // Need tempId attribute to update the rest of slide refs when saving the whole lesson.
                        slide.set({ tempId: slideId });
                        slidesApp.activeElement.view.model.set('slideId', newSlideModel.id);
                        that.saveElementAndUploadPdf();
                    });
                }
                else that.saveElementAndUploadPdf();
            }
            else this.triggerPdfUpload(slidesApp.slideSetModel.get('id'));
        },
        saveElementAndUploadPdf: function() {
            var that = this;
            slidesApp.activeElement.view.model.save().then(function (slideElementModel) {
                that.triggerPdfUpload(slideElementModel.id);
            });
        },
        triggerPdfUpload: function(entityId) {
            this.trigger('contentType:selected', this.contentType, entityId, this.orientationFormat);
        }
    });

    SideBarModule.ToolbarCollectionView = Marionette.CollectionView.extend({
        childView: SideBarModule.ToolbarItemView,
        onAddChild: function( childView ) {
            var collectionView = this;
            childView.$('.button')
                .tooltip({
                    container: collectionView.el,
                    placement: 'bottom',
                    trigger: 'hover'
                })
                .on('inserted.bs.tooltip', function () {
                    jQueryValamis(this).data('bs.tooltip').$tip
                        .css({
                            whiteSpace: 'nowrap',
                            'margin-top': '-10px'
                        });
                });
        }
    });

    var collection = new Backbone.Collection();
    SideBarModule.collectionView = new SideBarModule.ToolbarCollectionView({ collection: collection });

    var SideBarLayoutView = Marionette.LayoutView.extend({
        template: '#sideBarLayoutTemplate',

        regions: {
            items: '#toolbar-items',
            controls: '#toolbar-controls'
        }
    });
    SideBarModule.sidebarView = new SideBarLayoutView();

    slidesApp.commands.setHandler('toolbar:item:add', function(model){
        collection.add(model);
    });

    slidesApp.commands.setHandler('toolbar:item:delete', function(model){
        collection.remove(model);
    });
});

sidebarModule.on('start', function() {
    slidesApp.sidebar.show(sidebarModule.sidebarView);
    sidebarModule.sidebarView.items.show(sidebarModule.collectionView);
});