package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.tracking.ActivityStateNode
import com.arcusys.valamis.lesson.scorm.storage.tracking.{ActivityStateNodeStorage, ActivityStateStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateNodeModel
import com.arcusys.valamis.persistence.impl.scorm.schema._

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


abstract class ActivityStateNodeStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ActivityStateNodeStorage
  with ActivityStateTableComponent
  with ActivityStateNodeTableComponent
  with ActivityStateTreeTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile {

  import driver.simple._

  def activityStateStorage: ActivityStateStorage

  override def createAndGetID(treeId: Long, parentId: Option[Long], node: ActivityStateNode): Long = {
    db.withSession { implicit session =>
      val childrenIDs = node.availableChildren.map(_.item.activity.id).mkString("/")
      val activityStateNode = new ActivityStateNodeModel(None, parentId, Some(treeId), Some(childrenIDs))
      val id = (activityStateNodeTQ returning activityStateNodeTQ.map(_.id)) += activityStateNode

      activityStateStorage.createNodeItem(id, node.item)
      id
    }
  }

  override def getTree(treeId: Long): Option[ActivityStateNode] = {
    val rows = db.withSession { implicit session =>
      activityStateNodeTQ.filter(n => n.treeId === treeId).list
    }

    rows.find(_.parentId.isEmpty).map(convert(_, rows))
  }

  private def convert(entity: ActivityStateNodeModel, rows: Seq[ActivityStateNodeModel]): ActivityStateNode = {
    val id = entity.id
    val childrenIds = entity.availableChildrenIds

    val activityState = activityStateStorage.getNodeItem(id.get)
      .getOrElse(throw new Exception("Activity state should exist. Verify your DB integrity."))

    val children = rows.filter(_.parentId == id).sortBy(_.id)
    val node = new ActivityStateNode(activityState, children.map(convert(_, rows)))

    node.availableChildrenCollection.clear()
    if (childrenIds.isDefined && childrenIds.get.nonEmpty)
      childrenIds.get.split('/')
        .foreach(id => node.availableChildrenCollection += node.children.find(_.item.activity.id == id)
          .getOrElse(throw new Exception("Activity ID not found! Check DB integrity.")))
    node
  }
}

