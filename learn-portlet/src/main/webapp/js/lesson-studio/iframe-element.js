var LTI_PREFIX = 'LTI:';
var iframeElementModule = slidesApp.module('IframeElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(IframeElementModule, slidesApp, Backbone, Marionette, $, _){

        IframeElementModule.View = this.BaseView.extend({
            template: '#iframeElementTemplate',
            className: 'rj-element rj-iframe no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-update-iframe-url': function() { this.updateUrl(this.$('.iframe-url').val(),this); },
                'click .js-item-thirdparty': function() { this.openThirdPartySelector()}
            }),
            initialize: function () {
              this.guid = generateUUID();
              this.constructor.__super__.initialize.apply(this, arguments);
            },
            onRender: function() {
                this.$('.js-upload-pdf').parent().hide();
                this.$('.js-content-icon-pdf').hide();

                this.constructor.__super__.onRender.apply(this, arguments);
                var that = this;
                this.$('iframe').on('load',function() {
                    that.$('iframe').off('load');
                    if (that.model.get('content').indexOf(LTI_PREFIX) == 0) {
                        var contentUrl = that.model.get('content').replace(LTI_PREFIX, '');
                        var provider = iframeElementModule.thirdPartyCollection.find(function (elem) {
                            return elem.get('url') == contentUrl;
                        });
                        if (provider) {
                            that.loadIFrame(provider);
                        } else {
                            that.$('.js-no-lti').show();
                        }
                    }
                });
            },
            updateUrl: function(url, iframeElement) {
                iframeElement.$('.iframe-url').val(url);
                iframeElement.$('.warning').hide();

                iframeElement.model.set('content', url || iframeElement.$('.iframe-url').val());
                iframeElement.$('iframe').attr('src', url || iframeElement.$('.iframe-url').val());
                iframeElement.closeItemSettings();
                iframeElement.$('.content-icon-iframe').first().hide();
                iframeElement.$('.iframe-item').show();

                $.ajax(path.root + path.api.urlCheck, {
                    method: 'POST',
                    headers: { 'X-CSRF-Token': Liferay.authToken },
                    data: {
                        url: iframeElement.model.get('content'),
                        courseId: Utils.getCourseId()
                    },
                    complete: function (data) {
                        if(data.responseText === 'false')
                            iframeElement.$('.warning').show()
                    }
                });
            },
            updateContentProvider: function(url, iframeElement) {
                iframeElement.$('.iframe-url').val('');
                iframeElement.$('.warning').hide();

                iframeElement.model.set('content', LTI_PREFIX + url);
                iframeElement.$('.content-icon-iframe').first().hide();
                iframeElement.$('.iframe-item').show();

            },

            updateSize: function(width, height, iframeElement) {

                if(this.undefinedSize(width) || this.undefinedSize(height)){return;}

                var heightInt = parseInt(height);
                iframeElement.model.set('height', heightInt);
                iframeElement.$('iframe').attr('height', heightInt);

                var widthInt = parseInt(width);
                iframeElement.model.set('width', widthInt);
                iframeElement.$('iframe').attr('width', widthInt);

                var properties = iframeElement.model.copyProperties();
                var deviceLayoutCurrentId = slidesApp.devicesCollection.getCurrentId();

                properties[deviceLayoutCurrentId].height = heightInt;
                properties[deviceLayoutCurrentId].width = widthInt;

                iframeElement.model.set('properties', properties);
            },

            undefinedSize : function(size) {
                return size === null || size.length == 0 || size == "undefined" || size == "auto";
            },
            templateHelpers : function() {
                var contentVal = this.model.get('content');
                if(contentVal.indexOf(LTI_PREFIX) == 0)
                    contentVal = '';
                return {
                    contentVal: contentVal,
                    guid: this.guid
                };
            },

            createForm : function(id, target, actionUrl, parameters) {
                $(".tempForm").remove();
                var form = $(
                    '<form class="tempForm" style="display:none;"' +
                        'id="' + id + '"' +
                        'target="' + target + '"' +
                        'action="' + actionUrl + '"' +
                        'method="POST"' +
                    '></form>');
                for(var key in parameters) {
                    form.append('<input type="text" name="' + key + '" value="'+parameters[key]+'">');
                }
                form.append('<input type="submit">');
                this.$('.js-third-party-frame-area').append(form);
                return form;
            },

            loadIFrame : function(model) {

                var targetUrl = model.get('url');
                var customerKey = model.get('customerKey');
                var customerSecret = model.get('customerSecret');

                var nonce = (new Date()).getTime();

                var parameters = {
                    oauth_consumer_key : customerKey,
                    oauth_signature_method : lessonStudio.ltiOauthSignatureMethod,
                    oauth_version :lessonStudio.ltiOauthVersion,
                    lti_message_type : lessonStudio.ltiMessageType,
                    lti_version : lessonStudio.ltiVersion,
                    resource_link_id : "valamis-resource-link",
                    launch_presentation_return_url : lessonStudio.ltiLaunchPresentationReturnUrl,
                    selection_directive :'yes please',
                    oauth_nonce: nonce,
                    oauth_timestamp: Math.round((new Date()).getTime() / 1000.0),
                    user_id: Utils.getUserId(),
                    roles: "Instructor",
                    oauth_callback: "about:blank"
                };

                parameters['oauth_signature'] = decodeURIComponent(oauthSignature.generate('POST', targetUrl, parameters,  customerSecret));

                //We launch a POST request to the iframe by creating a hidden form.
                this.createForm("valamis-resource-link", "iframe-" + this.guid, targetUrl, parameters).submit();
            },

            openThirdPartySelector: function() {
                var w_settings = this;

                var thirdPartySelectorView = Marionette.CompositeView.extend({
                    template: '#iframeThirdPartySelector',
                    childView: IframeElementModule.ThirdPartySelectorItemView,
                    childViewContainer: '.js-third-party-list',
                    childEvents: {
                        'thirdPartyList:selected': function (childView, model) {
                            w_settings.loadIFrame(model, w_settings);

                            w_settings.updateContentProvider(model.get('url'), w_settings);
                            valamisApp.execute('modal:close', view);
                        }
                    },

                    onRender: function () {
                        this.$('.js-third-party-frame-area').hide();
                        this.$('.js-third-party-list').show();
                    }

                });

                var view = new valamisApp.Views.ModalView({
                    contentView: new thirdPartySelectorView({
                        collection: iframeElementModule.thirdPartyCollection
                    }),
                    className: 'lesson-studio-modal light-val-modal',
                    header: Valamis.language.valThirdPartySelectionLabel
                });
                valamisApp.execute('modal:show', view);
            }
        });

        IframeElementModule.ThirdPartySelectorItemView = Marionette.ItemView.extend({
            template: '#ThirdPartySelectorItem',
            className: 'item',
            events: {
                'click .js-select-provider-item': 'selectItem'
            },
            templateHelpers: function() {
                return {
                    name: this.model.get('name')
                }
            },
            selectItem: function(e){
                this.triggerMethod('thirdPartyList:selected', this.model);
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

iframeElementModule.thirdPartyCollection = new lessonStudioCollections.ThirdPartySelectorCollection;
iframeElementModule.thirdPartyCollection.fetch();

iframeElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Iframe', label: Valamis.language['embedLabel'], slideEntityType: 'iframe'});
});