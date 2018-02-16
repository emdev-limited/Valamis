/*
* Predefine backbone sync with needed headers
* */

if (!Backbone.isSyncOverriden) {
    Backbone.emulateJSON = true;
    var oldSync = Backbone.sync;
    Backbone.sync = function (method, model, options) {
        _.extend(options.data, {'p_auth': Liferay.authToken});
        options.beforeSend = function (xhr) {
            xhr.setRequestHeader('X-CSRF-Token', Liferay.authToken);
            xhr.setRequestHeader('X-VALAMIS-Layout-Id', Utils.getPlid());
            xhr.setRequestHeader('X-VALAMIS-Course-Id', Utils.getCourseId());
            xhr.setRequestHeader('layoutId', Utils.getPlid());
        };
        return oldSync(method, model, options);
    };
    Backbone.isSyncOverriden = true;
}