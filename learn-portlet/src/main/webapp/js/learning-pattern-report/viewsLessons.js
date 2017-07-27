learningReport.module('Views', function (Views, learningReport, Backbone, Marionette, $, _) {

    Views.ReportLessonView = Marionette.ItemView.extend({
        template: '#learningReportLessonView',
        className: 'learning-report-td lesson-block',
        ui: {
            lessonIcon: '.js-lesson-icon',
            pagesList: '.js-pages-list'
        },
        events: {
            'mouseover @ui.lessonIcon': 'showLessonTooltip',
            'click @ui.lessonIcon': 'expandLessonLayout',
            'click @ui.pagesList li': 'expandLessonLayout'
        },
        initialize: function (options) {
            this.model.set('userName', options.userName);
        },
        templateHelpers: function() {
            var svgSymbol = '';
            var lessonStateId = this.model.get('status') || 0;
            var legendState = _.find(learningReport.Entities.LESSON_LEGEND, function(obj) {
                return obj.id == lessonStateId
            });

            switch (lessonStateId) {
                case 4:
                    svgSymbol = 'half-book';
                    break;
                case 5:
                    svgSymbol = 'outline-book';
                    break;
                default:
                    svgSymbol = 'full-book';
            }

            return {
                lessonStatus: legendState.name,
                stateDescr: Valamis.language['lesson' + legendState.labelKey + 'StateDescrLabel'],
                lessonOpenDate: this.model.get('date')
                    ? new Date(this.model.get('date')).toLocaleDateString()
                    : false,
                bookIconSrc: Utils.getContextPath() + 'img/book-icon.svg#' + svgSymbol,
                bookIconSize: BOOK_ICON_SIZE,
                showIcon: Boolean(lessonStateId)
            }
        },
        showLessonTooltip: function(e) {
            var lessonIconEl = $(e.target).closest('.js-lesson-icon');

            if (!lessonIconEl.hasClass('tooltipstered')) {
                lessonIconEl.tooltipster({
                    functionInit: function(instance, helper){
                        var content = $(helper.origin).siblings('.element-tooltip-content').detach();
                        instance.content(content);
                    }
                });
            }
            lessonIconEl.tooltipster('open');
        },
        expandLessonLayout : function () {
            this.triggerMethod('pages:show:layout', this.model.get('lessonId'));
        }
    });

    Views.ReportUserView = Marionette.CompositeView.extend({
        template: '#learningReportUserView',
        className: 'learning-report-tr user-row',
        childView: Views.ReportLessonView,
        childViewOptions: function () {
            return {
                userName: this.model.get('user').name
            }
        },
        events: {
            'mouseover': 'mouseOverRow',
            'mouseout': 'mouseOutRow'
        },
        initialize: function() {
            var primaryLessonCollection = this.options.lessonCollection;

            var lessonIds = primaryLessonCollection.map(function(lessonModel) {
                return lessonModel.get('id');
            });

            var userLessonCollection = _.filter(this.model.get('lessons'), function(lesson) {
                return _.indexOf(lessonIds, lesson.lessonId) > -1;
            });

            _.each(userLessonCollection,function(lesson) {
                var primaryLessonModel = primaryLessonCollection.findWhere({'id' : lesson.lessonId});
                _.extend(lesson, {
                    title: primaryLessonModel.get('title'),
                    hasQuestion: primaryLessonModel.get('hasQuestion')
                });
            });

            this.collection = new learningReport.Entities.UserCellsCollection(userLessonCollection);
            this.$el.attr('data-user-id',this.model.get('id'));
        },
        mouseOverRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('id'), true);
        },
        mouseOutRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('id'), false);
        }
    });

    Views.ReportCollectionView = Marionette.CollectionView.extend({
        className: 'grid-content',
        childView : Views.ReportUserView,
        childViewOptions: function () {
            return {
                lessonCollection: this.options.headerCollection
            }
        },
        onRender: function () {
            this.triggerMethod('loading:finished');
        }
    });

    Views.ReportLessonsHeaderThView = Marionette.ItemView.extend({
        template: '#learningReportLessonsThView',
        className: 'learning-report-th',
        initialize: function () {
            this.model.set('isLoading', false);
            this.model.set('isDetailsShown', false);
        },
        ui: {
            lessonTitle: '.js-lesson-title',
            toggleLessonSlides: '.js-toggle-lesson-slides'
        },
        events: {
            'click @ui.lessonTitle': 'expandLessonLayout',
            'click @ui.toggleLessonSlides': 'toggleLessonSlides'
        },
        modelEvents: {
            'change:isLoading': function () {
                this.$el.toggleClass('loading', this.model.get('isLoading'));
            },
            'change:isDetailsShown': function () {
                var actionLabel = (this.model.get('isDetailsShown') ? 'hide' : 'show') + 'PagesLabel';
                this.ui.toggleLessonSlides.html(Valamis.language[actionLabel]);
            }
        },
        toggleLessonSlides: function (e) {
            this.triggerMethod(
                this.model.get('isDetailsShown')
                    ? 'pages:hide:inline'
                    : 'pages:show:inline',
                this.model.id
            );
        },
        expandLessonLayout : function () {
            this.triggerMethod('pages:show:layout', this.model.id);
        }
    });

    Views.ReportLessonsHeadersView = Marionette.CompositeView.extend({
        template: '#learningReportTheadView',
        className: 'learning-report-tr learning-report-headers',
        childView: Views.ReportLessonsHeaderThView,
        onShow: function () {
            this.triggerMethod('loading:finished');
        }
    });

    // Slides Layout Views

    Views.ReportSlideView = Marionette.ItemView.extend({
        template: '#learningReportSlideView',
        className: 'learning-report-td slide-block',
        ui: {
            slideIcon: '.js-slide-icon'
        },
        events: {
            'mouseover @ui.slideIcon': 'showSlideTooltip'
        },
        templateHelpers: function() {
            var slideStateId = this.model.get('status') || 0;
            var slideState = _.find(learningReport.Entities.LESSON_LEGEND, function(obj) {
                return obj.id == slideStateId
            });

            return {
                userName: this.options.userName,
                lessonState: this.options.lessonState > 0,
                slideStatus: 'slide-state-' + slideState.name,
                stateDescr: Valamis.language['lessonPage' + slideState.labelKey + 'StateDescrLabel'],
                lessonVersion: this.options.lessonVersion || false,
                slideOpenDate: this.model.get('date')
                    ? new Date(this.model.get('date')).toLocaleDateString()
                    : false
            }
        },
        showSlideTooltip: function(e) {
            var slideIconEl = $(e.target);

            if (!slideIconEl.hasClass('tooltipstered')) {
                slideIconEl.tooltipster({
                    functionInit: function(instance, helper){
                        var content = $(helper.origin).siblings('.element-tooltip-content').detach();
                        instance.content(content);
                    }
                });
            }
            slideIconEl.tooltipster('open');
        }
    });

    Views.ReportUserSlidesView = Marionette.CompositeView.extend({
        template: '#learningReportUserView',
        className: 'learning-report-tr user-row',
        childView: Views.ReportSlideView,
        childViewOptions: function () {
            return {
                userName: this.model.get('userName'),
                lessonVersion: this.model.get('lessonVersion'),
                lessonState: this.model.get('lessonState')
            }
        },
        initialize: function () {
            this.$el.attr('data-user-id',this.model.get('userId'));
            this.collection = new learningReport.Entities.ReportDetailDataCollection(
                this.model.get('userPages')
            );
        },
        events: {
            'mouseover': 'mouseOverRow',
            'mouseout': 'mouseOutRow'
        },
        mouseOverRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('userId'), true);
        },
        mouseOutRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('userId'), false);
        }
    });

    Views.ReportUserSlidesCollectionView = Marionette.CollectionView.extend({
        className: 'grid-content',
        childView : Views.ReportUserSlidesView,
        onRender: function () {
            this.triggerMethod('loading:finished');
        }
    });

});