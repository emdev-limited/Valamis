
function placeSlideControls(windowWidth, windowHeight) {
    windowWidth = windowWidth || jQueryValamis(window).width();
    windowHeight = windowHeight || jQueryValamis(window).height();

    var sidebarWidth = !jQueryValamis('.sidebar').is(':hidden') ? lessonStudio.fixedSizes.SIDEBAR_WIDTH : 0,
        container = jQueryValamis('#revealEditor'),
        revealWrapper = container.find('.reveal-wrapper'),
        mainWrapper = container.find('.slides-editor-main-wrapper'),
        workArea = container.find('.slides-work-area-wrapper'),
        slideContainer = revealWrapper.find('.reveal > .slides'),
        revealControls = container.find('.reveal-controls'),
        slideControls = jQueryValamis('#slide-controls'),
        isPositionOnTop = windowHeight < workArea.height() + lessonStudio.fixedSizes.TOPBAR_HEIGHT + 60;

    mainWrapper.find('.layout-resizable-handle').toggleClass('hidden', false);
    if( slidesApp.RevealControlsModule && slidesApp.RevealControlsModule.view ){
        slidesApp.RevealControlsModule.view.ui.buttons_add_page.show();
    }

    if(container.size() > 0 && slideContainer.size() > 0) {

        mainWrapper
            .css({
                marginLeft: sidebarWidth
            });

        revealControls.find('.button.down').toggleClass( 'fixed', isPositionOnTop );

        var buttonRightToggle = windowWidth < workArea.width() + sidebarWidth + (slideControls.width() * 2);
        revealControls.find('.button.right').toggleClass( 'fixed', buttonRightToggle );

        slideControls.toggleClass( 'fixed', buttonRightToggle );

        if (slidesApp.mode.indexOf('arrange') != -1) {
            jQueryValamis('#arrangeContainer').height(windowHeight - lessonStudio.fixedSizes.TOPBAR_HEIGHT);
            jQueryValamis('#arrangeContainer').width(windowWidth);
            arrangeModule.initDraggable();
        }
    }
}

function placeTemplateModal(windowWidth, windowHeight, isDirectionDown) {
    if( jQueryValamis('.slide-templates-modal').size() == 0 ){
        return;
    }
    var sidebarWidth = !jQueryValamis('.sidebar').is(':hidden') ? lessonStudio.fixedSizes.SIDEBAR_WIDTH : 0;
    var buttonMargin = 20;
    var rightButtonClientRect = jQueryValamis('.js-add-page.right').get(0).getBoundingClientRect(),
        downButtonClientRect = jQueryValamis('.js-add-page.down').get(0).getBoundingClientRect(),
        templateModal = jQueryValamis('.slide-templates-modal');

    if( typeof isDirectionDown == 'undefined' ){
        isDirectionDown = jQueryValamis('.slide-templates-modal').is('.downPosition');
    }

    if (isDirectionDown) {
        templateModal.find('.bbm-modal--open').css({
            'position': 'absolute',
            'left': ((windowWidth - sidebarWidth) / 2)
                - (templateModal.find('.bbm-modal--open').outerWidth() / 2)
                + sidebarWidth - 5,
            'top': 'auto',
            'bottom': windowHeight - ( downButtonClientRect.top - buttonMargin )
        });

        if( templateModal.find('.arrow-down').size() == 0 )
            templateModal.find('.bbm-modal--open')
                .prepend('<div class="arrow-down"></div>');
    }
    else {

        templateModal.find('.bbm-modal--open').css({
            'position': 'absolute',
            'left': 'auto',
            'right': windowWidth - ( rightButtonClientRect.left - buttonMargin ),
            'top': windowHeight / 2
                - ( templateModal.find('.bbm-modal--open').outerHeight() / 2 )
        });
        if( templateModal.find('.arrow-right').size() == 0 )
            templateModal.find('.bbm-modal--open')
                .prepend('<div class="arrow-right"></div>');

        var rightButtonOffsetY = rightButtonClientRect.top;
        rightButtonOffsetY -= templateModal.find('.bbm-modal--open').position()['top'];
        rightButtonOffsetY += jQueryValamis('.js-add-page.right').outerHeight() / 2 - 5;

        templateModal.find('.arrow-right').css({
            'top': rightButtonOffsetY
        });
    }
}

(function() {
    var objGlobal = this;
    if(!(objGlobal.escape && objGlobal.unescape)) {
        var escapeHash = {
            _ : function(input) {
                var ret = escapeHash[input];
                if(!ret) {
                    if(input.length - 1) {
                        ret = String.fromCharCode(input.substring(input.length - 3 ? 2 : 1));
                    }
                    else {
                        var code = input.charCodeAt(0);
                        ret = code < 256
                            ? "%" + (0 + code.toString(16)).slice(-2).toUpperCase()
                            : "%u" + ("000" + code.toString(16)).slice(-4).toUpperCase();
                    }
                    escapeHash[ret] = input;
                    escapeHash[input] = ret;
                }
                return ret;
            }
        };
        objGlobal.escape = objGlobal.escape || function(str) {
            return str.replace(/[^\w @\*\-\+\.\/]/g, function(aChar) {
                return escapeHash._(aChar);
            });
        };
        objGlobal.unescape = objGlobal.unescape || function(str) {
            return str.replace(/%(u[\da-f]{4}|[\da-f]{2})/gi, function(seq) {
                return escapeHash._(seq);
            });
        };
    }
})();

var mimeToExt = {
    'image': {
        'image/jpeg': 'jpg',
        'image/png': 'png',
        'image/gif': 'gif',
        'image/pjpeg': 'jpeg',
        'image/svg+xml': 'svg',
        'image/tiff': 'tiff',
        'image/x-targa': 'tga',
        'image/x-tga': 'tga',
        'image/vnd.microsoft.icon': 'ico',
        'image/bmp': 'bmp'
    },
    'video': {
        'video/mp4': 'mp4',
        'video/mpeg': 'mpeg',
        'video/x-flv': 'flv',
        'video/3gpp': '3gp',
        'video/quicktime': 'mov',
        'video/x-msvideo': 'avi',
        'video/ogg': 'ogv',
        'video/webm': 'webm'
    },
    'pdf': {
        'application/pdf': 'pdf'
    },
    'pptx': {
        'application/vnd.ms-powerpoint': 'ppt',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx'
    },
    'webgl': {
        'application/json': 'json',
        'application/javascript': 'js',
        'text/javascript': 'js',
        'text/plain': 'obj'
    }
};

function getExtByMime(mime) {
    for(var i in mimeToExt)
        if (mimeToExt[i].hasOwnProperty(mime))
            return mimeToExt[i][mime];
    return null;
}

function getMimeTypeGroupValues(typeGroup) {
    return _.chain(mimeToExt[typeGroup])
        .values()
        .compact()
        .uniq()
        .value();
}

var getQuestionAppearance = function (model) {
    var questionFontParts = [],
        answerFontParts = [],
        defaults = new lessonStudioModels.LessonPageThemeModel().defaults();
    if (model.get('questionFont'))  questionFontParts = model.get('questionFont').split('$');
    if (model.get('answerFont'))    answerFontParts = model.get('answerFont').split('$');

    var font = _.object(['fontFamily','fontSize','fontColor'], [model.get('fontFamily'), model.get('fontSize'), model.get('fontColor')]);
    _.defaults( font, _.pick( defaults, ['fontFamily','fontSize','fontColor'] ) );
    return {
        question: {
            family: questionFontParts[0] || font.fontFamily,
            size: questionFontParts[1] || font.fontSize,
            color: questionFontParts[2] || font.fontColor
        },
        answer: {
            family: answerFontParts[0] || font.fontFamily,
            size: defaults.fontSize,
            color: defaults.fontColor,
            background: ''
        }
    };
};

var getAnswerElement = function (questionElement, type) {

    switch(type) {
        case QuestionType.ChoiceQuestion:
            return {
                background: questionElement.find('label.choiceTextContainer'),
                text: questionElement.find('label.choiceTextContainer p')
            };
        case QuestionType.ShortAnswerQuestion:
            return {
                background: questionElement.find('input.playerShortAnswerField'),
                text: questionElement.find('input.playerShortAnswerField')
            };
        case QuestionType.NumericQuestion:
            return {
                background: questionElement.find('input.playerShortAnswerField'),
                text: questionElement.find('input.playerShortAnswerField')
            };
        case QuestionType.PositioningQuestion:
            return {
                background: questionElement.find('.playerPlacingAnswers li'),
                text: questionElement.find('.playerPlacingAnswers li p')
            };
        case QuestionType.MatchingQuestion:
            return {
                background: questionElement.find('li.matchingQuestion'),
                text: questionElement.find('li.matchingQuestion')
            };
        case QuestionType.EssayQuestion:
            return {
                background: questionElement.find('textarea'),
                text: questionElement.find('textarea')
            };
        case QuestionType.CategorizationQuestion:
            return {
                background: questionElement.find('.playerCategorizeAnswers li'),
                text: questionElement.find('.playerCategorizeAnswers li p')
            };
        case QuestionType.PlainText:
            return {
                background: questionElement.find('.SCORMPlayerContentDisplay'),
                text: questionElement.find('.playerMainArea p')
            }
    }
};

navigator.sayswho = (function(){
    var N = navigator.appName, ua = navigator.userAgent, tem;
    var M = ua.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
    if(M && (tem = ua.match(/version\/([\.\d]+)/i)) != null) M[2] = tem[1];
    M = M ? [M[1], M[2]] : [N, navigator.appVersion,'-?'];
    return M;
})();

function createObjectURL (file) {
    if (window.URL && window.URL.createObjectURL) {
        return window.URL.createObjectURL(file);
    } else if (window.webkitURL) {
        return window.webkitURL.createObjectURL(file);
    } else {
        return null;
    }
}

function imgSrcToBlob(image_src, callback_func){
    var xhr = new XMLHttpRequest();
    xhr.open( 'GET', image_src, true );
    xhr.responseType = 'arraybuffer';
    xhr.onload = function() {
        if (xhr.status == 200) {
            var contentType = this.getResponseHeader('Content-Type'),
                arrayBufferView = new Uint8Array( this.response );
            var blob = new Blob( [ arrayBufferView ], { type: contentType } );
            if ( typeof callback_func == 'function' ) {
                callback_func(blob);
            }
        }
    };
    xhr.send();
}

/**
 * jQuery plugin to fit text in a container with fixed size
 *
 * Call it on the element that contains text (or other elements)
 * you need to fit in the container, the element's font-size will
 * be adjusted.
 *
 * @param {Number} width - width of the container to fit text in
 * @param {Number} height - height of the container to fit text in
 * @param {Boolean} considerNotFitting - whether to consider elements
 * that do not fit inside the container (have negative margins, etc.)
 */
$.fn.fitTextToContainer = function(container, considerNotFitting) {
    if($(container).size() == 0) container = $(this);
    var width = $(container).width();
    var height = $(container).height();
    var el = $(this);
    el.css('margin-top', 0);
    var maxPossibleSize = Math.max(width, height);
    var maxWidth = el.css('font-size', maxPossibleSize + 'px').width(),
        maxHeight = el.css('font-size', maxPossibleSize + 'px').height();
    var factor = Math.min(width / maxWidth, height / maxHeight);
    var size = maxPossibleSize * factor;
    el.css('font-size', size);
    if(considerNotFitting) {
        var minTopOffset =
            _.min(_.map(el.find('*')
                    .filter(function () {
                        var top = $(this).css('top');
                        return top && top !== 'auto';
                    }),
                function (item) {
                    return $(item).offset().top;
                }
            ));
        minTopOffset = _.isFinite(minTopOffset) ? minTopOffset : 0;
        var partialFactor =  height / (el.height() + Math.abs(el.offset().top - minTopOffset));
        partialFactor = ((Math.abs(el.height() - height) > Math.abs(el.offset().top - minTopOffset)))
            ? 1
            : partialFactor;
        var marginTop = Math.abs(minTopOffset - el.offset().top) * partialFactor;
        size = size * partialFactor;
        el.css({
            'font-size': size + 'px',
            'margin-top': marginTop + 'px'
        });
    }
};