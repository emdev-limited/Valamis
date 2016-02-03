var videoElementModule = slidesApp.module('VideoElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(VideoElementModule, slidesApp, Backbone, Marionette, $, _){

        VideoElementModule.View = this.BaseView.extend({
            template: '#videoElementTemplate',
            className: 'rj-element rj-video no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-select-google-video': 'selectGoogleVideo'
            }),
            updateUrl: function(url, oldUrl) {
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'itemContentChanged';
                slidesApp.oldValue = {
                    contentType: 'video',
                    content: oldUrl,
                    width: this.model._previousAttributes.width,
                    height: this.model._previousAttributes.height
                };
                slidesApp.newValue = {
                    contentType: 'video',
                    content: url,
                    width: this.model.get('width'),
                    height: this.model.get('height')
                };
                slidesApp.execute('action:push');
                var self = this;
                if(url) {
                    if (url.indexOf('docs.google.com/file/d/') != -1) {
                        this.$('iframe').attr('src', url);
                        this.$('iframe').show();
                        slidesApp.execute('item:resize', 640, 360);
                        this.$('.video-js').hide();
                    }
                    else if (url.indexOf('youtube.com/') != -1) {
                        var videoId = /https?:\/\/(www\.)?youtube\.com\/embed\/([^&]*)/g.exec(url)[2];
                        this.$('iframe').attr('src', 'https://www.youtube.com/embed/' + videoId + '?enablejsapi=1');
                        try {
                            this.player = new YT.Player(self.$('iframe')[0], {});
                        } catch (e) {
                            console.log(e);
                        }
                        this.$('iframe').show();
                        this.$('.video-js').hide();
                    }
                    else {
                        this.$('iframe').hide();
                        this.$('.video-js').show();
                        this.$('video').attr('src', /(.*)&ext=/g.exec(url)[1]);
                        this.$('video > source').attr('src', /(.*)&ext=/g.exec(url)[1]);
                        this.$('video > source').attr('type', (_.invert(mimeToExt.video))[/&ext=([^&]*)/g.exec(url)[1]]);
                        this.$('video').load();
                        if (navigator.sayswho[0].toLowerCase() !== 'firefox') {
                            this.$('video').on('loadeddata', function () {
                                slidesApp.execute('item:focus', self);
                                if (slidesApp.isEditorReady)
                                    slidesApp.execute('item:blur');
                                self.player = videojs(self.$('video')[0], {
                                    "controls": true,
                                    "autoplay": false,
                                    "preload": "auto"
                                }, function () {
                                    // Player (this) is initialized and ready.
                                });
                                self.player.on('loadeddata', function () {
                                    self.player.currentTime(self.player.duration() / 2);
                                    self.player.play();
                                    self.player.pause();
                                });
                            });
                        }
                    }
                    this.content.css('background-color', 'transparent');
                    this.$('.content-icon-video').first().hide();
                }
            },
            selectGoogleVideo: function() {
                this.selectEl();
                loadPicker();
            }
        });

        VideoElementModule.CreateModel = function() {
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
                layoutWidth = deviceLayoutCurrent.get('minWidth'),
                elementWidth = Math.min(layoutWidth, 640);
            var model = new VideoElementModule.Model({
                'content': '',
                'slideEntityType': 'video',
                'width': elementWidth,
                'height': Math.round(elementWidth / (16/9))
            });
            return model;
        }
    }
});

videoElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Video', label: Valamis.language['videoLabel'], slideEntityType: 'video'});
});