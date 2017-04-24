var CurriculumManager = Marionette.Application.extend({
  channelName:'curriculumManager',
  initialize: function() {
    this.addRegions({
      mainRegion: '#curriculumManagerAppRegion'
    });
  },
  onStart: function(options){
    _.extend(this, options);

    this.settings = new SettingsHelper({url: window.location.href, portlet: 'curriculumManager'});
    this.settings.fetch();

    this.paginatorModel = new PageModel();
    this.certificates = new curriculumManager.Entities.CertificateCollection();

    this.filter = new valamisApp.Entities.Filter(this.settings.get('searchParams'));
    this.filter.on('change', function(){
      curriculumManager.execute('certificates:reload', true);
      curriculumManager.settings.set('searchParams', this.filter.toJSON());
      curriculumManager.settings.save();
    }, this);

    var layoutView = new curriculumManager.Views.CertificatesList.AppLayoutView();
    this.mainRegion.show(layoutView);
  }
});

var curriculumManager = new CurriculumManager();

// handlers

curriculumManager.commands.setHandler('certificates:reload', function(filterChanged) {
  if(filterChanged) {
    curriculumManager.paginatorModel.set('currentPage', 1);
  }

  curriculumManager.certificates.fetch({
    reset: true,
    filter: curriculumManager.filter.toJSON(),
    currentPage: curriculumManager.paginatorModel.get('currentPage'),
    itemsOnPage: curriculumManager.paginatorModel.get('itemsOnPage')
  });
});
