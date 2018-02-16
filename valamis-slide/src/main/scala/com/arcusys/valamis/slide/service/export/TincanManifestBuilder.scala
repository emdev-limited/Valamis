package com.arcusys.valamis.slide.service.export

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.slide.model.{Slide, SlideEntityType, SlideSet}

import scala.xml.Elem

/**
  * Created by mminin on 21/10/2016.
  */
object TincanManifestBuilder {

  def build(slideSet: SlideSet,
            slides: Seq[Slide],
            questions: Seq[(Question, Seq[Answer])]
           ): String = {

    implicit val rootActivityId = slideSet.activityId
    val rootActivity = getLaunchActivity(slideSet)

    val slideActivities = slides
      .orderByPositions
      .map(getSlideActivity(_, questions))

    val manifest =
      <tincan xmlns="http://projecttincan.com/tincan.xsd">
        <activities>
          {rootActivity +: slideActivities}
        </activities>
      </tincan>

    manifest.toString()
  }

  private def getSlideActivity(slide: Slide, questions: Seq[(Question, Seq[Answer])])
                              (implicit rootActivityId: String): Elem = {
    val slideQuestions = slide.slideElements
      .filter {
        _.slideEntityType == SlideEntityType.Question
      }
      .flatMap { e =>
        val questionId = e.content.toLong
        questions
          .find(_._1.id.contains(questionId))
          .map(q => (e, q))
      }

    slideQuestions.headOption match {
      case None => getEmptySlideActivity(slide)
      case Some((element, (question, answers))) => getQuestionActivity(question, answers, element.id)
    }
  }

  private def getQuestionActivity(question: Question,
                                  answers: Seq[Answer],
                                  elementId: Long)
                                 (implicit rootActivityId: String) = {
    lazy val textAnswers = answers.map(_.asInstanceOf[AnswerText])
    lazy val rangeAnswers = answers.map(_.asInstanceOf[AnswerRange])
    lazy val keyValueAnswers = answers.map(_.asInstanceOf[AnswerKeyValue])

    val activityId = s"$rootActivityId/task${question.id.get}_$elementId"

    question match {
      case q: ChoiceQuestion =>
        createIteractionActivity(q, activityId, "choice", textAnswers.filter(_.isCorrect).map(_.body))

      case q: NumericQuestion =>
        val correctLines = rangeAnswers.map(a => Seq(s"${a.rangeFrom}[:]${a.rangeTo}"))
        createIteractionActivity(q, activityId, "numeric", correctLines: _*)

      case q: TextQuestion =>
        createIteractionActivity(q, activityId, "fill-in", textAnswers.filter(_.isCorrect).map(_.body))

      case q: MatchingQuestion =>
        val correctLine = keyValueAnswers.map(a => s"${a.key}[.]${a.value.get}")
        createIteractionActivity(q, activityId, "matching", correctLine)

      case q: PositioningQuestion =>
        createIteractionActivity(q, activityId, "sequencing", textAnswers.sortBy(_.position).map(_.body))

      case q: CategorizationQuestion =>
        val correctLine = keyValueAnswers.map(a => s"${a.key}[.]${a.value.get}")
        createIteractionActivity(q, activityId, "matching", correctLine)

      case q: EssayQuestion =>
        createIteractionActivity(q, activityId, "other")
    }
  }

  private def createIteractionActivity(question: Question,
                                       activityId: String,
                                       interactionType: String,
                                       correctLines: Seq[String]*) = {
    <activity id={activityId}
              type="cmi.interaction">
      <name>
        {question.title}
      </name>
      <description lang="en-US">
        {question.title}
      </description>
      <interactionType>
        {interactionType}
      </interactionType>{correctLines.map { correctResponsePatterns =>
      <correctResponsePatterns>
        {correctResponsePatterns.map { value =>
        <correctResponsePattern>
          {value}
        </correctResponsePattern>
      }}
      </correctResponsePatterns>
    }}
    </activity>
  }

  private def getEmptySlideActivity(slide: Slide)
                                   (implicit rootActivityId: String) = {

    <activity id={rootActivityId + "/slide_" + slide.id}
              type="module">
      <name>
        {slide.title}
      </name>
      <description lang="en-US">
        {slide.title}
      </description>
    </activity>
  }

  private def getLaunchActivity(slideSet: SlideSet)
                               (implicit rootActivityId: String) = {
    <activity id={rootActivityId}
              type="course">
      <name>
        {slideSet.title}
      </name>
      <description lang="en-US">
        {slideSet.description}
      </description>
      <launch lang="en-US">data/index.html</launch>
    </activity>

  }

  private implicit class SlidesExt(val slides: Seq[Slide]) extends AnyVal {
    def orderByPositions: List[Slide] = {
      val head = slides.find(s => s.leftSlideId orElse s.topSlideId isEmpty)
        .getOrElse(throw new Exception("no head slide in slideset"))

      head :: getNextSlides(head.id)
    }

    private def getDownSlides(slideId: Long): List[Slide] = {
      slides
        .find(s => s.topSlideId contains slideId)
        .map(slide => slide :: getNextSlides(slide.id))
        .getOrElse(Nil)
    }

    private def getRightSlides(slideId: Long): List[Slide] = {
      slides
        .find(s => s.leftSlideId contains slideId)
        .map(slide => slide :: getNextSlides(slide.id))
        .getOrElse(Nil)
    }

    private def getNextSlides(slideId: Long): List[Slide] = {
      getDownSlides(slideId) ++ getRightSlides(slideId)
    }
  }

}