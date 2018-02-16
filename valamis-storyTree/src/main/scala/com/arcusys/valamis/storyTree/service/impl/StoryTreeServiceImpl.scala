package com.arcusys.valamis.storyTree.service.impl

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.service.LessonCategoryGoalService
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.storyTree.model.Story
import com.arcusys.valamis.storyTree.service.{StoryTreeBuilder, StoryTreeService}
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent
import com.arcusys.valamis.storyTree.storage.query.StoryQueries

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver._
import slick.jdbc._

abstract class StoryTreeServiceImpl(val db: JdbcBackend#DatabaseDef,
                                    val driver: JdbcProfile)
  extends StoryTreeService
    with StoryTreeBuilder
    with StoryTreeTableComponent
    with StoryQueries
    with DatabaseLayer {

  def fileService: FileService
  def lessonService: LessonService
  def lessonCategoryGoalService: LessonCategoryGoalService

  import DatabaseLayer._
  import driver.api._

  private def logoPathPrefix(treeId: Long) = s"files/StoryTree/$treeId/"

  override def create(courseId: Long,
                      title: String,
                      description: String,
                      logo: Option[String],
                      isDefault: Boolean): Story = execSyncInTransaction {
    val insertAction = (trees returning trees.map(_.id)) +=
      Story(id = None, courseId, title, description, logo = None, published = false, isDefault)

    for {
      newTreeId <- insertAction
      newTree <- trees.filterById(newTreeId).result.head
    } yield newTree
  }

  override def delete(treeId: Long): Unit = execSyncInTransaction {
    getTreeQuery(treeId) ifSomeThen { tree =>
      for (logo <- tree.logo) fileService.deleteFileStoryTree(logo)

      for {
        _ <- packages.filter(_.treeId === treeId).delete
        _ <- nodes.filter(_.treeId === treeId).delete
        _ <- trees.filterById(treeId).delete
      } yield {}
    }
  }

  override def get(treeId: Long): Story = getTree(treeId)

  override def getBy(courseId: Long,
                     title: Option[String],
                     published: Option[Boolean],
                     sortAsc: Boolean,
                     skipTake: Option[SkipTake]): Seq[Story] = execSync {

    trees
      .filter(_.courseId === courseId)
      .filterByPublished(published)
      .filterByTitle(title)
      .sortByTitle(sortAsc)
      .slice(skipTake)
      .result
  }

  override def getCountBy(courseId: Long,
                          title: Option[String] = None,
                          published: Option[Boolean] = None): Int = execSync {
    trees
      .filter(_.courseId === courseId)
      .filterByPublished(published)
      .filterByTitle(title)
      .length.result
  }

  private def getTree(treeId: Long): Story = {
    execSync(getTreeQuery(treeId))
      .getOrElse(throw new EntityNotFoundException(s"KnowledgeTree with id $treeId not found"))
  }

  private def getTreeQuery(treeId: Long): DBIO[Option[Story]] =
    trees.filterById(treeId).result.headOption

  override def update(treeId: Long, newTitle: String, newDescription: String, isDefault: Boolean): Story = {
    execSyncInTransaction {
      trees.filterById(treeId)
        .map(x => (x.title, x.description, x.isDefault))
        .update((newTitle, newDescription, isDefault))
        .andThen(getTreeQuery(treeId))
    }.getOrElse(throw new EntityNotFoundException(s"Tree with id $treeId not found"))
  }

  override def getLogo(treeId: Long): Option[Array[Byte]] = {
    val tree = getTree(treeId)
    val logoPath = logoPathPrefix(treeId)
    tree.logo
      .map(logoPath + _ )
      .flatMap(fileService.getFileContentOption)
  }

  override def setLogo(treeId: Long, name: String, content: Array[Byte]) = {
    fileService.setFileContent(
      s"StoryTree/$treeId/",
      name,
      content,
      deleteFolder = true
    )

    execSync(trees.filterById(treeId).map(_.logo).update(Some(name)))
  }

  override def deleteLogo(treeId: Long): Unit = {
    fileService.deleteByPrefix(logoPathPrefix(treeId))
    execSync(trees.filterById(treeId).map(_.logo).update(None))
  }

  override def publish(treeId: Long): Unit =
    execSync(trees.map(_.published).update(true))

  override def unpublish(treeId: Long): Unit =
    execSync(trees.filterById(treeId).map(x => (x.published, x.isDefault)).update((false, false)))
}