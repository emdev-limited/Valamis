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
            total: 0
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
        ASSIGNMENT: 'Assignment',
        EVENT: 'TrainingEvent'
    };

    Entities.STATUS = {
        NOTSTARTED: 'NotStarted',
        INPROGRESS: 'InProgress',
        SUCCESS: 'Success',
        FAILED: 'Failed'
    };

    Entities.toGoalResponse = function(response) {
        var goalType = Entities.GOAL_TYPE;

        var objectName = '',
          objectTitle = '';

        var id = response.goal.goalId;
        var hasGroup = !!response.goalData.groupId;
        var goal = {
            id: id,
            uniqueId: 'goal_' + (hasGroup ? (response.goalData.groupId + '_' + id) : id),
            groupId: response.goalData.groupId,
            oldGroupId: response.goalData.oldGroupId,
            title: response.goal.title,
            isSubjectDeleted: response.goal.isSubjectDeleted,
            count: response.goal.count,
            arrangementIndex: response.goalData.arrangementIndex,
            isOptional: response.goalData.isOptional,
            type: response.goalData.goalType,
            periodValue: response.goalData.periodValue,
            periodType: response.goalData.periodType,
            certificateId: response.goalData.certificateId,
            modifiedDate: Utils.formatDate(response.goalData.modifiedDate, 'HH:mm, DD.MM.YYYY'),
            isDeleted: response.goal.isDeleted,
            user: response.user
        };

        switch (response.goalData.goalType) {
            case goalType.COURSE:
                goal.goalItemTypeText = Valamis.language['courseLabel'];
                goal.courseLessonsAmount = response.goal.lessonsAmount;
                goal.url = response.goal.url;
                break;
            case goalType.STATEMENT:
                objectName = response.goal.objName;
                objectTitle = objectName
                  ? Utils.getLangDictionaryTincanValue(objectName)
                  : response.goal.obj;
                goal.title = Valamis.language[response.goal.verb] + ' ' + objectTitle;
                goal.goalItemTypeText = Valamis.language['statementLabel'];
                break;
            case goalType.ACTIVITY:
                goal.title = response.goal.title;
                goal.goalItemTypeText = Valamis.language['activityLabel'];
                goal.isActivity = true;
                goal.noDate = _.contains(['participation', 'contribution'], response.goal.activityName);
                break;
            case goalType.PACKAGE:
                goal.goalItemTypeText = Valamis.language['lessonLabel'];
                if (!goal.isSubjectDeleted) {
                    goal.url = Utils.getPackageUrl(response.goal.packageId);
                    if (!!response.goal.course) {
                        goal.packageCourse = {
                            url: response.goal.course.url,
                            title: response.goal.course.title
                        };
                    }
                }
                break;
            case goalType.ASSIGNMENT:
                goal.goalItemTypeText = Valamis.language['assignmentLabel'];
                break;
            case goalType.EVENT:
                goal.goalItemTypeText = Valamis.language['eventLabel'];
                goal.eventInfo = Utils.formatDate(response.goal.startTime, 'l') + ' - '
                    + Utils.formatDate(response.goal.endTime, 'l');
                break;
        }

        return goal;
    };

    Entities.BaseGoalsCollection = Backbone.Collection.extend({
        comparator: function(item) {
            return item.get('arrangementIndex');
        },
        toGroupResponse: function(rawResponse) {
            var groups = [];
            _.each(rawResponse, function (item) {
                var id = item.group.id;
                var group = {
                    id: id,
                    uniqueId: 'group_' + id,
                    isGroup: true,
                    collection: item.group.collection,
                    isDeleted: item.group.isDeleted,
                    count: item.group.count,
                    arrangementIndex: item.group.arrangementIndex,
                    periodValue: item.group.periodValue,
                    periodType: item.group.periodType,
                    certificateId: item.group.certificateId,
                    modifiedDate: Utils.formatDate(item.group.modifiedDate, 'HH:mm, DD.MM.YYYY'),
                    user: item.user
                };

                groups.push(group);
            });

            return groups;
        },
        toGoalResponse: function (rawResponse) {
            var certGoals = [];
            _(rawResponse).map(function (item) {
                return Entities.toGoalResponse(item);
            }).each(function(goal) {
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
                    case goalType.EVENT:
                        goalArray = goalsStatuses.trainingEvents;
                        break;
                }

                var goal = goalArray.filter(function (i) { return i.id == goalId })[0];
                var status = (goal) ? goal.status : '';
                var dateFinish = (goal) ? goal.dateFinish : '';

                return { status: status, dateFinish: dateFinish };
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