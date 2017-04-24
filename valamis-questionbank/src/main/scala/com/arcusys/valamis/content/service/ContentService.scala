package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.exceptions.NoCategoryException
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage._
import com.arcusys.valamis.persistence.common.DatabaseLayer

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait ContentService {
  def getTree(courseId: Long): ContentTree

  def getTreeFromCategory(categoryId: Long): CategoryTreeNode

  def getContentCount(courseId: Long): Int

  def getContentCountFromCategory(categoryId: Long): Int

}

abstract class ContentServiceImpl extends ContentService {

  def categoryStorage: CategoryStorage
  def plainTextStorage: PlainTextStorage
  def questionStorage: QuestionStorage
  def answerStorage: AnswerStorage
  def dbLayer: DatabaseLayer

  import DatabaseLayer.dbTimeout

  override def getTree(courseId: Long): ContentTree = {
    val categoryTask = dbLayer.execAsync(categoryStorage.getByCourse(courseId).map(_.groupBy(x => x.categoryId)))
    val plainTextTask = dbLayer.execAsync(plainTextStorage.getByCourse(courseId).map(_.groupBy(x => x.categoryId)))
    val questionTask = dbLayer.execAsync(questionStorage.getByCourse(courseId).map(_.groupBy(x => x.categoryId)))
    val answerTask = dbLayer.execAsync(answerStorage.getByCourse(courseId).map(_.groupBy(x => x.questionId.get)))

    val treeTask = for {
      categoriesByCategory <- categoryTask
      plainTextsByCategory <- plainTextTask
      questionsByCategory <- questionTask
      answersByQuestion <- answerTask
    } yield
      new TreeBuilder(
        x => categoriesByCategory.getOrElse(x, Seq()),
        x => plainTextsByCategory.getOrElse(x, Seq()),
        x => questionsByCategory.getOrElse(x, Seq()),
        x => answersByQuestion.getOrElse(x, Seq())
      ).getTree()

    Await.result(treeTask, dbTimeout)
  }


  override def getTreeFromCategory(categoryId: Long): CategoryTreeNode = {
    dbLayer.execSync(categoryStorage.getById(categoryId)).fold(throw new NoCategoryException(categoryId)) { category =>
      new TreeBuilder(
        x => dbLayer.execSync(categoryStorage.getByCategory(x, category.courseId)),
        x => dbLayer.execSync(plainTextStorage.getByCategory(x, category.courseId)),
        x => dbLayer.execSync(questionStorage.getByCategory(x, category.courseId)),
        qId => dbLayer.execSync(answerStorage.getByQuestion(qId))
      ).getCategoryNode(category)
    }
  }

  private def getCount(c: Category): Int = {
    val count = dbLayer.execSync(categoryStorage.getByCategory(c.id, c.courseId))
      .map(getCount)
      .sum
    count +
      dbLayer.execSync(plainTextStorage.getCountByCategory(c.id, c.courseId)) +
      dbLayer.execSync(questionStorage.getCountByCategory(c.id, c.courseId))
  }

  override def getContentCountFromCategory(categoryId: Long): Int = {
    dbLayer.execSync(categoryStorage.getById(categoryId)).fold(0)(getCount)
  }

  //categories should not be counted
  override def getContentCount(courseId: Long): Int = dbLayer.execSync {
    for {
      plTextCount <- plainTextStorage.getCountByCourse(courseId)
      qCount <- questionStorage.getCountByCourse(courseId)
    } yield plTextCount + qCount
  }
}

class TreeBuilder(categoriesByCategory: Option[Long] => Seq[Category],
                  plainTextsByCategory: Option[Long] => Seq[PlainText],
                  questionsByCategory: Option[Long] => Seq[Question],
                  answersByQuestion: Long => Seq[Answer]
                   ) {

  def this(answersByQuestion: Long => Seq[Answer]) {
    this(x => Seq(), x => Seq(), x => Seq(), answersByQuestion)
  }

  def this() {
    this(x => Seq(), x => Seq(), x => Seq(), x => Seq())
  }

  def getTree(): ContentTree = {
    val nodes = categoriesByCategory(None).map(getCategoryNode)
    val plainTexts = plainTextsByCategory(None).map(getPlainTextNode)
    val questions = questionsByCategory(None).map(getQuestionNode)

    new ContentTree(
      nodes.map(_.contentAmount).sum + plainTexts.size + questions.size,
      nodes ++ plainTexts ++ questions
    )
  }

  def getCategoryNode(category: Category): CategoryTreeNode = {
    val nodes = categoriesByCategory(category.id).map(getCategoryNode)
    val plainTexts = plainTextsByCategory(category.id).map(getPlainTextNode)
    val questions = questionsByCategory(category.id).map(getQuestionNode)

    new CategoryTreeNode(
      category,
      nodes.map(_.contentAmount).sum + plainTexts.size + questions.size,
      nodes ++ plainTexts ++ questions
    )
  }

  def getPlainTextNode(plainText: PlainText): PlainTextNode = {
    new PlainTextNode(plainText)
  }

  def getQuestionNode(question: Question): QuestionNode = {
    val answers = question.questionType match {
      case QuestionType.Essay =>
        Seq[Answer]()
      case _ =>
        answersByQuestion(question.id.get)
    }
    new QuestionNode(question, answers)
  }
}