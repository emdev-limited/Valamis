var revealModule = slidesApp.module('RevealModule', function (RevealModule, App, Backbone, Marionette, $, _) {
    RevealModule.startWithParent = false;

    RevealModule.View = Marionette.ItemView.extend({
        template: '#revealTemplate',
        className: 'reveal',
        ui: {
            'slides': '.slides'
        },
        initialize: function(options) {
            options = options || {};
            this.slideSetModel = options.slideSetModel || slidesApp.slideSetModel;
        },
        onShow: function(){
            this.bindUIElements();
            this.ui.main_wrapper = this.$el.closest('.slides-editor-main-wrapper');
            this.ui.work_area = this.$el.closest('.slides-work-area-wrapper');
            this.ui.reveal_wrapper = this.$el.closest('.reveal-wrapper');
            this.ui.main_wrapper
                .unbind('scroll')
                .bind('scroll', function(){
                    App.vent.trigger('containerScroll');
                });
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
                fragments: false,
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

            this.$el.unbind('slidechanged');
            this.$el.bind('slidechanged', this.onSlideChanged.bind(this));

            this.$el.unbind('ready');
            this.$el.bind('ready', this.onRevealReady.bind(this));
            slidesApp.execute('controls:place');
        },
        onRevealReady: function () {
            revealControlsModule.view.model.clear({silent: true});
            revealControlsModule.view.model.set(slidesApp.activeSlideModel.toJSON());
            revealControlsModule.view.updateTopDownNavigation(!!(slidesApp.slideSetModel.get('topDownNavigation')));
            slidesApp.initializing = true;
            this.updateSlideHeight();
            slidesApp.initializing = false;
        },
        onSlideChanged: function () {

            if (slidesApp.mode == 'versions') return;

            var $currentSlide = $(Reveal.getCurrentSlide());
            slidesApp.maxZIndex = _.max(_.map($currentSlide.find('div[id^="slideEntity_"]'),
                function (item) {
                    return $(item).find('.item-content').css('z-index');
                }
            ));

            slidesApp.maxZIndex = _.isFinite(slidesApp.maxZIndex) ? slidesApp.maxZIndex : 0;

            if ($currentSlide.attr('id')) {
                slidesApp.activeSlideModel = slidesApp.getSlideModel(parseInt($currentSlide.attr('id').replace('slide_', '')));
            }
            //TODO maybe check slides collection??
            if ($('.slides > section > section').length > 1) {
                $('.js-slide-delete').show();
            } else {
                $('.js-slide-delete').hide();
            }

            slidesApp.checkIsTemplate();

            revealControlsModule.view.model.clear({silent: true});
            revealControlsModule.view.model.set(slidesApp.activeSlideModel.toJSON());


            var iconQuestionDiv = $('.sidebar').find('span.val-icon-question').closest('div');
            if (slidesApp.activeSlideModel.hasQuestions()) {
                iconQuestionDiv.hide();
            }
            else {
                iconQuestionDiv.show();
            }

            this.updateSlideHeight();
            if (revealModule.view) {
                revealModule.view.placeWorkArea();
            }

            if (App.mode == 'edit') {
                RevealModule.selectableInit();
            } else {
                RevealModule.selectableDestroy();
            }
        },
        destroyReveal: function(){
            Reveal.removeEventListeners();
            this.destroy();
        },
        restartReveal: function(){
            if(Reveal.isReady()){
                Reveal.removeEventListeners();
            }
            RevealModule.view.initReveal();
        },
        addPage: function(slideModel) {
            var leftSlideId = slideModel.get('leftSlideId'),
                topSlideId = slideModel.get('topSlideId');

            var isRoot = _.isUndefined(leftSlideId) && _.isUndefined(topSlideId);

            slidesApp.execute('item:blur');
            slidesApp.activeSlideModel = slideModel;

            //Insert slide element
            var $newSlide = RevealModule.createRevealSlide(slideModel);

            if(!!topSlideId){
                var $topSlide = $('#slide_' + topSlideId);
                $topSlide.after($newSlide);

            } else if(!!leftSlideId){

                var $leftSlide = $('#slide_' + leftSlideId);
                var $wrapper = $('<section/>');
                $wrapper.append($newSlide);
                $leftSlide.parent().after($wrapper);

            } else if(isRoot) {
                //first check bottom slide
                var bottomSlide = slidesApp.slideCollection.find(function (model) {
                    return ( model.get('topSlideId') == slideModel.getId()
                    && !model.get('toBeRemoved'));
                });

                if(!!bottomSlide) {
                    var $bottomSlide = $('#slide_' + bottomSlide.getId());
                    $bottomSlide.parent().prepend($newSlide);
                }else {
                    var firstSlide = slidesApp.slideCollection.find(function (model) {
                        return (!model.get('leftSlideId') && slideModel.getId() != model.getId()
                        && !model.get('toBeRemoved'));
                    });
                    firstSlide.set('leftSlideId', slideModel.getId());
                    var $firstSlide = $('#slide_' + firstSlide.getId());
                    var $wrapper = $('<section/>');
                    $wrapper.append($newSlide);
                    $firstSlide.parent().parent().prepend($wrapper);
                }

            } else {
                console.error('Cannot find slide to add new slide');
            }

            var slideIndices = Reveal.getIndices($newSlide.get(0));

            Reveal.slide( slideIndices.h, slideIndices.v);
            slidesApp.slideRegistry.register(slideModel.getId(), slideIndices);

            slidesApp.execute('reveal:page:changeBackground', slideModel.get('bgColor'), slideModel);
            slidesApp.execute('reveal:page:changeBackgroundImage', slideModel.get('bgImage'), slideModel);

            if (!slideModel.get('title')) slideModel.set('title', '');

            slidesApp.execute('reveal:page:updateRefs', $newSlide, 'add');

            if(!slideModel.get('isSlideCopy') && slidesApp.slideSetModel.get('themeId')) {
              _.defer(function () {
                slidesApp.execute('reveal:page:applyTheme', slideModel, !!slideModel.has('bgPdf'));
              });
            }
        },
        onSlideAdd: function(model){
            if(!slidesApp.initializing) {
                this.addPage(model);
                if (slidesApp.mode == 'arrange'){
                    arrangeModule.onSlidesUpdated();
                }
            }
        },
        onEditorModeChanged: function(){
            if(App.mode != 'edit' || slidesApp.isEditing){
                RevealModule.selectableDestroy();
            } else {
                RevealModule.selectableInit();
            }
        },
        deletePage: function(slideModel) {
            slideModel = slideModel || slidesApp.activeSlideModel;
            if(!slideModel || !slideModel.get('toBeRemoved')){
                return;
            }
            $('.sidebar').find('.question-element').first().remove();
            var currentSlideId = slideModel.getId();
            var currentPage = slidesApp.mode == 'edit' ? $(' #slide_' + currentSlideId) : $(' #slidesArrangeTile_' + currentSlideId);

            if(!slidesApp.initializing) {
                if(slidesApp.mode == 'edit')
                   slidesApp.historyManager.groupOpenNext();

                var correctLinkedSlideElements = slidesApp.slideElementCollection.where({ correctLinkedSlideId: currentSlideId });
                var incorrectLinkedSlideElements = slidesApp.slideElementCollection.where({ incorrectLinkedSlideId: currentSlideId });
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

                //delete elements
                var slideElements = slideModel.getElements();
                _.each(slideElements, function(model){
                    model.set('toBeRemoved', true);
                });

                _.defer(function(){
                    var hasParent = currentPage.prevAll().length > 0  ||
                        currentPage.parent().prevAll().length > 0;
                    slidesApp.slideRegistry.remove(currentSlideId);
                    slidesApp.execute('reveal:page:updateRefs', currentPage, 'delete');
                    RevealModule.view.deletePageElement(currentSlideId);
                    RevealModule.view.navigationRefresh(hasParent);
                    slidesApp.historyManager.groupClose();
                });
            }
        },
        navigationRefresh: function(hasParent){
            if(hasParent)
                Reveal.prev();
            else
                Reveal.slide(0, 0);
            Reveal.sync();
            slidesApp.selectedItemView = null;
            slidesApp.execute('reveal:page:makeActive');
            if(Reveal.getTotalSlides() == 1){
                $(' .js-slide-delete').hide();
            }
        },
        deletePageElement: function(slideId){
            if(Reveal.getTotalSlides() == 1){
                return;
            }
            var $slideElement = $(' #slide_' + slideId);
            if( $slideElement.length > 0 ){
                if($slideElement.parent('section').length > 0
                    && $slideElement.parent('section').children('section').length == 1){
                        $slideElement.parent('section').remove();
                } else {
                    $slideElement.remove();
                }
            }
        },
        updateSlideRefs: function(currentPage, actionType) {
            //TODO change here leftSlideId and top slideId only
            var getSlideElementId = function ($slide) {
                return parseInt($slide.attr('id').replace('slide_', ''));
            };

            var nextPageRight = currentPage.parent().next().children('section[id^="slide_"]').first();
            var nextPageDown = currentPage.next('section[id^="slide_"]');
            var prevPageLeft = currentPage.parent().prev().children('section[id^="slide_"]').first();
            var prevPageUp = currentPage.prev('section[id^="slide_"]');
            switch(actionType) {
                case 'add':
                    if(nextPageRight.length > 0 && currentPage.prevAll('section').length === 0) {
                        var idToChangeLeft = getSlideElementId(nextPageRight);
                        var newLeftId = getSlideElementId(currentPage);
                        slidesApp.getSlideModel(idToChangeLeft).set('leftSlideId', newLeftId);
                    }
                    if(nextPageDown.length > 0) {
                        var idToChangeTop = getSlideElementId(nextPageDown);
                        var newTopId = getSlideElementId(currentPage);
                        slidesApp.getSlideModel(idToChangeTop).set('topSlideId', newTopId);
                    }

                    if(prevPageUp.length > 0)
                        slidesApp.activeSlideModel.set('topSlideId', getSlideElementId(prevPageUp));
                    else if(prevPageLeft.length > 0)
                        slidesApp.activeSlideModel.set('leftSlideId', getSlideElementId(prevPageLeft));
                    break;
                case 'delete':
                    if(nextPageRight.length > 0 && prevPageUp.length == 0) {
                        var idToChangeLeft = getSlideElementId(nextPageRight);
                        var newLeftId = (nextPageDown.length > 0)
                            ? getSlideElementId(nextPageDown)
                            : (prevPageLeft.length > 0)
                                ? getSlideElementId(prevPageLeft.first())
                                : undefined;
                        slidesApp.getSlideModel(idToChangeLeft).set('leftSlideId', newLeftId);
                    }
                    if(nextPageDown.length > 0) {
                        var idToChangeTop = getSlideElementId(nextPageDown);
                        var newTopId = (currentPage.prev('section[id^="slide_"]').length > 0)
                            ? getSlideElementId(currentPage.prev('section[id^="slide_"]'))
                            : undefined;

                        slidesApp.getSlideModel(idToChangeTop).set('topSlideId', newTopId);
                    }
                    if (prevPageLeft.length > 0  && nextPageDown.length > 0 && prevPageUp.length == 0 ){
                        var idToAddLeft = getSlideElementId(nextPageDown);
                        var newLeftId = getSlideElementId(prevPageLeft);
                        slidesApp.getSlideModel(idToAddLeft).set('leftSlideId', newLeftId);
                    }
                    break;
            }
        },
        getSlideByModel: function(slideModel) {
            var slideId = slideModel.getId();
            return $(' #slide_' + slideId);
        },
        changeBackground: function(color, slideModel) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var slide = this.getSlideByModel(slideModel);
            slideModel.set('bgColor', color);
            slide.attr('data-background-color', color);
            Reveal.sync();
            if (slidesApp.mode == 'arrange'){
                arrangeModule.changeBackgroundColor(slideModel.getId());
            }
        },
        changeBackgroundImage: function(image, slideModel) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var $slide = this.getSlideByModel(slideModel);
            RevealModule.updateBackgroundImage($slide, slideModel, image);
            Reveal.sync();
            if (slidesApp.mode == 'arrange'){
                arrangeModule.changeBackgroundImage(slideModel.getId());
            }
        },
        changeFont: function (font, slideModel) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var slide = this.getSlideByModel(slideModel);

            var fontData = slideModel.getFont(font);

            slide.attr('data-font', font);

            //Apply font to slide
            slide.css({
                'font-family': fontData.fontFamily,
                'font-size': fontData.fontSize,
                'color': fontData.fontColor
            });
            //TODO change in slideModel.get('slideElement')
            //Replace style in all text elements
            var slideId = slideModel.get('id') || slideModel.get('tempId');
            var textElements = slidesApp.slideElementCollection.where({
                slideId: slideId,
                slideEntityType: 'text',
                toBeRemoved: false
            });
            _.each(textElements, function (model){
                var modelView = Marionette.ItemView.Registry
                    .getByModelId(model.get('tempId') || model.get('id'));
                if( modelView ){
                    var elements = modelView.content.find('span');
                    _.each(elements, function (el) {
                        //Replace font in inner elements
                        if($(el).css('color')){
                            $(el).css('color', fontData.fontColor);
                        }
                        if($(el).css('font-family')){
                            $(el).css('font-family', fontData.fontFamily);
                        }
                    });
                    if (!slidesApp.initializing) {
                        model.set('content', modelView.content.html());
                    }
                }
            });
            if (slidesApp.mode == 'arrange'){
                arrangeModule.changeFont(slideModel.getId());
            }
        },

        applyTheme: function (slideModel, skipBackgroundChange) {
            var theme = slidesApp.themeModel;
            slidesApp.historyManager.groupOpenNext();
            if (slideModel){
                $.when(applyThemeForSlide(slideModel)).then(function() {
                    slidesApp.historyManager.groupClose();
                });
            }
            else {
                slidesApp.slideSetModel.set('themeId', theme.get('id'));
                $.when.apply($, _.map(slidesApp.slideCollection.models, function(slideModel) {
                    return applyThemeForSlide(slideModel);
                })).then(function(){
                    slidesApp.historyManager.groupClose();
                });
            }

            function applyThemeForSlide(slideModel) {
                var deferred = $.Deferred();
                slideModel.set({
                    bgColor: theme.get('bgColor') || '',
                    font: theme.get('font') || ''
                });

                slideModel.changeElementsFont();

                if (theme.get('bgImage') && !skipBackgroundChange) {
                    var src = slidesApp.getFileUrl(theme, theme.getBackgroundImageName());
                    var imageName = theme.getBackgroundImageName();
                    var imageSize = theme.getBackgroundSize();
                    imgSrcToBlob(src).then(function(blob){
                        var formData = new FormData();
                        formData.append('p_auth', Liferay.authToken);
                        formData.append('files[]', blob, imageName);
                        formData.itemModel = new FileUploaderItemModel({
                            filename: imageName
                        });
                        slideModel
                            .set('formData', formData)
                            .set('bgImage', createObjectURL(blob)+ ' ' + imageSize)
                            .set('bgImageChange', true, {silent: true})
                            .unset('fileModel');
                        deferred.resolve()
                    });
                }
                else {
                    if(!!slideModel.get('bgImage')){
                        slideModel.set('bgImageChange', true);
                        slideModel.unset('formData');
                        slideModel.unset('fileModel');
                    }
                    slideModel.set({bgImage: ''});
                    deferred.resolve();
                }
                return deferred.promise();
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
                if(slidesApp.gridSnapModule){
                    slidesApp.gridSnapModule.generateGrid();
                }
                if(slidesApp.activeElement && slidesApp.activeElement.view){
                    slidesApp.activeElement.view.updateControlsPosition();
                }
                window.placeSlideControls();
                slidesApp.RevealModule.configure({ height: layoutHeight });
            }
        },
        placeWorkArea: function(){
            if(this.ui && this.ui.work_area){
                var windowHeight = $(window).height(),
                    clientRect = this.ui.work_area.get(0).getBoundingClientRect(),
                    isPositionOnTop = windowHeight < clientRect.height + lessonStudio.fixedSizes.TOPBAR_HEIGHT;
                this.ui.work_area.toggleClass( 'top-position', isPositionOnTop );
            }
        }
    });

    RevealModule.updateBackgroundImage = function($slide, slideModel, image){
        if (image && image.indexOf('$') > -1) image.replace('$', ' ');
        var imageParts = image ? image.split(' ') : [];
        if (imageParts.length > 2) {
            imageParts[0] = _.initial(imageParts).join('+');
            imageParts[1] = imageParts.pop();
        }

        var src = slidesApp.getFileUrl(slideModel, imageParts[0]);

        $slide.attr('data-background-repeat', 'no-repeat');
        $slide.attr('data-background-position', 'center');
        $slide.attr('data-background', src);
        $slide.attr('data-background-image', imageParts[0] || '');
        $slide.attr('data-background-size', imageParts[1] || '');
    };

    RevealModule.slideBindActions = function(slideModel){
        slideModel
            .on('remove', function(model, value){
                RevealModule.view.deletePageElement(this.getId());
                App.vent.trigger('elementsUpdated');
                if (slidesApp.mode == 'arrange'){
                    arrangeModule.onSlideRemove(model, value);
                }
            })
            .on('change:bgColor', function(model, value){
                slidesApp.execute('reveal:page:changeBackground', value, model);
            })
            .on('change:bgImage', function(model, value){
                slidesApp.execute('reveal:page:changeBackgroundImage', value, model);
            })
            .on('change:font', function(model, value){
                slidesApp.execute('reveal:page:changeFont', value, model);
            })
            .on('change:toBeRemoved', function(model, value){
                if(!value){
                    RevealModule.view.addPage(model);
                } else {
                    slidesApp.activeSlideModel = model;
                    slidesApp.execute('reveal:page:delete', model);
                }
            });
    };

    RevealModule.onStart = function(options) {
        options = options || {};
        this.slideSetModel = options.slideSetModel || slidesApp.slideSetModel;
        if( $(lessonStudio.slidesWrapper + ' #arrangeContainer').length == 0 ) {
            $(lessonStudio.slidesWrapper + ' #revealEditor').append('<div id="arrangeContainer"></div>');
        }
        if( $(lessonStudio.slidesWrapper + ' #versionContainer').length == 0 ) {
            $(lessonStudio.slidesWrapper + ' #revealEditor').append('<div id="versionContainer"></div>');
        }
        slidesApp.editorArea.$el.closest('.slides-editor-main-wrapper').show();
        revealModule.view = new RevealModule.View({ slideSetModel: options.slideSetModel });
        var that = this;

        slidesApp.slideCollection.each(function(model){
            RevealModule.slideBindActions(model);
        });
        slidesApp.vent.on('slideAdd', this.slideBindActions, this);

        slidesApp.commands.setHandler('reveal:page:add', function(direction, slideModel) {
            if(!slideModel){
                slideModel = new lessonStudio.Entities.LessonPageModel({
                        tempId: slidesApp.newSlideId--,
                        slideSetId: that.slideSetModel.id
                    //TODO silent????
                    }, {silent: true});
            }
            if( direction ){
                if(direction == 'right'){
                    slideModel.set('leftSlideId', slidesApp.activeSlideModel.getId());
                    slideModel.unset('topSlideId');
                } else {
                    slideModel.set('topSlideId', slidesApp.activeSlideModel.getId());
                    slideModel.unset('leftSlideId');
                }
            }
            slidesApp.slideCollection.add(slideModel);
        });
        slidesApp.commands.setHandler('reveal:page:delete', function(slideModel){
            revealModule.view.deletePage(slideModel);
        });
        slidesApp.commands.setHandler('reveal:page:changeBackground', function(color,slideModel) {
            revealModule.view.changeBackground(color, slideModel);
        });
        slidesApp.commands.setHandler('reveal:page:changeBackgroundImage', function(image, slideModel, src) {
            revealModule.view.changeBackgroundImage(image, slideModel, src);
        });
        slidesApp.commands.setHandler('reveal:page:changeFont', function (font, slideModel) {
            revealModule.view.changeFont(font, slideModel);
        });
        slidesApp.commands.setHandler('reveal:page:applyTheme', function(slideModel, skipBackgroundChange) {
            skipBackgroundChange = skipBackgroundChange || false;
            revealModule.view.applyTheme(slideModel, skipBackgroundChange)
        });
        slidesApp.commands.setHandler('reveal:page:updateRefs', function(currentPage, actionType) {
            if (slidesApp.mode == 'edit') {
                revealModule.view.updateSlideRefs(currentPage, actionType);
            }
        });
        slidesApp.commands.setHandler('reveal:page:makeActive', function() {
            if(!slidesApp.initializing) {
                var currentSlideId = parseInt($(Reveal.getCurrentSlide()).attr('id').replace('slide_', ''));
                slidesApp.activeSlideModel = _.first(slidesApp.slideCollection.filter(function(model){
                    return model.getId() == currentSlideId;
                }));
            }
        });

        initializeKeepAlive();

        function initializeKeepAlive() {
            setInterval(sessionKeepAlive, 300000);  //every 5 minutes
        }

        function sessionKeepAlive(){
            if(typeof(Liferay.Session) != "undefined") {
                clearTimeout(Liferay.Session._stateCheck);
                Liferay.Session.extend();
            }
        }

        App.vent
            .on('elementsUpdated containerScroll containerResize', this.selectableRefresh, this)
            .on('editorModeChanged', this.view.onEditorModeChanged, this.view)
            .on('slideAdd', this.view.onSlideAdd, this.view);

        return revealModule.renderSlideset(options);
    };

    RevealModule.fitContentScrollInit = function(){
        var slidesWrapper = $(lessonStudio.slidesWrapper + ' .reveal-wrapper:first');
        slidesWrapper
            .addClass('scroll-y')
            .bind('scroll',function () {
                var scrollTop = $(this).scrollTop();
                $('.backgrounds', slidesWrapper).css('top', scrollTop + 'px');
            });
    };

    RevealModule.renderSlideset = function (options) {
        options = options || {};
        RevealModule.slideSetModel = options.slideSetModel || slidesApp.slideSetModel;
        RevealModule.slideCollection = options.slideCollection || slidesApp.slideCollection;
        RevealModule.slideElementCollection = options.slideElementCollection || slidesApp.slideElementCollection;

        slidesApp.initializing = true;

        slidesApp.editorArea.show(revealModule.view);

        var $slides = revealModule.view.$('.slides');
        $slides.html('');

        slidesApp.addedSlides = [];
        slidesApp.addedSlideIndices = [];
        slidesApp.maxZIndex = 0;

        var rootSlide = RevealModule.slideCollection.findWhere({
            leftSlideId: undefined,
            topSlideId: undefined,
            toBeRemoved: false
        });

        RevealModule.createSlides($slides, rootSlide.getId());

        RevealModule.view.restartReveal();

        slidesApp.module('RevealControlsModule').start();

        slidesApp.initDnD();

        Reveal.slide(0, 0);
        Reveal.sync();

        slidesApp.activeSlideModel = rootSlide;

        slidesApp.checkIsTemplate();

        //TODO the same in slidechanged
        var iconQuestionDiv = $('.sidebar').find('span.val-icon-question').closest('div');
        if (rootSlide.hasQuestions()) {
            iconQuestionDiv.hide();
        } else {
            iconQuestionDiv.show();
        }

        //TODO the same in slidechanged
        if ($('.slides > section > section').length > 1) {
            $('.js-slide-delete').show();
        } else {
            $('.js-slide-delete').hide();
        }

        slidesApp.execute('controls:place');

        if (!slidesApp.isRunning){
            lessonStudio.execute('editor-ready', this.slideSetModel);
        } else {
            slidesApp.execute('editor-reloaded');
        }

        slidesApp.isRunning = true;
        $(lessonStudio.slidesWrapper + ' #js-slide-title').attr('placeholder', Valamis.language['pageDefaultTitleLabel']);
        $(lessonStudio.slidesWrapper + ' #js-slide-statement-object').attr('placeholder', Valamis.language['pageDefaultTitleLabel']);

        this.slideSetModel.off('change:themeId').on('change:themeId', function (model) {
            var themeId = model.get('themeId');
            if (themeId) {
                slidesApp.themeModel.id = themeId;
                slidesApp.themeModel.fetch();
            }
        });

        slidesApp.initializing = false;
    };

    RevealModule.createSlides = function ($slides, parentId) {

        //need to save slide coordinates for slide links and
        //saving position between editor mode switching
        var registerSlide = function (slideId, slideIndices) {
            slidesApp.addedSlides.push(slideId);
            slidesApp.addedSlideIndices[slideId] = slideIndices;
            slidesApp.slideRegistry.register(slideId, slideIndices);
        };

        var shouldBeAdded = function (slide) {
            return (!!slide
                && slidesApp.addedSlides.indexOf(slide.getId()) === -1
                && !slide.get('toBeRemoved') // added to slide search (bottom and right)
            );
        };

        var addBottomSlides = function (slide, $slide, parentIndex) {

            var bottomSlide = RevealModule.slideCollection.find(function (item) {
                return (item.get('topSlideId') === slide.getId()
                && !item.get('toBeRemoved'));
            });

            if (shouldBeAdded(bottomSlide)) {

                var $bottomSlide = RevealModule.createRevealSlide(bottomSlide);

                $slide.after($bottomSlide);

                var slideIndex = {h: parentIndex.h, v: (parentIndex.v + 1)};

                registerSlide(bottomSlide.getId(), slideIndex);

                addBottomSlides(bottomSlide, $bottomSlide, slideIndex);
            }
        };

        var addRightSlide = function (leftSlide, $leftSlide, parentIndex) {

            var rightSlide = RevealModule.slideCollection.find(function (item) {
                return (item.get('leftSlideId') === leftSlide.getId()
                && !item.get('toBeRemoved'));
            });

            if (shouldBeAdded(rightSlide)) {

                var $rightSlide = RevealModule.createRevealSlide(rightSlide);

                var $wrapper = $('<section></section>');

                $wrapper.append($rightSlide);

                $leftSlide.parent().parent().append($wrapper);

                var slideIndex = {h: (parentIndex.h + 1), v: parentIndex.v};

                registerSlide(rightSlide.getId(), slideIndex);

                addBottomSlides(rightSlide, $rightSlide, slideIndex);

                addRightSlide(rightSlide, $rightSlide, slideIndex);
            }
        };

        var slide = RevealModule.slideCollection.find(function (item) {
            return item.getId() === parentId;
        });

        if (shouldBeAdded(slide)) {

            var $slide = RevealModule.createRevealSlide(slide);

            var $wrapper = $('<section></section>');

            $wrapper.append($slide);

            var slideIndex = {h: 0, v: 0};

            registerSlide(slide.getId(), slideIndex);

            $slides.append($wrapper);

            addBottomSlides(slide, $slide, slideIndex);

            addRightSlide(slide, $slide, slideIndex);
        }
    };

    RevealModule.createRevealSlide = function (slideModel) {

        var $slide = $('<section></section>');

        $slide.attr('id', 'slide_' + (slideModel.getId()));
        $slide.attr('title', slideModel.get('title') || '');
        if (slideModel.get('bgColor')) {
            $slide.attr('data-background-color', unescape(slideModel.get('bgColor')));
        }
        var image = slideModel.get('bgImage');
        if (image) {
            RevealModule.updateBackgroundImage($slide, slideModel, image);
        }
        if (slideModel.get('font')) {
            //Style text
            var fontParts = slideModel.get('font').split('$');
            $slide.css({
                'font-family': fontParts[0],
                'font-size': fontParts[1],
                'color': fontParts[2]
            })
        }

        var slideElements = slideModel.get('slideElements');

        //TODO change way to convert slideElements to collection
        if (!slideElements) {
            slideElements = RevealModule.slideElementCollection.where({slideId: slideModel.getId()});
        }
        if (!(slideElements instanceof Backbone.Collection)) {
            slideModel.set('slideElements', new lessonStudioCollections.LessonPageElementCollection(slideElements));
        }

        RevealModule.addElementsToSlide(slideModel.get('slideElements'), $slide);

        return $slide;
    };

    RevealModule.addElementsToSlide = function (slideElements, $currentSlide) {
        //TODO use it only for render elements,
        // should it check is it new element or not?
        //actually for a now it is not new always - can remove it

        slideElements.forEach(function (element) {
            if (element.get('toBeRemoved')) return;
            var slideEntityType = element.get('slideEntityType');

            var moduleName = slidesApp.getModuleName(slideEntityType);
            var module = slidesApp.module(moduleName);

            var view = new module.View({model: element});
            var $elem = view.render().$el;

            //copied from slidesApp item:create handler

            if (_.contains(['video', 'iframe'], slideEntityType)) {
                element.set('content', decodeURIComponent(element.get('content')));
            }

            if (!slidesApp.getSlideElementModel(element.getId())) {
                slidesApp.slideElementCollection.add(element);
            }

            if (element.get('content') !== '') {
                view.$('div[class*="content-icon-"]').hide();
                view.content.css('background-color', 'transparent');
            }

            switch (moduleName) {
                case slidesApp.IframeElementModule.moduleName:
                case slidesApp.PdfElementModule.moduleName:
                    if (element.get('content') !== '')
                        view.$('.iframe-item').show();
                    break;
                case slidesApp.ImageElementModule.moduleName:
                    view.updateUrl(
                        element.get('content'),
                        element._previousAttributes.content,
                        element.get('width'),
                        element.get('height'),
                        true);
                    break;
                case slidesApp.ContentElementModule.moduleName:
                    var iconQuestionDiv = $('.sidebar').find('span.val-icon-question').closest('div');

                    element.on('change:toBeRemoved', function () {
                        if (!element.get('toBeRemoved')) {
                            iconQuestionDiv.hide();
                        }
                        else {
                            iconQuestionDiv.show();
                        }
                    });
                    element.on('destroy', function () {
                        //this is needed for iconQuestion to show in case
                        //when we create question and then undo the creation
                        iconQuestionDiv.show();
                    });

                    if (element.get('content') !== '') {
                        view.updateQuestion(element);
                    }
                    break;
                case slidesApp.VideoElementModule.moduleName:
                    if (element.get('content') !== '') {
                        view.$('.video-js').show();
                        view.updateUrl(element.get('content'));
                        slidesApp.actionStack.pop();
                        slidesApp.toggleSavedState();
                    }
                    break;
                case slidesApp.MathElementModule.moduleName:
                    view.$('.ui-resizable-handle').toggleClass('hidden', true);
                    break;
                case slidesApp.WebglElementModule.moduleName:
                    view.updateUrl(element.get('content'));
                    break;
                case slidesApp.AudioElementModule.moduleName:
                    view.updateUrl(element.get('content'));
                    break;
            }

            //TODO - move into view later
            $elem.attr('id', 'slideEntity_' + element.getId());
            $currentSlide.append($elem);

            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
            element.applyLayoutProperties(deviceLayoutCurrent.get('id'));
        });
    };

    RevealModule.selectableInit = function() {
        var $currentSlide = $(Reveal.getCurrentSlide());
        if(!$currentSlide.data('ui-selectable')){
            $currentSlide
                .css({height: '100%'})
                .selectable({
                    appendTo: 'body',
                    filter: '.rj-element',
                    autoRefresh: false,
                    stop: function() {
                        var selectedIds = [];
                        $('.ui-selected', this).each(function() {
                            selectedIds.push(parseInt(this.id.replace('slideEntity_',''), 10));
                        });
                        if(!selectedIds.length) {
                            return;
                        }
                        if( selectedIds.length == 1 ){
                            App.activeSlideModel.updateAllElements({selected: false});
                            var slideElement = _.first(slidesApp.slideElementCollection.filter(function(model){
                                return model.getId() == selectedIds[0];
                            }));
                            if(slideElement){
                                var view = Marionette.ItemView.Registry.getByModelId(slideElement.getId());
                                if(view){
                                    slidesApp.execute('item:focus', view);
                                }
                            }
                        } else {
                            var slideElements = App.activeSlideModel.getElements();
                            slideElements.forEach(function(slideElement){
                                slideElement.set('selected', _.contains(selectedIds, slideElement.getId()));
                            });
                        }
                    }
                })
                .parent('section')
                .css({height: '100%'});
        } else {
            $currentSlide.selectable('refresh');
        }
    };

    RevealModule.selectableRefresh = function(){
        var $currentSlide = $(Reveal.getCurrentSlide());
        if( slidesApp.activeElement && slidesApp.activeElement.view){
            slidesApp.activeElement.view.updateControlsPosition();
        }
        else if (this.isOpenSettingPanel()) {
            slidesApp.execute('item:blur');
        }
        if($currentSlide.data('ui-selectable')){
            $currentSlide.selectable('refresh');
        }
    };

    RevealModule.isOpenSettingPanel = function() {
        var isVisibleSetting = $(lessonStudio.slidesWrapper + ' .slide-popup-panel.js-valamis-popup-panel').is(':visible');
        var isVisibleColor = $(lessonStudio.slidesWrapper + ' .colpick').is(':visible');

        return  isVisibleSetting || isVisibleColor;
    };

    RevealModule.selectableDestroy = function(){
        var $currentSlide = $(Reveal.getCurrentSlide());
        if($currentSlide.data('ui-selectable')){
            $currentSlide.selectable('destroy');
        }
    };

    RevealModule.configure = function( options ){
        if(Reveal.isReady()){
            Reveal.configure( options );
        }
    };

    RevealModule.onStop = function() {

        App.vent
            .off('elementsUpdated containerScroll containerResize', this.selectableRefresh)
            .off('editorModeChanged', this.view.onEditorModeChanged)
            .off('slideAdd', this.view.onSlideAdd);

        slidesApp.vent.off('slideAdd', this.slideBindActions);

        App.isRunning = false;
        this.view.destroyReveal();
        this.view = null;
    };

});
