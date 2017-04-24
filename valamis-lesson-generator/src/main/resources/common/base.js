"use strict";

// TODO: refactor func name format

var ROOT_ACTIVITY_ID = '';
var ROOT_ACTIVITY_TITLE = '';
var ROOT_ACTIVITY_DESCRIPTION = '';
var SCORE_LIMIT = null;
var VERSION = '';
var LESSON_LABELS = {};

var TinCanCourseModules = {},
    TinCanCourseHelpers = {},
    TinCanCourseResults = {},
    TinCanCourseSetAnswers = {},
    TinCanUserAnswers = {},
    TinCanViewedSlides = {},
    TinCanCourseQuestions = {},
    TinCanCourseQuestionsAll = {},
    TinCanCourseQuestionsContent = {};

var AttemptStatementId = '';

var tincan = null;

function ProcessTinCan(id) {
    if(id) {
        if (TinCanCourseModules.hasOwnProperty(id)) {
            var moduleResult = TinCanCourseModules[id](tincan);
            TinCanCourseResults[id] = moduleResult;
        }

        var questionId = id.substr(id.indexOf("_") + 1);
        var answersId = 'collectAnswers_' + questionId;

        if (TinCanCourseHelpers.hasOwnProperty(answersId)) {
            var userAnswer = TinCanCourseHelpers[answersId]();
            TinCanUserAnswers[id] = userAnswer.rawLearnerResponse || userAnswer.learnerResponse;
        }
    }
}

function moveAnswers(questionNumber) {
    var answersContainer = jQuery('#slideEntity_' + questionNumber);
    var leftIndent = Math.max((jQuery('.slides').width() - answersContainer.width()) / 2, 0),
        topIndent = Math.max((jQuery('.slides').height() - answersContainer.height()) / 2, 0);

    answersContainer.css({top: topIndent, left: leftIndent});
}

function markSlideAsViewed() {
    var indices = Reveal.getIndices();
    TinCanViewedSlides[indices.h + '_' + indices.v] = 1;
}

function checkIsLessonSummary() {
    if (jQuery('#lesson-summary', Reveal.getCurrentSlide()).length){
        ComposeLessonSummarySlide();
    } else {
        markSlideAsViewed();
    }
}

function disableQuestion(revealSlide) {
    var $slide = jQuery(revealSlide);

    // slidechanged event triggered by windows resize event,
    // and window resize event triggered in categorizations answers also by some reason
    if(!$slide) return;

    var question = $slide.find('.question-element')[0];
    if (!question) return;

    var $question = jQuery(question);
    if ( !$question.attr('disabled')) {
        var currentIndices = Reveal.getIndices(revealSlide);
        var currentIndicesString = currentIndices.h + '_' + currentIndices.v;
        var isViewed = _.some(_.keys(TinCanViewedSlides), function(item){
            return item === currentIndicesString;
        });

       if(isViewed) {
            var type = $slide.data('state').split('_')[0];
            var answer = $question.find('.js-valamis-question .js-answers-options *');
            if (_.contains(['matching', 'categorization'], type)){
                answer.draggable({disabled: true});
            } else if (type == 'positioning') {
                answer.sortable({disabled: true});
            } else {
                answer.attr('disabled', true);
            }
           $question.attr('disabled', true);
        }
    }
}

function getLessonSuccess() {
    var questionsStatistic = getLessonQuestionsStatistic();
    var totalProgress = getLessonTotalProgressPercent();
    var score = (questionsStatistic.hasQuestion) ? questionsStatistic.questionsProgressPercent : totalProgress;
    var success = (score >= SCORE_LIMIT*100);

    return success;
}

function getLessonQuestionsStatistic() {
    var totalQuestions = _.keys(TinCanCourseQuestions).length;
    var hasQuestions = (totalQuestions > 0);
    var numberOfCorrectAnswers = hasQuestions ? _.values(TinCanCourseResults).filter(function(item) {return item == 1}).length : 0;
    var questionsProgressPercent = (hasQuestions) ? (numberOfCorrectAnswers / totalQuestions * 100)|0 : 0;

    return {
        totalQuestions: totalQuestions,
        hasQuestion: hasQuestions,
        numberOfCorrectAnswers: numberOfCorrectAnswers,
        questionsProgressPercent: questionsProgressPercent
    }
}

function getLessonPagesStatistic() {
    var viewedPages = _.keys(TinCanViewedSlides).length;
    var summaryPagesAmount = jQuery('.slides section > section:has(div#lesson-summary)').length;
    var totalPages = Reveal.getTotalSlides() - summaryPagesAmount;
    var pagesProgressPercent = viewedPages / totalPages * 100|0;
    var viewedPagesSuccess = (pagesProgressPercent >= SCORE_LIMIT*100);
    var fullCompleted = pagesProgressPercent == 100;

    return {
        viewedPages: viewedPages,
        totalPages: totalPages,
        pagesProgressPercent: pagesProgressPercent,
        success: viewedPagesSuccess,
        fullCompleted: fullCompleted
    }
}

function getLessonTotalProgressPercent() {
    var lessonPages = getLessonPagesStatistic();
    var lessonQuestions = getLessonQuestionsStatistic();
    var totalProgress = lessonQuestions.hasQuestion ? lessonQuestions.questionsProgressPercent : lessonPages.pagesProgressPercent;

    return totalProgress;
}

function cretePagesStatiticPie(rate) {
    var lessonPagesStatistic = getLessonPagesStatistic();
    var success = lessonPagesStatistic.success;
    var resultColor = success ? 'success' : 'fail';

    var options = {
        rate: rate,
        resultColor: resultColor,
        pieContainer: '.js-pages-pie-graphic'
    };

    createPie(options);
}

function createQuestionsStatisticPie(rate) {
    var success = getLessonSuccess();
    var resultColor = success ? 'success' : 'fail';

    jQuery('#lesson-summary .lesson-statistic').addClass('both-pies');

    var options = {
        rate: rate,
        resultColor: resultColor,
        pieContainer : '.js-questions-pie-graphic'
    };

    createPie(options);
}

function createPie(options) {
    var height = 80,
        width = 80,
        margin = 2,
        lineWidth = 4;

    var defaultOptions = {
        rate: 0,
        resultColor: 'success',
        pieContainer : '.js-questions-pie-graphic'
    };

    options = options || {};
    options.rate = options.rate || defaultOptions.rate;

    var data=[
                {proposal: "proposal1", rate: options.rate, legend: options.rate, unitSign: '%' },
                {proposal: "proposal2", rate: 100 - options.rate, legend : '', unitSign: ''}
            ];
    
    var radius = Math.min(width- 2*margin, height- 2*margin) / 2;

    var arc = d3.svg.arc()
        .outerRadius(function (d) {
            if(d.data.radius) return radius*d.data.radius;
            return radius;})
        .innerRadius(Math.min(width- 2*margin, height- 2*margin) / 2 - lineWidth);

    var pie = d3.layout.pie()
        .sort(null)
        .value(function(d) { return d.rate; });

    var svgPie = $(options.pieContainer + ' svg');
    svgPie.remove();

    var svg = d3.select(options.pieContainer)
        .append("svg")
        .attr("class", "axis pie-circle")
        .attr("width", width)
        .attr("height", height)
        .append("g")
        .attr('class', options.resultColor)
        .attr("transform", "translate(" +(width / 2) + "," + (height / 2 ) + ")");
    
    svg.append("circle")
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("r", width / 2);

    var g = svg.selectAll(".arc")
        .data(pie(data))
        .enter().append("g")
        .attr("class", "arc");

    g.append("path")
        .attr("d", arc);

    var text = g.append('text')
        .attr('x', 0)
        .attr('y', 10)
        .attr('text-anchor', 'middle');

        text.append("tspan")
            .attr('class', 'rate-value')
            .text(function(d) { return d.data.legend;});

        text.append("tspan")
            .attr("class", "rate-unit-sign")
            .text(function(d) { return d.data.unitSign;});
}

(function($){

    $.fn.shuffle = function() {

        var allElems = this.get(),
            getRandom = function(max) {
                return Math.floor(Math.random() * max);
            },
            shuffled = $.map(allElems, function(){
                var random = getRandom(allElems.length),
                    randEl = $(allElems[random]).clone(true)[0];
                allElems.splice(random, 1);
                return randEl;
            });

        this.each(function(i){
            $(this).replaceWith($(shuffled[i]));
        });

        return $(shuffled);

    };

})(jQuery);

function PrepareMatchingAnswersView(idWithQuestionNumber) {
    jQuery('li.js-acceptable.categorization' + idWithQuestionNumber).draggable({
        helper: 'clone',
        cursor:'pointer',
        revert: 'invalid',
        revertDuration: 100,
        start: function() {
            Reveal.configure({ touch: false });
            jQuery(this).addClass('ui-state-highlight highlight-previous-position');
            jQuery('.ui-draggable-dragging').addClass('dragging-answer-item');
        },
        stop: function() {
            Reveal.configure({ touch: true });
            jQuery(this).removeClass('ui-state-highlight highlight-previous-position');
            jQuery('.ui-draggable-dragging').removeClass('dragging-answer-item');
        }
    });
    jQuery('.js-answer-container.container' + idWithQuestionNumber).droppable({
        accept:'li.js-acceptable.categorization' + idWithQuestionNumber,
        over:function (event, ui) {
            jQuery(this).parent()
                .addClass('hoverBox')
                .removeClass('noHoverBox');
            if (!jQuery(this).find('li').is('.js-answer-item')) {
                showShadow(this);
            }
        },
        out:function (event, ui) {
            jQuery(this).parent()
                .addClass('noHoverBox')
                .removeClass('hoverBox');
            deleteShadow(this);
        },
        drop:function (event, ui) {
            if (jQuery(this).find('li[id^="matchingAnswer"]').size() == 0) {
                jQuery(this)
                    .append(ui.draggable)
                    .parent().removeClass('hoverBox');
                deleteShadow(this);
            }
        }
    });
    jQuery('.js-answer-container-source.container' + idWithQuestionNumber).droppable({
        accept:'li.js-acceptable.categorization' + idWithQuestionNumber,
        over:function (event, ui) {
            jQuery(this).parent()
                .addClass('hoverBox')
                .removeClass('noHoverBox');
                showShadow(this);
        },
        out:function (event, ui) {
            jQuery(this).parent()
                .addClass('noHoverBox')
                .removeClass('hoverBox');
            deleteShadow(this);
        },
        drop:function (event, ui) {
                jQuery(this)
                    .append(ui.draggable)
                    .parent().removeClass('hoverBox');
                deleteShadow(this);
        }
    });
}

function PrepareCategorizationQuestionView(idWithQuestionNumber) {
    jQuery('li.js-acceptable.categorization' + idWithQuestionNumber).draggable({
        helper: 'clone',
        cursor:'pointer',
        revert: 'invalid',
        revertDuration: 100,
        start: function() {
            Reveal.configure({ touch: false });
            jQuery(this).addClass('ui-state-highlight highlight-previous-position');
            jQuery('.ui-draggable-dragging').addClass('dragging-answer-item');
        },
        stop: function() {
            Reveal.configure({ touch: true });
            jQuery(window).trigger('resize');//Trigger on resize events
            jQuery(this).removeClass('ui-state-highlight highlight-previous-position');
            jQuery('.ui-draggable-dragging').removeClass('dragging-answer-item');
        }
    });
    jQuery('.js-answer-container.container' + idWithQuestionNumber).droppable({
        accept:'li.js-acceptable.categorization' + idWithQuestionNumber,
        over:function (event, ui) {
            jQuery(this).parent()
                .addClass('hoverBox')
                .removeClass('noHoverBox');
            showShadow(this);
        },
        out:function (event, ui) {
            jQuery(this).parent()
                .addClass('noHoverBox')
                .removeClass('hoverBox');
            deleteShadow(this);
        },
        drop:function (event, ui) {
            jQuery(this)
                .append(ui.draggable)
                .parent().removeClass('hoverBox');
            deleteShadow(this);
        }
    });
    jQuery('.js-answer-container-source.container' + idWithQuestionNumber).droppable({
        accept:'li.js-acceptable.categorization' + idWithQuestionNumber,
        over:function (event, ui) {
            jQuery(this).parent()
                .addClass('hoverBox')
                .removeClass('noHoverBox');
            showShadow(this);
        },
        out:function (event, ui) {
            jQuery(this).parent()
                .addClass('noHoverBox')
                .removeClass('hoverBox');
            deleteShadow(this);
        },
        drop:function (event, ui) {
            jQuery(this)
                .append(ui.draggable)
                .parent().removeClass('hoverBox');
            deleteShadow(this);
        }
    });
}

function showShadow(categoryContainer) {
    var categoryDroppableContainer = jQuery(categoryContainer);
    if (!categoryDroppableContainer.find('li').is('.ui-state-highlight')) {
        var shadowDragElement = '<li class="categorization-answer-item highlight-previous-position js-shadow-drag-element"></li>';
        jQuery(categoryContainer).append(shadowDragElement);
        resizeCategoryContainer(categoryDroppableContainer);
    }
}

function deleteShadow(categoryContainer) {
    var categoryDroppableContainer = jQuery(categoryContainer);
    if (categoryDroppableContainer) {
        categoryDroppableContainer.find('.js-shadow-drag-element').remove();
    }
}

function resizeCategoryContainer(categoryDroppableContainer) {
    var answerDraggableHeight = jQuery('.ui-draggable-dragging').outerHeight();
    categoryDroppableContainer.find('.js-shadow-drag-element').outerHeight(answerDraggableHeight);
}

function PreparePositioningQuestionView(idWithQuestionNumber) {
    jQuery('#sortable' + idWithQuestionNumber).sortable({
        placeholder: 'ui-state-highlight',
        // revert:true,
        helper: 'clone',
        cursor:'pointer',
        revert: 'invalid',
        revertDuration: 100,
        start: function() {
            Reveal.configure({ touch: false });
            jQuery(this).addClass('ui-state-highlight highlight-previous-position');
            jQuery('.ui-draggable-dragging').addClass('dragging-answer-item');
        },
        stop: function() {
            Reveal.configure({ touch: true });
            jQuery(window).trigger('resize');//Trigger on resize events
            jQuery(this).removeClass('ui-state-highlight highlight-previous-position');
            jQuery('.ui-draggable-dragging').removeClass('dragging-answer-item');
        }
    });
    jQuery('#sortable' + idWithQuestionNumber).droppable({
        accept:'li.ui-sortable-handle',
        over:function (event, ui) {
            var answerDraggableHeight = jQuery('.ui-sortable-helper').outerHeight();
            jQuery(this).find('.ui-state-highlight').outerHeight(answerDraggableHeight);
        }
    });
}

function shuffle(myArray) {
    var copiedArray = myArray.slice();
    var i = copiedArray.length;
    if (i == 0) return [];
    while (--i) {
        var j = Math.floor(Math.random() * ( i + 1 ));
        var n = copiedArray[i];
        copiedArray[i] = copiedArray[j];
        copiedArray[j] = n;
    }
    return copiedArray;
}

function packageBegin() {
    tincan = new TinCan({
        url: window.location.href,
        activity: {
            id: ROOT_ACTIVITY_ID,
            definition: {
                name: {
                    "en-US": ROOT_ACTIVITY_TITLE
                },
                description: {
                    "en-US": ROOT_ACTIVITY_DESCRIPTION
                },
                type: "http://adlnet.gov/expapi/activities/assessment"
            }
        }
    });

    prepareAttemptedStatement();
}

function isUserAnonymous(actor){
    return actor && actor.account && actor.account.name && actor.account.name == 'anonymous';
}

function prepareAttemptedStatement() {

    if(!isUserAnonymous(tincan.actor)) {
        var lastAttemptStatement = getLastStatement(ROOT_ACTIVITY_ID, "http://adlnet.gov/expapi/verbs/attempted");

        if (lastAttemptStatement) {
            var lastResultByActivityId = getLastStatement(ROOT_ACTIVITY_ID);

            // for support both old and new valamis packages (will be fixed with VALAMIS-3367)
            var lastResult = (lastResultByActivityId)
                ? lastResultByActivityId
                : getLastStatement(lastAttemptStatement.id);

            var wasSuspended = (lastResult && lastResult.verb.id == "http://adlnet.gov/expapi/verbs/suspended");
            var isLastAttempt = (lastResultByActivityId)
                ? (lastResultByActivityId.context.statement && lastResultByActivityId.context.statement.id == lastAttemptStatement.id)
                : true;

            if (wasSuspended && isLastAttempt) {
                packageResume(lastAttemptStatement, lastResult);
                return;
            }
        }
    }

    AttemptStatementId = tincan.sendStatement(GetPackageAttemptedStatement()).statement.id;
}

function packageEnd(currentTinCanState) {
    ProcessTinCan(currentTinCanState);

    var slideId = $(Reveal.getCurrentSlide()).attr('id');
    var slideTitle = $(Reveal.getCurrentSlide()).attr('title');
    var score = getLessonTotalProgressPercent()/100;
    var success = getLessonSuccess();

    tincan.sendStatement(GetExperiencedStatement(slideId, slideTitle));
    tincan.sendStatement(GetPackageCompletedStatement(score, success));
}

function packageResume(lastAttemptStatement, lastSuspendedStatement) {
    AttemptStatementId = lastAttemptStatement.id;

    var stateResult = tincan.getState(
            ROOT_ACTIVITY_ID + "/_state",
        {
            agent: tincan.actor,
            registration: lastSuspendedStatement.id

        }
    );
    
    if (stateResult
        && stateResult.state
        && stateResult.state.contents) {

        var stateContent = JSON.parse(stateResult.state.contents);

        if (stateContent.results)
            TinCanCourseResults = stateContent.results;

        if (stateContent.answers) {
            TinCanUserAnswers = stateContent.answers;
        }

        onOpenToastr();
        toastr.info(jQuery('#startConfirmationView').html(), '',
            {
                'tapToDismiss': false,
                'positionClass': 'toast-center toast-center-for-viewer',
                'timeOut': '0',
                'showDuration': '0',
                'hideDuration': '0',
                'extendedTimeOut': '0'
            }
        ).addClass('toastr-start-package-confirmation');
        setTranslations(toastr.getContainer());
        toastr.getContainer().find('.js-confirmation').click(function() {
            onToastrConfirm();
            onCloseToastr();
        });
        toastr.getContainer().find('.js-decline').click(function() {
            onToastrDecline();
            onCloseToastr();
        });
    }

    tincan.deleteState(
        ROOT_ACTIVITY_ID + "/_state",
        {
            agent: tincan.actor
        }
    );

    function onToastrConfirm() {
        setStoredUserAnswers();
        if (stateContent.viewedSlides) {
            TinCanViewedSlides = stateContent.viewedSlides;
            _.keys(TinCanViewedSlides).forEach(function (item) {
                var indices = item.split('_');
                toggleNavigation(indices[0], indices[1]);
            });
        }

        if (stateContent.slide) {
            Reveal.slide(stateContent.slide.h, stateContent.slide.v, stateContent.slide.f);
            toggleNavigation(stateContent.slide.h, stateContent.slide.v);
        }
        checkIsLessonSummary();
        tincan.sendStatement(getResumeStatement(lastSuspendedStatement.result.duration));

        jQuery('#packageDuration').trigger('setTimer', [DURATION * 60 - lastSuspendedStatement.result.duration]);
    }

    function onToastrDecline() {
        //We start new attempt as old one is declined
        packageEnd(currentTinCanState);
        AttemptStatementId = tincan.sendStatement(GetPackageAttemptedStatement()).statement.id;
    }

    function onOpenToastr(){
        jQuery('.content-header', window.parent.document).addClass('val-inactive');
        jQuery('.controls').css('pointer-events','none');
        jQuery('body').addClass('val-inactive');
    }

    function onCloseToastr() {
        jQuery('.content-header', window.parent.document).removeClass('val-inactive');
        jQuery('.controls').css('pointer-events','');
        jQuery('body').removeClass('val-inactive');
        toastr.remove();
    }
}

function setStoredUserAnswers() {
    _.each(TinCanUserAnswers, function(value, key) {
        if (TinCanCourseSetAnswers.hasOwnProperty(key) && value) {
            TinCanCourseSetAnswers[key](value);
        }
    })
}

function packageSuspend(currentTinCanState) {
    ProcessTinCan(currentTinCanState);

    var suspendedStmt = getSuspendStatement();

    tincan.setState(
        ROOT_ACTIVITY_ID + "/_state",
        JSON.stringify({
            slide: Reveal.getIndices(),
            results: TinCanCourseResults,
            answers: TinCanUserAnswers,
            viewedSlides: TinCanViewedSlides
        }),
        {
            agent: tincan.actor,
            registration: suspendedStmt.id
        }
    );

    tincan.sendStatement(suspendedStmt);
}

function onPackageSlideEnd(slideId, slideTitle) {
    tincan.sendStatement(GetExperiencedStatement(slideId, slideTitle));
}

// record the results of a question
function GetQuestionAnswerStatement(id, questionText, title, questionType, learnerResponse, correctAnswer, wasCorrect, score, questionScore){
    //send question info
    // and score
    var scaledScore = score / 100;

    var object = {
        id: ROOT_ACTIVITY_ID +'/' + id,
        definition: {
            type: 'http://adlnet.gov/expapi/activities/cmi.interaction',
            name: {
                'en-US': title
            },
            description: {
                'en-US': replaceStringTags(questionText)
            },
            interactionType: questionType,
            correctResponsesPattern: [
                String(replaceStringTags(correctAnswer))
            ]
        }
    };

    var parser = document.createElement('a');
    parser.href = ROOT_ACTIVITY_ID;
    var rootUrl = parser.protocol + '//' + parser.host + '/question/score';

    var extObject = {};
    extObject[rootUrl] = questionScore || 0;

    return {
        verb: {
            "id": "http://adlnet.gov/expapi/verbs/answered",
            "display": {
                "en-US": "answered"
            }
        },
        object: object,
        result: {
            score: {
                scaled: scaledScore,
                raw: score,
                min: 0,
                max: 100
            },
            response: String(replaceStringTags(learnerResponse)),
            success: wasCorrect,
            extensions: extObject
        },
        context: getContext(ROOT_ACTIVITY_ID)
    };
}

function GetPackageCompletedStatement(score, success) {
    var context = getContext(ROOT_ACTIVITY_ID);

    return {
        verb: {
            "id": "http://adlnet.gov/expapi/verbs/completed",
            "display": {
                "en-US": "completed"
            }
        },
        object: {
            id: ROOT_ACTIVITY_ID,
            definition: {
                type: 'http://adlnet.gov/expapi/activities/course',
                name: { 'en-US': ROOT_ACTIVITY_TITLE }
            }
        },
        result: {
            score: { scaled: score },
            success: success,
            duration: getDurationFromStatements()
        },
        context: {
            contextActivities: context.contextActivities,
            statement: context.statement,
            revision: 'version ' + VERSION
        }
    };
}

function GetPackageAttemptedStatement(){
    return {
        verb: {
            "id": "http://adlnet.gov/expapi/verbs/attempted",
            "display": {"en-US": "attempted"}
        },
        object: {
            id: ROOT_ACTIVITY_ID,
            definition: {
                type: 'http://adlnet.gov/expapi/activities/course',
                name: { 'en-US': ROOT_ACTIVITY_TITLE }
            }
        },
        context: {
            contextActivities: {
                grouping: [{id: ROOT_ACTIVITY_ID}]
            },
            revision: 'version ' + VERSION
        }
    };
}

function GetVideoStatement(verbName, videoId, videoTitle, videoDuration, start, finish) {
    var verbId = 'http://activitystrea.ms/schema/1.0/play';
    var stmnt = {
        verb: {
            id: verbId,
            display: {
                'en-US': verbName
            }
        },
        object: {
            id: 'http://www.youtube.com/watch?v=' + videoId,
            definition: {
                type: 'http://activitystrea.ms/schema/1.0/video',
                name: {
                    'en-US': videoTitle
                },
                extensions: {
                    'http://id.tincanapi.com/extension/duration': videoDuration
                }
            }
        },
        context: {
            contextActivities: {
                grouping: {
                    id: ROOT_ACTIVITY_ID
                },
                category: {
                    id: 'http://id.tincanapi.com/recipe/video/base/1'
                }
            }
        }
    };
    switch(verbName) {
        case 'play':
            stmnt.context.extensions = {
                'http://id.tincanapi.com/extension/starting-point': start
            };
            break;
        case 'paused':
            stmnt.context.extensions = {
                'http://id.tincanapi.com/extension/ending-point': finish
            };
            stmnt.verb.id = 'http://activitystrea.ms/schema/1.0/pause';
            break;
        case 'watched':
        case 'skipped':
            stmnt.context.extensions = {
                'http://id.tincanapi.com/extension/starting-point': start,
                'http://id.tincanapi.com/extension/ending-point': finish
            };
            if(verbName === 'watched')
                stmnt.verb.id = 'http://activitystrea.ms/schema/1.0/watch';
            else
                stmnt.verb.id = 'http://activitystrea.ms/schema/1.0/skipped';
            break;
        case 'completed':
            stmnt.context.extensions = {
                'http://id.tincanapi.com/extension/ending-point': finish
            };
            stmnt.verb.id = 'http://activitystrea.ms/schema/1.0/complete';
            break;
    }
    return stmnt;
}

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
}

function getSuspendStatement() {
    var context = getContext(ROOT_ACTIVITY_ID);
    var id = guid();
    return {
        id: id,
        verb: {
            "id": "http://adlnet.gov/expapi/verbs/suspended",
            "display": {"en-US": "suspended"}
        },
        object: {
            id: ROOT_ACTIVITY_ID,
            definition: {
                type: 'http://adlnet.gov/expapi/activities/course',
                name: {'en-US': ROOT_ACTIVITY_TITLE}
            }
        },
        result: {
            //Saving in seconds
            duration: getDurationFromStatements()
        },
        context: {
            contextActivities: context.contextActivities,
            statement: context.statement,
            revision: 'version ' + VERSION
        }
    };
}

function getResumeStatement(duration) {
    var context =  getContext(ROOT_ACTIVITY_ID);
    return {
        verb: {
            "id": "http://adlnet.gov/expapi/verbs/resumed",
            "display": {"en-US": "resumed"}
        },
        object: {
            id: ROOT_ACTIVITY_ID,
            definition: {
                type: 'http://adlnet.gov/expapi/activities/course',
                name: {'en-US': ROOT_ACTIVITY_TITLE}
            }
        },
        result: {
            duration: duration
        },
        context: {
            contextActivities: context.contextActivities,
            statement: context.statement,
            revision: 'version ' + VERSION
        }
    };
}

function getContext(parentActivityId, category) {
    if(category) {
        var categoryUri = category.substr(0, category.lastIndexOf('/'));
        var categoryName = category.substr(category.lastIndexOf('/') + 1);
    }
    var contextActivities = {
        grouping: [
            {id: ROOT_ACTIVITY_ID}
        ]
    };
    if(category)
        contextActivities.category = {
            id: categoryUri,
            definition: {
                name: {
                    'en-US': categoryName
                }
            }
        };

    var statementRef = new TinCan.StatementRef({
        "objectType": "StatementRef",
        "id": AttemptStatementId
    });

    return {
        contextActivities: contextActivities,
        statement: statementRef
    };
}

function getLastStatement(activityId, verbId) {
    var result = tincan.getStatements({params: {
        activity: {id: activityId},
        agent: tincan.actor,
        related_activities: true,
        limit: 1,
        verb: {id: verbId || ""},
        until: new Date().toISOString()}
    });

    try {
        return result.statementsResult.statements[0];
    } catch (e) {
        return null;
    }
}

function getDurationFromStatements() {
    var startTime = new Date();
    var duration = 0;

    //Check last resumed statement send or attempted one
    var lastResumed = getLastStatement(AttemptStatementId, "http://adlnet.gov/expapi/verbs/resumed");
    if(lastResumed) {
        duration = parseInt(lastResumed.result.duration);
        startTime = new Date(lastResumed.timestamp);
    } else {
        var lastAttempted = getLastStatement(ROOT_ACTIVITY_ID, "http://adlnet.gov/expapi/verbs/attempted");
        if(lastAttempted)
            startTime = new Date(lastAttempted.timestamp);
    }

    return Math.round((new Date() - startTime) / 1000) + duration
}

function startTimer(duration, display){
    var timer = duration, hours, minutes, seconds;

    display.bind('setTimer', function(e, value) { timer = value });
    setInterval(function () {
        hours = parseInt(timer / 3600, 10);
        minutes = parseInt(timer % 3600 / 60, 10);
        seconds = parseInt(timer % 3600 % 60, 10);

        hours = hours < 10 ? "0" + hours : hours;
        minutes = minutes < 10 ? "0" + minutes : minutes;
        seconds = seconds < 10 ? "0" + seconds : seconds;

        display.text(hours + ":" + minutes + ":" + seconds);

        if (--timer < 0) window.frames.top.jQuery("#SCORMNavigationExit").click();
    }, 1000);
}

var escapeArray = {
    '<': "&lt;",
    '>': "&gt;",
    '&': "&amp;",
    '"': "&quot;",
    '\'': "&#39;",
    '\\': "&#92;",
    '\\\\\\\"': "\\\\\"" //Small fix if str has \"
};

var unescapeElement = function(str) {
    _.each(escapeArray, function (value, key) {
        str = str.split(value).join(key)
    });
    return str;
};

var escapeElement = function(str) {
    _.each(escapeArray, function (value, key) {
        str = str.split(key).join(value)
    });
    return str;
};

var answersToJSON = function(answers) {
    // Escape all html quotes in answers to be correctly parsed into JSON
    // Replace all tags with /> to >, as we get not closed tags in answer
    return JSON.parse(unescapeElement(answers.split('&amp;quot;').join('\\"')).replace(/[\s]+\/>/gi, '>'));
};

// ugly hack for checking matching and categorization questions
// problems are caused by differences between encodings in dataToCompare object
// and user answer (that is gotten from html element)
var htmlDecode = function(str) {
    str = unescapeElement(str);
    return jQuery('<textarea />').html(str).text();
};

var replaceStringTags = function(str) { // replace tags in string by space to exclude sticking words
    return (typeof str == 'string') ? str.replace(/<\/?[^>]+>/g, ' ') : str;
};

function setTranslations(container) {
    var locale = getParameterByName('locale') || 'en';
    LESSON_LABELS =  LessonTranslations.get(locale);

    container.find('.js-localized-label').each(function(index) {
        var labelData = jQuery(this).attr('data-value');
        jQuery(this).html(LESSON_LABELS[labelData]);
    });
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
      results = regex.exec(location.search);
    return results === null ? "" : results[1].replace(/\+/g, " ");
}

function recalculateFeedbackSlideHeight(id, questionNumber) {
    var answersContainer = jQuery('#slideEntity_' + questionNumber);
    var userAnswers = answersContainer.find('.js-question-feedback');

    var itemContent = answersContainer.find('.js-item-content');
    var slideContainer = jQuery('.slides');
    var explanationContainer = userAnswers.find('.js-question-explanation');
    var correctnessAnswerContainer = jQuery('#answerCorrectness' + id + '_' + questionNumber);

    answersContainer.css('height', 'auto');
    itemContent.css('height', 'auto');

    var answerCorrectnessContainerHeight = (correctnessAnswerContainer && correctnessAnswerContainer.height()) || 0;
    var slideHeight = (slideContainer && slideContainer.height()) || 0;
    var explanationHeight = (explanationContainer && explanationContainer.height()) || 0;

    var heightAdditionalInformation = slideHeight + answerCorrectnessContainerHeight + explanationHeight;

    //for old view of questions
    var answersContainerTop = parseFloat(answersContainer.css('top'));
    var additionalHeight = answersContainer.outerHeight() + answersContainerTop;
    if (additionalHeight > heightAdditionalInformation) {
        heightAdditionalInformation = additionalHeight;
    }

    slideContainer.css('height', heightAdditionalInformation);
    jQuery('.reveal-scroll-container').css('height', heightAdditionalInformation);
}

function refreshStatusDiv() {
    var dom = document.querySelector('.reveal'),
        currentSlide = dom.querySelector('.slides>section.present');
    document.getElementById('aria-status-div').textContent = currentSlide.innerText;
}