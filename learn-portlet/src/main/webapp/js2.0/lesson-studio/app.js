/**
 * Created by aklimov on 13.08.15.
 */

var LessonStudio = Marionette.Application.extend({
    channelName:'lessonStudio',
    initialize: function(options) {
        this.addRegions({
            mainRegion: '#lessonStudioAppRegion'
        });

        this.settings = new SettingsHelper({url: window.location.href, portlet: 'lessonStudio'});
        this.settings.fetch();

        this.fixedSizes = {
            MIN_WINDOW_WIDTH: 1200,
            MIN_WINDOW_HEIGHT: 760,
            MIN_RIGHT_BUTTON_OFFSET: 1320,
            MIN_DOWN_BUTTON_OFFSET: 860,
            MODAL_ARROW_OFFSET: 19,
            BUTTON_MAX_PADDING: 15,
            TOPBAR_HEIGHT: 60,
            SIDEBAR_WIDTH: 140,
            VERSION_SIDEBAR_WIDTH: 280,
            ELEMENT_CONTROLS_WIDTH: 40,
            TOOLTIP_WINDOW_BOUNDARY_WIDTH: 1500,
            MAX_ELEMENT_WIDTH:800
        };
    },
    onStart: function(options){
        var previousFilter = this.settings.get('searchParams');
        this.filter = new lessonStudio.Entities.Filter(previousFilter);

        this.filter.on('change', function(){
            lessonStudio.execute('lessons:reload', true);
            lessonStudio.settings.set('searchParams', this.filter.toJSON());
            lessonStudio.settings.save();
        }, this);

        _.extend(this, options);

        this.lessons = new lessonStudio.Entities.LessonCollection();
        this.lessonTemplates = new lessonStudio.Entities.LessonCollection();
        this.paginatorModel = new PageModel();

        var layoutView = new lessonStudio.Views.AppLayoutView();

        this.mainRegion.show(layoutView);

        jQueryValamis(window).resize(function() {
            if(slidesApp.isEditorReady) {
                var windowHeight = jQueryValamis(window).height(),
                    windowWidth = jQueryValamis(window).width();
                placeSlideControls(windowWidth, windowHeight);
                if( slidesApp.activeElement && slidesApp.activeElement.view ){
                    slidesApp.activeElement.view.updateControlsPosition();
                }
                slidesApp.vent.trigger('containerResize');
            }
            if(revealModule.view){
                revealModule.view.placeWorkArea();
            }
        });

        if (Liferay.Session) {
            var timerId = setTimeout(function tick(state) {
                var stateCurrent = Liferay.Session.get('sessionState');
                if (stateCurrent == 'active') {
                    timerId = setTimeout(tick, 990, stateCurrent);
                } else {
                    Liferay.Session.extend();
                }
            }, 990, 'active');
        }
    }
});

var lessonStudio = new LessonStudio();

lessonStudio.commands.setHandler('lesson:save', function(model){
    var options = {silent: true};
    model.save({}, options);
    model.trigger('lesson:saved');
});

lessonStudio.commands.setHandler('lesson:remove', function(model){
    model.destroy().then(function(){
        lessonStudio.execute('lessons:reload');
    });
});

lessonStudio.commands.setHandler('lessons:reload', function(filterChanged){
    var filter = lessonStudio.filter.toJSON();

    if(filterChanged) {
        lessonStudio.paginatorModel.set('currentPage', 1);
    }

    lessonStudio.lessons.fetch({
        reset: true,
        filter: filter,
        isTemplate: false,
        currentPage: lessonStudio.paginatorModel.get('currentPage'),
        itemsOnPage: lessonStudio.paginatorModel.get('itemsOnPage')
    });
    lessonStudio.lessonTemplates.fetch({
        reset: true,
        isTemplate: true,
        currentPage: 1,
        itemsOnPage: -1
    });
});

lessonStudio.commands.setHandler('editor:close', function(){
    jQueryValamis('.slideset-editor').toggleClass('hidden',true);
});

lessonStudio.commands.setHandler('editor-ready', function (slideSetModel) {
    slidesApp.isEditorReady = true;
    slidesApp.execute('controls:place');
    setTimeout(function(){
        valamisApp.execute('notify', 'clear');
    }, 500);

    if( slidesApp.slideCollection.size() == 1 && slidesApp.slideElementCollection.size() === 0 ){
        slidesApp.topbar.currentView.showDeviceSelectModal();
    }
    slidesApp.historyManager.start();
    slidesApp.gridSnapModule.start();
    slidesApp.keyboardModule.start();
    slidesApp.switchMode('edit', true);
    jQueryValamis(document.body).addClass('overflow-hidden');
});

lessonStudio.commands.setHandler('save-slideset', function () {
    if(slidesApp.topbar && slidesApp.topbar.currentView){
        slidesApp.topbar.currentView.triggerSaveSlideset({ close: false });
    }
});