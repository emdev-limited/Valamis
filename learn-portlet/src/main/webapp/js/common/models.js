/**
 * Created by igorborisov on 12.03.15.
 */


Valamis = (function(){

    var tagService = new Backbone.Service({ url: path.root,
        sync: {
            'read': {
                path: path.api.tags,
                'method': "get"
            }

        },
        targets: {
            'getPackagesTags': {
                'path': path.api.packages + 'tags',
                'data': function (collection, options) {
                    var params = {
                        courseId: options.courseId,
                        playerId: options.playerId
                    };
                    return params;
                },
                'method': 'get'
            }
        }
    });

    var Tag = Backbone.Model.extend({
        defaults: {
            id: '',
            text: ''
        }
    }).extend(tagService);

    var TagCollection = Backbone.Collection.extend({
        parse: function(response){
            return response;
        },
        model: Tag
    }).extend(tagService);

    // liferay organizations
    var OrganizationModel = Backbone.Model.extend({
        defaults: {
            'id': '',
            'name': ''
        }
    });

    var OrganizationCollectionService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': function () {
                return  path.api.organizations;
            }
        }
    });

    var OrganizationCollection = Backbone.Collection.extend({
          model: OrganizationModel
      })
      .extend(OrganizationCollectionService);

    return {
        Tag: Tag,
        TagCollection: TagCollection,
        OrganizationCollection: OrganizationCollection
    };
}());

