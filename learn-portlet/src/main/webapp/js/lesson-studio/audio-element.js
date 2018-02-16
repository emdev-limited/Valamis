var audioElementModule = slidesApp.module('AudioElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function (AudioElementModule, slidesApp, Backbone, Marionette, $, _) {

        AudioElementModule.View = this.BaseView.extend({
            template: '#audioElementTemplate',
            className: 'rj-element rj-audio no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-select-google-audio': 'selectGoogleAudio'
            }),

            onContentFileNameChange: function() {
                var contentFileName = this.model.get('contentFileName');
                this.model.set({ content: contentFileName });
                this.updateUrl(contentFileName);
            },

            selectGoogleAudio: function() {
                slidesApp.fileTypeGroup = null;
                this.selectEl();
                loadPicker();
            },

            updateUrl: function(url) {
                if(url) {
                    if (url.indexOf('docs.google.com') != -1) {
                        this.$('audio').attr('src', url);
                    }
                    else {
                        var src = (url.indexOf('/') == -1 && url.indexOf('blob:') == -1)
                            ? slidesApp.getFileUrl(this.model, this.model.get('content'))
                            : url;
                        this.$('audio').attr('src', src);
                    }
                }
                this.content.css('background-color', 'transparent');
            }
        });

        AudioElementModule.CreateModel = function() {
            var height = 36;
            var width = 300;
            var slidesEl = $(lessonStudio.slidesWrapper + ' .slides');
            var leftIndent = Math.max((slidesEl.width() - width) / 2, 0);
            var topIndent = Math.max((slidesEl.height() - height) / 2, 0);
            return new AudioElementModule.Model({
                'content': '',
                'slideEntityType': 'audio',
                'width': width,
                'height': height,
                'top': topIndent,
                'left': leftIndent
            });
        };

    }
});

audioElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Audio', label: Valamis.language['audioLabel'], slideEntityType: 'audio'});
});