var GenericEditorItemModule = Marionette.Module.extend({
    Model: lessonStudio.Entities.LessonPageElementModel,

    BaseView: Marionette.ItemView.extend({
        className: 'rj-element',
        id: 'slide-entity-' + (Marionette.ItemView.Registry.size() + 1),
        elementSettings: { attrs: {}, properties: {} },
        ui: {
            'content': '.item-content',
            'controls': '.item-controls',
            'border': '.item-border',
            'settings_panel': '.js-item-settings'
        },
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
            'change:classHidden': 'hideForDeviceApply',
            'change:correctLinkedSlideId': 'applyLinkedType',
            'change:incorrectLinkedSlideId': 'applyLinkedType',
            'sync': 'onModelSync',
            'remove': 'destroy',
            'change:toBeRemoved': function(model, value){
                slidesApp.execute('item:delete', this);
            },
            'change:fromHistory': 'onChangeFromHistory'
        },
        behaviors: {
            ValamisUIControls: {},
            ImageUpload: {
                'postponeLoading': false,
                'autoUpload': function() { return false; },
                'getFolderId': function (model) {
                    return (model.get('slideEntityType') == 'pdf'
                    ? 'slideData'+ model.id
                    : 'slide_item_' + model.id);
                },
                'getFileUploaderUrl': function (model) {
                    return path.root + path.api.files + 'slide-element/' + model.get('id') + '/file';
                },
                'uploadLogoMessage' : function(model) {
                    var elementType = model.get('slideEntityType');
                    if (elementType === 'image')
                        return Valamis.language['uploadLogoMessage'];
                    else if (elementType === 'webgl')
                        return Valamis.language['uploadWebglMessage'];
                    else
                        return Valamis.language['uploadAudioMessage']
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
                        _.reduce(Utils.getMimeTypeGroupValues(model.get('slideEntityType')), function(a, b) {
                            return a + ')|(' + b;
                        }) + ')';
                },
                'fileRejectCallback': function(context) {
                    slidesApp.execute('action:undo');
                    context.uploadImage(context);
                },
                'contentType': function (model) {
                    var elementType = model.get('slideEntityType');
                    if (elementType === 'webgl')
                        return 'webgl';
                    else if (elementType === 'audio')
                        return 'audio';
                    else
                        return 'icon';
                }
            }
        },
        onChangeFromHistory: function(){},//should to be overridden
        onModelSync: function (newModel) {
            if (this.model.get('tempId')){
                Marionette.ItemView.Registry
                    .update(this.model.get('tempId'), this.model.get('id'));
                jQueryValamis(lessonStudio.slidesWrapper + ' div#slideEntity_' + this.model.get('tempId'))
                    .attr('id', 'slideEntity_' + this.model.get('id'));
                this.model.unset('tempId');
            }
        },
        onContentFileNameChange: function(){},//overridden in videoElement
        onChangeContent: function(){
            setTimeout(this.updateControlsPosition.bind(this), 100);
        },
        onFileAdded: function(file, data) {
            slidesApp.historyManager.groupOpenNext();
            slidesApp.setFileContent(this, file, data);
            
            var moduleName = slidesApp.getModuleName(this.model.get('slideEntityType'));
            if (moduleName !== slidesApp.ImageElementModule.moduleName) {
                //for image elements history group is closed in updateUrl function
                slidesApp.historyManager.groupClose();
            }
            
            valamisApp.execute('modal:clear');
        },
        initialize: function(options) {
            this.model.set('googleClientApiAvailable', lessonStudio.googleClientApiReady && lessonStudio.googleApiConfigured);
            Marionette.ItemView.prototype.initialize.apply(this);
            this.cid = options.id || this.cid;
            Marionette.ItemView.Registry.register(this.model.getId(), this);

            var content = this.model.get('content');
            this.model.set('content', content);
            this.model.on('change', this.updateEl, this);//Do not move to "modelEvents"
            this.on('element-modal:open', this.onModalOpen);
            this.on('element-modal:close', this.onModalClose);
        },
        selectEl: function() {
            slidesApp.selectedItemView = this;
            slidesApp.execute('item:focus', this);
        },
        updateEl: function() {
            this.$el.css('width', this.model.get('width'));
            this.$el.css('height', this.model.get('height'));
            this.$el.css('top', this.model.get('top'));
            this.$el.css('left', this.model.get('left'));
            this.$el.attr('id').replace(/_.*/, '_' + this.model.getId());
            if (this.model.has('min-height')) {
                this.$el.css('min-height', this.model.get('min-height'));
            }
            this.ui.content.css('z-index', this.model.get('zIndex'));
            // update resizable handles with element: otherwise can't resize element
            // with bigger z-index in case of overlapping elements
            this.$('.ui-resizable-handle').css('z-index', this.model.get('zIndex'));

            this.ui.content.css('font-size', this.model.get('fontSize'));
            if(this.model.get('content') === '' &&
                !_.contains(['text','content', 'question', 'plaintext', 'randomquestion'], this.model.get('slideEntityType'))) {
                this.ui.content.css('background-color', '#1C1C1C');
                this.ui.content.css('background-image', '');
                this.$('iframe').hide();
                this.$('.video-js').hide();
                this.$('[class*="content-icon-' + this.model.get('slideEntityType') + '"]').first().show();
                this.$('div[class*="content-icon-' + this.model.get('slideEntityType') + '"]')
                    .css('font-size', Math.min(this.model.get('width') / 2, this.model.get('height') / 2) + 'px');
            }
            if (_.contains(['content', 'audio'], this.model.get('slideEntityType')) || !(this.model.get('content') === '')){
                if (!_.contains(['question', 'plaintext'], this.model.get('slideEntityType'))
                    || !this.ui.content.find('.removed-question').length){
                    this.ui.content.css('background-color', 'transparent');
                }
            }
            if (this.model.get('slideEntityType') == 'randomquestion') {
                this.ui.content.css('background-color', '');
            }
            this.$el.toggleClass('active', this.model.get('active') || this.model.get('selected'));
            this.ui.border.toggle((this.model.get('active') || this.model.get('selected')));
            // base has #lesson-summary-header and #lesson-summary-table for summary page elements
            this.ui.controls.toggle(this.model.get('active') && !this.model.get('selected') && this.$el.find('[id^=lesson-summary-]').length == 0);
            if (this.ui.content.find('#lesson-summary-table').html() == '') {
                this.ui.content.addClass('lesson-summary-block');
            }
            this.hideForDeviceApply(true);
            this.updateControlsPosition();
        },
        onRender: function() {
            this.bindUIElements();
            this.content = this.$('.item-content');
            this.controls = this.$('.item-controls');
            if(slidesApp.mode == 'edit' && this.$('#lesson-summary-table').length == 0){
                this.resizableInit();
                this.draggableInit();
            }
            this.$('.valamis-tooltip')
                .tooltip({
                    container: this.controls,
                    placement: function(){
                        var offset = this.$element.closest('.item-controls').offset(),
                            placement = this.$element.data('placement') || 'right';
                        return jQueryValamis(window).width() - offset.left < 150
                            ? 'left'
                            : placement;
                    },
                    trigger: 'hover'
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

            this.$('div[class*="content-icon-"]').css('font-size', Math.min(this.model.get('width') / 2, this.model.get('height') / 2) + 'px');

            this.updateEl();
        },
        resizableInit: function(){
            var that = this, isHidden, aspectRatio, direction;
            var handles = {};
            if (that.model.get('slideEntityType') == 'text') {
                handles = {
                    'e': '.ui-resizable-e',
                    'w': '.ui-resizable-w',
                };
            } else {
                handles = {
                    'n': '.ui-resizable-n',
                    'e': '.ui-resizable-e',
                    's': '.ui-resizable-s',
                    'w': '.ui-resizable-w',
                    'ne': '.ui-resizable-ne',
                    'se': '.ui-resizable-se',
                    'sw': '.ui-resizable-sw',
                    'nw': '.ui-resizable-nw'
                };
            }
            this.$el.resizable({
                handles: handles,
                start: function (event, ui) {
                    direction = jQueryValamis(this).data('ui-resizable').axis;
                    isHidden = !!that.model.get('classHidden');

                    // Keep aspect ratio if resized with corner handles, don't keep otherwise.
                    aspectRatio = false;

                    if (!slidesApp.activeElement.view)
                        slidesApp.execute('item:focus', that);
                    slidesApp.execute('resize:prepare', that);
                    if(!that.model.get('classHidden')) {
                        slidesApp.gridSnapModule.prepareItemsSnap();
                    }
                },
                resize: function (event, ui) {

                    if (that.model.get('slideEntityType') == 'text' && slidesApp.isEditing) {
                        return;
                    }
                    //Snap sizes
                    if(!isHidden) {
                        slidesApp.gridSnapModule.snapSize(direction, ui.position, ui.size, ui.originalSize, aspectRatio);
                    }

                    //Snap positions
                    if(!isHidden) {
                        switch (direction) {
                            case 'nw':
                                slidesApp.gridSnapModule.snapTopResize(direction, ui.position, ui.size, ui.originalSize, aspectRatio);
                                slidesApp.gridSnapModule.snapLeftResize(direction, ui.position, ui.size, ui.originalSize, aspectRatio);
                                break;
                            case 'n':
                            case 'ne':
                                slidesApp.gridSnapModule.snapTopResize(direction, ui.position, ui.size, ui.originalSize, aspectRatio);
                                break;
                            case 'w':
                            case 'sw':
                                slidesApp.gridSnapModule.snapLeftResize(direction, ui.position, ui.size, ui.originalSize, aspectRatio);
                                break;
                            default:
                                break;
                        }
                    }
                    slidesApp.execute('item:resize', ui.size.width, ui.size.height, that);
                    that.updateControlsPosition();
                },
                stop: function (event, ui) {
                    that.model.updateProperties({
                        left: Math.round(ui.position.left),
                        top: Math.round(ui.position.top),
                        width: Math.round(ui.size.width),
                        height: Math.round(ui.size.height)
                    });
                    slidesApp.activeElement.isResizing = false;
                    that.trigger('resize:stop');
                    slidesApp.gridSnapModule.removeLines();
                    slidesApp.vent.trigger('elementsUpdated');
                }
            });
        },
        draggableInit: function(){
            var that = this, group;
            var currentModelId = this.model.getId();
            this.$el
                .draggable({
                    start: function(){
                        var selectedElements = slidesApp.activeSlideModel.getElements({selected: true});
                        group = [];
                        _.each(selectedElements, function(model){
                            if( model.getId() != currentModelId ){
                                group.push(Marionette.ItemView.Registry.getByModelId(model.getId()));
                            }
                        });
                        if(!that.model.get('classHidden')){
                            slidesApp.gridSnapModule.prepareItemsSnap();
                        }
                    },
                    drag: function(e, ui){
                        if(!slidesApp.activeElement.view){
                            return false;
                        }
                        var posTop, posLeft;
                        var pos = jQueryValamis(this).position();

                        if(!that.model.get('classHidden')){
                            posTop = slidesApp.gridSnapModule.getPosSideTop(ui.position.top);
                            posLeft = slidesApp.gridSnapModule.getPosSideLeft(ui.position.left);
                            ui.position.top = posTop;
                            ui.position.left = posLeft;
                        }

                        //Drag grouped elements
                        _.each(group, function(view){
                            view.$el
                                .css({
                                    top: '+=' + (ui.position.top - pos.top),
                                    left: '+=' + (ui.position.left - pos.left)
                                });
                        });

                        that.updateControlsPosition();
                    },
                    stop: function(e, ui){
                        that.positionToModel();
                        //Save positions for group
                        _.each(group, function(view){
                            view.positionToModel();
                        });
                        that.onDragStop();
                    }
                })
                .css('position', 'absolute');
        },
        onDragStop: function(){
            slidesApp.gridSnapModule.removeLines();
            slidesApp.RevealModule.view.$el
                .add(slidesApp.RevealModule.view.ui.reveal_wrapper)
                .css('overflow', '');//Return default style
            slidesApp.vent.trigger('elementsUpdated');
        },
        draggableDestroy: function(){
            if(this.$el.data('ui-draggable')){
                this.$el.draggable('destroy');
            }
        },
        onMouseDown: function(e) {
            if(slidesApp.mode === 'edit') {
                slidesApp.selectedItemView = this;
                slidesApp.execute('item:focus', this);
            }
        },
        duplicateEl: function() {
            slidesApp.execute('item:duplicate', this);
        },
        deleteEl: function(e) {
            slidesApp.selectedItemView = this;
            this.model.set('toBeRemoved', true);
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
            if( this.model.get('classHidden') ) return;
            if( typeof update == 'undefined' ){
                update = true;
            }
            var height = Math.round(this.$el.innerHeight());
            var realHeight = this.getContentHeight();
            if( height > realHeight && (this.model.get('slideEntityType') != 'question' && this.model.get('slideEntityType') != 'plaintext')){
                if( !update ){ return; }
                realHeight = height;
            }
            this.$el.css('height', realHeight);
            if( update ) {
                slidesApp.execute('item:resize', this.$el.width(), realHeight, this, 'update');
            }
        },
        applyLinkedType: function() {
            this.$el.toggleClass( 'linked', ( this.model.get('correctLinkedSlideId') || this.model.get('incorrectLinkedSlideId') ) );
            var button = this.controls.find('.js-item-link');
            if(button.length == 0){
                return;
            }

            //question
            if (_.contains(['question','plaintext','randomquestion'], this.model.get('slideEntityType'))) {

                var button_incorrect = this.controls.find('.js-item-link-incorrect');

                //correct link
                if( this.model.get('correctLinkedSlideId') ){
                    button
                        .attr( 'title',
                        Valamis.language['valBadgeRemoveLink']
                        + ' (' + slidesApp.getSlideModel(parseInt(this.model.get('correctLinkedSlideId'))).get('title') + ')'
                    )
                        .tooltip('fixTitle');
                    button.html(Valamis.language['valBadgeRemoveLink']);
                }
                else if( !this.model.get('correctLinkedSlideId') ){
                    button
                        .attr( 'title', Valamis.language['valBadgeLinkToCorrectAnswer'] )
                        .tooltip('fixTitle');
                    button.html(Valamis.language['valBadgeSelectPage']);
                }

                //incorrect link
                if( this.model.get('incorrectLinkedSlideId') ){
                    button_incorrect
                        .attr( 'title',
                        Valamis.language['valBadgeRemoveLink']
                        + ' (' + slidesApp.getSlideModel(parseInt(this.model.get('incorrectLinkedSlideId'))).get('title') + ')'
                    )
                        .tooltip('fixTitle');
                    button_incorrect.html(Valamis.language['valBadgeRemoveLink']);
                }
                else if( !this.model.get('incorrectLinkedSlideId') ){
                    button_incorrect
                        .attr( 'title', Valamis.language['valBadgeLinkToIncorrectAnswer'] )
                        .tooltip('fixTitle');
                    button_incorrect.html(Valamis.language['valBadgeSelectPage']);
                }

            }
            //text or image
            else {

                if( this.model.get('correctLinkedSlideId') ){
                    button
                        .attr( 'title',
                            Valamis.language['valBadgeRemoveLink']
                            + ' (' + slidesApp.getSlideModel(parseInt(this.model.get('correctLinkedSlideId'))).get('title') + ')'
                        )
                        .tooltip('fixTitle');
                    button.html(Valamis.language['valBadgeRemoveLink']);
                }
                else if( !this.model.get('correctLinkedSlideId') ){
                    button
                        .attr( 'title', Valamis.language['valBadgeLinkToAnotherSlide'] )
                        .tooltip('fixTitle');
                    button.html(Valamis.language['valBadgeSelectPage']);
                }

            }

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
            e.preventDefault();
            var linkTypeName =
                linkType || jQueryValamis(e.target).closest('button').is('.js-item-link') ? 'correctLinkedSlideId' : 'incorrectLinkedSlideId';
            if(this.model.get(linkTypeName)){
                this.model.set(linkTypeName, null);
            } else {
                slidesApp.execute('linkUpdate', linkTypeName);
            }
        },
        openItemSettings: function (e) {
            if (!this.$('.item-settings').is(':hidden')) return;
            this.$('.js-item-notify-correct').attr('checked', this.model.get('notifyCorrectAnswer'));
            if (this.model.get('slideEntityType') == 'randomquestion')
                this.$('.js-question-link').addClass('hidden');
            else this.$('.js-question-link').removeClass('hidden');
            this.trigger('element-settings:open');
            this.trigger('element-modal:open');
            this.ui.settings_panel.show();
            this.updateControlsPosition();
            this.updateControlsScroll();
            this.applyLinkedType();
        },
        closeItemSettings: function () {
            this.ui.settings_panel.hide();
            placeSlideControls();
            this.updateControlsPosition();
            this.trigger('element-modal:close');
        },
        onModalOpen: function(){
            slidesApp.isEditing = true;
        },
        onModalClose: function(){
            slidesApp.isEditing = false;
        },
        zIndexChange: function(e){
            e.preventDefault();
            var model = this.model,
                direction = jQueryValamis(e.target).closest('button').is('.js-item-forward')
                    ? 'forward'
                    : 'backward';

            var siblingElements = slidesApp.activeSlideModel.getElements();
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
                    slidesApp.historyManager.groupOpenNext();
                    model.set( 'zIndex', zIndexNext );
                    siblingElements.splice(currentElIndex, 1);
                    zIndexArr.splice(zIndexArr.indexOf(zIndexNext), 1);
                    _.each(siblingElements, function(item, i){
                        item.set( 'zIndex', zIndexArr[i] );
                    });
                    slidesApp.historyManager.groupClose();
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
            var editorArea = slidesApp.getRegion('editorArea').$el;
            var editorAreaClientRect = editorArea.get(0).getBoundingClientRect();
            var elementClientRect = this.$el.get(0).getBoundingClientRect();
            var rightOffset = editorAreaClientRect.right - elementClientRect.right;
            this.controls
                .css('position','')//return default position value
                .position({
                    my: 'left+' + (Math.min(0, rightOffset) + 10) + ' top',
                    at: atPos,
                    of: this.$el,
                    collision: 'fit',
                    within: '.slides-editor-main-wrapper'
                });
            if(slidesApp.RevealControlsModule.view){
                revealControlsModule.view.ui.button_add_page_right
                    .toggle( rightOffset - lessonStudio.fixedSizes.ELEMENT_CONTROLS_WIDTH > 0 );
            }
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
            this.model.updateProperties({classHidden: newValue});
        },
        hideForDeviceApply: function(notUseBlur){
            var classHidden = this.model.get('classHidden');
            if(classHidden && !notUseBlur){
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
        },
        positionToModel: function(){
            var position = this.$el.position();
            this.model.updateProperties(position);
        },
        changeIconFontSize:function (width, height) {
            this.$el.find('div[class*="content-icon-"]')
                .first()
                .css('font-size', Math.min(width / 2, height / 2) + 'px');
        },
        saveSettings: function(){
            if(_.isEmpty(this.elementSettings.attrs)
                && _.isEmpty(this.elementSettings.properties)){
                return;
            }
            slidesApp.historyManager.groupOpenNext();
            if(!_.isEmpty(this.elementSettings.attrs)){
                this.model.set(this.elementSettings.attrs);
            }
            if(!_.isEmpty(this.elementSettings.properties)){
                this.model.updateProperties(this.elementSettings.properties);
            }
            this.elementSettings = { attrs: {}, properties: {} };
            slidesApp.historyManager.groupClose();
        },
        isAttached: function () {
            return jQueryValamis.contains(document.documentElement, this.$el[0]);
        }
    })
});