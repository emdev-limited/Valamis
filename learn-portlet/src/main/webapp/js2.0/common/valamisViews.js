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

    Views.DeleteConfirmationView = Marionette.ItemView.extend({
        template: '#valamisDeleteConfirmationTemplate',
        events: {
            'click .js-confirmation': 'confirmDelete'
        },
        initialize: function (options) {
            this.options.title = options.title || Valamis.language['deleteConfirmationTitle'];
        },
        templateHelpers: function(){
            return {
               message:  this.options.message || Valamis.language['deleteConfirmationMessage']
            }
        },
        confirmDelete: function () {
            this.trigger('deleteConfirmed', this);
        },
        onRender: function(){
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

    Views.SaveConfirmationView = Marionette.ItemView.extend({
        template: '#valamisSaveConfirmationTemplate',
        events: {
            'click .js-confirmation': 'confirmSave',
            'click .js-decline': 'declineSave'
        },
        initialize: function (options) {
            this.options.title = options.title || Valamis.language['saveConfirmationTitle'];
        },
        templateHelpers: function(){
            return {
                message:  this.options.message || Valamis.language['saveConfirmationMessage']
            }
        },
        confirmSave: function () {
            this.trigger('saveConfirmed', this);
        },
        declineSave: function () {
            this.trigger('saveDeclined', this);
        },
        onRender: function(){
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