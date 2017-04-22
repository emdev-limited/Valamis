var lessonStudioServices = {
    slideSetService: new Backbone.Service({
        url: path.root,
        sync: {
            'delete': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id);
                },
                'data': function (model) {
                    return _.extend({ courseId: Utils.getCourseId() },
                        _.omit(model.toJSON(), ['slides', 'tags', 'logoSrc'])
                    );
                },
                'method': 'delete'
            },
            'update': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id);
                },
                'data': function (model) {
                    return _.extend({
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
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/publish';
                },
                'data': function () {
                    return { courseId: Utils.getCourseId() };
                },
                'method': 'post'
            },
            'clone': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/clone';
                },
                'data': function (model) {
                    return _.extend({ courseId: Utils.getCourseId() },
                        _.omit(model.toJSON(), 'slides')
                    );
                },
                'method': 'post'
            },

            'getLessonById': {
                'path': function (model) {
                    return path.api.slideSets + model.get('id');
                },
                'data': {
                    courseId: Utils.getCourseId()
                },
                'method': 'get'
            },


            'changeLockStatus': {
                'path': function (model) {
                    return path.api.slideSets + model.get('id') + '/change-lock-status';
                },
                'data': function (model, options) {
                    var params = {courseId: Utils.getCourseId()};
                    if (!!options && options.lockUserId && options.lockDate)
                        _.extend(params, {
                            lockUserId: options.lockUserId,
                            lockDate: options.lockDate.toLocaleString()
                        });
                    return params
                },
                'method': 'post'
            },

            'saveTemplate': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/clone';
                },
                'data': function (model) {
                    return _.extend({
                            courseId: Utils.getCourseId(),
                            isTemplate: true
                        },
                        _.omit(model.toJSON(), 'slides')
                    );
                },
                'method': 'post'
            },
            'deleteAllVersions': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/versions';
                },
                'data': function (model) {
                    return _.extend({ courseId: Utils.getCourseId() },
                        _.omit(model.toJSON(), 'slides')
                    );
                },
                'method': 'delete'
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
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/lessonId';
                },
                'data': {courseId: Utils.getCourseId()},
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
                'path': function (model, options) {
                    return path.api.packages + options.lessonId
                },
                'data': {courseId: Utils.getCourseId()},
                'method': 'delete'
            },
            'edit': {
                'path': function (model) {
                    return path.api.slideSets + (model.get('id') || model.id) + '/update-info';
                },
                'data': function (model) {
                    return _.extend({
                            courseId: Utils.getCourseId(),
                            tags: model.getTagIds()
                        },
                        _.omit(model.toJSON(), ['slides', 'tags', 'logoSrc'])
                    );
                },
                'method': 'post'
            },
            'deleteLogo': {
                'path': function (model) {
                    return path.api.files + 'slide-set/' + (model.get('id') || model.id) + '/logo';
                },
                'data': {courseId: Utils.getCourseId()},
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

                    var sortByTypes = lessonStudioCollections.SORT_BY_TYPES;
                    var filter = options.filter || {
                            sort: sortByTypes.DATE_DESC,
                            searchtext: ''
                        };

                    var sortBy = '', asc = '';
                    switch (filter.sort) {
                        case sortByTypes.DATE_DESC:
                            sortBy = 'modifiedDate'; asc = false;
                            break;
                        case sortByTypes.DATE_ASC:
                            sortBy = 'modifiedDate'; asc = true;
                            break;
                        case sortByTypes.NAME_ASC:
                            sortBy = 'name'; asc = true;
                            break;
                        case sortByTypes.NAME_DESC:
                            sortBy = 'name'; asc = false;
                    }

                    return {
                        page: options.currentPage,
                        count: options.itemsOnPage,
                        titleFilter: filter.searchtext || '',
                        sortBy: sortBy,
                        sortAscDirection: asc,
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
                'path': function (model) {
                    return path.api.slides + (model.get('id') || model.id);
                },
                'data': function() {
                    return {courseId: Utils.getCourseId()};
                },
                'method': 'delete'
            },
            'update': {
                'path': function (model) {
                    return path.api.slides + (model.get('id') || model.id);
                },
                'data': function(model) {
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend({ courseId: Utils.getCourseId() },
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
                    return _.extend({ courseId: Utils.getCourseId() },
                        _.omit(model.toJSON(), ['slideElements', 'formData', 'file', 'fileUrl', 'logoSrc', 'fileModel','height']),
                        {properties: properties}
                    );
                },
                'method': 'post'
            }
        },
        'targets': {
            'updateBgImage':{
                'path': function (model) {
                    return path.api.slides + (model.get('id') || model.id) + '/change-bg-image';
                },
                'data': function(model){
                    return {
                        courseId: Utils.getCourseId(),
                        bgImage: model.get('bgImage')
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
                'path': function (model) {
                    return path.api.slideElements + (model.get('id') || model.id);
                },
                'data': function() {
                    return { courseId: Utils.getCourseId() }
                },
                'method': 'delete'
            },
            'update': {
                'path': function (model) {
                    return path.api.slideElements + (model.get('id') || model.id);
                },
                'data': function(model){
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend({ courseId: Utils.getCourseId() },
                            _.omit(model.toJSON(), ['questionModel', 'formData', 'file', 'fileUrl', 'fileModel', 'properties', 'fontSize', 'classHidden', 'fontColor', 'width', 'height', 'top', 'left']),
                            {properties: properties}
                        );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideElements,
                'data': function(model){
                    var properties = !_.isEmpty(model.get('properties'))
                        ? JSON.stringify(model.get('properties'))
                        : '{}';
                    return _.extend({ courseId: Utils.getCourseId() },
                        _.omit(model.toJSON(), ['questionModel', 'formData', 'file', 'fileUrl', 'fileModel', 'properties', 'fontSize', 'classHidden', 'fontColor', 'width', 'height', 'top', 'left']),
                        {properties: properties}
                    );
                },
                'method': 'post'
            }
        }
    }),

    slideElementCollectionService: new Backbone.Service({ url: path.root,
        sync: {
            'read': {
                'path': path.api.slideElements,
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
                'data': { courseId: Utils.getCourseId() },
                'method': 'get'
            },
            'delete': {
                'path': function (model) {
                    return path.api.slideThemes + (model.get('id') || model.id);
                },
                'data': function(model){
                    return _.extend({ courseId: Utils.getCourseId() },
                        model.toJSON()
                    );
                },
                'method': 'delete'
            },
            'update': {
                'path': function (model) {
                    return path.api.slideThemes + (model.get('id') || model.id);
                },
                'data': function(model){
                    return _.extend({ courseId: Utils.getCourseId() },
                        model.toJSON()
                    );
                },
                'method': 'post'
            },
            'create': {
                'path': path.api.slideThemes,
                'data': function(model, options){
                    return _.extend({
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
                'data': function(collection) {
                    return {
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
    }),

    thirdPartySelectorsCollectionService: new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.contentProviders,
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
        },
        cacheLogo: function() {
            var originalLogo = this.get('logo');
            this.set('originalLogo', originalLogo);
        },
        restoreLogo: function() {
            var originalLogo = this.get('originalLogo');
            this.set('logo', originalLogo);
            this.unset('logoSrc');
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


    LessonPageThemeModel: baseLessonStudioModels.LessonPageThemeModel
        .extend(lessonStudioServices.slideThemeService),

    LessonDeviceModel: baseLessonStudioModels.LessonDeviceModel,
    ThirdPartySelectorModel: baseLessonStudioModels.ThirdPartySelectorModel
};

var lessonStudioCollections = {
    SORT_BY_TYPES: {
        DATE_DESC: 'dateDesc',  // newest
        DATE_ASC: 'dateAsc',    // oldest
        NAME_ASC: 'nameAsc',
        NAME_DESC: 'nameDesc'
    },

    Filter: Backbone.Model.extend({
        defaults: {
            searchtext: '',
            sort: 'dateDesc'
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
    }).extend(lessonStudioServices.slideDevicesCollectionService),

    ThirdPartySelectorCollection: baseLessonStudioCollections.ThirdPartySelectorCollection.extend({
        model: lessonStudioModels.ThirdPartySelectorModel
    }).extend(lessonStudioServices.thirdPartySelectorsCollectionService)

};
