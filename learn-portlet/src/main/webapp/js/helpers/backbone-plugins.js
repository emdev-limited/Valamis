/*
 * A Backbone.js plugin to set specified model's attributes to 'undefined'
 * @param attrs An object containing keys of attributes to be unset
 * @param options Backbone.js options
 */
Backbone.Model.prototype.unsetValues = function(attrs, options) {
    this.set(_.object(attrs, []), options);
};