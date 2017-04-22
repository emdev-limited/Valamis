/**
 * Created by aklimov on 18.03.15.
 */
var slidesApp = new Backbone.Marionette.Application({container: 'body', type: 'player'});

var translations = { 'typeYourAnswerLabel': 'Type your answer here' };

slidesApp.addRegions({
    editorArea: '.reveal-wrapper'
});

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

            // Full list of configuration options available here:
            // https://github.com/hakimel/reveal.js#configuration
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
                maxScale: (deviceLayout.get('name') == 'desktop') ? 1.0 : 2.0,
                margin: 0,
                postMessageEvents: false,
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
                    $(target).data('touches', touches);
                })
                .bind('touchmove', this.updateSwipeState.bind(this));

            Reveal.initialize(this.defaultParams);

            //TODO move to css
            this.$('.slides').css({ overflow: 'hidden' });

            Reveal.addEventListener( 'slidechanged', function(event){
                if($(event.currentSlide).attr('id')){
                    slidesApp.activeSlideModel = slidesApp.getSlideModel(parseInt($(event.previousSlide).attr('id').replace('slide_', '')));
                    var currentSlide = slidesApp.getSlideModel(parseInt($(event.currentSlide).attr('id').replace('slide_', '')));
                    that.setPlayerTitle(currentSlide);
                }
                if(!slidesApp.initializing){
                    that.updateSlideHeight(currentSlide);
                }
            });
            Reveal.addEventListener( 'ready', function(event){
                that.setPlayerTitle(slidesApp.activeSlideModel);
                RevealModule.bindEventsToControls();
                that.updateSlideHeight();
            });

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
        getSlidesScale: function(){
            var $slides = this.$('.slides'),
                scale = 1;
            if( $slides.get(0).style.transform ){
                var matches = $slides.get(0).style.transform.match(/scale\((.*)\)/);
                if( matches.length >= 2 ){
                    scale = parseFloat( matches[1] );
                }
            }
            return scale;
        },
        updateSlideHeight: function(currentSlide){
            var that = this;
            if(!currentSlide){
                currentSlide = slidesApp.activeSlideModel;
            }
            if( currentSlide ){
                _.defer(function(){
                    currentSlide.applyLayoutProperties();
                    var layoutHeight = parseInt(currentSlide.get('height'), 10),
                        $slides = that.$('.slides'),
                        $scrollContainer = that.$el.closest('.reveal-scroll-container');
                    if( !layoutHeight ){
                        var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                        layoutHeight = parseInt(deviceLayoutCurrent.get('minHeight'), 10);
                    }
                    layoutHeight = Math.max(layoutHeight, that.getContentHeight());
                    $slides.css({ height: layoutHeight });
                    $scrollContainer.css({ height: layoutHeight });
                    that.$el.closest('.reveal-scroll-wrapper').scrollTop(0);
                    RevealModule.configure({ height: layoutHeight });

                    var scale = that.getSlidesScale();
                    $scrollContainer.css({ height: layoutHeight * scale });
                });
            }
        },
        getContentHeight: function(){
            var $currentSlide = $(Reveal.getCurrentSlide());
            if( $currentSlide.find('.question-element').size() > 0 ){
                return this.getQuestionHeight($currentSlide);
            }
            if ($currentSlide.find('.js-lesson-questions').size() > 0){
                return this.getSummaryHeight($currentSlide);
            }
            return 0;
        },
        getSummaryHeight: function($currentSlide){
            var height = 0;
            var $summaryEl = $currentSlide.find('.js-lesson-questions');
            if($summaryEl) {
                height += $summaryEl.position()['top'];
                height += $summaryEl.height() + 215;
            }
            return height;
        },
        getQuestionHeight: function($currentSlide){
            var  height = 0;
            var $questionEl = $currentSlide.find('.question-element:first');
            height += $questionEl.position()['top'];
            height += $questionEl.outerHeight() + 15;
            return height;
        },
        /** Disable swipe on touch screen if slide have scroll */
        updateSwipeState: function(e){
            var target = e.currentTarget || e.target,
                $element = $(target),
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

    RevealModule.slideSet = {};

    RevealModule.createSlides = function($slides, parentId) {

        //need addedSlideIndices for getting link to another slide
        var registerSlide = function(slideId, slideIndices){
            slidesApp.addedSlides.push(slideId);
            slidesApp.addedSlideIndices[slideId] = slideIndices;
        };

        var shouldBeAdded = function(slide){
            return (!!slide && slidesApp.addedSlides.indexOf(slide.id) === -1);
        };

        var addBottomSlides = function(slide, $slide, parentIndex){

            var bottomSlide = slidesApp.slideCollection.find(function (item) {
                return item.get('topSlideId') === slide.id;
            });

            if(shouldBeAdded(bottomSlide)) {

                var $bottomSlide = RevealModule.createRevealSlide(bottomSlide);

                $slide.after($bottomSlide);

                var slideIndex = { h: parentIndex.h, v: (parentIndex.v + 1)};

                registerSlide(bottomSlide.get('id'), slideIndex);

                addBottomSlides(bottomSlide, $bottomSlide, slideIndex);
            }
        };

        var addRightSlide = function(leftSlide, $leftSlide, parentIndex){

            var rightSlide = slidesApp.slideCollection.find(function (item) {
                return item.get('leftSlideId') === leftSlide.id;
            });

            if(shouldBeAdded(rightSlide)) {

                var $rightSlide = RevealModule.createRevealSlide(rightSlide);

                var $wrapper = $('<section></section>');

                $wrapper.append($rightSlide);

                $leftSlide.parent().parent().append($wrapper);

                var slideIndex = { h: (parentIndex.h + 1), v: parentIndex.v};

                registerSlide(rightSlide.get('id'), slideIndex);

                addBottomSlides(rightSlide, $rightSlide, slideIndex);

                addRightSlide(rightSlide, $rightSlide, slideIndex);
            }
        };

        var slide = slidesApp.slideCollection.find(function (item) {
            return item.get('id') === parentId;
        });

        if(shouldBeAdded(slide)) {

            var $slide = RevealModule.createRevealSlide(slide);

            var $wrapper = $('<section></section>');

            $wrapper.append($slide);

            var slideIndex = {h: 0, v: 0};

            registerSlide(slide.get('id'), slideIndex);

            $slides.append($wrapper);

            addBottomSlides(slide, $slide, slideIndex);

            addRightSlide(slide, $slide, slideIndex);
        }
    };

    RevealModule.createRevealSlide = function(slideModel){

        var $slideElem = $('<section></section>');

        $slideElem.attr('id', 'slide_' + (slideModel.id || slideModel.get('tempId')));
        $slideElem.attr('title', slideModel.get('title') || '');
        if(slideModel.get('bgColor')){
            $slideElem.attr('data-background-color', unescape(slideModel.get('bgColor')));
        }

        if (slideModel.get('bgImage')) {
            var bgImageParts = slideModel.get('bgImage').split(' '),
                bgImageUrl = bgImageParts[0],
                bgImageSize = bgImageParts[1];
            $slideElem.attr('data-background', RevealModule.getFileURL(bgImageUrl, 'slide_' + slideModel.get('id')));
            $slideElem.attr('data-background-repeat', 'no-repeat');
            $slideElem.attr('data-background-size', bgImageSize);
            $slideElem.attr('data-background-position', 'center');
        }
        if(slideModel.get('font')) {
            //Style text
            var fontParts = slideModel.get('font').split('$');
            $slideElem.css({
                'font-family': fontParts[0],
                'font-size': fontParts[1],
                'color': fontParts[2]
            })
        }

        var slideElements = slideModel.get('slideElements');

        slideModel.slideElements = new baseLessonStudioCollections.LessonPageElementCollection(slideElements);

        RevealModule.addElementsToSlide(slideModel.slideElements, $slideElem);

        return $slideElem;
    };

    RevealModule.addElementsToSlide = function (slideElements, $currentSlide) {
        slideElements.each(function (model) {
            var slideEntityType = model.get('slideEntityType');

            var view = new slidesApp.TinCanPackageGenericItem.GenericItemView({model: model});
            var $elem = view.render().$el;

            //TODO - move into view
            $elem.attr('id', 'slideEntity_' + (model.id || model.get('tempId')));
            $elem.addClass('lesson-element');
            $currentSlide.append($elem);

            if(model.isQuestion()){
                $currentSlide.data('state', view.dataState);
            }

            if(slideEntityType === 'math'){
                view.content.find('.math-content').fitTextToContainer(view.$el, true);
            }

            view.bindTrackballControls();

            // var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
            // model.applyLayoutProperties(deviceLayoutCurrent.get('id'));
        });
    };

    RevealModule.renderSlideset = function() {
        slidesApp.editorArea.on('show', revealView.initReveal.bind(revealView));
        slidesApp.editorArea.show(revealView);
        slidesApp.addedSlides = [];
        slidesApp.addedSlideIndices = [];
        slidesApp.playerCheckIntervals = [];
        slidesApp.initializing = true;
        var $slides = $('.slides');
        $slides.html('');

        //TODO move into css
        $slides.css('border', 'none');

        var rootSlide = slidesApp.slideCollection.where({ leftSlideId: undefined, topSlideId: undefined })[0];
        if (slidesApp.addedSlides.indexOf(rootSlide.id) === -1) {
            RevealModule.createSlides($slides, rootSlide.id);
        }

        Reveal.slide(0, 0);
        Reveal.sync();
        //needed for resize
        slidesApp.activeSlideModel = rootSlide;
        //update first slide
        RevealModule.onResize();

        window.slideId = 'slide_' + rootSlide.id;
        window.slideTitle = slidesApp.getSlideModel(rootSlide.id).get('title');

        slidesApp.initializing = false;
    };

    RevealModule.bindEventsToControls = function() {
        var pointerEvents = navigator.userAgent.match( /android/gi )
            ? [ 'touchstart' ]
            : [ 'touchstart', 'click' ];
        Reveal.removeEventListeners();
        pointerEvents.forEach( function( eventName ) {
            $('.reveal-wrapper .controls > [class^="navigate-"]').each(function(){
                $(this).get(0)
                    .addEventListener( eventName, RevealModule.onControlBeforeAction, false );
            });
        });
        Reveal.addEventListeners();
    };

    RevealModule.onControlBeforeAction = function(e) {

        var $currentSlide = $(Reveal.getCurrentSlide()),
            currentSlideId = $currentSlide.attr('id').replace('slide_',''),
            currentStateId = $currentSlide.data('state'),
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
            windowWidth = $(window).width(),
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
        slidesApp.slideCollection = new baseLessonStudioCollections.LessonPageCollection(slidesJson);
        slidesApp.slideElementCollection = new baseLessonStudioCollections.LessonPageElementCollection({});
    }
    if(typeof questionsJson !== 'undefined')
        slidesApp.questionCollection = new Backbone.Collection(questionsJson);

    if(typeof plaintextsJson !== 'undefined')
        slidesApp.plaintextsCollection = new Backbone.Collection(plaintextsJson);

    if(typeof randomQuestionJson!== 'undefined')
        slidesApp.randomQuestionCollection = new Backbone.Collection(randomQuestionJson);

    if(typeof randomPlaintextJson!== 'undefined')
        slidesApp.randomPlainTextCollection = new Backbone.Collection(randomPlaintextJson);

    slidesApp.devicesCollection = new baseLessonStudioCollections.LessonDeviceCollection(devicesJson);
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
    jQuery(window).bind( 'resize', _.debounce(revealModule.onResize, 250));
});

revealModule.on('start', function(options){
    revealModule.renderSlideset();
});
