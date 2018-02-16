package com.arcusys.valamis.storyTree.service.impl

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.model.LessonCategoryGoal
import com.arcusys.valamis.lesson.tincan.service.LessonCategoryGoalService
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.storyTree.model.{StoryNode, StoryPackageItem}
import com.arcusys.valamis.storyTree.service.StoryTreeNodeService
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent
import com.arcusys.valamis.storyTree.storage.query.StoryQueries

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver._
import slick.jdbc._

abstract class StoryTreeNodeServiceImpl(val db: JdbcBackend#DatabaseDef,
                                        val driver: JdbcProfile)
  extends StoryTreeNodeService
    with StoryTreeTableComponent
    with SlickProfile
    with StoryQueries
    with DatabaseLayer {

  def lessonService: LessonService
  def lessonCategoryGoalService: LessonCategoryGoalService

  import DatabaseLayer._
  import driver.api._

  override def createEmptyNode(treeId: Long,
                               parentId: Option[Long],
                               title: String,
                               description: String,
                               comment: Option[String]): StoryNode = {
    val insertAction = (nodes returning nodes.map(_.id)) +=
      StoryNode(None, parentId, treeId, title, description, comment)

    val action = for {
      newNodeId <- insertAction
      newNode <- nodes.filterById(newNodeId).result.head
    } yield newNode

    execSyncInTransaction(action)
  }


  override def createPackageNode(treeId: Long,
                                 parentId: Option[Long],
                                 lessonId: Long,
                                 comment: Option[String]): StoryNode = {
    val lesson = lessonService.getLessonRequired(lessonId)
    val title = lesson.title
    val description = lesson.description

    val insertAction = (nodes returning nodes.map(_.id)) +=
      StoryNode(None, parentId, treeId, title, description, comment)

    val action = for {
      newNodeId <- insertAction
      newPackageId <- {
        packages.returning(packages.map(_.id)) +=
          StoryPackageItem(None, newNodeId, treeId, lessonId, comment)
      }
      newNode <- nodes.filterById(newNodeId).result.head
    } yield newNode

    execSyncInTransaction(action)
  }

  override def createPackageItem(nodeId: Long,
                                 lessonId: Long,
                                 comment: Option[String]): StoryPackageItem = {

    val action = nodes.filterById(nodeId).result.headOption ifSomeThen { node =>
      val insertAction = (packages returning packages.map(_.id)) +=
        StoryPackageItem(None, node.id.get, node.treeId, lessonId, comment)

      insertAction flatMap { newItemId =>
        packages.filter(_.id === newItemId).result.head
      }
    } map (_.getOrElse(throw new EntityNotFoundException(s"StoryTreeNode with id $nodeId not found")))

    execSyncInTransaction(action)
  }

  override def getByParent(treeId: Long, parentId: Option[Long]): Seq[StoryNode] = {
    val filteredQ = nodes.filterByTreeId(treeId)
    val query = parentId match {
      case Some(id) => filteredQ.filter(_.parentId === id)
      case _ => filteredQ.filter(_.parentId.isEmpty)
    }
    execSync(query.result)
  }

  override def getItems(nodeId: Long): Seq[StoryPackageItem] =
    execSync(packages.filterByNodeId(nodeId).result)

  override def deleteNode(nodeId: Long): Unit =
    execSync(nodes.filterById(nodeId).delete)

  override def deletePackageItem(itemId: Long): Unit =
    execSync(packages.filterById(itemId).delete)

  override def move(nodeId: Long, newParentId: Option[Long]): StoryNode = {
    val q = nodes.filterById(nodeId)

    execSyncInTransaction {
      val updateQ = newParentId match {
        case None => {
          q
        }
        case Some(parentId) => {
          q.join(nodes.filterById(parentId))
            .on((n, p) => n.treeId === p.treeId)
            .map(_._1)
        }
      }
      updateQ.result.headOption ifSomeThen { x =>
        q.map(_.parentId)
          .update(newParentId)
          .andThen(nodes.filterById(nodeId).result.head)
      } map (_.getOrElse(throw new AssertionError(s"Can not move tree node ($nodeId) to node ($newParentId) from different tree")))
    }
  }

  def updatePackage(itemId: Long, newNodeId: Long, comment: Option[String]): StoryPackageItem = {
    val packageQ = packages.filterById(itemId)
    val joinQ = nodes.filterById(newNodeId)
      .join(packageQ)
      .on((n, p) => n.treeId === p.treeId)

    execSyncInTransaction {
      joinQ.result.headOption ifSomeThen { x =>
        packageQ
          .map(x => (x.nodeId, x.comment))
          .update((newNodeId, comment))
          .andThen(packageQ.result.head)
      } map (_.getOrElse(throw new AssertionError(s"Can not move package ($itemId) to node ($newNodeId) from different tree")))
    }
  }

  override def setInfo(nodeId: Long,
                       title: String,
                       description: String,
                       comment: Option[String]): StoryNode = execSyncInTransaction {
    nodes
      .filterById(nodeId)
      .map(x => (x.title, x.description, x.comment))
      .update((title, description, comment))
      .andThen(nodes.filterById(nodeId).result.head)
  }

  override def getAvailableLessons(courseId: Long) : Map[Lesson, Seq[LessonCategoryGoal]] = {
    lessonService.getAll(courseId)
      .map(p => (p, lessonCategoryGoalService.get(p.id)))
      .toMap
  }
}