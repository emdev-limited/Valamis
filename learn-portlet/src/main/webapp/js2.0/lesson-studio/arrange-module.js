var arrangeModule = slidesApp.module('ArrangeModule', function (ArrangeModule, slidesApp, Backbone, Marionette, $, _) {
    ArrangeModule.startWithParent = false;
    ArrangeModule.subAction = 'default';
    ArrangeModule.sortableEnabled = true;
    ArrangeModule.dragStarted = false;

    ArrangeModule.View = Marionette.ItemView.extend({
        template: '#arrangeTemplate',
        id: 'arrangeSlides'
    });

    ArrangeModule.tileListView = Marionette.ItemView.extend({
        tagName: 'td',
        template: '#arrangeListTemplate',
        className: 'tileListTemp js-sortable-slide-list'
    });
    ArrangeModule.tileView = Marionette.ItemView.extend({
        template: '#arrangeTileTemplate',
        className: 'slides-arrange-tile js-slides-arrange-tile text-center',
        events: {
            'mouseover': 'onMouseOver',
            'mouseout': 'onMouseOut',
            'click .js-arrange-tile-cover': 'onClick',
            'click .js-arrange-tile-preview': 'goToSlide',
            'click .js-arrange-tile-edit': 'editSlide',
            'click .js-arrange-tile-delete': 'deleteSlide',
            'click .js-arrange-tile-select': 'selectSlide'
        },
        initialize: function () {
            this.template = _.template(Mustache.to_html(jQueryValamis(this.template).html(), Valamis.language));
        },
        onRender: function(){
            this.$('.valamis-tooltip')
                .tooltip({
                    placement: 'right',
                    trigger: 'manual'
                })
                .bind('mouseenter', function(){
                    jQueryValamis(this).tooltip('show');
                    var tooltip = jQueryValamis(this).data('bs.tooltip').$tip;
                    tooltip
                        .css({
                            whiteSpace: 'nowrap',
                            left: '+=20'
                        });
                })
                .bind('mouseleave', function(){
                    jQueryValamis(this).tooltip('hide');
                })
                .parent().css('position','relative');
        },
        onMouseOver: function (e) {
            if( _.indexOf(['select','select-incorrect'], arrangeModule.subAction) > -1 ) {
                this.$('.js-arrange-tile-controls > div').addClass('hidden');
                this.$('.js-arrange-tile-controls').find('.js-arrange-tile-select').parent().removeClass('hidden');
                this.$('.js-arrange-tile-controls').show();
                jQueryValamis('#arrangeContainer .js-slides-arrange-tile').removeClass('arrange-tile-active');
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
                if( jQueryValamis(e.target).is('.arrange-tile-cover') && _.indexOf(['select','select-incorrect'], arrangeModule.subAction) > -1 ){
                    this.selectSlide();
                } else {
                    this.goToSlide();
                }
            }
        },
        goToSlide: function (e) {
            if(e) e.preventDefault();
            var slideId = parseInt(this.$el.attr('id').slice(this.$el.attr('id').indexOf('_') + 1));
            slidesApp.switchMode('preview', false, slideId);
        },
        editSlide: function (e) {
            if(e) e.preventDefault();
            var slideId = parseInt(this.$el.attr('id').slice(this.$el.attr('id').indexOf('_') + 1));
            slidesApp.switchMode('edit', false, slideId);
        },
        deleteSlide: function (e) {
            if(e) e.preventDefault();
            var slideId = parseInt(this.$el.attr('id').slice(this.$el.attr('id').indexOf('_') + 1));
            var slideModel = slidesApp.getSlideModel(slideId);
            slideModel.set('toBeRemoved', true);
            var listElement = jQueryValamis(e.target).closest('.js-slides-arrange-tile'),
                topListElement = listElement.prev(),
                leftListElement = listElement.parent().prevAll('.js-sortable-slide-list:has(>div)').first().children().first();
            var slideIndices = slidesApp.slideRegistry.getBySlideId(slideId);
            var slideEntities = slidesApp.slideElementCollection.where({slideId: slideId});
            var slideThumbnail = jQueryValamis('#slidesArrangeTile_' + slideId).clone();
            var rightSlideModel = slidesApp.slideCollection.where({leftSlideId: slideId || slideModel.get('tempId')})[0];
            var bottomSlideModel = slidesApp.slideCollection.where({topSlideId: slideId || slideModel.get('tempId')})[0];

            slidesApp.oldValue = {
                indices: { h: slideIndices.h, v: slideIndices.v },
                slideModel: slideModel,
                slideEntities: slideEntities,
                slideThumbnail: slideThumbnail,
                rightSlideId: rightSlideModel ? (rightSlideModel.id || rightSlideModel.get('tempId')) : undefined,
                bottomSlideId: bottomSlideModel ? (bottomSlideModel.id || bottomSlideModel.get('tempId')) : undefined
            };
            if(topListElement.length > 0)
                slidesApp.oldValue.direction = 'down';
            else if(leftListElement.length > 0)
                slidesApp.oldValue.direction = 'right';
            slidesApp.viewId = this.cid;
            slidesApp.actionType = 'slideRemoved';
            slidesApp.newValue = null;
            slidesApp.execute('action:push');

            if (listElement.siblings().length === 0) {
                listElement.parent().prev().remove();
                listElement.parent().remove();
            }
            listElement.remove();

            ArrangeModule.updateSlideRefs();
        },
        selectSlide: function(e){
            if(e) e.preventDefault();
            var slideId = parseInt(this.$el.attr('id').replace('slidesArrangeTile_','')),
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
        className: 'slides-thumbnail js-slides-thumbnail'
    });

    ArrangeModule.initSortable = function(elem) {
        if( !ArrangeModule.sortableEnabled ){
            return;
        }
        elem.sortable({
            placeholder: 'slides-arrange-placeholder',
            revert: true,
            delay: 50,
            connectWith: '.js-sortable-slide-list',
            sort: function(e, ui) {
                var placeholderBackground = jQueryValamis('<div></div>').css({
                    'width': '196px',
                    'height': '146px'
                });
                jQueryValamis(ui.placeholder).html('');
                jQueryValamis(ui.placeholder).append(placeholderBackground);
                jQueryValamis(ui.placeholder).addClass('slides-arrange-placeholder');
            },
            start: function(e, ui) {
                var slideId = parseInt(jQueryValamis(ui.item).attr('id').slice(jQueryValamis(ui.item).attr('id').indexOf('_') + 1));
                var slideModel = slidesApp.getSlideModel(slideId);
                ArrangeModule.slideSourceList = jQueryValamis(e.currentTarget);
                var rightSlideModel = slidesApp.slideCollection.where({leftSlideId: slideId || slideModel.get('tempId')})[0];
                var bottomSlideModel = slidesApp.slideCollection.where({topSlideId: slideId || slideModel.get('tempId')})[0];
                slidesApp.oldValue = {
                    slideAttrs: {
                        slideId: slideModel.id || slideModel.get('tempId'),
                        leftSlideId: slideModel.get('leftSlideId'),
                        topSlideId: slideModel.get('topSlideId')
                    },
                    rightSlideId: rightSlideModel ? (rightSlideModel.id || rightSlideModel.get('tempId')) : undefined,
                    bottomSlideId: bottomSlideModel ? (bottomSlideModel.id || bottomSlideModel.get('tempId')) : undefined
                };
                ArrangeModule.dragStarted = true;
            },
            stop: function(e, ui) {
                jQueryValamis(ui.placeholder).html('');
                jQueryValamis(ui.placeholder).removeClass('slides-arrange-placeholder');
                ArrangeModule.dragStarted = false;
            },
            receive: function(e, ui) {
                ArrangeModule.slideTargetList = jQueryValamis(e.target);
                ArrangeModule.manageSortableLists();
            },
            update: function(e, ui) {
                if(ui.sender === null) {
                    ArrangeModule.updateSlideRefs();
                    var slideId = parseInt(jQueryValamis(ui.item).attr('id').slice(jQueryValamis(ui.item).attr('id').replace('slidesArrangeTile_', '')));
                    var slideModel = slidesApp.getSlideModel(slideId);
                    slidesApp.viewId = undefined;
                    slidesApp.actionType = 'slideOrderChanged';
                    slidesApp.newValue = { slideModel: slideModel };
                    slidesApp.execute('action:push');
                    ArrangeModule.initDraggable();
                }
            }
        }).disableSelection();
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
            if (ArrangeModule.slideTargetList.prev().length === 0 || ArrangeModule.slideTargetList.prev().children().length > 0) {
                var arrangeList = jQueryValamis((new ArrangeModule.tileListView()).render().el);
                arrangeList.addClass('empty-arrange-list');
                arrangeList.insertBefore(ArrangeModule.slideTargetList);
                ArrangeModule.initSortable(arrangeList);
            }
            if (ArrangeModule.slideTargetList.next().length === 0 || ArrangeModule.slideTargetList.next().children().length > 0) {
                var arrangeList = jQueryValamis((new ArrangeModule.tileListView()).render().el);
                arrangeList.addClass('empty-arrange-list');
                arrangeList.insertAfter(ArrangeModule.slideTargetList);
                ArrangeModule.initSortable(arrangeList);
            }
        }
    };
    ArrangeModule.createSortableLists = function() {
        // Create  a sortable list for each stack of slides
        jQueryValamis('.slides > section').each(function() {
            var arrangeList = jQueryValamis((new ArrangeModule.tileListView()).render().el);
            jQueryValamis('#arrangeSlides tr:first').append(arrangeList);
            jQueryValamis(this).find('> section').each(function() {
                if(!jQueryValamis(this).attr('id')) return;
                var slideId = parseInt(jQueryValamis(this).attr('id').slice(6));
                var arrangeTile = jQueryValamis((new ArrangeModule.tileView({ id: 'slidesArrangeTile_' + slideId })).render().el);
                arrangeList.append(arrangeTile);
                // Create a thumbnail for the slide
                var slideThumbnail = jQueryValamis((new ArrangeModule.slideThumbnailView()).render().el);
                var originalSection = jQueryValamis('#slide_' + slideId);
                originalSection.clone().attr('id', 'slideThumbnail_' + slideId).appendTo(slideThumbnail);
                slideThumbnail.find('section').show().removeAttr('aria-hidden');//show if hidden

                var bgImage = originalSection.attr('data-background-image');
                var bgSize = originalSection.attr('data-background-size');
                var bgColor = originalSection.attr('data-background-color');
                slideThumbnail.css({'background-color': bgColor});
                if(bgImage)
                    slideThumbnail.css({
                        'background-image': 'url("' + bgImage + '")',
                        'background-size': bgSize,
                        'background-repeat': 'no-repeat',
                        'background-position': 'center'
                    });

                slideThumbnail.insertBefore(arrangeTile.find('.js-arrange-tile-controls'));

            });

            ArrangeModule.initSortable(arrangeList);
            // Create an empty sortable list after each list
            arrangeList = jQueryValamis((new ArrangeModule.tileListView()).render().el);
            arrangeList.addClass('empty-arrange-list');
            jQueryValamis('#arrangeSlides tr:first').append(arrangeList);
            ArrangeModule.initSortable(arrangeList);
        });
        // Add an additional sortable list at the beginning
        var firstList = jQueryValamis(new ArrangeModule.tileListView().render().el);
        firstList.addClass('empty-arrange-list');
        firstList.insertBefore(jQueryValamis('.js-sortable-slide-list').first());
        ArrangeModule.initSortable(firstList);

        if(jQueryValamis('.js-slides-arrange-tile').length == 1)
            jQueryValamis('.js-arrange-tile-delete').hide();

        jQueryValamis(document).trigger('arrange-module-ready');
    };

    ArrangeModule.updateSlideRefs = function() {
        var lists = jQueryValamis('.js-sortable-slide-list:has(>div)'), i = 0, j = 0;
        lists.each(function() {
            j = 0;
            var list = jQueryValamis(this);
            list.find('.js-slides-arrange-tile').each(function() {
                var listElement = jQueryValamis(this),
                    listElementId = parseInt(listElement.attr('id').slice(listElement.attr('id').indexOf('_') + 1)),
                    slideModel = slidesApp.getSlideModel(listElementId),
                    topListElement = listElement.prev(),
                    leftListElement = listElement.parent().prevAll('.js-sortable-slide-list:has(>div)').first().children().first(),
                    topListElementId = topListElement.length > 0
                        ? parseInt(topListElement.attr('id').slice(topListElement.attr('id').indexOf('_') + 1))
                        : undefined,
                    leftListElementId = leftListElement.length > 0
                        ? parseInt(leftListElement.attr('id').slice(leftListElement.attr('id').indexOf('_') + 1))
                        : undefined;
                if(slideModel){
                    //Only top row slides can have left one and we set them later
                    slideModel.unset('leftSlideId');
                    slideModel.unset('topSlideId');
                    if(topListElementId)
                        slideModel.set('topSlideId', topListElementId);
                    if(j === 0) {
                        // If it is a slide from the top row (where slides CAN refer to the left)
                        if(leftListElementId) {
                            slideModel.set('leftSlideId', leftListElementId);
                        }
                    }
                }
                j++;
            });
            i++;
        });
        if(jQueryValamis('.js-slides-arrange-tile').length == 1)
            jQueryValamis('.js-arrange-tile-delete').hide();
        else
            jQueryValamis('.js-arrange-tile-delete').show();
    };

    ArrangeModule.initDraggable = function() {
        jQueryValamis('#arrangeSlides table').css('width', 'auto');
        var sortableListContainerWidth = jQueryValamis('#arrangeContainer').width(),
            sortableListContainerHeight = jQueryValamis('#arrangeContainer').height();
        var sortableListTableWidth = Math.max(jQueryValamis('#arrangeSlides table').width(), jQueryValamis('#arrangeContainer').width()),
            sortableListTableHeight = jQueryValamis('#arrangeSlides table').height();
        var containmentStartX = 0 - Math.abs(sortableListContainerWidth - sortableListTableWidth),
            containmentStartY = 0 - Math.abs(sortableListContainerHeight - sortableListTableHeight),
            containmentEndX = 0,
            containmentEndY = 0,
            scrollTop = jQueryValamis( document ).scrollTop();
        if(sortableListTableWidth < sortableListContainerWidth) {
            containmentStartX = Math.abs(sortableListContainerWidth - sortableListTableWidth) / 2;
            containmentEndX = containmentStartX;
        }
        if(sortableListTableHeight < sortableListContainerHeight) {
            containmentStartY = containmentEndY = 0;
        }
        containmentEndY += jQueryValamis('.js-slides-editor-topbar').outerHeight();
        containmentStartY += scrollTop + lessonStudio.fixedSizes.TOPBAR_HEIGHT;
        containmentEndY += scrollTop;
        if(sortableListTableWidth > sortableListContainerWidth || sortableListTableHeight > sortableListContainerHeight) {
            if(!jQueryValamis('#arrangeSlides').data('uiDraggable')){
                jQueryValamis('#arrangeSlides').draggable();
            }
            jQueryValamis('#arrangeSlides')
                .draggable("option", "containment", [ containmentStartX, containmentStartY, containmentEndX, containmentEndY ]);
        }
    };
});

var arrangeView = new arrangeModule.View();

arrangeModule.on('start', function() {
    setTimeout(function() {
        valamisApp.execute('notify', 'info', Valamis.language['lessonModeSwitchingLabel'], { 'timeOut': '0', 'extendedTimeOut': '0' });
    }, 0);
    setTimeout(function() {
        jQueryValamis(document).on('arrange-module-ready', function(){
            setTimeout(function () {
                jQueryValamis('#arrangeContainer').show();
                jQueryValamis('#arrangeContainer').prevAll().hide();
                valamisApp.execute('notify', 'clear');
                arrangeModule.initDraggable();
            }, 0);
        });
        jQueryValamis('#arrangeContainer').append(arrangeView.render().el);
        jQueryValamis('#arrangeContainer').height(jQueryValamis(window.parent).height() - jQueryValamis('.js-slides-editor-topbar').outerHeight());
        jQueryValamis('#arrangeContainer').width(jQueryValamis(window.parent).width());
        jQueryValamis('body').css('background-color', '#f2f2f2');
        arrangeModule.subAction = window.editorMode && window.editorMode.indexOf(':') > -1
            ? _.last(window.editorMode.split(':'))
            : 'default';
        arrangeModule.sortableEnabled = _.indexOf(['select', 'select-incorrect'], arrangeModule.subAction) == -1;
        arrangeModule.createSortableLists();
    }, 500);
});

arrangeModule.on('stop', function() {
    jQueryValamis(document).off('arrange-module-ready');
    this.updateSlideRefs();
    window.editorMode = null;
    jQueryValamis('#arrangeContainer').hide();
    jQueryValamis('body').css('background-color', '');
});
