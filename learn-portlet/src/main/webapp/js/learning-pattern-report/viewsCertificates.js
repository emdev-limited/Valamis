learningReport.module('Views', function (Views, learningReport, Backbone, Marionette, $, _) {

    Views.ReportCertificateView = Marionette.ItemView.extend({
        template: '#learningReportCertificateView',
        className: 'learning-report-td certificate-block',
        ui: {
            certIcon: '.js-cert-icon',
            goalsList: '.js-goals-list'
        },
        events: {
            'mouseover @ui.certIcon': 'showCertificateTooltip',
            'click @ui.certIcon': 'expandDetailLayout',
            'click @ui.goalsList li': 'expandDetailLayout'
        },
        initialize: function (options) {
            this.model.set('userName', options.userName);
        },
        templateHelpers: function() {

            var certStateId = this.model.get('status') || 0;
            var certState = _.find(learningReport.Entities.PATHS_LEGEND, function(obj) {
                return obj.id == certStateId
            });

            return {
                certificateStatus: certState.name,
                stateDescr: Valamis.language['certificate' + certState.labelKey + 'StateDescrLabel'],
                pathDateTitle: Valamis.language['certificate' + certState.labelKey + 'DateLabel'],
                pathDate: this.model.get('endDate')
                    ? new Date(this.model.get('endDate')).toLocaleDateString()
                    : false,
                showIcon: Boolean(certStateId)
            }
        },
        showCertificateTooltip: function(e) {
            var certIconEl = $(e.target).closest('.js-cert-icon');

            if (!certIconEl.hasClass('tooltipstered')) {
                certIconEl.tooltipster({
                    functionInit: function(instance, helper){
                        var content = $(helper.origin).siblings('.element-tooltip-content').detach();
                        instance.content(content);
                    }
                });
            }
            certIconEl.tooltipster('open');
        },
        expandDetailLayout : function () {
            if (this.model.get('goalsCount') == 0) {
                valamisApp.execute('notify', 'warning', Valamis.language['goalsListEmptyLabel']);
            } else {
                this.triggerMethod('goals:show:layout', this.model.get('certificateId'));
            }
        }
    });

    Views.ReportPathsUserView = Marionette.CompositeView.extend({
        template: '#learningReportUserView',
        className: 'learning-report-tr user-row',
        childView: Views.ReportCertificateView,
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
            var that = this,
                primaryHeaderCollection = this.options.certificatesCollection,
                userCertificatesCollection = [];

            primaryHeaderCollection.each(function (certificate) {
                var userCertificateObj = _.find(that.model.get('certificates'), {
                    certificateId: certificate.id
                });

                if (userCertificateObj) {
                    _.extend(userCertificateObj, {
                        title: certificate.get('title'),
                        goalsCount: certificate.get('goals').length
                    });
                    userCertificatesCollection.push(userCertificateObj);
                } else {
                    userCertificatesCollection.push({
                        certificateId: certificate.id
                    });
                }
            });

            this.collection = new learningReport.Entities.UserCellsCollection(
                userCertificatesCollection
            );
            this.$el.attr('data-user-id',this.model.get('id'));
        },
        mouseOverRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('id'), true);
        },
        mouseOutRow: function () {
            this.triggerMethod('user:toggle_highlight', this.model.get('id'), false);
        }
    });

    Views.ReportPathsCollectionView = Marionette.CollectionView.extend({
        className: 'grid-content',
        childView : Views.ReportPathsUserView,
        childViewOptions: function () {
            return {
                certificatesCollection: this.options.certificatesCollection
            }
        },
        onRender: function () {
            this.triggerMethod('loading:finished');
        }
    });

    Views.ReportPathsHeaderThView = Marionette.ItemView.extend({
        template: '#learningReportPathsThView',
        className: 'learning-report-th',
        initialize: function () {
            this.model.set('isLoading', false);
            this.model.set('isDetailsShown', false);
        },
        ui: {
            columnTitle: '.js-path-title',
            toggleGoals: '.js-toggle-path-goals'
        },
        events: {
            'click @ui.columnTitle': 'expandDetailLayout',
            'click @ui.toggleGoals': 'togglePathGoals'
        },
        modelEvents: {
            'change:isLoading': function () {
                this.$el.toggleClass('loading', this.model.get('isLoading'));
            },
            'change:isDetailsShown': function () {
                var actionLabel = (this.model.get('isDetailsShown') ? 'hide' : 'show') + 'GoalsLabel';
                this.ui.toggleGoals.html(Valamis.language[actionLabel]);
            }
        },
        togglePathGoals: function (e) {
            if (this.model.get('goals').length == 0) {
                valamisApp.execute('notify', 'warning', Valamis.language['goalsListEmptyLabel']);
            } else {
                this.triggerMethod(
                    this.model.get('isDetailsShown')
                        ? 'goals:hide:inline'
                        : 'goals:show:inline'
                );
            }
        },
        expandDetailLayout: function () {
            if (this.model.get('goals').length == 0) {
                valamisApp.execute('notify', 'warning', Valamis.language['goalsListEmptyLabel']);
            } else {
                this.triggerMethod('goals:show:layout', this.model.id);
            }
        }
    });

    Views.ReportPathHeadersView = Marionette.CompositeView.extend({
        template: '#learningReportTheadView',
        className: 'learning-report-tr learning-report-headers',
        childView: Views.ReportPathsHeaderThView,
        onShow: function () {
            this.triggerMethod('loading:finished');
        }
    });

    // Goals Layout Views

    Views.ReportGoalView = Marionette.ItemView.extend({
        template: '#learningReportGoalView',
        className: 'learning-report-td goal-block',
        initialize: function (options) {
            this.model.set('userName', options.userName);
        },
        ui: {
            goalIcon: '.js-goal-icon'
        },
        events: {
            'mouseover @ui.goalIcon': 'showGoalTooltip'
        },
        templateHelpers: function() {

            var goalStateId = this.model.get('status') || 0;
            var goalState = _.find(learningReport.Entities.PATHS_LEGEND, function(obj) {
                return obj.id == goalStateId
            });

            return {
                userName: this.options.userName,
                goalStateClass: 'goal-state-' + goalState.name,
                pathDateTitle: Valamis.language['pathGoal' + goalState.labelKey + 'DateLabel'],
                lastDate: this.model.get('date')
                    ? new Date(this.model.get('date')).toLocaleDateString()
                    : false,
                showIcon: Boolean(goalStateId)
            }
        },
        showGoalTooltip: function(e) {
            var goalIconEl = $(e.target);

            if (!goalIconEl.hasClass('tooltipstered')) {
                goalIconEl.tooltipster({
                    functionInit: function(instance, helper){
                        var content = $(helper.origin).siblings('.element-tooltip-content').detach();
                        instance.content(content);
                    }
                });
            }
            goalIconEl.tooltipster('open');
        }
    });

    Views.ReportUserGoalsView = Marionette.CompositeView.extend({
        template: '#learningReportUserView',
        className: 'learning-report-tr user-row',
        childView: Views.ReportGoalView,
        childViewOptions: function () {
            return {
                userName: this.model.get('userName')
            }
        },
        initialize: function () {
            this.$el.attr('data-user-id',this.model.get('userId'));
            this.collection = new learningReport.Entities.ReportDetailDataCollection(
                this.model.get('userGoals')
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

    Views.ReportUserGoalsCollectionView = Marionette.CollectionView.extend({
        className: 'grid-content',
        childView : Views.ReportUserGoalsView,
        onRender: function () {
            this.triggerMethod('loading:finished');
        }
    });

});