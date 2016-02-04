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
            if(_.indexOf(['pptx', 'pdf', 'question'], this.model.get('slideEntityType')) == -1)
                slidesApp.execute('drag:prepare:new', this.model, e.clientX, e.clientY);
        },
        onClick: function(e) {
            var that = this;
            if(_.indexOf(['pdf', 'pptx'], this.model.get('slideEntityType')) > -1) {
                if(this.model.get('slideEntityType') === 'pptx') {
                    if (!slidesApp.activeSlideModel.get('id'))
                        slidesApp.activeSlideModel.save().then(function (slideModel) {
                            that.showDisplayFormatSelectionView();
                        });
                    else
                        that.showDisplayFormatSelectionView();
                }
                else this.showDisplayFormatSelectionView();
            } else if (_.indexOf(['question'], this.model.get('slideEntityType')) > -1) {
                slidesApp.execute('contentmanager:show:modal', this.model);
            }
            else {
                slidesApp.execute('drag:prepare:new', this.model, 0, 0);
                slidesApp.activeElement.isMoving = false;
                slidesApp.execute('item:create', true);
            }
        },
        showDisplayFormatSelectionView: function() {
            var fileSelectView = new sidebarModule.fileSelectView({ model: this.model });
            var modalView = new valamisApp.Views.ModalView({
                contentView: fileSelectView,
                className: 'lesson-studio-modal light-val-modal select-display-format-modal',
                header: (this.model.get('slideEntityType') === 'pdf') ? Valamis.language['AddPDFFileLabel'] : Valamis.language['AddPPTXFileLabel']
            });
            valamisApp.execute('modal:show', modalView);
        }
    });

    SideBarModule.fileSelectView = Marionette.ItemView.extend({
        template: '#fileSelectMethodViewTemplate',
        className: 'text-center',
        events: {
            'click .js-select-google-file': 'loadGooglePicker'
        },
        behaviors: {
            ImageUpload: {
                'postponeLoading': false,
                'fileUploaderLayout': '.js-file-uploader',
                'autoUpload': function(model) { return model.get('slideEntityType') !== 'pdf'; },
                'getFileUploaderUrl': function (model) {
                    var contentType = model.get('contentType') || 'import-from-pptx';
                    var uploaderUrl = path.root + path.api.files +
                        '?action=ADD&contentType=' + contentType +
                        '&courseId=' + Utils.getCourseId();
                    if(contentType === 'pdf')
                        uploaderUrl += '&entityId=' + model.get('entityId');
                    // If a document (PDF or PPTX) needs to be split in separate pages
                    else
                        uploaderUrl += '&slideSetId=' + slidesApp.slideSetModel.get('id') + '&slideId=' + model.get('slideId');

                    return uploaderUrl;
                },
                'uploadLogoMessage' : function() {
                    return Valamis.language['uploadFileMessage'];
                },
                'fileUploadModalHeader' : function() { return Valamis.language['selectDisplayFormatLabel']; },
                'selectDisplayFormatView': function(parentModalView) {
                    if(parentModalView.model.get('slideEntityType') === 'pdf')
                        return new sidebarModule.selectDisplayFormatView({});
                },
                'onBeforeInit': function (model, context, callback) {
                    slidesApp.fileTypeGroup = model.get('slideEntityType');
                    slidesApp.saveSlideset().then(function() {
                        if(!slidesApp.activeSlideModel.get('id'))
                            slidesApp.activeSlideModel.save().then(function(newSlideModel) {
                                // Creating a model, because a plain object is returned.
                                // Need tempId attribute to update the rest of slide refs when saving the whole lesson.
                                model.set('slideId', newSlideModel.id);
                                var slideModel = new lessonStudio.Entities.LessonPageModel(newSlideModel).set({ tempId: model.get('slideId') });
                                var slideIndices = { h: Reveal.getIndices().h, v: Reveal.getIndices().v };
                                slidesApp.slideRegistry.update( model.get('slideId'), slideModel.get('id'), slideIndices );
                                callback(context);
                            });
                        else {
                            model.set('slideId', slidesApp.activeSlideModel.get('id'));
                            if(typeof callback === 'function')
                                callback(context);
                        }
                    });
                },
                'acceptFileTypes': function(model) {
                    var types = _.has(mimeToExt, model.get('slideEntityType'))
                        ? '(' +
                            _.reduce(getMimeTypeGroupValues(model.get('slideEntityType')), function(a, b) {
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
        initialize: function() {
            this.isShown = false;
        },
        onShow: function () {
            if(!this.isShown) this.triggerMethod('InitStart');
            this.isShown = true;
        },
        loadGooglePicker: function(e) {
            loadPicker();
            valamisApp.execute('modal:close', this);
        },
        onFileAdded: function (file, data) {
            var filedata = _.clone(data);
            if (this.model.get('contentType') === 'pdf')
                slidesApp.activeElement.view.updateUrl(filedata.name, slidesApp.activeElement.view.model.get('content'));
            else {
                for(var i = 0; i < filedata.length; i++) {
                    slidesApp.execute('reveal:page:changeBackgroundImage', filedata[i]['_2'] + " contain");
                    if(i != 0)
                        slidesApp.tempSlideIds.push(slidesApp.activeSlideModel.id);
                    if(i != filedata.length - 1) {
                        var model = new lessonStudio.Entities.LessonPageModel({
                            id: filedata[i + 1]['_1'],
                            slideSetId: slidesApp.slideSetModel.id
                        });
                        slidesApp.execute('reveal:page:add', 'down', model, 'pdf');
                        slidesApp.execute('reveal:page:applyTheme', model, true, true);
                    }
                }

                slidesApp.viewId = undefined;
                slidesApp.actionType = 'documentImported';
                slidesApp.oldValue = undefined;
                slidesApp.newValue = { slideCount: filedata.length };
                slidesApp.execute('action:push');
            }
            valamisApp.execute('modal:close', this);
            slidesApp.execute('item:blur');
        }
    });

    SideBarModule.selectDisplayFormatView = Marionette.ItemView.extend({
        template: '#selectDisplayFormatViewTemplate',
        className: 'text-center',
        events: {
            'click .js-select-display-format': 'selectDisplayFormat'
        },
        templateHelpers: function() {
            return {
                singleSlideDisplayLabel: Valamis.language['buttonSingleSlidePdfDisplay'],
                splitLabel: Valamis.language['buttonSplitPdf']
            };
        },
        selectDisplayFormat: function(e) {
            var that = this;
            this.contentType = jQueryValamis(e.target).attr('data-value');
            if(this.contentType === 'pdf') {
                var toolbarItemModel = new Backbone.Model();
                toolbarItemModel.set({
                    title: 'PDF',
                    slideEntityType: 'pdf'
                });

                slidesApp.execute('drag:prepare:new', toolbarItemModel, 0, 0);
                slidesApp.execute('item:create', true);
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
            this.trigger('contentType:selected', this.contentType, entityId);
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
                            whiteSpace: 'nowrap'
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