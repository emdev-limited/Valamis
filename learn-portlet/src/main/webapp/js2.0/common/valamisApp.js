/**
 * Created by igorborisov on 16.04.15.
 */

var Valamis = Valamis || {};

var ValamisApp = Marionette.Application.extend({
    channelName: 'valamis',
    started : false,
    initialize: function(options) {

    },
    start: function(options){

        var oldSync = Backbone.sync;
        Backbone.sync = function(method, model, options){
            _.extend(options.data,{'p_auth': Liferay.authToken});
            options.beforeSend = function(xhr){
                xhr.setRequestHeader('X-CSRF-Token', Liferay.authToken);
            };
            return oldSync(method, model, options);
        };

        var appregionId = 'valamisAppRegion';

        if(jQueryValamis('#' + appregionId).length <= 0) {
            var appregionHtml = '<div id="' + appregionId + '" class="portlet-learn-scorm portlet-learn-scorm-slides"></div>';
            jQueryValamis('body').append(appregionHtml);
        }

        this.addRegions({
            mainRegion: '#' + appregionId
        });

        var layoutView = new valamisApp.Views.MainLayout();

        this.mainRegion.show(layoutView);
        this.started = true;
    }
});

var valamisApp = new ValamisApp();


valamisApp.commands.setHandler('modal:show', function(modalView){
    valamisApp.mainRegion.currentView.modals.show(modalView);
});

valamisApp.commands.setHandler('modal:close', function(modalView){
    valamisApp.mainRegion.currentView.modals.destroy(modalView);
});

valamisApp.commands.setHandler('modal:clear', function(){
    valamisApp.mainRegion.currentView.modals.destroyAll();
});

valamisApp.commands.setHandler('update:tile:sizes', function(viewEl){
    jQueryValamis(window).trigger('recompute:tile:sizes', viewEl);
});


valamisApp.commands.setHandler('delete:confirm', function(options, onConfirm){

    var dialog = new valamisApp.Views.DeleteConfirmationView(options);
    dialog.on('deleteConfirmed',function(){
        if(onConfirm && _.isFunction(onConfirm)) {
            onConfirm();
        }
        dialog.destroy();
    });
    dialog.render();
});

valamisApp.commands.setHandler('save:confirm', function(options, onConfirm, onDecline){

    var dialog = new valamisApp.Views.SaveConfirmationView(options);
    dialog.on('saveConfirmed',function(){
        if(onConfirm && _.isFunction(onConfirm)) {
            onConfirm();
        }
        dialog.destroy();
    });
    dialog.on('saveDeclined',function(){
        if(onDecline && _.isFunction(onDecline)) {
            onDecline();
        }
        dialog.destroy();
    });
    dialog.render();
});

valamisApp.commands.setHandler('notify', function(notificationType, message, options, title){
    var toastrFunc = getToastrFunc(notificationType);
    options = options || {};
    if(!toastr.options.positionClass && !(options && options.positionClass))
        _.extend(options, { 'positionClass': 'toast-top-right' });
    if(jQueryValamis('#toast-container').children().length > 0) {
        toastr.options.hideDuration = 0;
        toastr.clear();
        toastr.options.hideDuration = 1000;
    }
    toastrFunc(message, title, options);

    function getToastrFunc(type) {
        switch(type) {
            case 'success':
                return toastr.success;
                break;
            case 'warning':
                return toastr.warning;
                break;
            case 'error':
                return toastr.error;
                break;
            case 'clear':
                return toastr.clear;
                break;
            case 'info':
            default:
                return toastr.info;
                break;
        }
    }
});

valamisApp.commands.setHandler('subapp:start', function(options){
    //TODO check required options!!!;
    var defaultLanguage = 'en';
    var resourceName = options.resourceName;
    var app = options.app;
    var appOptions = options.appOptions;
    var permissions = options.permissions;

    Valamis = Valamis || {};
    Valamis.permissions = Valamis.permissions || {};
    _.extend(Valamis.permissions, permissions);

    Valamis.language = Valamis.language || {};

    var onBankLanguageLoad  = function(properties) {
        _.extend(Valamis.language , properties);

        app.start(appOptions);
    };

    var onBankLanguageError = function() {
        alert('Translation resource loading failed!');
    };

    var getPackSource = function(language){
        return Utils.getContextPath() + 'i18n/'+ resourceName +'_' + language + '.properties?v=' + Utils.getValamisVersion();
    };

    var getLanguageBank = function (options) {
        Backbone.emulateJSON = true;
        var defaultURL = getPackSource(defaultLanguage);
        var localizedURL = getPackSource(options.language);

        Utils.i18nLoader(localizedURL, defaultURL, onBankLanguageLoad, onBankLanguageError);
    };

    getLanguageBank({language : Utils.getLanguage()});
});

valamisApp.commands.setHandler('portlet:set:onbeforeunload', function(message, callback) {
    window.onbeforeunload = function (evt) {
        if(typeof callback == 'function' && !callback()){
            return null;
        }
        var warningMessage = message;
        if (typeof evt == "undefined") {
            evt = window.event;
        }
        if (evt) {
            evt.returnValue = warningMessage;
        }
        return warningMessage;
    }
});

valamisApp.commands.setHandler('portlet:unset:onbeforeunload', function(model) {
    window.onbeforeunload = null;
});