var LessonPageBaseModel = Backbone.Model.extend({
    getSlideElementsFromCollection: function() {
        var that = this;
        return _.filter (slidesApp.slideElementCollection.models, function(data){
            return (data.get('slideId') == that.get('id')
                || data.get('slideId') == that.get('tempId'))
                && !data.get('toBeRemoved')
        });
    },
    copyBgImage: function(slideId, callback){
        var slide = this,
            slideModel = slidesApp.slideCollection.findWhere({id: slideId}),
            imageName = slideModel.getBackgroundImageName(),
            imageSize = slideModel.getBackgroundSize();
        if(imageName.indexOf('blob:') > -1){
            return;
        }
        var bgImageUrl = slidesApp.getFileUrl(slideModel, imageName);
        imgSrcToBlob(bgImageUrl).then(function(blob){
            var formData = new FormData();
            formData.append('p_auth', Liferay.authToken);
            formData.append('files[]', blob, imageName);
            formData.itemModel = new FileUploaderItemModel({
                filename: imageName
            });
            slide
                .set('formData', formData)
                .set('bgImage', createObjectURL(blob) + ' '+ imageSize)
                .unset('fileModel');
            if(typeof callback === 'function')
                callback();
        });
    },
    getBackgroundSize: function () {
        return (this.attributes.bgImage || '').split(' ')[1];
    },
    getBackgroundImageName: function () {
        return (this.attributes.bgImage || '').split(' ')[0];
    },
    copyImageFromGallery: function (data, bgSize) {
        if (data && !this.get('formData')) {
            var fileExt = Utils.getExtByMime ? Utils.getExtByMime(data.get('mimeType')) : null;
            var fileName = fileExt
                ? data.get('title') + '.' + fileExt
                : data.get('title');
            var formData = new FormData();
            formData.append('contentType', 'document-library');
            formData.append('fileEntryID', data.get('id'));
            formData.append('file', fileName);
            formData.append('fileVersion', data.get('version'));
            formData.append('p_auth', Liferay.authToken);
            this
                .unset('fileModel')
                .set('formData', formData)
                .set('mediaGalleryTitle', fileName);
            //on slide and theme model need to set bgSize
            if (bgSize) {
                this.set('bgImage', fileName + ' ' + bgSize);
            }
        }
    }
});

var ModelWithDevicesProperties = LessonPageBaseModel.extend({
    copyProperties: function(keys){
        var properties = !_.isEmpty(this.get('properties')) ? this.get('properties') : {};
        if (typeof properties != 'object')
            properties = JSON.parse(properties);
        if(!keys){
            return _.cloneDeep(properties);
        }
        var newProperties = {};
        _.each(properties, function(props, deviceId){
            newProperties[deviceId] = {};
            _.each(props, function(v, k){
                if(_.contains(keys, k)){
                    newProperties[deviceId][k] = v;
                }
            });
        });
        return newProperties;
    },
    onChangeProperties: function(){
        this.prepareAttributes();
        var currentProperties = this.getCurrentProperties();
        _.each(this.devicesProperties, function(propName){
            if(_.isUndefined(currentProperties[propName])){
                currentProperties[propName] = '';
            }
        });
        this.set(currentProperties);
    },
    prepareAttributes: function(){
        if(this.get('properties')){
            var properties = this.copyProperties();
            _.each(properties, function(props){
                Object.keys(props).forEach(function(key) {
                    if(_.contains(['left','top','width','height','zIndex'], key)){
                        props[key] = !isNaN(props[key])
                            ? parseInt(props[key], 10)
                            : 0;
                    }
                });
            });
            this.set('properties', properties);
        }
        if(this.get('zIndex')){
            this.set('zIndex', !isNaN(this.get('zIndex'))
                ? parseInt(this.get('zIndex'), 10)
                : 0
            );
        }
    },
    getCurrentProperties: function(){
        var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
            deviceLayoutCurrentId = deviceLayoutCurrent.get('id'),
            properties = this.copyProperties();
        return !_.isEmpty(properties[deviceLayoutCurrentId])
            ? properties[deviceLayoutCurrentId]
            : {};
    },
    setLayoutProperties: function(layoutId){
        if(!layoutId){
            layoutId = slidesApp.devicesCollection.getCurrentId();
        }
        var properties = this.copyProperties();
        properties[layoutId] = {};
        _.each(this.devicesProperties, function(propName){
            if(this.get(propName)){
                properties[layoutId][propName] = this.get(propName);
            }
        }, this);
        this.set('properties', properties);
    },
    updateProperties: function(data){
        var deviceLayoutCurrentId = slidesApp.devicesCollection.getCurrentId();
        var properties = this.copyProperties();
        if(!properties[deviceLayoutCurrentId]){
            properties[deviceLayoutCurrentId] = {};
        }
        _.extend(properties[deviceLayoutCurrentId], data);
        this.set('properties', properties);
    },
    mergeProperties: function(newProperties){
        if(!_.isEmpty(newProperties)){
            var properties = this.copyProperties();
            properties = _.mapValues(properties, function(props, deviceId){
                if(newProperties[deviceId]){
                    _.extend(props, newProperties[deviceId]);
                }
                return props;
            });
            this.set('properties', properties);
        }
    }
});

var baseLessonStudioModels = {
    LessonPageModel: ModelWithDevicesProperties.extend({
        defaults: function(){
            return {
                title: '',
                height: '',
                bgColor: '',
                bgImage: '',
                font: '',
                properties: {},
                toBeRemoved: false,
                isTemplate: false,
                isLessonSummary: false,
                changed: false,
                fileModel: null,
                formData: null,
                fileUrl: null
            }
        },
        initialize: function(){
            this.devicesProperties = ['height'];
            this
              .on('add change', function(model, collection, options){
                  if(typeof options == 'undefined'){
                      options = collection;
                  }
                  if(slidesApp.historyManager && !slidesApp.initializing){
                      slidesApp.historyManager.pushModelChange(model, _.extend({
                          skipAttributes: ['id','tempId','slideElements','height','isTemplate','isLessonSummary', 'changed'],
                          omitUndoActions: ['height', 'changed'],
                          namespace: 'slides'
                      }, options));
                      if (_.intersection(_.keys(model.changedAttributes()), ['slideElements','isTemplate','isLessonSummary', 'changed']).length == 0 &&
                          _.keys(model.changedAttributes())[0] != 'height'){
                          model.set('changed', true, {silent: true})
                      }
                  }
              });
            this.on('change:height', function(model, value){
                if(!slidesApp.initializing){
                    if(slidesApp.RevealModule && slidesApp.RevealModule.view){
                        slidesApp.RevealModule.view.updateSlideHeight();
                    }
                }
            });
            this.on('change:properties', this.onChangeProperties, this);
        },
        getId: function(){
            return this.get('id') || this.get('tempId');
        },
        getLayoutProperties: function(layoutId){
            if(!layoutId){
                var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                layoutId = deviceLayoutCurrent.get('id');
            }
            var properties = !_.isEmpty(this.get('properties')) ? this.get('properties') : {};
            if( _.isEmpty(properties) || _.isEmpty( properties[layoutId] ) ){
                properties[layoutId] = {
                    height: deviceLayoutCurrent.get('minHeight')
                };
            }
            return properties[layoutId];
        },
        applyLayoutProperties: function(){
            var properties = this.getLayoutProperties();
            if(!_.isEmpty(properties)){
                this.set(properties);
            }
        },
        getFont: function(font){
            font = font || this.get('font');
            var fontData = _.object(['fontFamily', 'fontSize', 'fontColor'], font.split('$')),
              defaults = lessonStudioModels.LessonPageThemeModel.prototype.defaults();
            fontData = _.mapValues(fontData, function(val, key){
                return !val ? defaults[key] : val;
            });
            return fontData;
        },
        changeElementsFont: function(){
            this.updateElementsFont(['text','question','plaintext']);
        },
        updateQuestionFont: function(){
            this.updateElementsFont(['question','plaintext']);
        },
        updateElementsFont: function(elementTypes){
            var font = this.getFont();
            var elements = this.getElements();
            _.each(elements, function(model){
                if(_.contains(elementTypes, model.get('slideEntityType')) ){
                    model.resetProperties({
                        fontSize: font.fontSize
                    });
                }
            });
        },
        getElements: function(where){
            var slideId = this.getId();
            var search = _.extend({slideId: slideId, toBeRemoved: false}, where);
            return slidesApp.slideElementCollection.where( search );
        },
        updateAllElements: function(attributes){
            var elements = this.getElements();
            _.each(elements, function(model){
                model.set(attributes);
            });
        },
        updateElements: function(elements, attributes){
            _.each(elements, function(model){
                model.set(attributes);
            });
        },
        getMaxZIndex: function(){
            var elements = this.getElements(),
              zIndexes = [];
            _.each(elements, function(item){
                zIndexes.push(item.getZIndex());
            });
            return zIndexes.length > 0 ? _.max(zIndexes) : 1;
        },
        hasQuestions: function() {
            var slideId = this.getId();
            // it is not optimal way to check whole collection
            // but attribute 'toBeRemoved' is actual in slidesApp.slideElementCollection
            // and not updated in slidesApp.activeSlideModel
            var hasQuestions = slidesApp.slideElementCollection.filter(function(elem) {
                return elem.get('slideId') == slideId && !elem.get('toBeRemoved')
                  && _.contains(['question','plaintext', 'randomquestion'], elem.get('slideEntityType'))
            }).length > 0;
            return hasQuestions;
        }
    }),

    LessonPageElementModel: ModelWithDevicesProperties.extend({
        defaults: function(){
            return {
                id: null,
                tempId: null,
                width: 300,
                height: 50,
                top: 40,
                left: 30,
                zIndex: 1,
                fontSize: '',
                classHidden: '',
                properties: {},
                slideEntityType: '',
                content: '',
                slideId: 0,
                correctLinkedSlideId: null,
                incorrectLinkedSlideId: null,
                notifyCorrectAnswer: false,
                toBeRemoved: false,
                active: false,
                selected: false,
                changed: false
            };
        },
        initialize: function(){
            this.devicesProperties = ['width','height','top','left','classHidden','fontSize'];
            this.set( 'zIndex', this.getZIndex() );
            if(!_.isEmpty(this.attributes.properties)){
                this.prepareAttributes();
            }
            this.propertiesToAttrs();
            this.on('add change',function(model, collection, options){
                if(typeof options == 'undefined'){
                    options = collection;
                }
                if(slidesApp.historyManager && !slidesApp.initializing){
                    slidesApp.historyManager.pushModelChange(model, _.extend({
                        skipAttributes: ['id','tempId','googleClientApiAvailable','active','selected', 'changed'].concat(this.devicesProperties),
                        omitUndoActions: ['googleClientApiAvailable','active','selected', 'id', 'changed'].concat(this.devicesProperties),
                        namespace: 'slidesElements'
                    }, options));
                    if (_.intersection(_.keys(model.changedAttributes()), ['googleClientApiAvailable','active','selected', 'changed']).length == 0){
                        model.set('changed', true, {silent: true})
                    }
                }
            }, this);
            this.on('change:properties', this.onChangeProperties, this);
            this.on('change:active', this.onChangeActive, this);
            this.on('change:questionModel', this.onChangeQuestionModel, this);
            this.on('destroy', this.onDestroy, this);
        },
        getId: function(){
            return this.get('id') || this.get('tempId');
        },
        getZIndex: function(){
            var zIndex = !isNaN(this.get('zIndex'))
              ? Math.max(1, parseInt(this.get('zIndex')))
              : 1;
            return zIndex;
        },
        onChangeActive: function(model, value){
            if(value && model.collection){
                var modelId = model.getId();
                this.collection.each(function(item){
                    if(item.getId() != modelId){
                        item.set('active', false);
                    }
                });
            }
        },
        onDestroy: function(){
            if(Marionette.ItemView.Registry){
                var registeredItemView =
                  Marionette.ItemView.Registry.getByModelId(this.get('tempId')) ||
                  Marionette.ItemView.Registry.getByModelId(this.get('id'));
                if (registeredItemView)
                    Marionette.ItemView.Registry.remove(registeredItemView.cid);
            }
        },
        onChangeQuestionModel: function(model, value) {
            if (_.contains(['question', 'plaintext'], model.get('slideEntityType'))) {
                this.trigger('change:question:content', value);
            }
            else if(model.get('slideEntityType') == 'randomquestion') {
                this.trigger('change:question:random:content', value);
            }

        },
        getLayoutProperties: function(layoutId,  percent){
            var properties = this.copyProperties();
            percent = percent || [1,1];
            if (_.isEmpty(properties) || _.isEmpty(properties[layoutId])) {
                properties[layoutId] = {
                    width: this.valueByPercent('width', this.get('width'), percent),
                    height: this.valueByPercent('height', this.get('height'), percent),
                    top: this.valueByPercent('top', this.get('top'), percent),
                    left: this.valueByPercent('left', this.get('left'), percent)
                };

                var isQuestion = _.contains(['question', 'plaintext'], this.get('slideEntityType'));
                if (isQuestion) {
                    _.extend(properties[layoutId], { fontSize: this.get('fontSize') })
                }

                var MAX_WIDTH = 800;
                if (properties[layoutId].width > MAX_WIDTH) {
                    var oldWidth = properties[layoutId].width,
                      oldHeight = properties[layoutId].height;
                    properties[layoutId].width = MAX_WIDTH;
                    var diff = MAX_WIDTH / oldWidth;
                    properties[layoutId].height = oldHeight * diff;
                }
            }
            if( !properties[layoutId].fontSize ){
                properties[layoutId].fontSize = '';
            }
            if( !properties[layoutId].classHidden ){
                properties[layoutId].classHidden = '';
            }
            return properties[layoutId];
        },
        propertiesToAttrs: function(deviceLayoutId){
            if(!deviceLayoutId && slidesApp.devicesCollection){
                var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                deviceLayoutId = deviceLayoutCurrent ? deviceLayoutCurrent.get('id') : null;
            }
            if(deviceLayoutId){
                var properties = this.getLayoutProperties(deviceLayoutId);
                this.set(properties);
            }
        },
        applyLayoutProperties: function(deviceLayoutCurrentId, layoutSizeRatio){
            var properties = this.getLayoutProperties(deviceLayoutCurrentId, layoutSizeRatio);
            this.updateProperties(properties);
            this.set(properties);//Will be called only for cases if properties is not changed
        },
        /** Update properties for all devices */
        resetProperties: function(data){
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
              deviceLayoutCurrentId = deviceLayoutCurrent.get('id');
            var properties = this.copyProperties();
            if(!properties[deviceLayoutCurrentId]){
                properties[deviceLayoutCurrentId] = {};
            }
            var keys = _.keys(data);
            properties = _.mapValues(properties, function(props, key){
                if(deviceLayoutCurrentId == parseInt(key, 10)) {
                    _.extend(props, data);
                } else {
                    var percent = slidesApp.devicesCollection.getSizeRatio(deviceLayoutCurrentId, parseInt(key, 10));
                    props = _.mapValues(props, function(value, k){
                        if(_.contains(keys, k)){
                            value = this.valueByPercent(k, data[k], percent);
                        }
                        return value;
                    }, this);
                }
                return props;
            }, this);
            this.set('properties', properties);
        },
        valueByPercent: function(key, value, percent){
            switch (key){
                case "width":
                case "height":
                case "left":
                    value = Math.round(parseInt(value, 10) * percent[0]);
                    break;
                case "top":
                    value = Math.round(parseInt(value, 10) * percent[1]);
                    break;
                case "fontSize":
                    value = Math.ceil(parseInt(value.replace(/\D/g, '')) * percent[0]) + 'px';
                    break;
            }
            return value;
        },
        getNewPosition: function(left, top, width, height){
            var current = {
                left: left || this.get('left'),
                top: top || this.get('top')
            };
            var new_pos = {
                left: parseInt(current.left) + 10,
                top: parseInt(current.top) + 10
            };
            return new_pos;
        },
        isQuestion: function(){
            var elementType = this.get('slideEntityType');
            return elementType == 'question'
                || elementType == 'plaintext'
                ||  elementType == 'randomquestion';
        },
        isSummaryElement: function(){
            return _.contains(this.get('content'), 'lesson-summary-');
        }
    }),

    LessonPageThemeModel: LessonPageBaseModel.extend({
        defaults: function() {
            return {
                title: 'Theme',
                type: 'default',
                fontFamily: 'inherit',
                fontSize: '18px',
                fontColor: '#000',
                isTheme: true
            }
        },
        parse: function( response ){
            if ( response.font ) {
                var fontParts = response.font.split('$');
                response.fontFamily = fontParts[0];
                response.fontSize = fontParts[1];
                response.fontColor = fontParts[2];
            }
            return response;
        },
        url: function() {
            return path.root + path.api.slideThemes + this.id;
        }
    }),

    LessonDeviceModel: Backbone.Model.extend({
        defaults: function() {
            return {
                title: '',
                name: '',
                active: false,
                selected: false
            }
        },
        initialize: function(){
            this.on('change', function(model){
                if(slidesApp.historyManager && !slidesApp.initializing){
                    slidesApp.historyManager.pushModelChange(model);
                }
            });
            this.on('change:active', this.onChangeActive);
        },
        onChangeActive: function(){
            var model = this;
            if(model.get('active')){
                this.collection.each(function(item){
                    if( item.get('id') != model.get('id') && item.get('active') ){
                        item.set('active', false);
                    }
                }, this.collection);
            } else {
                if(!slidesApp.initializing){
                    this.collection.previousActiveId = model.get('id');
                }
            }
        }
    }),

    ThirdPartySelectorModel: Backbone.Model.extend({
        defaults: function() {
            return {
                name: '',
                description: '',
                image: '',
                url: '',
                width: '',
                height: '',
                customerKey: '',
                customerSecret: '',
                isPrivate: ''
            }
        }
    })
};


var baseLessonStudioCollections = {
    LessonPageCollection: Backbone.Collection.extend({
        model: baseLessonStudioModels.LessonPageModel,
        parse: function (response) {
            this.trigger('lessonPageCollection:updated', {total: response.length, records: response});
            return response;
        },
        isTemplates: false,
        initialize: function (models, options) {
            if (options && options.isTemplates) {
                this.isTemplates = true;
            }
            this.on('sync', this.onSync);
        },
        onSync: function () {
            if (slidesApp.initializing && !this.isTemplates) {
                slidesApp.devicesCollection.setSelectedDefault();
                slidesApp.topbar.currentView._renderChildren();
            }
        },
        getRootSlide: function() {
            return this.findWhere({
                leftSlideId: undefined,
                topSlideId: undefined,
                toBeRemoved: false
            });
        }
    }),

    LessonPageElementCollection: Backbone.Collection.extend({
        model: baseLessonStudioModels.LessonPageElementModel,
        parse: function (response) {
            this.trigger('lessonPageElementCollection:updated', { total: response.length });
            return response;
        },
        updateSelectedProperties: function(){
            var selectedDevicesIds = slidesApp.devicesCollection.getSelectedIds(),
              deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
            this.each(function(model){
                if( model.get('toBeRemoved') ) return;
                var properties = model.copyProperties();
                //Delete properties for unselected devices
                _.each(properties, function(props, key){
                    if(!_.contains(selectedDevicesIds, parseInt(key))){
                        delete properties[key];
                    }
                });
                //Set properties for new devices
                _.each(selectedDevicesIds, function(deviceId){
                    if(!properties[deviceId]){
                        var layoutSizeRatio = slidesApp.devicesCollection.getSizeRatio(deviceLayoutCurrent.get('id'), deviceId);
                        properties[deviceId] = model.getLayoutProperties(deviceId, layoutSizeRatio);
                    }
                });
                model.set('properties', properties);
            });
        }
    }),

    LessonDeviceCollection: Backbone.Collection.extend({
        model: baseLessonStudioModels.LessonDeviceModel,
        comparator: function (a, b) {
            return a.get('id') > b.get('id') ? -1 : 1;
        },
        setSelectedDefault: function(){
            if(slidesApp.slideCollection.size() > 0){
                var devicesIds = [],
                  slideElements = [];
                slidesApp.slideCollection.each(function(model){
                    if(model.get('slideElements').length > 0){
                        slideElements = model.get('slideElements');
                        return false;
                    }
                });
                if(slideElements.length == 0 && slidesApp.slideElementCollection.size() > 0){
                    slideElements = slidesApp.slideElementCollection.where({ toBeRemoved: false });
                }
                if( slideElements.length > 0 ){
                    devicesIds = slideElements[0] instanceof Backbone.Model
                      ? _.keys( slideElements[0].get('properties') )
                      : _.keys( slideElements[0]['properties'] );
                    devicesIds = _.map(devicesIds, function(num){
                        return parseInt(num);
                    });
                    this.each(function(model){
                        model.set('selected', _.contains(devicesIds, model.get('id')));
                    });
                }
                else {
                    this.each(function(model){
                        model.set('selected', model.get('id') === 1);
                    });
                    devicesIds = [1];
                }
            }
            //set active device
            var selectedDevices = this.where({selected: true}),
              activeId = 1;
            if( selectedDevices.length > 0 ){
                activeId = _.last(selectedDevices).get('id');
            }
            this.previousActiveId = null;
            var deviceLayoutCurrent = this.findWhere({ id: activeId });
            if(deviceLayoutCurrent){
                deviceLayoutCurrent.set('active', true);
            }
        },
        updateSelectedModels: function(selectedIds){
            slidesApp.historyManager.skipActions(true);
            selectedIds = selectedIds || [1];
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
            this.each(function(model){
                model.set('selected', _.contains(selectedIds, model.get('id')));
            });
            slidesApp.slideElementCollection.updateSelectedProperties(selectedIds);
            //Switch layout
            if( !_.contains(selectedIds, deviceLayoutCurrent.get('id')) && slidesApp.topbar ){
                var currentId = _.last(selectedIds);
                slidesApp.devicesCollection.get(currentId).set('active', true);
            }
            slidesApp.historyManager.skipActions(false);
        },
        getCurrent: function(){
            return this.findWhere({ active: true });
        },
        getCurrentId: function(){
            var deviceCurrent = this.getCurrent();
            return deviceCurrent ? deviceCurrent.get('id') : 1;
        },
        getSizeRatio: function(layoutOldId, layoutCurrentId){
            var deviceLayoutCurrent = layoutCurrentId
                ? this.findWhere({ id: layoutCurrentId })
                : this.getCurrent(),
              deviceLayoutOld = this.findWhere({ id: layoutOldId || 1 });

            var layoutWidth = deviceLayoutCurrent.get('minWidth'),
              layoutOldWidth = deviceLayoutOld.get('minWidth'),
              layoutHeight = deviceLayoutCurrent.get('minHeight'),
              layoutOldHeight = deviceLayoutOld.get('minHeight');

            return [layoutWidth / layoutOldWidth, layoutHeight / layoutOldHeight];
        },
        getSelectedIds: function(){
            var selected = this.where({ selected: true });
            return selected.map(function(item){
                return item.get('id');
            });
        }
    }),

/*    ThirdPartySelectorCollection: Backbone.Collection.extend({
        model: baseLessonStudioModels.ThirdPartySelectorModel
    })*/

    // was valamisApp.Entities.LazyCollection
    ThirdPartySelectorCollection: Backbone.Collection.extend({
        model: baseLessonStudioModels.ThirdPartySelectorModel,
        parse: function(response) {
            //this.trigger('thirdPartySelectorCollection:updated', { total: response.total, currentPage: response.page });
            this.total = response.total;
            return response.records;
        }
    })
};

