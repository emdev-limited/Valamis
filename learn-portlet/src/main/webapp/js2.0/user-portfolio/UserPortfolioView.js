var UserAccountView = Backbone.View.extend({
  initialize: function (options) {
    this.language = options.language;

    this.certificates = new CertificateCollection();
    this.certificates.on('sync', this.addAll, this);
    this.certificates.fetch({
      userId: this.model.get('id')
    });

    this.render();
  },

  events: {
    'click .js-open-goals': 'openGoals'
  },

  addOne: function (model) {
    /*Some certificate images are not on local server
     * We need to check this. */
    var element = model.toJSON();

    var template = Mustache.to_html(jQuery('#userCertificateTileItemView').html(), _.extend({
      decodedDescription: element.description,
      isLocal: !(element.id < 0)
    }, element, this.language));

    this.$('.js-certificates-list').append(template);
  },

  addAll: function() {
    this.certificates.each(this.addOne, this);
  },

  openGoals: function (e) {
    this.trigger('viewCertificateGoals', {id: jQuery(e.currentTarget).data('certificate')});
  },

  render: function () {
    var template = Mustache.to_html(jQuery('#liferayAccountInfoView').html(), _.extend({},
      this.model.toJSON(),
      this.language
    ));
    this.$el.append(template);

    return this;
  }
});


