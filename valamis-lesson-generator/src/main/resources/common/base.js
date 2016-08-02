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
    var answersContainer = jQuery("#slideEntity_" + questionNumber);

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
            var answer = $question.find('.SCORMPlayerContentDisplay .playerMainArea *');
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

function prepareLessonSummary() {
    var totalQuestions = _.keys(TinCanCourseQuestions).length;
    var correctQuestions = _.values(TinCanCourseResults).filter(function(item) {return item == 1}).length;

    var hasQuestions = (totalQuestions > 0);
    var questionsProgress = (hasQuestions) ? correctQuestions / totalQuestions : 0;
    var totalProgress = getTotalProgress();

    var score = (hasQuestions) ? questionsProgress : totalProgress;

    var success = (score >= SCORE_LIMIT);

    return {
        score: score,
        progress: totalProgress,
        hasQuestion: hasQuestions,
        success: success
    }
}

function getTotalProgress(){
    var viewedSlides = _.keys(TinCanViewedSlides).length;
    var summarySlidesAmount = jQuery('.slides section > section:has(div#lesson-summary)').length;
    var totalSlides = Reveal.getTotalSlides() - summarySlidesAmount;
    return viewedSlides / totalSlides;
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

function PrepareMatchingAnswersView(id) {
    jQuery("li.acceptable.categorization"+id).draggable({
        connectToSortable:'.answerContainer.container' + id,
        cursor:'pointer',
        revert:true,
        hoverClass:'hover',
        opacity:0.4,
        revertDuration: 0,
        start: function() {
           Reveal.configure({ touch: false });
        },
        stop: function() {
            Reveal.configure({ touch: true });
        }
    });
    jQuery('.answerContainer.container' + id).droppable({
        accept:'li.acceptable.categorization'+id,
        drop:function (event, ui) {
            if ($(this).find('li').size() == 0)
            {
                jQuery(this).append(ui.draggable);
            }
        }
    });
}

function PrepareCategorizationQuestionView(id) {
    jQuery("li.acceptable.categorization"+id).draggable({
        connectToSortable:'.answerContainer.container' + id,
        cursor:'pointer',
        revert:true,
        opacity:0.4,
        revertDuration: 0,
        start: function() {
            Reveal.configure({ touch: false });
        },
        stop: function() {
            Reveal.configure({ touch: true });
            jQuery(window).trigger('resize');//Trigger on resize events
        }
    });
    jQuery('.answerContainer.container' + id).droppable({
        accept:'li.acceptable.categorization'+id,
        over:function (event, ui) {
            jQuery(this).parent().addClass('hoverBox');
            jQuery(this).parent().removeClass('noHoverBox');
        },
        out:function (event, ui) {
            jQuery(this).parent().addClass('noHoverBox');
            jQuery(this).parent().removeClass('hoverBox');
        },
        drop:function (event, ui) {
            jQuery(this).parent().removeClass('hoverBox');
            jQuery(this).append(ui.draggable);
        }
    });
}

function PreparePositioningQuestionView(id) {
    jQuery("#sortable"+id).sortable({
        placeholder: 'ui-state-highlight',
        revert:true,
        start: function() {
            Reveal.configure({ touch: false });
        },
        stop: function() {
            Reveal.configure({ touch: true });
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

    var summary = prepareLessonSummary();

    var slideId = $(Reveal.getCurrentSlide()).attr('id');
    var slideTitle = $(Reveal.getCurrentSlide()).attr('title');

    tincan.sendStatement(GetExperiencedStatement(slideId, slideTitle));
    tincan.sendStatement(GetPackageCompletedStatement(summary.score, summary.success));
}

function packageResume(lastAttemptStatement, lastSuspendedStatement) {
    AttemptStatementId = lastAttemptStatement.id;

    var stateResult = tincan.getState(
            ROOT_ACTIVITY_ID + "/_state",
        {
            agent: tincan.actor
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
            setStoredUserAnswers();
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
        if(TinCanCourseSetAnswers.hasOwnProperty(key) && value){
            TinCanCourseSetAnswers[key](value);
        }
    })
}

function packageSuspend(currentTinCanState) {
    ProcessTinCan(currentTinCanState);

    tincan.setState(
        ROOT_ACTIVITY_ID + "/_state",
        JSON.stringify({
            slide: Reveal.getIndices(),
            results: TinCanCourseResults,
            answers: TinCanUserAnswers,
            viewedSlides: TinCanViewedSlides
        }),
        {
            agent: tincan.actor
        }
    );

    tincan.sendStatement(getSuspendStatement());
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
    var url = parser.protocol + '//' + parser.host + '/question/score';

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
            extensions: {
                url: questionScore || 0
            }
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

function getSuspendStatement() {
    var context = getContext(ROOT_ACTIVITY_ID);
    return {
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
    })

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
        verb: {id: verbId || ""}}
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
    '\\\\\\\"': "\\\\\"", //Small fix if str has \"

    //Scandinavian letters
    'Ä': "&Auml;",
    'Ö': "&Ouml;",
    'Å': "&Aring;",
    'ä': "&auml;",
    'ö': "&ouml;",
    'å': "&aring;"
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
