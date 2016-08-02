lessonViewer.module('Entities', function(Entities, lessonViewer, Backbone, Marionette, $, _) {

    var packageService = new Backbone.Service({
        url: path.root,
        targets: {
            sharePackage: {
                'path': path.api.activities,
                'data': function (model, options) {
                    var params = {
                        action: 'SHARELESSON',
                        packageId: model.get('id'),
                        comment: options.comment,
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            ratePackage: {
                'path': path.api.packages + 'rate/',
                'data': function (model, options) {
                    var params = {
                        action: 'UPDATERATING',
                        id: model.get('id'),
                        ratingScore: options.ratingScore
                    };
                    return params;
                },
                'method': 'post'
            },
            deletePackageRating: {
                'path': path.api.packages + 'rate/',
                'data': function (model) {
                    var params = {
                        action: 'DELETERATING',
                        id: model.get('id')
                    };
                    return params;
                },
                'method': 'post'
            }
        }
    });

    Entities.Package = Backbone.Model.extend({
        defaults: {}
    }).extend(packageService);

    var packageCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.packages,
                'data': function (collection, options) {
                    var filter = options.filter;
                    var order = filter.sort;
                    var sortBy = order.split(':')[0];
                    var asc = order.split(':')[1];

                    var tagId = '';
                    if(filter.selectedCategories && filter.selectedCategories.length > 0) {
                        tagId = filter.selectedCategories[0];
                    }

                    var params = {
                        action: 'VISIBLE',
                        courseId: Utils.getCourseId(),
                        playerId: options.playerId,
                        filter: filter.searchtext,
                        sortBy: sortBy,
                        sortAscDirection: asc,
                        tagId: tagId
                    };

                    if (options.currentPage != undefined) params.page = options.currentPage;
                    if (options.itemsOnPage != undefined) params.count = options.itemsOnPage;

                    return params;
                },
                'method': 'get'
            }
        },
        targets: {
            updateIndex: {
                'path': path.api.packages + 'order/',
                'data': function (model, options) {
                    var params = {
                        playerId: options.playerId,
                        packageIds: options.index
                    };
                    return params;
                },
                'method': 'post'
            }
        }
    });

    Entities.PackageCollection = Backbone.Collection.extend({
        model: Entities.Package,
        parse: function (data) {
            this.trigger('packageCollection:updated', { total: data.total, currentPage: data.currentPage });

            return _.map(data.records, function (record) {
                var limit = record.limit || {
                    passingLimit: 0,
                    rerunInterval: 0,
                    rerunIntervalType: 'UNLIMITED'
                };
                var lesson = {
                    id: record.lesson.id,
                    title: record.lesson.title,
                    description: record.lesson.description,
                    packageType: record.lesson.lessonType,
                    logo: record.lesson.logo,
                    status: record.state || 'none',
                    tags: record.tags,
                    rating: record.rating,
                    passingLimit: limit.passingLimit,
                    rerunInterval: limit.rerunInterval,
                    rerunIntervalType: limit.rerunIntervalType,
                    attemptsCount: record.attemptsCount,
                    suspendedId: record.suspendedId,
                    creationDate: record.lesson.creationDate,
                    ownerId: record.lesson.ownerId
                };
                if (record.owner != undefined) lesson.owner = record.owner.name;

                return lesson;
            });
        }
    }).extend(packageCollectionService);

    Entities.Filter = Backbone.Model.extend({
        defaults: {
            searchtext: '',
            sort: 'default:true',
            selectedCategories: [],
            isShowingAll: false
        },
        initialize: function(){
            var that = this;
            this.tags = new Valamis.TagCollection();
            var scope = this.get('scope');
            this.tags.getPackagesTags({}, {
                courseId: Utils.getCourseId(),
                playerId: lessonViewer.playerId
            }).then(function(collection) {
                    that.set('categories', collection);
                }
            );
        }
    });
});