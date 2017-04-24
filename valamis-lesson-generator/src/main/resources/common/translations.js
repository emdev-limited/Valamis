
var LessonTranslations = function() {

    var defaultLanguage = 'en';

    var LESSON_TRANSLATIONS = {
        'en': {
            'checkLabel': 'Check answer',
            'yourAnswersLabel': 'Your answer',
            'noAnswerLabel': 'Answer is missing',
            'correctAnswersLabel': 'Correct answers',
            'correctAnswersCaseSensitiveLabel': 'Correct answer (case sensitive)',
            'explanationLabel': 'Explanation:',
            'yourAnswerIsCorrectLabel': 'Your answer was correct.',
            'yourAnswerIsIncorrectLabel': 'Your answer was incorrect.',
            'fromLabel': 'from',
            'toLabel': 'to',
            'answerLabel': 'Answer',
            // lesson summary
            'lessonCompleteLabel': 'You have successfully passed the lesson',
            'lessonFailedLabel': 'You did not pass the lesson',
            'lessonCongratulationsLabel' : 'Congratulations',
            'lessonUnfortunatelyLabel' : 'Unfortunately',
            'lessonOpenedPagesLabel': 'Opened pages',
            'lessonCorrectlyAnsweredLabel': 'Correctly answered',
            'confirmationQuestionLabel': 'Would you like to resume where you previously left off?',
            'confirmButtonLabel': 'Yes',
            'declineButtonLabel': 'No'
        },
        'fi': {
            'checkLabel': 'Tarkista',
            'yourAnswersLabel': 'Oma vastauksesi',
            'noAnswerLabel': 'Vastaus puuttuu',
            'correctAnswersLabel': 'Oikea vastaus',
            'correctAnswersCaseSensitiveLabel': 'Oikea vastaus',
            'explanationLabel': 'Vastauksen selitys:',
            'yourAnswerIsCorrectLabel': 'Vastasit oikein!',
            'yourAnswerIsIncorrectLabel': 'Nyt meni väärin.',
            'fromLabel': 'alkaen',
            'toLabel': 'päättyy',
            // lesson summary
            'lessonCompleteLabel': 'Hienoa! Opintomoduuli on nyt suoritettu!',
            'lessonFailedLabel': 'Valitettavasti et läpäissyt opintomoduulia.',
            'lessonOpenedPagesLabel': 'Avattua sivua',
            'lessonCorrectlyAnsweredLabel': 'Oikein vastattu:',
            'confirmationQuestionLabel': 'Haluatko jatkaa kohdasta johon viimeksi jäit?',
            'confirmButtonLabel': 'Kyllä',
            'declineButtonLabel': 'Ei'
        },
        'sv-se': {
            'checkLabel': 'Kontrollera',
            'yourAnswersLabel': 'Ditt svar',
            'noAnswerLabel': 'Du har inte svarat rätt',
            'correctAnswersLabel': 'Rätt svar',
            'correctAnswersCaseSensitiveLabel': 'rätt svar (case sensitiv förklaring)',
            'explanationLabel': 'förklaring:',
            'yourAnswerIsCorrectLabel': 'Ditt svar är rätt',
            'yourAnswerIsIncorrectLabel': 'Ditt svar är inkorrekt',
            'fromLabel': 'från',
            'toLabel': 'till',
            // lesson summary
            'lessonCompleteLabel': 'Du har <b>framgångsrikt</b> fullbordat din lektion!',
            'lessonFailedLabel': 'Denna gång har du inte fullbordat din lektion.',
            'lessonOpenedPagesLabel': '(Öppnad sida)',
            'lessonCorrectlyAnsweredLabel': 'Rätta svar:',
            'confirmationQuestionLabel': 'Vill du fortsätta där du tidigare slutade',
            'confirmButtonLabel': 'Ja',
            'declineButtonLabel': 'Nej'
        },
        'sv-fi': {
            'checkLabel': 'Kontrollera',
            'yourAnswersLabel': 'Ditt svar',
            'noAnswerLabel': 'Du har inte svarat rätt',
            'correctAnswersLabel': 'Rätt svar',
            'correctAnswersCaseSensitiveLabel': 'Rätt svar',
            'explanationLabel': 'Förklaring till svaret:',
            'yourAnswerIsCorrectLabel': 'Du har svarat rätt!',
            'yourAnswerIsIncorrectLabel': 'Ditt svar är inkorrekt',
            'fromLabel': 'från',
            'toLabel': 'till',
            // lesson summary
            'lessonCompleteLabel': 'Fint! Lektionen är nu fullbordat!',
            'lessonFailedLabel': 'Tyvärr lektionen är underkänd.',
            'lessonOpenedPagesLabel': 'Öppnade sidor',
            'lessonCorrectlyAnsweredLabel': 'Rätt svar:',
            'confirmationQuestionLabel': 'Vill du fortsätta där du tidigare slutade',
            'confirmButtonLabel': 'Ja',
            'declineButtonLabel': 'Nej'
        }
    };

    return {
        get: function (language) {
            var languageKey = language.toLowerCase();
            var translations = LESSON_TRANSLATIONS[languageKey];
            if(!translations) {
                languageKey = languageKey.substr(0, languageKey.indexOf('-'));
                translations = LESSON_TRANSLATIONS[languageKey];
            }
            return translations || LESSON_TRANSLATIONS[defaultLanguage];
        }
    }
}();
