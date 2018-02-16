/**
 * Created by igorborisov on 16.04.15.
 */
valamisApp.module("Views", function (Views, valamisApp, Backbone, Marionette, $, _) {

    Views.MainLayout = Marionette.LayoutView.extend({
        tagName: 'div',
        className: 'portlet',
        template: '#valamisAppLayoutTemplate',
        regions:{
            modals: {
                selector: '#valamisAppModalRegion',
                regionClass: Marionette.Modals
            }
        },
        onRender: function() {}
    });

    Views.ConfirmationView = Marionette.ItemView.extend({
        template: '#valamisConfirmationTemplate',
        ui: {
            confirm: '.js-confirmation',
            decline: '.js-decline'
        },
        events: {
            'click @ui.confirm': 'confirm',
            'click @ui.decline': 'decline'
        },
        initialize: function (options) {
            this.options.title = options.title || Valamis.language['deleteConfirmationTitle'];
        },
        templateHelpers: function() {
            return {
                message: this.options.message || '',
                showDontSaveButton: !!this.options.showDontSaveButton
            }
        },
        confirm: function () {
            this.trigger('confirmed', this);
        },
        decline: function () {
            this.trigger('declined', this);
        },
        onRender: function() {
            valamisApp.execute('notify', 'info', this.$el,
              {
                  'positionClass': 'toast-center',
                  'timeOut': '0',
                  'showDuration': '0',
                  'hideDuration': '0',
                  'extendedTimeOut': '0'
              }, this.options.title);
        }
    });
});