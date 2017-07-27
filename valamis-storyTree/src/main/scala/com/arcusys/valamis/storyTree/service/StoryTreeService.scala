package com.arcusys.valamis.storyTree.service

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.storyTree.model.{Story, StoryTree}

import scala.concurrent.Future

trait StoryTreeService {
  def create(courseId: Long,
             title: String,
             description: String,
             logo: Option[String] = None,
             isDefault: Boolean): Story

  def get(treeId: Long): Story

  def getFullTree(treeId: Long): Future[StoryTree]

  def getBy(courseId: Long,
            title: Option[String] = None,
            published: Option[Boolean] = None,
            sortAsc: Boolean,
            skipTake: Option[SkipTake] = None): Seq[Story]

  def getCountBy(courseId: Long,
                 title: Option[String] = None,
                 published: Option[Boolean] = None): Int

  def delete(treeId: Long): Unit

  def update(treeId: Long, title: String, description: String, isDefault: Boolean): Story

  def getLogo(treeId: Long): Option[Array[Byte]]

  def setLogo(treeId: Long, name: String, content: Array[Byte])

  def deleteLogo(treeId: Long)

  def publish(treeId: Long): Unit

  def unpublish(treeId: Long): Unit
}