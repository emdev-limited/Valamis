PlayerPackageModelService = new Backbone.Service({
  url: path.root,
  targets: {
    sharePackage: {
      'path': path.api.activities,
      'data': function(model, options){
        var params =  {
          action: 'SHARELESSON',
          packageId: model.get('id'),
          comment: options.comment,
          courseId: Utils.getCourseId()
        };
        return params;
      },
      'method': 'post'
    },
    ratePackage: {
      'path': path.api.packages + 'rate/',
      'data': function(model, options){
        var params =  {
          action: 'UPDATERATING',
          id: model.get('id'),
          ratingScore: options.ratingScore
        };
        return params;
      },
      'method': 'post'
    },
    deletePackageRating: {
      'path': path.api.packages + 'rate/',
      'data': function(model){
        var params =  {
          action: 'DELETERATING',
          id: model.get('id')
        };
        return params;
      },
      'method': 'post'
    }
  }
});

PlayerPackageModel = Backbone.Model.extend({
  defaults: {
    title: '',
    summary: '',
    version: '2004 4th Edition',
    visibility: true,
    type: 'undefined'
  }
}).extend(PlayerPackageModelService);

PlayerPackageCollectionService = new Backbone.Service({ url: path.root,
  sync: {
    'read': {
      'path': path.api.packages,
      'data': function (e, options) {
        var order = jQueryValamis('#playerPackageOrder').data('value');
        var sortBy = order.split(':')[0];
        var asc = order.split(':')[1];
        var tagId = jQueryValamis('#playerPackageTags').data('value');

        var params = {
            action: 'VISIBLE',
            courseId: Utils.getCourseId(),
            pageID: jQueryValamis('#pageID').val(),
            playerID: jQueryValamis('#playerID').val(),
            filter: jQueryValamis('#playerPackageFilter').val(),
            sortBy: sortBy,
            sortAscDirection: asc,
            tagId: tagId
        };

        if (options.currentPage != undefined) params.page = options.currentPage;
        if (options.itemsOnPage != undefined) params.count = options.itemsOnPage;

        return params;
      },
      'method': 'get'
    }
  },
  targets: {
    updateIndex: {
      'path': path.api.packages + 'order/',
      'data': function (model, options) {
        var params = {
            playerID: options.playerId,
            packageIds: options.index
            };
        return params;
      },
      'method': 'post'
    }
  }
});

PlayerPackageModelCollection = Backbone.Collection.extend({
  model: PlayerPackageModel,
  parse: function (response) {
      this.trigger('packageCollection:updated', { total: response.total, currentPage: response.currentPage });

      return response.records;
  }
}).extend(PlayerPackageCollectionService);
