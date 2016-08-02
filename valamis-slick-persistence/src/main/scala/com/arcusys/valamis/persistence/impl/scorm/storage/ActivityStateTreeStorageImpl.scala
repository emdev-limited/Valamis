package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.tracking.{ActivityStateNode, ActivityStateTree}
import com.arcusys.valamis.lesson.scorm.storage.tracking.{ActivityStateNodeStorage, ActivityStateStorage, ActivityStateTreeStorage, GlobalObjectiveStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateTreeModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ActivityStateTreeTableComponent, AttemptTableComponent, ScormUserComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


abstract class ActivityStateTreeStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ActivityStateTreeStorage
  with ActivityStateTreeTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile {

  import driver.simple._

  def activityStateStorage: ActivityStateStorage

  def activityStateNodeStorage: ActivityStateNodeStorage

  def globalObjectiveStorage: GlobalObjectiveStorage = new GlobalObjectiveStorageImpl(db, driver)


  override def create(attemptId: Long, tree: ActivityStateTree): Unit =
    db.withSession { implicit session =>
      val activityStateTree = new ActivityStateTreeModel(None,
        tree.currentActivity.map(_.item.activity.id),
        tree.suspendedActivity.map(_.item.activity.id),
        Option(attemptId))

      val treeId = (activityStateTreeTQ returning activityStateTreeTQ.map(_.id)) += activityStateTree

      val organizationId = activityStateNodeStorage.createAndGetID(treeId, None, tree)

      def createStates(parentId: Option[Long], nodes: Seq[ActivityStateNode]) {
        nodes.foreach(node => {
          val id = activityStateNodeStorage.createAndGetID(treeId, parentId.map(_.toInt), node)
          createStates(Some(id), node.children)
        })
      }


      tree.globalObjectiveData.foreach(objective => globalObjectiveStorage.create(treeId, objective._1, objective._2))
      createStates(Some(organizationId), tree.children)
    }


  override def modify(attemptId: Long, tree: ActivityStateTree): Unit = {
    val activityStateTree = new ActivityStateTreeModel (None,
      tree.currentActivity.map (_.item.activity.id),
      tree.suspendedActivity.map (_.item.activity.id),
      Option (attemptId)
    )

    val treeId = db.withSession { implicit session =>
      activityStateTreeTQ
        .filter(_.attemptId === attemptId)
        .map(_.update).update(activityStateTree)

      activityStateTreeTQ
        .filter(_.attemptId === attemptId)
        .map(_.id)
        .firstOption
        .getOrElse(throw new UnsupportedOperationException("State tree should be defined for attempt " + attemptId))
    }

    def updateStates(nodes: Seq[ActivityStateNode]) {
      nodes.foreach(node => {
        activityStateStorage.modify(treeId, node.item)
        updateStates(node.children)
      })
    }

    activityStateStorage.modify(treeId, tree.item)
    updateStates(tree.children)

    tree.globalObjectiveData.foreach { objective =>
      globalObjectiveStorage.modify(attemptId.toInt, objective._1, objective._2)
    }
  }

  override def get(attemptId: Long): Option[ActivityStateTree] =
    db.withSession { implicit session =>
      val activityStateTrees = activityStateTreeTQ.filter(_.attemptId === attemptId).sortBy(_.id).firstOption
      activityStateTrees.map(convert(_))
    }

  private def convert(entity: ActivityStateTreeModel): ActivityStateTree = {
    val treeId = entity.id.get
    val tree = activityStateNodeStorage.getTree(treeId)
      .getOrElse(throw new Exception("No organization found!"))

    new ActivityStateTree(
      tree.item,
      tree.children,
      entity.currentActivityId,
      entity.suspendedActivityId,
      globalObjectiveStorage.getAllObjectives(treeId)
    )
  }
}
