var UserAccountService = new Backbone.Service({ url: path.root,
    sync:{
        'read':{
            path: function (model, options) {
                return path.api.users + options.data.userId;
            },
            'data': function () {
                return {
                    courseId: Utils.getCourseId()
                }
            },
            'method': 'get'
        }
    }
});


var UserAccountModel = Backbone.Model.extend({
    defaults:{
        userID:"",
        name:""
    }
}).extend(UserAccountService);

var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
        'read': {
            'path': function (e, options) {
               return path.api.users + options.userId + '/certificates'
            },
            'data': function () {
                var params = {
                    withOpenBadges: true,
                    courseId: Utils.getCourseId()
                };
                return params;
            },
            'method': 'get'
          }
    }
});

var CertificateCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function (response) {
        return response.records;
    }
}).extend(CertificateCollectionService);
