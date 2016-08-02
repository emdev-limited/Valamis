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
                        _.omit(model.toJSON(), ['slides', 'tags', 'logoSrc'])
                    );
                },
                'method': 'post'
            },
            'update': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'UPDATE',
                            courseId: Utils.getCourseId(),
                            tags: model.getTagIds()
                        },
                        _.omit(model.toJSON(), ['slides', 'tags', 'logoSrc'])
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'CREATE',
                            courseId: Utils.getCourseId(),
                            tags: model.getTagIds()
                        },
                        _.omit(model.toJSON(), ['slides', 'tags', 'logoSrc'])
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
                        _.omit(model.toJSON(), 'slides')
                    );
                },
                'method': 'post'
            },
            'clone': {
                'path': path.api.slideSets,
                'data': function (model, option) {
                    return _.extend({
                            action: 'CLONE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'slides')
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
                        _.omit(model.toJSON(), 'slides')
                    );
                },
                'method': 'post'
            },
            'deleteAllVersions': {
                'path': path.api.slideSets,
                'data': function (model) {
                    return _.extend({
                            action: 'DELETEALLVERSIONS',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), 'slides')
                    );
                },
                'method': 'post'
            },
            'getAllVersions': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/versions';
                },
                'data': function (model) {
                    return {
                        id: model.id,
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'get'
            },
            'getLessonId': {
                'path': function(model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/lessonId';
                },
                'data': function() {
                    return {
                        'action': 'prepareLesson',
                        courseId: Utils.getCourseId()
                    }
                },
                'method': 'post'
            },
            'updateLessonVisibility': {
                'path': path.api.packages,
                'data': function (model, options) {
                    return {
                        action: 'UPDATE_VISIBLE',
                        id: options.lessonId,
                        isVisible: options.isVisible,
                        courseId: Utils.getCourseId()
                    }
                },
                'method': 'post'
            },
            'deleteUnpublishedLesson': {
                'path': function(model, options){
                    return path.api.packages + options.lessonId
                },
                'data': function() {
                    return {
                        courseId: Utils.getCourseId()
                    }
                },
                'method': 'delete'
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
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend(
                        {
                            action: 'UPDATE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), ['slideElements', 'formData', 'file', 'fileUrl', 'logoSrc', 'fileModel','height']),
                        {properties: properties}
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slides,
                'data': function(model) {
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend({
                            action: 'CREATE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), ['slideElements', 'formData', 'file', 'fileUrl', 'logoSrc', 'fileModel','height']),
                        {properties: properties}
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
                    return {
                        action: 'DELETE',
                        courseId: Utils.getCourseId(),
                        id: model.get('id')
                    };
                },
                'method': 'post'
            },
            'update': {
                'path': path.api.slideEntities,
                'data': function(model){
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend({
                                action: 'UPDATE',
                                courseId: Utils.getCourseId()
                            },
                            _.omit(model.toJSON(), ['questionModel', 'formData', 'file', 'fileUrl', 'fileModel', 'properties', 'fontSize', 'classHidden', 'fontColor', 'width', 'height', 'top', 'left']),
                            {properties: properties}
                        );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideEntities,
                'data': function(model){
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend({
                            action: 'CREATE',
                            courseId: Utils.getCourseId()
                        },
                        _.omit(model.toJSON(), ['questionModel', 'formData', 'file', 'fileUrl', 'fileModel', 'properties', 'fontSize', 'classHidden', 'fontColor', 'width', 'height', 'top', 'left']),
                        {properties: properties}
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
            randomQuestionsAmount: 0,
            themeId: null,
            slideOrder: null
        },
        initialize: function(){
            this.on('change',function(model){
                if(slidesApp.historyManager && !slidesApp.initializing){
                    slidesApp.historyManager.pushModelChange(model, {
                        skipAttributes: ['isActive','slides', 'duration', 'isSelectedContinuity', 'oneAnswerAttempt', 'playerTitle', 'scoreLimit', 'topDownNavigation'],
                        namespace: 'lessons'
                    });
                }
            }, this);
        },
        getTagIds: function() {
            //from {id:#, text:###} to ids
            return _(this.get('tags')).map(function (tag) { return tag.id || tag }).value();
        },
        updateRandomAmount: function(increase) {
            var oldValue = this.get('randomQuestionsAmount');
            var newValue = (increase) ? oldValue + 1 : oldValue - 1;
            this.set('randomQuestionsAmount', newValue);
        }
    }).extend(lessonStudioServices.slideSetService),

    LessonPageModel: baseLessonStudioModels.LessonPageModel
      .extend(lessonStudioServices.slideService),

    LessonPageTemplateModel: ModelWithDevicesProperties.extend({
        defaults: function(){
            return {
                title: '',
                height: '',
                bgColor: '',
                bgImage: '',
                font: '',
                properties: {},
                toBeRemoved: false
            }
        }
    }).extend(lessonStudioServices.slideService),

    LessonPageElementModel: baseLessonStudioModels.LessonPageElementModel
      .extend(lessonStudioServices.slideElementService),

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

    LessonDeviceModel: baseLessonStudioModels.LessonDeviceModel
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
            _.each(response.records, function (record) {
                var tags = _(record.tags).map(function (item) { return item.text });
                record.tagList = tags.join(' • ');
            });
            return response.records;
        }
    }).extend(lessonStudioServices.slideSetCollectionService),

    LessonPageCollection: baseLessonStudioCollections.LessonPageCollection.extend({
          model: lessonStudioModels.LessonPageModel
    }).extend(lessonStudioServices.slideCollectionService),

    LessonPageElementCollection: baseLessonStudioCollections.LessonPageElementCollection.extend({
          model: lessonStudioModels.LessonPageElementModel
    }).extend(lessonStudioServices.slideElementCollectionService),

    LessonPageThemeCollection: Backbone.Collection.extend({
        model: lessonStudioModels.LessonPageThemeModel,
        mode: 'default'
    }).extend(lessonStudioServices.slideThemeCollectionService),

    LessonDeviceCollection: baseLessonStudioCollections.LessonDeviceCollection.extend({
          model: lessonStudioModels.LessonDeviceModel
    }).extend(lessonStudioServices.slideDevicesCollectionService)

};