var LessonViewer = Marionette.Application.extend({
    channelName: 'lessonViewer',
    initialize: function() {
        this.addRegions({
            mainRegion: '#listRegion',
            playerRegion: '#playerRegion'
        });

        this.settings = new SettingsHelper({url: window.location.href, portlet: 'lessonViewer'});
        this.settings.fetch();
    },
    onStart: function(options) {
        _.extend(this, options);

        var previousFilter = this.settings.get('searchParams');
        this.filter = new lessonViewer.Entities.Filter(previousFilter);

        this.filter.on('change', function(){
            lessonViewer.execute('packages:reload', true);
            layoutView.toggleSortable();
            lessonViewer.settings.set('searchParams', _.omit(this.filter.toJSON(), 'isShowingAll'));
            lessonViewer.settings.save();
        }, this);

        this.packages = new lessonViewer.Entities.PackageCollection();
        this.paginatorModel = new PageModel({'allowShowAll': Valamis.permissions.LessonViewer.LV_ORDER});

        var layoutView = new lessonViewer.Views.AppLayoutView();
        this.mainRegion.show(layoutView);

        lessonViewer.isLocalStorageAvailable = true;
        // Safari, in Private Browsing Mode, looks like it supports localStorage but all calls to setItem
        // throw QuotaExceededError. We're going to detect this and just silently drop any calls to setItem
        // to avoid the entire page breaking, without having to do a check at each usage of Storage.
        if (typeof localStorage === 'object') {
            try {
                localStorage.setItem('localStorage', 1);
                localStorage.removeItem('localStorage');
            } catch (e) {
                this.isLocalStorageAvailable = false;
                toastr.warning('Your web browser does not support storing settings locally. ' +
                    'Package sorting will not work in "Private Browsing Mode".');
            }
        }

        // checking for auto start lesson goes below
        // first priority: opening lesson by link, second: opening default lesson
        // third: open unfinished lesson for current lesson viewer (info is store in browser cache)

        var linkHash = window.location.hash;

        // lesson is opened by link
        if (!!linkHash) {
            window.location.hash = window.location.hash.replace(/(#\/lesson\/\d+\/.+\/)(.+)(\/.+)/,
              function (str, prefix, title, suffix) {
                  var encodedTitle = _.map(title.split('%'), function (part) {
                      return encodeURIComponent(part);
                  }).join('%');
                  return prefix + encodedTitle + suffix;
              });
        }
        else {
            // there is default lesson
            if (this.lessonToStartId) {
                window.location.href = this.getLessonLink(this.lessonToStartId,
                  this.lessonToStartType, this.lessonToStartTitle, false);
            }
            else {
                // lesson in this lesson viewer was not finished
                if (this.isLocalStorageAvailable) {
                    if (window.localStorage.getItem('playerSettings') !== null) {
                        var pdata = JSON.parse(window.localStorage.getItem('playerSettings'));

                        if(!isNaN(parseInt(pdata.id)) && parseInt(pdata['playerId']) == lessonViewer.playerId ){
                            window.location.href = this.getLessonLink(pdata['id'], pdata['type'],
                              pdata['title'], pdata['isSuspended']);
                        } else {
                            window.localStorage.removeItem('playerSettings');
                        }
                    }
                }
            }
        }

        var Router = Backbone.Router.extend({
            routes: {
                ''     : 'index',
                'lesson/:id/:type/:title/:isSuspended' : 'lesson'
            },

            index: function(){},

            lesson: function(id, type, title, isSuspended){
                lessonViewer.execute('player:start', id, title, type, isSuspended);
            }
        });
        this.router = new Router();

        if (!Backbone.History.started) Backbone.history.start();
    },
    scormGetNext: function () {
        lessonViewer.playerLayoutView.doContinue();
    },
    scormGetPrev: function () {
        lessonViewer.playerLayoutView.doPrevious();
    },
    scormJump: function (target) {
        lessonViewer.playerLayoutView.doJump(target);
    },
    getLessonLink: function(lessonId, lessonType, lessonTitle, isSuspended) {
        var winHref = window.location.href;
        var shIndex = winHref.indexOf('#');
        var href = (shIndex > -1) ? winHref.substr(0, shIndex) : winHref;

        return href + '#/lesson/' + lessonId + '/' + lessonType
          + '/' + encodeURIComponent(lessonTitle) + '/' + isSuspended;
    }
});

var lessonViewer = new LessonViewer();

lessonViewer.commands.setHandler('packages:reload', function(filterChanged){

    var filter = lessonViewer.filter.toJSON();

    lessonViewer.isSortDisabled = !!filter.searchtext ||
                                  !_.isEmpty(filter.selectedCategories[0]) ||
                                  filter.sort != lessonViewer.filter.defaults.sort ||
                                  !Valamis.permissions.LessonViewer.LV_ORDER;

    var params = {
        reset: true,
        playerId: lessonViewer.playerId,
        filter: filter
    };

    if(filterChanged) {
        lessonViewer.paginatorModel.set('currentPage', 1);
    }

    if (!filter.isShowingAll) {
        _.extend(params, {
            currentPage: lessonViewer.paginatorModel.get('currentPage'),
            itemsOnPage: lessonViewer.paginatorModel.get('itemsOnPage')
        });
    }

    // to hide paginator while request is pending
    lessonViewer.paginatorModel.set('totalElements', 0);

    lessonViewer.packages.reset();
    lessonViewer.packages.fetch(params);
});

lessonViewer.commands.setHandler('player:start', function (packageId, packageTitle, packageType, isSuspended) {
    if (packageId) {
        TincanHelper.SetActor(JSON.parse(lessonViewer.tincanActor));
        TincanHelper.SetLRS(JSON.parse(lessonViewer.endpointData));

        TincanHelper.initialize(function () {
            lessonViewer.execute('player:parameters:save', packageId, packageTitle, packageType, isSuspended);

            var viewOptions = {
                packageId: packageId,
                packageType: packageType,
                packageTitle: packageTitle,
                tincanActor: lessonViewer.tincanActor,
                isSuspended: isSuspended
            };

            lessonViewer.playerLayoutView = undefined;
            switch (packageType) {
                case 'tincanpackage':
                    lessonViewer.playerLayoutView = new lessonViewer.Views.TincanContent.PlayerLayoutView(viewOptions);
                    break;
                case 'scormpackage':
                    lessonViewer.playerLayoutView = new lessonViewer.Views.ScormContent.PlayerLayoutView(viewOptions);
                    break;
                default:
                    toastr.error('wrong lesson type');
            }

            if (lessonViewer.playerLayoutView) {
                lessonViewer.playerRegion.show(lessonViewer.playerLayoutView);
                lessonViewer.execute('player:content:show');
            }
         });
    }
});

lessonViewer.commands.setHandler('player:parameters:save', function (id, title, type, isSuspended){
    var pdata = {id: id, type: type, title: title, isSuspended: isSuspended, playerId: lessonViewer.playerId};
    if (lessonViewer.isLocalStorageAvailable) localStorage.setItem('playerSettings', JSON.stringify(pdata));
});

lessonViewer.commands.setHandler('player:session:end', function () {
    valamisApp.execute('portlet:unset:onbeforeunload');
    lessonViewer.execute('player:parameters:clear');
    lessonViewer.router.navigate('/');

    setTimeout(function(){
        lessonViewer.execute('packages:show');
        lessonViewer.execute('packages:reload');
    }, 800);
});

lessonViewer.commands.setHandler('packages:show', function () {
    lessonViewer.playerRegion.$el.hide();
    lessonViewer.playerRegion.empty();
    lessonViewer.mainRegion.$el.show();
});

lessonViewer.commands.setHandler('player:parameters:clear', function () {
    if (lessonViewer.isLocalStorageAvailable) localStorage.removeItem('playerSettings');
});

lessonViewer.commands.setHandler('player:content:show' , function () {
    valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
    lessonViewer.mainRegion.$el.hide();
    lessonViewer.playerRegion.$el.show();
});