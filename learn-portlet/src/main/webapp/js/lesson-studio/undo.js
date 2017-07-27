/**
 * HistoryManager
 *
 */
var HistoryManager = function( module, app ){

    this.startWithParent = false;

    var skipUndoAction, isGroupAction, objectsHistory, history;
    var moduleOptions = {
        saveModelAfterAction: false
    };

    this.historyIndex = -1;
    this.savedIndex = -1;

    this.onStart = function( options ){
        _.extend( moduleOptions, options );
        this.historyReset();
    };

    this.onStop = function(){
        this.historyReset();
    };

    this.isInitialized = function(){
        return this._isInitialized;
    };

    /** History reset */
    this.historyReset = function(){
        this.historyIndex = -1;
        this.savedIndex = -1;
        skipUndoAction = false;
        isGroupAction = false;
        objectsHistory = {
            models: {}
        };
        history = [];
        this.trigger('change:after');
        this.trigger('change:save');
    };

    /** Push new undo action */
    this.pushModelChange = function( model, opts ){

        var options = {
            skipAttributes: [],
            omitUndoActions: [],
            namespace: 'items'
        };
        _.extend(options, opts);

        this.trigger('change:before');

        if ( skipUndoAction || !this.isInitialized() || !model.collection ) {
            return;
        }
        var changed_keys = _.keys(model.changedAttributes()),
            modelCid = model.cid,
            modelIndex = model.collection.indexOf( model );

        if (
            !options.add
            && modelIndex > -1
            && changed_keys.length > 0
            && _.difference(changed_keys, options.skipAttributes).length == 0 ) {
            return;
        }

        if(!objectsHistory.models[options.namespace]){
            objectsHistory.models[options.namespace] = {};
        }

        if ( !objectsHistory.models[options.namespace][modelCid] ) {
            objectsHistory.models[options.namespace][modelCid] = {
                hIndex: -1,
                mIndex: Math.max( modelIndex, model.collection.length ),
                data: [],
                collection: model.collection
            };
        }

        var actionName = modelIndex == -1
            ? 'removeModel'
            : options.add || !model.hasChanged() ? 'addModel' : 'changeModel';

        //Save primary state
        if ( objectsHistory.models[options.namespace][modelCid].data.length == 0 ){

            var previousData = _.omit(model.previousAttributes(), options.omitUndoActions);
            objectsHistory.models[options.namespace][modelCid].data.push( previousData );
            objectsHistory.models[options.namespace][modelCid].hIndex++;

        }

        //Save new state
        var data = {};

        if ( actionName != 'removeModel' ) {
            data = model.changedAttributes()
                ? _.omit(model.changedAttributes(), _.extend(options.omitUndoActions, options.skipAttributes))
                : {};
        }

        objectsHistory.models[options.namespace][modelCid].hIndex++;
        var index = objectsHistory.models[options.namespace][modelCid].hIndex;
        objectsHistory.models[options.namespace][modelCid].data = objectsHistory.models[options.namespace][modelCid].data.splice( 0, index );
        objectsHistory.models[options.namespace][modelCid].data.push( data );

        this.historyIndex++;
        history = history.slice( 0, this.historyIndex );
        history.push({ name: actionName, cid: modelCid, namespace: options.namespace });

        this.trigger('change:after');
        this.trigger('change:save');
    };

    /** Apply undo/redo action */
    this.apply = function( actionType ){

        if( !this.isInitialized()
            || ( actionType == 'undo' && !this.isUndoAvailable() )
            || ( actionType == 'redo' && !this.isRedoAvailable() ) ){
            return;
        }
        this.skipActions(true);

        if( actionType == 'redo' ){
            this.historyIndex++;
        }

        var action = history[ this.historyIndex ],
            actionName = action.name,
            index, model;

        if ( actionType == 'undo' ) {
            if ( actionName == 'addModel' ) {
                actionName = 'removeModel';
            } else if ( actionName == 'removeModel' ) {
                actionName = 'addModel';
            }
        }

        switch ( actionName ) {
            case "addModel":
            case "changeModel":

                index = objectsHistory.models[action.namespace][action.cid].hIndex - ( actionType == 'undo' ? 1 : -1 );

                if ( index >= 0 && objectsHistory.models[action.namespace][action.cid].data[ index ] ) {

                    model = objectsHistory.models[action.namespace][action.cid].collection.get( action.cid );
                    var historyData = this.getHistoryModelData( action.cid, action.namespace, index );

                    if ( !model ) {

                        var mIndex = objectsHistory.models[action.namespace][action.cid].mIndex;
                        model = new objectsHistory.models[action.namespace][action.cid].collection.model( historyData );
                        model.cid = action.cid;
                        objectsHistory.models[action.namespace][action.cid].collection.add( model, { at: mIndex, isUndoAction: true } );

                    } else {
                        model.set( historyData );
                        this.afterModelUpdate( model );
                    }
                    this.renderArrangeModeSlide( model , action.namespace);
                    objectsHistory.models[action.namespace][action.cid].hIndex = index;

                }

                break;
            case "removeModel":

                index = objectsHistory.models[action.namespace][action.cid].hIndex - ( actionType == 'undo' ? 1 : -1 );
                model = objectsHistory.models[action.namespace][action.cid].collection.get( action.cid );
                if( model ){
                    if (action.namespace == 'slides') {
                        var currentSlideId = model.getId();
                        var currentPage = jQueryValamis('#slide_' + currentSlideId);
                        var hasParent = currentPage.prevAll().length > 0  ||
                            currentPage.parent().prevAll().length > 0;
                        revealModule.view.navigationRefresh(hasParent);
                    }
                    model.destroy();
                    this.renderArrangeModeSlide( model , action.namespace)
                }

                objectsHistory.models[action.namespace][action.cid].hIndex = index;

                break;
        }

        _.defer(function(){
            if( actionType == 'undo' ){
                module.historyIndex--;
            }
            module.skipActions(false);
            module.applyGroup(actionType, action);
            if(!isGroupAction){
                module.trigger( actionType + ':after' );
                module.trigger('change:after');
                module.trigger('change:save');
            }
        });

    };

    /** Apply group actions */
    this.applyGroup = function(actionType, action){
        if( (actionType == 'undo' && action.group == 'close')
            || (actionType == 'redo' && action.group == 'open')) {
                isGroupAction = true;
        }
        if( (actionType == 'undo' && action.group == 'open')
            || (actionType == 'redo' && action.group == 'close')) {
                isGroupAction = false;
        }
        if( isGroupAction ) {
            _.defer(function(){
                module.apply(actionType);
            });
        }
    };

    /** Merge history data */
    this.getHistoryModelData = function( modelCid, namespace, index ){
        if(!namespace){
            namespace = 'items';
        }
        if (!objectsHistory.models[namespace][modelCid]
            || !objectsHistory.models[namespace][modelCid].data[ index ]) {
            return {};
        }

        var data_keys = _.keys( objectsHistory.models[namespace][modelCid].data[0] ),
            data = objectsHistory.models[namespace][modelCid].data,
            historyData = {};

        var initial_data_keys = data_keys.slice(0);

        for( var i = index; i >= 0; i-- ){

            var cur_data_keys = _.keys( data[ i ]),
                initial_diff = _.difference( cur_data_keys, initial_data_keys );

            //if current keys was not in initially
            if(initial_diff.length > 0){
                data_keys = _.union(data_keys, initial_diff);
            }

            var cur_data = _.pick( data[ i ], data_keys );
            data_keys = _.difference( data_keys, cur_data_keys );

            _.extend( historyData, cur_data );

            if ( data_keys.length == 0 ) {
                break;
            }
        }

        return historyData;
    };

    /** Remove last action */
    this.undoPop = function(){

        var action = history[ this.historyIndex - 1 ];

        if ( action ) {

            history.pop();
            if ( action.cid ) {
                objectsHistory.models[action.namespace][action.cid].data.pop();
                objectsHistory.models[action.namespace][action.cid].hIndex--;
            }
            this.historyIndex--;

        }

    };

    /** clear history for model */
    this.clearHistory = function(modelCid, namespace, model){
        if(!namespace){
            namespace = 'items';
        }
        if(model){
            modelCid = model.cid;
        }
        var hIndex = _.findIndex(history, { cid: modelCid });
        if( hIndex > -1 ){
            this.historyIndex--;
            history.splice(hIndex, 1);
            this.clearHistory(modelCid, namespace);
        }
        if(objectsHistory.models[namespace] && objectsHistory.models[namespace][modelCid]){
            delete objectsHistory.models[namespace][modelCid];
        }
        if(model){
            module.pushModelChange(model, { add: true });
        }
        this.trigger('change:after');
        this.trigger('change:save');
    };

    this.setSaved = function(){
        this.savedIndex = this.historyIndex;
        this.trigger('change:save');
    };

    this.isSaved = function(){
        return this.savedIndex == this.historyIndex;
    };

    this.getHistorySize = function(){
        return history.length;
    };

    this.getHistory = function(){
        return history;
    };

    this.getObjectsHistory = function(){
        return objectsHistory;
    };

    this.skipActionOnce = function(){
        if(this.isInitialized()){
            this.once('change:before', function(){
                skipUndoAction = true;
            }, this);
        }
    };

    this.skipActions = function( needSkip ){
        skipUndoAction = needSkip;
    };
    
    this.getSkipActions = function() {
        return skipUndoAction;
    };

    this.isUndoAvailable = function(){
        return this.historyIndex > -1;
    };

    this.isRedoAvailable = function(){
        return this.historyIndex + 1 < history.length;
    };

    this.isUnsavedAvailable = function(){
        return this.historyIndex != this.savedIndex;
    };

    this.afterModelUpdate = function( model ){
        if(moduleOptions.saveModelAfterAction){
            model.save();
        }
        //TODO:create trigger on changing content instead of it
        model.trigger('change:fromHistory', model);
    };

    this.renderArrangeModeSlide = function (model, namespace) {
        if (slidesApp.mode == 'arrange' && namespace == 'slidesElements') {
            _.defer(function () {
                arrangeModule.renderSlide(model.get('slideId'));
            });
        }
    };

    /** Open history group after next history change */
    this.groupOpenNext = function(){
        if(this.isInitialized() && !skipUndoAction) {
            this.once('change:after', this.groupOpen, this);
        }
    };

    /** Open history group */
    this.groupOpen = function(){
        if(this.isInitialized() && !skipUndoAction && history[ module.historyIndex ]){
            var lastIndex = -1;
            if(module.historyIndex > -1) {
                lastIndex = _.findLastIndex(history, function (item) {
                    return !_.isUndefined(item.group);
                });
            }
            if( lastIndex === -1 || history[ lastIndex ].group != 'open' ){
                history[ module.historyIndex ].group = 'open';
            }
        }
    };

    /** Close history group */
    this.groupClose = function(){
        if(this.isInitialized() && !skipUndoAction) {
            _.defer(function() {
                var lastIndex = -1;
                if(module.historyIndex > -1) {
                    lastIndex = _.findLastIndex(history, function (item) {
                        return !_.isUndefined(item.group);
                    });
                }
                if ( lastIndex === -1 || history[ lastIndex ].group != 'open' ) {
                    module.off( 'change:after', module.groupOpen );
                } else {
                    if ( !history[ module.historyIndex ].group ) {
                        history[ module.historyIndex ].group = 'close';
                    } else if( history[ module.historyIndex ].group == 'open' ) {
                        delete history[ module.historyIndex ].group;
                    }
                }
            }, 0);
        }
    };

};

//TODO: remove this later
slidesApp.undoAction = function () {

    if(slidesApp.actionStack.length === 0) {
        return;
    }

    var action = slidesApp.actionStack.pop();
    slidesApp.isUndoAction = true;

    var view = action.viewId
        ? Marionette.ItemView.Registry.getByViewId(action.viewId)
        : null;

    var slide = action.slideId
        ? slidesApp.getSlideModel(action.slideId)
        : null;

    var saved = slidesApp.actionStack.length == slidesApp.savedIndex;
    slidesApp.toggleSavedState( saved );

    switch (action.type) {
        case 'itemMoved':
        case 'itemResized':
            if(view) {
                if (slidesApp.isSaved) {
                    slidesApp.oldValue = {'top': view.model.get('top'), 'left': view.model.get('left')};
                    slidesApp.newValue = action.oldValue;
                }
                view.model.set(action.oldValue);
                view.content.find('div[class*="content-icon-"]').first().css('font-size', Math.min(view.model.get('width') / 2, view.model.get('height') / 2) + 'px');
            }
            break;
        case 'itemCreated':
            if(view) {
                var modelId = view.model.get('id') || view.model.get('tempId');
                Marionette.ItemView.Registry.remove(modelId);
                if (JSON.stringify(Reveal.getIndices()) !== JSON.stringify(action.newValue.indices)) {
                    Reveal.slide(action.newValue.indices.h, action.newValue.indices.v, action.newValue.indices.f);
                    window.setTimeout(function () {
                        view.deleteEl(null, true);
                    }, 1000 * parseFloat(jQueryValamis('.slides').css('transition-duration').slice(0, -1)));
                }
                else {
                    view.deleteEl(null, true);
                }
            }
            break;
        case 'itemRemoved':
            var modelId = action.oldValue.view.model.get('id') || action.oldValue.view.model.get('tempId');
            Marionette.ItemView.Registry.register(modelId, action.oldValue.view);
            Reveal.slide(action.oldValue.indices.h, action.oldValue.indices.v, action.oldValue.indices.f);
            action.oldValue.view.model.unset('toBeRemoved');
            action.oldValue.view.$el.show();
            break;
        case 'itemContentChanged':
            if(view) {
                view.model.set('content', action.oldValue.content);
                if (action.oldValue.width) view.model.set('width', action.oldValue.width);
                if (action.oldValue.height) view.model.set('height', action.oldValue.height);
                switch (action.oldValue.contentType) {
                    case 'text':
                        view.content.html(action.oldValue.content);
                        break;
                    case 'math':
                        view.model.set('content', action.oldValue.content);
                        view.renderMath(action.oldValue.content);
                        break;
                    case 'image':
                        view.updateUrl(action.oldValue.content);
                        slidesApp.actionStack.pop();
                        break;
                    case 'url':
                        view.$('iframe').attr('src', action.oldValue.content);
                        break;
                    case 'questionId':
                        view.updateQuestion(action.oldValue.content);
                        slidesApp.actionStack.pop();
                        break;
                    case 'video':
                        view.updateUrl(action.oldValue.content);
                        slidesApp.actionStack.pop();
                        break;
                }
            }
            break;
        case 'questionSettingsChanged':
            if (view) {
                var deviceId = slidesApp.devicesCollection.getCurrent().get('id');
                var properties = !_.isEmpty(view.model.get('properties')) ? view.model.get('properties') : {};
                view.model.set({
                    'notifyCorrectAnswer': action.oldValue.notifyCorrectAnswer,
                    'fontSize': action.oldValue.fontSize
                });
                properties[action.oldValue.deviceId].fontSize = action.oldValue.fontSize;
                view.model.set('properties', properties);
                if (action.oldValue.deviceId == deviceId)
                    view.changeQuestionFontSize(action.oldValue.fontSize);
                else
                    view.model.set('fontSize', view.model.get('properties')[deviceId].fontSize);
            }
            break;
        case 'itemLinkedSlideChanged':
            if(view) {
                view.linkUpdate(null, action.oldValue.linkType);
                slidesApp.actionStack.pop();
                slidesApp.toggleSavedState();
            }
            break;
        case 'slideBackgroundChanged':
                Reveal.slide(action.oldValue.indices.h, action.oldValue.indices.v, action.oldValue.indices.f);
                switch (action.oldValue.backgroundType) {
                    case 'color':
                        slidesApp.execute('reveal:page:changeBackground', action.oldValue.background || '', slide, true);
                        break;
                    case 'image':
                        slidesApp.execute('reveal:page:changeBackgroundImage',
                            (action.oldValue.background) ? action.oldValue.background + ' ' + action.oldValue.backgroundSize : '',
                            (action.newValue.background) ? action.newValue.background + ' ' + action.newValue.backgroundSize : '',
                            slide,
                            null,
                            true
                        );
                        break;
                }
            break;
        case 'slideAdded':
            var newSlideIndices = action.newValue.indices;
            var oldSlideIndices = action.oldValue.indices;
            Reveal.slide(newSlideIndices.h, newSlideIndices.v, newSlideIndices.f);
            slidesApp.execute('reveal:page:delete');
            Reveal.slide(oldSlideIndices.h, oldSlideIndices.v, oldSlideIndices.f);
            slidesApp.actionStack.pop();
            break;
        case 'slideRemoved':
            var slideModel = action.oldValue.slideModel;
            slideModel.unset('toBeRemoved');
            var slideEntities = action.oldValue.slideEntities;
            for (var i in slideEntities) {
                slideEntities[i].unset('id');
                slideEntities[i].unset('toBeRemoved');
                slideEntities[i].set('tempId', slidesApp.newSlideElementId--);
                slideEntities[i].set('slideId', slideModel.get('tempId'));
            }

            switch (slidesApp.mode) {
                case 'edit':
                    Reveal.slide(action.oldValue.indices.h, action.oldValue.indices.v, action.oldValue.indices.f);
                    switch (action.oldValue.direction) {
                        case 'right':
                            slidesApp.execute('reveal:page:add', 'right', slideModel);
                            break;
                        case 'down':
                            slidesApp.execute('reveal:page:add', 'down', slideModel);
                            break;
                    }
                    break;
                case 'arrange':
                    slideModel.unset('leftSlideId');
                    slideModel.unset('topSlideId');
                    if (action.oldValue.bottomSlideId) {
                        slidesApp.getSlideModel(action.oldValue.bottomSlideId).unset('leftSlideId');
                        arrangeModule.slideTargetList = jQueryValamis('#slidesArrangeTile_' + action.oldValue.bottomSlideId);
                        action.oldValue.slideThumbnail.insertBefore(arrangeModule.slideTargetList);
                    }
                    else if (action.oldValue.rightSlideId) {
                        slidesApp.getSlideModel(action.oldValue.rightSlideId).unset('topSlideId');
                        arrangeModule.slideTargetList = jQueryValamis('#slidesArrangeTile_' + action.oldValue.rightSlideId).parent().prev()
                        action.oldValue.slideThumbnail.prependTo(arrangeModule.slideTargetList);
                    }
                    arrangeModule.manageSortableLists();
                    arrangeModule.updateSlideRefs();
                    break;
            }
            break;
        case 'slideOrderChanged':
            var slideModel = slidesApp.getSlideModel(action.oldValue.slideAttrs.slideId);
            slideModel.set({
                leftSlideId: action.oldValue.slideAttrs.leftSlideId,
                topSlideId: action.oldValue.slideAttrs.topSlideId
            });
            if (action.oldValue.rightSlideId) {
                slidesApp.getSlideModel(action.oldValue.rightSlideId).set('leftSlideId', action.newValue.slideModel.id || action.newValue.slideModel.get('tempId'));
            }
            if (action.oldValue.bottomSlideId) {
                slidesApp.getSlideModel(action.oldValue.bottomSlideId).set('topSlideId', action.newValue.slideModel.id || action.newValue.slideModel.get('tempId'));
            }
            break;
        case 'pageSettingsChanged':
            Reveal.slide(action.oldValue.indices.h, action.oldValue.indices.v, action.oldValue.indices.f);
            slidesApp.activeSlideModel.set({
                'title': action.oldValue.title,
                'statementVerb': action.oldValue.statementVerb,
                'statementObject': action.oldValue.statementObject,
                'statementCategoryId': action.oldValue.statementCategoryId,
                'duration': action.oldValue.duration,
                'playerTitle': action.oldValue.playerTitle
            });
            revealControlsModule.initPageSettings();
            break;
        case 'documentImported':
            for(var i = 0; i < 2 * action.newValue.slideCount - 1; i++)
                slidesApp.execute('action:undo');;
            break;
        case 'slideFontChanged':
            slidesApp.execute('reveal:page:changeFont', action.oldValue.font, slide, true);
            break;
        case 'questionViewChanged':
            Reveal.slide(action.oldValue.indices.h, action.oldValue.indices.v, action.oldValue.indices.f);
            var questionFontParts = action.oldValue.questionFont.split('$');
            var answerFontParts = action.oldValue.answerFont.split('$');
            var oldAppearance = {
                question: {
                    family: questionFontParts[0] || '',
                    size: questionFontParts[1] || '',
                    color: questionFontParts[2] || ''
                },
                answer: {
                    family: answerFontParts[0] || '',
                    size: answerFontParts[1] || '',
                    color: answerFontParts[2] || '',
                    background: action.oldValue.answerBg || ''
                }
            };
            slidesApp.execute('reveal:page:changeQuestionView', oldAppearance, action.oldValue.questionType, slide, true);
            break;
        case 'slideThemeChanged':
            if (action.oldValue.themeId) {
                var themeModel = new lessonStudio.Entities.LessonPageThemeModel;
                themeModel.id = action.oldValue.themeId;
                themeModel.fetch({
                    success: function(data) {
                        slidesApp.themeModel = data;
                        slidesApp.execute('reveal:page:applyTheme', slideModel, true);
                        slidesApp.actionStack.pop();
                    }
                });
            }
            else
            for (var i = 0; i < action.oldValue.amount; i++)
                slidesApp.execute('action:undo');
            slidesApp.slideSetModel.set('themeId', action.oldValue.themeId);
            break;
        case 'changeModelAttribute':
            if(view){
                view.model.set( action.oldValue );
            }
            if(slide){
                slide.set( action.oldValue );
            }
            break;
    }

    slidesApp.isUndoAction = false;

};