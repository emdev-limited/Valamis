gradebook.module('Entities', function(Entities, gradebook, Backbone, Marionette, $, _) {

  var CourseModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model) {
          if (gradebook.viewAll)
            return path.api.lessonResults + 'course/' + model.id + '/overview';
          else
            return path.api.lessonResults + 'course/' + model.id + '/user/' + Utils.getUserId() + '/overview';
        },
        'data': {
          courseId: Utils.getCourseId()
        },
        'method': 'get'
      }
    },
    targets: {
      'getCoursesStatistic': {
        'path': function () {
          if (gradebook.viewAll)
            return path.api.lessonResults + 'all-courses/overview';
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

      if (this.showAllCourses)
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
        'path': function (model) {
          return path.api.teacherGrades + 'course/' + gradebook.courseId + '/user/' + model.get('user').id;
        },
        'data': function(model) {
          var params = {
            courseId: Utils.getCourseId(),
            grade: model.get('teacherGrade').grade,
            comment: model.get('teacherGrade').comment
          };

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
        'path': function () {
          if (gradebook.courseId)
            return path.api.lessonResults + 'course/' + gradebook.courseId + '/users';
          else
            return path.api.lessonResults + 'all-courses/users';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count,
            lessonId: options.lessonId
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
        'path': function () {
          if (gradebook.courseId)
            return path.api.lessonResults + 'course/' + gradebook.courseId + '/lessons';
          else
            return path.api.lessonResults + 'all-courses/lessons';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'name',
            page: options.page,
            count: options.count,
            userId: options.userId
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

  var UserLessonModelService = new Backbone.Service({
    url: path.root,
    targets: {
      'setLessonGrade': {
        'path': function (model) {
          return path.api.teacherGrades + 'lesson/' + model.get('lesson').id + '/user/' + model.get('user').id;
        },
        'data': function(model) {
          var params = {
            courseId: Utils.getCourseId(),
            grade: model.get('teacherGrade').grade,
            comment: model.get('teacherGrade').comment
          };

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
        'path': function (collection) {
          if (gradebook.courseId)
            return path.api.lessongrades + 'course/' + gradebook.courseId + '/user/' + collection.userId;
          else
            return path.api.lessongrades + 'all-courses/user/' + collection.userId;
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

  Entities.UserLessonsCollection = valamisApp.Entities.LazyCollection.extend({
    attribute: 'uniqueId',
    model: Entities.UserLessonModel,
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
    hasMore: function(){
      return this.where({ type: 'attempt' }).length < this.total;
    }
  }).extend(UserLessonsCollectionService);

  //

  var LessonUsersCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection) {
          if (gradebook.courseId)
            return path.api.lessongrades + 'course/' + gradebook.courseId + '/lesson/' + collection.lessonId;
          else
            return path.api.lessongrades + 'all-courses/lesson/' + collection.lessonId;
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

  Entities.LessonUsersCollection = valamisApp.Entities.LazyCollection.extend({
    attribute: 'uniqueId',
    model: Entities.UserLessonModel,
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
    hasMore: function(){
      return this.where({ type: 'attempt' }).length < this.total;
    }
  }).extend(LessonUsersCollectionService);

  //

  var GradingCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function () {
          if (gradebook.courseId)
            return path.api.lessongrades + 'in-review/course/' + gradebook.courseId +'/';
          else
            return path.api.lessongrades + 'in-review/all-courses';
        },
        'data': function(collection, options) {
          var params = {
            courseId: Utils.getCourseId(),
            sortBy: 'lastattempted',
            sortAscDirection: false,
            page: options.page,
            count: options.count
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.GradingCollection = valamisApp.Entities.LazyCollection.extend({
    attribute: 'uniqueId',
    model: Entities.UserLessonModel,
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
    hasMore: function(){
      return this.where({ type: 'attempt' }).length < this.total;
    }
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
        'path': function () {
          return path.api.lessongrades + 'last-activity/course/' + gradebook.courseId + '/user/' + Utils.getUserId();
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
          return path.api.gradebooks + 'user/' + collection.userId + '/lesson/' + collection.lessonId + '/statements';
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
      var statements = [];

      _.forEach(response.records, function(record) {

        var attempt = _.extend({
          isAttempt: true
        }, _.omit(record, statements));
        statements.push(attempt);

        statements = statements.concat(record.statements);
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
            count: options.count
          };

          if (gradebook.courseId)
            _.extend(params, { groupId: gradebook.courseId });

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
            count: options.count
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
        'path': function (collection) {
          if (gradebook.courseId)
            return path.api.assignment + 'course/' + gradebook.courseId +  '/user/' + collection.userId;
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