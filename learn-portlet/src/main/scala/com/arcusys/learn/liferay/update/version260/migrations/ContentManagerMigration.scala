package com.arcusys.learn.liferay.update.version260.migrations

import com.arcusys.learn.liferay.update.version260.slide.SlideTableComponent
import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.schema.ContentTableComponent
import com.arcusys.valamis.core.SlickProfile
import com.liferay.portal.kernel.log.LogFactoryUtil

import scala.collection.mutable
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{GetResult, JdbcBackend, StaticQuery}

class ContentManagerMigration(val db: JdbcBackend#DatabaseDef,
                              val driver: JdbcProfile)
  extends ContentTableComponent
  with SlickProfile
  with SlideTableComponent {

  val log = LogFactoryUtil.getLog(getClass)

  val slideEntities: mutable.ListBuffer[(Long, String, String)] = mutable.ListBuffer()

  import driver.simple._

  implicit val catGetResult = GetResult[Category] { r =>
    val id = r.nextLong()
    Category(
      Some(id),
      r.nextStringOption().getOrElse(""),
      r.nextStringOption().getOrElse(""),
      r.nextLongOption(),
      r.nextIntOption().getOrElse(getDefaultCourseId(id, "learn_lfquestioncategory")).asInstanceOf[Long]
    )
  }

  case class OldContent(isPlainText: Boolean, id: Long, question: Option[BaseQuestion], plainText: Option[PlainText])

  implicit val qGetResult = GetResult[OldContent] {r =>
      val id = r.nextLong()
      val qTypeCode = r.nextIntOption().getOrElse(-1)
      qTypeCode match {
        case 8|9 => OldContent(
          isPlainText = true,id,
          None,
          Some(PlainText(
            id = Some(id),
            categoryId = r.nextLongOption(),
            title = r.nextStringOption().getOrElse(""),
            text = r.nextStringOption().getOrElse(""),
            courseId = r.nextIntOption().getOrElse(getDefaultCourseId(id,"learn_lfquestion")).asInstanceOf[Long]
          ))
          )
        case _ => OldContent(isPlainText = false, id,
          Some(BaseQuestion(
            id = Some(id),
            categoryId = r.nextLongOption(),
            title = r.nextStringOption().getOrElse(""),
            text = r.nextStringOption().getOrElse(""),
            courseId = r.nextIntOption().getOrElse(getDefaultCourseId(id,"learn_lfquestion")).asInstanceOf[Long],
            explanationText = r.nextStringOption().getOrElse(""),
            rightAnswerText = r.nextStringOption().getOrElse(""),
            wrongAnswerText = r.nextStringOption().getOrElse(""),
            forceCorrectCount = r.nextBooleanOption().getOrElse(false),
            isCaseSensitive = r.nextBooleanOption().getOrElse(false),
            questionType = Question.getTypeByCode(qTypeCode)
          )),
          None)
      }
    }

  implicit val textAnswerGetResult = GetResult[AnswerText] {r =>
    val id = r.nextLong()
    AnswerText(
    id = Some(id),
    body = r.nextStringOption().getOrElse(""),
    isCorrect = r.nextBooleanOption().getOrElse(false),
    questionId = r.nextLongOption(),
    courseId = -1,
    position = r.nextIntOption().getOrElse(0),
    score = r.nextDoubleOption()
  )}

  implicit val rangeAnswerGetResult = GetResult[AnswerRange]  {r =>
    val id = r.nextLong()
    r.skip//description
    r.skip//correct
    AnswerRange(
    id = Some(id),
    questionId = {
     val qId = r.nextLongOption()
     r.skip//answerposition
     qId},
    score = r.nextDoubleOption(),
    courseId = -1,
    rangeFrom = r.nextDoubleOption().getOrElse(0.0),
    rangeTo = r.nextDoubleOption().getOrElse(0.0)
  )}

  implicit val keyValueAnswerGetResult = GetResult[AnswerKeyValue]  {r =>
    val id = r.nextLong()
    AnswerKeyValue(
    id = Some(id),
    key = r.nextStringOption().getOrElse(""),
    questionId = {
     r.skip//correct
     val qId = r.nextLongOption()
     r.skip//answerposition
     qId},
     courseId = -1,
     score = {
     val score = r.nextDoubleOption()
     r.skip//rangefrom
     r.skip//rangeto
     score
     },
     value = r.nextStringOption()
    )
  }

  def getOldAnswersByQuestionId(questionId: Long, qType: QuestionType)(implicit session: JdbcBackend#Session): Seq[Answer] = {
    val sql = s"SELECT id_,description,correct,questionid, answerposition, score, rangefrom, rangeto, matchingtext " +
      s"FROM learn_lfanswer " +
      s"INNER JOIN learn_lfquizanswerscore on learn_lfquizanswerscore.answerid=learn_lfanswer.id_ " +
      s"where questionid = ?"
    val query =
      qType match {
        case QuestionType.Choice | QuestionType.Text | QuestionType.Positioning =>
          StaticQuery.query[Long,AnswerText](sql)
        case QuestionType.Numeric =>
          StaticQuery.query[Long,AnswerRange](sql)
        case QuestionType.Matching | QuestionType.Categorization =>
          StaticQuery.query[Long,AnswerKeyValue](sql)
        case _ => return Seq()
      }
    query(questionId).list
  }

  def getOldContentWithoutCategory()(implicit session: JdbcBackend#Session): Seq[OldContent] = {
    val query = StaticQuery.queryNA[OldContent](s"SELECT id_,questiontype,categoryid,title,description,courseid,explanationtext,rightanswertext, " +
    s"wronganswertext,forcecorrectcount,casesensitive FROM learn_lfquestion " +
    s"where categoryid is null")
    query.list
  }

  def getOldContentByCategoryId(catId: Long)(implicit session: JdbcBackend#Session): Seq[OldContent] = {
    val query = StaticQuery.query[Long,OldContent](s"SELECT id_,questiontype,categoryid,title,description,courseid,explanationtext,rightanswertext, " +
    s"wronganswertext,forcecorrectcount,casesensitive FROM learn_lfquestion " +
    s"where categoryid = ?")
    query(catId).list
  }

  def getCategoriesByCategoryId(catId: Long)(implicit session: JdbcBackend#Session): Seq[Category] = {
    val query = StaticQuery.query[Long,Category](s"SELECT id_,title,description,parentid,courseid FROM learn_lfquestioncategory " +
    s"where parentid = ?")
    query(catId).list
  }

  def getOldCategoriesWithoutParent(implicit session: JdbcBackend#Session): Seq[Category] = {
    StaticQuery.queryNA[Category](s"SELECT id_,title,description,parentid,courseid FROM learn_lfquestioncategory where parentid is null").list
  }

  private def getDefaultCourseId(entityId: Long, tableName: String): Int = {
    log.warn(s"$tableName with id $entityId has no courseId")
    -1
  }

  private def migrateQuestion(q: BaseQuestion, newCatId: Option[Long])(implicit session: JdbcBackend#Session): Long = {
    val newId = (questions returning questions.map(_.id)).insert(q.copy(categoryId = newCatId))
    migrateAnswers(q, newId)
    newId
  }

  private def migrateAnswers(oldQuestion: BaseQuestion, newQuestionId: Long)(implicit session: JdbcBackend#Session): Unit = {
    getOldAnswersByQuestionId(oldQuestion.id.get, oldQuestion.questionType).foreach { answer =>
      oldQuestion.questionType match {
        case QuestionType.Essay => ()
        case _ =>
          answers += makeAnswerRow(newQuestionId, answer).copy(courseId = oldQuestion.courseId)
      }
    }
  }

  private def migratePlainText(pt: PlainText, newCatId: Option[Long])(implicit session: JdbcBackend#Session): Long = {
    (plainTexts returning plainTexts.map(_.id)).insert(pt.copy(categoryId = newCatId))
  }

  private def addToSlideElements(oldContentId: Long, newContentId: Long, isPlainText: Boolean)(implicit session: JdbcBackend#Session): Unit = {
    val entityType = if (isPlainText) "plaintext" else "question"

    val query = for {s <- slideElements if s.content === oldContentId.toString && s.slideEntityType === "question"} yield s.id

    query.list.foreach(slideElementId =>
      slideEntities.append((slideElementId, entityType, newContentId.toString))
    )
  }

  private def migrateContent(item: OldContent, newCatId: Option[Long])(implicit session: JdbcBackend#Session): Unit = {
    val newId = if (item.isPlainText) {
      migratePlainText(item.plainText.get, newCatId)
    } else {
      migrateQuestion(item.question.get, newCatId)
    }
    addToSlideElements(item.id, newId, item.isPlainText)
  }

  private def migrateCategoryContent(oldCatId: Long, newCatId: Long)(implicit session: JdbcBackend#Session): Unit = {
    getOldContentByCategoryId(oldCatId).foreach {
      migrateContent(_, Some(newCatId))
    }

    getCategoriesByCategoryId(oldCatId: Long).foreach(cat => migrateCategory(cat.copy(categoryId = Some(newCatId))))
  }

  private def migrateCategory(cat: Category)(implicit session: JdbcBackend#Session): Unit = {
    val newId = (questionCategories returning questionCategories.map(_.id)).insert(cat)
    migrateCategoryContent(cat.id.get, newId)
  }

  def migrate(implicit session: scala.slick.jdbc.JdbcBackend#SessionDef): Unit = {

    getOldContentWithoutCategory().foreach { item => migrateContent(item, None) }

    getOldCategoriesWithoutParent.foreach { cat => migrateCategory(cat) }

    for ((slideElementId, entityType, newContent) <- slideEntities) {
      val query = for {s <- slideElements if s.id === slideElementId} yield (s.slideEntityType, s.content)
      query.update(entityType, newContent)
    }
  }
}