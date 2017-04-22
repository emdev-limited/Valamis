valamisActivities.module('Views', function (Views, valamisActivities, Backbone, Marionette, $, _) {

  var OBJECT_TYPE = {
    LESSON: 'Lesson',
    CERTIFICATE: 'Certificate',
    COURSE: 'Course',
    USER_STATUS: 'UserStatus'
  };

  var ACTIVITIES_COUNT = 30;

  Views.UserStatusView = Marionette.ItemView.extend({
    template: '#userStatusViewTemplate',
    className: 'activity-item user-status',
    events: {
      'click .js-post-status': 'postStatus'
    },
    postStatus: function() {
      var that = this;

      var userStatus = this.$('.js-user-status').val();
      if (userStatus) {
        this.model.postStatus({}, {content: userStatus}).then(function (result) {
          that.triggerMethod('activities:addactivity', result);
          that.render();
        }, function (err, res) {
          toastr.error(Valamis.language['failedLabel']);
        });
      }
    }
  });

  Views.UsersLikedItemView = Marionette.ItemView.extend({
    template: '#valamisActivityUsersLikedItemViewTemplate',
    tagName: 'tr'
  });

  Views.UsersLikedCollectionView = Marionette.CollectionView.extend({
    childView: Views.UsersLikedItemView,
    tagName: 'table',
    className: 'val-table medium list'
  });

  Views.ValamisCommentItemView = Marionette.ItemView.extend({
    template: '#valamisCommentItemViewTemplate',
    className: 'comment-item',
    events: {
      'click .js-delete-comment': 'deleteComment'
    },

    templateHelpers: function() {
      return {
        canDelete: (this.model.get('user')['id'] === Valamis.currentUserId),
        commentContent: Utils.makeUrl(this.model.get('content')),
        isDeleted: this.model.get('user')['isDeleted']
      }
    },
    deleteComment: function() {
      var that = this;
      var modelId = this.model.get('id');
      this.model.deleteComment().then(function (result) {
        that.model.trigger('model:deleted', modelId);
      }, function (err, res) {
        toastr.error(Valamis.language['failedLabel']);
      });
      this.destroy();
    }
  });

  Views.ValamisCommentCollectionView = Marionette.CollectionView.extend({
    childView: Views.ValamisCommentItemView,
    className: 'comments-block'
  });

  Views.ValamisActivityItemView = Marionette.LayoutView.extend({
    template: '#valamisActivityItemViewTemplate',
    className: 'activity-item',
    regions: {
      'commentsRegion' : '.js-activity-comments'
    },
    events: {
      'focus .js-my-comment-field': function() {this.$('.js-post-my-comment').show();},
      'blur .js-my-comment-field': function() {this.$('.js-post-my-comment').hide();},
      'keypress .js-my-comment-field': 'keyAction',
      'click .js-action-like': 'toggleLike',
      'click .js-action-comment': function() {this.$('.js-activity-comments').toggle();},
      'click .js-action-share': 'shareActivity',
      'click .js-action-delete': 'deleteActivity',
      'click .js-show-liked-users': 'showUsersModal'
    },
    initialize: function(options) {
      this.currentUserModel = options.currentUserModel;
    },
    templateHelpers: function() {
      var commentAmount = this.model.get('comments').length;
      var commentAmountLabel = (commentAmount > 1) ? Valamis.language['commentsLabel'] : Valamis.language['commentLabel'];
      var link = '';
      var imageApi = '';
      switch (this.model.get('obj')['tpe']) {
        case OBJECT_TYPE.LESSON:
          imageApi = path.api.packages;
          link = Utils.getPackageUrl(this.model.get('obj')['id']);
          break;
        case OBJECT_TYPE.CERTIFICATE:
          imageApi = path.api.certificates;
          link = Utils.getCertificateUrl(this.model.get('obj')['id']);
          break;
        case OBJECT_TYPE.COURSE:
          var logo = this.model.get('obj').logoCourse;
          imageApi = (logo) ? (Liferay.ThemeDisplay.getPathImage() + logo) : '';
          break;
      }

      var activityStmnt = (this.model.get('obj')['tpe'] !== OBJECT_TYPE.USER_STATUS)
      ? (Valamis.language[this.model.get('verb') + 'VerbLabel']) + ' ' + Valamis.language[this.model.get('obj')['tpe'].toLowerCase() + 'ActivityLabel']
      : '';


      var userLikedList = this.model.get('userLiked');
      var likesAmount = userLikedList.length;
      var iLikeThis = this.model.get('currentUserLike');

      var actLike = {};
      actLike.verb = (likesAmount === 1 && !iLikeThis) ? Valamis.language['likesThisLabel'] : Valamis.language['likeThisLabel'];
      actLike.isLink = false;
      var likeItems = ['',''];

      if (likesAmount > 2) {
        actLike.isLink = true;
        if (iLikeThis) {
          likeItems[1] = Valamis.language['youLabel'];
          likeItems[0] = (likesAmount - 1) + ' ' + Valamis.language['peopleLabel'];
        } else {
          likeItems[0] = likesAmount + ' ' + Valamis.language['peopleLabel'];
        }
      }
      else {
        likeItems = userLikedList.filter(function(item) {
          return item['id'] !== Valamis.currentUserId
        }).map(function(item) {
          return item['name'];
        });
        if (iLikeThis)
          likeItems.push(Valamis.language['youLabel']);
      }

      actLike.firstItem = likeItems[1];
      actLike.secondItem = likeItems[0];

      var objectType = this.model.get('obj')['tpe'];
      var withImage = this.model.get('obj')['withImage'];

      return {
        currentUser: this.options.currentUserModel.toJSON(),
        activityStmnt: activityStmnt,
        objectClassName: objectType.toLowerCase(),
        withImage: withImage,
        commentText: (commentAmount || '') + ' ' + commentAmountLabel,
        canShare: objectType === OBJECT_TYPE.LESSON,
        canDelete: (objectType === OBJECT_TYPE.USER_STATUS || this.model.get('verb') == 'Shared')
        && this.model.get('user')['id'] === Valamis.currentUserId,
        actLike: actLike,
        imageApi: imageApi,
        courseId: Utils.getCourseId,
        objectComment: Utils.makeUrl(this.model.get('obj')['comment'] || ''),
        link: link
      }
    },
    onRender: function() {
      this.commentsCollection = new valamisActivities.Entities.ActivitiesCollection(this.model.get('comments'));

      this.commentsCollection.on('model:deleted', function(modelId) {
        var correctComments =  _.filter(this.model.get('comments'), function(item) {
          return item.id != modelId });
        this.model.set('comments',correctComments);
        this.render();
      }, this);
      var commentsView = new Views.ValamisCommentCollectionView({collection: this.commentsCollection});
      this.commentsRegion.show(commentsView);

      var that = this;
      this.$('.js-post-my-comment').on('mousedown', function(event) {
        event.preventDefault();
      }).on('click', function() {
        that.sendComment();
      });
    },
    keyAction: function(e) {
      if(e.keyCode === 13) {
        this.sendComment();
      }
    },
    sendComment: function() {
      var that = this;
      var comment = that.$('.js-my-comment-field').val();
      var $button = that.$('.js-post-my-comment');

      if (comment) {
        $button.prop('disabled', true);
        that.model.commentActivity({}, {content: comment}).then(function (result) {
          (that.model.get('comments')).push(result);
          $button.prop('disabled', false);
          that.render();
        }, function (err, res) {
          toastr.error(Valamis.language['failedLabel']);
        });
      }
    },
    toggleLike: function() {
      var iLikeThis = this.model.get('currentUserLike');
      var userLikedList = this.model.get('userLiked');
      var $button = this.$('.js-action-like');
      var that = this;
      // disable button while request in progress. enable automatically on render
      $button.css('pointer-events', 'none');
      if (iLikeThis)
        this.model.unlikeActivity().then(function (result) {
          that.model.set('userLiked', userLikedList.filter(function(i) {return i['id'] !== Valamis.currentUserId}));
          that.model.set('currentUserLike', false);
          that.render();
        }, function (err, res) {
          toastr.error(Valamis.language['failedLabel']);
        });
      else
        this.model.likeActivity().then(function (result) {
          userLikedList.push(that.currentUserModel.toJSON());
          that.model.set('userLiked', userLikedList);
          that.model.set('currentUserLike', true);
          that.render();
        }, function (err, res) {
          toastr.error(Valamis.language['failedLabel']);
        });
    },
    showUsersModal: function() {
      var usersLikedView = new Views.UsersLikedCollectionView({
        collection: new valamisActivities.Entities.LiferayUserCollection(this.model.get('userLiked'))
      });

      var usersLikedModalView = new valamisApp.Views.ModalView({
        contentView: usersLikedView,
        header: Valamis.language['usersLikedLabel'],
        customClassName: 'valamis-activities-users-liked'
      });

      valamisApp.execute('modal:show', usersLikedModalView);
    },
    shareActivity: function() {
      var $button = this.$('.js-action-share');
      $button.css('pointer-events', 'none');
      var that = this;
      this.model.shareActivity().then(function (result) {
        that.triggerMethod('activities:addactivity', result);
        $button.css('pointer-events', 'auto');
      }, function (err, res) {
        toastr.error(Valamis.language['failedLabel']);
      });
    },
    deleteActivity: function() {
      this.model.destroy();
    }
  });

  Views.ValamisActivitiesCollectionView = Marionette.CompositeView.extend({
    template: '#valamisActivityCollectionViewTemplate',
    childView: Views.ValamisActivityItemView,
    childViewContainer: '.js-list-view',
    childViewOptions: function() {
      return {
        currentUserModel: this.options.currentUserModel
      }
    },
    events: {
      'click .js-show-more': 'showMore'
    },
    initialize: function(options) {
      this.isMyActivities = options.isMyActivities;
      this.activitiesCount = options.activitiesCount;
      this.resourceURL = options.resourceURL;
      this.page = 1;
      this.activitiesCollection = new valamisActivities.Entities.ActivitiesCollection();

      this.collection.on('reset', function(){
        this.page = 1;
        this.fetchCollection();
      }, this);

      this.collection.on('remove', function() {
        this.$('.js-no-activities').toggleClass('hidden', this.collection.length > 0);
      }, this);

      this.collection.on('activities:add', function (){
        this.$('.js-no-activities').toggleClass('hidden', this.collection.length > 0);
      }, this)
    },
    onRender: function() {
      this.activitiesCollection.on('sync', function() {
        this.collection.add(this.activitiesCollection.toJSON());

        if (this.collection.length === 0)
          this.$('.js-no-activities').removeClass('hidden');

        this.$('.js-show-more').toggleClass('hidden', this.activitiesCollection.length === 0);

      }, this);
    },
    fetchCollection: function() {
      this.activitiesCollection.fetch({
        page: this.page,
        count: this.activitiesCount,
        getMyActivities: this.isMyActivities,
        resPath: this.resourceURL
      });
    },
    showMore: function() {
      this.page++;
      this.fetchCollection();
    }
  });

  Views.AppLayoutView = Marionette.LayoutView.extend({
    template: '#activitiesLayoutTemplate',
    className: 'val-activities',
    regions: {
      'statusRegion' : '#statusRegion',
      'activitiesRegion' : '#activitiesRegion',
      'myActivitiesRegion': '#myActivitiesRegion'
    },
    childEvents: {
      'activities:addactivity':function(childView, activity){
        if (activity['id'] == 0)
          delete activity['id'];

        if(this.$('#activitiesTabs .active a[href="#activitiesRegion"]').size())
          this.addInCollection(this.allActivitiesCollection, activity);
        else
          this.addInCollection(this.myActivitiesCollection, activity);
      }
    },
    addInCollection: function(collection, activity){
      collection.unshift(activity);
      if (collection.length === 1)
        collection.trigger('activities:add')
    },
    events: {
      'click li a[href="#activitiesRegion"]': 'resetAllActivities',
      'click li a[href="#myActivitiesRegion"]': 'resetMyActivities'
    },
    initialize: function(options) {
      this.currentUserModel = options.currentUserModel;
      this.resourceURL = options.resourceURL;
      this.activitiesCount = options.activitiesCount;
      this.allActivitiesCollection = new valamisActivities.Entities.ActivitiesCollection();
      this.myActivitiesCollection = new valamisActivities.Entities.ActivitiesCollection();
    },
    onRender: function () {
      var statusView = new Views.UserStatusView({model: this.currentUserModel});
      this.statusRegion.show(statusView);

      var allActivitiesView = new Views.ValamisActivitiesCollectionView({
        resourceURL: this.resourceURL,
        activitiesCount: this.activitiesCount,
        collection: this.allActivitiesCollection,
        currentUserModel: this.currentUserModel,
        isMyActivities: false
      });
      this.activitiesRegion.show(allActivitiesView);

      var myActivitiesView = new Views.ValamisActivitiesCollectionView({
        resourceURL: this.resourceURL,
        activitiesCount: this.activitiesCount,
        collection: this.myActivitiesCollection,
        currentUserModel: this.currentUserModel,
        isMyActivities: true
      });
      this.myActivitiesRegion.show(myActivitiesView);

      this.resetAllActivities();
    },
    resetAllActivities: function() {
      this.allActivitiesCollection.reset();
    },
    resetMyActivities: function() {
      this.myActivitiesCollection.reset();
    }
  });

});