package com.arcusys.valamis.liferay

import java.util.Date

import com.arcusys.learn.liferay.LiferayClasses.{LSocialActivity, LSocialActivityFeedEntry}
import com.arcusys.learn.liferay.model.Activity
import com.arcusys.learn.liferay.services.{ActivityConverter, ServiceContextHelper, SocialActivityInterpreterLocalServiceHelper, SocialActivityLocalServiceHelper}
import com.liferay.portal.kernel.util.StringPool
import org.joda.time.DateTime

import scala.util.Random

/**
  * Created by mminin on 21.02.16.
  */

class SocialActivityHelper[T: Manifest](className: String) extends ActivityConverter {

  // Enumeration support
  // we can get correct className only from instance ex: Colors.getClass.getName
  def this(entity: T) = {
    this(entity.getClass.getName)
  }
  def this() = {
    this(manifest[T].runtimeClass.getName)
  }

  private lazy val random = new Random

  def deleteActivities(classPK: Long) : Unit = {
    SocialActivityLocalServiceHelper.deleteActivities(className, classPK)
  }

  //Creates activity with activitySet, because activity portlet of social office are retrieved for sets.
  def addWithSet(companyId: Long,
                 userId: Long,
                 courseId: Option[Long] = None,
                 receiverUserId: Option[Long] = None,
                 `type`: Option[Int] = None,
                 classPK: Option[Long] = None,
                 extraData: Option[String] = None,
                 createDate: DateTime): Activity = {
    val socialActivity =
      create(companyId, userId, className, courseId, receiverUserId, `type`, classPK, extraData, createDate)

    SocialActivityLocalServiceHelper.addActivity(socialActivity, null)
    toModel(socialActivity)
  }

  private def create(companyId: Long,
                     userId: Long,
                     className: String,
                     courseId: Option[Long],
                     receiverUserId: Option[Long],
                     `type`: Option[Int],
                     classPK: Option[Long],
                     extraData: Option[String],
                     createDate: DateTime) = {
    val socialActivity = SocialActivityLocalServiceHelper.createSocialActivity(0)

    socialActivity.setCompanyId(companyId)
    socialActivity.setUserId(userId)
    socialActivity.setClassName(className)
    courseId.foreach(socialActivity.setGroupId)
    receiverUserId.foreach(socialActivity.setReceiverUserId)
    `type`.foreach(socialActivity.setType)

    socialActivity.setClassPK(classPK.getOrElse(random.nextLong()))
    // Comments in activity portlet of social office are done toward classPK

    socialActivity.setCreateDate(createDate.getMillis)

    extraData.foreach(socialActivity.setExtraData)

    socialActivity
  }

  private def getLiferayFeedEntry(activity: LSocialActivity): Option[LSocialActivityFeedEntry] = {
    lazy val ctx = ServiceContextHelper.getServiceContext

    Some(activity)
      .filter(_.getClassName.contains("com.liferay"))
      .filter(a => Option(ctx.getThemeDisplay).isDefined)
      .map(a => SocialActivityInterpreterLocalServiceHelper.interpret(StringPool.BLANK, activity, ctx))
  }
}