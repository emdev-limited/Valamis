package com.arcusys.valamis.storyTree.storage.query

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.persistence.common.{DbNameUtils, SlickProfile}
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent

trait StoryQueries {
  self: SlickProfile with StoryTreeTableComponent =>

  import driver.api._

  private type StoryTreeQuery = Query[StoryTreeTable, StoryTreeTable#TableElementType, Seq]
  private type StoryNodeQuery = Query[StoryTreeNodeTable, StoryTreeNodeTable#TableElementType, Seq]
  private type StoryPackageQuery = Query[StoryPackagesItemTable, StoryPackagesItemTable#TableElementType, Seq]

  implicit class StoryTreeExt(val query: StoryTreeQuery) {
    def filterById(id: Long) =
      query.filter(_.id === id)

    def filterByTitle(title: Option[String]): StoryTreeQuery = title match {
      case Some(value) => query.filter(_.title.toLowerCase like DbNameUtils.likePattern(value.toLowerCase))
      case _ => query
    }

    def filterByPublished(published: Option[Boolean]) = published match {
      case Some(value) => query.filter(_.published === value)
      case _ => query
    }

    def slice(skipTake: Option[SkipTake]): StoryTreeQuery = {
      skipTake match {
        case Some(SkipTake(skip, take)) => query.drop(skip).take(take)
        case None => query
      }
    }

    def sortByTitle(ascending: Boolean): StoryTreeQuery = {
      if (ascending) {
        query.sortBy(x => x.title)
      }
      else {
        query.sortBy(_.title.desc)
      }
    }
  }

  implicit class StoryNodeExt(val query: StoryNodeQuery) {
    def filterById(id: Long) =
      query.filter(_.id === id)

    def filterByTreeId(treeId: Long) =
      query.filter(_.treeId === treeId)
  }

  implicit class StoryPackageExt(val query: StoryPackageQuery) {
    def filterById(id: Long) =
      query.filter(_.id === id)

    def filterByNodeId(nodeId: Long) =
      query.filter(_.nodeId === nodeId)

    def filterByTreeId(treeId: Long) =
      query.filter(_.treeId === treeId)
  }
}