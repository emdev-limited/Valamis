achievedCertificates.module('Views', function (Views, achievedCertificates, Backbone, Marionette, $, _) {

    Views.CertificateItemView = Marionette.ItemView.extend({
        template: '#achievedCertificatesItemViewTemplate',
        templateHelpers: function () {
            var validPeriod = this.model.get('validPeriod');
            var endDateString = '';
            if (validPeriod) {
                endDateString = moment(this.model.get('statusModifiedDate'))
                    .add(moment.duration(validPeriod))
                    .locale(Utils.getLanguage()).format('L');
            }

            return {
                courseId: Utils.getCourseId,
                dateString: endDateString,
                fullLogoUrl: this.model.getFullLogoUrl(),
                url: Utils.getCertificateUrl(this.model.get('id'))
            }
        },
        tagName: 'div',
        className: 'tile'
    });

    Views.AppLayoutView = Marionette.CompositeView.extend({
        tagName: 'div',
        className: 'val-portlet achieved-certificates',
        template: '#achievedCertificatesLayoutTemplate',
        templateHelpers: {
            availableCertificatesUrl: Utils.getCertificateUrl()
        },
        childViewContainer: '.js-list-view',
        childView: Views.CertificateItemView,
        ui: {
            showMore: '.js-show-more',
            noItems: '.js-no-certificates'
        },
        events: {
            'click @ui.showMore': 'fetchMore'
        },
        collectionEvents: {
            'sync': 'checkUi',
            'error': 'checkUi'
        },
        fetchMore: function () {
            this.collection.fetchMore();
        },
        checkUi: function () {
            this.ui.noItems.toggleClass('hidden', this.collection.hasItems());
            this.ui.showMore.toggleClass('hidden', !this.collection.hasMore());
        }
    });

});