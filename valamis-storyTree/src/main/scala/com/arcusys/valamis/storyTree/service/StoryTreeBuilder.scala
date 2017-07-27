package com.arcusys.valamis.storyTree.service

import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.service.LessonCategoryGoalService
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.storyTree.model._
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent
import com.arcusys.valamis.storyTree.storage.query.StoryQueries

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by mminin on 16.06.15.
 */
trait StoryTreeBuilder
  extends StoryTreeService
    with StoryTreeTableComponent
    with SlickProfile
    with StoryQueries
    with DatabaseLayer {

  def lessonService: LessonService
  def lessonCategoryGoalService: LessonCategoryGoalService

  import driver.api._

  override def getFullTree(treeId: Long): Future[StoryTree] = {
    val treeF = execAsync(trees.filterById(treeId).result.head)
    val treeNodeF = execAsync(nodes.filterByTreeId(treeId).result)
    val packageF = execAsync(packages.filterByTreeId(treeId).result)

    for {
      tree <- treeF
      treeNodes <- treeNodeF
      treeNodePackages <- packageF
    } yield {
      val rootNodes = treeNodes
        .filter(_.parentId.isEmpty)
        .map(getTreeNode(_, treeNodes, treeNodePackages))

      StoryTree(tree.id.get, tree.title, tree.description, rootNodes)
    }
  }

  private def getTreeNode(node: StoryNode,
                          nodes: Seq[StoryNode],
                          packages: Seq[StoryPackageItem]): StoryTreeNode = {

    val subNodes = nodes.filter(_.parentId == node.id).map(getTreeNode(_, nodes, packages))
    val subPackages = packages.filter(_.nodeId == node.id.get).map(getTreePackage)

    StoryTreeNode(node.id.get, node.title, node.description, subNodes, node.comment, subPackages)
  }

  private def getTreePackage(lessonItem: StoryPackageItem): StoryTreePackageItem = {
    lessonService.getLesson(lessonItem.packageId) match {
      case Some(lesson) =>
        val categories = lessonCategoryGoalService.get(lesson.id).map(_.name)
        StoryTreePackageItem(
          id = lessonItem.id.get,
          packageId = lesson.id,
          title = Some(lesson.title),
          description = Option(lesson.description),
          topics = categories,
          relationComment = lessonItem.comment
        )
      case None =>
        StoryTreePackageItem(
          id = lessonItem.id.get,
          packageId = lessonItem.packageId,
          title = None,
          description = None,
          topics = Seq(),
          relationComment = lessonItem.comment
        )
    }
  }
}
