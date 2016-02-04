var PdfElementModule = slidesApp.module('PdfElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(PdfElementModule, slidesApp, Backbone, Marionette, $, _){

        PdfElementModule.View = this.BaseView.extend({
            template: '#iframeElementTemplate',
            className: 'rj-element rj-iframe no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-upload-pdf': 'uploadPdf'
            }),
            templateHelpers: function() {
                return {
                    content: slidesApp.getFileUrl(this.model, this.model.get('content')),
                    noSettings: true
                }
            },
            onRender: function() {
                this.$('.js-edit-iframe-url').parent().hide();
                this.$('.content-icon-iframe').hide();
                this.constructor.__super__.onRender.apply(this, arguments);
            },
            updateUrl: function(filename) {
                var src = slidesApp.getFileUrl(this.model, filename);
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'itemContentChanged';
                slidesApp.oldValue = {contentType: 'url', content: this.model.get('content')};
                this.model.set('content', filename);
                slidesApp.newValue = {contentType: 'url', content: this.model.get('content')};
                slidesApp.execute('action:push');
                this.$('iframe').attr('src', src);
                this.content.css('background-color', 'transparent');
                this.$('.content-icon-pdf').first().hide();
                this.$('.iframe-item').show();
            }
        });

        PdfElementModule.CreateModel = function() {
            var model = new PdfElementModule.Model( {
                'content': '',
                'slideEntityType': 'pdf',
                'width': 640,
                'height': 360
            });
            return model;
        }
    }
});

PdfElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'PDF', label: 'PDF', slideEntityType: 'pdf'});
});