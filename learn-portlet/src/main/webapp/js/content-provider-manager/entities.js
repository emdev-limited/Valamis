/**
 * Created by zsoltberki on 27.9.2016.
 */

contentProviderManager.module('Entities', function (Entities, contentProviderManager, Backbone, Marionette, $, _) {

    var ProviderCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.contentProviders,
                'data': function (collection, options) {

                    options.filter = options.filter || {};
                    var sort = options.filter.sort === "nameAsc";

                    return {
                        sortAscDirection: sort,
                        page: options.currentPage,
                        count: options.itemsOnPage,
                        filter: options.filter.searchtext || ''
                    };
                },
                'method': 'get'
            }
        }
    });

    var ProviderService = new Backbone.Service({
        url: path.root,
        sync: {
            'create': {
                'path': path.api.contentProviders,
                'method': 'post',
                'data': function(model){
                    return {
                        name: model.get('name'),
                        description: model.get('description'),
                        url: model.get('url'),
                        width: model.get('width'),
                        height: model.get('height'),
                        customerKey: model.get('customerKey'),
                        customerSecret: model.get('customerSecret'),
                        isPrivate: model.get("isPrivate"),
                        image: model.get("image")
                    };
                }
            },
            'update': {
                'path': path.api.contentProviders,
                'method': 'put',
                'data': function(model){
                    return {
                        id: model.get('id'),
                        name: model.get('name'),
                        description: model.get('description'),
                        url: model.get('url'),
                        width: model.get('width'),
                        height: model.get('height'),
                        customerKey: model.get('customerKey'),
                        customerSecret: model.get('customerSecret'),
                        isPrivate: model.get("isPrivate"),
                        image: model.get("image")
                    };
                }
            }
        },
        targets: {
            'deleteProvider': {
                'path': path.api.contentProviders,
                'method': 'delete',
                'data': function (model) {
                    var params = {
                        id: model.get('id')
                    };
                    return params;
                }
            }
        }
    });

    Entities.Provider = Backbone.Model.extend({
        defaults: {
            name: '',
            description: '',
            image: '',
            url: '',
            width: '',
            height: '',
            customerKey: '',
            customerSecret: '',
            isPrivate: ''
        }
    }).extend(ProviderService);

    Entities.ProviderCollection = valamisApp.Entities.LazyCollection
        .extend({
            model: Entities.Provider,
            parse: function(response) {
                this.trigger('providerCollection:updated', { total: response.total, currentPage: response.page });
                this.total = response.total;
                return response.records;
            }
        }).extend(ProviderCollectionService);

    Entities.Filter = Backbone.Model.extend({
        defaults: {
            searchtext: '',
            sort: 'nameAsc'
        }
    });

});