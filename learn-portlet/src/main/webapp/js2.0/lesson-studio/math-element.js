var mathElementModule = slidesApp.module('MathElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(MathElementModule, slidesApp, Backbone, Marionette, $, _){

        MathElementModule.View = this.BaseView.extend({
            template: '#mathElementTemplate',
            className: 'rj-element rj-math no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-item-edit-math-expression': 'openMathEditPanel',
                'click .js-close-slide-popup-panel': 'closeMathEditPanel',
                'click .js-update-math-expr': 'updateMathExpr'
            }),
            onRender: function() {
                var that = this;
                this.constructor.__super__.onRender.apply(this, arguments);
                this.content.css({
                    width: 'auto',
                    height: 'auto'
                });
                this.renderMath(this.model.get('content'));
                this.$el.find('.math-expr').bind('input', function(e) {
                    try {
                        that.renderMath(jQueryValamis(e.target).val());
                    } catch(error) {
                        that.$el.find('.math-content').html('An error occured while parsing expression.');
                    }
                });
            },
            renderMath: function(expr) {
                var that = this;
                katex.render(expr, that.$el.find('.math-content')[0], { displayMode: true });
                that.content.find('.math-content').fitTextToContainer(that.$el, true);
            },
            openMathEditPanel: function() {
                this.$('.math-edit-panel').show();
                this.$('.math-expr').val(this.model.get('content'));
                this.$('.math-expr').focus();
                this.trigger('element-modal:open');
            },
            closeMathEditPanel: function() {
                katex.render(this.model.get('content'), this.$el.find('.math-content')[0], { displayMode: true });
                this.$('.math-edit-panel').hide();
                this.selectEl();
                this.trigger('element-modal:close');
            },
            updateMathExpr: function () {
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'itemContentChanged';
                slidesApp.oldValue = {contentType: 'math', content: this.model.get('content')};
                this.model.set('content', this.$el.find('.math-expr').val());
                slidesApp.newValue = {contentType: 'math', content: this.model.get('content')};
                slidesApp.execute('action:push');
                this.closeMathEditPanel();
            }    
        });

        MathElementModule.CreateModel = function() {
            var model = new MathElementModule.Model( {
                'content': 'e^{x-n}',
                'slideEntityType': 'math'
            });
            return model;
        }
    }
});

mathElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Math', label: Valamis.language['mathLabel'], slideEntityType: 'math'});
});