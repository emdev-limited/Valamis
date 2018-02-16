achievedCertificates.module('Entities', function (Entities, achievedCertificates, Backbone, Marionette, $, _) {

    var CertificateCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.learningPaths + 'users/current/learning-paths',
                'data': function (collection, options) {
                    var params = {
                        sort: 'title',
                        status: 'Success',
                        skip: options.skip,
                        take: options.take
                    };
                    return params;
                }
            }
        }
    });

    Entities.CertificateModel = Backbone.Model.extend({
        getFullLogoUrl: function() {
            var logoUrl = this.get('logoUrl');
            return (logoUrl) ? '/' + path.api.prefix + logoUrl : '';
        }
    });

    Entities.CertificateCollection = valamisApp.Entities.LazyCollection.extend({
        model: Entities.CertificateModel,
        parse: function (response) {
            this.total = response.total;
            return response.items;
        }
    }).extend(CertificateCollectionService);

});