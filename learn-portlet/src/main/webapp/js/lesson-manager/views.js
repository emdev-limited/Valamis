/**
 * Created by igorborisov on 03.03.15.
 */

lessonManager.module("Views", function (Views, lessonManager, Backbone, Marionette, $, _) {

    var DISPLAY_TYPE = {
        LIST: 'list',
        TILES: 'tiles'
    };

    Views.EditPackageDetailsView = Marionette.ItemView.extend({
        DEFAULT_SCORE: 0.7,
        template: '#packageManagerEditItemView',
        templateHelpers :function () {
            return {
                'courseId': Utils.getCourseId
            }
        },
        events: {
            'change .js-passing-limit-enable': 'togglePassingLimit',
            'change .js-rerun-interval-enable': 'toggleRerunInterval',
            'change .js-able-to-run-enable' : 'toggleIsAbleToRuns',
            'change .js-required-review' : 'toggleRequiredReview',
            'click .js-delete-logo': 'deleteLogo'
        },
        modelEvents: {
            "change:logoSrc": "onModelLogoChanged"
        },
        behaviors: {
            ValamisUIControls: {},
            ImageUpload: {
                'postponeLoading': true,
                'getFolderId': function(model){
                    return 'package_logo_' + model.get('id');
                },
                'getFileUploaderUrl': function (model) {
                    return path.root + path.api.files + 'package/' + model.get('id') + '/logo';
                },
                'uploadLogoMessage' : function() { return Valamis.language['uploadLogoMessage'];},
                'fileUploadModalHeader' : function() { return Valamis.language['fileUploadModalHeader']; }
            }
        },
        focusTitle: function() {
            this.$('.js-package-title').val(this.model.get('title')); // for cursor after last character
            this.$('.js-package-title').focus();
        },
        onValamisControlsInit: function () {
            var that = this;
            this.$('.js-is-able-from').datepicker({
                changeMonth: true,
                numberOfMonths: 1,
                onClose: function (selectedDate) {
                    that.$('.js-is-able-to').datepicker('option', 'minDate', selectedDate);
                }
            });
            this.$('.js-is-able-to').datepicker({
                changeMonth: true,
                numberOfMonths: 1,
                onClose: function (selectedDate) {
                    that.$('.js-is-able-from').datepicker('option', 'maxDate', selectedDate);
                }
            });
            this.$('.js-is-able-to').datepicker('setDate', null);
            this.$('.js-is-able-from').datepicker('setDate', null);

            var storedFrom = this.model.get('beginDate');
            var storedTo = this.model.get('endDate');

            var hasAbleToRunTo = !_.isEmpty(storedFrom);
            var hasAbleToRunFrom = !_.isEmpty(storedTo);
            this.$('.js-able-to-run-enable').attr('checked', hasAbleToRunTo || hasAbleToRunFrom);
            this.updateAbletoRun(hasAbleToRunTo || hasAbleToRunFrom);
            if (storedFrom) {
                this.$('.js-is-able-from').datepicker('setDate', new Date(storedFrom));
            }
            if (storedTo) {
                this.$('.js-is-able-to').datepicker('setDate', new Date(storedTo));
            }


            var hasPassingLimit = (parseInt(this.model.get('passingLimit')) || 0) > 0;
            this.$('.js-passing-limit-enable').attr('checked', hasPassingLimit);
            if(hasPassingLimit) {
                this.$('.js-passing-limit').valamisPlusMinus('value', this.model.get('passingLimit'));
            }
            this.updatePassingLimit(hasPassingLimit);

            var rerunType = this.model.get('rerunIntervalType');
            var rerunInterval = this.model.get('rerunInterval');
            var hasRerunType = (rerunType !== 'UNLIMITED') && rerunInterval > 0;
            this.$('.js-rerun-interval-enable').attr('checked', hasRerunType);
            if(hasRerunType){
                this.$('.js-rerun-interval-type option[value=' + rerunType + ']').prop('selected', true);
                this.$('.js-rerun-interval').valamisPlusMinus('value', this.model.get('rerunInterval'));
            }
            this.updateRerunInterval(hasRerunType);

            var self = this;
            this.tags = new Valamis.TagCollection();
            this.tags.fetch({reset: true});

            this.tags.on('reset', function() {
                self.populateTagSelect();
            });

            this.$('.js-score-limit').valamisPlusMinus({
                min: 0, max: 1, step: 0.05, allowFloat: true
            });
            this.$('.js-score-limit').valamisPlusMinus('value', this.model.get('scoreLimit') || this.DEFAULT_SCORE);
            var isReviewRequired = !!this.model.get('requiredReview');
            this.$('.js-required-review').attr('checked', isReviewRequired);
            this.updateRequiredReview(isReviewRequired);

            this.updateDeleteLogoButton();

            if (!Valamis.permissions.LM_MODIFY)  {
                this.$('.js-plus-minus').valamisPlusMinus('disable');
                this.$('.js-is-able-from').datepicker('option', 'disabled', true);
                this.$('.js-is-able-to').datepicker('option', 'disabled', true);
                this.$('.js-rerun-interval-type').prop('disabled', true)
            }
        },
        updateDeleteLogoButton: function() {
            this.$('.js-delete-logo').toggleClass('hidden', !this.model.get('logo'));
        },
        updatePassingLimit: function(isEnabled){
            if(isEnabled){
                this.$('.js-passing-limit').valamisPlusMinus('enable');
            }else{
                this.$('.js-passing-limit').valamisPlusMinus('disable');
            }
        },
        updateRerunInterval: function(isEnabled){
            this.$('.js-rerun-interval-type').attr('disabled', !isEnabled);
            if(isEnabled){
                this.$('.js-rerun-interval').valamisPlusMinus('enable');
            }else{
                this.$('.js-rerun-interval').valamisPlusMinus('disable');
            }
        },
        updateAbletoRun: function(isEnabled){
            this.$('.js-is-able-from').datepicker("option", "disabled", !isEnabled);
            this.$('.js-is-able-to').datepicker("option", "disabled", !isEnabled);
            this.$('.js-is-able-from').datepicker('setDate', isEnabled?(new Date()):null);
            this.$('.js-is-able-to').datepicker('setDate', isEnabled?(new Date()):null);
        },
        updateRequiredReview: function(isEnabled) {
            if(isEnabled){
                this.$('.js-score-limit').valamisPlusMinus('enable');
            }
            else {
                this.$('.js-score-limit').valamisPlusMinus('disable');
            }
        },
        togglePassingLimit: function () {
            var passLimitisEnabled = this.$('.js-passing-limit-enable').is(':checked');
            //TODO hide block?
            this.updatePassingLimit(passLimitisEnabled);
            if(passLimitisEnabled){
                if (this.model.get('passingLimit') <= 0 && this.$('.js-passing-limit').valamisPlusMinus('value') <= 0) {
                    this.$('.js-passing-limit').valamisPlusMinus('value', 1);
                }
            }
        },
        toggleRerunInterval: function () {
            var rerunIntervalEnabled = this.$('.js-rerun-interval-enable').is(':checked');
            //TODO hide rerun block?
            this.updateRerunInterval(rerunIntervalEnabled);
            if (rerunIntervalEnabled) {
                if (this.model.get('rerunInterval') <= 0 && this.$('.js-rerun-interval').valamisPlusMinus('value') <= 0) {
                    this.$('.js-rerun-interval').valamisPlusMinus('value', 1);
                }
            }
        },
        toggleIsAbleToRuns: function(){
            var ableToRunEnabled = this.$('.js-able-to-run-enable').is(':checked');
            this.updateAbletoRun(ableToRunEnabled);

        },
        toggleRequiredReview: function() {
            var requiredReview = this.$('.js-required-review').is(':checked');
            this.updateRequiredReview(requiredReview);
        },
        saveModelsTextValues: function () {
            var title = this.$('.js-package-title').val();
            var description = this.$('.js-package-description').val();
            var passingLimit = -1;
            if(this.$('.js-passing-limit-enable').is(':checked')){
                passingLimit = this.$('.js-passing-limit').valamisPlusMinus('value') || -1;
            }

            var rerunInterval = -1;
            var rerunIntervalType = 'UNLIMITED';
            if (this.$('.js-rerun-interval-enable').is(":checked")) {
                rerunInterval = this.$('.js-rerun-interval').valamisPlusMinus('value') || -1;
                rerunIntervalType = this.$('.js-rerun-interval-type').val();
            }

            var ableToRunFrom = '';
            var ableToRunTo = '';
            if (this.$('.js-able-to-run-enable').is(":checked")) {
                ableToRunFrom = this.$ ('.js-is-able-from').datepicker('getDate');
                ableToRunTo = this.$('.js-is-able-to').datepicker('getDate');
            }

            var tagsElem = this.$el.find('.val-tags')[0].selectize;

            var tagsIds = tagsElem.getValue().split(',');

            var tags = [], taglist = [];
            if(tagsIds[0] != '') {
                _.forEach(tagsIds, function (tagId) {
                    taglist.push(tagsElem.options[tagId].text);
                    tags.push({id: tagId, text: tagsElem.options[tagId].text});
                });
            }
            var isRequiredReview = this.$('.js-required-review').is(':checked');
            var scoreLimit = this.$('.js-score-limit').valamisPlusMinus('value') || this.DEFAULT_SCORE;

            this.model.set({
                title: title,
                description: description,
                passingLimit: passingLimit,
                rerunInterval: rerunInterval,
                rerunIntervalType: rerunIntervalType,
                beginDate: $.datepicker.formatDate('yy-mm-dd', ableToRunFrom),
                endDate: $.datepicker.formatDate('yy-mm-dd', ableToRunTo),
                tags: tags,
                tagsList: taglist.join(' • '),
                requiredReview: isRequiredReview,
                scoreLimit: scoreLimit
            });
        },
        onModelLogoChanged: function () {
            this.$('.js-logo').attr('src', this.model.get('logoSrc'));
            this.updateDeleteLogoButton();
        },
        populateTagSelect: function(tags) {
            var packageTags = this.model.get('tags');
            var selectTags = [], packageTagIDs = [];
            for(var i = 0; i < this.tags.models.length; i++) {
                selectTags.push({id: this.tags.models[i].get('id'), text: this.tags.models[i].get('text')});
            }
            for(var tagID in packageTags){
                packageTagIDs.push(packageTags[tagID].id);
            }

            var selectize = this.$('#input-tags').selectize({
                delimiter: ',',
                persist: false,
                valueField: 'id',
                options: selectTags,
                create: true
            });
            selectize[0].selectize.setValue(packageTagIDs);
        },
        deleteLogo: function () {
            this.model.unset('logo');
            this.model.set('logoSrc', '');
        }
    });

    Views.EditPackageView = Marionette.LayoutView.extend({
        template: '#packageManagerEditPackageViewTemplate',
        regions: {
            'editDetails': '#editDetails',
            'editVisibility': '#editVisibility'
        },
        ui: {
            tabs: '#editPackageTabs a'
        },
        events: {
            'click @ui.tabs': 'showTab'
        },
        showTab: function (e) {
            e.preventDefault();
            $(e.target).tab('show');
        },
        onRender: function () {
            this.model.cacheLogo();

            var editDetailsView = new Views.EditPackageDetailsView({model: this.model});
            this.editDetails.show(editDetailsView);
            if (this.model.get('isVisible') === undefined) {
                this.$('#editPackageTabs a[href="#editVisibility"]').removeClass('hidden');
                var editVisibilityView = new valamisApp.Views.EditVisibility.EditVisibilityView({packageId: this.model.get('id')});
                this.editVisibility.show(editVisibilityView);
            }
        },
        onShow: function () {
            var that = this;
            this.$('#editPackageTabs a[href="#editDetails"]').on('shown.bs.tab', function () {
                that.editDetails.currentView.focusTitle();
            });

            var activeTabSelector = '#edit' + (this.options.activeTab || 'Details');
            this.$('#editPackageTabs a[href="' + activeTabSelector + '"]').tab('show');
        },
        saveModel: function () {
            var that = this;
            this.editDetails.currentView.saveModelsTextValues();

            var d1 = $.Deferred();
            if (!that.model.get('logo') && !!that.model.get('originalLogo')) {
                that.model.deleteLogo().then(
                  function () { d1.resolve(); },
                  function () { d1.reject(); }
                );
            }
            else {
                d1.resolve();
            }

            $.when(d1).then(function() {
                that.editDetails.currentView.trigger('view:submit:image', function () {
                    lessonManager.execute('package:save', that.model);
                }, !that.model.get('logo'));
            });
        }
    });

    Views.ToolbarView = Marionette.ItemView.extend({
        template: '#packageManagerToolbarTemplate',
        ui: {
            searchField: '.js-search > input[type="text"]'
        },
        events: {
            'click .dropdown-menu > li.js-display': 'changePackageType',
            'click .dropdown-menu > li.js-scope': 'changeScope',
            'click .dropdown-menu > li.js-category': 'changeCategory',
            'click .dropdown-menu > li.js-sort': 'changeSort',
            'keyup @ui.searchField': 'changeSearchText',
            'click .js-list-view': 'listDisplayMode',
            'click .js-tile-view': 'tilesDisplayMode'
        },
        triggers: {
            "click .js-package-upload": "toolbar:upload:package"
        },
        behaviors: {
            ValamisUIControls: {}
        },
        initialize: function(){
            this.model.on('change:categories', this.render);
        },
        onValamisControlsInit: function(){
            this.$('.js-scope-filter').valamisDropDown('select', this.model.get('scope'));
            this.$('.js-display-filter').valamisDropDown('select', this.model.get('packageType'));
            this.$('.js-category-filter').valamisDropDown('select', this.model.get('selectedCategories')[0]);
            this.$('.js-sort-filter').valamisDropDown('select', this.model.get('sort'));
            this.ui.searchField.val(this.model.get('searchtext')).trigger('input');

            var displayMode = lessonManager.settings.get('displayMode');
            if (displayMode === DISPLAY_TYPE.TILES)
                this.$('.js-tile-view').addClass('active');
            else
                this.$('.js-list-view').addClass('active');
        },
        onRender: function(){
            var paginatorShowingView = new ValamisPaginatorShowing({
                language: Valamis.language,
                model: this.options.paginatorModel,
                el: this.$('#lessonManagerToolbarShowing')
            });
            paginatorShowingView.render();
        },
        changePackageType: function(e){
            this.model.set('packageType', $(e.target).attr("data-value"));
        },
        changeScope: function(e){
            this.model.set('scope', $(e.target).attr("data-value"));
        },
        changeCategory: function(e){
            this.model.set('selectedCategories', [ $(e.target).attr("data-value") ]);
        },
        changeSort: function(e){
            this.model.set('sort', $(e.target).attr("data-value"));
        },
        changeSearchText:function(e){
            var that = this;
            clearTimeout(this.inputTimeout);
            this.inputTimeout = setTimeout(function(){
                that.model.set('searchtext', $(e.target).val());
            }, 800);
        },
        listDisplayMode: function(){
            this.changeDisplayMode('list');
            this.$('.js-list-view').addClass('active');
        },
        tilesDisplayMode: function(){
            this.changeDisplayMode('tiles');
            this.$('.js-tile-view').addClass('active');
        },
        changeDisplayMode: function(displayMode){
            this.triggerMethod('toolbar:displaymode:change', displayMode);
            this.$('.js-display-option').removeClass('active');
            lessonManager.settings.set('displayMode', displayMode);
            lessonManager.settings.save();
        }
    });

    Views.PackageItemView = Marionette.ItemView.extend({
        template: '#packageManagerItemView',
        templateHelpers :function () {
            var itemVisibilityLabel;
            switch (this.visibleValue) {
                case 'some': itemVisibilityLabel = 'visibleForSomeLabel'; break;
                case 'true': itemVisibilityLabel = 'visibleForAllLabel'; break;
                case 'false': itemVisibilityLabel = 'hiddenLabel'; break;
            }

            return {
                courseId: Utils.getCourseId,
                timestamp: Date.now(),
                canChangeVisibility: Valamis.permissions.LM_MODIFY && Valamis.permissions.LM_SET_VISIBLE,
                dateString: new Date(this.model.get('creationDate')).toLocaleDateString(),
                packageAuthor: this.model.get('owner') || Valamis.language['removedUserLabel'],
                itemVisibilityLabel: Valamis.language[itemVisibilityLabel],
                downloadItemLabel: Valamis.language['download' + capitalize(this.model.get('packageType')) + 'Label']
            }

            function capitalize(string) {
                return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
            }
        },
        className: 'tile s-12 m-4 l-2',
        events: {
            'click .dropdown-menu > li.js-package-edit': 'editPackage',
            'click .dropdown-menu > li.js-package-delete': 'deletePackage',
            'click .dropdown-menu > li.js-package-export': 'exportPackage',
            'click .dropdown-menu > li.js-package-download': 'downloadPackageItem',
            'click .dropdown-menu.js-visible > li': 'changeVisibility'
        },
        behaviors: {
            ValamisUIControls: {}
        },
        onValamisControlsInit: function() {
            this.$('.js-visible-dropdown').valamisDropDown('select', this.visibleValue);
        },
        /* set the template used to display this view */
        modelEvents: {
            'model:change': 'render',
            'itemSaved': 'render'
        },
        /* used to show the order in which these method are called */
        initialize: function(options){
            var isVisible = this.model.get('isVisible');
            this.visibleValue = ( isVisible === undefined) ? 'some' :  (isVisible + '');
        },
        onRender: function(){

        },
        onShow: function(){

        },
        editPackage: function(){
            this.triggerMethod('package:edit');
        },
        deletePackage: function(){
            var that = this;
            valamisApp.execute('valamis:confirm', { message: Valamis.language['warningDeletePackageMessageLabel'] }, function(){
                that.deletePack();
            });
        },
        deletePack: function () {
            lessonManager.execute("package:remove", this.model);
        },
        exportPackage: function(){
            window.location = path.root + path.api.files + 'export/?action=EXPORT&contentType=package' +
            '&id=' + this.model.id +
            '&courseId=' + Utils.getCourseId();
        },
        downloadPackageItem: function(){
            window.location = path.root + path.api.files + 'export/?action=DOWNLOAD&contentType=package' +
                '&id=' + this.model.id +
                '&courseId=' + Utils.getCourseId();
        },
        changeVisibility: function(e) {
            var visibilityType = $(e.target).attr('data-value');
            this.visibleValue = visibilityType;
            switch (visibilityType) {
                case 'true':
                    this.model.updateVisibility({}, { isVisible: true });
                    this.model.set('isVisible', true);
                    break;
                case 'false':
                    this.model.updateVisibility({}, { isVisible: false });
                    this.model.set('isVisible', false);
                    break;
                case 'some':
                    this.model.updateVisibility({}, { isVisible: 'null' });
                    this.model.unset('isVisible', { silent: true });
                    this.triggerMethod('package:edit', 'Visibility');
                    break;
            }
        }
    });

    // TODO create PagedCollectionView
    Views.Packages = Marionette.CollectionView.extend({
        className: 'js-package-items val-row',
        childView: Views.PackageItemView,
        initialize: function (options) {
            this.paginatorModel = options.paginatorModel;
        },
        onRender: function() {
            var displayMode = lessonManager.settings.get('displayMode')|| DISPLAY_TYPE.LIST;
            this.$el.addClass(displayMode);
        },
        onShow: function(){},
        childEvents: {
            "package:edit":function(childView, activeTab){
                var editView = new lessonManager.Views.EditPackageView({
                    model: childView.model,
                    activeTab: activeTab
                });
                var editModalView = new valamisApp.Views.ModalView({
                    template: '#packageManagerEditModalTemplate',
                    contentView: editView,
                    submit: function () {
                        editView.saveModel();
                    },
                    beforeCancel: function() {
                        // restore original logo field if close modal
                        childView.model.restoreLogo();
                    },
                    onDestroy: function() {
                        valamisApp.execute('portlet:unset:onbeforeunload');
                    }
                });

                valamisApp.execute('modal:show', editModalView);
                valamisApp.execute('portlet:set:onbeforeunload', Valamis.language['loseUnsavedWorkLabel']);
            }
        }
    });

    Views.NewPackagesItemView = Marionette.ItemView.extend({
        template: '#packageManagerNewPackagesItemView',
        templateHelpers :function () {
            return {
                'courseId': Utils.getCourseId
            }
        },
        tagName: 'tr',
        events: {
            'keyup .js-new-package-title': 'updateTitle',
            'mouseover .js-package-cover-image': 'showUploadButton',
            'mouseout .js-package-cover-image': 'hideUploadButton'
        },
        modelEvents: {
            "change:logo": "onModelChanged",
            "change:logoSrc": "onModelLogoChanged"
        },
        behaviors: {
            ValamisUIControls: {},
            ImageUpload: {
                'postponeLoading': false,
                'getFolderId': function(model){
                    return 'package_logo_' + model.get('id');
                },
                'getFileUploaderUrl': function (model) {
                    return path.root + path.api.files + 'package/' + model.get('id') + '/logo';
                },
                'getImageUrl': function (model) {
                    return path.root + path.api.prefix + 'packages/' + model.get('id') + '/logo?courseId=' + Utils.getCourseId();
                },
                'fileuploaddoneCallback': function(result, uploader, context) {
                    context.view.triggerMethod('FileAdded', null, result);
                    var src = context.options.getImageUrl(context.view.model);
                    uploader.trigger("fileupload:done", {src: src, name: result.filename});
                },
                'uploadLogoMessage' : function() { return Valamis.language['uploadLogoMessage'];},
                'fileUploadModalHeader' : function() { return Valamis.language['fileUploadModalHeader']; }
            }
        },
        initialize: function(){
        },
        changing: true,
        updateTitle: function () {
            var me = this;
            var title = me.$('.js-new-package-title').val();
            me.model.set('title', title);
        },
        showUploadButton: function () {
            this.$('.js-package-settings').show();
        },
        hideUploadButton: function () {
            this.$('.js-package-settings').hide();
        },
        onModelChanged: function () {
            this.render();
        },
        onModelLogoChanged: function () {
            this.$('.js-logo').attr('src', this.model.get('logoSrc'));
        }
    });

    Views.NewPackagesView = Marionette.CompositeView.extend({
        tagName: "div",
        template: "#packageManagerNewPackageList",
        childView: Views.NewPackagesItemView,
        childViewContainer: ".js-new-package-items",
        events: {
            'click .js-cancel-upload-data' : 'cancelUpload'
        },
        initialize: function (options) {
        },
        onShow: function() {
            this.$('.js-new-package-title').focus();
        },
        cancelUpload: function () {
            lessonManager.execute('packages:remove', this.collection);
        }
    });

    Views.AppLayoutView = Marionette.LayoutView.extend({
        template: '#lessonManagerLayoutTemplate',
        regions:{
            'toolbar' : '#lessonManagerToolbar',
            'packageList' : '#lessonManagerPackages',
            'paginator': '#lessonManagerPaginator'
        },
        childEvents: {
            'toolbar:displaymode:change': function(childView,displayMode ) {
                this.packageList.currentView.$el.removeClass('list');
                this.packageList.currentView.$el.removeClass('tiles');
                this.packageList.currentView.$el.addClass(displayMode);

                valamisApp.execute('update:tile:sizes', this.packageList.currentView.$el);
            },
            'toolbar:upload:package': function (childView) {
                ////TODO scorm-package?
                var endpointparam = {
                    action: 'ADD',
                    courseId: Utils.getCourseId(),
                    contentType: 'scorm-package'
                };

                var fileUploaderUrl = path.root + path.api.files + "?" + $.param(endpointparam);

                var uploader = new FileUploader({
                    endpoint: fileUploaderUrl,
                    message:  Valamis.language['uploadPackageMessage'],
                    onFailFunction: function(e, data) {
                        var errorMessage = (_.contains(data.jqXHR.responseText, 'unsupportedPackageException'))
                            ? Valamis.language['unsupportedPackageMessage']
                            : Valamis.language['failedMessageLabel'];
                        toastr.error(errorMessage);
                        valamisApp.execute('modal:close', uploaderModalView);
                    }
                });
                var uploaderModalView = new valamisApp.Views.ModalView({
                    contentView: uploader,
                    header: Valamis.language['fileUploadModalHeader']
                });

                uploader.on("fileuploaddone", function (result) {
                    var newPackages =  new lessonManager.Entities.NewPackageCollection();
                    var i = 0;
                    if(_.isArray(result)){
                        for(i=0;i < result.length; i++){
                            var pack = result[i];
                            var newPackage = new lessonManager.Entities.Package({
                                id: pack.id,
                                filename: pack.filename,
                                title: pack.filename.substr(0,pack.filename.lastIndexOf('.')),
                                packageType: pack.contentType
                            });
                            newPackages.add(newPackage);
                        }
                    }else {
                        newPackages.add(
                            new lessonManager.Entities.Package({
                                id: result.id,
                                filename: result.filename,
                                title: result.filename.substr(0,result.filename.lastIndexOf('.')),
                                packageType: result.contentType
                            }));
                    }

                    var newPackagesView = new Views.NewPackagesView({collection : newPackages});

                    var newPackagesModalView = new valamisApp.Views.ModalView({
                        contentView: newPackagesView,
                        header: Valamis.language['uploadPackagesLabel'],
                        title: Valamis.language['newPackageTitle'],
                        description: Valamis.language['newPackageDescription'],
                        submit: function(){
                            lessonManager.execute('packages:update', newPackages);
                        }
                    });

                    valamisApp.execute('modal:close', uploaderModalView);
                    valamisApp.execute('modal:show', newPackagesModalView);
                });

                valamisApp.execute('modal:show', uploaderModalView);
            }
        },
        initialize: function() {
            var that = this;
            that.paginatorModel = lessonManager.paginatorModel;
            that.packages = lessonManager.packages;

            that.packages.on('packageCollection:updated', function (details) {
                that.updatePagination(details);
            });
        },
        onRender: function() {
            var toolbarView = new Views.ToolbarView({
                model: lessonManager.filter,
                paginatorModel: this.paginatorModel
            });

            var packageListView = new Views.Packages({
                collection: lessonManager.packages,
                paginatorModel: this.paginatorModel
            });

            this.toolbar.show(toolbarView);

            packageListView.on("render:collection", function(view){
                valamisApp.execute('update:tile:sizes', view.$el);
            });

            this.paginatorView = new ValamisPaginator({
                language: Valamis.language,
                model : this.paginatorModel,
                topEdgeParentView: this,
                topEdgeSelector: '#lessonManagerToolbarShowing'
            });
            this.paginatorView.on('pageChanged', function () {
                lessonManager.execute('packages:reload');
            }, this);

            this.paginator.show(this.paginatorView);

            this.packageList.show(packageListView);
            lessonManager.execute('packages:reload');
        },

        updatePagination: function (details, context) {
            this.paginatorView.updateItems(details.total);
        },

        /* called when the view displays in the UI */
        onShow: function() {}
    });

});