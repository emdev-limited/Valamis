allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.ViewCourseView = Marionette.ItemView.extend({
        template: '#allCoursesViewCourseView',
        templateHelpers: function () {
            var logoUrl = this.model.get('logoUrl');
            var timestamp = Date.now();
            
            var formattedBeginDate = (this.model.get('beginDate'))
                ? Utils.formatDate(this.model.get('beginDate'), 'lll')
                : '';
            var formattedEndDate = (this.model.get('endDate')) 
                ? Utils.formatDate(this.model.get('endDate'), 'lll')
                : '';

            var tagsList = this.model.get('tags').map(function(tag){return tag.text;}).join(" • ");
            var certificatesList = this.model.get('certificates').map(function(certificate){return certificate.title;}).join(" • ");

            return {
                formattedBeginDate: formattedBeginDate,
                formattedEndDate: formattedEndDate,
                saveCourseButtonLabel1: 'test save',
                tagsList: tagsList,
                certificatesList: certificatesList,
                logoUrlTmpstamp: (logoUrl) ? logoUrl + '&tmstamp=' + timestamp : ''
            }
        },
        ui: {
            title: '.js-course-title',
            description: '.js-course-description',
            friendlyUrl: '.js-course-friendly-url',
            longDescription: '.js-course-long-description',
            userLimit: '.js-course-user-limit',
            beginDate: '.js-course-begin-datetimepicker',
            endDate: '.js-course-end-datetimepicker',
            template: '.js-course-template',
            theme: '.js-course-theme',
            certificates: '.js-course-certificates'
        },
        manualUrlEdit: false,
        focusTitle: function () {
            this.ui.title.val(this.model.get('title')); // for cursor after last character
            this.ui.title.focus();
        },
        onRender: function () {
            var that = this;
            var elem, 
                icon;
            this.model.cacheLogo();
            var membersCount = this.model.get('userCount');
            var limit = this.model.get('userLimit');
            var queueCount = this.model.get('queueCount');
            if (limit) {
                var text = membersCount + '/' + limit + ' ' + Valamis.language['participantLabel']
                    + (queueCount
                    ? ' (' + queueCount + ' ' + Valamis.language['participantsQueueLabel'] + ')' : '');
                this.$('.js-course-participants').text(text);
            }
            else {
                this.$('.js-course-participants').text(membersCount + ' ' + Valamis.language['participantLabel']);
            }
            var certificates = [];
            this.model.get('certificates').map(function(model) {
                certificates.push({
                    name: model.title,
                    url: Utils.getCertificateUrl(model.id),
                    status: model.status
                });
            });
            if(certificates.length) {
                _.forEach(certificates, function(certificate){
                    icon = jQuery("<span>").addClass('val-rounded-icon');
                    icon.addClass(certificate.status == 'Success'
                        ? 'val-icon-checkmark success' : 'val-icon-exit failed');
                    elem = jQuery("<a>").append(certificate.name);
                    elem.attr('href', certificate.url);
                    elem.attr('target', '_blank');
                    that.$('.js-course-certificates').append(jQuery("<li>").append(icon, elem));
                });
            }
        }
    });
});
