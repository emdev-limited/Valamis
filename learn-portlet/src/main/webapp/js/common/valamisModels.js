/**
 * Created by igorborisov on 16.04.15.
 */
valamisApp.module("Entities", function(Entities, valamisApp, Backbone, Marionette, $, _){

    Entities.BaseModel = Backbone.Model.extend({
        defaults: {
            selected: false
        },
        toggle: function(){
            var isSelected = this.get('selected');
            this.set('selected', !isSelected);
        }
    });

    Entities.Filter = Backbone.Model.extend({
        defaults: {
            scopeId: '',
            searchtext: '',
            sort: 'name:true'
        }
    });

    Entities.LiferaySiteModel = Backbone.Model.extend({
        defaults: {
            siteID: '',
            title: '',
            url: '',
            description: ''
        }
    });

    var LiferaySiteCollectionService = new Backbone.Service({ url: path.root,
        sync: {
            'read':{
                'path': path.api.courses,
                'data': function (collection, options) {
                    var filter = options.filter || '';
                    var sort = 'true';
                    if (options.sort) sort = options.sort;

                    var result ={
                        filter: filter,
                        sortAscDirection: sort
                    };

                    if(options.currentPage) result.page = options.currentPage;
                    if(options.itemsOnPage) result.count = options.itemsOnPage;

                    return result;
                }
            }
        }
    });

    Entities.LiferaySiteCollection = Backbone.Collection.extend({
        model: Entities.LiferaySiteModel,
        parse: function (response) {
            this.trigger('siteCollection:updated', { total: response.total, currentPage: response.currentPage, listed: response.records.length });
            return response.records;
        }
    }).extend(LiferaySiteCollectionService);

    // lessons model and collection

    Entities.LessonModel = Entities.BaseModel;

    var LessonCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                path: path.api.lesson,
                'data': function (collection, options) {

                    var order = options.filter.sort;
                    var sortBy = order.split(':')[0];
                    var asc = order.split(':')[1];

                    var params = {
                        courseId: Utils.getCourseId(),
                        //sortBy: sortBy,
                        sortAscDirection: asc,
                        filter: options.filter.searchtext,
                        page: options.currentPage,
                        count: options.itemsOnPage
                    };

                    params.action = options.filter.action || 'ALL';

                    if (options.filter.packageType != undefined) params.packageType = options.filter.packageType;
                    if (options.filter.scope != undefined) params.scope = options.filter.scope;
                    if (options.filter.playerId != undefined) params.playerId = options.filter.playerId;

                    var tagId = options.filter.tagId;
                    if (tagId)
                        _.extend(params, {tagId: tagId});

                    return params;
                },
                'method': 'get'
            }
        }
    });

    Entities.LessonCollection = Backbone.Collection.extend({
        model: Entities.LessonModel,
        parse: function (response) {
            this.trigger('lessonCollection:updated', { total: response.total, currentPage: response.currentPage });

            var record = response.records[0];
            // todo ugly code because this collection is used for different services
            if (record != undefined && record.lesson != undefined && record.id == undefined) {
                var lessons = [];
                _.forEach(response.records, function(record){
                    lessons.push(record.lesson);
                });
                return lessons;
            }
            else
                return response.records;
        }
    }).extend(LessonCollectionService);

    Entities.ViewerModel = Backbone.Model;

    var ViewerCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.packages,
                'data': function (collection, options) {
                    var filter = options.filter;

                    var sort = filter.sort.split(':');
                    var sortBy = sort[0];
                    var asc = sort[1];

                    var params = {
                        id: filter.packageId,
                        viewerType: filter.viewerType,
                        orgId: filter.orgId,
                        courseId: Utils.getCourseId(),
                        filter: filter.searchtext,
                        sortAscDirection: asc,
                        page: options.currentPage,
                        count: options.itemsOnPage
                    };

                    params.action = (filter.available) ? 'AVAILABLE_MEMBERS' : 'MEMBERS';

                    return params;
                },
                'method': 'get'
            }
        },
        targets: {
            'addViewers': {
                'path': path.api.packages,
                'data': function(collection, options) {
                    return {
                        action: 'ADD_MEMBERS',
                        courseId: Utils.getCourseId(),
                        viewerType: options.viewerType,
                        id: options.packageId,
                        viewerIds: options.viewerIds
                    }
                },
                'method': 'post'
            },
            'deleteViewers': {
                'path':path.api.packages,
                'data': function(collection, options) {
                    return {
                        action: 'REMOVE_MEMBERS',
                        courseId: Utils.getCourseId(),
                        viewerType: options.viewerType,
                        id: options.packageId,
                        viewerIds: options.viewerIds
                    }
                },
                'method': 'post'
            }
        }
    });

    // assignments model and collection

    Entities.AssignmentModel = Entities.BaseModel;

    var AssignmentCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                path: path.api.assignment,
                'data': function (collection, options) {

                    var order = options.filter.sort;
                    var sortBy = order.split(':')[0];
                    var asc = order.split(':')[1];

                    var params = {
                        courseId: Utils.getCourseId(),
                        sortBy: sortBy,
                        sortAscDirection: asc,
                        filter: options.filter.searchtext,
                        page: options.currentPage,
                        count: options.itemsOnPage
                    };

                    params.action = options.filter.action || 'ALL';

                    if (options.status)
                      params.status = options.status;

                    return params;
                },
                'method': 'get'
            }
        }
    });

    Entities.AssignmentCollection = Backbone.Collection.extend({
        model: Entities.AssignmentModel,
        parse: function (response) {
            this.trigger('assignmentCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        }
    }).extend(AssignmentCollectionService);

    Entities.ViewersCollection = Backbone.Collection.extend({
        model: Entities.ViewerModel,
        parse: function (response) {
            this.trigger('viewerCollection:updated', { total: response.total });
            return response.records;
        }
    }).extend(ViewerCollectionService);

    Entities.LazyCollection = Backbone.Collection.extend({
        defaults: {
            page: 0,
            itemsPerPage: 10,
            total: 0,
            useSkipTake: false
        },
        initialize: function(models, options){
            options = _.defaults(options || {}, this.defaults);
            _.extend(this, options);
        },
        fetchMore: function(options) {
            this.trigger('fetchingMore');

            options = options || {};
            if (options.reset || options.firstPage) { this.page = 1; }
            else { this.page++; }

            var paging = {};
            if (!this.useSkipTake) {
                paging.page = this.page;
                paging.count = this.itemsPerPage;
            }
            else {
                paging.skip = (this.page - 1) * this.itemsPerPage;
                paging.take = this.itemsPerPage;
            }

            var params = _.extend({
                add: true,
                remove: false,
                merge: false
            }, options, paging);

            return this.fetch(params);
        },
        parse: function (response) {
            this.total = response.total;
            return response.records;
        },
        hasItems: function(){
            return this.total > 0;
        },
        hasMore: function(){
            return this.length < this.total
        }
    });
});