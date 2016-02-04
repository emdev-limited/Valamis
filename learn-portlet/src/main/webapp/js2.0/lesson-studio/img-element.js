var imageElementModule = slidesApp.module('ImageElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function (ImageElementModule, slidesApp, Backbone, Marionette, $, _) {

        ImageElementModule.View = this.BaseView.extend({
            template: '#imageElementTemplate',
            className: 'rj-element rj-image no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-select-google-image': 'selectGoogleImage'
            }),
            updateUrl: function(url, oldUrl, width, height) {
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'itemContentChanged';
                slidesApp.oldValue = {
                    contentType: 'image',
                    content: oldUrl,
                    width: this.model.get('width'),
                    height: this.model.get('height')
                };
                if(url) {
                    if (url.indexOf('docs.google.com/file/d/') != -1) {
                        this.content.css('background-color', 'transparent');
                        this.$('.content-icon-image').first().hide();
                        this.$('iframe').attr('src', url);
                        this.$('iframe').show();
                        slidesApp.execute('item:resize', 640, 480);
                    }
                    else {
                        this.$('iframe').attr('src', '');
                        this.$('iframe').hide();
                        var src = (url.indexOf('/') == -1 && url.indexOf('blob:') == -1)
                            ? slidesApp.getFileUrl(this.model, this.model.get('content'))
                            : url;

                        var image = new Image();
                        image.onload = function () {
                            var newSize = ImageElementModule.resizeImage(width || image.width, height || image.height, 800, 800);
                            slidesApp.execute('item:resize', newSize.width, newSize.height);
                        };
                        image.src = src;

                        this.content.css('background-image', 'url("' + src + '")');
                        this.content.css('background-color', 'transparent');
                        this.$('.content-icon-image').first().hide();
                    }
                }
                slidesApp.newValue = {
                    contentType: 'image',
                    content: this.model.get('content'),
                    width: this.model.get('width'),
                    height: this.model.get('height')
                };
                slidesApp.execute('action:push');
            },
            selectGoogleImage: function() {
                this.selectEl();
                loadPicker();
            }
        });

        ImageElementModule.CreateModel = function() {
            var model = new ImageElementModule.Model( {
                'content': '',
                'slideEntityType': 'image',
                'width': 200,
                'height': 200
            });
            return model;
        };

        ImageElementModule.resizeImage = function(width, height, maxWidth, maxHeight) {
            var ratio = width/height;
            var newWidth, newHeight;
            if(ratio > 1) {
                if (width > maxWidth) {
                    newWidth = maxWidth;
                    newHeight = newWidth / ratio;
                }
                else {
                    newWidth = width;
                    newHeight = height;
                }
            }
            else {
                if(height > maxHeight) {
                    newHeight = maxHeight;
                    newWidth = newHeight * ratio;
                }
                else {
                    newWidth = width;
                    newHeight = height;
                }
            }
            return {width: newWidth, height: newHeight};
        };
    }
});

imageElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Image', label: Valamis.language['imageLabel'], slideEntityType: 'image'});
});