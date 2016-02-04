var lessonStudioServices = {
    slideSetService: new Backbone.Service({
        url: path.root,
        sync: {
            'delete': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'DELETE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'logoSrc')
                    );
                },
                'method': 'post'
            },
            'update': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'UPDATE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'logoSrc')
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'CREATE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'logoSrc')
                    );
                },
                'method': 'post'
            }
        },
        targets: {
            'publish': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'PUBLISH',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'logoSrc')
                    );
                },
                'method': 'post'
            },
            'clone': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'CLONE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'logoSrc')
                    );
                },
                'method': 'post'
            },
            'saveTemplate': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'CLONE',
                            courseId: Utils.getCourseId(),
                            isTemplate: true
                        },
                        _.omit(model.toJSON(), 'logoSrc')
                    );
                },
                'method': 'post'
            }
        }
    }),

    slideSetCollectionService: new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.slideSets,
                'data': function (collection, options) {
                    var filter = options.filter || {
                            sort: 'nameAsc',
                            searchtext: ''
                        };
                    return {
                        page: options.currentPage,
                        itemsOnPage: options.itemsOnPage,
                        titleFilter: filter.searchtext || '',
                        sortTitleAsc: filter.sort === 'nameAsc',
                        courseId: Utils.getCourseId(),
                        isTemplate: options.isTemplate
                    };
                },
                'method': 'get'
            }
        }
    }),

    slideService: new Backbone.Service({ 'url': path.root,
        'sync': {
            'delete': {
                'path': path.api.slides,
                'data': function(model) {
                    return _.extend({
                            action: 'DELETE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'update': {
                'path': path.api.slides,
                'data': function(model) {
                    return _.extend(
                        _.omit(model.toJSON(), 'logoSrc'),
                        model.toJSON(), {
                            action: 'UPDATE',
                            courseId: Utils.getCourseId()
                        }
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slides,
                'data': function(model) {
                    return _.extend({
                            action: 'CREATE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            }
        },
        'targets': {
            'clone': {
                'path': path.api.slides,
                'data': function(model){
                    return _.extend({
                            action: 'CLONE',
                            courseId: Utils.getCourseId(),
                            fromTemplate: true,
                            cloneElements: false
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'saveTemplate': {
                'path': path.api.slides,
                'data': function(model){
                    return {
                        id: model.id,
                        slideSetId: 0,
                        action: 'CLONE',
                        courseId: Utils.getCourseId(),
                        isTemplate: true
                    };
                },
                'method': 'post'
            },
            'copyFileFromTheme':{
                'path': path.api.slides,
                'data': function(model, options){
                    return {
                        id: options.id,
                        themeId: options.themeId,
                        action: 'COPYFILE',
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'post'
            }
        }
    }),

    slideCollectionService: new Backbone.Service({ url: path.root,
        sync: {
            'read': {
                'path': path.api.slides,
                'data': function(collection, options) {
                    return {
                        slideSetId: (options.slideSetId || 0),
                        isTemplate: (options.isTemplate || ''),
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'get'
            }
        }
    }),

    slideElementService: new Backbone.Service({ url: path.root,
        sync: {
            'delete': {
                'path': path.api.slideEntities,
                'data': function(model){
                    return _.extend({
                            action: 'DELETE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'update': {
                'path': path.api.slideEntities,
                'data': function(model){
                    return _.extend({
                            action: 'UPDATE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideEntities,
                'data': function(model){
                    return _.extend({
                            action: 'CREATE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            }
        },
        'targets': {
            'clone': {
                'path': path.api.slideEntities,
                'data': function (model) {
                    var clonedId = model.get('id') || model.get('clonedId'),
                        data = _.omit(model.toJSON(), ['id','clonedId']);
                    _.extend(data, {id: clonedId, action: 'CLONE', courseId: Utils.getCourseId()});
                    return data;
                },
                'method': 'post'
            }
        }
    }),

    slideElementCollectionService: new Backbone.Service({ url: path.root,
        sync: {
            'read': {
                'path': path.api.slideEntities,
                'data': function (collection, options) {
                    return {
                        slideId: options.model.id,
                        courseId: Utils.getCourseId()
                    };

                },
                'method': 'get'
            }
        }
    }),

    slideThemeService: new Backbone.Service({ url: path.root,
        sync: {
            'read': {
                'path': function (model) {
                    return path.api.slideThemes + (model.get('id') || model.id);
                },
                'data': function (model) {
                    return {
                        courseId: Utils.getCourseId()
                    }
                },
                'method': 'get'
            },
            'delete': {
                'path': function (model) {
                    return path.api.slideThemes;
                },
                'data': function(model){
                    return _.extend({
                            action: 'DELETE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'update': {
                'path': path.api.slideThemes,
                'data': function(model){
                    return _.extend({
                            action: 'UPDATE',
                            courseId: Utils.getCourseId()
                        },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideThemes,
                'data': function(model, options){
                    return _.extend({
                            action: 'CREATE',
                            isMyThemes: (options.themeType && options.themeType == 'personal'),
                            slideId : options.slideId,
                            bgImage: model.get('bgImage') ? model.get('bgImage') : [],
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), ['bgImage'])
                    );
                },
                'method': 'post'
            }
        }
    }),

    slideThemeCollectionService: new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.slideThemes,
                'data': function(collection, options) {
                    return {
                        id: (options && options.model) ? options.model.id : '',
                        isDefault: collection.mode == 'default',
                        isMyThemes: collection.mode == 'personal',
                        courseId: Utils.getCourseId()
                    };

                },
                'method': 'get'
            }
        }
    }),

    slideDevicesCollectionService: new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.slideDevices,
                'method': 'get'
            }
        }
    })
};

var lessonStudioModels = {
    LessonModel: Backbone.Model.extend({
        defaults: {
            title: '',
            description: '',
            courseId: '',
            devices: [1],
            randomQuestionsAmount: 0
        },
        initialize: function(){
            this.on('change:devices', this.onChangeDevices);
        },
        toJSON: function () {
            var res = _.omit(this.attributes, ['devices']);
            return res;
        },
        updateDevices: function(){
            var selected = slidesApp.devicesCollection.where({selected: true}),
                devices = [];
            if( selected.length > 0 ){
                selected.forEach(function(item){
                    devices.push( item.get('id') );
                });
                this.set( 'devices', devices );
            }
            slidesApp.slideElementCollection.updateSelectedProperties();
        },
        onChangeDevices: function(){
            var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent(),
                devices = this.get('devices');
            if(slidesApp.topbar.currentView){
                slidesApp.topbar.currentView.updateDevicesView();
            }
            //Switch layout
            if( !_.contains(devices, deviceLayoutCurrent.get('id')) ){
                var layoutId = _.last(devices);
                slidesApp.topbar.currentView.setLayout(layoutId);
            }
        },
        updateRandomAmount: function(increase) {
            var oldValue = this.get('randomQuestionsAmount');
            var newValue = (increase) ? oldValue + 1 : oldValue - 1;
            this.set('randomQuestionsAmount', newValue);
        }
    }).extend(lessonStudioServices.slideSetService),

    LessonPageModel: Backbone.Model.extend({
        defaults: function(){
            return {
                title: '',
                height: '',
                properties: {},
                toBeRemoved: false
            }
        },
        initialize: function(){
            this.on('change:height', function(model, value){
                if(!slidesApp.initializing){
                    this.setLayoutProperties();
                    if(slidesApp.RevealModule && slidesApp.RevealModule.view){
                        slidesApp.RevealModule.view.updateSlideHeight();
                    }
                }
            });
        },
        toJSON: function () {
            var properties = !_.isEmpty(this.get('properties'))
                ? JSON.stringify(this.get('properties'))
                : '{}',
                res = _.omit(this.attributes, ['slideElements', 'formData', 'file', 'fileUrl', 'fileModel','height','devices']);
            return _.extend(res, {properties: properties});
        },
        setLayoutProperties: function(layoutId){
            if(!layoutId){
                var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                layoutId = deviceLayoutCurrent.get('id');
            }
            var properties = !_.isEmpty(this.get('properties')) ? this.get('properties') : {};
            properties[layoutId] = {};
            if(this.get('height')){
                properties[layoutId].height = this.get('height');
            }
            this.set('properties', properties);
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
        getSlideElements: function() {
            var slideElements = [];
            _.each(this.get('slideElements'), function(slideElement) {
                slideElements.push(new lessonStudioModels.LessonPageElementModel(slideElement));
            });
            return slideElements;
        },
        getBackgroundSize: function() { return (this.attributes.bgImage || '').split(' ')[1]; },
        getBackgroundImageName: function() { return (this.attributes.bgImage || '').split(' ')[0]; },
        /** copy background image as formData */
        copyBgImage: function(slideId){
            var slide = this,
                slideModel = slidesApp.slideCollection.findWhere({id: slideId}),
                imageName = slideModel.getBackgroundImageName();
            if(imageName.indexOf('blob:') > -1){
                return;
            }
            var bgImageUrl = slidesApp.getFileUrl(slideModel, imageName);
            imgSrcToBlob(bgImageUrl, function(blob){
                var formData = new FormData();
                formData.append('p_auth', Liferay.authToken);
                formData.append('files[]', blob, imageName);
                slide
                    .set('formData', formData)
                    .unset('fileModel');
            });
        }
    }).extend(lessonStudioServices.slideService),

    LessonPageElementModel: Backbone.Model.extend({
        defaults: function(){
            return {
                width: 300,
                height: 50,
                top: 40,
                left: 40,
                zIndex: 0,
                fontSize: '',
                classHidden: '',
                properties: {},
                slideEntityType: '',
                content: '',
                slideId: 0,
                correctLinkedSlideId: null,
                incorrectLinkedSlideId: null,
                notifyCorrectAnswer: false,
                toBeRemoved: false
            };
        },
        initialize: function(){
            this.set( 'zIndex', parseInt( this.get('zIndex') ) );
            if(Marionette.ItemView.Registry){
                this.on('change:classHidden',function(){
                    var modelView = Marionette.ItemView.Registry
                        .getByModelId(this.get('tempId') || this.get('id'));
                    if(modelView){
                        modelView.hideForDeviceApply();
                    }
                });
            }
        },
        toJSON: function () {
            var properties = !_.isEmpty(this.get('properties'))
                ? JSON.stringify(this.get('properties'))
                : '{}';
            var res = _.omit(this.attributes, ['formData', 'file', 'fileUrl', 'fileModel', 'properties', 'fontSize', 'classHidden', 'fontColor'/*,'width','height','top','left'*/]);
            return _.extend(res, {properties: properties});
        },
        getLayoutProperties: function(layoutId, percent){
            var properties = !_.isEmpty(this.get('properties')) ? this.get('properties') : {};
            percent = percent || [1,1];
            if( _.isEmpty(properties) || _.isEmpty( properties[layoutId] ) ){
                var fontSize = this.get('fontSize') || '16px';
                properties[layoutId] = {
                    width: Math.round(parseInt(this.get('width')) * percent[0]),
                    height: Math.round(parseInt(this.get('height')) * percent[0]),
                    top: Math.round(Math.max(parseInt(this.get('top')) * percent[1], 10)),
                    left: Math.round(Math.max(parseInt(this.get('left')) * percent[0], 10)),
                    fontSize: this.get('fontSize')
                        ? Math.ceil(parseInt(fontSize.replace(/\D/g, '')) * percent) + 'px'
                        : ''
                };
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
        setLayoutProperties: function(layoutId){
            if(!layoutId){
                var deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
                layoutId = deviceLayoutCurrent.get('id');
            }
            var properties = !_.isEmpty(this.get('properties')) ? this.get('properties') : {};
            properties[layoutId] = {
                width: parseInt(this.get('width')),
                height: parseInt(this.get('height')),
                top: parseInt(this.get('top')),
                left: parseInt(this.get('left'))
            };
            if(this.get('fontSize')){
                properties[layoutId].fontSize = this.get('fontSize');
            }
            if(this.get('fontColor')){
                properties[layoutId].fontColor = this.get('fontColor');
            }
            if(this.get('classHidden')){
                properties[layoutId].classHidden = this.get('classHidden');
            }
            this.set('properties', properties);
        },
        applyLayoutProperties: function(deviceLayoutCurrentId, layoutSizeRatio){
            var properties = this.getLayoutProperties(deviceLayoutCurrentId, layoutSizeRatio);
            this.set(properties);
        },
        copyProperties: function(){
            var properties = !_.isEmpty(this.get('properties')) ? this.get('properties') : {},
                newProperties = {};
            _.each(properties, function(prop, deviceId){
                newProperties[deviceId] = jQueryValamis.extend({}, prop);
            });
            return newProperties;
        },
        getNewPosition: function(left, top, width, height, areaHeight){
            var current = {
                left: left || this.get('left'),
                top: top || this.get('top'),
                width: width || this.get('width'),
                height: height || this.get('height'),
                areaHeight: areaHeight || slidesApp.RevealModule.view.ui.work_area.height()
            };
            var offset = {
                left: parseInt(current.left),
                top: parseInt(current.top) + parseInt(current.height) + 10
            };
            if(offset.top + parseInt(current.height) > current.areaHeight){
                offset.left = parseInt(current.left) + parseInt(current.width) + 10;
                offset.top = parseInt(current.top);
            }
            return offset;
        }
    }).extend(lessonStudioServices.slideElementService),

    LessonPageThemeModel: Backbone.Model.extend({
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
        },
        getBackgroundSize: function() { return (this.attributes.bgImage || '').split(' ')[1]; },
        getBackgroundImageName: function() { return (this.attributes.bgImage || '').split(' ')[0]; }
    }).extend(lessonStudioServices.slideThemeService),

    LessonDeviceModel: Backbone.Model.extend({
        defaults: function() {
            return {
                title: '',
                name: '',
                active: false
            }
        },
        initialize: function(){
            if(slidesApp.type == 'editor'){
                this.on('change:active', this.onChangeActive);
            }
        },
        onChangeActive: function(model, active){
            if(active){
                this.collection.each(function(item){
                    if( item.get('id') != model.get('id') ){
                        item.set('active', false);
                    }
                });
                if(slidesApp.editorArea){
                    slidesApp.editorArea.$el.closest('.slides-work-area-wrapper')
                        .attr('data-layout', model.get('name'));
                }
                slidesApp.RevealModule.configure({
                    width: model.get('minWidth') || model.get('maxWidth')
                });
                if(slidesApp.RevealModule.view){
                    slidesApp.RevealModule.view.updateSlideHeight();
                }
            }
        }
    })

};

var lessonStudioCollections = {
    Filter: Backbone.Model.extend({
        defaults: {
            searchtext: '',
            sort: 'nameAsc'
        }
    }),

    LessonCollection: Backbone.Collection.extend({
        model: lessonStudioModels.LessonModel,
        parse: function(response) {
            this.trigger('lessonCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        }
    }).extend(lessonStudioServices.slideSetCollectionService),

    LessonPageCollection: Backbone.Collection.extend({
        model: lessonStudioModels.LessonPageModel,
        parse: function(response) {
            this.trigger('lessonPageCollection:updated', { total: response.length, records: response });
            return response;
        },
        isTemplates: false,
        initialize: function(models, options){
            if( options && options.isTemplates ){
                this.isTemplates = true;
            }
            this.on('sync', this.onSync);
        },
        onSync: function(){
            if(slidesApp.initializing && !this.isTemplates){
                slidesApp.devicesCollection.setSelectedDefault();
                slidesApp.topbar.currentView._renderChildren();
            }
        }
    }).extend(lessonStudioServices.slideCollectionService),

    LessonPageElementCollection: Backbone.Collection.extend({
        model: lessonStudioModels.LessonPageElementModel,
        parse: function (response) {
            this.trigger('lessonPageElementCollection:updated', { total: response.length });
            return response;
        },
        updateSelectedProperties: function(){
            var devices = slidesApp.slideSetModel.get('devices'),
                deviceLayoutCurrent = slidesApp.devicesCollection.getCurrent();
            this.each(function(model){
                if( model.get('toBeRemoved') ) return;
                var properties = model.get('properties');
                //Delete properties for unselected devices
                _.each(properties, function(props, key){
                    if(!_.contains(devices, parseInt(key))){
                        delete properties[key];
                    }
                });
                //Set properties for new devices
                _.each(devices, function(deviceId){
                    if(!properties[deviceId]){
                        var layoutSizeRatio = slidesApp.devicesCollection.getSizeRatio(deviceLayoutCurrent.get('id'), deviceId);
                        properties[deviceId] = model.getLayoutProperties(deviceId, layoutSizeRatio);
                    }
                });
                model.set('properties', properties);
            });
        }
    }).extend(lessonStudioServices.slideElementCollectionService),

    LessonPageThemeCollection: Backbone.Collection.extend({
        model: lessonStudioModels.LessonPageThemeModel,
        mode: 'default'
    }).extend(lessonStudioServices.slideThemeCollectionService),

    LessonDeviceCollection: Backbone.Collection.extend({
        model: lessonStudioModels.LessonDeviceModel,
        comparator: function (a, b) {
            return a.get('id') > b.get('id') ? -1 : 1;
        },
        updateSelected: function(){
            var devices = slidesApp.slideSetModel
                ? slidesApp.slideSetModel.get( 'devices' )
                : [1];
            this.each(function(model){
                var selected = _.indexOf( devices, model.get('id') ) > -1;
                model.set('selected', selected);
            });
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
            var deviceLayoutCurrent = this.findWhere({ id: activeId });
            if(deviceLayoutCurrent){
                deviceLayoutCurrent.set('active', true);
            }
            if(slidesApp.slideSetModel){
                slidesApp.slideSetModel.set( 'devices', devicesIds );
            }
        },
        getCurrent: function(){
            return this.findWhere({ active: true });
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
        }
    }).extend(lessonStudioServices.slideDevicesCollectionService)

};