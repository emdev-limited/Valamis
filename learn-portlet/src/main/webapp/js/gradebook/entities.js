gradebook.module('Entities', function(Entities, gradebook, Backbone, Marionette, $, _) {

  Entities.NavigationCollection = Backbone.Collection.extend({
    model: Backbone.Model,
    comparator: 'id',
    updateLevels: function(index, options) {
      if (_.isObject(options)) {
        _.extend(options, { id: index });  // id is level number
        var model = this.at(index);
        if (!model) {
          this.add(options);
          this.filterText = '';
        }
        else {
          model.set(options);
          this.filterText = model.get('filter');
        }
      }

      // remove all items after this index when navigate back
      var that = this;
      while (this.length > index + 1) {
        that.remove(that.at(index+1));
      }
    },
    changeLevel: function(index, options) {
      if (this.at(index))
        this.at(index).set(options);
    }
  });

  var CourseModelService = new Backbone.Service({
    url: path.root,
    targets: {
      'getCoursesStatistic': {
        'path': function (model, options) {
          if (options.currentCourseId)
            return path.api.lessonResults + 'course/' + model.id + '/overview';
          else
            return path.api.lessonResults + 'all-courses/overview';
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      },
      'getUserCoursesStatistic': {
        'path': function (model, options) {
          if (options.currentCourseId)
            return path.api.lessonResults + 'course/' + model.id + '/user/' + Utils.getUserId()
              + '/overview';
          else
            return path.api.lessonResults + 'all-courses/user/' + Utils.getUserId() + '/overview';
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    }
  });

  Entities.CourseModel = Backbone.Model.extend(CourseModelService);

  var CoursesCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.courses + 'list/mySites';
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    }
  });

  Entities.CoursesCollection = Backbone.Collection.extend({
    model: Entities.CourseModel,
    parse: function(response) {
      var records = response.records;

      if (this.showAllCoursesOption)
        records.unshift({ id: '', title: Valamis.language['allCoursesLabel'] });
      return response.records;
    }
  }).extend(CoursesCollectionService);

  var UserModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.users + Utils.getUserId();
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    },
    targets: {
      'setCourseGrade': {
        'path': function (model, options) {
          var url = path.api.teacherGrades + 'course/' + options.currentCourseId
            + '/user/' + model.get('user').id;

          var grade = model.get('teacherGrade').grade;
          url += (!isNaN(grade)) ? '/grade' : '/comment';

          return url;
        },
        'data': function(model) {
          var params = {
            courseId: Utils.getCourseId(),
            comment: model.get('teacherGrade').comment
          };

          var grade = model.get('teacherGrade').grade;
          if (!isNaN(grade)) {
            _.extend(params, { grade: grade });
          }

          return params;
        },
        'method': 'post'
      }
    }
  });

  Entities.UserModel = Backbone.Model.extend(UserModelService);

  var UsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          if (options.currentCourseId)
            return path.api.lessonResults + 'course/' + options.currentCourseId + '/users';
          else
            return path.api.lessonResults + 'all-courses/users';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count,
            lessonId: options.lessonId,
            filter: options.filter
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.UsersCollection = valamisApp.Entities.LazyCollection.extend({
    model: Entities.UserModel
  }).extend(UsersCollectionService);


  var LessonsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          if (options.currentCourseId)
            return path.api.lessonResults + 'course/' + options.currentCourseId + '/lessons';
          else
            return path.api.lessonResults + 'all-courses/lessons';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count,
            userId: options.userId,
            filter: options.filter
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.LessonsCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function(response) {
      this.total = response.total;
      var lessons = response.records;
      _.forEach(lessons, function(record) {
        record.averageGrade = (record.userCount) ? record.grade / record.userCount : NaN
      });
      return lessons;
    }
  }).extend(LessonsCollectionService);

  //

  var LessonAttemptsCollection = valamisApp.Entities.LazyCollection.extend({
    attribute: 'uniqueId',
    parse: function(response) {
      this.total = response.total;
      var coll = [];

      _.forEach(response.records, function(record) {
        var result = _.extend({
          uniqueId: 'result_' + record.user.id + '_' + record.lesson.id,
          type: 'result'
        }, record);

        var attempt = _.extend({
          uniqueId: 'attempt_' + record.user.id + '_' + record.lesson.id,
          type: 'attempt'
        }, record);

        coll.push(result);
        coll.push(attempt);
      });

      return coll;
    },
    // redefine LazyCollection initialize
    initialize: function(models, options){
      options = _.defaults(options || {}, this.defaults);
      _.extend(this, options);

      // update teacherGrade and state in both result and attempt
      this.on('change:teacherGrade', function(model) {
        this.updateModels(model, 'teacherGrade');
      }, this);

      this.on('change:state', function(model) {
        this.updateModels(model, 'state');
      }, this);

      // update total count only once because attempt and result model corresponds to one lesson
      this.on('remove', function(model) {
        if (model.get('type') == 'attempt') {
          this.total = this.total - 1;
          this.trigger('update:total');
        }
      }, this);
    },
    updateModels: function(model, property) {
      var prefix = (model.get('type') == 'attempt') ? 'result_' : 'attempt_';
      this.findWhere({ uniqueId: prefix + model.get('user').id + '_' + model.get('lesson').id })
        .set(property, model.get(property));
    },
    hasMore: function(){
      return this.where({ type: 'attempt' }).length < this.total;
    }
  });

  var UserLessonModelService = new Backbone.Service({
    url: path.root,
    targets: {
      'setLessonGrade': {
        'path': function (model) {
          var url =  path.api.teacherGrades + 'lesson/' + model.get('lesson').id
            + '/user/' + model.get('user').id;

          var grade = model.get('teacherGrade').grade;
          url += (!isNaN(grade)) ? '/grade' : '/comment';

          return url;
        },
        'data': function(model) {
          var params = {
            courseId: Utils.getCourseId(),
            comment: model.get('teacherGrade').comment
          };

          var grade = model.get('teacherGrade').grade;
          if (!isNaN(grade)) {
            _.extend(params, { grade: grade });
          }

          return params;
        },
        'method': 'post'
      }
    }
  });

  Entities.UserLessonModel = Backbone.Model.extend(UserLessonModelService);

  var UserLessonsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          if (options.currentCourseId)
            return path.api.lessongrades + 'course/' + options.currentCourseId
              + '/user/' + collection.userId;
          else
            return path.api.lessongrades + 'all-courses/user/' + collection.userId;
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count,
            filter: options.filter
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.UserLessonsCollection = LessonAttemptsCollection.extend({
    model: Entities.UserLessonModel
  }).extend(UserLessonsCollectionService);

  //

  var LessonUsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          if (options.currentCourseId)
            return path.api.lessongrades + 'course/' + options.currentCourseId + '/lesson/'
              + collection.lessonId;
          else
            return path.api.lessongrades + 'all-courses/lesson/' + collection.lessonId;
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count,
            filter: options.filter
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.LessonUsersCollection = LessonAttemptsCollection.extend({
    model: Entities.UserLessonModel
  }).extend(LessonUsersCollectionService);

  //

  var GradingCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          if (options.currentCourseId)
            return path.api.lessongrades + 'in-review/course/' + options.currentCourseId +'/';
          else
            return path.api.lessongrades + 'in-review/all-courses';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'lastattempted',
            sortAscDirection: false,
            page: options.page,
            count: options.count,
            filter: options.filter
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.GradingCollection = LessonAttemptsCollection.extend({
    model: Entities.UserLessonModel
  }).extend(GradingCollectionService);

  Entities.LastGradingCollection = Backbone.Collection.extend({
    model: Entities.UserLessonModel,
    parse: function(response) {
      return response.records;
    }
  }).extend(GradingCollectionService);

  //

  var LastActivityCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          return path.api.lessongrades + 'last-activity/course/' + options.currentCourseId
            + '/user/' + Utils.getUserId();
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            page: options.page,
            count: options.count
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.LastActivityCollection = Backbone.Collection.extend({
    model: Backbone.Model,
    parse: function(response) {
      return response.records;
    }
  }).extend(LastActivityCollectionService);

  //

  var StatementModelService = new Backbone.Service({
    url: path.root,
    targets: {
      'sendNotification': {
        'path': function (model) {
          return path.api.notifications + 'gradebook/';
        },
        'data': function(model, options) {
          var params = {
            targetId: options.userId,
            courseId: Utils.getCourseId(),
            packageTitle: options.lessonTitle
          };

          return params;
        },
        'method': 'post'
      }
    }
  });

  Entities.StatementModel = Backbone.Model.extend(StatementModelService);

  var StatementsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection) {
          return path.api.gradebooks + 'user/' + collection.userId + '/lesson/'
            + collection.lessonId + '/statements';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.StatementsCollection = valamisApp.Entities.LazyCollection.extend({
    model: Entities.StatementModel,
    parse: function(response) {
      this.total = response.total;
      var statements = [];  // todo duplication

      _.forEach(response.records, function(record) {

        var attempt = _.extend({
          isAttempt: true,
          contextActivity: record.activity
        }, _.omit(record, statements));
        statements.push(attempt);

        var attemptStatements = _.map(record.statements, function(stmt) {
          stmt.contextActivity = record.activity;
          return stmt;
        });
        statements = statements.concat(attemptStatements);
      });

      return statements;
    },
    hasMore: function(){
      return this.where({ isAttempt: true }).length < this.total;
    }
  }).extend(StatementsCollectionService);

  //

  var AssignmentCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function () {
          return path.api.assignment;
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'title',
            status: 'Published',
            page: options.page,
            count: options.count,
            filter: options.filter
          };

          if (options.currentCourseId)
            _.extend(params, { groupId: options.currentCourseId });

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.AssignmentCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function(response) {
      this.total = response.total;
      return response.records;
    }
  }).extend(AssignmentCollectionService);

  var AssignmentUsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection) {
          return path.api.assignment + collection.assignmentId + '/users';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            page: options.page,
            count: options.count,
            filter: options.filter
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.AssignmentUsersCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function(response) {
      this.total = response.total;
      return response.records;
    }
  }).extend(AssignmentUsersCollectionService);

  var UserAssignmentsCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {
          if (options.currentCourseId)
            return path.api.assignment + 'course/' + options.currentCourseId
              + '/user/' + collection.userId;
          else
            return path.api.assignment + 'all-courses/user/' + collection.userId;

        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            page: options.page,
            count: options.count
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.UserAssignmentsCollection = valamisApp.Entities.LazyCollection.extend({
    model: Backbone.Model,
    parse: function(response) {
      this.total = response.total;

      var assignments = [];

      _.forEach(response.records, function(record) {

        var a = _.extend({
          submission: record.users[0].submission
        }, _.omit(record, 'users'));
        assignments.push(a);

      });

      return assignments;
    }
  }).extend(UserAssignmentsCollectionService);

});