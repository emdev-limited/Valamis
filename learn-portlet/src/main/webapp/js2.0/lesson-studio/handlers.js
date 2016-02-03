/**
 * Created by aklimov on 24.04.15.
 */
slidesApp.commands.setHandler('drag:prepare:new', function (model, mx, my) {
    slidesApp.activeElement.isMoving = true;
    slidesApp.actionType = 'itemCreated';
    slidesApp.oldValue = null;
    slidesApp.activeElement.model = model;
    slidesApp.activeElement.view = null;

    var moduleNamePrefix = _.contains(['question','plaintext','randomquestion'], model.get('slideEntityType'))
        ? 'content'
        : model.get('slideEntityType');

    slidesApp.activeElement.moduleName = moduleNamePrefix.charAt(0).toUpperCase() + moduleNamePrefix.slice(1) + 'ElementModule';

    slidesApp.activeElement.startX = mx;
    slidesApp.activeElement.startY = my;
});

slidesApp.commands.setHandler('item:create', function (isNew, slideElementModel) {
    var activeModule = slidesApp.module(slidesApp.activeElement.moduleName);
    var ViewModel = activeModule.View;
    var model;
    if (isNew) {
        var slideId = slidesApp.activeSlideModel.get('id') || slidesApp.activeSlideModel.get('tempId');
        model = activeModule.CreateModel();
        model
            .set('zIndex', ++slidesApp.maxZIndex)
            .set('tempId', slidesApp.newSlideElementId--)
            .set('slideId', slideId);
    }
    else {
        model = slideElementModel;
        model.set('content', unescape(model.get('content')));
    }

    if (!slidesApp.getSlideElementModel(model.get('id') || model.get('tempId')))
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

    switch (slidesApp.activeElement.moduleName) {
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
                model.get('height'));
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

            if (!isNew && view.model.get('content') !== '') {
                slidesApp.execute('question:update', model)
                slidesApp.actionStack.pop();
                slidesApp.toggleSavedState();
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
            view.$('.ui-resizable-handle').hide();
            break;
        case slidesApp.WebglElementModule.moduleName:
            view.updateUrl(model.get('content'));
            break;
    }
    elem.attr('id', 'slideEntity_' + (model.id || model.get('tempId')));
    jQueryValamis(Reveal.getCurrentSlide()).append(elem);

    slidesApp.activeElement.offsetX = elem.width() / 2;
    slidesApp.activeElement.offsetY = elem.height() / 2;

    slidesApp.activeElement.view.selectEl();

    if (slidesApp.activeElement.moduleName === slidesApp.MathElementModule.moduleName)
        view.content.find('.math-content').fitTextToContainer(view.$el, true);

    if (!slidesApp.initializing) {
        slidesApp.viewId = view.cid;
        slidesApp.actionType = 'itemCreated';
        slidesApp.oldValue = null;
        slidesApp.newValue = {indices: Reveal.getIndices(), view: view.cid};
        slidesApp.execute('action:push');

        if (slidesApp.activeElement.isMoving) {//new element moving from sidebar
            slidesApp.getRegion('editorArea').$el
                .css('overflow', 'visible');
        }
    }
    Marionette.ItemView.Registry.register(view.model.get('id') || view.model.get('tempId'), view);
});

slidesApp.commands.setHandler('drag:prepare:existing', function (view, mx, my, offsetX, offsetY) {
    if (isEditorEnabled()) return;

    slidesApp.activeElement.isMoving = true;
    slidesApp.actionType = 'itemMoved';
    slidesApp.oldValue = {'top': view.model.get('top'), 'left': view.model.get('left')};
    slidesApp.activeElement.startX = mx;
    slidesApp.activeElement.startY = my;
    slidesApp.activeElement.offsetX = offsetX;
    slidesApp.activeElement.offsetY = offsetY;
    slidesApp.execute('item:focus', view);
    if(!view.model.get('classHidden')){
        slidesApp.GridSnapModule.prepareItemsSnap();
    }
});

slidesApp.commands.setHandler('resize:prepare', function (view) {
    slidesApp.activeElement.isResizing = true;
    slidesApp.execute('item:focus', view);
    slidesApp.actionType = 'itemResized';
    slidesApp.viewId = view.cid;
    slidesApp.oldValue = {
        'top': view.model.get('top'), 'left': view.model.get('left'),
        'width': view.model.get('width'), 'height': view.model.get('height')
    };
});

slidesApp.commands.setHandler('item:delete', function (isUndoAction) {
    if (!isUndoAction) {
        slidesApp.viewId = slidesApp.selectedItemView.model.get('id') || slidesApp.selectedItemView.model.get('tempId');
        slidesApp.actionType = 'itemRemoved';
        slidesApp.oldValue = {indices: Reveal.getIndices(), view: slidesApp.selectedItemView};
        slidesApp.newValue = null;
        slidesApp.execute('action:push');
    }
    slidesApp.selectedItemView.model.set('toBeRemoved', true);
    slidesApp.selectedItemView.$el.hide();
    slidesApp.execute('item:blur');
    --slidesApp.maxZIndex;

    //sort models by z-index
    if (slidesApp.selectedItemView.model.get('zIndex') <= slidesApp.maxZIndex) {
        var slideIdCurrent = slidesApp.activeSlideModel.get('id') || slidesApp.activeSlideModel.get('tempId'),
            slideElements = slidesApp.slideElementCollection.where({slideId: slideIdCurrent, toBeRemoved: false});

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
    jQueryValamis('.ui-resizable-handle').hide();
    jQueryValamis('.item-controls').hide();
    jQueryValamis('.item-border').hide();

    //To prevent item resize
    slidesApp.activeElement.view = null;
    slidesApp.activeElement.moduleName = null;
    slidesApp.activeElement.model = null;

    slidesApp.activeElement.view = view;
    slidesApp.activeElement.moduleName = view.model.get('slideEntityType').charAt(0).toUpperCase() + view.model.get('slideEntityType').slice(1) + 'ElementModule';
    view.controls.show();
    view.resizeControls.show();
    if (view.model.get('slideEntityType') != 'question' && view.model.get('slideEntityType') != 'plaintext') {
        view.$el.find('> .ui-resizable-handle').show();
    }
    jQueryValamis('.rj-element').removeClass('active');
    view.resizeControls.addClass('active');
    jQueryValamis('#slide-controls').hide();
});

slidesApp.commands.setHandler('item:blur', function (slideId) {
    var slideModel = slidesApp.getSlideModel(slideId);
    if (slideId && slideModel && slideModel.get('isLessonSummary')) {
        jQueryValamis('.js-hide-if-summary').hide();
        jQueryValamis('#slide_' + slideId + ' .rj-element #lesson-summary-table').parents(':eq(2)').unbind();
    }
    else
        jQueryValamis('#slide-controls').show();
    jQueryValamis('.item-controls').hide();
    jQueryValamis('.item-border').show();
    jQueryValamis('.ui-resizable-handle').hide();
    jQueryValamis('.iframe-edit-panel').hide();
    jQueryValamis('.slide-popup-panel').hide();
    jQueryValamis('.rj-element .item-border').removeClass('active');

    //To prevent item resize
    slidesApp.activeElement.view = null;
    slidesApp.activeElement.moduleName = null;
    slidesApp.activeElement.model = null;
    slidesApp.activeElement.isMoving = false;

    slidesApp.isEditing = false;
    placeSlideControls();
});

slidesApp.commands.setHandler('item:duplicate', function (view) {
    var offset = view.model.getNewPosition(),
        newModel = new lessonStudio.Entities.LessonPageElementModel(_.omit(view.model.attributes, ['id','properties'])),
        properties = view.model.copyProperties();

    slidesApp.copyImageFromGallery(newModel);
    newModel.set({
        slideEntityType: view.model.get('slideEntityType'),
        left: offset.left,
        top: offset.top,
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
    newModel.set('properties', properties);

    if (_.indexOf(['image', 'webgl', 'pdf'], newModel.get('slideEntityType')) > -1
        && newModel.get('content')
        && newModel.get('content').indexOf('/') == -1) {
        view.model.clone().then(function (clonedModel) {
            newModel.set(_.extend(
                clonedModel,
                {
                    left: offset.left,
                    top: offset.top,
                    zIndex: ++slidesApp.maxZIndex
                }
            ));
            createElement(newModel);
        });
    }
    else createElement(newModel);

    function createElement(model) {
        slidesApp.execute('drag:prepare:new', model, 0, 0);
        slidesApp.execute('item:create', false, model);
        slidesApp.activeElement.isMoving = false;
        slidesApp.execute('item:focus', view);
    }
});

slidesApp.commands.setHandler('item:resize', function (width, height, view) {
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
        view.model.set({
            width: width,
            height: height
        });
        view.content.find('div[class*="content-icon-"]').first()
            .css('font-size', Math.min(view.model.get('width') / 2, view.model.get('height') / 2) + 'px');
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
                var val = (key === 'view' && _.isObject(value)) ? value.model.toJSON() : value;
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
    jQueryValamis('#arrangeContainer').prevAll().show();
    jQueryValamis('#arrangeContainer').empty();
    slidesApp.slideSetModel = null;
    jQueryValamis('.slide-popup-panel').hide();
    slidesApp.toggleSavedState();
    slidesApp.switchMode('edit', true);
    slidesApp.isEditorReady = false;
    slidesApp.isRunning = false;
    slidesApp.unBindKeys();
});

slidesApp.commands.setHandler('editor-reloaded', function () {
    jQueryValamis('#arrangeContainer').empty();
    slidesApp.module('arrangeModule').stop();
    valamisApp.execute('notify', 'clear');
    slidesApp.execute('controls:place');
    if (slidesApp.mode === 'preview') {
        slidesApp.togglePreviewMode('preview');
        slidesApp.switchMode('preview', true);
    }
});

slidesApp.commands.setHandler('question:update', function(model) {
    if (model.get('slideEntityType') !== 'randomquestion')
        slidesApp.activeElement.view.updateQuestion(model.get('content'), model.get('slideEntityType'));
    else
        slidesApp.activeElement.view.renderRandomQuestion(model.get('content'), model);
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
        if (!isFirstNode)
            slidesApp.execute('reveal:page:add', 'right');
        else
            isFirstNode = false;

        slidesApp.execute('drag:prepare:new', sidebarModel, 0, 0);
        slidesApp.activeElement.isMoving = false;
        slidesApp.execute('item:create', true);
        slidesApp.activeElement.view.renderRandomQuestion(content);
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
                if (!isFirstNode)
                    slidesApp.execute('reveal:page:add', 'right');
                else
                    isFirstNode = false;

                slidesApp.execute('drag:prepare:new', sidebarModel, 0, 0);
                slidesApp.activeElement.isMoving = false;
                slidesApp.execute('item:create', true);
                slidesApp.activeElement.view.renderQuestion(new QuestionModel(node));

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
            if (sidebarModel.get('isRandom'))
                slidesApp.execute('random:question:render', this.selectedQuestions, sidebarModel);
            else
                slidesApp.execute('question:render', this.selectedQuestions, sidebarModel);

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
        }
    });
    valamisApp.execute('modal:show', view);

});

slidesApp.commands.setHandler('linkUpdate', function (linkTypeName) {
    window.editorMode = linkTypeName == 'correctLinkedSlideId' ? 'arrange:select' : 'arrange:select-incorrect';
    slidesApp.switchMode('arrange');
});

slidesApp.commands.setHandler('temp:delete', function () {
    _.each(slidesApp.tempSlideIds, function (id) {
        var model = slidesApp.slideCollection.findWhere({id: id});
        if (model) model.destroy()
    });
});