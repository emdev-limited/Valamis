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

                    if(slidesApp.mode != 'versions')
                        slidesApp.checkIsTemplate();

                    revealControlsModule.view.model.clear({silent: true});
                    revealControlsModule.view.model.set(slidesApp.activeSlideModel.toJSON());
                }

                var iconQuestionDiv = jQueryValamis('.sidebar').find('span.val-icon-question').closest('div');
                if (slidesApp.activeSlideModel.hasQuestions()) {
                    iconQuestionDiv.hide();
                }
                else {
                    iconQuestionDiv.show();
                }

                if( !slidesApp.initializing ){
                    self.updateSlideHeight();
                    if(revealModule.view){
                        revealModule.view.placeWorkArea();
                    }
                }
                if(App.mode == 'edit'){
                    RevealModule.selectableInit();
                } else {
                    RevealModule.selectableDestroy();
                }
            }.bind(this) );

            Reveal.addEventListener( 'ready', function( event ) {
                revealControlsModule.view.model.clear({silent: true});
                revealControlsModule.view.model.set(slidesApp.activeSlideModel.toJSON());
                revealControlsModule.view.updateTopDownNavigation(!!(slidesApp.slideSetModel.get('topDownNavigation')));
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
        restartReveal: function(){
            if(Reveal.isReady()){
                Reveal.removeEventListeners();
                this.ui.slides
                    .empty()
                    .append( $('<section/>').append('<section/>') );
            }
            RevealModule.view.initReveal();
        },
        addPage: function(slideModel) {
            var isRoot = _.isUndefined(slideModel.get('leftSlideId')) && _.isUndefined(slideModel.get('topSlideId')),
                leftSlideId = slideModel.get('leftSlideId'),
                topSlideId = slideModel.get('topSlideId');

            slidesApp.execute('item:blur');
            slidesApp.slideAdd = true;
            slidesApp.activeSlideModel = slideModel;
            var currentPage;

            //Insert slide element
            var previousSlideModel = _.first(slidesApp.slideCollection.filter(function(model){
                    return topSlideId
                        ? model.getId() == topSlideId
                        : model.getId() == leftSlideId;
                }));
            var nextSlideModel = slidesApp.slideCollection.findWhere({topSlideId: slideModel.getId(), toBeRemoved: false});
            if(!nextSlideModel){
                nextSlideModel = slidesApp.slideCollection.findWhere({leftSlideId: slideModel.getId(), toBeRemoved: false});
            }

            if( nextSlideModel && jQueryValamis( '#slide_' + nextSlideModel.getId()).size() > 0 ){
                currentPage = jQueryValamis( '#slide_' + nextSlideModel.getId());
                //insert top
                if( nextSlideModel.get('topSlideId') == slideModel.getId() ){
                    jQueryValamis('<section/>', { id: 'slide_' + slideModel.getId() }).insertBefore(currentPage);
                } else {
                    //insert left
                    jQueryValamis('<section/>').append(
                            jQueryValamis('<section/>', { id: 'slide_' + slideModel.getId() })
                        )
                        .insertBefore(currentPage.parent());
                }
            } else {
                if( previousSlideModel && jQueryValamis( '#slide_' + previousSlideModel.getId()).size() > 0 ){
                    currentPage = jQueryValamis( '#slide_' + previousSlideModel.getId());
                } else {
                    currentPage = jQueryValamis(Reveal.getCurrentSlide());
                }
                if( isRoot || leftSlideId ) {
                    //insert right
                    jQueryValamis('<section/>').append(
                            jQueryValamis('<section/>', { id: 'slide_' + slideModel.getId() })
                        )
                        .insertAfter(currentPage.parent());
                }
                else {
                    //insert down
                    jQueryValamis('<section/>', { id: 'slide_' + slideModel.getId() }).insertAfter(currentPage);
                }
            }

            slidesApp.slideAdd = false;
            currentPage = jQueryValamis('#slide_' + slideModel.getId());
            Reveal.slide.apply(Reveal, _.values(Reveal.getIndices(currentPage.get(0))));

            slidesApp.execute('reveal:page:changeBackground', slideModel.get('bgColor'), slideModel);
            slidesApp.execute('reveal:page:changeBackgroundImage', slideModel.get('bgImage'), slideModel);

            if(!slideModel.get('title'))
                slideModel.set('title', '');

            if(slideModel.get('font') && slidesApp.initializing) {
                slidesApp.execute('reveal:page:changeFont', slideModel.get('font'));
            }

            if(!slidesApp.initializing) {
                slidesApp.execute('reveal:page:updateRefs', currentPage, 'add');
            }
            var slideIndices = _.pick( Reveal.getIndices(Reveal.getCurrentSlide()), ['h','v'] );
            if(slidesApp.initializing)
                slideIndices.h--;
            slidesApp.slideRegistry.register(slideModel.getId(), slideIndices);

            slidesApp.execute('reveal:page:makeActive');
            if ( slidesApp.slideSetModel.get('themeId') && !slidesApp.initializing ){
                _.defer(function(){
                    slidesApp.execute('reveal:page:applyTheme', slideModel);
                });
            }
        },
        onSlideAdd: function(model){
            if(!slidesApp.initializing) {
                this.addPage(model);
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
            jQueryValamis('.sidebar').find('.question-element').first().remove();
            var currentSlideId = slideModel.getId();
            var currentPage = jQueryValamis('#slide_' + currentSlideId);

            if(!slidesApp.initializing) {
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
                jQueryValamis('.js-slide-delete').hide();
            }
        },
        deletePageElement: function(slideId){
            if(Reveal.getTotalSlides() == 1){
                return;
            }
            var $slideElement = jQueryValamis('#slide_' + slideId);
            if( $slideElement.size() > 0 ){
                if($slideElement.parent('section').size() > 0
                    && $slideElement.parent('section').children('section').size() == 1){
                        $slideElement.parent('section').remove();
                } else {
                    $slideElement.remove();
                }
            }
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
                        var newLeftId = (nextPageDown.length > 0)
                            ? parseInt(nextPageDown.attr('id').replace('slide_', ''))
                            : (prevPageLeft.length > 0)
                                ? parseInt(prevPageLeft.first().attr('id').replace('slide_', ''))
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
                    if (prevPageLeft.length > 0  && nextPageDown.length > 0 && prevPageUp.length == 0 ){
                        var idToAddLeft = parseInt(nextPageDown.attr('id').replace('slide_', ''));
                        var newLeftId = parseInt(prevPageLeft.attr('id').replace('slide_', ''));
                        slidesApp.getSlideModel(idToAddLeft).set('leftSlideId', newLeftId);
                    }
                    break;
            }
        },
        changeBackground: function(color, slideModel) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));

            slideModel.set('bgColor', color);
            slide.attr('data-background-color', color);
            Reveal.sync();
            if (slidesApp.mode == 'arrange'){
                arrangeModule.changeBackgroundColor(slideModel.getId());
            }
        },
        changeBackgroundImage: function(image, slideModel) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;

            if (image && image.indexOf('$') > -1) image.replace('$', ' ');
            var imageParts = image ? image.split(' ') : [];
            if (imageParts.length > 2){
                imageParts[0] = _.initial(imageParts).join('+');
                imageParts[1] = imageParts.pop();
            }
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));
            var src = slidesApp.getFileUrl(slideModel, imageParts[0]);

            slide.attr('data-background-repeat', 'no-repeat');
            slide.attr('data-background-position', 'center');
            slide.attr('data-background', src);
            slide.attr('data-background-image', imageParts[0] || '');
            slide.attr('data-background-size', imageParts[1] || '');
            Reveal.sync();
            if (slidesApp.mode == 'arrange'){
                arrangeModule.changeBackgroundImage(slideModel.getId());
            }
        },
        changeFont: function (font, slideModel) {
            if(!slideModel) slideModel = slidesApp.activeSlideModel;
            var slide = jQueryValamis('#slide_' + slideModel.get('id')).length > 0
                ? jQueryValamis('#slide_' + slideModel.get('id'))
                : jQueryValamis('#slide_' + slideModel.get('tempId'));

            var fontData = slideModel.getFont(font);

            slide.attr('data-font', font);

            //Apply font to slide
            slide.css({
                'font-family': fontData.fontFamily,
                'font-size': fontData.fontSize,
                'color': fontData.fontColor
            });
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
                        if(jQueryValamis(el).css('color')){
                            jQueryValamis(el).css('color', fontData.fontColor);
                        }
                        if(jQueryValamis(el).css('font-family')){
                            jQueryValamis(el).css('font-family', fontData.fontFamily);
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
                jQueryValamis.when(applyThemeForSlide(slideModel)).then(function() {
                    slidesApp.historyManager.groupClose();
                });
            }
            else {
                slidesApp.slideSetModel.set('themeId', theme.get('id'));
                jQueryValamis.when.apply(jQueryValamis, _.map(slidesApp.slideCollection.models, function(slideModel) {
                    return applyThemeForSlide(slideModel);
                })).then(function(){
                    slidesApp.historyManager.groupClose();
                });
            }

            function applyThemeForSlide(slideModel) {
                var deferred = jQueryValamis.Deferred();
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
                var windowHeight = jQueryValamis(window).height(),
                    clientRect = this.ui.work_area.get(0).getBoundingClientRect(),
                    isPositionOnTop = windowHeight < clientRect.height + lessonStudio.fixedSizes.TOPBAR_HEIGHT;
                this.ui.work_area.toggleClass( 'top-position', isPositionOnTop );
            }
        }
    });

    RevealModule.slideBindActions = function(slideModel){
        slideModel
            .on('remove', function(){
                RevealModule.view.deletePageElement(this.getId());
                App.vent.trigger('elementsUpdated');
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
        if( jQueryValamis('#arrangeContainer').size() == 0 ) {
            jQueryValamis('#revealEditor').append('<div id="arrangeContainer"></div>');
        }
        if( jQueryValamis('#versionContainer').size() == 0 ) {
            jQueryValamis('#revealEditor').append('<div id="versionContainer"></div>');
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
                    }, {silent: true});
            }
            if( direction ){
                if(direction == 'right'){
                    slideModel.set('leftSlideId', slidesApp.activeSlideModel.getId());
                } else {
                    slideModel.set('topSlideId', slidesApp.activeSlideModel.getId());
                }
            }
            slidesApp.slideCollection.add(slideModel);
        });
        slidesApp.commands.setHandler('reveal:page:delete', function(slideModel){revealModule.view.deletePage(slideModel);});
        slidesApp.commands.setHandler('reveal:page:changeBackground', function(color,slideModel) {revealModule.view.changeBackground(color, slideModel);});
        slidesApp.commands.setHandler('reveal:page:changeBackgroundImage', function(image, slideModel, src) {revealModule.view.changeBackgroundImage(image, slideModel, src);});
        slidesApp.commands.setHandler('reveal:page:changeFont', function (font, slideModel) {revealModule.view.changeFont(font, slideModel);});
        slidesApp.commands.setHandler('reveal:page:applyTheme', function(slideModel, skipBackgroundChange) {
            skipBackgroundChange = skipBackgroundChange || false;
            revealModule.view.applyTheme(slideModel, skipBackgroundChange)
        });
        slidesApp.commands.setHandler('reveal:page:updateRefs', function(currentPage, actionType) {revealModule.view.updateSlideRefs(currentPage, actionType)});
        slidesApp.commands.setHandler('reveal:page:makeActive', function() {
            if(!slidesApp.initializing) {
                var currentSlideId = parseInt(jQueryValamis(Reveal.getCurrentSlide()).attr('id').replace('slide_', ''));
                slidesApp.activeSlideModel = _.first(slidesApp.slideCollection.filter(function(model){
                    return model.getId() == currentSlideId;
                }));
            }
        });

        App.vent
            .on('elementsUpdated containerScroll containerResize', this.selectableRefresh, this)
            .on('editorModeChanged', this.view.onEditorModeChanged, this.view)
            .on('slideAdd', this.view.onSlideAdd, this.view);

        return revealModule.renderSlideset(options);
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

    RevealModule.renderSlideset = function(options) {
        options = options || {};
        RevealModule.slideSetModel = options.slideSetModel || slidesApp.slideSetModel;
        RevealModule.slideCollection = options.slideCollection || slidesApp.slideCollection;
        RevealModule.slideElementCollection = options.slideElementCollection || slidesApp.slideElementCollection;
        var deferred = jQueryValamis.Deferred();
        slidesApp.editorArea.show(revealModule.view);

        slidesApp.addedSlides = [];
        slidesApp.addedSlideIndices = [];
        slidesApp.maxZIndex = 0;
        Marionette.ItemView.Registry.items = {};
        slidesApp.initializing = true;

        RevealModule.view.restartReveal();

        var rootSlide = RevealModule.slideCollection.findWhere({ leftSlideId: undefined, topSlideId: undefined, toBeRemoved: false });
        if (slidesApp.addedSlides.indexOf(rootSlide.get('tempId')) != -1)
            delete slidesApp.addedSlides[rootSlide.get('tempId')];

        if (slidesApp.addedSlides.indexOf(rootSlide.id) == -1) {
            RevealModule.view.addPage(rootSlide);
            slidesApp.addedSlideIndices[rootSlide.getId()] = Reveal.getIndices();

            if (!slidesApp.isRunning)
                var slideElements = rootSlide.getSlideElements();
            else
                var slideElements = RevealModule.slideElementCollection.where({slideId: (rootSlide.getId())});

            if (slideElements.length > 0)
                RevealModule.forEachSlideElement(slideElements);
            slidesApp.addedSlides.push(rootSlide.getId());
            RevealModule.forEachSlide(rootSlide.getId());
        }

        jQueryValamis('.slides > section:first').remove();
        jQueryValamis('.backgrounds > .slide-background').remove();
        Reveal.slide(0, 0);
        Reveal.sync();

        slidesApp.maxZIndex = _.max(_.map(jQueryValamis(Reveal.getCurrentSlide()).find('div[id^="slideEntity_"]'),
            function (item) { return $(item).find('.item-content').css('z-index'); }
        ));
        slidesApp.maxZIndex = _.isFinite(slidesApp.maxZIndex) ? slidesApp.maxZIndex : 0;

        slidesApp.activeSlideModel = this.slideCollection.get(parseInt(jQueryValamis(Reveal.getCurrentSlide()).attr('id').replace('slide_', '')));
        slidesApp.checkIsTemplate();

        if (slidesApp.activeSlideModel.hasQuestions()) {
            jQueryValamis('.sidebar').find('span.val-icon-question').closest('div').hide();
        }

        slidesApp.module('RevealControlsModule').start();
        slidesApp.initDnD();
        if (this.slideCollection.models.length > 1)
           jQueryValamis('.js-slide-delete').show();
        else
           jQueryValamis('.js-slide-delete').hide();

        slidesApp.execute('controls:place');
        slidesApp.execute('item:blur');
        if(!slidesApp.isRunning)
            lessonStudio.execute('editor-ready', this.slideSetModel);
        else
            slidesApp.execute('editor-reloaded');
        slidesApp.isRunning = true;
        jQueryValamis('#js-slide-title').attr('placeholder', Valamis.language['pageDefaultTitleLabel']);
        jQueryValamis('#js-slide-statement-object').attr('placeholder', Valamis.language['pageDefaultTitleLabel']);

        slidesApp.slideSetModel.off('change:themeId').on('change:themeId', function (model) {
            var themeId = model.get('themeId');
            if (themeId) {
                slidesApp.themeModel.id = themeId;
                slidesApp.themeModel.fetch();
            }
        });

        _.defer(function(){
            slidesApp.initializing = false;
        });

        deferred.resolve();

        return deferred.promise();
    };

    RevealModule.forEachSlide = function(id) {
        var that = this;
        that.slideCollection.each(function(slide) {
            if(!slide.get('toBeRemoved')) {
                if (slide.get('leftSlideId') == id || slide.get('topSlideId') == id) {
                    var slideId = slide.getId();
                    if (slideId != id) {
                        Reveal.slide(slidesApp.addedSlideIndices[id].h, slidesApp.addedSlideIndices[id].v);
                        RevealModule.view.addPage(slide);
                        slidesApp.addedSlideIndices[slideId] = Reveal.getIndices();
                        if(!slidesApp.isRunning)
                            var slideElements = slide.getSlideElements();
                        else
                            var slideElements = RevealModule.slideElementCollection.where({slideId: slideId});
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
        _.each(slideElements, function(slideElementModel){
            if(!slideElementModel.get('toBeRemoved')) {
                slidesApp.execute('prepare:new', slideElementModel);
                slidesApp.execute('item:create', slideElementModel);
                slidesApp.activeElement.isMoving = false;
                slideElementModel.trigger('change:classHidden');
            }
        });
    };

    RevealModule.selectableInit = function() {
        var $currentSlide = jQueryValamis(Reveal.getCurrentSlide());
        if(!$currentSlide.data('uiSelectable')){
            $currentSlide
                .css({height: '100%'})
                .selectable({
                    appendTo: 'body',
                    filter: '.rj-element',
                    autoRefresh: false,
                    stop: function() {
                        var selectedIds = [];
                        jQueryValamis('.ui-selected', this).each(function() {
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
        var $currentSlide = jQueryValamis(Reveal.getCurrentSlide());
        if( slidesApp.activeElement && slidesApp.activeElement.view){
            slidesApp.activeElement.view.updateControlsPosition();
        }
        else if (this.isOpenSettingPanel()) {
            slidesApp.execute('item:blur');
        }
        if($currentSlide.data('uiSelectable')){
            $currentSlide.selectable('refresh');
        }
    };

    RevealModule.isOpenSettingPanel = function() {
        var isVisibleSetting = jQueryValamis('.slide-popup-panel.js-valamis-popup-panel').is(':visible');
        var isVisibleColor = jQueryValamis('.colpick').is(':visible');

        return  isVisibleSetting || isVisibleColor;
    };

    RevealModule.selectableDestroy = function(){
        var $currentSlide = jQueryValamis(Reveal.getCurrentSlide());
        if($currentSlide.data('uiSelectable')){
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
