/**
 * Created by igorborisov on 02.04.15.
 */

// valamis dropdown
(function($) {

    var methods = {
        init : function( options ) {
            var that = this;
            this.each(function(){
                var elem = $(this);

                var actionButton = elem.find(".button");
                var dropdownMenu = elem.find('.dropdown-menu');
                var arrowSpan = actionButton.find(">span[class*='val-icon-arrow']");
                actionButton.unbind('click').on('click', function(){
                    dropdownMenu.toggleClass("dropdown-visible");
                    arrowSpan.toggleClass("val-icon-arrow-down val-icon-arrow-up");
                });

                var dropdownItems = dropdownMenu.find("li");

                dropdownItems.each(function(ind, itm){
                    var item =$(itm);
                    item.unbind('click').click(function() {
                            if(!elem.hasClass('actions')){
                                item.addClass('selected').siblings().removeClass('selected');
                                item.closest('.dropdown')
                                    .data('value', item.data('value'))
                                    .find('.dropdown-text').html(item.html());
                            }
                            dropdownMenu.removeClass('dropdown-visible');
                            arrowSpan.toggleClass("val-icon-arrow-up val-icon-arrow-down");
                            $(document).ajaxStart(function() {
                                actionButton.prop('disabled', true);
                            });
                            $(document).ajaxStop(function() {
                                actionButton.prop('disabled', false);
                            });
                        }
                    );
                    }
                );

                $('body').on('click', function(e){
                    if (elem.has(e.target).length === 0) {
                        dropdownMenu.removeClass('dropdown-visible');
                        if(arrowSpan.hasClass("val-icon-arrow-up")) {
                            arrowSpan.toggleClass("val-icon-arrow-up val-icon-arrow-down");
                        }
                    }
                });
            });

            return this;
        },
        select: function(options){
            if(this.hasClass('actions')) return;

            var item = this.find('.dropdown-menu > li[data-value="'+ options +'"]');

            item.addClass('selected').siblings().removeClass('selected');
            item.closest('.dropdown')
                .data('value', item.data('value'))
                .find('.dropdown-text').html(item.html());
        },
        disable: function() {
            var elem = $(this);
            elem.find('.button').prop('disabled', true);
        },
        enable: function() {
            var elem = $(this);
            elem.find('.button').prop('disabled', false);
        }
    };

    $.fn.valamisDropDown = function(method) {

        if ( methods[method] ) {
            return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist in jQuery.valamisDropDown' );
        }

    };
})(jQuery);

//valamis sidebar
(function($) {
    $.fn.valamisSidebar = function() {

        this.each(function(){
            var elem = $(this);
            elem.unbind('click').click(function() {
                var portletContainer = elem.parents('.portlet-wrapper');
                portletContainer.toggleClass('sidebar-hidden');
                var isSidebarHidden = portletContainer.hasClass('sidebar-hidden');
                portletContainer.find('.portlet-sidebar')[0].setAttribute( 'aria-hidden', isSidebarHidden);
            });
        });
    };
})(jQuery);

//digits only
(function($){

    var defaults = {
        'allowNegative': false,
        'allowFloat': false
    };

    // methods
    var methods = {
        init : function(params) {

            var options = $.extend({}, defaults, params);

            return this.each(function() {
                var elem = $(this);

                elem.unbind('click').click(function(e){
                    if(e.target.selectionStart !== undefined) $(e.target).attr('caretpos', e.target.selectionStart);
                });
                elem.unbind('keypress').keypress(function(e){
                    $(e.target).attr('keypressvalue', $(e.target).val());
                    if(e.target.selectionStart !== undefined) {
                        $(e.target).attr('caretpos', e.target.selectionStart);
                    }
                    if(e.keyCode == 13) {
                        e.target.blur();
                    }
                });
                elem.unbind('input').on('input', function(e){
                    var pattern = '[0-9]*';
                    options.allowNegative ? pattern = '^-?' + pattern : pattern = '^' + pattern;
                    options.allowFloat ? pattern += '\\.?([0-9]+)?$' : pattern += '$';

                    var regexp = new RegExp(pattern);

                    if(!regexp.test($(e.target).val())) {
                        $(e.target).val($(e.target).attr('keypressvalue'));
                        if(e.target.setSelectionRange && $(e.target).attr('caretpos') !== undefined) {
                            var caretPosition = parseInt($(e.target).attr('caretpos'));
                            e.target.setSelectionRange(caretPosition, caretPosition);
                        }
                    }

                    var val = parseFloat($(e.target).val());
                    var regexp2 = new RegExp('\\.0*$');
                    if(!isNaN(val) && !regexp2.test($(e.target).val())) {
                        $(e.target).val(val);
                        $(e.target).attr('keypressvalue', val);
                    }

                    if (_.isFunction(options.callback_func)) {
                        options.callback_func();
                    }
                });
                elem.unbind('change').on('change', function(e){
                    var val = parseFloat($(e.target).val());
                    if(isNaN(val)) {
                        val = 0;
                    }
                    $(e.target).val(val);
                    $(e.target).attr('keypressvalue', val);

                    if (_.isFunction(options.callback_func)) {
                        options.callback_func();
                    }
                });
            });
        }
    };

    $.fn.valamisDigitsOnly = function(method) {
        if ( methods[method] ) {
            return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || !method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist in jQuery.valamisDigitsOnly' );
        }
    }
})(jQuery);

// `valamis plus-minus control
(function($) {

    var defaults = {
        'min': '0',
        'max': '100',
        'step': '1',
        'allowNegative': false,
        'allowFloat': false,
        'obligatory': true
    };

    var changeInputWidth = function(textInput, value){
        var amount = ('' + value).length;
        var width = 14 + 8 * (amount);
        if(width < 34) width = 34;
        textInput.css('width', width + 'px');
    };

// methods
    var methods = {
        init : function(params) {

            var getFixed = function(step){
                var afterComma = (''+ step).split('.')[1];
                return afterComma? afterComma.length : 0;
            };

            var fixValue = function(value){
                var minValue = parseFloat(options.min);
                var maxValue = parseFloat(options.max);
                var step = parseFloat(options.step);

                if(value > maxValue) value = maxValue;
                if(value < minValue) {
                    if(options.obligatory) {
                        value = minValue;
                    }
                    else {
                        value = '';
                    }
                }
                return value;
            };

            var options = $.extend({}, defaults, params);
            options.fixed = getFixed(options.step);

            var iniValue = '';
            if(options.obligatory) iniValue = options.min;

            var html = '<button class="button medium neutral no-text minus-button">'
                + '<span class="val-icon-minus"></span>'
                + '</button>'
                    //+ '<span>'
                + '<input type="text" class="text-input valamis" value="' + iniValue + '"/>'
                    //+ '</span>'
                + '<button class="button medium neutral no-text plus-button">'
                + '<span class="val-icon-plus"></span>'
                + '</button>';

            return this.each(function(){
                var elem = $(this);
                elem.html(html);
                elem.addClass('valamis-plus-minus');
                var minusButton = elem.find('button.minus-button');
                var plusButton = elem.find('button.plus-button');
                var textInput = elem.find('input[type=text]');

                minusButton.unbind('click').click(function() {
                    var step = parseFloat(options.step);
                    var value = parseFloat(textInput.val() || 0).toFixed(options.fixed);
                    var newValue = parseFloat(value) - step;

                    newValue = fixValue(newValue.toFixed(options.fixed));

                    textInput.val(newValue);
                    changeInputWidth(textInput, newValue);
                });

                plusButton.unbind('click').click(function() {
                    var step = parseFloat(options.step);
                    var maxValue = parseFloat(options.max);
                    var value = parseFloat(textInput.val() || options.min).toFixed(options.fixed);
                    if(!options.obligatory && textInput.val() == '') {
                        var newValue = parseFloat(value);
                    }
                    else {
                        var newValue = parseFloat(value) + step;
                    }

                    newValue = fixValue(newValue.toFixed(options.fixed));

                    textInput.val(newValue);
                    changeInputWidth(textInput, newValue);
                });
                textInput.unbind('click').click(function(e){
                    if(e.target.selectionStart !== undefined) $(e.target).attr('caretpos', e.target.selectionStart);
                });
                textInput.unbind('keypress').keypress(function(e){
                    $(e.target).attr('keypressvalue', $(e.target).val());
                    if(e.target.selectionStart !== undefined) {
                        $(e.target).attr('caretpos', e.target.selectionStart);
                    }
                    if(e.keyCode == 13) {
                        e.target.blur();
                    }
                });
                textInput.unbind('input').on('input', function(e){
                    var pattern = '[0-9]*';
                    options.allowNegative ? pattern = '^-?' + pattern : pattern = '^' + pattern;
                    options.allowFloat ? pattern += '\\.?([0-9]+)?$' : pattern += '$';

                    var regexp = new RegExp(pattern);

                    if(!regexp.test($(e.target).val())) {
                        $(e.target).val($(e.target).attr('keypressvalue'));
                        if(e.target.setSelectionRange && $(e.target).attr('caretpos') !== undefined) {
                            var caretPosition = parseInt($(e.target).attr('caretpos'));
                            e.target.setSelectionRange(caretPosition, caretPosition);
                        }
                    }

                    var val = parseFloat($(e.target).val());
                    var regexp2 = new RegExp('\\.0*$');
                    if(!isNaN(val) && !regexp2.test($(e.target).val())) {
                        val = fixValue(val);
                        $(e.target).val(val);
                        $(e.target).attr('keypressvalue', val);
                    }
                    changeInputWidth($(e.target), $(e.target).val());
                });

                textInput.unbind('change').on('change', function(e){
                    var val = parseFloat($(e.target).val());
                    if(isNaN(val)) {
                        options.obligatory ? val =  options.min : val = '';
                    }
                    $(e.target).val(val);
                    $(e.target).attr('keypressvalue', val);
                    changeInputWidth($(e.target), $(e.target).val());
                });
            });
        },
        show: function( ) {
            var elem = $(this);
            elem.show();
        },
        hide: function( ) {
            var elem = $(this);
            elem.hide();
        },
        disable : function() {
            var elem = $(this);
            elem.find('button.minus-button').attr('disabled', true);
            elem.find('button.plus-button').attr('disabled', true);
            elem.find('input[type=text]').attr('disabled', true);
        },
        enable : function() {
            var elem = $(this);
            elem.find('button.minus-button').attr('disabled', false);
            elem.find('button.plus-button').attr('disabled', false);
            elem.find('input[type=text]').attr('disabled', false);
        },
        value: function(value){
            var elem = $(this);
            var textInput = elem.find('input[type=text]');
            if(value) {
                textInput.val(parseFloat(value));
                changeInputWidth(textInput, textInput.val());
            }
            else {
                return parseFloat(textInput.val());
            }
        },
        destroy: function( ) {

            return this.each(function(){
                var elem = $(this);
                var minusButton = elem.find('button.minus-button');
                var plusButton = elem.find('button.plus-button');
                var textInput = elem.find('input[type=text]');
                minusButton.unbind('click');
                plusButton.unbind('click');
                textInput.unbind('keypress');
                textInput.unbind('click');
                textInput.unbind('change');
                textInput.unbind('input');
                elem.destroy();
            });
        }

    };

    $.fn.valamisPlusMinus = function(method) {
        if ( methods[method] ) {
            return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || !method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist in jQuery.valamisPlusMinus' );
        }
    }

})(jQuery);

// valamis infinite scroll TODO: add valamisView with infinite scroll
(function ($) {

  $.fn.valamisInfiniteScroll = function(collection, fetchFunc) {
    var that = this;

    var fetchCollection = function() {};
    if (_.isFunction(fetchFunc)) {
      fetchCollection = fetchFunc;
    }

    var elem = this.find('.js-scroll-bounded');
    var waitForResponse = false;
    elem.on('scroll', function () {
      var scrolltop = elem.scrollTop();
      var difference = elem.find('> .js-scroll-list').height() - elem.height();

      if (scrolltop >= difference * 0.9 && !waitForResponse) {
        waitForResponse = true;
        that.find('.js-loading-gif').removeClass('hidden');

        fetchCollection();
      }
    });

    collection.on('sync', function () {
      if (!collection.hasMore()) {
        elem.off('scroll');
      }

      that.find('.js-loading-gif').addClass('hidden');
      waitForResponse = false;
    });

    fetchCollection();
  }
})(jQuery);

// valamis canvas background
(function($){

  $.fn.valamisCanvasBackground = function(canvasWidth, canvasHeight) {
    var canvas = this.find('#canvas-grid');
    canvas.attr('width', canvasWidth);
    canvas.attr('height', canvasHeight);
    var context = canvas[0].getContext("2d");

    var zonesAmount = 4;

    var labelsDiv = this.find('#canvas-labels');
    context.beginPath();
    var wholePercentWidth = 100;
    var elemWidth = wholePercentWidth / 2;
    var step = wholePercentWidth / (2 * zonesAmount);

    var text, shift;

    for (var x = elemWidth, i = 0; x <= wholePercentWidth; x += step, i++) {
      if (i === zonesAmount) {
          x = wholePercentWidth;
      }

      text = (i === zonesAmount) ? wholePercentWidth : Math.floor(wholePercentWidth * i / zonesAmount);
      shift = (i === zonesAmount) ? -20 : -10;
      labelsDiv.append('<span style="left: ' + x + '%; margin-left: ' + shift + 'px;">' + text + '%</span>');
      labelsDiv.append('<span class="labels-line-dash" style="height: ' + canvas.height() + 'px; left: ' + x + '%;"></span>')
    }

    context.stroke();
  }
})(jQuery);

// valamis popup panel
(function($){
    $.fn.valamisPopupPanel = function() {
        this.each(function(){
            var elem = $(this);
            var actionButton = elem.find('.js-valamis-popup-button');
            var popupPanel = elem.find('.js-valamis-popup-panel');
            var closeButton = elem.find('.js-valamis-popup-close');

            actionButton.unbind('click').on('click', function(){
                popupPanel.toggle();
            });

            closeButton.unbind('click').on('click', function(){
                popupPanel.hide();
            });

            $('body').on('click', function(e){
                if (elem.has(e.target).length === 0) {
                    popupPanel.hide();
                }
            });
        });
    };
})(jQuery);

// valamis rating
(function ($) {

    var defaults = {
        stars: 5,
        score: 0,
        average: 0
    };

    var methods = {
        init: function (params) {

            var that = this;
            var options = $.extend({}, defaults, params);
            var ratingStarsAmount = options.stars;

            if (this.length == 0) return;

            return this.each(function () {
                var elem = $(this);
                var starHtml = '<span class="star js-rating-rate"><i class="val-icon val-icon-star"></i></span>';
                var ratingContentElem = elem.find('.js-rating-content');
                var ratingScoreElem = elem.find('.js-rating-score');
                var deleteRatingElem = elem.find('.js-rating-delete');
                var ratingAreaElem = elem.find('.js-rating-area');

                $(ratingAreaElem).html('');

                for (var i = ratingStarsAmount; i >= 1; i--) {
                    var newStar = $(starHtml);

                    newStar.attr('data-value', i);
                    newStar.click(function (e) {
                        var newValue = parseInt($(e.target).closest('.js-rating-rate',ratingAreaElem).attr('data-value'));
                        $(this).trigger('valamisRating:changed', newValue);
                    });

                    $(ratingAreaElem).append(newStar);
                }

                if (Utils.isTouchDevice()) {

                    ratingContentElem.click(function(e) {

                        if (elem.hasClass('not-rated')) {
                            elem.addClass('show-rating-area');
                            e.stopPropagation();
                        } else {
                            if ($(e.target).closest('.js-rating-score',ratingScoreElem).length) {
                                ratingScoreElem.addClass('edit-rating');
                                e.stopPropagation();
                            }
                        }
                    });

                } else {

                    elem.mouseover(function (e) {
                        if (elem.hasClass('not-rated')) {
                            elem.addClass('show-rating-area');
                        }
                    });

                    ratingScoreElem.mouseover(function (e) {
                        ratingScoreElem.addClass('edit-rating');
                    });

                }

                deleteRatingElem.click(function (e) {
                    elem.trigger('valamisRating:deleted');
                });

                ratingScoreElem.mouseleave(function (e) {
                    ratingScoreElem.removeClass('edit-rating');
                });

                elem.mouseleave(function (e) {
                    elem.removeClass('show-rating-area');
                });

            })
        },

        _updateRatingBlock: function (elem, average, score) {
            var elemAverage = elem.find('.js-rating-average');
            var elemScore = elem.find('.js-rating-current');
            var ratingContentElem = elem.find('.js-rating-content');

            elemAverage.html(Math.round(average * 10) / 10);
            elemScore.html(score);

            elem.toggleClass('no-average', (average == 0));
            elem.toggleClass('not-rated', (score == 0));
            elem.removeClass('show-rating-area');
            ratingContentElem.removeClass('edit-rating').css('display','');
        },

        score: function (average, score) {
            var rateAverage = average || 0;
            var rateScore = score || 0;

            methods._updateRatingBlock.call(this, $(this), rateAverage, rateScore);
        },

        disable: function() {
            this.each(function () {
                $(this).removeClass('not-rated');
                $(this).find('.js-rating-area').html('');
            });
        },

    };

    $.fn.valamisRating = function (method) {

        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist in jQuery.valamisRating');
        }

    }

})(jQuery);

//valamis search field
(function ($) {
  $.fn.valamisSearch = function () {
      this.each(function () {
          var elem = $(this);
          elem
              .on('click', function (e) {
                  if($(e.target).closest('input[type="text"]').length == 0){
                      $(e.target).children('input[type="text"]').val('').focus().trigger('keyup');
                      $(e.target).removeClass('cleartext');
                  }
              });
          elem.children('input[type="text"]')
              .on('focus', function(){
                  elem.addClass('focus');
              })
              .on('blur', function(){
                  elem.removeClass('focus');
              })
              .on('input', function(e){
                  if($(e.target).val() == '') {
                      elem.removeClass('cleartext');
                  }
                  else {
                      elem.addClass('cleartext');
                  }
              });

      });
  };
})(jQuery);

//valamis grid size modifier
/*
 * @param sizes The new grid column sizes object (contains up to 3 values (s, m, l) from 1 to 12)
 */
(function ($) {
    $.fn.changeSizeInGrid = function (sizes) {

        sizes || (sizes = {});
        this.each(function () {
            $(this).attr('class',
                function(i, cls) {
                    return cls.replace(/(^|\s)(([sml])-(\d{1,2}))/g, function(match, prefix, oldSizeClass, sizePrefix, size) {

                        function getNewSizeClass(p, newSize) {
                            if(Math.floor(newSize) == newSize && $.isNumeric(newSize) && newSize > 0 && newSize <= 12) {
                                return p + '-' + newSize;
                            } else {
                                console.warn('New grid size (' + p + '-' + newSize + ') has incorrect format.');
                                return sizePrefix + '-' + size;
                            }
                        }
                        var sizeClass;
                        switch (sizePrefix) {
                            case 's':
                            case 'm':
                            case 'l':
                                sizeClass = getNewSizeClass(sizePrefix, sizes[sizePrefix]);
                                break;
                            default:
                                sizeClass = sizePrefix + '-' + size;
                                break;
                        }
                        return prefix + sizeClass;
                    });
                });
        });
    };
})(jQuery);

//valamis disable submit buttons
(function ($) {
    var classes = '.js-submit-button, .js-publish-lesson, .js-action-share, ' +
        '.js-action-delete, .js-confirmation, .js-delete-comment, .js-post-my-comment, .js-post-status';

    function toggleElAvailability(e, disable) {
        var $el = $(e.target.activeElement);
        var isButton = $el.is(classes);
        if (!isButton) {
            return;
        }

        $el.prop('disabled', disable);
    };

    $(document).ajaxStart(function (e) {
        try {
            toggleElAvailability(e, true);
        } catch (ex) {
            console.log(ex);
        }
    });

    $(document).ajaxStop(function (e) {
        try {
            toggleElAvailability(e, false);
        } catch (ex) {
            console.log(ex);
        }
    });
})(jQuery);