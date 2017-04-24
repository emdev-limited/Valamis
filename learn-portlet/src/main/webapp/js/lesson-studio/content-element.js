var contentElementModule = slidesApp.module('ContentElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function (ContentElementModule, slidesApp, Backbone, Marionette, $, _) {

        ContentElementModule.View = this.BaseView.extend({
            template: '#questionElementTemplate',
            className: 'rj-element no-select question-element',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-item-save-settings': 'saveQuestionSettings'
            }),
            initialize: function () {
                this.constructor.__super__.initialize.apply(this, arguments);
                this.fontSizes = [];
                var that = this;
                var fontSizes = [9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72];
                _.each(fontSizes, function (item) {
                    that.fontSizes.push({value: item + 'px', text: item})
                });
                this.on('element-settings:open', this.onSettingsOpen);
                this.model.on('change:question:content', function (questionModel) {
                    that.fillContentForQuestion(questionModel);
                });
                this.model.on('change:question:random:content', function () {
                    that.fillContentForRandomQuestion();
                });
                this.model.on('change:notifyCorrectAnswer', function() {
                    var questionModel = that.model.get('questionModel');
                    if (that.model.get('slideEntityType') == 'question' && !!questionModel)
                        that.fillContentForQuestion(questionModel);
                });
            },
            templateHelpers: function () {
                return {
                    itemId: this.model.get('id') || this.model.get('tempId')
                }
            },
            onRender: function () {
                this.constructor.__super__.onRender.apply(this, arguments);
                slidesApp.questionFontSize = this.$('#js-font-size-question').selectize({
                    delimiter: ',',
                    persist: false,
                    valueField: 'value',
                    options: this.fontSizes
                })[0].selectize;
            },
            renderRandomQuestion: function(content, model) {
                slidesApp.slideSetModel.updateRandomAmount(true);
                slidesApp.viewId = this.cid;

                if (model === undefined) {
                    this.model.set('slideEntityType', 'randomquestion');

                    if (this.model.get('width') == '' && this.model.get('height') == '') {
                        var baseIndent = 100;

                        var slidesEl = $(lessonStudio.slidesWrapper + ' .slides');
                        var newStyles = {
                            width: slidesEl.width() - 2 * baseIndent,
                            height: slidesEl.height() - 2 * baseIndent,
                            left: baseIndent,
                            top: baseIndent
                        };
                        this.model.updateProperties(newStyles);
                        slidesApp.historyManager.clearHistory(null, this.model);
                    }

                    var questionModelValue = 'random-item';
                    if (this.model.get('questionModel') == questionModelValue && this.model.get('content') == content) {
                        this.model.trigger('change:questionModel');
                    } else {
                        this.model.set({ 'questionModel': questionModelValue, 'content': content });
                    }
                } else {
                    this.fillContentForRandomQuestion();
                }

                var that = this;
                this.model.on('change:toBeRemoved', function() {
                    slidesApp.slideSetModel.updateRandomAmount(!that.model.get('toBeRemoved'));
                });
            },
            renderQuestion: function(questionModel, slidesAppIsInitializing) {
                var oldSkipActionsValue = slidesApp.historyManager.getSkipActions();
                if (slidesAppIsInitializing) {
                    slidesApp.historyManager.skipActions(true);
                }                
                var questionId = questionModel.get('id'),
                    questionType = questionModel.get('questionType');

                if (typeof questionType !=='undefined' && questionType != QuestionType.PlainText) {
                    questionModel.set('questionType', questionType);
                    this.model.set('slideEntityType', 'question');
                } else {
                    questionModel.set('questionType', QuestionType.PlainText);
                    this.model.set('slideEntityType', 'plaintext');
                }

                if (!slidesApp.questionCollection.get(questionId))
                    slidesApp.questionCollection.add(questionModel);

                this.model.set({ 'questionModel': questionModel, 'content': questionId });

                var that = this;
                if (that.model.get('width') == '' && that.model.get('height') == '') {
                    var contentWidth = that.getContentWidth();
                    var contentHeight = that.getContentHeight();

                    var slidesEl = $(lessonStudio.slidesWrapper + ' .slides');
                    var leftIndent = Math.max((slidesEl.width() - contentWidth) / 2, 0),
                        topIndent = Math.max((slidesEl.height() - contentHeight) / 2, 0);

                    var newStyles = {
                        width: contentWidth,
                        height: contentHeight,
                        left: leftIndent,
                        top: topIndent
                    };

                    that.model.updateProperties(newStyles);
                    _.defer(function() {
                        slidesApp.historyManager.clearHistory(null, that.model);
                    });
                }
                if (!this.model.get('fontSize')) {
                    this.model.set('fontSize', this.$el.find('h2').css('font-size'));
                }
                else {
                    this.updateQuestionAnswers(this.model.get('fontSize'));
                }

                slidesApp.questionFontSize.setValue(this.model.get('fontSize'));

                if (slidesAppIsInitializing) {
                    slidesApp.historyManager.skipActions(oldSkipActionsValue);
                }

            },
            fillContentForQuestion: function(questionModel) {
                var questionTypeString = (_.invert(QuestionType))[questionModel.get('questionType')];
                var randomAnswers = [];

                var answersText = [],
                    categoriesTextWithDuplicate = [],
                    categoriesText = [],
                    rowsCategoriesText = [];

                if (questionTypeString === 'CategorizationQuestion') {
                    questionModel.get('answers').forEach(function(answer) {
                        categoriesTextWithDuplicate.push(answer.answerText);
                        answersText.push(answer.matchingText);
                    });

                    _.unique(categoriesTextWithDuplicate).forEach(function(category) {
                        categoriesText.push(category);
                    });

                    rowsCategoriesText = this.groupedCategories(categoriesText);
                    
                    if (categoriesText.length != 0) {
                        randomAnswers = this.shuffleAnswers(questionModel, categoriesText.length);
                    }
                }
                else if (questionTypeString === 'MatchingQuestion') {
                    questionModel.get('answers').forEach(function(answer) {
                        categoriesText.push(answer.answerText);
                        answersText.push(answer.matchingText);
                    });

                    rowsCategoriesText = this.groupedCategories(categoriesText);

                    if (categoriesText.length != 0) {
                        randomAnswers = this.shuffleAnswers(questionModel, categoriesText.length);
                    }
                }
                var templateName = '#' + questionTypeString + (questionTypeString === 'PlainText' ? 'Question' : '') + 'Template';
                var questionTemplate = Mustache.to_html($(templateName).html(), _.extend(
                    questionModel.toJSON(),
                    Valamis.language,
                    {
                        randomAnswers: randomAnswers,
                        hasExplanation: !!(questionModel.get('explanationText')),
                        explanation: questionModel.get('explanationText'),
                        rowsCategoriesText: rowsCategoriesText,
                        answersText: answersText,
                        multipleChoice: (!questionModel.get('forceCorrectCount') ||
                            questionModel.get('answers').filter(function(answer) {
                                return !!answer.isCorrect
                            }).length > 1),
                        autoShowAnswer: this.model.get('notifyCorrectAnswer')
                    }
                ));

                this.content.find('.content-icon-question').hide();
                this.content.find('.removed-question').addClass('hidden');
                this.content.find('.content-icon-question').siblings().remove();
                this.content.html(questionTemplate);
            },
            groupedCategories: function(categoriesText) {
                var rowsCategoriesText = [];
                var categoriesInRow = 3;
                var categoriesInCurrentRow = categoriesInRow;
                var categoriesAmount = categoriesText.length;
                var categoriesRowsAmount = Math.ceil(categoriesAmount / categoriesInRow);
                var distributedCategories = 0;

                for (var row = 0; row < categoriesRowsAmount; row++) {
                    rowsCategoriesText[row] = [];
                    categoriesInCurrentRow = (categoriesAmount - distributedCategories >= categoriesInRow)
                        ? categoriesInRow
                        : (categoriesAmount - distributedCategories);
                    for (var column = 0; column < categoriesInCurrentRow; column++) {
                        rowsCategoriesText[row][column] = categoriesText[row*categoriesInRow + column];
                    }
                    distributedCategories = distributedCategories + categoriesInCurrentRow;
                }

                return rowsCategoriesText;
            },
            shuffleAnswers: function (questionModel, amountOfCategories) {
                var randomAnswers = [];
                var shuffled = _.shuffle(questionModel.get('answers'));
                while (shuffled.length > 0){
                    randomAnswers.push(shuffled.splice(0, amountOfCategories));
                }

                return randomAnswers;
            },
            fillContentForRandomQuestion: function() {
                this.$('.js-random-question').removeClass('hidden');
                this.$('.item-content').css('background-color', '');
                this.$('.item-content').addClass('random-item');
            },
            updateQuestion: function (model) {
                var questionId = model.get('content');
                var slideEntityType = model.get('slideEntityType');
                var that = this;
                var questionModel = new QuestionModel({});
                if (questionId) {
                    questionModel.set('id', questionId);

                    if (slideEntityType === 'plaintext') {
                        questionModel.set('questionType', QuestionType.PlainText);
                    }
                    else if (slideEntityType == 'randomquestion') {
                        that.renderRandomQuestion(questionId, model);
                        return;
                    }

                    var oldQuestionModel = model.toJSON().questionModel;
                    var slidesAppIsInitializing = slidesApp.initializing;
                    if (oldQuestionModel && questionId === oldQuestionModel.get('id')) {
                        model.unset('questionModel', {silent: true});
                        that.renderQuestion(oldQuestionModel, true);
                    } else {
                        questionModel.fetch({
                            success: function () {
                                that.renderQuestion(questionModel, slidesAppIsInitializing);
                            },
                            error: function (data) {                                
                                that.content.css({'background-color': '#1C1C1C'});
                                that.content.find('.removed-question').removeClass('hidden');
                            }
                        });
                    }
                }
                else {
                    this.content.find('.content-icon-question').show();
                    this.content.find('.content-icon-question').siblings().remove();
                    this.model.set('width', 800);
                    this.model.set('height', 100);
                    this.content.css({'background': '#1C1C1C'});
                }
            },
            updateQuestionAnswers: function(questionFontSize) {
                // set font sizes for labels, input and textarea in question element
                // otherwise these elements will have styles according aui and reveal styles
                this.$('.answer-container > label, .answer-textarea, .question-short-answers')
                    .css('font-size', questionFontSize);
            },
            saveQuestionSettings: function () {
                var notifyCorrectAnswer = this.$('.js-item-notify-correct').is(':checked');
                var questionFontSize = this.$('#js-font-size-question').val();
                this.model.set({
                    'notifyCorrectAnswer': notifyCorrectAnswer
                });
                this.model.updateProperties({fontSize: questionFontSize});
                this.$('.item-settings').hide();
                this.updateQuestionAnswers(questionFontSize);
            },
            onSettingsOpen: function(){
                slidesApp.questionFontSize.setValue(this.model.get('fontSize'));
            }
        });

        ContentElementModule.CreateModel = function () {
            var model = new ContentElementModule.Model({
                content: '',
                slideEntityType: 'content',
                fontSize: '18px',
                width: '',
                height: '',
                top:'',
                left:'',
                notifyCorrectAnswer: false
            });

            return model;
        }
    }
});

contentElementModule.on('start', function(){
    slidesApp.execute('toolbar:item:add', {slideEntityType: 'question', label: Valamis.language['questionLabel'], title: 'Question'});
});