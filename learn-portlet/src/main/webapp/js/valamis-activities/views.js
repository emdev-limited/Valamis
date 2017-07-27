valamisActivities.module('Views', function (Views, valamisActivities, Backbone, Marionette, $, _) {

  var OBJECT_TYPE = {
    LESSON: 'Lesson',
    CERTIFICATE: 'Certificate',
    COURSE: 'Course',
    USER_STATUS: 'UserStatus'
  };

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
    className: function () {
      var defaultClassName = 'activity-item';
      return defaultClassName + ((this.model.get('isInGroup')) ? ' grouped' : '');
    },
    regions: {
      'commentsRegion' : '.js-activity-comments',
      'activityGroupRegion': '.js-items'
    },
    events: {
      'focus .js-my-comment-field': function(e) {
        e.stopPropagation();
        this.$('.js-post-my-comment').removeClass('hidden');
      },
      'blur .js-my-comment-field': function(e) {
        e.stopPropagation();
        this.$('.js-post-my-comment').addClass('hidden');
      },
      'keypress .js-my-comment-field': 'onCommentFieldFocus',
      'click .js-action-like': 'toggleLike',
      'click .js-action-comment': 'toggleComment',
      'click .js-action-share': 'shareActivity',
      'click .js-action-delete': 'deleteActivity',
      'click .js-show-liked-users': 'showUsersModal',
      'click .js-toggle-details': 'toggleDetails',
      'click .js-post-my-comment': 'sendComment',
      'mousedown .js-post-my-comment': function(e) {e.preventDefault();}
    },
    childViewContainer: '.js-items',
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
              imageApi = '/' + path.api.packages + this.model.get('obj')['id'] + '/logo?courseId=' + Utils.getCourseId();
              link = Utils.getPackageUrl(this.model.get('obj')['id']);
              break;
          case OBJECT_TYPE.CERTIFICATE:
              imageApi = Liferay.ThemeDisplay.getPortalURL() + "/" + this.model.get('obj')['logo'];
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
        likeItems = _.filter(userLikedList, function(item) {
          return item['id'] !== Valamis.currentUserId;
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
      var userName = this.getUserName().join(' ');

      var activityTitle = this.model.get('obj')['title'];
      if(this.model.get('obj')['liferayEntry']) {
        activityTitle = activityTitle.replace(this.model.get('user')['name'], userName).replace(this.model.get('user')['firstName'], userName);
      }

       var date = '';
       if (!!this.model.get('date')) {
           var lang = Utils.getUserLocale();
           date = moment(this.model.get('date')).locale(lang).fromNow();
       }

      return {
        currentUser: this.options.currentUserModel.toJSON(),
        userName: userName,
        activityTitle: activityTitle,
        activityStmnt: activityStmnt,
        objectClassName: (objectType === OBJECT_TYPE.CERTIFICATE) ? 'certificate' : '',
        withImage: withImage,
        commentText: (commentAmount || '') + ' ' + commentAmountLabel,
        canShare: objectType === OBJECT_TYPE.LESSON,
        canDelete: (objectType === OBJECT_TYPE.USER_STATUS || this.model.get('verb') == 'Shared') &&
        this.model.get('user')['id'] === Valamis.currentUserId,
        actLike: actLike,
        imageApi: imageApi,
        courseId: Utils.getCourseId(),
        objectComment: Utils.makeUrl(this.model.get('obj')['comment'] || ''),
        link: link,
        date: date
      }
    },
    onRender: function() {
      this.commentsCollection = new valamisActivities.Entities.ActivitiesCollection(this.model.get('comments'));

      this.commentsCollection.on('model:deleted', function(modelId) {
        var correctComments =  _.filter(this.model.get('comments'), function(item) {
          return item.id != modelId });
        this.model.set('comments', correctComments);
        this.render();
      }, this);
      var commentsView = new Views.ValamisCommentCollectionView({collection: this.commentsCollection});
      this.commentsRegion.show(commentsView);

      if(this.model.get('isGroup')) {
        var activityGroupView = new Views.ValamisActivitiesCollectionView({
          collection: this.model.get('collection'),
          activitiesCount: this.model.get('collection').length,
          currentUserModel: this.options.currentUserModel,
          isGroup: this.model.get('isGroup')
        });
        this.activityGroupRegion.show(activityGroupView);
      }
    },
    onCommentFieldFocus: function(e) {
      if(e.keyCode === 13) {
        this.sendComment(e);
      }
    },
    sendComment: function(e) {
      e.stopPropagation();
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
    toggleLike: function(e) {
      e.stopPropagation();

      var iLikeThis = this.model.get('currentUserLike');
      var userLikedList = this.model.get('userLiked');
      var $button = this.$('.js-action-like');
      var that = this;
      // disable button while request in progress. enable automatically on render
      $button.css('pointer-events', 'none');
      if (iLikeThis)
        this.model.unlikeActivity().then(function (result) {
          that.model.set({
            userLiked: userLikedList.filter(function(i) {return i['id'] !== Valamis.currentUserId}),
            currentUserLike: false
          });
          that.render();
        }, function (err, res) {
          toastr.error(Valamis.language['failedLabel']);
        });
      else
        this.model.likeActivity().then(function (result) {
          userLikedList.push(that.currentUserModel.toJSON());
          that.model.set({
            userLiked: userLikedList,
            currentUserLike: true
          });
          that.render();
        }, function (err, res) {
          toastr.error(Valamis.language['failedLabel']);
        });
    },
    toggleComment: function(e) {
      e.stopPropagation();
      this.$('.js-activity-comments').toggle();
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
    },
    // Construct the user name(s) string:
    // if the model is a single activity, use the user name provided;
    // if the model is a group:
    //  - join all but last names by "," and join the last name by "and";
    //  - if a group contains more than 3 activities, join the first user's name by "and N others",
    //    where N is the total group user count - 1
    getUserName: function () {
      var userName = [this.model.get('user')['name']];
      if(this.model.get('isGroup')) {
        var userNames = _(this.model.get('collection').toJSON()).map(function (activity) {
          return activity.user.name;
        }).unique().value();
        userName = (userNames.length > 3)
          ? [_.first(userNames), Valamis.language['andLabel'], (userNames.length - 1), Valamis.language['othersLabel']]
          : [_.initial(userNames).join(', '), ((userNames.length > 1) ? Valamis.language['andLabel'] : ''), _.last(userNames)];
      }

      return userName;
    },
    toggleDetails: function(e) {
      var targetButton = $(e.target).closest('.expand');
      var targetGroup = this.$('.activity-group');
      targetButton.toggleClass('open');
      targetGroup.toggleClass('hidden');
    }
  });

  Views.ValamisActivitiesGroupView = Marionette.CompositeView.extend({
    template: '#valamisActivityCollectionViewTemplate',
    childView: Views.ValamisActivityItemView,
    childViewContainer: '.js-list-view',
    childViewOptions: function() {
      return {
        currentUserModel: this.options.currentUserModel
      }
    },
    templateHelpers: function () {
      return {
        isGroup: this.options.isGroup
      }
    },
    initialize: function (options) {
      this.activitiesCount = options.activitiesCount;
      this.resourceURL = options.resourceURL;

      this.collection.on('remove', function() {
        this.$('.js-no-activities').toggleClass('hidden', this.collection.length > 0);
      }, this);

      this.collection.on('activities:add', function (){
        this.$('.js-no-activities').toggleClass('hidden', this.collection.length > 0);
      }, this)
    }
  });

  Views.ValamisActivitiesCollectionView = Views.ValamisActivitiesGroupView.extend({
    events: {
      'click .js-show-more': 'showMore'
    },
    initialize: function(options) {
      this.constructor.__super__.initialize.apply(this, arguments);
      this.isMyActivities = options.isMyActivities;
      this.page = 1;
      this.activitiesCollection = new valamisActivities.Entities.ActivitiesCollection();

      this.collection.on('reset', function(){
        this.page = 1;
        this.fetchCollection();
      }, this);
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

        if(this.$('#activitiesTabs .active a[href="#activitiesRegion"]').length)
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