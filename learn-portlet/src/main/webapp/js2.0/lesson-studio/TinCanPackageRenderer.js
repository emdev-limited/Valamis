/**
 * Created by aklimov on 18.03.15.
 */
var slidesApp = new Backbone.Marionette.Application({container: 'body', type: 'player'});

var translations = { 'typeYourAnswerLabel': 'Type your answer here' };

slidesApp.addRegions({
    editorArea: '.reveal-wrapper'
});

slidesApp.activeElement = {
    model: undefined,
    view: undefined,
    moduleName: '',
    offsetX: 0,
    offsetY: 0,
    startX: 0,
    startY: 0,
    isMoving: false,
    isResizing: false
};

slidesApp.getSlideModel = function (id) {
    return (id > 0)
        ? slidesApp.slideCollection.get(id)
        : slidesApp.slideCollection.where({tempId: id})[0];
};

var revealModule = slidesApp.module('RevealModule', function (RevealModule, slidesApp, Backbone, Marionette, $, _) {
    RevealModule.startWithParent = false;

    RevealModule.View = Marionette.ItemView.extend({
        template: '#revealTemplate',
        className: 'reveal',
        initReveal: function() {
            var that = this;
            var deviceLayout = slidesApp.devicesCollection.getCurrent();
            this.defaultParams = {
                width: deviceLayout.get('minWidth') || deviceLayout.get('maxWidth'),
                height: deviceLayout.get('minHeight'),
                controls: true,
                progress: true,
                history: true,
                center: true,
                viewDistance: 2,
                transition: 'slide', // none/fade/slide/convex/concave/zoom
                backgroundTransition: 'none', // none/fade/slide/convex/concave/zoom
                keyboard: true,
                minScale: 0.2,
                maxScale: 2.0,
                margin: 0,
                postMessageEvents: false,
                dependencies: [
                    // Syntax highlight for <code> elements
                    { src: 'plugin/highlight/highlight.js', async: true, callback: function() { hljs.initHighlightingOnLoad(); } }
                ],
                help: false
            };

            //events for touch screen
            this.$el.closest('.reveal')
                .bind('touchstart', function(e){
                    var target = e.currentTarget || e.target;
                    var touches = {
                        x: e.originalEvent.touches[0].clientX,
                        y: e.originalEvent.touches[0].clientY
                    };
                    jQuery(target).data('touches', touches);
                })
                .bind('touchmove', this.updateSwipeState.bind(this));

            Reveal.initialize(this.defaultParams);
            Reveal.slide(0, 0);
            this.$el.find('.slides').css({ overflow: 'hidden' });

            Reveal.addEventListener( 'slidechanged', function(event){
                if(!slidesApp.slideAdd && jQuery(event.currentSlide).attr('id')){
                    slidesApp.activeSlideModel = slidesApp.getSlideModel(parseInt(jQuery(event.previousSlide).attr('id').replace('slide_', '')));
                    var currentSlide = slidesApp.getSlideModel(parseInt(jQuery(event.currentSlide).attr('id').replace('slide_', '')));
                    that.setPlayerTitle(currentSlide);
                }
                that.fitContent(event);
                if(!slidesApp.initializing){
                    that.updateSlideHeight(currentSlide);
                }
            });
            Reveal.addEventListener( 'ready', function(event){
                that.setPlayerTitle(slidesApp.activeSlideModel);
                that.fitContent(event);
                RevealModule.bindEventsToControls();
                that.updateSlideHeight();
            });

        },
        fitContent: function(event){
            var currentSlide = event && event.currentSlide ? jQuery(event.currentSlide) : jQuery(Reveal.getCurrentSlide()),
                slidesWrapper = jQuery('.reveal-wrapper:first'),
                contentHeight = 0;
            slidesWrapper.scrollTop(0).removeClass('scroll-y').unbind('scroll');
            jQuery('.backgrounds',slidesWrapper).css('top','auto');
            jQuery('.controls',slidesWrapper).css('bottom','10px');

            if(!slidesApp.initializing) {
                //Style questions
                var slideModel = slidesApp.getSlideModel(parseInt(jQuery(Reveal.getCurrentSlide()).attr('id').replace('slide_', ''))),
                    appearance = getQuestionAppearance(slideModel),
                    questionElementModel = _.find(slideModel.get("slideElements"), function (el) { return el.slideEntityType == 'question'; });

                if(!questionElementModel) //We don't have questions on that slide
                    return;

                var questionType = slidesApp.questionCollection.get(questionElementModel.content).get("questionType"),
                    question = currentSlide.find('#slideEntity_' + questionElementModel.id + ' .item-content'),
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
                answers.text.css({
                    'font-family': appearance.answer.family,
                    'font-size': appearance.answer.size,
                    'color': appearance.answer.color
                });
                answers.background.css({
                    'background': 'none',
                    'border': 'none',
                    'background-color': appearance.answer.background,
                    //Icons
                    'color': appearance.answer.color
                });

                answers = question.find('.answers').find('li, h2, h4, strong');
                answers.css({
                    'background': 'none',
                    'color': appearance.question.color
                });
                //Fix for returning back to the slide
                question.find('.answers li p').attr('style', '');
            }
        },
        addPage: function (direction, slideModel) {
            var currentPage = jQuery(Reveal.getCurrentSlide());
            slidesApp.slideAdd = true;
            if (direction === 'right') {
                jQuery('<section><section></section></section>').insertAfter(currentPage.parent());
                Reveal.right();
            }
            else if (direction === 'down') {
                jQuery('<section></section>').insertAfter(currentPage);
                Reveal.down();
            }
            else
                return;

            slidesApp.slideAdd = false;
            currentPage = jQuery(Reveal.getCurrentSlide());
            currentPage.attr('id', 'slide_' + (slideModel.id || slideModel.get('tempId')));
            currentPage.attr('title', slideModel.get('title') || '');
            if(slideModel.get('bgColor'))
                currentPage.attr('data-background-color', unescape(slideModel.get('bgColor')));

            if (slideModel.get('bgImage')) {
                var bgImageParts = slideModel.get('bgImage').split(' '),
                    bgImageUrl = bgImageParts[0],
                    bgImageSize = bgImageParts[1];
                currentPage.attr('data-background', RevealModule.getFileURL(bgImageUrl, 'slide_' + slideModel.get('id')));
                currentPage.attr('data-background-repeat', 'no-repeat');
                currentPage.attr('data-background-size', bgImageSize);
                currentPage.attr('data-background-position', 'center');
            }
            if(slideModel.get('font')) {
                //Style text
                var fontParts = slideModel.get('font').split('$');
                currentPage.css({
                    'font-family': fontParts[0],
                    'font-size': fontParts[1],
                    'color': fontParts[2]
                })
            }
            Reveal.sync();
        },
        deleteCurrentPage: function() {
            var currentPage = jQuery(Reveal.getCurrentSlide());
            var currentPageSiblingsBefore = currentPage.parent().prevAll().length;
            var isOnlyPageInGroup = (jQuery('section', currentPage.parent()).length === 1);
            if (isOnlyPageInGroup) {
                // can delete the whole group and move to the right/left
                currentPage.parent().remove();
            } else {
                // can delete only section and move to the down/up
                currentPage.remove();
            }
            for(var i in slidesApp.addedSlideIndices){
                if(slidesApp.addedSlideIndices.hasOwnProperty(i)) {
                    slidesApp.addedSlideIndices[i].h--;
                }
            }
            if(currentPageSiblingsBefore > 0)
                Reveal.prev();
            else
                Reveal.slide(0, 0);

            slidesApp.activeSlideModel = slidesApp.getSlideModel(parseInt(jQuery(Reveal.getCurrentSlide()).attr('id').replace('slide_', '')));
        },
        setPlayerTitle: function (slide){
            if (slide){
                var playerTitle = slide.get('playerTitle') || PLAYER_TITLE;
                var titleElement = jQuery('#currentPackageName', window.parent.document);
                if (playerTitle == 'lesson') titleElement.html(ROOT_ACTIVITY_TITLE);
                else if (playerTitle == 'page') titleElement.html(slide.get('title'));
                else if (playerTitle =='empty') titleElement.html('');
                else titleElement.html(slide.get('title'));
            }
        },
        updateSlideHeight: function(currentSlide){
            if(!currentSlide){
                currentSlide = slidesApp.activeSlideModel;
            }
            if( currentSlide ){
                currentSlide.applyLayoutProperties();
                var scale = 1,
                    layoutHeight = parseInt(currentSlide.get('height'), 10),
                    $slides = jQuery('.slides'),
                    $scrollContainer = jQuery('.reveal-scroll-container');
                if( !layoutHeight ){
                    var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                    layoutHeight = parseInt(deviceLayoutCurrent.get('minHeight'), 10);
                }
                if( $slides.get(0).style.transform ){
                    var matches = $slides.get(0).style.transform.match(/scale\((.*)\)/);
                    if( matches.length >= 2 ){
                        scale = parseFloat( matches[1] );
                    }
                }
                $slides.css({ height: layoutHeight });
                $scrollContainer.css({ height: layoutHeight * scale });
                jQuery('.reveal-scroll-wrapper').scrollTop(0);
                RevealModule.configure({ height: layoutHeight });
            }
        },
        /** Disable swipe on touch screen if slide have scroll */
        updateSwipeState: function(e){
            var target = e.currentTarget || e.target,
                $element = jQuery(target),
                $wrapper = $element.closest('.reveal-scroll-wrapper'),
                lastTouches = $element.data('touches'),
                currentX = e.originalEvent.touches[0].clientX,
                currentY = e.originalEvent.touches[0].clientY,
                deltaX = currentX - lastTouches.x,
                deltaY = currentY - lastTouches.y;
            $element.removeAttr('data-prevent-swipe');
            //if is vertical swipe
            if( Math.abs(deltaY) > Math.abs(deltaX) ){
                var height = $wrapper.height(),
                    scrollHeight = $wrapper.get(0).scrollHeight,
                    scrollTop = $wrapper.scrollTop(),
                    scrollMax = scrollHeight - height;
                var preventSwipe = ( deltaY > 0 && scrollMax > 0 && scrollTop > 0 )//swipe up
                    || ( deltaY < 0 && scrollMax > 0 && scrollTop < scrollMax );//swipe down
                if( preventSwipe ){
                    $element.attr('data-prevent-swipe', '1');//prevent swipe
                }
            }
        }
    });

    RevealModule.getFileURL = function(content, folderName, model) {
        var data = content || '#1C1C1C';
        if(model && model.get('slideEntityType') === 'pdf')
            data = 'pdf/web/viewer.html?file=../../resources/' + folderName + '/' + content;
        else {
            if (content.indexOf('url("../') == 0)
                data = content.replace('url("../', '').replace('")', '');
            else if (content.indexOf('/') == -1)
                data = 'resources/' + folderName + '/' + content;
            else if (content.indexOf('/documents') == 0) {
                var folderName = /entryId=([^&]*)/g.exec(content)[1];
                var fileName = /documents\/[^/]*\/[^/]*\/([^/]*)/g.exec(content)[1];
                var fileExt = /ext=([^&]*).*"\)/g.exec(content)[1];
                data = 'resources/' + folderName + '/' + fileName + '.' + fileExt;
            }
            else if (content.indexOf('/documents/') == 0) {
                var folderName = /([^/?]*)\?groupId=/g.exec(content)[1];
                var fileName = /documents\/[^/]*\/[^/]*\/([^/]*)/g.exec(content)[1];
                var fileExt = /ext=([^&]*).*/g.exec(content)[1];
                data = 'resources/' + folderName + '/' + fileName + '.' + fileExt;
            }
            else if (content.indexOf('docs.google.com/file/d/') != -1 || content.indexOf('youtube.com/') != -1)
                data = content.replace(/watch\?v=/g, 'embed/');
        }
        return data;
    };

    var revealView = new RevealModule.View();

    RevealModule.forEachSlide = function (id) {
        slidesApp.slideCollection.each(function (slide) {
            if (slide.get('leftSlideId') === id || slide.get('topSlideId') === id) {
                if (slidesApp.addedSlides.indexOf(slide.id) === -1) {
                    Reveal.slide(slidesApp.addedSlideIndices[id].h, slidesApp.addedSlideIndices[id].v);
                    if (slide.get('leftSlideId') === id) {
                        revealView.addPage('right', slide);
                    }
                    else if (slide.get('topSlideId') === id) {
                        revealView.addPage('down', slide);
                    }
                    slidesApp.addedSlideIndices[slide.id] = Reveal.getIndices();
                    var slideElements = slide.get('slideElements');
                    slide.slideElements = new lessonStudioCollections.LessonPageElementCollection(slideElements);
                    RevealModule.forEachSlideElement(slide.slideElements);
                    slidesApp.addedSlides.push(slide.id);
                    RevealModule.forEachSlide(slide.id);
                }
            }
        });
    };

    RevealModule.forEachSlideElement = function (slideElements) {
        slideElements.each(function (model) {
            slidesApp.activeElement.model = model;
            slidesApp.activeElement.view = undefined;
            slidesApp.activeElement.moduleName = model.get('slideEntityType').charAt(0).toUpperCase() + model.get('slideEntityType').slice(1) + 'ElementModule';

            var view = new slidesApp.TinCanPackageGenericItem.GenericItemView({model: model});
            var elem = view.render().$el;
            elem.attr('id', 'slideEntity_' + (model.id || model.get('tempId')));
            jQuery(Reveal.getCurrentSlide()).append(elem);
            if(model.get('slideEntityType') === 'math')
                view.content.find('.math-content').fitTextToContainer(view.$el, true);
            view.bindTrackballControls();
            slidesApp.activeElement.view = view;
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
            model.applyLayoutProperties(deviceLayoutCurrent.get('id'));
        });
    };

    RevealModule.renderSlideset = function() {
        slidesApp.editorArea.on('show', revealView.initReveal.bind(revealView));
        slidesApp.editorArea.show(revealView);
        slidesApp.addedSlides = [];
        slidesApp.addedSlideIndices = [];
        slidesApp.playerCheckIntervals = [];
        slidesApp.initializing = true;
        jQuery('.slides').find('> section:gt(0)').remove();
        jQuery('.slides').find('> section > section:gt(0)').remove();
        jQuery('.slides').css('border', 'none');
        var rootSlide = slidesApp.slideCollection.where({ leftSlideId: undefined, topSlideId: undefined })[0];
        if (slidesApp.addedSlides.indexOf(rootSlide.id) === -1) {
            revealView.addPage('right', rootSlide);
            slidesApp.addedSlideIndices[rootSlide.id] = Reveal.getIndices();
            var slideElements = rootSlide.get('slideElements');
            rootSlide.slideElements = new lessonStudioCollections.LessonPageElementCollection(slideElements);
            if (slideElements.length > 0)
                RevealModule.forEachSlideElement(rootSlide.slideElements);
            slidesApp.addedSlides.push(rootSlide.id);
            RevealModule.forEachSlide(rootSlide.id);
        }
        Reveal.slide(0, 0);
        revealView.deleteCurrentPage();
        Reveal.sync();
        window.slideId = $(Reveal.getCurrentSlide()).attr('id');
        window.slideTitle = slidesApp.getSlideModel(parseInt($(Reveal.getCurrentSlide()).attr('id').replace('slide_', ''))).get('title');
        slidesApp.initializing = false;
    };

    RevealModule.bindEventsToControls = function() {
        var pointerEvents = navigator.userAgent.match( /android/gi )
            ? [ 'touchstart' ]
            : [ 'touchstart', 'click' ];
        Reveal.removeEventListeners();
        pointerEvents.forEach( function( eventName ) {
            jQuery('.reveal-wrapper .controls > [class^="navigate-"]').each(function(){
                jQuery(this).get(0)
                    .addEventListener( eventName, RevealModule.onControlBeforeAction, false );
            });
        });
        Reveal.addEventListeners();
    };

    RevealModule.onControlBeforeAction = function(e) {

        var currentSlide = Reveal.getCurrentSlide(),
            currentSlideId = jQuery(currentSlide).attr('id').replace('slide_',''),
            currentStateId = jQuery(currentSlide).data('state'),
            questionIdWithNumber = currentStateId ? currentStateId.replace(currentStateId.split('_')[0]+'_', '') : undefined;

        if( questionIdWithNumber ){

            var currentSlideModel = slidesApp.slideCollection.get( { id: currentSlideId }),
                questionElement = _.find(currentSlideModel.get('slideElements'), function(slideElement) {
                    return slideElement.slideEntityType == 'question';
                }),
                questionResults = TinCanCourseHelpers['collectAnswers_' + questionIdWithNumber]
                    ? TinCanCourseHelpers['collectAnswers_' + questionIdWithNumber]()
                    : undefined;

            if( questionElement && questionResults && ( questionElement.correctLinkedSlideId || questionElement.incorrectLinkedSlideId ) ) {
                var nextSlideId = questionResults.isPassed ? questionElement.correctLinkedSlideId : questionElement.incorrectLinkedSlideId;
                if( nextSlideId && slidesApp.addedSlideIndices[parseInt(nextSlideId)] ) {
                    var slideIndices = slidesApp.addedSlideIndices[parseInt(nextSlideId)];
                    Reveal.slide(slideIndices.h, slideIndices.v);
                    e.stopImmediatePropagation();
                }
            }
        }
    };

    RevealModule.configure = function( options ){
        if(Reveal.isReady()){
            Reveal.configure( options );
        }
    };

    RevealModule.onResize = function(){
        var STEP_OFFSET = 2;
        var newWidth = 0,
            newDeviceId = null,
            windowWidth = jQuery(window).width(),
            deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
        slidesApp.devicesCollection.each(function(model){
            var deviceMinWidth = model.get('name') == 'phone'
                ? 0//phone start from width = 0
                : model.get('minWidth') - STEP_OFFSET;
            if( windowWidth >= deviceMinWidth
                && model.get('selected')
                && deviceMinWidth >= newWidth ){
                newWidth = deviceMinWidth;
                newDeviceId = model.get('id');
            }
        });
        if( newDeviceId && (!deviceLayoutCurrent || newDeviceId != deviceLayoutCurrent.get('id')) ){
            var newDevice = slidesApp.devicesCollection.findWhere({ id: newDeviceId });
            newDevice.set('active', true);
        }
        revealView.updateSlideHeight();
    };

});

slidesApp.on('start', function() {
    if(typeof slidesJson !== 'undefined') {
        slidesApp.slideCollection = new lessonStudioCollections.LessonPageCollection(slidesJson);
        slidesApp.slideElementCollection = new lessonStudioCollections.LessonPageElementCollection({});
    }
    if(typeof questionsJson !== 'undefined')
        slidesApp.questionCollection = new Backbone.Collection(questionsJson);

    if(typeof plaintextsJson !== 'undefined')
        slidesApp.plaintextsCollection = new Backbone.Collection(plaintextsJson);

    if(typeof randomQuestionJson!== 'undefined')
        slidesApp.randomQuestionCollection = new Backbone.Collection(randomQuestionJson);

    if(typeof randomPlaintextJson!== 'undefined')
        slidesApp.randomPlainTextCollection = new Backbone.Collection(randomPlaintextJson);

    slidesApp.devicesCollection = new lessonStudioCollections.LessonDeviceCollection(devicesJson);
    slidesApp.devicesCollection.on('change:active',function(deviceModel, active){
        if(active){
            this.each(function(item){
                if( item.get('id') != deviceModel.get('id') ){
                    item.set('active', false);
                }
            });
            var deviceId = deviceModel.get('id');
            slidesApp.slideCollection.each(function(slide){
                if(slide.slideElements){
                    slide.slideElements.each(function(model){
                        model.applyLayoutProperties(deviceId);
                    });
                }
            });
            slidesApp.RevealModule.configure({
                width: deviceModel.get('minWidth')
            });
            if(revealModule.currentView){
                revealModule.currentView.updateSlideHeight();
            }
        }
    });
    slidesApp.devicesCollection.setSelectedDefault();
    revealModule.onResize();
    slidesApp.questionsArray =[];
    revealModule.start();
    jQuery(window).bind( 'resize', revealModule.onResize );
});

revealModule.on('start', function(options){
    revealModule.renderSlideset();
});
