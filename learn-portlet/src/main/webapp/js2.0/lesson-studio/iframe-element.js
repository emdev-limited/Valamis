var iframeElementModule = slidesApp.module('IframeElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(IframeElementModule, slidesApp, Backbone, Marionette, $, _){

        IframeElementModule.View = this.BaseView.extend({
            template: '#iframeElementTemplate',
            className: 'rj-element rj-iframe no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-item-open-settings': 'openItemSettings',
                'click .js-item-close-settings': 'closeItemSettings',
                'click .js-update-iframe-url': function() { this.updateUrl(this.$('.iframe-url').val()); }
            }),
            onRender: function() {
                this.$('.js-upload-pdf').parent().hide();
                this.$('.content-icon-pdf').hide();
                this.constructor.__super__.onRender.apply(this, arguments);
            },
            updateUrl: function(url) {
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'itemContentChanged';
                slidesApp.oldValue = {contentType: 'url', content: this.model.get('content')};
                this.model.set('content', url || this.$('.iframe-url').val());
                slidesApp.newValue = {contentType: 'url', content: this.model.get('content')};
                slidesApp.execute('action:push');
                this.$('iframe').attr('src', url || this.$('.iframe-url').val());
                this.closeItemSettings();
                this.$('.content-icon-iframe').first().hide();
                this.$('.iframe-item').show();
            },
            openItemSettings: function() {
                this.$('.item-settings').show();
                this.$('.iframe-url').focus();
                slidesApp.isEditing = true;
            },
            closeItemSettings: function() {
                this.$('.item-settings').hide();
                slidesApp.isEditing = false;
                this.selectEl();
            }
        });

        IframeElementModule.CreateModel = function() {
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
                layoutWidth = deviceLayoutCurrent.get('minWidth'),
                elementWidth = Math.min(layoutWidth, 640);
            var model = new IframeElementModule.Model( {
                'content': '',
                'slideEntityType': 'iframe',
                'width': elementWidth,
                'height': Math.round(elementWidth / (16/9))
            });
            return model;
        }
    }
});

iframeElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Iframe', label: Valamis.language['embedLabel'], slideEntityType: 'iframe'});
});