var iframeElementModule = slidesApp.module('IframeElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(IframeElementModule, slidesApp, Backbone, Marionette, $, _){

        IframeElementModule.View = this.BaseView.extend({
            template: '#iframeElementTemplate',
            className: 'rj-element rj-iframe no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-update-iframe-url': function() { this.updateUrl(this.$('.iframe-url').val()); }
            }),
            onRender: function() {
                this.$('.js-upload-pdf').parent().hide();
                this.$('.content-icon-pdf').hide();
                this.constructor.__super__.onRender.apply(this, arguments);
            },
            updateUrl: function(url) {
                var that = this;

                this.$('.warning').hide();

                this.model.set('content', url || this.$('.iframe-url').val());
                this.$('iframe').attr('src', url || this.$('.iframe-url').val());
                this.closeItemSettings();
                this.$('.content-icon-iframe').first().hide();
                this.$('.iframe-item').show();

                jQueryValamis.ajax(path.root + path.api.urlCheck, {
                    method: 'POST',
                    headers: { 'X-CSRF-Token': Liferay.authToken },
                    data: {
                        url: this.model.get('content'),
                        courseId: Liferay.ThemeDisplay.getScopeGroupId()
                    },
                    complete: function (data) {
                        if(data.responseText === 'false')
                            that.$('.warning').show()
                    }
                });
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