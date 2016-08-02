package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest._
import com.arcusys.valamis.lesson.scorm.storage.sequencing.SequencingStorage
import com.arcusys.valamis.lesson.scorm.storage.{ActivityDataStorage, ActivityStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ActivityTableComponent
import com.arcusys.valamis.util.TreeNode

import scala.collection.mutable
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


abstract class ActivityStorageImpl(val db: JdbcBackend#DatabaseDef,
                                   val driver: JdbcProfile)
  extends ActivityStorage
    with ActivityTableComponent
    with SlickProfile {

  import driver.simple._

  def sequencingStorage: SequencingStorage
  def activityDataStorage: ActivityDataStorage

  override def getAllFlat(packageId: Long): Seq[Activity] = {
    db.withSession { implicit session =>
      val activities = activityTQ.filter(_.packageId === packageId).sortBy(a => (a.indexNumber, a.id)).run
      activities.map(convert(_))
    }
  }


  override def get(packageId: Long, id: String): Option[Activity] = {
    db.withSession { implicit session =>
      val activity = activityTQ.filter(a => a.packageId === packageId && a.id === id).firstOption
      activity.map(convert(_))
    }
  }

  /**
    * Forms the activity path as the ordered series of activities from the Current Activity to the common ancestor
    *
    * @param packageId  given package ID
    * @param activityId given activity ID
    * @return activity path
    */
  override def getActivityPath(packageId: Long, activityId: String): Seq[Activity] = {
    //Find the activity that is the common ancestor of the Current Activity and the identified activity
    val activities = getAllFlat(packageId)
    val targetActivity = get(packageId, activityId).getOrElse(throw new Exception("Activity not found in package"))
    val mappedActivities = Map(activities.map(activity => (activity.id, activity)): _*)
    val activityPath = mutable.Buffer[Activity](targetActivity)

    while (activityPath.head.parentId != None) {
      activityPath.prepend(mappedActivities.getOrElse(activityPath.last.parentId.get, throw new Exception("Problem in Activity DB.")))
    }
    activityPath
  }


  override def getParent(packageId: Long, activityId: String): Option[Activity] = {
    val targetActivity = get(packageId, activityId)
    targetActivity match {
      case Some(activity) => activity.parentId match {
        case Some(parentID) => get(packageId, parentID)
        case _ => None
      }
      case _ => None
    }
  }

  override def getAllOrganizations(packageId: Long): Seq[Organization] =
    db.withSession { implicit session =>
      val activities = activityTQ.filter(_.packageId === packageId).sortBy(a => (a.indexNumber, a.id)).run

      activities.map(convert).filter(_.isInstanceOf[Organization]).map(_.asInstanceOf[Organization])
    }

  override def getChildren(packageId: Long, activityId: Option[String]): Seq[Activity] = {
    getAllFlat(packageId).filter(_.parentId == activityId)
  }

  override def create(packageId: Long, entity: Activity): Unit =
    db.withSession { implicit session =>
      val activity = entity match {
        case l: LeafActivity =>
          new ActivityModel(1L,
            Some(entity.id),
            Some(packageId),
            entity.organizationId,
            entity.parentId,
            entity.title,
            Some(l.resourceIdentifier),
            l.resourceParameters,
            Some(entity.hiddenNavigationControls.map(_.toString).mkString("|")),
            entity.visible,
            None,
            None,
            l.masteryScore,
            l.maxTimeAllowed)
        case o: Organization =>

          new ActivityModel(1L,
            Some(entity.id),
            Some(packageId),
            entity.organizationId,
            entity.parentId,
            entity.title,
            None,
            None,
            Some(entity.hiddenNavigationControls.map(_.toString).mkString("|")),
            entity.visible,
            Some(o.objectivesGlobalToSystem),
            Some(o.sharedDataGlobalToSystem),
            None,
            None)
        case _ => {
          new ActivityModel(1L,
            Some(entity.id),
            Some(packageId),
            entity.organizationId,
            entity.parentId,
            entity.title,
            None,
            None,
            Some(entity.hiddenNavigationControls.map(_.toString).mkString("|")),
            entity.visible,
            None,
            None,
            None,
            None)
        }
      }

      activityTQ += activity

      if (entity.isInstanceOf[LeafActivity])
        entity.asInstanceOf[LeafActivity].data.foreach(data => activityDataStorage.create(packageId, entity.id, data))

      sequencingStorage.create(packageId, entity.id, entity.sequencing)
    }


  override def getOrganizationTree(packageId: Long, organizationId: String): TreeNode[Activity] =
    db.withSession { implicit session =>
      val activities = activityTQ.filter(a => a.packageId === packageId && a.organizationId === organizationId)
        .sortBy(a => (a.indexNumber, a.id)).run

      TreeNode.parseNodes(
        activities.map(convert(_)),
        (a: Activity) => a.id,
        (a: Activity) => a.parentId,
        None
      ).head
    }


  override def getAll: Seq[Activity] = db.withSession { implicit session =>
    val activities = activityTQ.sortBy(a => (a.indexNumber, a.id)).run
    activities.map(convert(_))
  }


  private def convert(activity: ActivityModel) = db.withSession { implicit session =>
    val sequencing = sequencingStorage
      .get(activity.packageId.getOrElse(-1), activity.id.getOrElse(""))
      .getOrElse(Sequencing.Default)

    if (activity.parentId.isEmpty) {
      new Organization(
        activity.id.get,
        activity.title,
        objectivesGlobalToSystem = false,
        sharedDataGlobalToSystem = false,
        sequencing = sequencing
      )
    } else if (activity.identifierRef.isEmpty) {
      new ContainerActivity(
        activity.id.get,
        activity.title,
        activity.parentId.orNull,
        activity.organizationId,
        sequencing,
        CompletionThreshold.Default,
        activity.hideLMSUI.getOrElse("").split('|').toSet.filter(!_.isEmpty).map(NavigationControlType.withName(_)),
        activity.visible
      )
    } else {
      // leaf activity
      new LeafActivity(
        activity.id.get,
        activity.title,
        activity.parentId.orNull,
        activity.organizationId,
        activity.identifierRef.get,
        activity.resourceParameters,
        None, //timeLimitAction
        Some("dataFromLMS"),
        activityDataStorage.getForActivity(activity.packageId.get, activity.id.get),
        sequencing,
        CompletionThreshold.Default,
        activity.hideLMSUI.getOrElse("").split('|').toSet.filter(!_.isEmpty).map(NavigationControlType.withName(_)),
        activity.visible,
        None,
        activity.masteryScore,
        activity.maxTimeAllowed
      )
    }
  }

}
