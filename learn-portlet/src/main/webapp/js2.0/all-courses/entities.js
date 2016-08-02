allCourses.module('Entities', function (Entities, allCourses, Backbone, Marionette, $, _) {

    var CourseCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.courses + "list/all",
                'data': function (collection, options) {

                    options.filter = options.filter || {};
                    var sort = options.filter.sort === "nameAsc";

                    var params = {
                        sortAscDirection: sort,
                        page: options.currentPage,
                        count: options.itemsOnPage,
                        filter: options.filter.searchtext || ''
                    };
                    return params;
                },
                'method': 'get'
            }
        }
    });

    var CourseService = new Backbone.Service({
        url: path.root,
        sync: {
            'create': {
                'path': path.api.courses + "create/",
                'method': 'post',
                'data': function(model){
                    return {
                        title: model.get('title'),
                        description: model.get('description'),
                        friendlyUrl: model.get('friendlyUrl'),
                        courseId: Utils.getCourseId(),
                        membershipType: model.get('membershipType'),
                        isMember: model.get('isMember'),
                        isActive: model.get('isActive'),
                        tags: model.getTagIds()
                    };
                }
            },
            'update': {
                'path': path.api.courses + "update/",
                'method': 'put',
                'data': function(model){
                    return {
                        id: model.get('id'),
                        title: model.get('title'),
                        description: model.get('description'),
                        friendlyUrl: model.get('friendlyUrl'),
                        courseId: Utils.getCourseId(),
                        membershipType: model.get('membershipType'),
                        isMember: model.get('isMember'),
                        isActive: model.get('isActive'),
                        tags: model.getTagIds()
                    };
                }
            }
        },
        targets: {
            'setRating': {
                'path': path.api.courses + 'rate/',
                'data': function (model, options) {
                    var params = {
                        id: model.get('id'),
                        ratingScore: options.ratingScore
                    };
                    return params;
                },
                'method': 'post'
            },
            'unsetRating': {
                'path': path.api.courses + 'unrate/',
                'data': function (model) {
                    var params = {
                        id: model.get('id')
                    };
                    return params;
                },
                'method': 'post'
            },
            'deleteCourse': {
                'path': path.api.courses + 'delete/',
                'data': function (model) {
                    var params = {
                        id: model.get('id')
                    };
                    return params;
                },
                'method': 'post'
            },
            'joinCourse': {
                'path': path.api.courses + 'join/',
                'data': function (model) {
                    var params = {
                        id: model.get('id')
                    };
                    return params;
                },
                'method': 'post'
            },
            'requestJoinCourse': {
                'path': path.api.courses + 'requests/add/',
                'data': function (model) {
                    var params = {
                        id: model.get('id'),
                        comment: Valamis.language['defaultRequestComment']
                    };
                    return params;
                },
                'method': 'post'
            },
            'leaveCourse': {
                'path': path.api.courses + 'leave/',
                'data': function (model) {
                    var params = {
                        id: model.get('id')
                    };
                    return params;
                },
                'method': 'post'
            }
        }
    });

    Entities.Course = Backbone.Model.extend({
        defaults: {
            title: '',
            description: '',
            url: '',
            membershipType: 'OPEN',
            isMember:'',
            tags: '',
            isActive: true
        },
        getTagIds: function() {
            //from {id:#, text:###} to ids
            return _(this.get('tags')).map(function (tag) { return tag.id || tag }).value();
        }
    }).extend(CourseService);

    Entities.CourseCollection = valamisApp.Entities.LazyCollection
        .extend({
            model: Entities.Course,
            parse: function(response) {
                this.trigger('courseCollection:updated', { total: response.total, currentPage: response.page });
                this.total = response.total;
                return response.records;
            }
        }).extend(CourseCollectionService);

    Entities.Filter = Backbone.Model.extend({
        defaults: {
            searchtext: '',
            sort: 'nameAsc'
        }
    });

    var UserCourseCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.courses + "list/my",
                'data': function (collection, options) {

                    options.filter = options.filter || {};
                    var sort = options.filter.sort === "nameAsc";

                    var params = {
                        sortAscDirection: sort,
                        page: options.currentPage,
                        count: options.itemsOnPage,
                        filter: options.filter.searchtext || ''
                    };
                    return params;
                },
                'method': 'get'
            }
        }
    });

    Entities.UserCourseCollection = valamisApp.Entities.LazyCollection.extend({
        // if group is organization isMember will be false, but in fact user is a assign to organization
        model: Entities.Course,
        parse: function(response) {
            var records = response.records;
            records.forEach(function(item) { item.isMember = true });
            this.trigger('courseCollection:updated', { total: response.total, currentPage: response.page });
            this.total = response.total;
            return records;
        }
    }).extend(UserCourseCollectionService);

    var NotMemberCourseCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.courses + "list/notmember",
                'data': function (collection, options) {

                    options.filter = options.filter || {};
                    var sort = options.filter.sort === "nameAsc";

                    var params = {
                        sortAscDirection: sort,
                        page: options.currentPage,
                        count: options.itemsOnPage,
                        filter: options.filter.searchtext || ''
                    };
                    return params;
                },
                'method': 'get'
            }
        }
    });

    Entities.NotMemberCourseCollection = valamisApp.Entities.LazyCollection
        .extend({
            model: Entities.Course,
            parse: function(response) {
                this.trigger('courseCollection:updated', { total: response.total, currentPage: response.page });
                this.total = response.total;
                return response.records;
            }
        }).extend(NotMemberCourseCollectionService);

    //Course Members

    var MemberModelService = new Backbone.Service({
        url: path.root,
        sync: {
            'delete': {
                'path': function (model, options) {
                    return path.api.courses + options.courseId + '/users'
                },
                'data': function (model) {
                    return {
                        courseId: Utils.getCourseId(),
                        userIds: model.id
                    }
                },
                'method': 'delete'
            }
        }
    });

    Entities.MemberModel = Backbone.Model.extend({
        defaults: {
            selected: false
        }
    }).extend(MemberModelService);

    var MembersCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    return path.api.courses + options.filter.courseId + '/member'
                },
                'data': function (collection, options) {

                    var order = options.filter.sort;
                    var sortBy = order.split(':')[0];
                    var asc = order.split(':')[1];

                    var params = {
                        //courseId: Utils.getCourseId(),
                        courseId: options.filter.courseId,
                        memberType: options.filter.memberType,
                        sortBy: sortBy,
                        sortAscDirection: asc,
                        filter: options.filter.searchtext,
                        page: options.currentPage,
                        count: options.itemsOnPage
                    };

                    params.action = (options.filter.available) ? 'AVAILABLE_MEMBERS' : 'MEMBERS';

                    var organizationId = options.filter.orgId;
                    if (organizationId)
                        _.extend(params, {orgId: organizationId});

                    return params;
                },
                'method': 'get'
            }
        },
        targets: {
            'deleteFromCourse': {
                'path': function (model, options) {
                    return path.api.courses + options.courseId + '/members'
                },
                'data': function (model, options) {
                    return {
                        courseId: Utils.getCourseId(),
                        memberIds: options.memberIds,
                        memberType: options.memberType
                    };
                },
                method: 'delete'
            },
            saveToCourse: {
                path: function (model, options) {
                    return path.api.courses + options.courseId + '/member';
                },
                'data' : function(model, options){
                    var params = {
                        courseId:  Utils.getCourseId(),
                        memberIds : options.memberIds,
                        memberType: options.memberType
                    };

                    return params;
                },
                method: 'post'
            }
        }
    });

    Entities.MembersCollection = Backbone.Collection.extend({
        model: Entities.MemberModel,
        parse: function (response) {
            this.trigger('userCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        },
        getSelected: function() {
            return this.filter(function(item) { return item.get('selected') });
        }
    }).extend(MembersCollectionService);

    //Membership Requests

    var RequestModelService = new Backbone.Service({
        url: path.root
    });

    Entities.RequestModel = Backbone.Model.extend({
        defaults: {
            selected: false
        }
    }).extend(RequestModelService);

    var RequestCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    return path.api.courses + 'requests'
                },
                'data': function (collection, options) {

                    var order = options.filter.sort;
                    var sortBy = order.split(':')[0];
                    var asc = order.split(':')[1];

                    var params = {
                        id: options.filter.courseId,
                        sortBy: sortBy,
                        sortAscDirection: asc,
                        page: options.currentPage,
                        count: options.itemsOnPage
                    };

                    var organizationId = options.filter.orgId;
                    if (organizationId)
                        _.extend(params, {orgId: organizationId});

                    return params;
                },
                'method': 'get'
            },
        },
        targets: {
            'acceptRequest': {
                'path': function (model, options) {
                    return path.api.courses + 'requests/handle/accept'
                },
                'data': function (model, options) {
                    return {
                        id: options.courseId,
                        memberIds: options.memberIds
                    };
                },
                method: 'post'
            },
            'denyRequest': {
                'path': function (model, options) {
                    return path.api.courses + 'requests/handle/reject'
                },
                'data': function (model, options) {
                    return {
                        id: options.courseId,
                        memberIds: options.memberIds
                    };
                },
                method: 'post'
            }
        }
    });

    Entities.RequestCollection = Backbone.Collection.extend({
        model: Entities.RequestModel,
        parse: function (response) {
            this.trigger('userCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        },
        getSelected: function() {
            return this.filter(function(item) { return item.get('selected') });
        }
    }).extend(RequestCollectionService);

    //Site Roles
    var SiteRoleModelService = new Backbone.Service({
        url: path.root
    });

    Entities.SiteRoleModel = Backbone.Model.extend({
/*        defaults: {
            selected: false
        }*/
    }).extend(SiteRoleModelService);

    var SiteRoleCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    return path.api.courses + 'siteroles'
                },
                'data': function (collection, options) {

                    var params = {
                        id: options.filter.courseId
                    };

                    var organizationId = options.filter.orgId;
                    if (organizationId)
                        _.extend(params, {orgId: organizationId});

                    return params;
                },
                'method': 'get'
            }
        },
        targets: {
            'setSiteRoles': {
                'path': function (model, options) {
                    return path.api.courses + 'siteroles'
                },
                'data': function (model, options) {
                    return {
                        id: options.courseId,
                        memberIds: options.memberIds,
                        siteRoleIds: options.roleIds
                    };
                },
                method: 'post'
            }
        }
    });

    Entities.SiteRoleCollection = Backbone.Collection.extend({
        model: Entities.SiteRoleModel,
        initialize: function(models, options) {
            this.roles = options.roles;
        },
        parse: function (response) {
            var roles = this.roles;
            var superResponse = response.map(function(role) {
                return {
                    id: role.id,
                    description: role.description,
                    name: role.name,
                    selected: roles.indexOf(role.name) >= 0
                };
            });
            return superResponse
        }
        /*parse: function (response) {
            this.trigger('userCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        },
        getSelected: function() {
            return this.filter(function(item) { return item.get('selected') });
        }*/
    }).extend(SiteRoleCollectionService);
});