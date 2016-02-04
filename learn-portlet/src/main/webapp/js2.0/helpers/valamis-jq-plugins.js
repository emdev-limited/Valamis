/**
 * Created by igorborisov on 02.04.15.
 */

// valamis dropdown
(function($) {

    var methods = {
        init : function( options ) {
            this.each(function(){
                var elem = $(this);

                var actionButton = elem.find(".button");
                var dropdownMenu = elem.find('.dropdown-menu');
                actionButton.unbind('click').on('click', function(){
                    dropdownMenu.toggleClass("dropdown-visible");
                });

                var dropdownItems = dropdownMenu.find("li");

                dropdownItems.each(function(ind, itm){
                        var item =$(itm);
                        item.unbind('click').click(function() {
                                if(!elem.hasClass('actions')){
                                    item.addClass('selected').siblings().removeClass('selected');
                                    item.parents('.dropdown')
                                        .data('value', item.data('value'))
                                        .find('.dropdown-text').html(item.html());
                                }

                                dropdownMenu.removeClass('dropdown-visible');
                            }
                        );
                    }
                );

                $('body').on('click', function(e){
                    if (elem.has(e.target).length === 0) {
                        dropdownMenu.removeClass('dropdown-visible');
                    }
                });
            });

            return this;
        },
        select: function(options){
            if(this.hasClass('actions')) return;

            var item = this.find('.dropdown-menu > li[data-value='+ options +']');

            item.addClass('selected').siblings().removeClass('selected');
            item.parents('.dropdown')
                .data('value', item.data('value'))
                .find('.dropdown-text').html(item.html());
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
                elem.parents('.portlet-wrapper').toggleClass('sidebar-hidden');
            });
        });
    };
})(jQuery);

//digits only
(function($){
    // methods
    var methods = {
        init : function(params) {

            return this.each(function() {
                var elem = $(this);

                //TODO add '-' ability
                elem.unbind('keypress').keypress(function(e) {
                    var code = e.keyCode ? e.keyCode : e.charCode;
                    if (($(this).val().indexOf('.') > 0) && e.charCode == 46) {
                        return false
                    }

                    var allowed = [46, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 8, 9, 39, 37, 190];
                    if (allowed.indexOf(code) < 0) {
                        return false;
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
        'step': '1'
    };


// methods
    var methods = {
        init : function(params) {

            var getFixed = function(step){
                var afterComma = (''+ step).split('.')[1];
                return afterComma? afterComma.length : 0;
            };

            var changeInputWidth = function(textInput, value){
                var amount = ('' + value).length;
                var width = 14 + 8 * (amount);
                if(width < 34) width = 34;
                textInput.css('width', width + 'px');
            };

            var fixValue = function(value){
                var minValue = parseFloat(options.min);
                var maxValue = parseFloat(options.max);
                var step = parseFloat(options.step);

                if(value > maxValue - step) value = maxValue;
                if(value< minValue + step) value = minValue;
                return value;
            };

            var options = $.extend({}, defaults, params);
            options.fixed = getFixed(options.step);

            var html = '<button class="button medium neutral no-text minus-button">'
                + '<span class="val-icon-minus"></span>'
                + '</button>'
                    //+ '<span>'
                + '<input type="text" class="text-input valamis box-sizing" value="0"/>'
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
                    var value = parseFloat(textInput.val() || 0).toFixed(options.fixed);
                    var newValue = parseFloat(value) + step;

                    newValue = fixValue(newValue.toFixed(options.fixed));

                    textInput.val(newValue);
                    changeInputWidth(textInput, newValue);
                });

                textInput.unbind('keypress').keypress(function(e){
                    var code = e.keyCode ? e.keyCode : e.charCode;
                    if(($(this).val().indexOf('.') > 0) && e.charCode == 46){
                        return false
                    }

                    var allowed = [46, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 8, 9, 39, 37, 190];
                    if (allowed.indexOf(code) < 0) {
                        return false;
                    }

                });

                textInput.unbind('keyup').keyup(function(e){
                    setTimeout(function(){
                        textInput.val(fixValue(textInput.val()));
                        changeInputWidth(textInput, textInput.val());
                    }, 300);

                });

            });
        },
        show : function( ) {
            var elem = $(this);
            elem.show();
        },
        hide : function( ) {
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
            if(value){
                textInput.val(parseFloat(value));
            }else{
                return parseFloat(textInput.val());
            }
        },
        destroy : function( ) {

            return this.each(function(){
                var elem = $(this);
                var minusButton = elem.find('button.minus-button');
                var plusButton = elem.find('button.plus-button');
                var textInput = elem.find('input[type=text]');
                minusButton.unbind('click');
                plusButton.unbind('click');
                textInput.unbind('keypress');
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
(function($){
    var fetchCollection = function(collection, options) {
      collection.fetch(options);
    };

  $.fn.valamisInfiniteScroll = function(collection, options) {
    var page = 1;
    var that = this;
    var itemsCount = options.count;

    var elem = this.find('.js-scroll-bounded');
    var waitForResponse = false;
    elem.on('scroll', function () {
      var scrolltop =   elem.scrollTop();
      var difference = elem.find('> .js-scroll-list').height() - elem.height();

      if (scrolltop >= difference * 0.9 && !waitForResponse) {
        waitForResponse = true;
        that.find('.js-loading-gif').removeClass('hidden');
        page++;

        fetchCollection(collection, _.extend(options, {page: page}));
      }
    });

    collection.on('sync', function() {
      if (collection.length < itemsCount) {
        elem.off('scroll');
      }

      that.find('.js-loading-gif').addClass('hidden');
      waitForResponse = false;
    });

    fetchCollection(collection, _.extend(options, {page: page}));
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
    context.setLineDash([5, 5]);
    context.beginPath();
    var elemWidth = Math.floor(canvas.width() / 2);
    var step = Math.floor(canvas.width() / (2 * zonesAmount));

    var text, shift;

    for (var x = elemWidth, i = 0; x <= canvas.width(); x += step, i++) {
      if (i === zonesAmount)
        x = canvas.width();

      context.moveTo(x, 0);
      context.lineTo(x, canvas.height());

      text = (i === zonesAmount) ? 100 : Math.floor(100 * i / zonesAmount);
      shift = (i === zonesAmount) ? 20 : 10;
      labelsDiv.append('<span style="left: ' + (x - shift) + 'px;" >' + text + '%</span>');
    }

    context.strokeStyle = "#DEDEDE";
    context.lineWidth = 1;
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

// methods
  var methods = {
    init: function (params) {

      var ratingScore, ratingStarsAmount, ratingAverage;

      var options = $.extend({}, defaults, params);
      ratingStarsAmount = options.stars;
      ratingScore = parseInt(options.score);
      ratingAverage = methods._round(options.average);

      var that = this;

      return this.each(function () {
        var starHtml = '<span class="val-icon rating-star default js-rating-star"></span>';
        var elem = $(this);

        elem.html('');

        for (var i = 1; i <= ratingStarsAmount; i++) {
          var newStar = $(starHtml);
          newStar.attr('data-value', i);

          newStar.unbind('mouseover').mouseover(function (e) {
            elem.addClass('hovered');
            methods._fillStars.call(that, elem, $(e.target).attr('data-value'));
          });

          newStar.unbind('mouseleave').mouseleave(function (e) {
            elem.removeClass('hovered');
            methods._fillStars.call(that, elem, elem.attr('data-value'));
          });

          newStar.unbind('click').click(function (e) {
            elem.removeClass('hovered');
            var newValue = parseInt($(e.target).attr('data-value'));
            var oldValue = parseInt(elem.attr('data-value'));
            if (elem.hasClass('voted') && newValue === oldValue) {
              elem.removeClass('voted');
              elem.trigger('valamisRating:deleted');
            } else {
              elem.addClass('voted');
              methods._setScore.call(that, elem, newValue);
              elem.trigger('valamisRating:changed', newValue);
            }

          });

          elem.append(newStar);
        }

        methods._setScore.call(this, elem, ratingScore || ratingAverage);
        if (ratingScore > 0) elem.addClass('voted');
      })
    },

    _round: function(number) {
      return Math.ceil(parseFloat(number) * 100) / 100;
    },

    _fillStars: function (elem, score) {
      var elemStars = elem.find('.js-rating-star');
      var scoreRound = Math.round(score);

      for (var i = 0; i < elemStars.length; i++) {
        $(elemStars[i]).toggleClass('default', i >= scoreRound);
        $(elemStars[i]).removeClass('half-star');
      }

      var isHalf = (score - scoreRound) > 0;
      if (isHalf) $(elemStars[scoreRound]).addClass('half-star');
    },

    _setScore: function (elem, score) {
      elem.attr('data-value', score);
      methods._fillStars.call(this, elem, score);
    },

    score: function (score) {
      if (score != undefined)
        methods._setScore.call(this, $(this), parseInt(score));
      else
        return parseInt($(this).attr('data-value'));
    },

    average: function (average) {
      if (average != undefined)
        methods._setScore.call(this, $(this), methods._round(average));
    },

    destroy: function () {
      return this.each(function () {
        var elem = $(this);
        elem.find('.js-rating-star')
          .unbind('mouseover')
          .unbind('mouseleave')
          .unbind('click');
        elem.destroy();
      });
    }

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
        .on('focus', function () {
          elem.parent('.val-search').addClass('focus');
        })
        .on('blur', function () {
          elem.parent('.val-search').removeClass('focus');
        });
    });
  };
})(jQuery);