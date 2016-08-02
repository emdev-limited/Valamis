/**
 * Created by aklimov on 24.04.15.
 */
slidesApp.commands.setHandler('prepare:new', function (model, mx, my) {
    slidesApp.activeElement.isMoving = false;
    slidesApp.activeElement.view = null;
    slidesApp.activeElement.moduleName = slidesApp.getModuleName(model.get('slideEntityType'));
    slidesApp.activeElement.startX = mx || 0;
    slidesApp.activeElement.startY = my || 0;
});

slidesApp.commands.setHandler('item:create', function (slideElementModel, select) {
    if(typeof select == 'undefined'){
        select = false;
    }
    var model, ViewModel, isNew;
    var moduleName = slideElementModel
        ? slidesApp.getModuleName(slideElementModel.get('slideEntityType'))
        : slidesApp.activeElement.moduleName;
    var activeModule = slidesApp.module(moduleName);
    if (slideElementModel) {
        isNew = false;
        model = slideElementModel;
        if(_.contains(['image','video','iframe','webgl'], model.get('slideEntityType'))){
            model.set('content', decodeURIComponent(model.get('content')));
        }
    } else {
        isNew = true;
        var slideId = slidesApp.activeSlideModel.getId();
        model = activeModule.CreateModel();
        model
            .set({
                zIndex: slidesApp.activeSlideModel.getMaxZIndex() + 1,
                tempId: slidesApp.newSlideElementId--,
                slideId: slideId
            });
        slidesApp.maxZIndex++;//TODO: Remove this variable and code refactoring
        model.setLayoutProperties();
    }
    ViewModel = activeModule.View;

    if (!slidesApp.getSlideElementModel(model.getId()))
        slidesApp.slideElementCollection.add(model);

    var slideEntities = [];
    _.each(slidesApp.slideElementCollection.where({slideId: model.get('slideId')}), function (slideEntity) {
        slideEntities = slideEntities.concat(slideEntity.toJSON());
    });
    slidesApp.activeSlideModel.set('slideElements', slideEntities);
    var view = new ViewModel({model: model});
    var elem = view.render().$el;
    slidesApp.selectedItemView = slidesApp.activeElement.view = view;

    view.$('div[class*="content-icon-"]').css('font-size', Math.min(view.model.get('width') / 2, view.model.get('height') / 2) + 'px');
    if (!isNew && view.model.get('content') !== '') {
        view.$('div[class*="content-icon-"]').hide();
        view.content.css('background-color', 'transparent');
    }

    switch (moduleName) {
        case slidesApp.IframeElementModule.moduleName:
        case slidesApp.PdfElementModule.moduleName:
            if (!isNew && view.model.get('content') !== '')
                view.$('.iframe-item').show();
            break;
        case slidesApp.ImageElementModule.moduleName:
            view.updateUrl(
                model.get('content'),
                model._previousAttributes.content,
                model.get('width'),
                model.get('height'),
                !isNew);
            break;
        case slidesApp.ContentElementModule.moduleName:
            var iconQuestionDiv = jQueryValamis('.sidebar').find('span.val-icon-question').closest('div');
            iconQuestionDiv.hide();

            model.on('change:toBeRemoved', function() {
                if (!model.get('toBeRemoved')) {
                    iconQuestionDiv.hide();
                }
                else {
                    iconQuestionDiv.show();
                }
            });
            model.on('destroy', function() {
               //this is needed for iconQuestion to show in case
               //when we create question and then undo the creation
               iconQuestionDiv.show();
            });

            if (!isNew && view.model.get('content') !== '') {
                slidesApp.selectedItemView.updateQuestion(model);
            }
            break;
        case slidesApp.VideoElementModule.moduleName:
            if (!isNew && view.model.get('content') !== '') {
                view.$('.video-js').show();
                view.updateUrl(view.model.get('content'));
                slidesApp.actionStack.pop();
                slidesApp.toggleSavedState();
            }
            break;
        case slidesApp.MathElementModule.moduleName:
            view.$('.ui-resizable-handle').toggleClass('hidden', true);
            break;
        case slidesApp.WebglElementModule.moduleName:
            view.updateUrl(model.get('content'));
            break;
    }
    elem.attr('id', 'slideEntity_' + (model.id || model.get('tempId')));
    jQueryValamis('#slide_' + model.get('slideId')).append(elem);

    if (moduleName === slidesApp.MathElementModule.moduleName)
        view.content.find('.math-content').fitTextToContainer(view.$el, true);

    if( select ){
        view.selectEl();
    }

    //New element moving from sidebar
    if (slidesApp.activeElement.isMoving && !slidesApp.initializing) {
        var offset = slidesApp.RevealModule.view.ui.work_area.offset(),
            scrollTop = jQueryValamis( document ).scrollTop();

        elem.css({
            left: slidesApp.activeElement.startX
                ? (slidesApp.activeElement.startX - offset.left) - elem.width() / 2
                : 0,
            top: slidesApp.activeElement.startY
                ? slidesApp.activeElement.startY
                    - (offset.top - scrollTop)
                    - (elem.height() / 2)
                : 0
        });
        slidesApp.activeElement.isMoving = false;

        slidesApp.RevealModule.view.$el
            .add(slidesApp.RevealModule.view.ui.reveal_wrapper)
            .css('overflow', 'visible');
    }
    Marionette.ItemView.Registry.register(view.model.get('id') || view.model.get('tempId'), view);
});

slidesApp.commands.setHandler('resize:prepare', function (view) {
    slidesApp.activeElement.isResizing = true;
    slidesApp.execute('item:focus', view);
});

slidesApp.commands.setHandler('item:delete', function (view) {
    view = view || slidesApp.selectedItemView;
    if(!view.model.get('toBeRemoved') && jQueryValamis('#slideEntity_' + view.model.getId()).size() == 0){
        slidesApp.execute('item:create', view.model);
    }

    view.$el.toggle(!view.model.get('toBeRemoved'));
    slidesApp.execute('item:blur');
    slidesApp.maxZIndex += view.model.get('toBeRemoved') ? -1 : 1;//TODO: Remove this variable and code refactoring
    var maxZIndex = slidesApp.activeSlideModel.getMaxZIndex();

    //sort models by z-index
    if (slidesApp.selectedItemView && slidesApp.selectedItemView.model.get('zIndex') < maxZIndex) {
        var slideElements = slidesApp.activeSlideModel.getElements();
        if (slideElements.length > 0) {
            slideElements.sort(function (a, b) {
                return a.get('zIndex') - b.get('zIndex');
            });
            slideElements.forEach(function (model, index) {
                model.set('zIndex', (index + 1));
            });
        }
    }
});

slidesApp.commands.setHandler('item:focus', function (view) {
    if(slidesApp.initializing || view.model.get('active')){
        return;
    }
    if(!view.model.get('selected')) {
        slidesApp.execute('item:blur');
    }
    jQueryValamis('.ui-resizable-handle').toggleClass('hidden', true);
    slidesApp.activeElement.view = view;
    slidesApp.activeElement.view.model.set('active', true);
    slidesApp.activeElement.moduleName = slidesApp.getModuleName(view.model.get('slideEntityType'));
    view.$el.find('> .ui-resizable-handle').toggleClass('hidden', false);
    jQueryValamis('#slide-controls').hide();
    view.updateControlsPosition();
});

slidesApp.commands.setHandler('item:blur', function (slideId) {
    if(slidesApp.initializing){
        return;
    }
    slideId = slideId || slidesApp.activeSlideModel.getId();
    var slideModel = slidesApp.getSlideModel(slideId);
    if (slideId && slideModel && slideModel.get('isLessonSummary')) {
        jQueryValamis('.js-hide-if-summary').hide();
        jQueryValamis('#slide_' + slideId + ' .rj-element .lesson-summary-text').nextAll('.item-controls').hide();
    }
    else
        jQueryValamis('#slide-controls').show();
    jQueryValamis('.ui-resizable-handle').toggleClass('hidden', true);
    jQueryValamis('.iframe-edit-panel').hide();
    jQueryValamis('.rj-element .item-border').removeClass('active');

    if(slidesApp.activeElement.view && slidesApp.activeElement.view.isAttached()){
        slidesApp.activeElement.view.closeItemSettings();
    }

    //To prevent item resize
    slidesApp.activeElement.view = null;
    slidesApp.activeElement.moduleName = null;
    slidesApp.isEditing = false;
    slidesApp.trigger('editorModeChanged');

    if(slideModel){
        slideModel.updateAllElements({selected: false, active: false});
    }
    placeSlideControls();
});

slidesApp.commands.setHandler('item:duplicate', function (view, placeModel) {
    view = view || slidesApp.activeElement.view;
    placeModel = placeModel || view.model;//model for copy location
    if(!view){
        return;
    }
    var currentSlideId = slidesApp.activeSlideModel.getId(),
        newModel = new lessonStudio.Entities.LessonPageElementModel(_.omit(view.model.attributes, ['id','properties','active','selected'])),
        properties = view.model.copyProperties();

    if(view.model.getId() != placeModel.getId()){
        var position;
        properties = _.mapValues(properties, function(props, deviceId){
            position = placeModel.getLayoutProperties(deviceId);
            return _.extend({}, props, { left: position.left, top: position.top });
        });
    }

    slidesApp.copyImageFromGallery(newModel);
    newModel.set({
        slideEntityType: view.model.get('slideEntityType'),
        tempId: slidesApp.newSlideElementId--,
        zIndex: ++slidesApp.maxZIndex
    });

    //new positions for all devices
    slidesApp.devicesCollection.each(function(deviceModel){
        var deviceId = deviceModel.get('id'),
            deviceHeight = deviceModel.get('minHeight'),
            props = properties[deviceId] || {};
        var currOffset = newModel.getNewPosition(props.left, props.top, props.width, props.height, deviceHeight);
        props.left = currOffset.left;
        props.top = currOffset.top;
    });
    newModel.set({
        properties: properties,
        slideId: currentSlideId
    });

    if (_.contains(['image', 'webgl', 'pdf'], newModel.get('slideEntityType'))
        && newModel.get('content')
        && newModel.get('content').indexOf('/') == -1) {

        newModel
            .set('clonedId', (view.model.get('id') || view.model.get('clonedId')));

    }

    createElement(newModel);

    function createElement(model) {
        slidesApp.execute('prepare:new', model);
        slidesApp.activeElement.isMoving = false;
        slidesApp.execute('item:create', model, true);
    }
});

slidesApp.commands.setHandler('item:resize', function (width, height, view, updateMode) {
    var moduleName;
    if (!view) {
        view = slidesApp.activeElement.view;
        moduleName = slidesApp.activeElement.moduleName;
    }
    else {
        moduleName = view.model.get('slideEntityType').charAt(0).toUpperCase() + view.model.get('slideEntityType').slice(1) + 'ElementModule';
    }
    if (view) {
        if (moduleName == slidesApp.MathElementModule.moduleName)
            view.content.find('.math-content').fitTextToContainer(view.$el, true);
        if( updateMode == 'reset' ){
            view.model.resetProperties({width: width, height: height});
        } else if( updateMode == 'update'){
            view.model.updateProperties({width: width, height: height});
        } else if( !_.contains(['question','plaintext'], view.model.get('slideEntityType')) ) {
            view.$el.css({width: width, height: height});
        }
        view.content.find('div[class*="content-icon-"]').first()
            .css('font-size', Math.min(width / 2, height / 2) + 'px');
    }
});

slidesApp.commands.setHandler('controls:place', function () {
    window.placeSlideControls(jQueryValamis(window).width(), jQueryValamis(window).height());
    slidesApp.execute('item:blur');
});

slidesApp.commands.setHandler('action:push', function () {
    function stringifyObject(obj) {
        var json = _.chain(obj)
            .map(function (value, key) {
                var val = value;
                if(key === 'view' && _.isObject(val)){
                    val = val.model.toJSON();
                }
                if(val instanceof jQueryValamis){
                    val = val.attr('class');
                }
                return [key, val];
            })
            .object()
            .value();
        return JSON.stringify(json);
    }

    if (!slidesApp.initializing && stringifyObject(slidesApp.newValue) !== stringifyObject(slidesApp.oldValue)) {
        slidesApp.actionStack.push({
            viewId: slidesApp.viewId,
            type: slidesApp.actionType,
            oldValue: slidesApp.oldValue,
            newValue: slidesApp.newValue,
            slideId: slidesApp.slideId
        });
        if (slidesApp.isSaved)
            slidesApp.toggleSavedState();
    }
});

slidesApp.commands.setHandler('action:undo', function() {
    slidesApp.undoAction();
});

slidesApp.commands.setHandler('app:stop', function () {
    revealModule.stop();
    arrangeModule.stop();
    versionModule.stop();
    slidesApp.historyManager.stop();
    slidesApp.keyboardModule.stop();
    jQueryValamis('#arrangeContainer').prevAll().show();
    jQueryValamis('#arrangeContainer').empty();
    slidesApp.slideSetModel = null;
    jQueryValamis('.slide-popup-panel').hide();
    slidesApp.toggleSavedState(true);
    slidesApp.switchMode('edit', true);
    slidesApp.isEditorReady = false;
    slidesApp.isRunning = false;
    _.defer(function(){
        jQueryValamis(document.body).removeClass('overflow-hidden');
    });
});

slidesApp.commands.setHandler('editor-reloaded', function () {
    jQueryValamis('#arrangeContainer').empty();
    slidesApp.module('arrangeModule').stop();
    valamisApp.execute('notify', 'clear');
    slidesApp.execute('controls:place');
    if (slidesApp.mode === 'preview') {
        slidesApp.togglePreviewMode(slidesApp.mode);
        slidesApp.switchMode(slidesApp.mode, true);
    }
});

slidesApp.commands.setHandler('random:question:render', function (selectedQuestions, sidebarModel) {
    var isFirstNode = true;
    var ids = [];

    function collectIds(nodes) {
        _.each(nodes, function(item) {
            var node = (item instanceof Backbone.Model) ? item.toJSON() : item;
            if(node.contentType === 'category')
                collectIds(node.children);
            else
                ids.push(node.uniqueId)
        });
    }

    collectIds(selectedQuestions);
    var content = ids.join(',');

    for (var i = 0; i < sidebarModel.get('randomQuestions'); i++) {
        if (!isFirstNode){
            slidesApp.execute('reveal:page:add', 'right');
        }

        slidesApp.execute('prepare:new', sidebarModel);
        slidesApp.activeElement.isMoving = false;
        slidesApp.execute('item:create');
        slidesApp.activeElement.view.renderRandomQuestion(content);

        if (isFirstNode){
            isFirstNode = false;
            if (slidesApp.slideSetModel.get('themeId')) {
                slidesApp.execute('reveal:page:applyTheme', slidesApp.activeSlideModel);
            }
        }
    }
});

slidesApp.commands.setHandler('question:render', function (selectedQuestions, sidebarModel) {
    var isFirstNode = true;

    function addQuestions(nodes) {

        _.each(nodes, function(item) {
            var node = (item instanceof Backbone.Model) ? item.toJSON() : item;
            if(node.contentType === 'category') {
                addQuestions(node.children);
            } else {
                if (!isFirstNode){
                    slidesApp.execute('reveal:page:add', 'right');
                }

                slidesApp.execute('prepare:new', sidebarModel);
                slidesApp.activeElement.isMoving = false;
                slidesApp.execute('item:create');
                slidesApp.activeElement.view.renderQuestion(new QuestionModel(node));

                if (isFirstNode){
                    isFirstNode = false;
                    if (slidesApp.slideSetModel.get('themeId')) {
                        slidesApp.execute('reveal:page:applyTheme', slidesApp.activeSlideModel);
                    }
                }
            }
        });

    }

    addQuestions(selectedQuestions);
});

slidesApp.commands.setHandler("contentmanager:show:modal", function (model) {

    var sidebarModel = model;
    var questionModalView = new contentManagerModalView();
    var view = new valamisApp.Views.ModalView({
        contentView: questionModalView,
        header: Valamis.language['valContentManagementModalTitleLabel'],
        customClassName: 'add-questions-modal',
        selectedQuestions: '',
        beforeSubmit: function () {

            var topbarModel = contentManager.mainRegion.currentView.topbar.currentView.model;
            sidebarModel.set({
                'isRandom': topbarModel.get('isRandom'),
                'randomQuestions': topbarModel.get('randomQuestions') || topbarModel.get('defaultRandomQuestions')
            });

            var contentRegion = contentManager.mainRegion.currentView.regionManager.get('content'),
                contentListRegion = contentRegion.currentView.regionManager.get('content'),
                nodesCollection = contentListRegion.currentView.model.nodes;

            this.selectedQuestions = nodesCollection.filter(function(node){
                return node.get('selected');
            });

            if (this.selectedQuestions.length > 0) {
                return true;
            }

            valamisApp.execute('notify', 'error', Valamis.language['selectQuestionMessageLabel']);

            return false;
        },
        submit: function () {
            slidesApp.historyManager.groupOpenNext();
            if (sidebarModel.get('isRandom')) {
                slidesApp.execute('random:question:render', this.selectedQuestions, sidebarModel);
            }
            else {
                slidesApp.execute('question:render', this.selectedQuestions, sidebarModel);
            }
            slidesApp.historyManager.groupClose();

            valamisApp.execute('notify', 'clear');
        },
        onDestroy: function () {
            //return applied elementQuery
            var portlet_container = jQueryValamis('#valamisAppModalRegion').closest('.portlet'),
                old_min_width = portlet_container.data('old-min-width');

            if (old_min_width) {
                portlet_container.attr('min-width', old_min_width);
                portlet_container.removeAttr('data-elementquery-bypass');
            }
            contentManager.onStop();
            contentManager.mainRegion.reset();
            slidesApp.isEditing = false;
        }
    });
    view.on('before:show', function(){
        slidesApp.isEditing = true;
    });
    valamisApp.execute('modal:show', view);

});

slidesApp.commands.setHandler('linkUpdate', function (linkTypeName) {
    window.editorMode = linkTypeName == 'correctLinkedSlideId' ? 'arrange:select' : 'arrange:select-incorrect';
    slidesApp.switchMode('arrange');
});

slidesApp.commands.setHandler('temp:delete', function () {
    _.each(slidesApp.tempBlobUrls, revokeBlobURL);
    slidesApp.tempBlobUrls = []
});