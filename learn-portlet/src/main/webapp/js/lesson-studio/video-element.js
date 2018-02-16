var videoElementModule = slidesApp.module('VideoElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(VideoElementModule, slidesApp, Backbone, Marionette, $, _){

        VideoElementModule.View = this.BaseView.extend({
            template: '#videoElementTemplate',
            className: 'rj-element rj-video no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-select-google-video': 'selectGoogleVideo'
            }),
            onContentFileNameChange: function() {
                var contentFileName = this.model.get('contentFileName').replace(/watch\?v=/g, 'embed/');
                var oldContent = this.model.get('content');
                this.model.set({ content: contentFileName });
                this.updateUrl(contentFileName, oldContent);
            },
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
                var that = this;

                this.$('.warning').hide();
                if(url) {
                    if (url.indexOf('docs.google.com/file/d/') != -1) {
                        this.$('iframe').attr('src', url);
                        this.$('iframe').show();
                        slidesApp.execute('item:resize', this.model.get('width') || 640, this.model.get('height') || 360, this);
                        this.$('.video-js').hide();

                        jQueryValamis.ajax(path.root + path.api.urlCheck, {
                            method: 'POST',
                            headers: { 'X-CSRF-Token': Liferay.authToken },
                            data: {
                                url: this.model.get('content'),
                                courseId: Liferay.ThemeDisplay.getScopeGroupId()
                            },
                            complete: function (data) {
                                if(data.responseText === 'false') {
                                    that.$('.js-not-allow').show();
                                }
                            }
                        });
                    }
                    else if (url.indexOf('youtube.com/') != -1) {
                        var videoId = /https?:\/\/(www\.)?youtube\.com\/embed\/([^&]*)/g.exec(url)[2];
                        this.$('iframe').attr('src', 'https://www.youtube.com/embed/' + videoId + '?enablejsapi=1');
                        try {
                            this.player = new YT.Player(that.$('iframe')[0], {});
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
                        this.$('video > source').attr('type', (_.invert(Utils.mimeToExt.video))[/&ext=([^&]*)/g.exec(url)[1]]);
                        this.$('video').load();
                        if (navigator.sayswho[0].toLowerCase() !== 'firefox') {
                            this.$('video').on('loadeddata', function () {
                                slidesApp.execute('item:focus', that);
                                if (slidesApp.isEditorReady)
                                    slidesApp.execute('item:blur');
                                that.player = videojs(that.$('video')[0], {
                                    "controls": true,
                                    "autoplay": false,
                                    "preload": "auto"
                                }, function () {
                                    // Player (this) is initialized and ready.
                                });
                                that.player.on('loadeddata', function () {
                                    that.player.currentTime(that.player.duration() / 2);
                                    that.player.play();
                                    that.player.pause();
                                });
                            });
                        }
                    }
                    this.content.css('background-color', 'transparent');
                    this.$('.content-icon-video').first().hide();
                }
            },
            selectGoogleVideo: function() {
                slidesApp.fileTypeGroup = null;
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