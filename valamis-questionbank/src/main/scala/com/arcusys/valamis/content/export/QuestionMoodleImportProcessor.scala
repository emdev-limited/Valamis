package com.arcusys.valamis.content.export

import java.io.File

import com.arcusys.valamis.content.export.model.{ImportResult, MoodleAnswer, MoodleQuestion}
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.service.{CategoryService, PlainTextService, QuestionService}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import scala.collection.immutable.HashMap
import scala.io.Source
import scala.xml.MetaData
import scala.xml.pull._


/**
 *
 * Created by pkornilov on 12.10.15.
 */

class QuestionMoodleImportProcessor(courseId: Int, userId: Long = -1)(implicit configuration: BindingModule) extends Injectable {

  override implicit def bindingModule: BindingModule = configuration

  private lazy val questionService = inject[QuestionService]
  private lazy val plaintextService = inject[PlainTextService]
  private lazy val categoryService = inject[CategoryService]

  private var curCatId: Option[Long] = None
  private var catMap = HashMap[String,Long]()

  private var moodleRootCategory: Option[Category] = None

  private val moodleRootCategoryName = "Moodle"

  var skippedCount = 0
  var allCount = 0
  var okCount = 0
  var errorCount = 0

  def importItems(file: File): ImportResult = {

    moodleRootCategory = categoryService.getByTitle(moodleRootCategoryName)

    val inputReader = new XMLEventReader(Source.fromFile(file))

    inputReader.foreach {
      case EvElemStart(_, labelStart, attrs, _) if labelStart == "question" =>
        val itemIter = inputReader.takeWhile {
          case EvElemEnd(_, labelEnd) if labelEnd == "question" => false
          case _ => true;
        }
        importItem(getAttr(attrs, "type"), itemIter)
      case _ => ()
    }
    ImportResult(allCount, okCount, errorCount, skippedCount)
  }

  private def getAttr(attrs: MetaData, name: String): String = {
    attrs.get(name).fold("")(_.headOption.fold("")(_.mkString))
  }

  private def importItem(itemType: String, iter: Iterator[XMLEvent]) {

    itemType match {
      case str if str.isEmpty => ()
      case "category" => importCategory(iter)
      case _ => importQuestion(itemType, iter)
    }
  }

  private def createCatsIfNeed(fullName: String): Unit = {
    var prevCatId: Option[Long] = None
    fullName.split("/").foreach(catName => {
      var catID = catMap.get(catName)
      if (catID.isEmpty) {
        val actualCatName = if (catName == "$course$") moodleRootCategoryName else catName

        val qc = if (actualCatName != moodleRootCategoryName || moodleRootCategory.isEmpty) {
          categoryService.create(
            Category(id = None,
              title = actualCatName,
              description = "Imported from Moodle",
              categoryId = prevCatId,
              courseId = courseId)
          )
        } else {
          moodleRootCategory.get
        }
        catID = qc.id
        catMap += (catName -> qc.id.get)
      }
      prevCatId = catID
      curCatId = catID
    }
    )
  }

  private def importCategory(iter: Iterator[XMLEvent]) {
    var catName: String = ""
    iter.foreach({
      case EvElemStart(_, label, attrs, _) =>
        val format = Some(getAttr(attrs, "format"))
        label match {
          case "category" => catName = getTextValue(iter, label, format)
        }
      case ev => ()
    })
    if (!catName.isEmpty) {
      createCatsIfNeed(catName)
    }
  }

  /**
   * Read answers for matching question and return tuple of next start event after last answer and list of answers
   *
   * This method assumed that start label(<subquestion>) for first answer already was read
   *
   * @param firstOne - first answer object
   * @param iter - question's iterator
   * @return
   */
  private def readMatchAnswers(firstOne: MoodleAnswer, iter: Iterator[XMLEvent]): (Option[XMLEvent], Seq[MoodleAnswer]) = {
    val buf = scala.collection.mutable.ListBuffer.empty[MoodleAnswer]
    var ans: MoodleAnswer = firstOne
    var isAnswerReading = true
    iter.foreach {
      case event@EvElemStart(_, label, attrs, _) =>
        label match {
          case "subquestion" =>
            isAnswerReading = true
            ans = new MoodleAnswer()
            ans.format = Some(getAttr(attrs, "format"))
          case "text" if isAnswerReading => ans.text = getFormattedText(getValue(iter, label), ans.format)
          case "answer" if isAnswerReading => ans.matchingElement = getTextValue(iter, label, ans.format)
          case _ if !isAnswerReading => return (Some(event), buf.toList)
        }
      case EvElemEnd(_, label) if label == "subquestion" => buf += ans; isAnswerReading = false;
      case _ => ()

    }

    (None, buf.toSeq)
  }

  /**
   * Read answers for question (for matching question there are another method - readMatchAnswers)
   * and return tuple of next start event after last answer and list of answers
   *
   * This method assumed that start label (<answer>) for first answer already was read
   *
   * @param firstOne - first answer object
   * @param iter - question's iterator
   * @return
   */
  private def readAnswers(firstOne: MoodleAnswer, iter: Iterator[XMLEvent]): (Option[XMLEvent], Seq[MoodleAnswer]) = {
    val buf = scala.collection.mutable.ListBuffer.empty[MoodleAnswer]
    var ans: MoodleAnswer = firstOne
    var isAnswerReading = true
    iter.foreach {
      case event@EvElemStart(_, label, attrs, _) =>
        label match {
          case "answer" =>
            isAnswerReading = true
            ans = new MoodleAnswer()
            ans.fraction = getAttr(attrs, "fraction").toInt
            ans.format = Some(getAttr(attrs, "format"))
          case "text" if isAnswerReading => ans.text = getFormattedText(getValue(iter, label), ans.format)
          case "feedback" if isAnswerReading => ans.feedback = getTextValue(iter, label, Some(getAttr(attrs, "format")))
          case "tolerance" if isAnswerReading => ans.tolerance = getValue(iter, label).toDouble
          case _ if !isAnswerReading => return (Some(event), buf.toList)
        }
      case EvElemEnd(_, endLabel) if endLabel == "answer" => buf += ans; isAnswerReading = false;
      case _ => ()
    }

    (None, buf.toSeq)
  }

  private def processEventForQuestion(event: XMLEvent, iter: Iterator[XMLEvent], mq: MoodleQuestion): Unit = event match {
    case EvElemStart(_, label, attrs, _) =>
      val format = Some(getAttr(attrs, "format"))
      label match {
        case "name" => mq.name = getTextValue(iter, label, format)
        case "defaultgrade" => mq.defaultGrade = getValue(iter, label)
        case "questiontext" => mq.text = getTextValue(iter, label, format)
        case "generalfeedback" => mq.generalFeedback = getTextValue(iter, label, format)

        case "single" => mq.single = getValue(iter, label) == "true"
        case "shuffleanswers" => mq.shuffleAnswers = getValue(iter, label) == "true"
        case "answernumbering" => mq.answerNumbering = getValue(iter, label)
        case "correctfeedback" => mq.correctFeedback = getTextValue(iter, label, format)
        case "incorrectfeedback" => mq.incorrectFeedback = getTextValue(iter, label, format)
        case "partiallycorrectfeedback" => mq.partiallyCorrectFeedback = getTextValue(iter, label, format)
        case "usecase" => mq.usecase = getValue(iter, label) == "1"

        //answers for matching questions
        case "subquestion" =>
          val ans = new MoodleAnswer()
          ans.format = format
          val (nextEvent, answers) = readMatchAnswers(ans, iter)
          mq.answers = answers
          nextEvent.foreach(processEventForQuestion(_, iter, mq))

        case "answer" =>
          val ans = new MoodleAnswer()
          ans.fraction = getAttr(attrs, "fraction").toInt
          ans.format = format
          val (nextEvent, answers) = readAnswers(ans, iter)
          mq.answers = answers
          nextEvent.foreach(processEventForQuestion(_, iter, mq))
        case _ => ()
      }

    case ev => ()
  }


  private def importQuestion(itemType: String, iter: Iterator[XMLEvent]) {
    allCount += 1
    val qTypeCode = getItemType(itemType)
    if (qTypeCode == -1) {
      skippedCount += 1
      return
    }
    val mq: MoodleQuestion = new MoodleQuestion()
    mq.qType = qTypeCode

    if (itemType == "truefalse") {
      mq.isTrueFalseQuestion = true
      mq.single = true
    }

    iter.foreach(processEventForQuestion(_, iter, mq))

    try {
      if (qTypeCode == 8) {
        //Plaintext (Description in Moodle)
        plaintextService.create(PlainText(id = None,
          categoryId = curCatId,
          title = mq.name,
          text = mq.text,
          courseId = courseId
        ))
      } else {
        val qType = Question.getTypeByCode(qTypeCode)

        //for Moodle "truefalse" questions correct/incorrect feedback for question should be taken from answer's feedback
        if (mq.isTrueFalseQuestion) {
          mq.correctFeedback = mq.answers.find(_.fraction == 100).fold("")(_.feedback) //take correct feedback from correct answer
          mq.incorrectFeedback = mq.answers.find(_.fraction == 0).fold("")(_.feedback) //take incorrect feedback from incorrect answer
        }

        val question = fromMoodleQuestion(mq)

        val answers = mq.answers.filter(ans => qType match {
          case QuestionType.Matching => !ans.text.isEmpty
          case QuestionType.Text => ans.fraction > 0
          case _ => true
        }).map(_.toValamisAnswer(qType, courseId))

        questionService.create(question, answers)
      }
      okCount += 1
    } catch {
      case e: Exception =>
        errorCount += 1
        //TODO: add logger
        println("Failed to import question: " + mq.name, e)
    }
  }

  private def fromMoodleQuestion(mq: MoodleQuestion): Question = Question.getTypeByCode(mq.qType) match {
    case QuestionType.Choice => ChoiceQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      trimHtml(mq.correctFeedback),
      trimHtml(mq.incorrectFeedback),
      mq.single,
      courseId)

    case QuestionType.Categorization => CategorizationQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      trimHtml(mq.correctFeedback),
      trimHtml(mq.incorrectFeedback),
      courseId)

    case QuestionType.Essay => EssayQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      courseId)

    case QuestionType.Matching => MatchingQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      trimHtml(mq.correctFeedback),
      trimHtml(mq.incorrectFeedback),
      courseId)

    case QuestionType.Numeric => NumericQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      trimHtml(mq.correctFeedback),
      trimHtml(mq.incorrectFeedback),
      courseId)

    case QuestionType.Positioning => PositioningQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      trimHtml(mq.correctFeedback),
      trimHtml(mq.incorrectFeedback),
      forceCorrectCount = false,
      courseId)

    case QuestionType.Text => TextQuestion(
      None,
      curCatId,
      mq.name,
      mq.text,
      trimHtml(mq.generalFeedback),
      trimHtml(mq.correctFeedback),
      trimHtml(mq.incorrectFeedback),
      mq.usecase,
      courseId)
  }

  private def getItemType(moodleType: String): Int = {
    moodleType match {
      case "multichoice" => 0
      case "truefalse" => 0
      case "shortanswer" => 1
      case "numerical" => 2
      case "matching" => 4
      case "essay" => 5;
      case "description" => 8
      case _ => -1
    }
  }


  private def getTextValue(iter: Iterator[XMLEvent], parent: String, format: Option[String]): String = {
    var res: String = ""
    var isTextElement: Boolean = false
    iter.foreach {
      case EvElemStart(_, label, _, _) if label == "text" => isTextElement = true
      case EvText(text) => if (isTextElement) {
        isTextElement = false; //sometimes there are more than one text event...
        //but we only need first one
        //other events are some kind of junk event... (for example, EvText with empty text in place where it should not to be)
        res = getFormattedText(text, format)
      }
      case EvElemEnd(_, label) if label == parent =>
        return res
      case ev => ()
    }
    res
  }

  private def getValue(iter: Iterator[XMLEvent], name: String): String = {
    var res: String = ""
    iter.foreach {
      case EvText(text) => res = text.trim
      case EvElemEnd(_, label) if label == name => return res
    }
    res
  }

  private def getFormattedText(text: String, format: Option[String]): String = {
    text.trim //TODO handle format
  }

  private def trimHtml(text: String): String = {
    val tagRegex = "<[a-zA-Z/]{1,6}>".r
    tagRegex.replaceAllIn(text, "")
  }

}
