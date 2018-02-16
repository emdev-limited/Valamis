slidesApp.togglePreviewMode = function(newMode) {
    var isEditView = newMode == 'edit';
    var isPreview = (newMode == 'preview' || newMode == 'versions');
    if( newMode == 'edit' && !slidesApp.isEditorReady ){
        _.each(Marionette.ItemView.Registry.items, function(item) {
            item.$el.removeClass('inactive');
            item.goToSlideActionDestroy();
            item.delegateEvents();
            item.draggableInit();
            if(item.model.get('slideEntityType') === 'webgl') {
                item.$el.unbind('mouseover mouseout');
            }
        });
        slidesApp.getRegion('editorArea').$el.removeAttr('style');
        jQueryValamis(lessonStudio.slidesWrapper + ' div[id^="slideEntity_"] .video-js').removeAttr('controls');
        slidesApp.isEditorReady = true;
    }
    else if( (newMode == 'preview' || newMode == 'versions') && slidesApp.isEditorReady ) {
        _.each(Marionette.ItemView.Registry.items, function(item) {
            item.$el.addClass('inactive');
            item.undelegateEvents();
            item.draggableDestroy();
            item.goToSlideActionInit();
            if(item.model.get('slideEntityType') === 'webgl') {
                item.$el.unbind('mouseover').bind('mouseover', function() {
                    item.trackballControls.handleResize();
                    item.trackballControls.enabled = true;
                });
                item.$el.unbind('mouseout').bind('mouseout', function() {
                    item.trackballControls.enabled = false;
                });
            }
        });
        jQueryValamis(lessonStudio.slidesWrapper + ' .reveal-wrapper').css('left', 0);
        jQueryValamis(lessonStudio.slidesWrapper + ' div[id^="slideEntity_"] .video-js')
          .attr('controls', 'controls');
        slidesApp.isEditorReady = false;
    }
    slidesApp.execute('item:blur');

    if (newMode != 'arrange') {
        slidesApp.getRegion('sidebar').$el.toggle(isEditView);
    }

    if (newMode != 'preview') {
        _.forEach(jQueryValamis(lessonStudio.slidesWrapper + ' div[id^="slideEntity_"] audio'), function(audio) {
            audio.pause();
        })
    }

    var editorTopbar = jQueryValamis(lessonStudio.slidesWrapper + ' .js-slides-editor-topbar');
    slidesApp.getRegion('revealControls').$el.toggle(isEditView);
    jQueryValamis(lessonStudio.slidesWrapper + ' div[id^="slideEntity_"] .video-js')
      .toggleClass('no-pointer-events', isEditView);
    jQueryValamis(lessonStudio.slidesWrapper + ' div[id^="slideEntity_"] iframe')
      .toggleClass('no-pointer-events', isEditView);
    jQueryValamis(lessonStudio.slidesWrapper + ' div[id^="slideEntity_"] audio')
        .toggleClass('no-pointer-events', isEditView);
    editorTopbar.find('.js-editor-save-container').toggle(!isPreview);
    editorTopbar.find('.js-undo').toggle(!isPreview);
    editorTopbar.find('.js-redo').toggle(!isPreview);
    slidesApp.topbar.currentView.ui.button_change_theme.toggleClass('hidden', isPreview);
    slidesApp.topbar.currentView.ui.button_change_settings.toggleClass('hidden', isPreview);
    slidesApp.topbar.currentView.ui.button_display_grid.toggleClass('hidden', !isEditView);

    if (newMode == 'versions') {
        editorTopbar.find('.js-close-slideset').hide();
        editorTopbar.find('.js-close-version').show();
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-mode-switcher .button-group').hide();
        editorTopbar.find('.js-version-label').show().prevAll().hide();
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-version-sidebar').show();
    }
    else {
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-version-sidebar').hide();
        editorTopbar.find('.js-close-version').hide();
        editorTopbar.find('.js-close-slideset').show();
        editorTopbar.find('.js-lesson-title').show();
        editorTopbar.find('.js-versions-history').show();
        jQueryValamis(lessonStudio.slidesWrapper + ' .js-slides-editor-topbar .js-version-label').hide();
        if(slidesApp.historyManager){
            slidesApp.toggleSavedState(!slidesApp.historyManager.isUnsavedAvailable());
        }
    }

    placeSlideControls(jQueryValamis(window.parent).width(), jQueryValamis(window.parent).height());
};

slidesApp.switchMode = function(mode, visualOnly, slideId) {
    slidesApp.execute('item:blur');
    var btn = mode == 'versions'
        ? jQueryValamis(lessonStudio.slidesWrapper + ' .js-editor-preview-mode')
        : jQueryValamis(lessonStudio.slidesWrapper + ' .js-editor-' + mode.replace(':select', '') + '-mode');
    var previousMode = slidesApp.mode;
    var btnSiblings = btn.siblings();
    btnSiblings.removeClass('slides-primary active');
    btnSiblings.addClass('slides-dark');
    btn.addClass('slides-primary active');

    btn.removeClass('slides-dark');
    jQueryValamis(lessonStudio.slidesWrapper + ' .slides-work-area-wrapper').attr('data-mode', mode);

    slidesApp.togglePreviewMode(mode);
    slidesApp.gridSnapModule.disableGrid();

    if(!visualOnly) {
        switch (mode) {
            case 'arrange':
                arrangeModule.start();
                slidesApp.topbar.currentView.$childViewContainer.hide();
                slidesApp.keyboardModule.unBindKeys();
                break;
            case 'preview':
                if (previousMode === 'arrange') {
                    valamisApp.execute('notify', 'info', Valamis.language['lessonModeSwitchingLabel'], { 'timeOut': '0', 'extendedTimeOut': '0' });
                    setTimeout(function() {
                        slidesApp.isRunning = true;
                        slidesApp.editorArea.$el.closest('.slides-editor-main-wrapper').show();
                        revealModule.renderSlideset();
                        arrangeModule.stop();
                        jQueryValamis(lessonStudio.slidesWrapper + ' #arrangeContainer').empty();
                        if (slideId) {
                            var slideIndices = slidesApp.slideRegistry.getBySlideId(slideId);
                            Reveal.slide(slideIndices.h, slideIndices.v);
                        }
                    }, 0);
                    slidesApp.topbar.currentView.$childViewContainer.show();
                }
                else {
                    revealModule.configure({ backgroundTransition: 'none', transition: 'slide' });
                }
                slidesApp.keyboardModule.unBindKeys();
                break;
            case 'versions':

                slidesApp.mode = mode;
                slidesApp.VersionModule.view.changeActive();
                var slidesWrapper = jQueryValamis(lessonStudio.slidesWrapper + ' #revealEditor .slides-work-area-wrapper');
                var newMargin = slidesWrapper.offset().left + lessonStudio.fixedSizes.VERSION_SIDEBAR_WIDTH-lessonStudio.fixedSizes.SIDEBAR_WIDTH;
                slidesWrapper.css('margin-left', newMargin);
                if (previousMode === 'arrange') {
                    slidesApp.editorArea.$el.closest('.slides-editor-main-wrapper').show();
                    arrangeModule.stop();
                    jQueryValamis(lessonStudio.slidesWrapper + ' #arrangeContainer').empty();
                }
                slidesApp.keyboardModule.unBindKeys();
                break;
            case 'edit':
                slidesApp.mode = mode;
                var entitySlideId = slidesApp.selectedItemView
                    ? slidesApp.selectedItemView.model.get('slideId')
                    : null;
                if (previousMode != 'preview'){
                    valamisApp.execute('notify', 'info', Valamis.language['lessonModeSwitchingLabel'], { 'timeOut': '0', 'extendedTimeOut': '0' });
                    setTimeout(function() {
                        slidesApp.isRunning = true;
                        slidesApp.editorArea.$el.closest('.slides-editor-main-wrapper').show();
                        revealModule.renderSlideset();

                        arrangeModule.stop();
                        var nextSlideId = slideId ? slideId : entitySlideId;
                        if ( nextSlideId && previousMode != 'versions' ) {
                            var slideIndices = slidesApp.slideRegistry.getBySlideId(nextSlideId);
                            Reveal.slide(slideIndices.h, slideIndices.v);
                            if(entitySlideId && nextSlideId == entitySlideId && slidesApp.selectedItemView){
                                var selectedEntityId = slidesApp.selectedItemView.model.getId(),
                                    currentEntity = slidesApp.getSlideElementModel(selectedEntityId),
                                    selectedView = Marionette.ItemView.Registry.getByModelId(currentEntity.getId());
                                selectedView.selectEl();
                            }
                        }
                    }, 0);
                }
                else {
                    Reveal.slide(0,0);
                }

                revealModule.configure({ backgroundTransition: 'none', transition: 'none' });
                slidesApp.topbar.currentView.$childViewContainer.show();
                slidesApp.keyboardModule.bindKeys();
                break;
        }
    }
    if(revealModule.view){
        revealModule.view.updateSlidesContainer();
    }
    setTimeout(function() {
        slidesApp.mode = mode;
        slidesApp.vent.trigger('editorModeChanged');
    }, 200);
};