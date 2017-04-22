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
                        filter: options.filter.searchtext || '',
                        courseId: Utils.getCourseId()
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
                'path': path.api.courses,
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
                        tags: model.getTagIds(),
                        certificateIds: model.getCertificateIds(),
                        longDescription: model.get('longDescription'),
                        beginDate: model.get('beginDate'),
                        endDate: model.get('endDate'),
                        userLimit: model.get('userLimit'),
                        themeId: model.get('themeId'),
                        templateId: model.get('templateId'),
                        instructorIds: model.getInstructorIds(),
                        hasLogo: model.get('hasLogo')
                    };
                }
            },
            'update': {
                'path': function (model, options) {
                    return path.api.courses + model.get('id')
                },
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
                        tags: model.getTagIds(),
                        certificateIds: model.getCertificateIds(),
                        longDescription: model.get('longDescription'),
                        beginDate: model.get('beginDate'),
                        endDate: model.get('endDate'),
                        userLimit: model.get('userLimit'),
                        themeId: model.get('themeId'),
                        templateId: model.get('templateId'),
                        instructorIds: model.getInstructorIds(),
                        hasLogo: model.get('hasLogo')
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
                        ratingScore: options.ratingScore,
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            'deleteRating': {
                'path': path.api.courses + 'unrate/',
                'data': function (model) {
                    var params = {
                        id: model.get('id'),
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            'deleteCourse': {
                'path': function (model, options) {
                    return path.api.courses + model.get('id')
                },
                'data': function (model) {
                    var params = {
                        id: model.get('id'),
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'delete'
            },
            'setTheme': {
                'path': function (model, options) {
                    return path.api.courses + model.get('id') + '/theme'
                },
                'data': function (model) {
                    var params = {
                        id: model.get('id'),
                        courseId: Utils.getCourseId(),
                        themeId: model.get('themeId')
                    };
                    return params;
                },
                'method': 'post'
            },
            'joinCourse': {
                'path': path.api.courses + 'join/',
                'data': function (model) {
                    var params = {
                        id: model.get('id'),
                        courseId: Utils.getCourseId()
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
                        comment: Valamis.language['defaultRequestComment'],
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            'leaveCourse': {
                'path': path.api.courses + 'leave/',
                'data': function (model) {
                    var params = {
                        id: model.get('id'),
                        courseId: Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'post'
            },
            deleteLogo: {
                'path': function (model) {
                    return path.api.files + 'course/' + model.get('id') + '/logo';
                },
                'data': { courseId: Utils.getCourseId() },
                'method': 'delete'
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
            isActive: true,
            courseId: Utils.getCourseId()
        },
        getTagIds: function() {
            //from {id:#, text:###} to ids
            return _(this.get('tags')).map(function (tag) { return tag.id || tag }).value();
        },
        getCertificateIds: function() {
            //from {id:#, text:###} to ids
            return _(this.get('certificates')).map(function (certificate) {
                return certificate.id || certificate
            }).value();
        },
        getInstructorIds: function() {
            return _(this.get('instructors')).map(function (instructor) {
                return instructor.id || instructor
            }).value();
        },
        cacheLogo: function() {
            var originalLogo = this.get('logoUrl');
            this.set('originalLogo', originalLogo);
        },
        restoreLogo: function() {
            var originalLogo = this.get('originalLogo');
            this.set('logoUrl', originalLogo);
            this.unset('logoSrc');
            this.unset('logo');
        },
        hasFreePlaces: function() {
            return !isNaN(this.get('userLimit')) ? this.get('userLimit') > this.get('userCount') : true;
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
                        filter: options.filter.searchtext || '',
                        courseId: Utils.getCourseId()
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
                        filter: options.filter.searchtext || '',
                        courseId: Utils.getCourseId()
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

    // Templates
    var TemplatesService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path':  path.api.courses + 'templates',
                'data': function () {
                    return {
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'get'
            }
        }
    });

    Entities.TemplatesCollection = Backbone.Collection.extend({
        model: Backbone.Model
    }).extend(TemplatesService);

    // Themes
    var ThemesService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.courses + 'themes',
                'data': function () {
                    return {
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'get'
            }
        }
    });

    Entities.ThemesCollection = Backbone.Collection.extend({
        model: Backbone.Model
    }).extend(ThemesService);
    
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
                        count: options.itemsOnPage,
                        courseId: Utils.getCourseId()
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
                        userId: options.userId,
                        courseId: Utils.getCourseId()
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
                        userId: options.userId,
                        courseId: Utils.getCourseId()
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


    //Membership Queue

    var QueueModelService = new Backbone.Service({
        url: path.root
    });

    Entities.QueueModel = Backbone.Model.extend({
        defaults: {
            selected: false
        }
    }).extend(QueueModelService);

    var QueueCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    return path.api.courses + options.filter.courseId + '/queue'
                },
                'data': function (collection, options) {
                    return {
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'get'
            },
        },
        targets: {
            'joinToQueue': {
                path: function (model, options) {
                    return path.api.courses + options.courseId + '/member';
                },
                'data' : function(model, options){
                    var params = {
                        courseId:  Utils.getCourseId(),
                        memberIds : [options.userId],
                        memberType: options.memberType
                    };

                    return params;
                },
                method: 'post'
            },
            'denyFromQueue': {
                path: function (model, options) {
                    return path.api.courses + options.courseId + '/queue';
                },
                'data' : function(model, options){
                    return {
                        courseId:  Utils.getCourseId(),
                        memberIds : [options.userId],
                    };
                },
                method: 'delete'
            }
        }
    });

    Entities.QueueCollection = Backbone.Collection.extend({
        model: Entities.QueueModel,
        parse: function (response) {
            this.trigger('userCollection:updated', { total: response.total, currentPage: response.currentPage });
            return response.records;
        },
        getSelected: function() {
            return this.where({ 'selected': true });
        }
    }).extend(QueueCollectionService);

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

    // certificates
    var CertificateCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.certificates,
                'data': function () {
                    return {
                        courseId: Utils.getCourseId(),
                        sortBy: 'creationDate',
                        sortAscDirection: true,
                        isActive: true,
                    };
                },
                'method': 'get'
            }
        }
    });

    Entities.CertificateCollection = Backbone.Collection.extend({
        model: Backbone.Model,
        parse: function (response) {
            return response.records;
        }
    }).extend(CertificateCollectionService);

    // users
    var UsersCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.users,
                'data': function (collection, options) {
                    return {
                        courseId: Utils.getCourseId(),
                        sortBy: 'name'
                    };
                },
                'method': 'get'
            }
        }
    });

    Entities.UsersCollection = Backbone.Collection.extend({
        model: Backbone.Model,
        parse: function (response) {
            return response.records;
        }
    }).extend(UsersCollectionService);

    // instructors
    var InstructorsCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': function (model, options) {
                    return path.api.courses + options.courseId + '/instructors'
                },
                'data': function (collection, options) {
                    return {
                        courseId: Utils.getCourseId()
                    };
                },
                'method': 'get'
            }
        }
    });

    Entities.InstructorsCollection = Backbone.Collection.extend({
        model: Backbone.Model
    }).extend(InstructorsCollectionService);
});