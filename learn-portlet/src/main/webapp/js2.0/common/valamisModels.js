/**
 * Created by igorborisov on 16.04.15.
 */
valamisApp.module("Entities", function(Entities, contentManager, Backbone, Marionette, $, _){

    Entities.LiferaySiteModel = Backbone.Model.extend({
        defaults: {
            siteID: '',
            title: '',
            url: '',
            description: ''
        }
    });

    var LiferaySiteCollectionService = new Backbone.Service({ url: path.root,
        sync: {
            'read':{
                'path': path.api.courses,
                'data': function (collection, options) {
                    var filter = options.filter || '';
                    var sort = 'true';
                    if (options.sort) sort = options.sort;

                    var result ={
                        filter: filter,
                        sortAscDirection: sort
                    };

                    if(options.currentPage) result.page = options.currentPage;
                    if(options.itemsOnPage) result.count = options.itemsOnPage;

                    return result;
                }
            }
        }
    });

    Entities.LiferaySiteCollection = Backbone.Collection.extend({
        model: Entities.LiferaySiteModel,
        parse: function (response) {
            this.trigger('siteCollection:updated', { total: response.total, currentPage: response.currentPage, listed: response.records.length });
            return response.records;
        }
    }).extend(LiferaySiteCollectionService);
});