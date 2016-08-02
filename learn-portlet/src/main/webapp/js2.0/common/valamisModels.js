/**
 * Created by igorborisov on 16.04.15.
 */
valamisApp.module("Entities", function(Entities, valamisApp, Backbone, Marionette, $, _){

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

    Entities.LessonModel = Backbone.Model.extend({
        defaults: {
            selected: false
        },
        toggle: function(){
            var isSelected = this.get('selected');
            this.set('selected', !isSelected);
        }
    });

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

    Entities.AssignmentModel = Backbone.Model.extend({
        defaults: {
            selected: false
        },
        toggle: function(){
            var isSelected = this.get('selected');
            this.set('selected', !isSelected);
        }
    });

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
            total: 0
        },
        initialize: function(models, options){
            options = _.defaults(options || {}, this.defaults);
            _.extend(this, options);
        },
        fetchMore: function(options) {
            options = options || {};
            if (options.reset || options.firstPage) { this.page = 1; }
            else { this.page++; }

            return this.fetch(_.extend({
                page: this.page,
                count: this.itemsPerPage,
                add: true,
                remove: false,
                merge: false
            }, options));
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

    // model and collection for certificate goals

    Entities.GOAL_TYPE = {
        COURSE: 'Course',
        STATEMENT: 'Statement',
        ACTIVITY: 'Activity',
        PACKAGE: 'Package',
        ASSIGNMENT: 'Assignment'
    };

    Entities.STATUS = {
        NOTSTARTED: 'NotStarted',
        INPROGRESS: 'InProgress',
        SUCCESS: 'Success',
        FAILED: 'Failed'
    };

    Entities.BaseGoalsCollection = Backbone.Collection.extend({
        comparator: function(item) {
            return item.get('arrangementIndex');
        },
        toGroupResponse: function(rawResponse) {
            var goalType = Entities.GOAL_TYPE;

            var certGoals = [];
            _.each(rawResponse, function (item) {

                var objectName = '',
                  objectTitle = '';

                var id = item.goal.goalId;
                var goal = {
                    id: id,
                    uniqueId: 'goal_' + id,
                    groupId: item.goalData.groupId,
                    title: item.goal.title,
                    isDeleted: item.goal.isDeleted,
                    count: item.goal.count,
                    arrangementIndex: item.goalData.arrangementIndex,
                    isOptional: item.goalData.isOptional,
                    type: item.goalData.goalType,
                    periodValue: item.goalData.periodValue,
                    periodType: item.goalData.periodType,
                    certificateId: item.goalData.certificateId
                };

                switch (item.goalData.goalType) {
                    case goalType.COURSE:
                        goal.goalItemTypeText = Valamis.language['courseLabel'];
                        goal.courseLessonsAmount = item.goal.lessonsAmount;
                        goal.url = item.goal.url;
                        break;
                    case goalType.STATEMENT:
                        objectName = item.goal.objName;
                        objectTitle = objectName
                          ? Utils.getLangDictionaryTincanValue(objectName)
                          : item.goal.obj;
                        goal.title = Valamis.language[item.goal.verb] + ' ' + objectTitle;
                        goal.goalItemTypeText = Valamis.language['statementLabel'];
                        break;
                    case goalType.ACTIVITY:
                        goal.title = Valamis.language[item.goal.activityName];
                        goal.goalItemTypeText = Valamis.language['activityLabel'];
                        goal.isActivity = true;
                        goal.noDate = _.contains(['participation', 'contribution'], item.goal.activityName);
                        break;
                    case goalType.PACKAGE:
                        goal.goalItemTypeText = Valamis.language['lessonLabel'];
                        if (!goal.isDeleted) {
                            goal.url = Utils.getPackageUrl(item.goal.packageId);
                            if (!!item.goal.course) {
                                goal.packageCourse = {
                                    url: item.goal.course.url,
                                    title: item.goal.course.title
                                };
                            }
                        }
                        break;
                    case goalType.ASSIGNMENT:
                        goal.goalItemTypeText = Valamis.language['assignmentLabel'];
                        break;
                }

                certGoals.push(goal);
            });

            return certGoals;
        },
        setUserStatuses: function(goalsStatuses) {
            var statuses = Entities.STATUS;

            function getGoalStatusAndDateFinish(goalId, type) {
                var goalArray = [];
                var goalType = Entities.GOAL_TYPE;

                switch(type) {
                    case goalType.COURSE:
                        goalArray = goalsStatuses.courses;
                        break;
                    case goalType.STATEMENT:
                        goalArray = goalsStatuses.statements;
                        break;
                    case goalType.ACTIVITY:
                        goalArray = goalsStatuses.activities;
                        break;
                    case goalType.PACKAGE:
                        goalArray = goalsStatuses.packages;
                        break;
                    case goalType.ASSIGNMENT:
                        goalArray = goalsStatuses.assignments;
                        break;
                }

                var goal = goalArray.filter(function (i) { return i.id == goalId })[0];
                var status = (goal) ? goal.status : '';
                var dateFinish = (goal) ? goal.dateFinish : '';

                return { status: status, dateFinish: dateFinish }
            }

            function setStatuses(collection) {

                collection.each(function(goal) {
                    var result = { status: '', dateFinish: '', doneCount: '' };
                    if (goal.get('isGroup')) {
                        setStatuses(goal.get('collection'));

                        // todo set on backend?
                        var coll = goal.get('collection');
                        var failedCount = coll.filter(function(model) {
                            return model.get('status') == statuses.FAILED
                        }).length;
                        var doneCount = coll.filter(function (model) {
                            return model.get('status') == statuses.SUCCESS
                        }).length;
                        var wasFailed = failedCount > (coll.length - goal.get('count'));
                        if (wasFailed)  // group will be failed if failedCount > collection.length
                            result.status = statuses.FAILED;
                        else {
                            var wasSucceed = doneCount >= goal.get('count');
                            result.status = (wasSucceed) ? statuses.SUCCESS : statuses.INPROGRESS;
                            result.doneCount = doneCount;
                        }
                    }
                    else {
                        result = getGoalStatusAndDateFinish(goal.get('id'), goal.get('type'));
                    }

                    goal.set({
                        status: result.status,
                        dateFinish: result.dateFinish,
                        doneCount: result.doneCount
                    });
                });
            }

            setStatuses(this);

            var totalRequired = 0;
            var doneGoals = 0;
            this.each(function(goal) {
                if (!goal.get('isOptional')) totalRequired = totalRequired + 1;
                if (goal.get('status') == statuses.SUCCESS && !goal.get('isOptional')) doneGoals = doneGoals + 1;
                else
                    if (goal.get('isGroup')) {
                        doneGoals = doneGoals + (goal.get('doneCount') / goal.get('count'));
                    }
            });

            this.progress = doneGoals / totalRequired;
        }
    });
});