package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.schema.ContentTableComponent
import com.arcusys.valamis.core.OptionFilterSupport

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 * Created by pkornilov on 23.10.15.
 *
 */

class QuestionStorageImpl(val db: JdbcBackend#DatabaseDef,
                          val driver: JdbcProfile)
  extends QuestionStorage with ContentTableComponent with OptionFilterSupport {

  import driver.simple._

  override def getById(id: Long): Option[Question] = db.withSession { implicit s =>
    questions.filter(_.id === id).firstOption.map(makeCustomQuestion)
  }

  override def create(question: Question): Question = db.withSession { implicit s =>
    val id = (questions returning questions.map(_.id)).insert(makeBaseQuestion(question))
    makeCustomQuestion(questions.filter(_.id === id).first)
  }

  override def createWithCategory(question: Question, categoryNewId: Option[Long]): Question = db.withSession { implicit s =>
    val id = (questions returning questions.map(_.id)).insert(makeBaseQuestion(question).copy(categoryId = categoryNewId))
    makeCustomQuestion(questions.filter(_.id === id).first)
  }

  override def update(question: Question): Unit = db.withSession { implicit s =>
    questions.filter(_.id === question.id).map(q => (q.categoryId,
                                                     q.courseId,
                                                     q.title,
                                                     q.text,
                                                     q.explanationText, 
                                                     q.rightAnswerText, 
                                                     q.wrongAnswerText, 
                                                     q.forceCorrectCount, 
                                                     q.isCaseSensitive, 
                                                     q.questionType)).update(getUnitForUpdate(question))
  }

  override def delete(id: Long): Unit = db.withSession { implicit s =>
    questions.filter(_.parentQuestionId === id).delete
    questions.filter(_.id === id).delete
  }

  override def getByCourse(courseId: Long): Seq[Question] = db.withSession { implicit s =>
    questions.filter(_.courseId === courseId).list.map(makeCustomQuestion)
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Question] = db.withSession { implicit s =>
    questions.filter(q => optionFilter(q.categoryId, categoryId) && q.courseId === courseId).list.map(makeCustomQuestion)
  }

  override def getCountByCourse(courseId: Long): Int = db.withSession { implicit s =>
    questions.filter(_.courseId === courseId).length.run
  }

  override def getCountByCategory(categoryId: Option[Long], courseId: Long): Int = db.withSession { implicit s =>
    questions.filter(q => optionFilter(q.categoryId, categoryId) && q.courseId === courseId).length.run
  }

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long) = db.withSession { implicit s =>
    val query = for {q <- questions if q.id === id} yield (q.categoryId, q.courseId)
    query.update(newCategoryId, courseId).run
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean) = db.withSession { implicit s =>
    if (moveToRoot) {
      val query = for {q <- questions if q.id === id} yield (q.courseId, q.categoryId)
      query.update(courseId, None).run
    } else {
      val query = for {q <- questions if q.id === id} yield q.courseId
      query.update(courseId).run
    }
  }

  private def makeCustomQuestion(q: BaseQuestion): Question = q.questionType match {
    case QuestionType.Choice => ChoiceQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.forceCorrectCount, q.courseId)

    case QuestionType.Categorization => CategorizationQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.courseId)

    case QuestionType.Essay => EssayQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.courseId)

    case QuestionType.Matching => MatchingQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.courseId)

    case QuestionType.Numeric => NumericQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.courseId)

    case QuestionType.Positioning => PositioningQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.forceCorrectCount, q.courseId)

    case QuestionType.Text => TextQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.isCaseSensitive, q.courseId)

  }

  private def getUnitForUpdate (question : Question) = {
    question value match {
      case q: ChoiceQuestion => ((
         question.categoryId,    
         question.courseId,
         question.title,
         question.text,
         question.explanationText, 
         q.rightAnswerText, 
         q.wrongAnswerText, 
         q.forceCorrectCount, 
         false, 
         question.questionType
      ))
      case q: TextQuestion => ((
         question.categoryId,    
         question.courseId,
         question.title,
         question.text,
         question.explanationText, 
         q.rightAnswerText, 
         q.wrongAnswerText, 
         false, 
         q.isCaseSensitive, 
         question.questionType
      ))
      case q: NumericQuestion => ((
          question.categoryId,
          question.courseId,
          question.title,
          question.text,
          question.explanationText,
          q.rightAnswerText,
          q.wrongAnswerText,
          false,
          false,
          question.questionType
      ))
      case q: PositioningQuestion => ((
          question.categoryId,
          question.courseId,
          question.title,
          question.text,
          question.explanationText,
          q.rightAnswerText,
          q.wrongAnswerText,
          q.forceCorrectCount,
          false,
          question.questionType
      ))
      case q : MatchingQuestion => ((
          question.categoryId,
          question.courseId,
          question.title,
          question.text,
          question.explanationText,
          q.rightAnswerText,
          q.wrongAnswerText,
          false,
          false,
          question.questionType    
      ))
      case q: EssayQuestion => ((
          question.categoryId,
          question.courseId,
          question.title,
          question.text,
          question.explanationText,
          "",
          "",
          false,
          false,
          question.questionType    
      ))
      case q: CategorizationQuestion => ((
          question.categoryId,
          question.courseId,
          question.title,
          question.text,
          question.explanationText,
          q.rightAnswerText,
          q.wrongAnswerText,
          false,
          false,
          question.questionType    
      ))
    }
  }
  

  private def makeBaseQuestion(q: Question): BaseQuestion = {
    q match {
      case qq: ChoiceQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        qq.forceCorrectCount,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: TextQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        qq.isCaseSensitive,
        q.questionType
      )
      case qq: NumericQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: PositioningQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        qq.forceCorrectCount,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: MatchingQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: EssayQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        "",
        "",
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: CategorizationQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      /* case qq: ClozeQuestion => BaseQuestion(q.id,
         q.categoryId,
         q.courseId,
         q.title,
         q.text,
         q.explanationText,
         "",
         "",
         forceCorrectCount = false,
         isCaseSensitive = false,
         q.questionType)*/
    }
  }


}
