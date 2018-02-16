contentProviderManager.module('Views', function (Views, contentProviderManager, Backbone, Marionette, $, _) {

    Views.MainEditView =  Marionette.LayoutView.extend({
        template: '#contentProviderManagerMainEditView',
        ui: {
            requestCount: '.request-count-tab-label',
            tabs: '#editCourseTabs a'
        },
        regions: {
            'editProviderDetails': '#editProviderDetails',
        },
        events: {
            'click @ui.tabs': 'showTab'
        },
        showTab: function(e) {
            e.preventDefault();
            $(e.target).tab('show');
        },
        onRender: function() {
            this.editProviderView = new Views.EditProviderView({ model: this.model });
            this.editProviderDetails.show(this.editProviderView);

            var that = this;
            this.$('#editProviderTabs a[href="#editProviderDetails"]').on('shown.bs.tab', function () {
                that.editProviderView.focusName();
            });
        },
        onShow: function() {
            var activeTabSelector = '#editProviderDetails';
            this.$('#editProviderTabs a[href="'+ activeTabSelector +'"]').tab('show');
            this.$(activeTabSelector).addClass('active');
        },
        updateModel: function() {
            return this.editProviderView.updateModel();
        }
    });

    Views.EditProviderView = Marionette.ItemView.extend({
        template: '#contentProviderManagerEditCourseView',
        ui: {
            name: '.js-provider-name',
            description: '.js-provider-description',
            url: '.js-provider-url',
            imageUrl: '.js-provider-image',

            credential: 'input[name="isProviderPrivate"]',
            credentialsArea: '.js-provider-credentials-area',
            customerKey:'.js-provider-customer-key',
            customerSecret:'.js-provider-customer-secret',
            isSelective: '.js-is-selective-enable',
            sizeType: 'input[name="providerSizeType"]',
            sizeArea: '.js-provider-size-area',
            height:'.js-provider-height',
            width:'.js-provider-width'

        },
        manualUrlEdit: false,
        events: {
            'change @ui.imageUrl': 'imageUrlChanged',
            'change @ui.credential': 'credentialChanged',
            'change @ui.sizeType': 'sizeTypeChanged'
        },
        behaviors: {
            ValamisUIControls: {}
        },

        imageUrlChanged: function() {
            this.$('.js-upload-image').attr("src",this.ui.imageUrl.val());
        },

        credentialChanged: function() {
            this.showCredentialsArea(this.isPrivateProvider());
        },

        sizeTypeChanged: function() {
            this.showSizeArea(!this.isAutoSize());
        },

        checkIsPrivateProvider: function(isPrivate) {
            this.$('#providerPrivateYes').prop('checked', isPrivate);
            this.$('#providerPrivateNo').prop('checked', !isPrivate);
            this.showCredentialsArea(isPrivate);
        },

        showCredentialsArea: function(isShow) {
            if(isShow) {
                this.ui.credentialsArea.show();
            } else {
                this.ui.credentialsArea.hide();
            }
        },

        isPrivateProvider: function() {
            return this.$('#providerPrivateYes').is(':checked');
        },

        checkIsAutoSize: function(isAuto) {
            this.$('#providerSizeAuto').prop('checked', isAuto);
            this.$('#providerSizeManual').prop('checked', !isAuto);
            this.showSizeArea(!isAuto);
        },

        isAutoSize: function() {
            return this.$('#providerSizeAuto').is(':checked');
        },

        showSizeArea: function(isShow) {
            if(isShow) {
                this.ui.sizeArea.show();
            } else {
                this.ui.sizeArea.hide();
            }
        },

        updateModel: function () {
            var name = this.ui.name.val();
            if (name === '') {
                valamisApp.execute('notify', 'warning', Valamis.language['providerNameIsEmpty']);
                return false;
            }

            var description = this.ui.description.val();
            var url= this.ui.url.val();

            if (url === '') {
                valamisApp.execute('notify', 'warning', Valamis.language['providerUrlIsEmpty']);
                return false;
            }

            var imageUrl = this.ui.imageUrl.val();

            var isAutoSize = this.isAutoSize();
            var width = 'auto';
            var height = 'auto';
            if(!isAutoSize) {
                width = this.ui.width.val();
                height = this.ui.height.val();
            }

            var isPrivate = this.isPrivateProvider();
            var customerKey = '';
            var customerSecret = '';
            if(isPrivate) {
                customerKey = this.ui.customerKey.val();
                customerSecret = this.ui.customerSecret.val();
            }


            this.model.set({
                name: name,
                description: description,
                image: imageUrl,
                url: url,
                width: width,
                height: height,
                isPrivate: isPrivate,
                customerKey: customerKey,
                customerSecret: customerSecret,
                isSelective: this.ui.isSelective.is(':checked')
            });
            return true;
        },

        onRender: function(){
            this.checkIsPrivateProvider(!!this.model.get('isPrivate'));
            this.checkIsAutoSize(this.model.get('width').length==0 || this.model.get('width') === 'auto')
        },
        focusName: function() {
            this.ui.name.val(this.model.get('name')); // for cursor after last character
            this.ui.name.focus();
        }
    });
});