/**
 * Created by igorborisov on 26.05.14.
 */


lessonManager.module("Entities", function(Entities, lessonManager, Backbone, Marionette, $, _){

    var apiUrl = path.api.packages;

    var packageService = new Backbone.Service({ url: path.root,
        sync: {
            'update' : {
                'path': apiUrl,
                'data': function(model, options){

                        //from {id:#, text:###} to ids
                        var tagIds = [];
                        var modelTags = model.get('tags');
                        for(var i in modelTags) {
                            var tag = modelTags[i];
                            tagIds.push( tag.id || tag )
                        }

                        var params = {  // todo: replace visibility by isVisible
                            action : 'UPDATE',
                            id: model.get('id'),
                            ableToRunFrom: model.get('ableToRunFrom'),
                            ableToRunTo: model.get('ableToRunTo'),
                            attemptCount: model.get('attemptCount'),
                            beginDate: model.get('beginDate'),
                            endDate: model.get('endDate'),
                            description: model.get('description') || "",
                            passingLimit: model.get('passingLimit'),
                            rerunInterval: model.get('rerunInterval'),
                            rerunIntervalType: model.get('rerunIntervalType'),
                            tags: tagIds,
                            isVisible: model.get('isVisible'),
                            title: model.get('title'),
                            courseId: Utils.getCourseId(),
                            requiredReview: model.get('requiredReview'),
                            scoreLimit: model.get('scoreLimit')
                        };

                        return params;
                },
                'method': "post"
            },
            'delete': {
                'path': function(model){ return apiUrl + model.get('id')},
                'data': {courseId: Utils.getCourseId()},
                'method': 'delete'
            }

        },
        targets: {
            updateLogo: {  //TODO: is it needed, it call file service
                'path': apiUrl,
                'data': function (model) {
                    var params = {
                        action: 'UPDATELOGO',
                        id: model.get('id'),
                        logo: model.get('logo'),
                        packageType: model.get('packageType'),
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            deleteLogo: {
                'path': function (model) {
                    return path.api.files + 'lesson/' + model.get('id') + '/logo';
                },
                'data': {courseId: Utils.getCourseId()},
                'method': 'delete'
            },
            'updateVisibility': {
                'path': apiUrl,
                'data': function (model, options) {
                    return {
                        action: 'UPDATE_VISIBLE',
                        id: model.get('id'),
                        isVisible: options.isVisible,
                        courseId: Utils.getCourseId()
                    }
                },
                'method': 'post'
            }
        }
    });

    Entities.Package = Backbone.Model.extend({
        defaults: {
            id: '',
            title: '',
            description: '',
            packageType: '',
            isDefault: false,
            logo:'',
            type:'',
            contextPath: path.root,
            passingLimit: -1,
            rerunInterval: 0,
            rerunIntervalType: 'UNLIMITED',
            ableToRunFrom: '',
            ableToRunTo: '',
            filename: ''
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
    }).extend(packageService);

    var packageCollectionService = new Backbone.Service({ url: path.root,
        sync: {
            'read': {
                'path': apiUrl,
                'data': function(collection, options) {

                    var filter = options.filter || {
                            scope: 'site',
                            sort : 'nameAsc',
                            packageType: '',
                            searchtext: '',
                            selectedCategories: []
                        };

                    var tagId = '';
                    if(filter.selectedCategories && filter.selectedCategories.length >0) {
                        tagId = filter.selectedCategories[0];
                    }

                    var params = {   //TODO: extra parameters (M)
                        action: 'ALL',
                        courseId: Utils.getCourseId(),
                        scope: filter.scope || 'site',
                        //sortBy: sortBy,
                        sortAscDirection: filter.sort == 'nameAsc',
                        filter: filter.searchtext || '',
                        packageType: filter.packageType || '',
                        page: options.currentPage,
                        count: options.itemsOnPage,
                        tagId: tagId
                    };

                    return params;
                },
                'method': "get"
            }
        },
        targets: {
            'removePackages': {
                'path': apiUrl,
                'data': function (collection, options) {
                    var packageIds = collection.map(function (item) {
                        return item.get('id');
                    });
                    var params = {
                        action: 'REMOVEPACKAGES',
                        packageIds: packageIds,
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            'updatePackages': {
                'path': apiUrl,
                'data': function (collection, options) {
                    var packages = JSON.stringify(collection.map(function (item) {
                        return {
                            id: item.get('id'),
                            title: item.get('title') || "New lesson",
                            description: item.get('description') || "",
                            packageType: item.get('packageType'),
                            logo: item.get('logo')
                        };
                    }));

                    var scope = options.scope || 'site';

                    var params = {
                        action: 'UPDATEPACKAGES',
                        packages: packages,
                        scope: scope,
                        courseId: Utils.getCourseId()
                    };

                    return params;
                },
                'method': 'post'
            }
        }
    });

    Entities.PackageCollection = Backbone.Collection.extend({
        model: Entities.Package,
        parse: function(data){
            this.trigger('packageCollection:updated', { total: data.total, currentPage: data.currentPage });

            var lessons = [];

            _.forEach(data.records,function(record){
                var limit = record.limit || {
                        rerunIntervalType: "unlimited"
                    };
                var lesson = {
                    id: record.lesson.id,
                    title: record.lesson.title,
                    description: record.lesson.description,
                    isVisible: record.lesson.isVisible,
                    packageType: record.lesson.lessonType,
                    logo: record.lesson.logo,
                    passingLimit: limit.passingLimit,
                    rerunInterval: limit.rerunInterval,
                    rerunIntervalType: limit.rerunIntervalType,
                    tags: record.tags,
                    beginDate: record.lesson.beginDate,
                    endDate: record.lesson.endDate,
                    creationDate: record.lesson.creationDate,
                    requiredReview: record.lesson.requiredReview,
                    scoreLimit: record.lesson.scoreLimit
                };

                if (record.owner != undefined) lesson.owner = record.owner.name;

                var tags = Array();
                if (lesson.tags) {
                    lesson.tags.forEach(function (item) {
                        tags.push(item.text);
                    });
                }
                lesson.tagsList = tags.join(' • ');

                lessons.push(lesson);
            });

           return lessons;
        }
    }).extend(packageCollectionService);

    Entities.NewPackageCollection = Backbone.Collection.extend({
        model: Entities.Package,
        initialize: function(){
        }
    }).extend(packageCollectionService);

    Entities.Filter = Backbone.Model.extend({
        defaults: {
            scope: 'site',
            packageType: '',
            categories: [],
            searchtext: '',
            sort: 'nameAsc',
            selectedCategories: []
        },
        initialize: function(){
            var that = this;
            this.tags = new Valamis.TagCollection();
            var scope = this.get('scope');
            this.tags.getPackagesTags({}, {courseId: scope =='site' ? Utils.getCourseId() : ''}).then(function(collection) {
                    that.set('categories', collection);
                }
            );
        }
    });

});
