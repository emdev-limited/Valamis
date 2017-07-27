allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.EditCourseView = Marionette.ItemView.extend({
        template: '#allCoursesEditCourseView',
        templateHelpers: function () {
            var logoUrl = this.model.get('logoUrl');
            var timestamp = Date.now();
            return {
                logoUrlTmpstamp: (logoUrl) ? logoUrl + '&tmstamp=' + timestamp : ''
            }
        },
        ui: {
            title: '.js-course-title',
            description: '.js-course-description',
            friendlyUrl: '.js-course-friendly-url',
            membershipType: 'input[name="membershipType"]',
            deleteLogo: '.js-delete-logo',
            longDescription: '.js-course-long-description',
            userLimit: '.js-course-user-limit',
            beginDate: '.js-course-begin-datetimepicker',
            endDate: '.js-course-end-datetimepicker',
            template: '.js-course-template',
            theme: '.js-course-theme',
            certificates: '.js-course-certificates',
            tags: '.js-course-tags',
            instructors: '.js-course-instructors'
        },
        manualUrlEdit: false,
        events: {
            'change @ui.title': 'titleChanged',
            'change @ui.friendlyUrl': 'urlChanged',
            'change @ui.membershipType': 'membershipTypeChanged',
            'click @ui.deleteLogo': 'deleteLogo'
        },
        modelEvents: {
            'change:logoSrc': 'onModelLogoChanged'
        },
        behaviors: {
            ValamisUIControls: {},
            ImageUpload: {
                'postponeLoading': true,
                'getFolderId': function (model) {
                    return 'course_logo_' + model.get('id');
                },
                'getFileUploaderUrl': function (model) {
                    return path.root + path.api.files + 'course/' + model.get('id') + '/logo';
                },
                'uploadLogoMessage': function () {
                    return Valamis.language['uploadLogoMessage'];
                },
                'fileUploadModalHeader': function () {
                    return Valamis.language['fileUploadModalHeader'];
                }
            }
        },
        titleChanged: function () {
            if (!this.manualUrlEdit) {
                var title = this.ui.title.val();
                if (!_.isEmpty(title)) {
                    var url = title.trim().toLowerCase().replace(/[^a-zA-Z0-9-+._\s/]/g, '').replace(/\s+/g, '-');
                    this.ui.friendlyUrl.val(url);
                }
            }
        },
        urlChanged: function () {
            this.manualUrlEdit = true;
        },
        checkMembershipType: function (type) {
            this.$('#openMembership').prop('checked', type === Views.CourseTypes.OPEN);
            this.$('#requestMembership').prop('checked', type === Views.CourseTypes.ON_REQUEST);
            this.$('#closedMembership').prop('checked', type === Views.CourseTypes.CLOSED);
        },
        getMembershipType: function () {
            if (this.$('#openMembership').is(':checked')) return Views.CourseTypes.OPEN;
            else if (this.$('#requestMembership').is(':checked')) return Views.CourseTypes.ON_REQUEST;
            else if (this.$('#closedMembership').is(':checked')) return Views.CourseTypes.CLOSED;

            else return Views.CourseTypes.OPEN;
        },
        membershipTypeChanged: function () {
            this.model.membershipType = this.getMembershipType();

            var commentLanguagekey;
            switch (this.getMembershipType()) {
                case Views.CourseTypes.OPEN:
                    commentLanguagekey = 'membershipTypeOptionOpenComment';
                    break;
                case Views.CourseTypes.ON_REQUEST:
                    commentLanguagekey = 'membershipTypeRestrictedComment';
                    break;
                case Views.CourseTypes.CLOSED:
                    commentLanguagekey = 'membershipTypePrivateComment';
                    break;
            }

            if (commentLanguagekey) {
                this.$('#membershipSelectionComment').text(Valamis.language[commentLanguagekey])
            }
        },
        checkCourseActiveStatus: function (isActive) {
            this.$('#siteActiveYes').prop('checked', isActive);
            this.$('#siteActiveNo').prop('checked', !isActive);
        },

        getCourseActiveStatus: function () {
            return this.$('#siteActiveYes').is(':checked');
        },

        updateModel: function () {
            var title = this.ui.title.val();
            if (title === '') {
                valamisApp.execute('notify', 'warning', Valamis.language['titleIsEmptyError']);
                return false;
            } else if (!isNaN(title)) {
                valamisApp.execute('notify', 'warning', Valamis.language['titleIsNumberError']);
                return false;
            }

            var userLimit = this.$('.js-course-user-limit').valamisPlusMinus('value') || '';
            var beginDate = '';
            var endDate = '';
            if (!userLimit && this.model.get('queueCount')) {
                valamisApp.execute('notify', 'warning', Valamis.language['queueNotEmptyError']);
                return false;
            }
            if (this.$('.js-course-begin-datetimepicker').data("DateTimePicker").date()) {
                beginDate = moment(this.$('.js-course-begin-datetimepicker').data("DateTimePicker")
                    .date()).utc().format();
            }

            if (this.$('.js-course-end-datetimepicker').data("DateTimePicker").date()) {
                endDate = moment(this.$('.js-course-end-datetimepicker').data("DateTimePicker")
                    .date()).utc().format();
            }
            if (beginDate && !endDate || !beginDate && endDate) {
                valamisApp.execute('notify', 'warning', Valamis.language['fillDatesError']);
                return false;
            }
            var description = this.ui.description.val();
            var friendlyUrl = this.ui.friendlyUrl.val();
            var longDescription = CKEDITOR.instances.allCoursesLongDescriptionTextView.getData();
            var templateId = this.ui.template.val();
            var themeId = this.ui.theme.val();

            var re = /^[a-zA-Z0-9-+._/]+[^/]$/;
            if (!re.test(friendlyUrl)) {
                valamisApp.execute('notify', 'warning', Valamis.language['friendlyUrlIsWrongError']);
                return false;
            }

            var tagsElem = this.ui.tags[0].selectize;
            var tagsIds = tagsElem.getValue().split(',');

            var tags = [], tagList = [];
            if (tagsIds[0] != '') {
                _.forEach(tagsIds, function (tagId) {
                    tagList.push(tagsElem.options[tagId].text);
                    tags.push({
                        id: tagId,
                        text: tagsElem.options[tagId].text
                    });
                });
            }

            var certificatesElem = this.ui.certificates[0].selectize;

            var certificatesIds = (!!certificatesElem )? certificatesElem.getValue().split(',') : [];

            var certificates = [], certificateList = [];
            if (!!certificatesIds && certificatesIds[0] != '') {
                _.forEach(certificatesIds, function (certificatesId) {
                    certificateList.push(certificatesElem.options[certificatesId].text);
                    certificates.push({
                        id: certificatesId,
                        text: certificatesElem.options[certificatesId].text
                    });
                });
            }

            var instructorsElem = this.ui.instructors[0].selectize;
            var instructorsIds = instructorsElem.getValue().split(",");
            var instructors = [];
            if (instructorsIds[0] != '') {
                _.forEach(instructorsIds, function (instructorsId) {
                    instructors.push({
                        id: instructorsId,
                        name: instructorsElem.options[instructorsId].name
                    });
                });
            }

            this.model.set({
                title: title,
                description: description,
                friendlyUrl: friendlyUrl,
                membershipType: this.getMembershipType(),
                isActive: this.getCourseActiveStatus(),
                tags: tags,
                tagList: tagList.join(' • '),
                certificates: certificates,
                certificateList: certificateList.join(' • '),
                longDescription: longDescription,
                userLimit: userLimit,
                beginDate: beginDate,
                endDate: endDate,
                templateId: templateId,
                themeId: themeId,
                instructors: instructors
            });
            return true;
        },
        onModelLogoChanged: function () {
            this.$('.js-logo').attr('src', this.model.get('logoSrc'));
            this.updateDeleteLogoButton();
        },
        updateDeleteLogoButton: function () {
            this.$('.js-delete-logo').toggleClass('hidden', !this.model.get('logoUrl') && !this.model.get('logo'));
        },
        onRender: function () {
            this.checkMembershipType(this.model.get('membershipType') || '');
            this.checkCourseActiveStatus(!!this.model.get('isActive'));
            this.model.cacheLogo();
            this.updateDeleteLogoButton();
            //if CKEDITOR already initialized, remove area. Else in error case CKEDITOR will be broken
            if (CKEDITOR.instances.allCoursesLongDescriptionTextView) {
                CKEDITOR.remove(CKEDITOR.instances.allCoursesLongDescriptionTextView);
            }
            this.activateEditor();
        },
        activateEditor: function () {
            var editorDeffered = $.Deferred();

            var intervalId = setInterval(function(){
                if(typeof CKEDITOR !== 'undefined' && typeof CKEDITOR._bundle !== 'undefined'
                        && CKEDITOR._bundle == 'valamis' && CKEDITOR.status == 'loaded'
                        && $('#allCoursesLongDescriptionTextView').length > 0) {

                    clearInterval(intervalId);
                    CKEDITOR.replace('allCoursesLongDescriptionTextView', {
                        toolbarLocation: 'top',
                        height: 100,
                        toolbar: [
                            {name: 'document', items: ['Source']},
                            {
                                name: 'paragraph',
                                items: ['NumberedList', 'BulletedList', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock']
                            },
                            {name: 'basicstyles', items: ['Bold', 'Italic', 'Underline']},
                            {name: 'styles', items: ['Font', 'FontSize']},
                            {name: 'colors', items: ['TextColor', 'BGColor']},
                            {name: 'insert', items: ['Image']}
                        ]
                    });

                    editorDeffered.resolve();
                }
            }, 100);

            return editorDeffered.promise();
        },
        focusTitle: function () {
            this.ui.title.val(this.model.get('title')); // for cursor after last character
            this.ui.title.focus();
        },
        changeDatepickerClass: function () {
            var datepicker = $('.bootstrap-datetimepicker-widget');
            if (datepicker.hasClass('dtp-dropdown-menu')) {
                datepicker.removeClass('dtp-dropdown-menu');
                datepicker.addClass('dropdown-menu');
            }
        },
        onValamisControlsInit: function () {
            var that = this;

            // will be in next release
            if (!this.model.id) {
                this.templates = new allCourses.Entities.TemplatesCollection();
                this.templates.fetch({
                    reset: true,
                    success: function () {
                        that.fillTemplate();
                    }
                });
            }
            
            this.themes = new allCourses.Entities.ThemesCollection();
            this.themes.fetch({
                reset: true,
                success: function() {
                    that.fillTheme();
                }
            });

            this.tags = new Valamis.TagCollection();
            this.tags.on('reset', function (tags) {
                that.fillTagSelect(tags);
            });
            this.tags.fetch({reset: true});

            this.certificates = new allCourses.Entities.CertificateCollection();
            this.certificates.fetch({
                reset: true,
                success: function() {
                    that.fillCertificates();
                }
            });

            this.users = new allCourses.Entities.UsersCollection();
            this.users.fetch({
                reset: true,
                success: function() {
                    that.fillInstructors();
                }
            });
            
            this.$('.js-course-begin-date').datepicker({
                changeMonth: false,
                showAnim: 'fadeIn',
                numberOfMonths: 1,
                onClose: function (selectedDate) {
                    that.$('.js-course-end-date').datepicker('option', 'minDate', selectedDate);
                }
            });
            this.$('.js-course-end-date').datepicker({
                changeMonth: false,
                showAnim: 'fadeIn',
                numberOfMonths: 1,
                onClose: function (selectedDate) {
                    that.$('.js-course-begin-date').datepicker('option', 'maxDate', selectedDate);
                }
            });
            this.$('.js-course-begin-datetimepicker').datetimepicker()
                .on('dp.show', function (e) {
                    that.changeDatepickerClass();
                })
                .on('dp.change', function (selectedDate) {
                    that.$('.js-course-end-datetimepicker').data('DateTimePicker').minDate(selectedDate.date);
                });
            this.$('.js-course-end-datetimepicker').datetimepicker()
                .on('dp.show', function (e) {
                    that.changeDatepickerClass();
                })
                .on('dp.change', function (selectedDate) {
                    that.$('.js-course-begin-datetimepicker').data('DateTimePicker').maxDate(selectedDate.date);
                });

            if (this.model.get('beginDate')) {
                this.$('.js-course-begin-datetimepicker').data('DateTimePicker')
                    .defaultDate(new Date(moment(this.model.get('beginDate'))));
            } else {
                this.$('.js-course-begin-datetimepicker').data('DateTimePicker').defaultDate(null);
            }
            if (this.model.get('endDate')) {
                this.$('.js-course-end-datetimepicker').data('DateTimePicker')
                    .defaultDate(new Date(moment(this.model.get('endDate'))));
            } else {
                this.$('.js-course-end-datetimepicker').data('DateTimePicker').defaultDate(null);
            }
            this.$('.js-course-user-limit').valamisPlusMinus({
                min: 1, max: 9999, obligatory: false
            });
            if (this.model.get('userLimit')) {
                this.$('.js-course-user-limit').valamisPlusMinus("value", this.model.get('userLimit'));
            }else{
                this.$('.js-course-user-limit').valamisPlusMinus("value", null);
            }
        },
        fillTagSelect: function (tags) {
            var selectTags = tags.map(function (tagModel) {
                return {
                    id: tagModel.get('id'),
                    text: tagModel.get('text')
                }
            });

            var modelTags = _.map(this.model.get('tags'), function (tag) {
                return tag.id
            });

            var selectize = this.ui.tags.selectize({
                delimiter: ',',
                persist: false,
                valueField: 'id',
                options: selectTags,
                create: true,
                plugins: ['remove_button']
            });
            selectize[0].selectize.setValue(modelTags);
        },

        fillTemplate: function () {
            var selectTemplates = this.templates.map(function(model) {
                return {
                    id: model.get('id'),
                    name: model.get('name')
                }
            });

            var selectize = this.ui.template.selectize({
                valueField: 'id',
                labelField: 'name',
                options: selectTemplates
            });
            if (this.model.get('template')){
                selectize[0].selectize.setValue(this.model.get('template').id);
            }
        },

        fillTheme: function () {
            var selectThemes = this.themes.map(function(model) {
                return {
                    id: model.get('id'),
                    name: model.get('name')
                }
            });

            var defaultThemeId = "";
            if (this.model.get('themeId')){
                defaultThemeId = this.model.get('themeId');
            } else if (selectThemes.length > 0){
                defaultThemeId = selectThemes[0].id;
            }

            var selectize = this.ui.theme.selectize({
                valueField: 'id',
                labelField: 'name',
                options: selectThemes
            });
            selectize[0].selectize.setValue(this.model.get('theme')
                ? this.model.get('theme').id : defaultThemeId);

        },

        fillCertificates: function () {
            var selectCertificates = [];
            this.certificates.map(function(model) {
                selectCertificates.push({
                    id: model.get('id'),
                    name: model.get('title')
                });
            });

            var modelCertificates = _.pluck(this.model.get('certificates'), 'id');
            var inactiveCertificates = _.filter(this.model.get('certificates'), function(item) {
                return !item.isActive;
            }).map(function(model){
                return{
                    id: model.id,
                    name: model.title
                }
            });

            var selectize = this.ui.certificates.selectize({
                delimiter: ',',
                persist: false,
                valueField: 'id',
                labelField: 'name',
                options: selectCertificates,
                plugins: ['remove_button'],
                items: modelCertificates
            });

            if (inactiveCertificates.length){
                _.forEach(inactiveCertificates, function(item){
                    selectize[0].selectize.addOption(item);
                    selectize[0].selectize.addItem(item.id);
                })
            }
        },

        fillInstructors: function () {
            var selectInstructors = [];
            var that = this;
            this.users.map(function(model) {
                selectInstructors.push({
                    id: model.get('id'),
                    name: model.get('name')
                });
            });
            var modelInstructors = [];

            var initSelectize = function(hasInstructors) {
                var selectize = that.ui.instructors.selectize({
                    delimiter: ',',
                    persist: false,
                    valueField: 'id',
                    labelField: 'name',
                    options: selectInstructors,
                    plugins: ['remove_button']
                });
                if (hasInstructors) {
                    selectize[0].selectize.setValue(modelInstructors);
                }
            }

            if (this.model.get('id')) {
                this.instructors = new allCourses.Entities.InstructorsCollection();
                this.instructors.fetch({
                    courseId: this.model.get('id'),
                    reset: true,
                    success: function () {
                        modelInstructors = that.instructors.pluck('id');
                        initSelectize(true);
                    }
                });
            } else {
                initSelectize(false);
            }
        },

        deleteLogo: function () {
            this.model.unset('logoUrl');
            this.model.unset('logo');
            this.model.set('logoSrc', '');
            this.model.set('hasLogo', false);            
        }
    });
});
