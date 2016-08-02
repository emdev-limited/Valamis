allCourses.module('Views', function (Views, allCourses, Backbone, Marionette, $, _) {

    Views.EditCourseView = Marionette.ItemView.extend({
        template: '#allCoursesEditCourseView',
        ui: {
            title: '.js-course-title',
            description: '.js-course-description',
            friendlyUrl: '.js-course-friendly-url',
            membershipType: 'input[name="membershipType"]'
        },
        manualUrlEdit: false,
        events: {
            'change @ui.title': 'titleChanged',
            'change @ui.friendlyUrl': 'urlChanged',
            'change @ui.membershipType': 'membershipTypeChanged'
        },
        modelEvents: {
            'change:logoSrc': 'onModelLogoChanged'
        },
        behaviors: {
            ValamisUIControls: {},
            ImageUpload: {
                'postponeLoading': true,
                'getFolderId': function(model){
                    return 'course_logo_' + model.get('id');
                },
                'getFileUploaderUrl': function (model) {
                    return path.root + path.api.files + 'course/' + model.get('id') + '/logo';
                },
                'uploadLogoMessage' : function() { return Valamis.language['uploadLogoMessage'];},
                'fileUploadModalHeader' : function() { return Valamis.language['fileUploadModalHeader']; }
            }
        },
        titleChanged: function(){
            if(!this.manualUrlEdit){
                var title = this.ui.title.val();
                if(!_.isEmpty(title)){
                    var url = title.trim().toLowerCase().replace(/[^a-zA-Z0-9-+._\s/]/g, '').replace(/\s+/g,'-');
                    this.ui.friendlyUrl.val(url);
                }
            }
        },
        urlChanged: function(){
            this.manualUrlEdit = true;
        },
        checkMembershipType: function(type) {
            this.$('#openMembership').prop('checked', type === Views.CourseTypes.OPEN);
            this.$('#requestMembership').prop('checked', type === Views.CourseTypes.ON_REQUEST);
            this.$('#closedMembership').prop('checked', type === Views.CourseTypes.CLOSED);
        },
        getMembershipType: function() {
            if (this.$('#openMembership').is(':checked')) return Views.CourseTypes.OPEN;
            else if (this.$('#requestMembership').is(':checked')) return Views.CourseTypes.ON_REQUEST;
            else if (this.$('#closedMembership').is(':checked')) return Views.CourseTypes.CLOSED;

            else return Views.CourseTypes.OPEN;
        },
        membershipTypeChanged: function() {
            this.model.membershipType = this.getMembershipType();

            var commentLanguagekey;
            switch(this.getMembershipType()) {
                case Views.CourseTypes.OPEN: commentLanguagekey = 'membershipTypeOptionOpenComment'; break;
                case Views.CourseTypes.ON_REQUEST: commentLanguagekey = 'membershipTypeRestrictedComment'; break;
                case Views.CourseTypes.CLOSED: commentLanguagekey = 'membershipTypePrivateComment'; break;
            }

            if(commentLanguagekey) {
                this.$('#membershipSelectionComment').text(Valamis.language[commentLanguagekey])
            }
        },
        checkCourseActiveStatus: function(isActive) {
            this.$('#siteActiveYes').prop('checked', isActive);
            this.$('#siteActiveNo').prop('checked', !isActive);
        },

        getCourseActiveStatus: function() {
            return this.$('#siteActiveYes').is(':checked');
        },

        updateModel: function () {
            var title = this.ui.title.val();
            if (title === '') {
                valamisApp.execute('notify', 'warning', Valamis.language['titleIsEmptyError']);
                return false;
            }

            var description = this.ui.description.val();
            var friendlyUrl= this.ui.friendlyUrl.val();

            var re = /^[a-zA-Z0-9-+._/]+[^/]$/;
            if(!re.test(friendlyUrl)) {
                valamisApp.execute('notify', 'warning', Valamis.language['friendlyUrlIsWrongError']);
                return false;
            }

            var tagsElem = this.$('.val-tags')[0].selectize;
            var tagsIds = tagsElem.getValue().split(',');

            var tags = [], tagList = [];
            if(tagsIds[0] != '') {
                _.forEach(tagsIds, function (tagId) {
                    tagList.push(tagsElem.options[tagId].text);
                    tags.push({
                        id: tagId,
                        text: tagsElem.options[tagId].text
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
                tagList: tagList.join(' • ')
            });
            return true;
        },
        onModelLogoChanged: function () {
            this.$('.js-logo').attr('src', this.model.get('logoSrc'));
        },
        onRender: function(){
            this.checkMembershipType(this.model.get('membershipType') || '');
            this.checkCourseActiveStatus(!!this.model.get('isActive'));
        },
        focusTitle: function() {
            this.ui.title.val(this.model.get('title')); // for cursor after last character
            this.ui.title.focus();
        },
        onValamisControlsInit: function () {
            var that = this;
            this.tags = new Valamis.TagCollection();
            this.tags.on('reset', function (tags) {
                that.fillTagSelect(tags);
            });

            this.tags.fetch({reset: true});
        },

        fillTagSelect: function (tags) {
            var selectTags = tags.map(function(tagModel) {
                return {
                    id: tagModel.get("id"),
                    text: tagModel.get("text")
                }
            });

            var modelTags = _(this.model.get('tags')).map(function(tag) { return tag.id });

            var selectize = this.$('.js-course-tags').selectize({
                delimiter: ',',
                persist: false,
                valueField: 'id',
                options: selectTags,
                create: true
            });
            selectize[0].selectize.setValue(modelTags.value());
        }
    });
});
