package com.arcusys.learn.liferay.update.version260.storyTree

import com.arcusys.valamis.core.DbNameUtils._

import scala.slick.driver.JdbcProfile

trait StoryTreeTableComponent {

  protected val driver: JdbcProfile
  import driver.simple._

  case class Story(
    id: Option[Long],
    courseId: Long,
    title: String,
    description: String,
    logo: Option[String],
    published: Boolean)

  class StoryTreeTable(tag : Tag) extends Table[Story](tag, tblName("STORY_TREE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def courseId = column[Long]("COURSE_ID", O.NotNull)
    def title = column[String]("TITLE", O.NotNull)
    def description = column[String]("DESCRIPTION", O.NotNull)
    def logo = column[Option[String]]("LOGO")
    def published = column[Boolean]("PUBLISHED", O.NotNull)

    def * = (id.?, courseId, title, description, logo, published) <> (Story.tupled, Story.unapply)
  }

  case class StoryNode(
    id: Option[Long],
    parentId: Option[Long],
    treeId: Long,
    title: String,
    description: String)

  class StoryTreeNodeTable(tag : Tag) extends Table[StoryNode](tag, tblName("STORY_TREE_NODE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def parentId = column[Option[Long]]("PARENT_ID")
    def treeId = column[Long]("TREE_ID", O.NotNull)
    def title = column[String]("TITLE", O.NotNull)
    def description = column[String]("DESCRIPTION", O.NotNull)

    def * = (id.?, parentId, treeId, title, description) <> (StoryNode.tupled, StoryNode.unapply)

    def treeFK = foreignKey("NODE_TREE_FK", treeId, TableQuery[StoryTreeTable])(_.id, onDelete = ForeignKeyAction.Cascade)
    def parentFK = foreignKey("NODE_PARENT_FK", parentId, TableQuery[StoryTreeNodeTable])(_.id, onDelete = ForeignKeyAction.NoAction)
  }

  type StoryPackageItem = (Option[Long], Long, Long, Long)
  class StoryPackagesItemTable(tag : Tag) extends Table[StoryPackageItem](tag, tblName("STORY_TREE_PACKAGE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def nodeId = column[Long]("NODE_ID", O.NotNull)
    def treeId = column[Long]("TREE_ID", O.NotNull)
    def packageId = column[Long]("PACKAGE_ID", O.NotNull)

    def * = (id.?, nodeId, treeId, packageId)

    def treeFK = foreignKey("PACKAGE_TREE_FK", treeId, TableQuery[StoryTreeTable])(_.id, onDelete = ForeignKeyAction.NoAction)
    def parentFK = foreignKey("PACKAGE_NODE_FK", nodeId, TableQuery[StoryTreeNodeTable])(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  val trees = TableQuery[StoryTreeTable]
  val nodes = TableQuery[StoryTreeNodeTable]
  val packages = TableQuery[StoryPackagesItemTable]
}
