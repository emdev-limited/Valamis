learningTranscript.module('Views', function (Views, learningTranscript, Backbone, Marionette, $, _) {

  Views.UserInfoView = Marionette.ItemView.extend({
    template: '#transcriptUserInfoTemplate',
    events: {
      'click .js-select-user': 'selectUser',
      'click .js-print-form': 'printForm'
    },
    selectUser: function () {
      var selectUserView = new LiferayUserSelectDialog({
        singleSelect: true,
        language: Valamis.language
      });
      var selectUserModalView = new valamisApp.Views.ModalView({
        contentView: selectUserView,
        header: Valamis.language['selectUserHeaderLabel']
      });

      selectUserView.on('closeModal', function () {
        valamisApp.execute('modal:close', selectUserModalView);
      });
      selectUserView.on('lfUserSelected', function (model) {
        valamisApp.execute('modal:close', selectUserModalView);
        learningTranscript.execute('show:user:transcript', model.get('id'));
      });

      valamisApp.execute('modal:show', selectUserModalView);
    },

    printForm: function() {
      window.open(this.model.getPrintUrl(), '_blank');
    }
  });

  Views.AssignmentItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#transcriptAssignmentItemTemplate'
  });

  Views.AssignmentsCollectionView = Marionette.CompositeView.extend({
    template: '#transcriptAssignmentsCollectionTemplate',
    childView: Views.AssignmentItemView,
    childViewContainer: 'table tbody'
  });

  Views.LessonItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#transcriptLessonsItemTemplate',
    templateHelpers: function () {
      var autoGrade = Utils.gradeToPercent(this.model.get('autoGrade'));
      var teacherGradeModel = this.model.get('teacherGrade');
      var teacherGrade = Utils.gradeToPercent(
          (teacherGradeModel) ? teacherGradeModel.grade : undefined
      );

      return {
        autoGradeValue: autoGrade || '',
        teacherGradeValue: teacherGrade || ''
      }
    }
  });

  Views.LessonsCollectionView = Marionette.CompositeView.extend({
    template: '#transcriptLessonsCollectionTemplate',
    childView: Views.LessonItemView,
    childViewContainer: 'table tbody'
  });

  Views.CourseItemView = Marionette.LayoutView.extend({
    template: '#transcriptCourseItemTemplate',
    className: 'course-item',
    regions: {
      'lessonsListRegion': '.js-lessons-list',
      'assignmentsListRegion': '.js-assignments-list'
    },
    templateHelpers: function () {
      return {
        timestamp: Date.now(),
        gradeFormatted: Utils.gradeToPercent(this.model.get('grade')) || ''
      }
    },
    events: {
      'click .js-show-details': function () {
        this.toggleDetails(false)
      },
      'click .js-hide-details': function () {
        this.toggleDetails(true)
      }
    },
    onRender: function () {
      var that = this;

      var lessonsCollection = new learningTranscript.Entities.LessonsCollection();
      lessonsCollection.fetch({
        courseId: this.model.get('course').id,
        userId: learningTranscript.selectedUserId
      }).then(function () {
        if (lessonsCollection.length > 0) {
          that.$('.js-show-details').removeClass('hidden');
          var lessonsListView = new Views.LessonsCollectionView({
            collection: lessonsCollection
          });
          that.lessonsListRegion.show(lessonsListView);
        }
      });

      if (learningTranscript.isAssignmentDeployed) {
        var assignmentsCollection = new learningTranscript.Entities.AssignmentsCollection();

        assignmentsCollection.fetch({
          courseId: this.model.get('course').id,
          userId: learningTranscript.selectedUserId
        }).then(function () {
          if (assignmentsCollection.length > 0) {
            that.$('.js-show-details').removeClass('hidden');
            var assignmentsListView = new Views.AssignmentsCollectionView({
              collection: assignmentsCollection
            });
            that.assignmentsListRegion.show(assignmentsListView);
          }
        });
      }
    },
    toggleDetails: function (hideDetails) {
      this.$('.js-show-details').toggleClass('hidden', !hideDetails);
      this.$('.js-hide-details').toggleClass('hidden', hideDetails);
      this.$('.js-lessons-list').toggleClass('hidden', hideDetails);
      this.$('.js-assignments-list').toggleClass('hidden', hideDetails);
    }
  });

  Views.CoursesCollectionView = Marionette.CompositeView.extend({
    template: '#transcriptCoursesCollectionTemplate',
    className: 'transcript-layout',
    childView: Views.CourseItemView,
    childViewContainer: '.js-courses-table'
  });

  Views.CertificateItemView = Marionette.ItemView.extend({
    template: '#transcriptCertificateItemTemplate',
    className: 'certificate-item',
    templateHelpers: function () {
      var achievementDate = this.model.get('achievementDate');
      var expirationDate = this.model.get('expirationDate');
      var logo = this.model.get('logo');
      var logoUrl = (this.model.get('isOpenbadges'))
          ? this.model.get('logo')
          : '/' + path.api.certificates + this.model.get('id') + '/logo?courseId=' + Utils.getCourseId() + '&tmstamp=' + Date.now();

      return {
        achievementDateFormatted: (achievementDate)
            ? $.datepicker.formatDate("dd MM, yy", new Date(achievementDate))
            : '',
        expirationDateFormatted: (expirationDate)
            ? $.datepicker.formatDate("dd MM, yy", new Date(expirationDate))
            : '',
        logoUrl: logoUrl
      }
    },
    events: {
      'click': 'printCertificate'
    },
    onRender: function () {
      if (this.model.get('isOpenbadges')) {
        this.$el.addClass('openbadges')
      }
    },
    printCertificate: function () {
      if (!this.model.get('isOpenbadges')) {
        window.open(this.model.getPrintUrl(this.model, learningTranscript.selectedUserId), '_blank');
      }
    }
  });

  Views.CertificatesCollectionView = Marionette.CompositeView.extend({
    template: '#transcriptCertificatesCollectionTemplate',
    className: 'transcript-layout',
    childView: Views.CertificateItemView,
    childViewContainer: '.js-certificates-list'
  });

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#transcriptLayoutTemplate',
    regions: {
      'userInfoRegion': '#transcriptUserInfo',
      'coursesInfoRegion': '#transcriptCoursesInfo',
      'certificatesInfoRegion': '#transcriptCertificatesInfo'
    },
    onRender: function () {
      var d1 = $.Deferred();
      var d2 = $.Deferred();
      var that = this;

      var userModel = new learningTranscript.Entities.UserModel();
      var userInfoView = new Views.UserInfoView({model: userModel});
      userModel.fetch({userId: learningTranscript.selectedUserId}).then(function () {
        that.userInfoRegion.show(userInfoView);
      });

      $.when(d1, d2).then(function() {
        that.$('.js-loading').addClass('hidden');
      });

      var coursesCollection = new learningTranscript.Entities.CoursesCollection();
      coursesCollection.fetch({userId: learningTranscript.selectedUserId}).then(function () {
        d1.resolve();
        if (coursesCollection.length > 0) {
          var coursesCollectionView = new Views.CoursesCollectionView({
            collection: coursesCollection
          });
          that.coursesInfoRegion.show(coursesCollectionView);
        }
      });

      var certificateCollection = new learningTranscript.Entities.CertificatesCollection();
      certificateCollection.fetch({userId: learningTranscript.selectedUserId}).then(function () {
        d2.resolve();
        if (certificateCollection.length > 0) {
          var certificateCollectionView = new Views.CertificatesCollectionView({
            collection: certificateCollection
          });
          that.certificatesInfoRegion.show(certificateCollectionView);
        }
      });
    }
  });

});