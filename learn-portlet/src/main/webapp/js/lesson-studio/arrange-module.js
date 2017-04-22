var arrangeModule = slidesApp.module('ArrangeModule', function (ArrangeModule, slidesApp, Backbone, Marionette, $, _) {
    ArrangeModule.startWithParent = false;
    ArrangeModule.subAction = 'default';
    ArrangeModule.sortableEnabled = true;
    ArrangeModule.dragStarted = false;
    ArrangeModule.isChanged = false;
    ArrangeModule.slideOrder =[];


    var getSlideId = function($el){
        return parseInt($el.attr('id').slice($el.attr('id').indexOf('_') + 1));
    };

    ArrangeModule.View = Marionette.ItemView.extend({
        template: '#arrangeTemplate',
        id: 'arrangeSlides'
    });

    var arrangeView = new ArrangeModule.View();

    ArrangeModule.tileListView = Marionette.ItemView.extend({
        tagName: 'td',
        template: '#arrangeListTemplate',
        className: 'tileListTemp js-sortable-slide-list'
    });

    ArrangeModule.tileView = Marionette.ItemView.extend({
        template: '#arrangeTileTemplate',
        className: 'reveal-wrapper slides-arrange-tile js-slides-arrange-tile text-center',
        events: {
            'mouseover': 'onMouseOver',
            'mouseout': 'onMouseOut',
            'click .js-arrange-tile-cover': 'onClick',
            'click .js-arrange-tile-preview': 'goToSlide',
            'click .js-arrange-tile-edit': 'editSlide',
            'click .js-arrange-tile-delete': 'deleteSlide',
            'click .js-arrange-tile-select': 'selectSlide'
        },
        onRender: function(){
            this.$('.valamis-tooltip')
                .tooltip({
                    placement: 'right',
                    trigger: 'manual'
                })
                .bind('mouseenter', function(){
                    $(this).tooltip('show');
                    var tooltip = $(this).data('bs.tooltip').$tip;
                    tooltip
                        .css({
                            whiteSpace: 'nowrap',
                            left: '+=20'
                        });
                })
                .bind('mouseleave', function(){
                    $(this).tooltip('hide');
                })
                .parent().css('position','relative');
        },
        onMouseOver: function (e) {
            if( _.indexOf(['select','select-incorrect'], arrangeModule.subAction) > -1 ) {
                this.$('.js-arrange-tile-controls > div').addClass('hidden');
                this.$('.js-arrange-tile-controls').find('.js-arrange-tile-select').parent().removeClass('hidden');
                this.$('.js-arrange-tile-controls').show();
                $(lessonStudio.slidesWrapper + ' #arrangeContainer .js-slides-arrange-tile').removeClass('arrange-tile-active');
                this.$el.addClass('arrange-tile-active');
            }
            else{
                this.$('.js-arrange-tile-controls').show();
            }
        },
        onMouseOut: function (e) {
            this.$('.js-arrange-tile-controls').hide();
        },
        onClick: function (e) {
            if( !ArrangeModule.dragStarted ){
                if( $(e.target).is('.arrange-tile-cover') && _.indexOf(['select','select-incorrect'], arrangeModule.subAction) > -1 ){
                    this.selectSlide();
                } else {
                    this.goToSlide();
                }
            }
        },

        goToSlide: function (e) {
            if(e) e.preventDefault();
            var slideId = getSlideId(this.$el);
            slidesApp.switchMode('preview', false, slideId);
        },
        editSlide: function (e) {
            if(e) e.preventDefault();
            var slideId = getSlideId(this.$el);
            slidesApp.switchMode('edit', false, slideId);
        },
        deleteSlide: function (e) {
            if(e) e.preventDefault();
            var slides = slidesApp.slideCollection.where({toBeRemoved: false});
            if(slides.length > 1) {
                var slideId = getSlideId(this.$el);
                var slideModel = slidesApp.getSlideModel(slideId);
                slideModel.set('toBeRemoved', true);
            }
        },
        selectSlide: function(e){
            if(e) e.preventDefault();
            var slideId = getSlideId(this.$el),
                selectedEntityId = slidesApp.selectedItemView.model.id || slidesApp.selectedItemView.model.get('tempId'),
                linkTypeName = window.editorMode == 'arrange:select' ? 'correctLinkedSlideId' : 'incorrectLinkedSlideId';
            slidesApp.getSlideElementModel(selectedEntityId).set(linkTypeName, slideId);
            slidesApp.newValue = { linkType: linkTypeName, linkedSlideId: slideId };
            slidesApp.execute('action:push');
            slidesApp.switchMode('edit');
        }
    });

    ArrangeModule.slideThumbnailView = Marionette.ItemView.extend({
        template: '#slideThumbnailTemplate',
        className: 'reveal slides-thumbnail js-slides-thumbnail'
    });


    ArrangeModule.initSortable = function(elem) {
        if( !ArrangeModule.sortableEnabled ){
            return;
        }
        elem.sortable({
            placeholder: 'slides-arrange-placeholder',
            revert: true,
            delay: 50,
            sort: function(e, ui) {
                var placeholderBackground = $('<div></div>').css({
                    'width': '196px',
                    'height': '146px'
                });
                $(ui.placeholder).html('');
                $(ui.placeholder).append(placeholderBackground);
                $(ui.placeholder).addClass('slides-arrange-placeholder');
            },
            start: function(e, ui) {
                ArrangeModule.slideSourceList = $(e.currentTarget);
                ArrangeModule.dragStarted = true;
            },
            stop: function(e, ui) {
                $(ui.placeholder).html('');
                $(ui.placeholder).removeClass('slides-arrange-placeholder');
                ArrangeModule.dragStarted = false;
            },
            update: function(e, ui) {
                if(ui.sender === null) {
                    ArrangeModule.updateSlideRefs();
                    ArrangeModule.initDraggable();
                    slidesApp.slideSetModel.set('slideOrder', ArrangeModule.slideOrder);
                }
                ArrangeModule.isChanged = true;
            }
        }).disableSelection();

        var connectWithClass = (slidesApp.slideSetModel.get('topDownNavigation'))
          ? '.js-sortable-slide-list'
          : '.js-sortable-slide-list.empty-arrange-list';
        elem.sortable('option', 'connectWith', connectWithClass);
    };

    ArrangeModule.manageSortableLists = function() {
        if(ArrangeModule.slideSourceList && ArrangeModule.slideSourceList.children().length === 0) {
            if(ArrangeModule.slideSourceList.prev().children().length === 0)
                ArrangeModule.slideSourceList.prev().remove();
            ArrangeModule.slideSourceList.remove();
        }
        // If the target list was empty before current item appeared in it
        if(ArrangeModule.slideTargetList.children().length === 1) {
            ArrangeModule.slideTargetList.removeClass('empty-arrange-list');
            var $prevElements = ArrangeModule.slideTargetList.prev();
            if ($prevElements.length === 0 || $prevElements.children().length > 0) {
                var arrangeList = new ArrangeModule.tileListView().render().$el;
                arrangeList.addClass('empty-arrange-list');
                arrangeList.insertBefore(ArrangeModule.slideTargetList);
                ArrangeModule.initSortable(arrangeList);
            }
            var $nextElements = ArrangeModule.slideTargetList.next();
            if ($nextElements.length === 0 || $nextElements.children().length > 0) {
                var arrangeList = new ArrangeModule.tileListView().render().$el;
                arrangeList.addClass('empty-arrange-list');
                arrangeList.insertAfter(ArrangeModule.slideTargetList);
                ArrangeModule.initSortable(arrangeList);
            }
        }
    };

    ArrangeModule.createSortableLists = function() {
        // Create  a sortable list for each stack of slides
        $(lessonStudio.slidesWrapper + ' .slides > section').each(function() {
            var arrangeList = new ArrangeModule.tileListView().render().$el;
            $(lessonStudio.slidesWrapper + ' #arrangeSlides tr:first').append(arrangeList);
            $(this).find('> section').each(function() {
                if(!$(this).attr('id')) return;
                ArrangeModule.renderSlide(parseInt($(this).attr('id').slice(6)), arrangeList)
            });
            ArrangeModule.initSortable(arrangeList);
            // Create an empty sortable list after each list
            ArrangeModule.createEmptyList(arrangeList);
        });
        ArrangeModule.createFirstEmptyList();

        if($(lessonStudio.slidesWrapper + ' .js-slides-arrange-tile').length == 1)
            $(lessonStudio.slidesWrapper + ' .js-arrange-tile-delete').hide();

        slidesApp.vent.trigger('arrange-module-ready');
    };

    ArrangeModule.renderSortableLists = function (slideOrder) {
        var $firstTr = $(lessonStudio.slidesWrapper + ' #arrangeSlides tr:first');
        $firstTr.empty();
        _.each(slideOrder, function (ids) {
            var arrangeList = new ArrangeModule.tileListView().render().$el;
            $firstTr.append(arrangeList);
            _.each(ids, function (id) {
                ArrangeModule.renderSlide(id, arrangeList)
            });
            ArrangeModule.initSortable(arrangeList);
            ArrangeModule.createEmptyList(arrangeList);
        });
        ArrangeModule.createFirstEmptyList();

        if ($(lessonStudio.slidesWrapper + ' .js-slides-arrange-tile').length == 1)
            $(lessonStudio.slidesWrapper + ' .js-arrange-tile-delete').hide();

        slidesApp.vent.trigger('arrange-module-ready');
    };


    ArrangeModule.renderSlide = function(slideId, arrangeList){
        var arrangeTile = (arrangeList)
            ? new ArrangeModule.tileView({ id: 'slidesArrangeTile_' + slideId }).render().$el
            : $(lessonStudio.slidesWrapper + ' #slidesArrangeTile_' + slideId);

        if (arrangeList) {
            arrangeList.append(arrangeTile);
        }
        else {
            arrangeTile.find('.slides-thumbnail').empty();
        }
        // Create a thumbnail for the slide
        var slideThumbnail = new ArrangeModule.slideThumbnailView().render().$el;
        ArrangeModule.changeFont(slideId, slideThumbnail);
        slideThumbnail.find('section').show().removeAttr('aria-hidden');//show if hidden
        ArrangeModule.changeBackgroundColor(slideId, slideThumbnail);
        ArrangeModule.changeBackgroundImage(slideId, slideThumbnail);
        slideThumbnail.insertBefore(arrangeTile.find('.js-arrange-tile-controls'));
    };

    ArrangeModule.createEmptyList = function(arrangeList) {
        arrangeList = new ArrangeModule.tileListView().render().$el;
        arrangeList.addClass('empty-arrange-list');
        $(lessonStudio.slidesWrapper + ' #arrangeSlides tr:first').append(arrangeList);
        ArrangeModule.initSortable(arrangeList);
    };

    ArrangeModule.createFirstEmptyList = function() {
        var firstList = new ArrangeModule.tileListView().render().$el;
        firstList.addClass('empty-arrange-list');
        firstList.insertBefore($(lessonStudio.slidesWrapper + ' .js-sortable-slide-list').first());
        ArrangeModule.initSortable(firstList);
    };

    ArrangeModule.changeBackgroundColor = function (slideId, slideThumbnail) {
        var thumbnail = slideThumbnail ||
          $(lessonStudio.slidesWrapper + ' #arrangeContainer #slidesArrangeTile_' + slideId + ' .slides-thumbnail');
        var slide = $(lessonStudio.slidesWrapper + ' #slide_' + slideId);
        var bgColor = slide.attr('data-background-color');
        thumbnail.css({
            'background-color': bgColor
        });
    };

    ArrangeModule.changeBackgroundImage = function (slideId, slideThumbnail) {
        var thumbnail = slideThumbnail ||
          $(lessonStudio.slidesWrapper + ' #arrangeContainer #slidesArrangeTile_' + slideId + ' .slides-thumbnail');
        var slide = $(lessonStudio.slidesWrapper + ' #slide_' + slideId);
        var bgImage = slide.attr('data-background-image');
        var bgSize = slide.attr('data-background-size');
        thumbnail.css({
            'background-image': (bgImage) ? 'url("' + bgImage + '")' : '',
            'background-size': (bgImage) ? bgSize : '',
            'background-repeat': (bgImage) ? 'no-repeat' : '',
            'background-position': (bgImage) ? 'center' : ''
        });
    };

    ArrangeModule.changeFont = function (slideId, slideThumbnail) {
        var thumbnail = slideThumbnail ||
          $(lessonStudio.slidesWrapper + ' #arrangeContainer #slidesArrangeTile_' + slideId + ' .slides-thumbnail');
        var slide = $(lessonStudio.slidesWrapper + ' #slide_' + slideId);
        thumbnail.find('section').remove();
        slide.clone().attr('id', 'slideThumbnail_' + slideId).appendTo(thumbnail);
    };

    ArrangeModule.updateSlideRefs = function() {
        var lists = $(lessonStudio.slidesWrapper + ' .js-sortable-slide-list:has(>div)'),
          i = 0,
          j = 0;
        ArrangeModule.slideOrder = [];
        lists.each(function() {
            j = 0;
            var list = $(this);
            var listOrder = [];
            list.find('.js-slides-arrange-tile').each(function() {
                var listElement = $(this),
                    listElementId = parseInt(listElement.attr('id').slice(listElement.attr('id').indexOf('_') + 1)),
                    slideModel = slidesApp.getSlideModel(listElementId),
                    topListElement = listElement.prev(),
                    leftListElement = listElement.parent().prevAll('.js-sortable-slide-list:has(>div)').first().children().first(),
                    topListElementId = topListElement.length > 0
                        ? getSlideId(topListElement)
                        : undefined,
                    leftListElementId = leftListElement.length > 0
                        ? getSlideId(leftListElement)
                        : undefined;
                listOrder.push(listElementId);
                if(slideModel){
                    //need to set this attribute in order to know that this model should be updated
                    slideModel.set('changed', true, {silent: true});
                    //Only top row slides can have left one and we set them later
                    slideModel.unset('leftSlideId', { silent: true });
                    slideModel.unset('topSlideId', { silent: true });
                    if(topListElementId)
                        slideModel.set('topSlideId', topListElementId, { silent: true });
                    if(j === 0) {
                        // If it is a slide from the top row (where slides CAN refer to the left)
                        if(leftListElementId) {
                            slideModel.set('leftSlideId', leftListElementId, { silent: true });
                        }
                    }
                }
                j++;
            });
            i++;
            ArrangeModule.slideOrder.push(listOrder)
        });
        if($(lessonStudio.slidesWrapper + ' .js-slides-arrange-tile').length == 1)
            $(lessonStudio.slidesWrapper + ' .js-arrange-tile-delete').hide();
        else
            $(lessonStudio.slidesWrapper + ' .js-arrange-tile-delete').show();
    };

    ArrangeModule.initDraggable = function() {
        var $arrangeContainer = $(lessonStudio.slidesWrapper + ' #arrangeContainer');
        var $arrangeSlides= $(lessonStudio.slidesWrapper + ' #arrangeSlides');
        var $arrangeSlideTable = $arrangeSlides.find('table');
        $arrangeContainer.find('table').css('width', 'auto');
        var sortableListContainerWidth = $arrangeContainer.width(),
            sortableListContainerHeight = $arrangeContainer.height();
        var sortableListTableWidth = Math.max($arrangeSlideTable.width(), $arrangeContainer.width()),
            sortableListTableHeight = $arrangeSlideTable.height();
        var containmentStartX = 0 - Math.abs(sortableListContainerWidth - sortableListTableWidth),
            containmentStartY = 0 - Math.abs(sortableListContainerHeight - sortableListTableHeight),
            containmentEndX = 0,
            containmentEndY = 0,
            scrollTop = $( document ).scrollTop();
        if(sortableListTableWidth < sortableListContainerWidth) {
            containmentStartX = Math.abs(sortableListContainerWidth - sortableListTableWidth) / 2;
            containmentEndX = containmentStartX;
        }
        if(sortableListTableHeight < sortableListContainerHeight) {
            containmentStartY = containmentEndY = 0;
        }
        containmentEndY += $(lessonStudio.slidesWrapper + ' .js-slides-editor-topbar').outerHeight();
        containmentStartY += scrollTop + lessonStudio.fixedSizes.TOPBAR_HEIGHT;
        containmentEndY += scrollTop;
        if(sortableListTableWidth > sortableListContainerWidth || sortableListTableHeight > sortableListContainerHeight) {
            if(!$arrangeSlides.data('ui-draggable')){
                $arrangeSlides.draggable();
            }
            $arrangeSlides
                .draggable("option", "containment", [ containmentStartX, containmentStartY, containmentEndX, containmentEndY ]);
        }
    };

    ArrangeModule.onSlidesUpdated = function(){
        _.defer(function(){
            $(lessonStudio.slidesWrapper + ' #arrangeSlides tr:first').empty();
            ArrangeModule.createSortableLists();
        });
    };

    ArrangeModule.onSlidesOrderUpdated = function() {
        if (slidesApp.slideSetModel.get('slideOrder')) {
            ArrangeModule.renderSortableLists(slidesApp.slideSetModel.get('slideOrder'));
        } else {
            $(lessonStudio.slidesWrapper + ' #arrangeSlides tr:first').empty();
            ArrangeModule.createSortableLists();
        }
        ArrangeModule.updateSlideRefs();
    };

    ArrangeModule.onSlideRemove = function(model, toBeRemoved){
        if( toBeRemoved ) {
            var listElement = $(lessonStudio.slidesWrapper + ' #slidesArrangeTile_' + model.getId());
            if (listElement.siblings().length === 0) {
                listElement.parent().prev().remove();
                listElement.parent().remove();
            }
            listElement.remove();
        } else {
            //When undo action complete
            slidesApp.historyManager
                .once('undo:after', arrangeModule.onSlidesUpdated, arrangeModule);
        }
    };


    ArrangeModule.onStart = function(){
        var arrangeModule = this;
        setTimeout(function() {
            valamisApp.execute('notify', 'info', Valamis.language['lessonModeSwitchingLabel'], { 'timeOut': '0', 'extendedTimeOut': '0' });
        }, 0);
        setTimeout(function() {
            $arrangeContainer = $(lessonStudio.slidesWrapper + ' #arrangeContainer');
            slidesApp.vent.on('arrange-module-ready', function(){
                setTimeout(function () {
                    $arrangeContainer.show();
                    $arrangeContainer.prevAll().hide();
                    valamisApp.execute('notify', 'clear');
                    arrangeModule.initDraggable();
                }, 0);
            });
            $arrangeContainer.append(arrangeView.render().el);
            var $parent = $(window.parent);
            $arrangeContainer.height($parent.height() - $(lessonStudio.slidesWrapper + ' .js-slides-editor-topbar').outerHeight());
            $arrangeContainer.width($parent.width());

            arrangeModule.subAction = window.editorMode && window.editorMode.indexOf(':') > -1
                ? _.last(window.editorMode.split(':'))
                : 'default';
            arrangeModule.sortableEnabled = _.indexOf(['select', 'select-incorrect'], arrangeModule.subAction) == -1;
            arrangeModule.createSortableLists();
        }, 500);

        slidesApp.slideSetModel
            .on('change:slideOrder', arrangeModule.onSlidesOrderUpdated, arrangeModule);
        slidesApp.slideCollection
            .on('change:toBeRemoved', arrangeModule.onSlideRemove, arrangeModule);

        arrangeModule.isChanged = false;
    };

    ArrangeModule.onStop = function(){
        var arrangeModule = this;

        slidesApp.vent.off('arrange-module-ready');
        arrangeModule.updateSlideRefs();
        window.editorMode = null;
        arrangeModule.isChanged = false;
        $(lessonStudio.slidesWrapper + ' #arrangeContainer').hide();
        slidesApp.slideSetModel
            .off('change:slideOrder', arrangeModule.onSlidesOrderUpdated);
        slidesApp.slideCollection
            .off('change:toBeRemoved', arrangeModule.onSlideRemove);
        if(slidesApp.historyManager){
            slidesApp.historyManager.off('undo:after', arrangeModule.onSlidesUpdated);
        }
    }
});
