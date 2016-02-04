valamisApp.module('Entities', function(Entities, valamisApp, Backbone, Marionette, $, _) {

  var categoryApiUrl = path.api.competences.category;

  var CategoryModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function(options) {
          return categoryApiUrl + '/' + options.id;
        },
        'method': 'get',
        'data': { courseId: Utils.getCourseId() }
      },
      'create': {
        'path': categoryApiUrl,
        'data': function (model) {
          return {
            action: 'create',
            title: model.get('title'),
            order: model.get('order'),
            parentCategoryId: model.get('parentCategoryId'),
            childCategories: JSON.stringify(model.get('childCategories')),
            childSkills: JSON.stringify(model.get('childSkills')),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'update': {
        'path': categoryApiUrl,
        'data': function (model) {
          return {
            action: 'update',
            id: model.get('id'),
            title: model.get('title'),
            order: model.get('order'),
            parentCategoryId: model.get('parentCategoryId'),
            childCategories: JSON.stringify(model.get('childCategories')),
            childSkills: JSON.stringify(model.get('childSkills')),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'delete': {
        'path': categoryApiUrl,
        'data': function (model) {
          return {
            action: 'delete',
            id: model.get('id'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      }
    }
  });

  Entities.CategoryModel = Backbone.Model.extend({
    defaults: {
      type: 'category'
    }
  }).extend(CategoryModelService);

  var CategoryCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': categoryApiUrl,
        'method': 'get',
        'data': { courseId: Utils.getCourseId() }
      }
    }
  });

  Entities.CategoryCollection = Backbone.Collection.extend({
    model: Entities.CategoryModel
  }).extend(CategoryCollectionService);

  var CategoryPathCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': categoryApiUrl + '/simple/',
        'data': function(collection, options) {
          var params = {
            'excludeChildrenOfCategoryId': options.categoryId,
            'courseId': Utils.getCourseId()
          };

          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.CategoryPathModel = Backbone.Model.extend({
    defaults: {
      type: 'categoryPath'
    }
  }).extend(CategoryModelService);

  Entities.CategoryPathCollection = Backbone.Collection.extend({
    model: Entities.CategoryPathModel
  }).extend(CategoryPathCollectionService);

  //
  var levelApiUrl = path.api.competences.level;

  var LevelModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'create': {
        'path': levelApiUrl,
        'data': function (model) {
          return {
            action: 'create',
            title: model.get('title'),
            value: model.get('value'),
            description: model.get('description'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'update': {
        'path': levelApiUrl,
        'data': function (model) {
          return {
            action: 'update',
            id: model.get('id'),
            title: model.get('title'),
            value: model.get('value'),
            description: model.get('description'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'delete': {
        'path': levelApiUrl,
        'data': function (model) {
          return {
            action: 'delete',
            id: model.get('id'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      }
    }
  });

  Entities.LevelModel = Backbone.Model.extend({
    defaults: {
      type: 'level'
    }
  }).extend(LevelModelService);


  var LevelCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': levelApiUrl,
        'method': 'get',
        'data': { courseId: Utils.getCourseId() }
      }
    }
  });

  Entities.LevelCollection = Backbone.Collection.extend({
    model: Entities.LevelModel
  }).extend(LevelCollectionService);

  //
  var skillApiUrl = path.api.competences.skill;

  var SkillModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'create': {
        'path': skillApiUrl,
        'data': function (model) {
          return {
            action: 'create',
            title: model.get('title'),
            description: model.get('description'),
            parentCategoryId: model.get('parentCategoryId'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'update': {
        'path': skillApiUrl,
        'data': function (model) {
          return {
            action: 'update',
            id: model.get('id'),
            title: model.get('title'),
            description: model.get('description'),
            parentCategoryId: model.get('parentCategoryId'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'delete': {
        'path': skillApiUrl,
        'data': function (model) {
          return {
            action: 'delete',
            id: model.get('id'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      }
    }
  });

  Entities.SkillModel = Backbone.Model.extend({
    defaults: {
      description: '',
      type: 'skill'
    }
  }).extend(SkillModelService);

  var SkillCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': skillApiUrl,
        'method': 'get',
        'data': { courseId: Utils.getCourseId() }
      }
    }
  });

  Entities.SkillCollection = Backbone.Collection.extend({
    model: Entities.SkillModel
  }).extend(SkillCollectionService);

  //
  var competenceApiUrl = path.api.competences.competence;

  var CompetenceModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'create': {
        'path': competenceApiUrl,
        'data': function (model) {
          return {
            action: 'create',
            levelId: model.get('levelId'),
            skillId: model.get('skillId'),
            userId: model.get('userId'),
            description: model.get('description'),
            favourite: model.get('favourite'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'update': {
        'path': competenceApiUrl,
        'data': function (model) {
          return {
            action: 'update',
            id: model.get('id'),
            levelId: model.get('levelId'),
            skillId: model.get('skillId'),
            userId: model.get('userId'),
            description: model.get('description'),
            favourite: model.get('favourite'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      },
      'delete': {
        'path': competenceApiUrl,
        'data': function (model) {
          return {
            action: 'delete',
            id: model.get('id'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      }
    }
  });

  Entities.CompetenceModel = Backbone.Model.extend({
    defaults: {
      description: '',
      type: 'competence'
    }
  }).extend(CompetenceModelService);

  var CompetenceCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': competenceApiUrl,
        'data': function (collection, options) {
          return {
            userId: options.userId,
            skillId: options.skillId,
            courseId: Utils.getCourseId(),
            page: options.currentPage,
            count: options.itemsOnPage
          }
        },
        'method': 'get'
      }
    },
    targets: {
      'updateCompetences': {
        'path': competenceApiUrl,
        'data': function (collection, options) {
          var params = {
            action: 'updateCollection',
            models: JSON.stringify(collection),
            deletedIds: JSON.stringify(options.unchecked),
            courseId: Utils.getCourseId()
          };
          return params;
        },
        'method': 'post'
      }
    }
  });

  Entities.CompetenceCollection = Backbone.Collection.extend({
    model: Entities.CompetenceModel,
    parse: function (response) {
      this.trigger('collectionRecords:updated', { total: response.total });
      return response.records;
    }
  }).extend(CompetenceCollectionService);

  //

  var LiferayUserModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (model, options) {
          return path.api.users + options.userId;
        },
        'data': function (collection) {
          var params = {
            courseId: Utils.getCourseId()
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.LiferayUserModel = Backbone.Model.extend({
    defaults: {
      type: 'user'
    }
  }).extend(LiferayUserModelService);

  Entities.LiferayUserCollection = Backbone.Collection.extend({
    model: Entities.LiferayUserModel
  });

  var CertificateCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': function (collection, options) {return path.api.certificateStates + options.userId},
        'data': {
            courseId: Utils.getCourseId(),
            statuses: 'Success'
          }
        },
        'method': 'get'
      }
  });

  Entities.CertificateModel = Backbone.Model.extend({
    defaults: {
      type: 'valamisCertificate'
    }
  });

  Entities.CertificateCollection = Backbone.Collection.extend({
    model: Entities.CertificateModel
  }).extend(CertificateCollectionService);

  //

  var experienceApiUrl = path.api.competences.experience;

  var ExperienceModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'delete': {
        'path': experienceApiUrl,
        'data': function (model) {
          return {
            action: 'delete',
            id: model.get('id'),
            courseId: Utils.getCourseId()
          }
        },
        'method': 'post'
      }
    }
  });

  Entities.ExperienceModel = Backbone.Model.extend({
    defaults: {
      description: '',
      type: 'experience'
    }
  }).extend(ExperienceModelService);


  var ExperienceCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': experienceApiUrl,
        'data': function (collection, options) {
          return {
            userId: options.userId,
            courseId: Utils.getCourseId()
          }
        },
        'method': 'get'
      }
    },
    targets: {
      'updateExperiences': {
        'path': experienceApiUrl,
        'data': function (collection) {
          var params = {
            action: 'alterCollection',
            models: JSON.stringify(collection),
            courseId: Utils.getCourseId()
          };
          return params;
        },
        'method': 'post'
      }
    }
  });

  Entities.ExperienceCollection = Backbone.Collection.extend({
    model: Entities.ExperienceModel
  }).extend(ExperienceCollectionService);

  //

  var SearchResultModelService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': competenceApiUrl + '/search/',
        'data': function (model, options) {
          var params = {
            searchText: options.searchText,
            courseId: Utils.getCourseId()
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.SearchResultModel = Backbone.Model.extend({

  }).extend(SearchResultModelService);


  var SearchPromptCollectionService = new Backbone.Service({
    url: path.root,
    sync: {
      'read': {
        'path': competenceApiUrl + '/promptsearch/',
        'data': function (model, options) {
          var params = {
            searchText: options.searchText,
            count: 5,
            courseId: Utils.getCourseId()
          };
          return params;
        },
        'method': 'get'
      }
    }
  });

  Entities.SearchPromptCollection = Backbone.Collection.extend({
    model: Backbone.Model.extend({}),
    parse: function (response) {
      response.forEach(function(item) {
        if (item.section == 'Certificate')  // to exclude differences with server enumeration
          item.section = 'experience';

        item.section = item.section.substring(0,1).toLowerCase() + item.section.substring(1); //  starts with uppercase in enumeration
      });

      return response;
    }
  }).extend(SearchPromptCollectionService);

});
