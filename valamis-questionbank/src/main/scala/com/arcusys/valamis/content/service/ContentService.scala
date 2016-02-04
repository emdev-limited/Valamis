package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.exceptions.{NoCategoryException, NoQuestionException}
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait ContentService {
  def getTree(courseId: Long): ContentTree

  def getTreeFromCategory(categoryId: Long): CategoryTreeNode

  def getContentCount(courseId: Long): Int

  def getContentCountFromCategory(categoryId: Long): Int

}

class ContentServiceImpl(implicit val bindingModule: BindingModule)
  extends ContentService
  with Injectable {

  lazy val categoryStorage = inject[CategoryStorage]

  lazy val plainTextStorage = inject[PlainTextStorage]

  lazy val questionStorage = inject[QuestionStorage]

  lazy val answerStorage = inject[AnswerStorage]

  override def getTree(courseId: Long): ContentTree = {
    val categoriesByCategory = categoryStorage.getByCourse(courseId).groupBy(x => x.categoryId)
    val plainTextsByCategory = plainTextStorage.getByCourse(courseId).groupBy(x => x.categoryId)
    val questionsByCategory = questionStorage.getByCourse(courseId).groupBy(x => x.categoryId)
    val answersByQuestion = answerStorage.getByCourse(courseId).groupBy(x => x.questionId.get)

    new TreeBuilder(
      x => categoriesByCategory.getOrElse(x, Seq()),
      x => plainTextsByCategory.getOrElse(x, Seq()),
      x => questionsByCategory.getOrElse(x, Seq()),
      x => answersByQuestion.getOrElse(x, Seq())
    ).getTree()
  }


  override def getTreeFromCategory(categoryId: Long): CategoryTreeNode = {
    categoryStorage.getById(categoryId).fold(throw new NoCategoryException(categoryId))(category =>
      new TreeBuilder(
        x => categoryStorage.getByCategory(x, category.courseId),
        x => plainTextStorage.getByCategory(x, category.courseId),
        x => questionStorage.getByCategory(x, category.courseId),
        answerStorage.getByQuestion
      ).getCategoryNode(category)
    )
  }

  private def getCount(c: Category): Int = {
    val count = categoryStorage.getByCategory(c.id, c.courseId)
      .map(getCount)
      .sum
    count +
      plainTextStorage.getCountByCategory(c.id, c.courseId) +
      questionStorage.getCountByCategory(c.id, c.courseId)
  }

  override def getContentCountFromCategory(categoryId: Long): Int = {
    categoryStorage.getById(categoryId).fold(0)(getCount)
  }

  override def getContentCount(courseId: Long): Int = {
    //categoryStorage.getCountByCourse(courseId) +//categories should not be counted
    plainTextStorage.getCountByCourse(courseId) +
      questionStorage.getCountByCourse(courseId)
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