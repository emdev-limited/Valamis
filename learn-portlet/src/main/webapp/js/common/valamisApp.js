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

        var appregionId = 'valamisAppRegion';

        if(jQueryValamis('#' + appregionId).length <= 0) {
            var appregionHtml = '<div id="' + appregionId + '" class="valamis valamis-slides valamis-reports"></div>';
            jQueryValamis('body').append(appregionHtml);
        }

        this.addRegions({
            mainRegion: '#' + appregionId
        });

        var layoutView = new valamisApp.Views.MainLayout();

        this.mainRegion.show(layoutView);
        this.started = true;
        this.loadedResources = [];
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

valamisApp.commands.setHandler('valamis:confirm', function(options, onConfirm, onDecline){

    var dialog = new valamisApp.Views.ConfirmationView(options);
    dialog.on('confirmed',function() {
        if(_.isFunction(onConfirm)) {
            onConfirm();
        }
        dialog.destroy();
    });
    dialog.on('declined',function() {
        if(_.isFunction(onDecline)) {
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
    var resourceName = (_.isArray(options.resourceName)) 
    	? options.resourceName : [options.resourceName];
    var app = options.app;
    var appOptions = options.appOptions;
    var permissions = options.permissions;

    Valamis = Valamis || {};
    Valamis.permissions = Valamis.permissions || {};
    _.extend(Valamis.permissions, permissions);

    Valamis.language = Valamis.language || {};

    var onBankLanguageLoad  = function(properties, resourceName, deferred) {
        _.extend(Valamis.language , properties);
        valamisApp.loadedResources.push(resourceName);
        deferred.resolve();
    };

    var onBankLanguageError = function(properties, resourceName, deferred) {
        alert('Translation resource '+ + ' loading failed!');
        deferred.reject();
    };

    var getPackSource = function(resourceName, language){
        return Utils.getContextPath() + 'i18n/'+ resourceName +'_'
            + language + '.properties?v=' + Utils.getValamisVersion();
    };

    var getLanguageBank = function (resourceName, options, deferred) {
        Backbone.emulateJSON = true;
        var defaultURL = getPackSource(resourceName, defaultLanguage);
        var localizedURL = getPackSource(resourceName, options.language);

        Utils.i18nLoader(localizedURL, defaultURL,
            function (properties) {
                onBankLanguageLoad(properties, resourceName, deferred)
            },
            function (properties) {
                onBankLanguageError(properties, resourceName, deferred)
            });
    };

    var defArray = [];
    resourceName.forEach(function (name) {
        if (!_.contains(valamisApp.loadedResources, name)) {
            var deferred = jQueryValamis.Deferred();
            defArray.push(deferred);
            getLanguageBank(name, {language: Utils.getLanguage()}, deferred);
        }
    });

    jQueryValamis.when.apply(this, defArray).then(
        function() {
            app.start(appOptions);

            if (window.hasOwnProperty('elementQuery')) {
                elementQuery.init();
            }
        }
    );
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