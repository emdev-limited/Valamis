gradebook.module('Views.AssignmentsList', function (AssignmentsList, gradebook, Backbone, Marionette, $, _) {

  AssignmentsList.AssignmentItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookAssignmentItemViewTemplate',
    templateHelpers: function() {
      var deadline = this.model.get('deadline');
      return {
        deadlineFormatted: (deadline) ? gradebook.formatDate(new Date(deadline)) : '',
        isAllCourses: !(gradebook.courseId)
      }
    },
    events: {
      'click .js-assignment-name': 'showAssignment'
    },
    showAssignment: function() {
      this.triggerMethod('assignments:show:users', this.model);
    }
  });

  AssignmentsList.AssignmentCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookAssignmentCollectionViewTemplate',
    childView: AssignmentsList.AssignmentItemView,
    childViewContainer: '.js-items-list',
    templateHelpers: function() {
      return {
        courseName: gradebook.courseModel.get('title')
      }
    }
  });

  AssignmentsList.AssignmentUserItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookAssignmentUserItemViewTemplate',
    templateHelpers: function() {
      return {
        dateFormatted: gradebook.formatDate(new Date(this.model.get('submission').date)),
        organizationList: this.model.get('userInfo').organizations.join(', '),
        statusText: Valamis.language[this.model.get('submission').status + 'Label']
      }
    }
  });

  AssignmentsList.AssignmentUsersCollectionView = Marionette.CompositeView.extend({
    template: '#gradebookAssignmentUsersCollectionViewTemplate',
    childView: AssignmentsList.AssignmentUserItemView,
    childViewContainer: '.js-items-list',
    templateHelpers: function() {
      var deadline = this.model.get('deadline');
      return {
        deadlineFormatted: (deadline) ? gradebook.formatDate(new Date(deadline)) : '',
        isAllCourses: !(gradebook.courseId)
      }
    }
  });

  AssignmentsList.CurrentUserAssignmentItemView = Marionette.ItemView.extend({
    tagName: 'tr',
    template: '#gradebookCurrentUserAssignmentItemViewTemplate',
    templateHelpers: function() {
      var deadline = this.model.get('deadline');
      return {
        deadlineFormatted: (deadline) ? gradebook.formatDate(new Date(deadline)) : '',
        dateFormatted: gradebook.formatDate(new Date(this.model.get('submission').date)),
        statusText: Valamis.language[this.model.get('submission').status + 'Label'],
        isAllCourses: !(gradebook.courseId)
      }
    }
  });

  AssignmentsList.CurrentUserAssignmentsCollectionView = gradebook.Views.ItemsCollectionView.extend({
    template: '#gradebookCurrentUserAssignmentsCollectionViewTemplate',
    childView: AssignmentsList.CurrentUserAssignmentItemView,
    childViewContainer: '.js-items-list',
    templateHelpers: function() {
      return {
        courseName: gradebook.courseModel.get('title')
      }
    }
  });
});