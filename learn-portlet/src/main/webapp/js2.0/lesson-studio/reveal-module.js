var revealModule = slidesApp.module('RevealModule', function (RevealModule, MyApp, Backbone, Marionette, $, _) {
    RevealModule.startWithParent = false;

    RevealModule.View = Marionette.ItemView.extend({
        template: '#revealTemplate',
        className: 'reveal',
        ui: {
            'slides': '.slides'
        },
        onShow: function(){
            this.bindUIElements();
            this.ui.work_area = this.$el.closest('.slides-work-area-wrapper');
            this.ui.reveal_wrapper = this.$el.closest('.reveal-wrapper');
            this.initReveal();
        },
        initReveal: function() {
            var self = this;
            var deviceLayout = slidesApp.devicesCollection.getCurrent();
            Reveal.initialize({
                width: deviceLayout.get('minWidth') || deviceLayout.get('maxWidth'),
                height: deviceLayout.get('minHeight'),
                controls: true,
                progress: false,
                history: false,
                keyboard: false,
                loop: false,
                center: true,
                embedded: true,
                postMessage: false,
                postMessageEvents: false,
                // Bounds for smallest/largest possible scale to apply to content
                minScale: 1.0,
                maxScale: 1.0,
                backgroundTransition: 'none',// none/fade/slide/convex/concave/zoom
                theme: Reveal.getQueryHash().theme, // available themes are in /css/theme
                transition: !slidesApp.isEditorReady
                    ? Reveal.getQueryHash().transition || 'slide'// none/fade/slide/convex/concave/zoom
                    : 'none'
            });

            this.updateSlidesContainer();
            this.ui.work_area.css({
                height: deviceLayout.get('minHeight')
            });

            Reveal.addEventListener( 'slidechanged', function( event ) {
                this.indexh = event.indexh;
                this.indexv = event.indexv;
                slidesApp.maxZIndex = _.max(_.map(jQueryValamis(Reveal.getCurrentSlide()).find('div[id^="slideEntity_"]'),
                    function (item) { return $(item).find('.item-content').css('z-index'); }
                ));
                slidesApp.maxZIndex = _.isFinite(slidesApp.maxZIndex) ? slidesApp.maxZIndex : 0;
                if(!slidesApp.initializing) {
                    if(!slidesApp.slideAdd && jQueryValamis(Reveal.getCurrentSlide()).attr('id'))
                        slidesApp.activeSlideModel = slidesApp.getSlideModel(parseInt(jQueryValamis(Reveal.getCurrentSlide()).attr('id').replace('slide_', '')));
                    if(jQueryValamis('.slides > section > section').length > 1)
                        jQueryValamis('.js-slide-delete').show();
                    else
                        jQueryValamis('.js-slide-delete').hide();

                    slidesApp.checkIsTemplate();

                    revealControlsModule.view.model.clear({silent: true});
                    revealControlsModule.view.model.set(slidesApp.activeSlideModel.toJSON());
                }
                if(jQueryValamis(Reveal.getCurrentSlide()).find('.question-element').length > 0) {
                    jQueryValamis('.sidebar').find('span.val-icon-question').closest('div').hide();
                }
                else {
                    jQueryValamis('.sidebar').find('span.val-icon-question').closest('div').show();
                }
                if( !slidesApp.initializing ){
                    self.updateSlideHeight();
                    if(revealModule.view){
                        revealModule.view.placeWorkArea();
                    }
                }
            }.bind(this) );

            Reveal.addEventListener( 'ready', function( event ) {
                revealControlsModule.view.model.clear({silent: true});
                revealControlsModule.view.model.set(slidesApp.activeSlideModel.toJSON());
                slidesApp.initializing = true;
                self.updateSlideHeight();
                slidesApp.initializing = false;
            }.bind(this) );

            Reveal.slide(0);
            slidesApp.execute('controls:place');
        },
        destroyReveal: function(){
            Reveal.removeEventListeners();
            this.destroy();
        },
        addPage: function(direction, slideModel, type) {
            slidesApp.viewId = this.cid;
            slidesApp.actionType = 'slideAdded';
            slidesApp.oldValue = {
                indices: Reveal.getIndices(),
                type: type || 'singleSlide'
            };
            var currentPage = jQueryValamis(Reveal.getCurrentSlide());
            slidesApp.slideAdd = true;
            slidesApp.activeSlideModel = slideModel;
            if(direction === 'right') {
                jQueryValamis('<section><section></section></section>').insertAfter(currentPage.parent());
                Reveal.right();
            }
            else if(direction === 'down') {
                jQueryValamis('<section></section>').insertAfter(currentPage);
                Reveal.down();
            }
            else
                return;

            slidesApp.slideAdd = false;
            currentPage = jQueryValamis(Reveal.getCurrentSlide());
            currentPage.attr('id', 'slide_' + (slideModel.id || slideModel.get('tempId')));
            if(slideModel.get('bgColor'))
                currentPage.attr('data-background-color', unescape(slideModel.get('bgColor')));

            if(slideModel.get('bgImage')) {
                var bgImageUrl = slidesApp.getFileUrl(slideModel, slideModel.getBackgroundImageName()),
                    bgImageSize = slideModel.getBackgroundSize();
                currentPage.attr('data-background', bgImageUrl);
                currentPage.attr('data-background-image', bgImageUrl);
                currentPage.attr('data-background-repeat', 'no-repeat');
                currentPage.attr('data-background-size', bgImageSize);
                currentPage.attr('data-background-position', 'center');
            }
            if(!slideModel.get('title'))
                slideModel.set('title', '');

            Reveal.sync();
            slidesApp.newValue = {
                indices: Reveal.getIndices(),
                type: type || 'singleSlide'
            };

            if(slideModel.get('font') && slidesApp.initializing) {
                var isSaved = slidesApp.isSaved;
                slidesApp.execute('reveal:page:changeFont', slideModel.get('font'));
                slidesApp.actionStack.pop();
                if(isSaved != slidesApp.isSaved)
                    slidesApp.toggleSavedState();
            }

            if(!slidesApp.initializing) {
                slidesApp.execute('reveal:page:updateRefs', currentPage, 'add');
                slidesApp.execute('action:push');
            }
            var slideIndices = _.pick( Reveal.getIndices(), ['h','v'] );
            if(slidesApp.initializing)
                slideIndices.h--;
            slidesApp.slideRegistry.register(slideModel.id || slideModel.get('tempId'), slideIndices);
            slidesApp.slideCollection.add(slideModel);
            slidesApp.execute('reveal:page:makeActive');
            if ( slidesApp.slideSetModel.get('themeId') && !slidesApp.initializing ){
                slidesApp.execute('reveal:page:applyTheme', slideModel, true);
            }
        },
        deleteCurrentPage: function() {
            jQueryValamis('.sidebar').find('.question-element').first().remove();
            var currentPage = jQueryValamis(Reveal.getCurrentSlide());
            var currentPageSiblingsBefore = currentPage.prevAll().length + currentPage.parent().prevAll().length;
            var isOnlyPageInGroup = (jQueryValamis('section', currentPage.parent()).length == 1);

            if(!slidesApp.initializing) {
                slidesApp.activeSlideModel.set('toBeRemoved', true);
                var prevPageLeft = currentPage.parent().prev().children('section[id^="slide_"]').first();
                var prevPageUp = currentPage.prev('section[id^="slide_"]');
                var currentPageIndices = Reveal.getIndices();
                var slideElements = slidesApp.slideElementCollection.where({ slideId: slidesApp.activeSlideModel.id || slidesApp.activeSlideModel.get('tempId') });
                var correctLinkedSlideElements = slidesApp.slideElementCollection.where({ correctLinkedSlideId: slidesApp.activeSlideModel.id || slidesApp.activeSlideModel.get('tempId') });
                var incorrectLinkedSlideElements = slidesApp.slideElementCollection.where({ incorrectLinkedSlideId: slidesApp.activeSlideModel.id || slidesApp.activeSlideModel.get('tempId') });
                _.each(correctLinkedSlideElements, function(slideElementModel) {
                    slideElementModel.set('correctLinkedSlideId', undefined);
                    Marionette.ItemView.Registry
                        .getByModelId(slideElementModel.get('id') || slideElementModel.get('tempId'))
                        .applyLinkedType('correctLinkedSlideId');
                });
                _.each(incorrectLinkedSlideElements, function(slideElementModel) {
                    slideElementModel.set('incorrectLinkedSlideId', undefined);
                    Marionette.ItemView.Registry
                        .getByModelId(slideElementModel.get('id') || slideElementModel.get('tempId'))
                        .applyLinkedType('incorrectLinkedSlideId');
                });

                slidesApp.oldValue = {
                    indices: { h: currentPageIndices.h, v: currentPageIndices.v - 1, f: currentPageIndices.f },
                    slideModel: slidesApp.activeSlideModel,
                    slideEntities: slideElements
                };
                if(prevPageUp.length > 0)
                    slidesApp.oldValue.direction = 'down';
                else if(prevPageLeft.length > 0)
                    slidesApp.oldValue.direction = 'right';
                slidesApp.execute('reveal:page:updateRefs', currentPage, 'delete');
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'slideRemoved';
                slidesApp.newValue = null;

                slidesApp.execute('action:push');
                slidesApp.slideRegistry.remove(slidesApp.activeSlideModel.id || slidesApp.activeSlideModel.get('tempId'));
            }

            if (isOnlyPageInGroup) {
                // can delete the whole group and move to the right/left
                currentPage.parent().remove();
            } else {
                // can delete only section and move to the down/up
                currentPage.remove();
            }

            if(currentPageSiblingsBefore > 0)
                Reveal.prev();
            else
                Reveal.slide(0, 0);
            Reveal.sync();
            slidesApp.activeSlideModel = slidesApp.getSlideModel(parseInt(jQueryValamis(Reveal.getCurrentSlide()).attr('id').replace('slide_', '')));
            slidesApp.execute('reveal:page:makeActive');

            if(jQueryValamis('.slides > section > section').length == 1)
                jQueryValamis('.js-slide-delete').hide();
        },
        updateSlideRefs: function(currentPage, actionType) {
            var nextPageRight = currentPage.parent().next().children('section[id^="slide_"]').first();
            var nextPageDown = currentPage.next('section[id^="slide_"]');
            var prevPageLeft = currentPage.parent().prev().children('section[id^="slide_"]').first();
            var prevPageUp = currentPage.prev('section[id^="slide_"]');
            switch(actionType) {
                case 'add':
                    if(nextPageRight.length > 0 && currentPage.prevAll('section').length === 0) {
                        var idToChangeLeft = parseInt(nextPageRight.attr('id').replace('slide_', ''));
                        var newLeftId = parseInt(currentPage.attr('id').replace('slide_', ''));
                        slidesApp.getSlideModel(idToChangeLeft).set('leftSlideId', newLeftId);
                    }
                    if(nextPageDown.length > 0) {
                        var idToChangeTop = parseInt(nextPageDown.attr('id').replace('slide_', ''));
                        var newTopId = parseInt(currentPage.attr('id').replace('slide_', ''));
                        slidesApp.getSlideModel(idToChangeTop).set('topSlideId', newTopId);
                    }

                    if(prevPageUp.length > 0)
                        slidesApp.activeSlideModel.set('topSlideId', parseInt(prevPageUp.attr('id').replace('slide_', '')));
                    else if(prevPageLeft.length > 0)
                        slidesApp.activeSlideModel.set('leftSlideId', parseInt(prevPageLeft.attr('id').replace('slide_', '')));
                    break;
                case 'delete':
                    if(nextPageRight.length > 0 && prevPageUp.length == 0) {
                        var idToChangeLeft = parseInt(nextPageRight.attr('id').replace('slide_', ''));
                        var newLeftId = (currentPage.next('section[id^="slide_"]').length > 0)
                            ? parseInt(currentPage.next('section[id^="slide_"]').attr('id').replace('slide_', ''))
                            : (currentPage.parent().prev().children('section[id^="slide_"]').first().length > 0)
                                ? parseInt(currentPage.parent().prev().children('section[id^="slide_"]').first().attr('id').replace('slide_', ''))
                                : undefined;
                        slidesApp.getSlideModel(idToChangeLeft).set('leftSlideId', newLeftId);
                    }
                    if(nextPageDown.length > 0) {
                        var idToChangeTop = parseInt(nextPageDown.attr('id').replace('slide_', ''));
                        var newTopId = (currentPage.prev('section[id^="slide_"]').length > 0)
                            ? parseInt(currentPage.prev('section[id^="slide_"]').attr('id').replace('slide_', ''))
                            : undefined;

                        slidesApp.getSlideModel(idToChangeTop).set('topSlideId', newTopId);
                    }
                    break;
            }
        },
        changeBackground: function(color, slideModel, skipUndo) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));

            if( !skipUndo ){
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'slideBackgroundChanged';
                slidesApp.slideId = slideModel.get('id') || slideModel.get('tempId');
                slidesApp.oldValue = {
                    indices: Reveal.getIndices(),
                    backgroundType: 'color',
                    background: slide.attr('data-background-color') || ''
                };
                slidesApp.newValue = {
                    indices: Reveal.getIndices(),
                    backgroundType: 'color',
                    background: color
                };
                slidesApp.execute('action:push');
            }

            slideModel.set('bgColor', color);
            slide.attr('data-background-color', color);
            Reveal.sync();

            // Update slide thumbnails in tooltips
            var slideId = slideModel.id || slideModel.get('tempId');
            var slideIsLinkedTo = slidesApp.slideElementCollection.where({ correctLinkedSlideId: slideId }).concat(
                slidesApp.slideElementCollection.where({ incorrectLinkedSlideId: slideId })
            );
            for(var i in slideIsLinkedTo) {
                var slideElementDOMNode = jQueryValamis('#slideEntity_' + (slideIsLinkedTo[i].id || slideIsLinkedTo[i].get('tempId')));
                if(slideIsLinkedTo[i].get('correctLinkedSlideId') == slideId)
                    slideElementDOMNode.find('.linked-slide-thumbnail').css('background-color', color);
                if(slideIsLinkedTo[i].get('incorrectLinkedSlideId') == slideId)
                    slideElementDOMNode.find('.linked-slide-thumbnail.incorrect').css('background-color', color);
            }
        },
        changeBackgroundImage: function(image, oldImage, slideModel, src, skipUndo) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            if(!oldImage)   oldImage   = slidesApp.activeSlideModel.get('bgImage');

            if (image && image.indexOf('$') > -1) image.replace('$', ' ');

            var imageParts = image ? image.split(' ') : [];
            if (imageParts.length > 2){
                imageParts[0] = _.initial(imageParts).join('+');
                imageParts[1] = imageParts.pop();
            }
            var oldImageParts = oldImage ? oldImage.split(' ') : [];
            if (oldImageParts.length > 2){
                oldImageParts[0] = _.initial(oldImageParts).join('+');
                oldImageParts[1] = oldImageParts.pop();
            }
            var oldColor = slideModel.get('bgColor');
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));
            if (!src || src == '') src = slidesApp.getFileUrl(slideModel, imageParts[0]);

            slideModel.set('bgImage', imageParts.join(' '));

            if( !skipUndo ){
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'slideBackgroundChanged';
                slidesApp.slideId = slideModel.get('id') || slideModel.get('tempId');
                slidesApp.oldValue = {
                    indices: Reveal.getIndices(),
                    backgroundType: 'image',
                    background: oldImageParts[0] || oldColor,
                    backgroundSize: oldImageParts[1] || 'cover',
                    amount: 1
                };
                slidesApp.newValue = {
                    indices: Reveal.getIndices(),
                    backgroundType: 'image',
                    background: imageParts[0] || '',
                    backgroundSize: imageParts[1] || 'cover'
                };
                slidesApp.execute('action:push');
            }

            slide.attr('data-background-repeat', 'no-repeat');
            slide.attr('data-background-position', 'center');
            slide.attr('data-background', src);
            slide.attr('data-background-image', imageParts[0] || '');
            slide.attr('data-background-size', imageParts[1] || '');
            Reveal.sync();

            // Update slide thumbnails in tooltips
            var slideIsLinkedTo = slidesApp.slideElementCollection.where({ correctLinkedSlideId: slideModel.id || slideModel.get('tempId') }).concat(
                slidesApp.slideElementCollection.where({ incorrectLinkedSlideId: slideModel.id || slideModel.get('tempId') })
            );
            _.each(slideIsLinkedTo, function (slideElement) {
                var slideElementDOMNode = jQueryValamis('#slideEntity_' + (slideElement.id || slideElement.get('tempId')));
                slideElementDOMNode.find('.linked-slide-thumbnail').css({
                    'background': imageParts[0] + ' no-repeat',
                    'background-size': imageParts[1],
                    'background-position': 'center'
                });
                slideElementDOMNode.find('.linked-slide-thumbnail.incorrect').css({
                    'background': imageParts[0] + ' no-repeat',
                    'background-size': imageParts[1],
                    'background-position': 'center'
                });
            });
        },
        changeQuestionView: function (appearance, questionType, slideModel, skipUndo) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));

            var questionFont = appearance.question.family + '$' + appearance.question.size + '$' + appearance.question.color;
            var answerFont = appearance.answer.family + '$' + appearance.answer.size + '$' + appearance.answer.color;
            var answerBg = appearance.answer.background;

            if( !skipUndo ){
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'questionViewChanged';
                slidesApp.slideId = slideModel.get('id') || slideModel.get('tempId');
                slidesApp.oldValue = {
                    indices: Reveal.getIndices(),
                    questionFont: slide.attr('data-question-font') || '',
                    answerFont: slide.attr('data-answer-font') || '',
                    answerBg: slide.attr('data-answer-bg') || '',
                    questionType: questionType
                };
                slidesApp.newValue = {
                    indices: Reveal.getIndices(),
                    questionFont: questionFont,
                    answerFont: answerFont,
                    answerBg: answerBg
                };
                slidesApp.execute('action:push');
            }

            slide.attr({
                'data-question-font': questionFont,
                'data-answer-font': answerFont,
                'data-answer-bg': answerBg
            });

            slideModel.set({
                questionFont: questionFont,
                answerFont: answerFont,
                answerBg: answerBg
            });
            if(!questionType && questionType !== 0)
                return;

            var updateQuestion = function (slide) {
                //Apply view to slide
                var question = slide.find('.question-item'),
                    header = question.find('h2');

                header.css({
                    'font-family': appearance.question.family,
                    'font-size': appearance.question.size,
                    'color': appearance.question.color
                });

                var answers = getAnswerElement(question, questionType);
                if( questionType == QuestionType.PlainText ){
                    appearance.answer.color = appearance.question.color;
                }

                if (answers) {
                    answers.text.css({
                        'font-family': appearance.answer.family,
                        'font-size': appearance.answer.size,
                        'color': appearance.answer.color
                    });
                    answers.background.css({
                        background: 'none',
                        'border': 'none',
                        'background-color': appearance.answer.background
                    });
                    //Some icons
                    answers.background.find(' > *').css('color', appearance.answer.color);
                }
            };

            updateQuestion(slide);

            //Update slides thumbnails
            var slideIsLinkedTo = slidesApp.slideElementCollection.where({
                correctLinkedSlideId: slidesApp.activeSlideModel.id || slidesApp.activeSlideModel.get('tempId')
            })
                .concat(
                slidesApp.slideElementCollection.where({
                    incorrectLinkedSlideId: slidesApp.activeSlideModel.id || slidesApp.activeSlideModel.get('tempId')
                })
            );
            _.each(slideIsLinkedTo, function (slideElement) {
                var slideElementDOMNode = jQueryValamis('#slideEntity_' + (slideElement.id || slideElement.get('tempId')));
                updateQuestion(slideElementDOMNode.find('.linked-slide-thumbnail'));
                updateQuestion(slideElementDOMNode.find('.linked-slide-thumbnail.incorrect'));
            });
        },
        changeFont: function (font, slideModel, skipUndo) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var fontParts = font ? font.split('$') : [];
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));

            if( !skipUndo ){
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'slideFontChanged';
                slidesApp.slideId = slideModel.get('id') || slideModel.get('tempId');
                slidesApp.oldValue = {
                    indices: Reveal.getIndices(),
                    font: slide.attr('data-font') || ''
                };
                slidesApp.newValue = {
                    indices: Reveal.getIndices(),
                    font: font
                };
                slidesApp.execute('action:push');
            }

            slide.attr('data-font', font);

            if(!slideModel) slideModel = slidesApp.getSlideModel(slide.attr('id').replace('slide_', ''));
            slideModel.set('font', font);

            //Apply font to slide
            slide.css({
                'font-family': fontParts[0] || 'inherit',
                'font-size': fontParts[1] || 'inherit',
                'color': fontParts[2] || 'inherit'
            });
            //Replace style in all text elements
            var slideId = slideModel.get('id') || slideModel.get('tempId');
            var textElements = slidesApp.slideElementCollection.where({
                slideId: slideId,
                slideEntityType: 'text',
                toBeRemoved: false
            });
            _.each(textElements, function (model){
                //apply font-size
                model.set('fontSize', fontParts[1] || '16px');
                var properties = !_.isEmpty(model.get('properties')) ? model.get('properties') : {};
                _.each(properties, function(props){
                    props.fontSize = model.get('fontSize');
                });
                model.set('properties', properties);

                var modelView = Marionette.ItemView.Registry
                    .getByModelId(model.get('tempId') || model.get('id'));
                if( modelView ){
                    var slideElement = modelView.content;
                    var elements = slideElement.find('span');
                    _.each(elements, function (el) {
                        //Replace font in inner elems
                        jQueryValamis(el).css({
                            'font-family': fontParts[0] || 'inherit',
                            'color': fontParts[2] || 'inherit'
                        });
                    });
                    if (!slidesApp.initializing) {
                        model.set('content', slideElement.html());
                    }
                }
            });

            //Update slides thumbnails
            var slideIsLinkedTo = slidesApp.slideElementCollection.where({ correctLinkedSlideId: slideModel.id || slideModel.get('tempId') }).concat(
                slidesApp.slideElementCollection.where({ incorrectLinkedSlideId: slideModel.id || slideModel.get('tempId') })
            );
            _.each(slideIsLinkedTo, function (slideElement) {
                var slideElementDOMNode = jQueryValamis('#slideEntity_' + (slideElement.id || slideElement.get('tempId')));
                slideElementDOMNode.find('.linked-slide-thumbnail').css({
                    'font-family': fontParts[0] || 'inherit',
                    'font-size': fontParts[1] || 'inherit',
                    'color': fontParts[2] || 'inherit'
                });
                slideElementDOMNode.find('.linked-slide-thumbnail.incorrect').css({
                    'font-family': fontParts[0] || 'inherit',
                    'font-size': fontParts[1] || 'inherit',
                    'color': fontParts[2] || 'inherit'
                });
            });
        },

        applyTheme: function (slideModel, skipUndo, skipBackgroundChange) {
            var theme = slidesApp.themeModel;
            var amount = 0;
            if (slideModel){
                applyThemeForSlide(slideModel, skipUndo);
            }
            else {
                _.each(slidesApp.slideCollection.models, function(slideModel){
                    applyThemeForSlide(slideModel, skipUndo);
                });
                slidesApp.oldValue = { amount: amount, themeId: slidesApp.slideSetModel.get('themeId') };
                slidesApp.newValue = { themeId: theme.get('id')};
                slidesApp.slideSetModel.set('themeId', theme.get('id'));
                slidesApp.actionType = 'slideThemeChanged';
            }
            slidesApp.execute('action:push');

            function applyThemeForSlide(slideModel, skipUndo) {
                slidesApp.execute('reveal:page:changeFont', theme.get('font'), slideModel, skipUndo);

                var question = _.find(slideModel.get('slideElements'), function (el) {
                    return _.contains(['question', 'content'], el.slideEntityType);
                });
                if (question) {
                    var elementModel = slidesApp.getSlideElementModel(question.id || question.tempId);
                    var questionId = elementModel.get('content') || question.content;
                    var questionModel = slidesApp.questionCollection.get(questionId);
                    var questionType = questionModel.get('questionType');
                }
                slidesApp.execute('reveal:page:changeQuestionView', getQuestionAppearance(theme), questionType, slideModel, skipUndo);

                slidesApp.execute('reveal:page:changeBackground', theme.get('bgColor') || '', slideModel, skipUndo);
                amount += 3;

                if (theme.get('bgImage') && !skipBackgroundChange) {
                    var image = theme.get('bgImage');
                    var oldImage = slideModel.get('bgImage');
                    slideModel.set('bgImageChange', true);
                    var src = slidesApp.getFileUrl(theme, theme.getBackgroundImageName());
                    slidesApp.execute('reveal:page:changeBackgroundImage', image, oldImage, slideModel, src, skipUndo);
                    amount ++
                }

            }
        },
        updateSlidesContainer: function(){
            if( slidesApp.editorArea.currentView ){
                this.ui.slides.toggleClass('slides-static', !!slidesApp.isEditorReady);
            }
        },
        updateSlideHeight: function(){
            if( slidesApp.activeSlideModel ){
                slidesApp.activeSlideModel.applyLayoutProperties();
                var layoutHeight = slidesApp.activeSlideModel.get('height');
                if( !layoutHeight ){
                    var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                    layoutHeight = deviceLayoutCurrent.get('minHeight');
                }
                if(RevealModule.view && RevealModule.view.ui.work_area) {
                    RevealModule.view.ui.work_area
                        .add(RevealModule.view.ui.slides)
                        .css({ height: layoutHeight });
                    RevealModule.view.ui.work_area
                        .closest('.slides-editor-main-wrapper').scrollTop(0);
                    RevealModule.view.placeWorkArea();
                }
                if(slidesApp.GridSnapModule){
                    slidesApp.GridSnapModule.generateGrid();
                }
                window.placeSlideControls();
                slidesApp.RevealModule.configure({ height: layoutHeight });
            }
        },
        placeWorkArea: function(){
            if(this.ui && this.ui.work_area){
                var windowHeight = jQueryValamis(window).height(),
                    clientRect = this.ui.work_area.get(0).getBoundingClientRect(),
                    isPositionOnTop = windowHeight < clientRect.height + lessonStudio.fixedSizes.TOPBAR_HEIGHT;
                this.ui.work_area.toggleClass( 'top-position', isPositionOnTop );
            }
        }
    });

    RevealModule.onStart = function() {
        if( jQueryValamis('#arrangeContainer').size() == 0 ) {
            jQueryValamis('#revealEditor').append('<div id="arrangeContainer"></div>');
        }
        jQueryValamis(document.body).addClass('overflow-hidden');
        slidesApp.editorArea.$el.closest('.slides-editor-main-wrapper').show();
        revealModule.view = new RevealModule.View();

        slidesApp.commands.setHandler('reveal:page:add', function(direction, model, type) {
            var slideModel = model
                ? model
                : new lessonStudio.Entities.LessonPageModel({
                    tempId: slidesApp.newSlideId--,
                    slideSetId: slidesApp.slideSetModel.id
                });
            revealModule.view.addPage(direction, slideModel, type);
        });
        slidesApp.commands.setHandler('reveal:page:delete', revealModule.view.deleteCurrentPage);
        slidesApp.commands.setHandler('reveal:page:changeBackground', function(color,slideModel, skipUndo) {revealModule.view.changeBackground(color, slideModel, skipUndo)});
        slidesApp.commands.setHandler('reveal:page:changeBackgroundImage', function(image, oldImage, slideModel, src, skipUndo) {revealModule.view.changeBackgroundImage(image, oldImage, slideModel, src, skipUndo)});
        slidesApp.commands.setHandler('reveal:page:changeQuestionView', function (appearance, questionType, slideModel, skipUndo) {revealModule.view.changeQuestionView(appearance, questionType, slideModel, skipUndo)});
        slidesApp.commands.setHandler('reveal:page:changeFont', function (font, slideModel, skipUndo) {revealModule.view.changeFont(font, slideModel, skipUndo)});
        slidesApp.commands.setHandler('reveal:page:applyTheme', function(slideModel, skipUndo, skipBackgroundChange) {
            skipBackgroundChange = skipBackgroundChange || false;
            revealModule.view.applyTheme(slideModel, skipUndo, skipBackgroundChange)
        });
        slidesApp.commands.setHandler('reveal:page:updateRefs', function(currentPage, actionType) {revealModule.view.updateSlideRefs(currentPage, actionType)});
        slidesApp.commands.setHandler('reveal:page:makeActive', function() {
            if(!slidesApp.initializing) {
                var activeSlide = slidesApp.slideCollection.where({tempId: parseInt(jQueryValamis(Reveal.getCurrentSlide()).attr('id').replace('slide_', ''))});
                for(var i in activeSlide) {
                    slidesApp.activeSlideModel = activeSlide[i];
                }
            }
        });
        return revealModule.renderSlideset();
    };

    //TODO: remove this later (deprecated)
    RevealModule.fitContent = function(event){
        var windowHeight = window.parent ? jQueryValamis(window.parent).height() : jQueryValamis(window).height(),
            currentSlide = event && event.currentSlide ? jQueryValamis(event.currentSlide) : jQueryValamis(Reveal.getCurrentSlide()),
            slidesWrapper = jQueryValamis('.reveal-wrapper:first'),
            contentHeight = 0;
        var revealControlsPosY = windowHeight - jQueryValamis('.controls', slidesWrapper).outerHeight() - 60;
        if( windowHeight > 860 ){ revealControlsPosY += 10; }
        slidesWrapper
            .removeClass('scroll-y')
            .unbind('scroll');
        if( event && event.currentSlide ){
            slidesWrapper.scrollTop(0);
        }
        jQueryValamis('.backgrounds', slidesWrapper).css('top','auto');
        jQueryValamis('.item-content', currentSlide).each(function(i){
            if( !jQueryValamis(this).parent().hasClass('rj-video') && jQueryValamis.trim(jQueryValamis(this).text()).length > 10 ){
                var realHeight = jQueryValamis(this).css('height','auto').outerHeight(true);
                jQueryValamis(this).css('height',''); //remove height style (return to default)
                realHeight += jQueryValamis(this).closest('.rj-element').position().top;
                if( realHeight > contentHeight ){
                    contentHeight = realHeight;
                }
            }
        });
        if( contentHeight > slidesWrapper.height() ){
            RevealModule.fitContentScrollInit();
        }
    };

    RevealModule.fitContentScrollInit = function(){
        var slidesWrapper = jQueryValamis('.reveal-wrapper:first');
        slidesWrapper
            .addClass('scroll-y')
            .bind('scroll',function(){
                var windowHeight = window.parent ? jQueryValamis(window.parent).height() : jQueryValamis(window).height();
                var scrollTop = jQueryValamis(this).scrollTop();
                jQueryValamis('.backgrounds', slidesWrapper).css('top', scrollTop + 'px');
            });
    };

    RevealModule.renderSlideset = function() {
        var deferred = jQueryValamis.Deferred();
        slidesApp.editorArea.show(revealModule.view);

        slidesApp.addedSlides = [];
        slidesApp.addedSlideIndices = [];
        slidesApp.maxZIndex = 0;
        Marionette.ItemView.Registry.items = {};
        slidesApp.initializing = true;

        jQueryValamis('.slides').find('> section:gt(0)').remove();
        jQueryValamis('.slides').find('> section > section:gt(0)').remove();
        jQueryValamis('.slides').find('> section > section').empty();
        var rootSlide = slidesApp.slideCollection.findWhere({ leftSlideId: undefined, topSlideId: undefined });
        if(slidesApp.addedSlides.indexOf(rootSlide.get('tempId')) != -1)
            delete slidesApp.addedSlides[rootSlide.get('tempId')];

        if (slidesApp.addedSlides.indexOf(rootSlide.id) == -1) {
            revealModule.view.addPage('right', rootSlide);
            slidesApp.addedSlideIndices[rootSlide.id || rootSlide.get('tempId')] = Reveal.getIndices();
            if(!slidesApp.isRunning)
                var slideElements = rootSlide.getSlideElements();
            else
                var slideElements = slidesApp.slideElementCollection.where({slideId: (rootSlide.id || rootSlide.get('tempId'))});
            if (slideElements.length > 0)
                RevealModule.forEachSlideElement(slideElements);
            slidesApp.addedSlides.push(rootSlide.id || rootSlide.get('tempId'));
            RevealModule.forEachSlide(rootSlide.id || rootSlide.get('tempId'));
        }

        jQueryValamis('.slides > section:first').remove();
        Reveal.slide(0, 0);

        Reveal.sync();
        if(jQueryValamis(Reveal.getCurrentSlide()).find('.question-element').length > 0)
            jQueryValamis('.sidebar').find('span.val-icon-question').closest('div').hide();

        slidesApp.maxZIndex = _.max(_.map(jQueryValamis(Reveal.getCurrentSlide()).find('div[id^="slideEntity_"]'),
            function (item) { return $(item).find('.item-content').css('z-index'); }
        ));
        slidesApp.maxZIndex = _.isFinite(slidesApp.maxZIndex) ? slidesApp.maxZIndex : 0;

        var slideElementControls = jQueryValamis('.item-controls').find('button');
        _.each(slideElementControls, function(btn) {
            if(jQueryValamis(btn).is('.js-item-link') || jQueryValamis(btn).is('.js-item-link-incorrect')) {
                var slideElementModel = slidesApp.getSlideElementModel(jQueryValamis(btn).closest('div[id^="slideEntity_"]').attr('id').replace('slideEntity_', ''));
                if(slideElementModel && !slideElementModel.get('toBeRemoved')) {
                    var linkTypeName = jQueryValamis(btn).is('.js-item-link') ? 'correct' : 'incorrect';
                    var slideModel = slideElementModel.get(linkTypeName + 'LinkedSlideId') ? slidesApp.getSlideModel(slideElementModel.get(linkTypeName + 'LinkedSlideId')) : undefined;
                    if (slideModel) {
                        var slideBackgroundImageParts = slideModel.get('bgImage') ? slideModel.get('bgImage').split(' ') : ['', ''];
                        var slideThumbnail = jQueryValamis(btn).find('.linked-slide-thumbnail').css({
                            'background': slideBackgroundImageParts[0] + ' no-repeat',
                            'background-size': slideBackgroundImageParts[1],
                            'background-position': 'center',
                            'background-color': decodeURIComponent(slideModel.get('bgColor')) || ''
                        });
                        var slide_clone = jQueryValamis('#slide_' + slideElementModel.get(linkTypeName + 'LinkedSlideId')).clone();
                        slide_clone
                            .removeAttr('id')
                            .find('.rj-element').removeAttr('id')
                            .find('.item-controls').remove();
                        slideThumbnail.html(slide_clone.html());
                        slideThumbnail.addClass('slide-thumbnail-bordered');
                        slideThumbnail.find('.item-border, .item-controls, .ui-resizable-handle').hide();
                    } else {
                        var slideThumbnail = jQueryValamis(btn).find('.linked-slide-thumbnail').css({
                            'background-color': 'transparent',
                            'background-image': ''
                        });
                        slideThumbnail.html('');
                        slideThumbnail.removeClass('slide-thumbnail-bordered');
                    }
                }
            }
        });
        Reveal.sync();

        slidesApp.activeSlideModel = slidesApp.slideCollection.get(parseInt(jQueryValamis(Reveal.getCurrentSlide()).attr('id').replace('slide_', '')));
        slidesApp.checkIsTemplate();

        slidesApp.module('RevealControlsModule').start();
        slidesApp.initDnD();
        if (slidesApp.slideCollection.models.length > 1)
           jQueryValamis('.js-slide-delete').show();
        else
           jQueryValamis('.js-slide-delete').hide();

//        slidesApp.saveInterval = setInterval(saveSlideset, 60000, {close: false});
        slidesApp.execute('controls:place');
        slidesApp.execute('item:blur');
        if(!slidesApp.isRunning)
            lessonStudio.execute('editor-ready', slidesApp.slideSetModel);
        else
            slidesApp.execute('editor-reloaded');
        slidesApp.isRunning = true;
        jQueryValamis('#js-slide-title').attr('placeholder', Valamis.language['pageDefaultTitleLabel']);
        jQueryValamis('#js-slide-statement-object').attr('placeholder', Valamis.language['pageDefaultTitleLabel']);

        slidesApp.initializing = false;

        deferred.resolve();

        return deferred.promise();
    };

    RevealModule.forEachSlide = function(id) {
        slidesApp.slideCollection.each(function(slide) {
            if(!slide.get('toBeRemoved')) {
                if (slide.get('leftSlideId') == id || slide.get('topSlideId') == id) {
                    if (slide.id != id || slide.get('tempId') != id) {
                        var slideId = slide.id || slide.get('tempId');

                        Reveal.slide(slidesApp.addedSlideIndices[id].h, slidesApp.addedSlideIndices[id].v);
                        if (slide.get('leftSlideId') == id) {
                            revealModule.view.addPage('right', slide);
                        }
                        else if (slide.get('topSlideId') == id) {
                            revealModule.view.addPage('down', slide);
                        }
                        slidesApp.addedSlideIndices[slideId] = Reveal.getIndices();
                        if(!slidesApp.isRunning)
                            var slideElements = slide.getSlideElements();
                        else
                            var slideElements = slidesApp.slideElementCollection.where({slideId: slideId});
                        if (slideElements.length > 0)
                            RevealModule.forEachSlideElement(slideElements);

                        if(slidesApp.addedSlides.indexOf(slide.get('tempId')) != -1)
                            delete slidesApp.addedSlides[slide.get('tempId')];

                        if(slidesApp.addedSlides.indexOf(slideId) == -1)
                            slidesApp.addedSlides.push(slideId);

                        RevealModule.forEachSlide(slideId);
                    }
                }
            }
        });
    };

    RevealModule.forEachSlideElement = function(slideElements) {
        var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
            layoutSizeRatio = slidesApp.devicesCollection.getSizeRatio();

        _.each(slideElements, function(slideElementModel){
            if(!slideElementModel.get('toBeRemoved')) {
                slidesApp.execute('drag:prepare:new', slideElementModel, 0, 0);
                slidesApp.execute('item:create', false, slideElementModel);
                slidesApp.activeElement.isMoving = false;
                slideElementModel.applyLayoutProperties(deviceLayoutCurrent.get('id'), layoutSizeRatio);
                slideElementModel.trigger('change:classHidden');
            }
        });
    };

    RevealModule.configure = function( options ){
        if(Reveal.isReady()){
            Reveal.configure( options );
        }
    };

});

revealModule.on('stop', function() {
    slidesApp.isRunning = false;
    revealModule.view.destroyReveal();
    revealModule.view = null;
    jQueryValamis(document.body).removeClass('overflow-hidden');
});