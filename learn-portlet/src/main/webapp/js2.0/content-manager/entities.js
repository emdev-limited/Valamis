/**
 * Created by igorborisov on 21.04.15.
 */

contentManager.module("Entities", function (Entities, contentManager, Backbone, Marionette, $, _) {

    Entities.options = {
        sortby: ['title','type'],
        sortbyIndex: 0
    };

    Entities.TopbarModel = Backbone.Model.extend({
        defaults: {
            selectedQuestions: 0,
            isRandom: false,
            defaultRandomQuestions: 1
        }
    });

    Entities.Filter = Backbone.Model.extend({
        defaults: {
            titlePattern: '',
            type: '',
            typeValue: ''
        }
    });

    var CategoryService = new Backbone.Service({
        url: path.root,
        targets: {
            'move': {
                'path': function (model) {
                    return path.api.category+"move/"+model.get('id');
                },
                'data': function (model, options) {
                    return {
                        parentId: options.parentId,
                        index: options.index,
                        courseId: model.get('courseId')
                    };
                },
                'method': 'post'
            },
            'getChildren': {
                'path': path.api.category,
                'data': function (model) {
                    return {
                        parentId: model.get('id'),
                        courseId: model.get('courseId')
                    };
                },
                'method': 'get'
            },
            'getContentAmount': {
                'path': path.api.category,
                'method': 'get',
                'data': function (model, options) {
                    return {
                        'action': 'CONTENTAMOUNT',
                        'parentId': model.get('id'),
                        'courseId': model.get('courseId')
                    };
                }
            }

        },
        sync: {
            'read': {
                'path': path.api.category,
                'data': function (model) {
                    var params = {
                        parentId: model.get('id'),
                        courseId: model.get('courseId')
                    };
                    return params;
                },
                'method': 'get'
            },
            'create': {
                'path': path.api.category,
                'data': function (model) {
                    var params = {
                        action: 'add'
                    };
                    _.extend(params, model.toJSON());
                    return params;
                },
                'method': 'post'
            },
            'update': {
                'path': function (model) {
                    return path.api.category+"update/"+model.get('id');
                },
                'data': function (model) {
                    var params = {
                    };
                    _.extend(params, model.toJSON());
                    return params;
                },
                'method': 'post'
            },
            'delete': {
                'path': function (model) {
                    return path.api.category+"delete/"+model.get('id');
                },
                'data': function (model) {
                    var params = {
                        courseId: model.get('courseId')
                    };

                    return params;
                },
                'method': 'post'
            }
        }
    });

    var ContentItemsService = new Backbone.Service({
        url: path.root,
        sync: {
            'read': {
                'path': path.api.category,
                'data': function (collection, options) {
                    var params = {
                        action: 'ALLCHILDREN',
                        parentId: options.parentId,
                        courseId: options.courseId || Utils.getCourseId()
                    };
                    return params;
                },
                'method': 'get'
            }
        }
    });

    // TODO: use string names from scala enumeration instead numbers
    var QuestionType = {
        ChoiceQuestion: 0,
        ShortAnswerQuestion: 1,
        NumericQuestion: 2,
        PositioningQuestion: 3,
        MatchingQuestion: 4,
        EssayQuestion: 5,
        CategorizationQuestion: 7,
        PlainText: 8
    };

    var QuestionAnswerType = {
        ChoiceQuestion: Entities.ChoiceAnswer,
        ShortAnswerQuestion: Entities.ShortAnswer,
        NumericQuestion: Entities.NumericAnswer,
        PositioningQuestion: Entities.PositioningAnswer,
        MatchingQuestion: Entities.MatchingAnswer,
        CategorizationQuestion: Entities.CategorizationAnswer
    };

    var QuestionService = new Backbone.Service({
        url: path.root,
        targets: {
            'move': {
                'path': function(model) {
                    if (parseInt(model.get('questionType'))==QuestionType.PlainText) {
                        return path.api.plainText+"move/"+model.get('id');
                    } else {
                        return path.api.questions+"move/"+model.get('id');
                    }
                },
                'method': 'post',
                'data': function (model, options) {
                    return {
                        'parentID': options.parentId,
                        'index': options.index,
                        'courseId': model.get('courseId')
                    };
                }
            }
        },
        sync: {
            'read': {
                'path': function (model) {
                    if (parseInt(model.get('questionType'))==QuestionType.PlainText) {
                        return path.api.plainText + model.get('id')
                    } else {
                        return path.api.questions + model.get('id')
                    }
                },
                'data': function (model) {
                    var params = {
                        'courseId': model.get('courseId')
                    };

                    return params;
                },
                'method': 'get'
            },
            'create': {
                // TODO: split to questions and plaintext
                'path': function (model) {
                    //console.log(model);
                    switch (parseInt(model.get('questionType'))) {
                        // plain text content
                        case QuestionType.PlainText:
                            return path.api.plainText + "add";

                        // questions content
                        case QuestionType.ChoiceQuestion:
                            return path.api.questions + "add/choice/";
                        case QuestionType.ShortAnswerQuestion:
                            return path.api.questions + "add/text/";
                        case QuestionType.NumericQuestion:
                            return path.api.questions + "add/numeric/";
                        case QuestionType.PositioningQuestion:
                            return path.api.questions + "add/positioning/";
                        case QuestionType.MatchingQuestion:
                            return path.api.questions + "add/matching/";
                        case QuestionType.EssayQuestion:
                            return path.api.questions + "add/essay/";
                        case QuestionType.CategorizationQuestion:
                            return path.api.questions + "add/categorization/";

                        default:
                            return path.api.questions + this.get('questionType') + "/";
                    }
                },
                'data': function (model) {
                    var params = {};

                    _.extend(params, model.toJSON(), {
                        answers: JSON.stringify(model.get('answers'))
                    });

                    return params;
                },
                'method': 'post'
            },
            'update': {
                'path': function (model) {
                    if (parseInt(model.get('questionType'))==QuestionType.PlainText) {
                        return path.api.plainText + "update/" + model.get('id');
                    } else {
                        return path.api.questions + "update/" + model.get('id');
                    }
                },
                'data': function (model) {
                    var params = {};
                    _.extend(params, model.toJSON(), {
                        answers: JSON.stringify(model.get('answers'))
                    });

                    return params;
                },
                'method': 'post'
            },
            'delete': {
                'path': function (model) {
                    if (parseInt(model.get('questionType'))==QuestionType.PlainText) {
                        return path.api.plainText + "delete/" + model.get('id');
                    } else {
                        return path.api.questions + "delete/" + model.get('id');
                    }
                },
                'data': function (model) {
                    var params = {
                        'action': 'delete',
                        'courseId': model.get('courseId')
                    };

                    return params;
                },
                'method': 'post'
            }
        }
    });


    //Answers

    Entities.AnswerModel = Backbone.Model.extend({});
    Entities.AnswerModelCollection = Backbone.Collection.extend({
        model: Entities.AnswerModel
    });

    Entities.ChoiceAnswer = Entities.AnswerModel.extend({
        defaults: {
            answerText: '',
            isCorrect: false,
            score: null
        }
    });

    Entities.ShortAnswer = Entities.AnswerModel.extend({
        defaults: {
            answerText: '',
            score: null
        }
    });

    Entities.NumericAnswer = Entities.AnswerModel.extend({
        defaults: {
            rangeFrom: 0,
            rangeTo: 0,
            score: null
        }
    });

    Entities.PositioningAnswer = Entities.AnswerModel.extend({
        defaults: {
            answerText: '',
            isCorrect: false,
            score: null
        }
    });

    Entities.MatchingAnswer = Entities.AnswerModel.extend({
        defaults: {
            answerText: '',
            matchingText: '',
            score: null
        }
    });

    Entities.CategorizationAnswer = Entities.AnswerModel.extend({
        defaults: {
            answerText: '',
            matchingText: '',
            score: null
        }
    });


    Entities.Category = Backbone.Model.extend({
        defaults: {
            title: '',
            description: '',
            parentId: null,
            arrangementIndex: 0,
            defaultIndex: null,
            contentType: 'category',
            level: 1,
            selected: false,
            courseId: '',
            hidden: false
        }
    }).extend(CategoryService);

    Entities.Question = Backbone.Model.extend({
        defaults: {
            title: '',
            text: '',
            explanationText: '',
            wrongAnswerText: '',
            rightAnswerText: '',
            questionType: QuestionType.ChoiceQuestion,
            categoryID: null,
            forceCorrectCount: false,
            isCaseSensitive: false,
            answers: '[]',
            type: '',
            arrangementIndex: 0,
            defaultIndex: null,
            contentType: 'question',
            level: 2,
            selected: false,
            courseId: '',
            hidden: false
        },
        isContent: function(){
            return this.get('questionType') == '8';
        },
        initialize: function () {
            this.updateAnswerModel();
            this.on('change', this.updateAnswerModel, this);
            this.on('sync', this.updateAnswerModel, this);
            this.set('type', this.getStringType());
            this.on('change:questionType', function () {
                this.set('type', this.getStringType());
            }, this);
        },
        getStringType: function () {
            switch (parseInt(this.get('questionType'))) {
                case QuestionType.ChoiceQuestion:
                    return 'Choice question';
                    break;
                case QuestionType.ShortAnswerQuestion:
                    return 'Short answer question';
                    break;
                case QuestionType.NumericQuestion:
                    return 'Numeric question';
                    break;
                case QuestionType.PositioningQuestion:
                    return 'Positioning question';
                    break;
                case QuestionType.MatchingQuestion:
                    return 'Matching question';
                    break;
                case QuestionType.EssayQuestion:
                    return 'Essay question';
                    break;
                case QuestionType.EmbeddedAnswerQuestion:
                    return 'Embedded question';
                    break;
                case QuestionType.CategorizationQuestion:
                    return 'Categorization question';
                    break;
                case QuestionType.PlainText:
                    return 'Plain text';
                    break;
                case QuestionType.PurePlainText:
                    return 'Plain text';
                    break;
                default:
                    return '';
            }
        },
        updateAnswerModel: function () {
            switch (this.get('questionType')) {
                case QuestionType.ChoiceQuestion:
                    this.answerModel = QuestionAnswerType.ChoiceQuestion;
                    break;
                case QuestionType.ShortAnswerQuestion:
                    this.answerModel = QuestionAnswerType.ShortAnswerQuestion;
                    break;
                case QuestionType.NumericQuestion:
                    this.answerModel = QuestionAnswerType.NumericQuestion;
                    break;
                case QuestionType.PositioningQuestion:
                    this.answerModel = QuestionAnswerType.PositioningQuestion;
                    break;
                case QuestionType.MatchingQuestion:
                    this.answerModel = QuestionAnswerType.MatchingQuestion;
                    break;
                case QuestionType.CategorizationQuestion:
                    this.answerModel = QuestionAnswerType.CategorizationQuestion;
                    break;
                default:
                    this.answerModel = null;
            }
        }
    }).extend(QuestionService);

    Entities.TreeNode = {
        idAttribute: 'uniqueId',
        getParentId: function () {},
        isNode: function () {
            return true;
        },
        isLeaf: function () {
            return true;
        },
        getDescendants: function () {
            var models = [];
            var children = this.nodes.models;

            models = models.concat(children);
            _(children).each(function (child) {
                if (child.isNode()) {
                    var descendants = child.getDescendants();
                    if (descendants.length) {
                        models = models.concat(descendants);
                    }
                }
            });

            return models;
        },
        getChild: function (filter) {
            return _.filter(this.getDescendants(), filter)[0] || {}
        },
        getChildNode: function (id) {
            return _.filter(this.getDescendants(), function (item) {
                    return item.get('id') == id && item.isNode();
                })[0] || {};
        },
        fetchChildren: function () { }
    };

    Entities.TreeNodes = Backbone.Collection.extend({
        model: Entities.TreeNode,
        comparator: function (a,b) {
            var sortByIndex = Entities.options.sortby[ Entities.options.sortbyIndex ]
                ? Entities.options.sortbyIndex
                : 0;

            var sortBy = Entities.options.sortby[ sortByIndex ],
                sortConfig = [
                    {
                        field: 'level',
                        order: 'asc'
                    },
                    {
                        field: sortBy,
                        order: 'asc'
                    }
                ];
            return this.multiFieldComparator( sortConfig, a, b );
        },
        multiFieldComparator: function(sortConfig, a, b) {
            for (var i = 0; i < sortConfig.length; i++) {
                if (a.get(sortConfig[i].field) > b.get(sortConfig[i].field)) {
                    return (sortConfig[i].order != 'desc') ? 1 : -1;
                } else if (a.get(sortConfig[i].field) == b.get(sortConfig[i].field)) {
                    // do nothing, go to next comparison
                } else {
                    return (sortConfig[i].order != 'desc') ? -1 : 1;
                }
            }
            return 0;
        },
        prepareSorting: function(){
            this.forEach(function(model,i){//set default index
                model.set('defaultIndex', i);
            });
           // this.sort();
        },
        filterNodes: function(filter) {
            this.forEach(function(model) {
                var patternMatch = _.contains(model.get('title').toLowerCase(), filter.titlePattern.toLowerCase());
                var typeMatch = (filter.type) ? model.get('contentType') === filter.type : true;

                // fix for just added plaintext content
                if (filter.type === 'plaintext') {
                   typeMatch = typeMatch ||
                       (model.get('contentType') === 'question' && model.get('questionType') === QuestionType.PlainText);
                }

                var valueMatch = (filter.type === 'question')
                    ? model.get('questionType') === parseInt(filter.typeValue)
                    : true;

                model.set('hidden', !(patternMatch && typeMatch && valueMatch));
            });
        }
    });

    Entities.TreeContentItems = Entities.TreeNodes.extend({
        model: function (attrs, options) {
            switch (attrs.contentType) {
                case 'category' :
                    return new Entities.TreeCategory(attrs, options);
                case 'question' :
                    return new Entities.TreeQuestion(attrs, options);
                case 'plaintext':
                    return new Entities.TreeQuestion(attrs, options);
            }
        },
        hasChildNodes: function () {
            return this.some(function (item) {
                return item.isNode();
            });
        }
    }).extend(ContentItemsService);

    Entities.TreeQuestion = Entities.Question.extend(Entities.TreeNode).extend({
        isNode: function () {
            return false;
        },
        getParentId: function () {
            return this.get('categoryID');
        }
    });

    Entities.TreeCategory = Entities.Category.extend(Entities.TreeNode).extend({
        defaults: {
            title: '',
            description: '',
            parentId: null,
            arrangementIndex: 0,
            defaultIndex: null,
            contentType: 'category',
            level: 1,
            selected: false,
            courseId: '',
            hidden: false
        },
        getParentId: function () {
            return this.get('parentId');
        },
        updateContentAmount: function() {
            var that = this;
            that.getContentAmount().then(function (data) {
                that.set('childrenAmount', data || 0);
            });
        },
        initialize: function (args, options) {
            var that = this;
            that.nodes = new Entities.TreeContentItems(args.children);
            that.nodes.prepareSorting();

            that.nodes.on('add', function () {
                that.updateContentAmount();
            });
            that.nodes.on('remove', function (model) {
                model.once('sync', function(){
                    that.updateContentAmount();
                });
            });

            that.nodes.on('change:title', function(){
                that.nodes.sort();
            });

            that.nodes.on('change:childrenAmount', function(){
                that.updateContentAmount();
            });
        },
        isNode: function () {
            return true;
        },
        fetchChildren: function (opts) {
            var options = {parentId: this.get('id') || '', courseId: this.get('courseId')};
            _.extend(options, opts);
            options.success = function(collection){
                collection.prepareSorting();
                //collection.sort();
            };
            return this.nodes.fetch(options);
        }
    });

});
