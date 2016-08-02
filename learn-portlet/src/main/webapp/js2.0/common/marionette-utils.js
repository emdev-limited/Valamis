/**
 * Created by igorborisov on 10.04.15.
 */


window.Valamis = window.Valamis || {};
window.Valamis.language = window.Valamis.language || {};

Marionette.TemplateCache.prototype.compileTemplate = function(rawTemplate, options) {
    Mustache.parse(rawTemplate);
    return _.partial(Mustache.render, rawTemplate);
};

// Render a template with data by passing in the template
// selector and the data to render.
Marionette.Renderer = {

    // Render a template with data. The `template` parameter is
    // passed to the `TemplateCache` object to retrieve the
    // template function. Override this method to provide your own
    // custom rendering and template handling for all of Marionette.
    render: function(template, data, view) {

        _.extend(data, Valamis.language);
        _.extend(data, Valamis.permissions);

        if (!template) {
            throw new Marionette.Error({
                name: 'TemplateNotFoundError',
                message: 'Cannot render the template since its false, null or undefined.'
            });
        }

        var templateFunc = _.isFunction(template) ? template : Marionette.TemplateCache.get(template);
        return templateFunc(data);
    }
};


Behaviors = {};

Backbone.Marionette.Behaviors.behaviorsLookup = function() {
    return Behaviors;
};

Behaviors.ValamisUIControls = Backbone.Marionette.Behavior.extend({
    defaults:{
        'dropdown':'.dropdown',
        'plusminus':'.js-plus-minus',
        'digitsOnly':'.js-digits-only',
        'sidebarToggler': '.js-toggle-sidebar',
        'popupPanel': '.js-valamis-popup',
        'valamisRating': '.js-valamis-rating',
        'valamisSearch': '.js-search'
    },
    onRender:function(){
        this.$(this.options.dropdown).valamisDropDown();
        this.$(this.options.plusminus).valamisPlusMinus();
        this.$(this.options.digitsOnly).valamisDigitsOnly();
        this.$(this.options.sidebarToggler).valamisSidebar();
        this.$(this.options.popupPanel).valamisPopupPanel();
        this.$(this.options.valamisRating).valamisRating();
        this.$(this.options.valamisSearch).valamisSearch();

        if(this.view.onValamisControlsInit){
            this.view.onValamisControlsInit();
        }
    }
});

Behaviors.ImageUpload = Backbone.Marionette.Behavior.extend({
    defaults:{
        'postponeLoading': false,
        'autoUpload': function() {return true},
        'uploadImage':'.js-upload-image',
        'uploadLogoMessage' : 'uploadLogoMessage',
        'fileUploadModalHeader' : 'fileUploadModalHeader',
        'selectImageModalHeader': 'selectImageModalHeader',
        'rootUrl': function() {return ''},
        'getFolderId' : function() {return 'getDefaultFolderId'},
        'getFileUploaderUrl': function() {return ''},
        'fileUploaderLayout': '',
        'imageAttribute': 'logo',
        'acceptFileTypes': function() {return ''},
        'selectDisplayFormatView': function() {return null},
        'contentType': function() {return 'icon'},
        'onBeforeInit': function(model, context, callback) {
            if(typeof callback === 'function')
                callback(context);
        },
        'fileuploaddoneCallback': function(result, uploader, context) {
            context.view.triggerMethod('FileAdded', null, result);
            uploader.trigger("fileupload:done", {src: '', name: result.filename});
        },
        'fileuploadaddCallback': function() {},
        'fileRejectCallback': function(context) { context.uploadImage(context); },
        'addMethod': function(e, data, context) {
            var acceptFileTypes = new RegExp(context.options.acceptFileTypes(context.view.model) + '$', 'gi');
            var allowUpload = !!data.originalFiles[0].type &&
                acceptFileTypes.test(Utils.getExtByMime(data.originalFiles[0].type));
            if(!allowUpload) {
                var message = Valamis.language['unsupportedFileTypeMessage'] + '\n' +
                    Valamis.language['supportedFileTypesLabel'] + ' ' +
                    _.map(Utils.getMimeTypeGroupValues(context.view.model.get('slideEntityType')), function(tpe) {
                        return '<i>' + tpe + '</i>';
                    }).join(', ');
                valamisApp.execute('notify', 'warning', message, { timeOut: 5000 });
                context.options.fileRejectCallback(context);
            } else {
                if(context.autoUpload) data.submit();
            }
        }
    },
    events: {
        'click .js-upload-image': 'onInitStart',
        'click .js-select-from-media-gallery': 'onInitStart',
        'click .js-select-from-media-gallery-video': 'onInitStart',
        'click .js-design-new-badge': 'onInitStart'
    },
    initialize: function(){
        var that = this;
        that.logoData = new FormDataHelper();
        that.view.on('view:submit:image', function(callback){
          var folderId = that.options.getFolderId(that.view.model);
          that.logoData.setFolderId(folderId);
          var portletFileUploaderUrl = that.options.getFileUploaderUrl(that.view.model);
          that.logoData.setPortletFileUploaderUrl(portletFileUploaderUrl);
          that.logoData.submitData({success: function(name) { callback(name);} });
        });

        // for design badge support
        window.onmessage = function (e) {
            if (e.origin == 'https://www.openbadges.me' && e.data !== 'cancelled') {
                var fileName = 'icon.png';
                var postponeLoading = that.options.postponeLoading;
                if(postponeLoading && that.logoData.supports()){
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.CONTENT_TYPE, 'base64-icon');
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.INPUT_BASE64, e.data);
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.FILE_NAME, fileName);
                }
                // for old browsers saving images immediately
                else {
                    var endpointparam = {
                        action: 'ADD',
                        courseId: Utils.getCourseId(),
                        contentType: 'base64-icon',
                        folderId: that.view.model.get('id')
                    };

                    var portletFileUploaderUrl = that.options.getFileUploaderUrl(that.view.model);
                    var fileUploaderUrl = (portletFileUploaderUrl || path.root + path.api.files) + "?" + jQueryValamis.param(endpointparam);

                    var formData = {
                        'contentType': 'base64-icon',
                        'inputBase64': e.data,
                        'p_auth': Liferay.authToken
                    };

                    jQueryValamis.ajax({
                        url: fileUploaderUrl,
                        type: 'POST',
                        data: formData,
                        headers: {
                            'X-CSRF-Token': Liferay.authToken
                        }
                    });
                }

                var data = e.data.split(';');
                var src = e.data.slice(0, -(data[data.length - 1].length + 1));
                that.view.model.set({ 'logoSrc': src });
                that.view.model.set({ 'logo': fileName });
            }
        };
    },
    onInitStart: function(e) {
        var callback = this.uploadImage;
        if(e) {
            var el = jQueryValamis(e.target).closest('button,img');

            if (el.hasClass('js-upload-image'))
                callback = this.uploadImage;
            else if (el.hasClass('js-select-from-media-gallery'))
                callback = this.selectImage;
            else if (el.hasClass('js-select-from-media-gallery-video'))
                callback = this.selectVideo;
            else if (el.hasClass('js-design-new-badge'))
                callback = this.openBadgeDesigner;
        }
        this.view.trigger('element-modal:open');
        this.options.onBeforeInit(this.view.model, this, callback);
    },
    openBadgeDesigner: function(that) {
        var URL = 'https://www.openbadges.me/designer.html?origin=http://' + that.options.rootUrl();
        URL = URL + '&email=developer@example.com';
        URL = URL + '&close=true';
        var options = 'width=1015,height=680,location=0,menubar=0,status=0,toolbar=0';
        var designerWindow = window.open(URL, '', options);
    },
    uploadImage: function(that) {
        that.autoUpload = that.options.autoUpload(that.view.model);
        var postponeLoading = that.options.postponeLoading;
        var imageModel = {
          logo: '',
          logoSrc: ''
        };

        var folderId = that.options.getFolderId(that.view.model);
//        that.logoData.setFolderId(folderId);

        var fileUploaderUrl = '';
        if(!postponeLoading || !that.logoData.supports()){
          var endpointparam = {
            action:'ADD',
            courseId:  Utils.getCourseId(),
            contentType: that.options.contentType(that.view.model),
            folderId: folderId
          };
          var portletFileUploaderUrl = that.options.getFileUploaderUrl(that.view.model);
          // uploader URL will already contain all necessary parameters for PDF and PPTX in Lesson Studio
          fileUploaderUrl = (portletFileUploaderUrl.indexOf('action') > -1)
              ? portletFileUploaderUrl
              : (portletFileUploaderUrl || path.root + path.api.files) + "?" + jQueryValamis.param(endpointparam);
        }

        var uploader = new FileUploader({
            addMethod: function(e, data) { that.options.addMethod(e, data, that); },
            endpoint: fileUploaderUrl,
            autoUpload: that.autoUpload,
            message: that.options.uploadLogoMessage(that.view.model)
        });

        if(postponeLoading && that.logoData.supports()){
          uploader.on('fileuploadadd', function (data) {
            that.logoData.setSetting(IMAGE_PARAM_TYPE.CONTENT_TYPE, that.options.contentType(that.view.model));
            that.logoData.setSetting(IMAGE_PARAM_TYPE.FILE, data);

            var filename = data.name;
            that.logoData.readAsDataURL(data, function (img) {
              uploader.trigger("fileupload:done", { src: img, name: filename });
            });
          });
        }
        else {
            // use one uploader modal for pdf and ppt in lesson studio, set fileUploaderUrl after mime checking
            if (that.view.model.get('slideEntityType') === 'imported') {
                uploader.on('fileuploadadd', function (file, data) {
                    var formData = data;
                    var fileExt = Utils.getExtByMime(formData.originalFiles[0].type);
                    that.view.model.set('fileExt', fileExt);
                    that.selectDisplayFormatView = that.options.selectDisplayFormatView(that.view);

                    if(that.selectDisplayFormatView) {
                        var modalView = new valamisApp.Views.ModalView({
                            contentView: that.selectDisplayFormatView,
                            className: 'lesson-studio-modal select-display-format-modal',
                            header: that.options.fileUploadModalHeader,
                            beforeCancel: function () {
                                that.uploadImage(that);
                            }
                        });

                        that.selectDisplayFormatView.on('contentType:selected', function (contentType, entityId, orientationFormat) {
                            that.view.model.set('contentType', contentType);
                            that.view.model.set('entityId', entityId);
                            that.logoData.setSetting(IMAGE_PARAM_TYPE.CONTENT_TYPE, contentType);
                            that.logoData.setSetting(IMAGE_PARAM_TYPE.FILE, file);
                            // This is necessary because otherwise "progress" does not happen in uploader
                            formData.url = that.options.getFileUploaderUrl(that.view.model);
                            formData.submit().done(function (result) {
                                                valamisApp.execute('modal:close', modalView);
                                                that.view.triggerMethod('FileAdded', formData, result, orientationFormat);
                                             })
                                             .fail(function() {
                                                valamisApp.execute('notify', 'error', Valamis.language['failedUploadLabel']);
                                                valamisApp.execute('modal:close', modalView);
                                             });

                            valamisApp.execute('modal:close', this);
                            that.view.$('.js-select-google-file').hide();
                            that.view.$('.js-upload-hint-label').addClass('hidden');
                            that.view.$('.js-processing-hint-label').removeClass('hidden');
                            that.view.$('.js-converting-hint-label').toggleClass('hidden', contentType == 'pdf');
                        });
                        valamisApp.execute('modal:show', modalView);
                    }
                });
            }
            else {
                uploader.on('fileuploadadd', function (file, data) {
                    that.options.fileuploadaddCallback(that, file, data);
                    that.view.$('.js-select-google-file').hide();
                });
                uploader.on('fileuploaddone', function (result) {
                    that.options.fileuploaddoneCallback(result, uploader, that);
                });
            }
        }

        var showModal = !(that.options.fileUploaderLayout);

        if (showModal) {
            var imageUploaderModalView = new valamisApp.Views.ModalView({
                contentView: uploader,
                header: that.options.fileUploadModalHeader,
                onDestroy: function(){
                    that.view.trigger('element-modal:close');
                }
            });

            uploader.on('fileupload:done', function (result) {
                that.onImageUploaded(imageModel, result);
                valamisApp.execute('modal:close', imageUploaderModalView);
            });

            valamisApp.execute('modal:show', imageUploaderModalView);
        }
        else {
            that.$(that.options.fileUploaderLayout).html(uploader.render().$el);

            uploader.on('fileupload:done', function (result) {
                that.onImageUploaded(imageModel, result);
            });
        }
    },
    onImageUploaded: function(imageModel, result){
        imageModel.logo = result.name;
        imageModel.logoSrc = result.src;

        if (imageModel.logo) this.view.model.set(this.options.imageAttribute, imageModel.logo);
        this.view.model.set('logoSrc', imageModel.logoSrc);
    },
    onSubmit: function(){

    },
    selectImage: function(that) {
        var postponeLoading = that.options.postponeLoading;

        var galleryView = new GalleryContainer({
            language: Valamis.language,
            folderID: that.options.getFolderId(that.view.model),
            saveToFileStorage: !that.logoData.supports()
        });

        var galleryModalView = new valamisApp.Views.ModalView({
            contentView: galleryView,
            submitEl: '.js-save-package',
            header: Valamis.language['selectImageModalHeader'],
            onDestroy: function(){
                that.view.trigger('element-modal:close');
            }
        });

        galleryView.on('savedLogo', function (data) {
            if(that.options.autoUpload(that.view.model))
                uploadMediaGalleryImage(data);
            // For Lesson Studio uploading needs to be postponed
            // and triggered only when the corresponding model is saved
            else {
                var src = "/documents/" + Utils.getCourseId() + "/"
                    + data.get('folderId') + "/"
                    + data.get('title') + "/?version="
                    + data.get('version');
                that.view.triggerMethod('FileAdded', src, data);
                that.view.off('mediagallery:image:upload').on('mediagallery:image:upload', function (data, callback) {
                    uploadMediaGalleryImage(data);
                    if(typeof callback === 'function')
                        callback();
                });
            }

            function uploadMediaGalleryImage(data) {
                var imgdata = {};
                if (postponeLoading && that.logoData.supports()) {
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.CONTENT_TYPE, 'document-library');
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.FILE_ENTRY_ID, data.get('id'));
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.FILE, data.get('title'));
                    that.logoData.setSetting(IMAGE_PARAM_TYPE.FILE_VERSION, data.get('version'));

                    imgdata.src = "/documents/" + Utils.getCourseId() + "/"
                        + data.get('folderId') + "/"
                        + data.get('title') + "/?version="
                        + data.get('version') + "&imageThumbnail=1";
                    imgdata.fileName = data.get('fileName') || data.get('title');

                    that.onImageGalleryUploaded(galleryModalView, imgdata);
                } else { // old browsers, image saved immediately in GalleryContainer
                    // Using mimeToExt object from lesson-studio/helper.js to add extension to the filename
                    if (typeof Utils.mimeToExt === 'object') {
                        var fileExt = Utils.getExtByMime ? Utils.getExtByMime(data.get('mimeType')) : null;
                        data.set({
                            title: fileExt
                                ? data.get('title') + '.' + fileExt
                                : data.get('title')
                        });
                    }
                    imgdata.fileName = data.get('title');

                    var folderId = that.options.getFolderId(that.view.model);
                    var endpointparam = {
                        action: 'ADD',
                        courseId: Utils.getCourseId(),
                        contentType: 'document-library',
                        folderId: folderId
                    };

                    var formData = {
                        'contentType': 'document-library',
                        'fileEntryID': data.get('id'),
                        'file': data.get('title'),
                        'fileVersion': data.get('version'),
                        'p_auth': Liferay.authToken
                    };

                    var portletFileUploaderUrl = that.options.getFileUploaderUrl(that.view.model);
                    var fileUploaderUrl = (portletFileUploaderUrl || path.root + path.api.files) + "?" + jQueryValamis.param(endpointparam);

                    jQueryValamis.ajax({
                        url: fileUploaderUrl,
                        type: "POST",
                        data: formData,
                        headers: {
                            'X-CSRF-Token': Liferay.authToken
                        },
                        success: function () {
                            that.onImageGalleryUploaded(galleryModalView, imgdata);
                        }
                    })
                }
            }
        });

        valamisApp.execute('modal:show', galleryModalView);
    },
    selectVideo: function (that) {
        var videoModel = new Backbone.Model();
        var videoModalView = new VideoModal({
            model: videoModel,
            onDestroy: function(){
                that.view.trigger('element-modal:close');
            }
        });

        videoModalView.on('video:added', function (data) {
            var src = '/documents/' + Utils.getCourseId() + '/0/' +
                    data.get('title') + '/' + data.get('uuid') +
                    '?groupId=' + data.get('groupID') +
                    '&ext=' + Utils.getExtByMime(data.get('mimeType'));
            that.onImageGalleryUploaded(videoModalView, { src: src, fileName: src });
        });
        valamisApp.execute('modal:show', videoModalView);
        // Remove the "Embed YouTube video" row because there is a YT search already
        // Also remove the "Title" row because it is not used (could be used later to create a video caption (text element))
        // Hide radio buttons because there will be just 1 left
        videoModalView.$el.find('#EMBED').closest('tr').remove();
        videoModalView.$el.find('.js-title-edit').closest('tr').remove();
        videoModalView.$el.find('.radio').hide();
    },
    onImageGalleryUploaded: function(galleryModalView, imgdata) {
        this.view.model.set(this.options.imageAttribute, imgdata.fileName);
        this.view.model.set('logoSrc', imgdata.src);

        valamisApp.execute('modal:close', galleryModalView);
    }
});