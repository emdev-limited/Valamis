package com.arcusys.valamis.storyTree.service

import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.tincan.model.LessonCategoryGoal
import com.arcusys.valamis.storyTree.model.{StoryNode, StoryPackageItem}

/**
 * Created by mminin on 15.05.15.
 */
trait StoryTreeNodeService {
  def createEmptyNode(treeId: Long, parentId: Option[Long], title: String, description: String, comment: Option[String]): StoryNode

  def createPackageNode(treeId: Long, parentId: Option[Long], lessonId: Long, comment: Option[String]): StoryNode

  def createPackageItem(nodeId: Long, lessonId: Long, comment: Option[String]): StoryPackageItem

  def getByParent(treeId:Long, parentId: Option[Long]): Seq[StoryNode]

  def getItems(nodeId: Long): Seq[StoryPackageItem]

  def deleteNode(nodeId: Long): Unit

  def deletePackageItem(itemId: Long): Unit

  def move(nodeId: Long, newParentId: Option[Long]): StoryNode

  def updatePackage(itemId: Long, newNodeId: Long, comment: Option[String]): StoryPackageItem

  def setInfo(nodeId: Long, title: String, description: String, comment: Option[String]) : StoryNode

  def getAvailableLessons(courseId: Long) : Map[Lesson, Seq[LessonCategoryGoal]]
}