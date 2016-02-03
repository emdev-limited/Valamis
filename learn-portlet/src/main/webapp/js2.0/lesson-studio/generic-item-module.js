var GenericEditorItemModule = Marionette.Module.extend({
    Model: lessonStudio.Entities.LessonPageElementModel,

    BaseView: Marionette.ItemView.extend({
        className: 'rj-element',
        id: 'slide-entity-' + (Marionette.ItemView.Registry.size() + 1),
        events: {
            'mousedown .item-content': 'onMouseDown',
            'click .js-item-delete': 'deleteEl',
            'click .js-item-duplicate': 'duplicateEl',
            'click .js-item-link': 'linkUpdate',
            'click .js-item-link-incorrect': 'linkUpdate',
            'click .js-item-forward': 'zIndexChange',
            'click .js-item-backward': 'zIndexChange',
            'click .js-item-open-settings': 'openItemSettings',
            'click .js-item-close-settings': 'closeItemSettings',
            'click .js-item-hide-for-device': 'hideForDeviceToggle'
        },
        modelEvents: {
            'change:contentFileName': 'onContentFileNameChange',
            'change:content': 'onChangeContent',
            'sync': 'onModelSync'
        },
        behaviors: {
            ImageUpload: {
                'postponeLoading': false,
                'autoUpload': function() { return false; },
                'getFolderId': function (model) { return 'slide_item_' + model.id; },
                'uploadLogoMessage' : function(model) {
                    return model.get('slideEntityType') === 'image'
                        ? Valamis.language['uploadLogoMessage']
                        : Valamis.language['uploadWebglMessage'];
                },
                'fileUploadModalHeader' : function() { return Valamis.language['fileUploadModalHeader']; },
                'selectImageModalHeader': function() { return Valamis.language['galleryLabel']; },
                'imageAttribute': 'contentFileName',
                'onBeforeInit': function(model, context, callback) {
                    if(typeof callback === 'function')
                        callback(context);
                },
                'fileuploaddoneCallback': function() {},
                'fileuploadaddCallback': function(context, file, data) {
                    context.view.$('.js-select-google-file').hide();
                    context.view.triggerMethod('FileAdded', file, data);
                },
                'acceptFileTypes': function(model) {
                    return '(' +
                        _.reduce(getMimeTypeGroupValues(model.get('slideEntityType')), function(a, b) {
                            return a + ')|(' + b;
                        }) + ')';
                },
                'fileRejectCallback': function(context) {
                    slidesApp.execute('action:undo');
                    context.uploadImage(context);
                }
            }
        },
        onModelSync: function (newModel) {
            if (this.model.get('tempId')){
                Marionette.ItemView.Registry
                    .update(this.model.get('tempId'), this.model.get('id'));
                jQueryValamis('div#slideEntity_' + this.model.get('tempId'))
                    .attr('id', 'slideEntity_' + this.model.get('id'));
                this.model.unset('tempId');
            }
        },
        onContentFileNameChange: function() {
            var contentFileName = this.model.get('contentFileName').replace(/watch\?v=/g, 'embed/');
            var oldContent = this.model.get('content');
            this.model.set({ content: contentFileName });
            this.updateUrl(contentFileName, oldContent);
        },
        onChangeContent: function(){
            setTimeout(this.updateControlsPosition.bind(this), 100);
        },
        onFileAdded: function(file, data) {
            var fileUrl = (typeof file === 'string')
                ? file
                : createObjectURL(file);
            this.model.set('file', file);
            this.model.set('fileUrl', fileUrl);
            this.model.set('fileModel', data);

            if(typeof file === 'object')
                this.model.set('formData', data);

            var oldContent = this.model.get('content');
            this.model.set('content', fileUrl);
            this.updateUrl(fileUrl, oldContent);
            valamisApp.execute('modal:clear');
        },
        initialize: function(options) {
            this.model.set('googleClientApiAvailable', lessonStudio.googleClientApiReady);
            this.listenTo(this.model, 'change', this.updateEl);
            Marionette.ItemView.prototype.initialize.apply(this);
            this.cid = options.id || this.cid;
            Marionette.ItemView.Registry.register(this.model.get('id') || this.model.get('tempId'), this);

            var content = this.model.get('content');
            // Chrome (and Opera) needs escaped colons in object URLs for some reason
            if(this.model.get('content').indexOf('blob:') != -1 && navigator.sayswho[0].toLowerCase() === 'chrome')
                content = content.substring(0, content.indexOf('blob:') + 5) +
                    content.substring(content.indexOf('blob:') + 5).replace(/:/g, window.escape(':'));
            this.model.set('content', content);
        },
        selectEl: function() {
            slidesApp.selectedItemView = this;
            slidesApp.execute('item:focus', this);
        },
        updateEl: function() {
            if(!_.contains(['question','plaintext'], this.model.get('slideEntityType'))){
                this.$el.css('width', this.model.get('width') + 'px');
                this.$el.css('height', this.model.get('height') + 'px');
            }
            this.$el.css('top', this.model.get('top') + 'px');
            this.$el.css('left', this.model.get('left') + 'px');
            this.$el.attr('id').replace(/_.*/, '_' + this.model.get('id') || this.model.get('tempId'));
            this.$('.item-content').css('z-index', this.model.get('zIndex'));
            this.$('.item-content').css('font-size', this.model.get('fontSize'));
            if(this.model.get('content') === '' && this.model.get('slideEntityType') !== 'text') {
                this.content.css('background-color', '#1C1C1C');
                this.content.css('background-image', '');
                this.$('iframe').hide();
                this.$('.video-js').hide();
                this.$('[class*="content-icon-' + this.model.get('slideEntityType') + '"]').first().show();
                this.$('div[class*="content-icon-' + this.model.get('slideEntityType') + '"]')
                    .css('font-size', Math.min(this.model.get('width') / 2, this.model.get('height') / 2) + 'px');
            }
        },
        onRender: function() {
            this.content = this.$('.item-content');
            this.controls = this.$('.item-controls');
            this.resizeControls = this.$('.item-border');
            if (!_.contains(['question','plaintext', 'content', 'randomquestion'], this.model.get('slideEntityType'))) {
                this.resizableInit();
            }
            this.$('.valamis-tooltip')
                .tooltip({
                    container: this.controls,
                    placement: function(){
                        var offset = this.$element.closest('.item-controls').offset();
                        return jQueryValamis(window).width() - offset.left < 150
                            ? 'left'
                            : 'right';
                    }
                })
                .on('inserted.bs.tooltip', function () {
                    jQueryValamis(this).data('bs.tooltip').$tip
                        .css({
                            whiteSpace: 'nowrap'
                        });
                })
                .bind('click',function(){
                    jQueryValamis(this).data('bs.tooltip').$tip.remove();
                });
            this.updateEl();
            this.applyLinkedType('correctLinkedSlideId');
            this.applyLinkedType('incorrectLinkedSlideId');
        },
        resizableInit: function(){
            var self = this;
            this.$el.resizable({
                resize: function (event, ui) {
                    var direction = jQueryValamis(this).data('ui-resizable').axis;
                    // Keep aspect ratio if resized with corner handles, don't keep otherwise.
                    var aspectRatio = false,
                        isHidden = !!self.model.get('classHidden');
                    if (self.model.get('slideEntityType') === 'image') {
                        aspectRatio = direction.length !== 1;
                        jQueryValamis(this).resizable("option", "aspectRatio", 'se').data('uiResizable')._aspectRatio = aspectRatio;
                    }

                    if(!isHidden) {
                        //Sizes
                        slidesApp.GridSnapModule.snapSize(direction, ui.size, ui.originalPosition, aspectRatio);
                    }

                    if (self.model.get('slideEntityType') == 'text' && slidesApp.isEditing) {
                        return;
                    }

                    //Positions
                    switch (direction) {
                        case 'nw':
                            var posSideTop = ui.originalPosition.top + ui.originalSize.height - ui.size.height;
                            var posSideLeft = ui.originalPosition.left + ui.originalSize.width - ui.size.width;
                            if(!isHidden) {
                                posSideTop = slidesApp.GridSnapModule.snapTopResize(posSideTop, self.model.get('top'), ui.size);
                                posSideLeft = slidesApp.GridSnapModule.snapLeftResize(posSideLeft, self.model.get('left'), ui.size);
                            }
                            self.model.set({
                                'top': posSideTop,
                                'left': posSideLeft
                            });
                            break;
                        case 'n':
                        case 'ne':
                            var posSideTop = ui.originalPosition.top + ui.originalSize.height - ui.size.height;
                            if(!isHidden) {
                                posSideTop = slidesApp.GridSnapModule.snapTopResize(posSideTop, self.model.get('top'), ui.size);
                            }
                            self.model.set('top', posSideTop);
                            break;
                        case 'w':
                        case 'sw':
                            var posSideLeft = ui.originalPosition.left + ui.originalSize.width - ui.size.width;
                            if(!isHidden) {
                                posSideLeft = slidesApp.GridSnapModule.snapLeftResize(posSideLeft, self.model.get('left'), ui.size);
                            }
                            self.model.set('left', posSideLeft);
                            break;
                        default:
                            break;
                    }

                    slidesApp.execute('item:resize', ui.size.width, ui.size.height, self);
                    self.updateEl();
                    self.updateControlsPosition();
                },
                start: function (event, ui) {
                    if (!slidesApp.activeElement.view)
                        slidesApp.execute('item:focus', self);
                    slidesApp.execute('resize:prepare', self);
                    if(!self.model.get('classHidden')) {
                        slidesApp.GridSnapModule.prepareItemsSnap();
                    }
                },
                stop: function (event, ui) {
                    slidesApp.newValue = {
                        'top': self.model.get('top'),
                        'left': self.model.get('left'),
                        'width': self.model.get('width'),
                        'height': self.model.get('height')
                    };
                    slidesApp.execute('action:push');
                    slidesApp.activeElement.isResizing = false;
                    if (self.model.get('slideEntityType') == 'text') {
                        self.wrapperUpdate(!slidesApp.isEditing);
                    }
                    self.trigger('resize:stop');
                },
                handles: {
                    'n': '.ui-resizable-n',
                    'e': '.ui-resizable-e',
                    's': '.ui-resizable-s',
                    'w': '.ui-resizable-w',
                    'ne': '.ui-resizable-ne',
                    'se': '.ui-resizable-se',
                    'sw': '.ui-resizable-sw',
                    'nw': '.ui-resizable-nw'
                }
            });
        },
        onMouseDown: function(e) {
            if(slidesApp.mode === 'edit' && !slidesApp.isEditing) {
                slidesApp.selectedItemView = this;
                var offsetX = e.pageX - this.$el.offset().left;
                var offsetY = e.pageY - this.$el.offset().top;
                if (this.$('.iframe-edit-panel').is(':hidden') || this.$('.iframe-edit-panel').length === 0){
                    e.preventDefault();
                    slidesApp.execute('drag:prepare:existing', this, e.clientX, e.clientY, offsetX, offsetY);
                }
            }
        },
        duplicateEl: function() {
            slidesApp.execute('item:duplicate', this);
        },
        deleteEl: function(e, isUndoAction) {
            slidesApp.selectedItemView = this;
            slidesApp.execute('item:delete', isUndoAction);
        },
        getContentHeight: function(){
            var content = this.$('.item-content');
            var realHeight = Math.round(content.css('height','auto').innerHeight());
            content.css('height',''); //remove height style (return to default)
            return realHeight;
        },
        getContentWidth: function(){
            var content = this.$('.item-content');
            var realWidth = Math.round(content.css('width','auto').innerWidth());
            content.css('width',''); //remove width style (return to default)
            return realWidth;
        },
        wrapperUpdate: function( update ){
            if( typeof update == 'undefined' ){
                update = true;
            }
            var height = Math.round(this.$el.innerHeight());
            var realHeight = this.getContentHeight();
            if( height > realHeight && (this.model.get('slideEntityType') != 'question'  && this.model.get('slideEntityType') != 'plaintext')){
                if( !update ){ return; }
                realHeight = height;
            }
            this.$el.css('height', realHeight);
            if( update ) {
                slidesApp.execute('item:resize', this.$el.width(), realHeight, this);
                //slidesApp.actionStack.pop();
            }
        },
        applyLinkedType: function(linkTypeName) {

            this.$el.toggleClass( 'linked', ( this.model.get('correctLinkedSlideId') || this.model.get('incorrectLinkedSlideId') ) );
            var button = this.controls.find('.js-item-link');

            //question
            if (_.contains(['question','plaintext','randomquestion'], this.model.get('slideEntityType'))) {

                var button_incorrect = this.controls.find('.js-item-link-incorrect');

                //correct link
                if( this.model.get('correctLinkedSlideId') && !button.is('.active-button') ){
                    button
                        .attr( 'title',
                        Valamis.language['valBadgeRemoveLink']
                        + ' (' + slidesApp.getSlideModel(parseInt(this.model.get('correctLinkedSlideId'))).get('title') + ')'
                    )
                        .tooltip('fixTitle');
                    button.html(Valamis.language['valBadgeRemoveLink']);
                }
                else if( !this.model.get('correctLinkedSlideId') && button.is('.active-button') ){
                    button
                        .attr( 'title', Valamis.language['valBadgeLinkToCorrectAnswer'] )
                        .tooltip('fixTitle').tooltip('show');
                    button.html(Valamis.language['valBadgeSelectPage']);
                }

                //incorrect link
                if( this.model.get('incorrectLinkedSlideId') && !button_incorrect.is('.active-button') ){
                    button_incorrect
                        .attr( 'title',
                        Valamis.language['valBadgeRemoveLink']
                        + ' (' + slidesApp.getSlideModel(parseInt(this.model.get('incorrectLinkedSlideId'))).get('title') + ')'
                    )
                        .tooltip('fixTitle');
                    button_incorrect.html(Valamis.language['valBadgeRemoveLink']);
                }
                else if( !this.model.get('incorrectLinkedSlideId') && button_incorrect.is('.active-button') ){
                    button_incorrect
                        .attr( 'title', Valamis.language['valBadgeLinkToIncorrectAnswer'] )
                        .tooltip('fixTitle').tooltip('show');
                    button_incorrect.html(Valamis.language['valBadgeSelectPage']);
                }

            }
            //text or image
            else {

                if( this.model.get('correctLinkedSlideId') && !button.is('.active-button') ){
                    button
                        .attr( 'title',
                            Valamis.language['valBadgeRemoveLink']
                            + ' (' + slidesApp.getSlideModel(parseInt(this.model.get('correctLinkedSlideId'))).get('title') + ')'
                        )
                        .tooltip('fixTitle');
                    button.html(Valamis.language['valBadgeRemoveLink']);
                }
                else if( !this.model.get('correctLinkedSlideId') && button.is('.active-button') ){
                    button
                        .attr( 'title', Valamis.language['valBadgeLinkToAnotherSlide'] )
                        .tooltip('fixTitle').tooltip('show');
                    button.html(Valamis.language['valBadgeSelectPage']);
                }

            }

            this.controls.find('.js-item-link')
                .toggleClass('active-button', !!this.model.get('correctLinkedSlideId'));
            this.controls.find('.js-item-link-incorrect')
                .toggleClass('active-button', !!this.model.get('incorrectLinkedSlideId'));

            var slideThumbnail = this.controls.find('button.' + linkTypeName.substr(0, linkTypeName.indexOf('LinkedSlideId')) + ' > .linked-slide-thumbnail');
            slideThumbnail.html('');
            slideThumbnail.css({
                'background-color': 'transparent',
                'background-image': ''
            });
            slideThumbnail.removeClass('slide-thumbnail-bordered');
        },
        goToSlideActionInit: function() {
            var self = this;
            if( _.indexOf(['text','image'], this.model.get('slideEntityType')) > -1 && this.model.get('correctLinkedSlideId') ) {
                this.$el.bind('click', {slideId: this.model.get('correctLinkedSlideId')}, self.goToSlideAction);
            }
        },
        goToSlideAction: function(e) {
            if(e.data && e.data.slideId ) {
                var slideIndices = slidesApp.slideRegistry.getBySlideId(e.data.slideId);
                Reveal.slide(slideIndices.h, slideIndices.v);
            }
        },
        goToSlideActionDestroy: function() {
            var self = this;
            this.$el.unbind('click',self.goToSlideAction);
        },
        linkUpdate: function(e, linkType) {
            var linkTypeName =
                linkType || jQueryValamis(e.target).closest('button').is('.js-item-link') ? 'correctLinkedSlideId' : 'incorrectLinkedSlideId';
            slidesApp.viewId = this.model.get('id') || this.model.get('tempId');
            slidesApp.actionType = 'itemLinkedSlideChanged';
            slidesApp.oldValue = { linkType: linkTypeName, linkedSlideId: this.model.get(linkTypeName) };
            if(this.model.get(linkTypeName)){
                this.model.set(linkTypeName, null);
                this.applyLinkedType(linkTypeName);
                slidesApp.newValue = { linkType: linkTypeName, linkedSlideId: this.model.get(linkTypeName) };
                slidesApp.execute('action:push');
            } else {
                slidesApp.execute('linkUpdate', linkTypeName);
            }
        },
        openItemSettings: function (e) {
            if (!this.$('.item-settings').is(':hidden')) return;
            this.$('.js-item-notify-correct').attr('checked', this.model.get('notifyCorrectAnswer'));
            slidesApp.isEditing = true;
            if (this.model.get('slideEntityType') == 'randomquestion')
                this.$('.js-question-link').addClass('hidden');
            else this.$('.js-question-link').removeClass('hidden');
            this.$('.item-settings').show();
            this.updateControlsPosition();
            this.updateControlsScroll();
        },
        closeItemSettings: function () {
            this.$('.item-settings').hide();
            placeSlideControls();
            this.updateControlsPosition();
            slidesApp.isEditing = false;
        },
        zIndexChange: function(e){
            e.preventDefault();
            var model = this.model,
                direction = jQueryValamis(e.target).closest('button').is('.js-item-forward')
                    ? 'forward'
                    : 'backward',
                slideIdCurrent = slidesApp.activeSlideModel.get('id') || slidesApp.activeSlideModel.get('tempId');

            var siblingElements = slidesApp.slideElementCollection.where({
                slideId: slideIdCurrent,
                toBeRemoved: false
            });
            if( siblingElements.length > 0 ){
                var zIndexArr = _.range(1, siblingElements.length + 1);
                siblingElements = _.sortBy(siblingElements, function(item){
                    return item.get('zIndex');
                });
                var currentElIndex = _.findIndex(siblingElements, function(item){
                    return item.get('id')
                        ? item.get('id') == model.get('id')
                        : item.get('tempId') == model.get('tempId');
                });
                if( direction == 'forward' ){
                    var zIndexNext = currentElIndex + 1 < siblingElements.length
                        ? zIndexArr[ currentElIndex + 1 ]
                        : null;
                } else {
                    var zIndexNext = currentElIndex > 0
                        ? zIndexArr[ currentElIndex - 1 ]
                        : null;
                }
                if( zIndexNext ){
                    model.set( 'zIndex', zIndexNext );
                    siblingElements.splice(currentElIndex, 1);
                    zIndexArr.splice(zIndexArr.indexOf(zIndexNext), 1);
                    _.each(siblingElements, function(item, i){
                        item.set( 'zIndex', zIndexArr[i] );
                    });
                }
            }
        },
        updateControlsPosition: function(){
            var atPos = 'right top';
            if( slidesApp.isEditing ) {
                //store controls position for scrolling
                if( !this.$el.data('offset') ){
                    var offset = this.$el.offset()['top'] - this.controls.offset()['top'];
                    this.$el.data('offset', offset);
                } else {
                    var offset = this.$el.data('offset');
                }
                if(offset) atPos += '-' +  offset;
            } else {
                this.$el.removeData('offset');
            }
            var editorArea = slidesApp.getRegion('editorArea').$el,
                editorAreaClientRect = slidesApp.getRegion('editorArea').$el.get(0).getBoundingClientRect(),
                elementClientRect = this.$el.get(0).getBoundingClientRect(),
                rightOffset = editorAreaClientRect.right - elementClientRect.right;
            this.controls
                .position({
                    my: 'left+' + (Math.min(0, rightOffset) + 10) + ' top',
                    at: atPos,
                    of: this.$el,
                    collision: 'fit',
                    within: '.slides-editor-main-wrapper'
                });

            revealControlsModule.view.ui.button_add_page_right
                .toggle( rightOffset - lessonStudio.fixedSizes.ELEMENT_CONTROLS_WIDTH > 0 );
        },
        updateControlsScroll: function(){
            var editorArea = slidesApp.getRegion('editorArea').$el,
                wrapper = editorArea.closest('.slides-editor-main-wrapper'),
                workArea = editorArea.closest('.slides-work-area-wrapper'),
                scrollTop = jQueryValamis( document ).scrollTop(),
                innerScroll = wrapper.scrollTop(),
                workAreaOffsetTop = ((workArea.offset()['top'] - scrollTop) + innerScroll)
                    - lessonStudio.fixedSizes.TOPBAR_HEIGHT,
                workAreaBottomPos = workAreaOffsetTop + workArea.height(),
                bottomPos = ((this.$('.item-settings').offset()['top'] - scrollTop) + innerScroll)
                    + this.$('.item-settings').height();
            if( bottomPos > wrapper.height() ){
                workArea.css({
                    position: 'relative',
                    marginTop: Math.max(workAreaOffsetTop, 0),
                    marginBottom: (bottomPos - workAreaBottomPos) + 10
                });
                wrapper
                    .animate({
                        scrollTop: wrapper.get(0).scrollHeight
                    }, 500);
            }

            if( workAreaBottomPos < bottomPos ){
                slidesApp.getRegion('editorArea').$el
                    .parent()
                    .find('.layout-resizable-handle')
                    .add( revealControlsModule.view.ui.button_add_page_down )
                    .toggleClass('hidden', true);
            }
        },
        hideForDeviceToggle: function(e){
            e.preventDefault();
            var oldValue = this.model.get('classHidden'),
                newValue = !oldValue ? 'hidden' : '';
            this.model.set('classHidden', newValue);
            if(!slidesApp.isUndoAction && !slidesApp.initializing) {
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'changeModelAttribute';
                slidesApp.oldValue = {classHidden: oldValue};
                slidesApp.newValue = {classHidden: newValue};
                slidesApp.slideId = null;
                slidesApp.execute('action:push');
            }
        },
        hideForDeviceApply: function(){
            var classHidden = this.model.get('classHidden');
            if(classHidden){
                slidesApp.execute('item:blur');
            }
            this.$el.toggleClass('hidden-element', !!this.model.get('classHidden'));
            this.$('.js-item-hide-for-device')
                .attr( 'title',
                    classHidden
                        ? Valamis.language['valItemShowForDeviceLabel']
                        : Valamis.language['valItemHideForDeviceLabel']
                )
                .tooltip('fixTitle')
                .blur();
        }
    })
});