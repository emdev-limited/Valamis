/**
 * Created by igorborisov on 16.04.15.
 */
valamisApp.module("Views", function (Views, valamisApp, Backbone, Marionette, $, _) {

    Views.ModalView = Backbone.Modal.extend({
        template: '#valamisAppModalTemplate',
        className: 'val-modal',
        submitEl: '.js-submit-button',
        cancelEl: '.modal-close',
        clickOutside: function(){void 0},
        initialize: function(options){
            this.header = options.header;
            this.contentView = options.contentView;
            this.customClassName = options.customClassName;
            if(options.template) this.template = options.template;
            if(options.submit) this.submit = options.submit;
            if(options.beforeSubmit) this.beforeSubmit = options.beforeSubmit;
            if(options.beforeCancel) this.beforeCancel = options.beforeCancel;
            if(options.onDestroy) this.onDestroy = options.onDestroy;
            var mustacheAccumulator = {};
            if (this.model) {
                _.extend(mustacheAccumulator, this.model.toJSON());
            }
            _.extend(mustacheAccumulator,  Valamis.language);
            _.extend(mustacheAccumulator, { header : this.header });
            this.template = _.template(Mustache.to_html($(this.template).html(), mustacheAccumulator));
        },
        onRender: function(){
            if (this.customClassName)
                this.$el.addClass(this.customClassName);
            this.$('.modal-content').html(this.contentView.render().el);
        },
        onShow: function(){
            if(_.isFunction(this.contentView.onShow)) {
                this.contentView.onShow();
            }
        }
    });

    Views.ArrowModalView = Backbone.Modal.extend({ //Modal with arrow and position near event target
        template: '#valamisAppArrowModalTemplate',
        className: 'arrow-modal',
        submitEl: '.js-submit-button',
        cancelEl: '.modal-close',

        initialize: function (options) {
            this.contentView = options.contentView;
            this.commonClassName = options.commonClassName;

            //Target options
            this.target = options.target;
            this.targetWidth = (options.targetWidth) ? options.targetWidth : this.target.outerWidth();
            this.targetHeight = (options.targetHeight) ? options.targetHeight : this.target.outerHeight();

            var mustacheAccumulator = {};
            if (this.model)
            {
                _.extend(mustacheAccumulator, this.model.toJSON());
            }
            _.extend(mustacheAccumulator, Valamis.language);
            this.template = _.template(Mustache.to_html($(this.template).html(), mustacheAccumulator));
        },
        onRender: function () {
            if (this.commonClassName)
                this.$el.addClass(this.commonClassName);
            this.$('.modal-content').html(this.contentView.render().el);
            this.foundPosition = false;
        },

        onShow: function () {
            if (_.isFunction(this.contentView.onShow)) {
                this.contentView.onShow();
            }
            if (!this.foundPosition)
                this.findPosition(); //In onRender width is wrong
        },

        //move to position of clicked object
        findPosition: function () {
            var position = this.setPosition();
            this.foundPosition = true;
            if (!position)
                return;

            var modal = this.$el.find('.bbm-modal').first();
            modal.offset(position);
            modal.addClass(position.isRight ? 'left-arrow' : 'right-arrow');
        },

        //set position from position of clicked obj
        setPosition: function () {
            if (!this.target)
                return null;

            var arrowSize = 10;
            var modal = this.$el.find('.bbm-modal').first();

            var position = this.target.offset();

            position.top -= arrowSize;
            position.isRight = false;
            if (position.left - modal.outerWidth() < 0) {   //If screen ends, display it to the right
                position.left += arrowSize + this.targetWidth;
                position.isRight = true;
            }
            else
                position.left -= (arrowSize * 1.5 + modal.outerWidth());

            return position;
        }
    });
});