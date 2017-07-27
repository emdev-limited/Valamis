var AchievedCertificates = Marionette.Application.extend({
  channelName:'achievedCertificates',
  initialize: function() {
    this.addRegions({
      mainRegion: '#achievedCertificatesAppRegion'
    });
  },
  onStart: function(options){
    this.certificates = new achievedCertificates.Entities.CertificateCollection([], {
        useSkipTake: true
    });
    var layoutView = new achievedCertificates.Views.AppLayoutView({collection: this.certificates});
    this.mainRegion.show(layoutView);
    this.certificates.fetchMore();
  }
});

var achievedCertificates = new AchievedCertificates();
