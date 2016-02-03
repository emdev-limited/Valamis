package com.arcusys.learn.models

class MoodleQuestion(
  var qType:Int = -1,
  var isTrueFalseQuestion:Boolean = false,
  var name:String="",
  var text:String="",
  var defaultGrade:String="",
  var generalFeedback:String="",
  var single:Boolean=false,
  var correctFeedback:String="",
  var partiallyCorrectFeedback:String="",
  var incorrectFeedback:String="",
  var shuffleAnswers:Boolean=false,
  var answerNumbering:String="",
  var usecase:Boolean=false,
  var answers:Seq[MoodleAnswer]=Seq()
)

