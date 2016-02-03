var contentElementModule = slidesApp.module('ContentElementModule', {
    moduleClass: GenericEditorItemModule,
    define: function (ContentElementModule, slidesApp, Backbone, Marionette, $, _) {

        ContentElementModule.View = this.BaseView.extend({
            template: '#questionElementTemplate',
            className: 'rj-element no-select question-element',
            events: _.extend({}, this.BaseView.prototype.events, {
                'click .js-item-save-settings': 'saveNotifyCorrectAnswer'
            }),
            templateHelpers: function() {
                return {
                    itemId: this.model.get('id') || this.model.get('tempId')
                }
            },
            onRender: function () {
                this.constructor.__super__.onRender.apply(this, arguments);
            },
            renderRandomQuestion: function(content, model) {
                slidesApp.slideSetModel.updateRandomAmount(true);
                slidesApp.viewId = this.cid;

                if (model === undefined) {
                    this.model.set('content', content);
                    this.model.set('slideEntityType', 'randomquestion');

                    if (this.model.get('width') == '' && this.model.get('height') == '') {
                        var baseIndent = 100;

                        var newStyles = {
                            width: jQueryValamis('.slides').width() - 2 * baseIndent,
                            height: jQueryValamis('.slides').height() - 2 * baseIndent,
                            left: baseIndent,
                            top: baseIndent
                        };

                        this.model.set(newStyles);
                    }
                }

                var that = this;
                this.model.on('change:toBeRemoved', function() {
                    slidesApp.slideSetModel.updateRandomAmount(!that.model.get('toBeRemoved'));
                });

                this.$('.js-random-question').removeClass('hidden');
                this.$('.item-content').css('background-color', '');
                this.$('.item-content').addClass('random-item');
            },
            renderQuestion: function(questionModel) {
                var questionId = questionModel.get('id'),
                    questionType = questionModel.get('questionType');

                slidesApp.viewId = this.cid;
                this.model.set('content', questionId);

                if (typeof questionType !=='undefined' && questionType != QuestionType.PlainText) {
                    questionModel.set('questionType', questionType);
                    this.model.set('slideEntityType', 'question');
                } else {
                    questionModel.set('questionType', QuestionType.PlainText);
                    this.model.set('slideEntityType', 'plaintext');
                }

                if (!slidesApp.questionCollection.get(questionId))
                    slidesApp.questionCollection.add(questionModel);
                var questionTypeString = (_.invert(QuestionType))[questionModel.get('questionType')];
                var rawAnswerCategories = [],
                    answerCategories = [];
                if (questionTypeString === 'CategorizationQuestion') {
                    for (var i in questionModel.get('answers')) {
                        var answer = questionModel.get('answers')[i];
                        rawAnswerCategories[answer.answerText.replace(/\<(\/)*p\>/g, '')] = answer.answerText.replace(/\<(\/)*p\>/g, '');
                    }
                    for (var category in rawAnswerCategories) {
                        answerCategories.push({'categoryText': category});
                    }
                }
                questionModel.set('answers', _.shuffle(questionModel.get('answers')));
                if (answerCategories.length != 0) {
                    var countRows = Math.ceil(questionModel.get('answers').length / answerCategories.length);

                    var rawsRandomAnswers = [];
                    for (var i = 0; i < countRows; i++) {
                        var randomAnswers = questionModel.get('answers').splice(0, answerCategories.length);
                        rawsRandomAnswers.push(randomAnswers);
                    }

                    questionModel.set('randomAnswers', rawsRandomAnswers);
                    questionModel.set('answers', rawsRandomAnswers);
                }

                var templateName = '#' + questionTypeString + (questionTypeString === 'PlainText' ? 'Question' : '') + 'Template';
                var questionTemplate = Mustache.to_html(jQueryValamis(templateName).html(), _.extend(
                    questionModel.toJSON(),
                    Valamis.language,
                    {
                        hasExplanation: (questionModel.get('explanationText') !== ''),
                        explanation: questionModel.get('explanationText'),
                        categories: answerCategories,
                        multipleChoice: (!questionModel.get('forceCorrectCount') || jQueryValamis.grep(questionModel.get('answers'), function (answer) {
                            return answer.isCorrect.toString() == 'true'
                        }).length > 1)
                    }
                ));


                // For each type of questions create arrays containing HTML and JS separately
                // Then apply HTML part as mustache template, join it with JS part and add to the resulting index.html
                try {
                    questionTemplate = questionTemplate;
                }
                catch (ex) {
                    if (ex instanceof URIError)
                        questionTemplate = unescape(questionTemplate);
                }

                var that = this;
                that.content.find('.content-icon-question').hide();
                that.content.find('.removed-question').addClass('hidden');
                that.content.find('.content-icon-question').siblings().remove();
                that.content.append(questionTemplate);
                that.content.css('background-color', 'transparent');

                if (!slidesApp.initializing)
                    that.updateAppearance(questionModel);

                if (that.model.get('width') == '' && that.model.get('height') == '') {
                    var contentWidth = that.getContentWidth();
                    var contentHeight = that.getContentHeight();

                    var leftIndent = Math.max((jQueryValamis('.slides').width() - contentWidth) / 2, 0),
                        topIndent = Math.max((jQueryValamis('.slides').height() - contentHeight) / 2, 0);

                    var newStyles = {
                        width: contentWidth,
                        height: contentHeight,
                        left: leftIndent,
                        top: topIndent
                    };

                    that.model.set(newStyles);
                }
            },

            updateQuestion: function (questionId, slideEntityType) {
                var that = this;
                var questionModel = new QuestionModel({});
                if (questionId) {
                    questionModel.set('id', questionId);

                    if (slideEntityType === 'plaintext') {
                        questionModel.set('questionType', QuestionType.PlainText);
                        this.model.set('slideEntityType', 'plaintext');
                    } else if (slideEntityType === 'question') {
                        this.model.set('slideEntityType', 'question');
                    }

                    questionModel.fetch({
                        success: function () {
                            that.renderQuestion(questionModel);
                        },
                        error: function (data) {
                            that.content.css({'background-color': '#1C1C1C'});
                            that.content.find('.removed-question').removeClass('hidden');
                        }
                    });
                }
                else {
                    this.content.find('.content-icon-question').show();
                    this.content.find('.content-icon-question').siblings().remove();
                    this.model.set('width', 800);
                    this.model.set('height', 100);
                    this.content.css({'background': '#1C1C1C'});
                }
            },
            updateAppearance: function (questionModel) {
                var isSaved = slidesApp.isSaved;
                var slideModel = slidesApp.getSlideModel(this.model.get('slideId')),
                    questionAppearance = getQuestionAppearance(slideModel);

                slidesApp.execute('reveal:page:changeQuestionView', questionAppearance, questionModel.get("questionType"), slideModel);
                slidesApp.actionStack.pop(); //To prevent saving this in undo history
                if (slidesApp.isSaved !== isSaved)
                    slidesApp.toggleSavedState();
            },
            saveNotifyCorrectAnswer: function () {
                var oldValue = this.model.get('notifyCorrectAnswer');
                var newValue = this.$('.js-item-notify-correct').is(':checked');

                this.model.set('notifyCorrectAnswer', newValue);

                slidesApp.viewId = this.cid;
                slidesApp.actionType = 'correctAnswerNotificationChanged';
                slidesApp.oldValue = oldValue;
                slidesApp.newValue = newValue;
                slidesApp.execute('action:push');

                this.$('.item-settings').hide();
            }
        });

        ContentElementModule.CreateModel = function () {
            var model = new ContentElementModule.Model({
                'content': '',
                'slideEntityType': 'content',
                'width': '',
                'height': '',
                'top':'',
                'left':'',
                'notifyCorrectAnswer': false
            });

            return model;
        }
    }
});

contentElementModule.on('start', function(){
    slidesApp.execute('toolbar:item:add', {slideEntityType: 'question', label: Valamis.language['questionLabel'], title: 'Question'});
});