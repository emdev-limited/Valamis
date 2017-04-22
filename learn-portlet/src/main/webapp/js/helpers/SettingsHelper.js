var SettingsHelper = Backbone.Model.extend({
  initialize: function(options){
    this.url = options.url;
    this.portlet = options.portlet;
  },
  save: function() {
    try {
      if (window.localStorage && JSON.stringify) {
        localStorage.setItem(this.portlet + '_' + this.url, JSON.stringify(this.toJSON()))
      }
    }catch(e) {
      console.log(e);
      localStorage.clear();
      console.log("An error occurred during localeStorage.setItem. We suspect that the storage structure is corrupted, so we have cleared it.");
    }
  },
  fetch: function() {
    try {
      if (window.localStorage && JSON.parse) {
        var data = localStorage.getItem(this.portlet + '_' + this.url);
        this.set(JSON.parse(data));
      }
    }catch(e) {
      console.log(e);
      localStorage.clear();
      console.log("An error occurred during localeStorage.getItem. We suspect that the storage structure is corrupted, so we have cleared it.");
    }
  }
});