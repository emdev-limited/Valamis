package com.arcusys.valamis.storyTree.storage

import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.storyTree.model._
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait StoryTreeTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>

  import driver.simple._

  class StoryTreeTable(tag : Tag) extends LongKeyTable[Story](tag, "STORY_TREE") {

    def courseId = column[Long]("COURSE_ID", O.NotNull)
    def title = column[String]("TITLE", O.NotNull)
    def description = column[String]("DESCRIPTION", O.NotNull, O.Length(2000, varying = true))
    def logo = column[Option[String]]("LOGO")
    def published = column[Boolean]("PUBLISHED", O.NotNull)
    def isDefault = column[Boolean]("IS_DEFAULT", O.NotNull)

    def * = (id.?, courseId, title, description, logo, published, isDefault) <> (Story.tupled, Story.unapply)

    def update = (courseId, title, description, logo, published, isDefault) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  class StoryTreeNodeTable(tag : Tag) extends LongKeyTable[StoryNode](tag, "STORY_TREE_NODE") {
    def parentId = column[Option[Long]]("PARENT_ID")
    def treeId = column[Long]("TREE_ID", O.NotNull)
    def title = column[String]("TITLE", O.NotNull)
    def description = column[String]("DESCRIPTION", O.NotNull, O.Length(2000, varying = true))
    def comment = column[Option[String]]("COMMENT")

    def * = (id.?, parentId, treeId, title, description, comment) <> (StoryNode.tupled, StoryNode.unapply)

    def update = (parentId, treeId, title, description, comment) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def treeFK = foreignKey("NODE_TREE_FK", treeId, TableQuery[StoryTreeTable])(_.id, onDelete = ForeignKeyAction.Cascade)
    def parentFK = foreignKey("NODE_PARENT_FK", parentId, TableQuery[StoryTreeNodeTable])(_.id, onDelete = ForeignKeyAction.NoAction)
  }

  class StoryPackagesItemTable(tag : Tag) extends LongKeyTable[StoryPackageItem](tag, "STORY_TREE_PACKAGE") {
    def nodeId = column[Long]("NODE_ID", O.NotNull)
    def treeId = column[Long]("TREE_ID", O.NotNull)
    def packageId = column[Long]("PACKAGE_ID", O.NotNull)
    def comment = column[Option[String]]("COMMENT")

    def * = (id.?, nodeId, treeId, packageId, comment) <> (StoryPackageItem.tupled, StoryPackageItem.unapply)

    def update = (nodeId, treeId, packageId, comment) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def treeFK = foreignKey("PACKAGE_TREE_FK", treeId, TableQuery[StoryTreeTable])(_.id, onDelete = ForeignKeyAction.NoAction)
    def parentFK = foreignKey("PACKAGE_NODE_FK", nodeId, TableQuery[StoryTreeNodeTable])(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  val trees = TableQuery[StoryTreeTable]
  val nodes = TableQuery[StoryTreeNodeTable]
  val packages = TableQuery[StoryPackagesItemTable]
}
