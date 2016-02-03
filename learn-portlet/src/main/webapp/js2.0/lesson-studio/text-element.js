var textElementModule = slidesApp.module('TextElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function(TextElementModule, slidesApp, Backbone, Marionette, $, _){

        TextElementModule.View = this.BaseView.extend({
            template: '#textElementTemplate',
            className: 'rj-element rj-text no-select',
            events: _.extend({}, this.BaseView.prototype.events, {
                'dblclick': 'initEditor',
                'blur': 'destroyEditor',
                'click  .js-item-select-liferay-article': 'selectLiferayArticle'
            }),
            updateEl: function() {
                this.constructor.__super__.updateEl.apply(this, arguments);
                if (this.editor) this.destroyEditor();
                else this.content.html(this.model.get('content'));
            },
            onRender: function() {
                var self = this;
                this.constructor.__super__.onRender.apply(this, arguments);
                this.model.on('sync', function () {
                    self.destroyEditor();
                });
            },
            initEditor: function() {
                this.undelegateEvents();
                this.content[0].contentEditable = true;
                var view = this;
                var model = view.model;
                this.editorOptions = {};
                this.editor = CKEDITOR.inline(this.content[0], {
                    extraPlugins: 'contextmenu,table,tabletools,lineheight',
                    enterMode: CKEDITOR.ENTER_BR,
                    forcePasteAsPlainText: true,
                    fontSize_sizes: '9/0.563em;10/0.625em;11/0.688em;12/0.750em;14/0.875em;16/1em;'
                        + '18/1.125em;20/1.25em;22/1.375em;24/1.5em;28/1.75em;36/2.25em;48/3em;72/4.5em;',
                    on:{
                        focus: function(){
                            if( this.getData() == Valamis.language['newTextElementLabel'] ){
                                this.setData('');
                            }
                            view.wrapperUpdate( false );
                        },
                        blur: function(event){
                            view.destroyEditor();
                        },
                        change: function (event) {
                            view.wrapperUpdate( false );
                            if(slidesApp.isSaved)
                                slidesApp.toggleSavedState();
                        },
                        instanceReady: function(event){
                            var editor = event.editor,
                                toolbarStyles = _.find(editor.toolbar, function(item){
                                    return item.name == 'styles';
                                }),
                                toolFontSize = _.find(toolbarStyles.items, function(item){
                                    return item.name == 'fontsize';
                                });
                            toolFontSize.applyChanges = toolFontSize.onClick;
                            toolFontSize.onClick = function(value){
                                var selection = editor.getSelection(true),
                                    contentSelectedText = selection.getSelectedText().replace(/\n(\s*)/g, ''),
                                    contentText = editor.element.getText(),
                                    isReset = view.editorOptions.fontSize
                                        && contentText.length == contentSelectedText.length
                                        && view.editorOptions.fontSize.replace(/\D/g, '') == value ? true : false;
                                if( contentText.length == contentSelectedText.length ){
                                    view.content.css('font-size', value+'px');
                                    view.editorOptions.fontSize = value+'px';
                                    //need to set fontSize forcibly when the focus isn't removed from editor
                                    model.set('fontSize', view.editorOptions.fontSize);
                                    //TODO: think about logic
                                    //if(isReset) view.removeStyle(isReset, 'font-size');
                                } else {
                                    this.applyChanges(value);
                                }
                            };
                        }
                    }
                });
                this.content[0].focus();
                this.$el.removeClass('no-select');
                slidesApp.isEditing = true;
                slidesApp.oldValue = {contentType: 'text', content: this.model.get('content')};
            },
            /** Remove elements style, except the root element */
            removeStyle: function( isReset, styleName ){
                if(!slidesApp.isEditing) return;
                var editor = this.editor,
                    selection = editor.getSelection(true),
                    contentSelectedText = selection.getSelectedText().replace(/\n(\s*)/g, ''),
                    path = editor.elementPath();
                styleName = styleName || 'font-size';
                if( path.elements.length > 0 ){
                    for(var i=0; i < path.elements.length - 1; i++){
                        if(isReset || (!isReset && path.elements[i].getText().length == contentSelectedText.length)){
                            if( i == 0 ){
                                var el = path.elements[i].$.nextSibling;
                                while (el) {
                                    var element = new CKEDITOR.dom.element(el);
                                    if( element.getName() != '#text' ){
                                        element.removeStyle(styleName);
                                    }
                                    el = el.nextSibling;
                                }
                            }
                            path.elements[i].removeStyle(styleName);
                        }
                    }
                }
            },
            destroyEditor: function() {
                if( !this.editor ){ return; }
                if(this.editor && this.editor.focusManager.hasFocus) {
                    slidesApp.execute('item:focus', this);
                }
                var data = {
                    left: this.$el.position().left,
                    top: this.$el.position().top,
                    width: this.$el.width(),
                    height: this.$el.height()
                };
                if(this.editor) {
                    data.content = this.editor.getData();
                    this.editor.destroy();
                    this.editor = undefined;
                }
                if(!_.isEmpty(this.editorOptions)){
                    _.extend(data, this.editorOptions);
                }
                this.content[0].contentEditable = false;
                this.$el.addClass('no-select');
                this.model.set( data );
                this.wrapperUpdate();

                this.delegateEvents();
                slidesApp.isEditing = false;
                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'itemContentChanged';
                slidesApp.newValue = {contentType: 'text', content: this.model.get('content')};
                slidesApp.execute('action:push');
            },
            selectLiferayArticle: function() {
                var that = this;
                var liferayArticleModel = new Backbone.Model();
                liferayArticleModel.set('tempId', this.model.get('id') || this.model.get('tempId'));
                var AddTextArticleModalView = new AddTextArticleModal({ model: liferayArticleModel });
                valamisApp.execute('modal:show', AddTextArticleModalView);
                AddTextArticleModalView.$el.find('.js-title-edit').closest('tr').hide();

                AddTextArticleModalView.on('article:added', function (data) {
                    slidesApp.oldValue = { contentType: 'text', content: that.model.get('content') };
                    that.model.set('width', 'auto');
                    that.model.set('content', unescape(data.replace(/\+/g, ' ')));
                    that.model.set('width', Math.min(800, that.content.width()));
                    valamisApp.execute('modal:close', AddTextArticleModalView);
                    var images = that.content.find('img');
                    if(images.length > 0) {
                        that.content.find('img').last().load(function() {
                            updateAndPushAction();
                        });
                    }
                    else updateAndPushAction();
                });
                function updateAndPushAction() {
                    that.wrapperUpdate();
                    slidesApp.viewId = that.cid;
                    slidesApp.actionType = 'itemContentChanged';
                    slidesApp.newValue = { contentType: 'text', content: that.model.get('content') };
                    slidesApp.execute('action:push');
                }
                this.$('.item-settings').hide();
            }
        });

        TextElementModule.CreateModel = function() {
            var model = new TextElementModule.Model( {
                'content': Valamis.language['newTextElementLabel'],
                'slideEntityType': 'text'
            });
            return model;
        }
    }
});

textElementModule.on('start', function() {
    slidesApp.execute('toolbar:item:add', {title: 'Text', label: Valamis.language['textLabel'], slideEntityType: 'text'});
});