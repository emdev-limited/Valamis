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
        case 'correctAnswerNotificationChanged':
            if(view) {
                view.toggleNotifyCorrectAnswer();
                slidesApp.actionStack.pop();
                slidesApp.toggleSavedState();
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
                    slidesApp.RevealModule.forEachSlideElement(new window.collection(slideEntities));
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