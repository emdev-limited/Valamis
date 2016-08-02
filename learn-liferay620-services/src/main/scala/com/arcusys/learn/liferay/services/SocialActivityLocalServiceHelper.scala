package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.model.Activity
import com.liferay.portal.kernel.dao.orm.{QueryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.kernel.util.StringPool
import com.liferay.portal.service.{ServiceContext, ServiceContextThreadLocal}
import com.liferay.portlet.social.model.{SocialActivity, SocialActivityFeedEntry}
import com.liferay.portlet.social.service.{SocialActivityInterpreterLocalServiceUtil, SocialActivityLocalServiceUtil}
import org.joda.time.DateTime

import scala.collection.JavaConversions._

object SocialActivityLocalServiceHelper extends ActivityConverter {

  def interpret(selector: String, activity: LSocialActivity, ctx: ServiceContext): SocialActivityFeedEntry =
    SocialActivityInterpreterLocalServiceUtil.interpret(selector, activity, ctx)

  def updateSocialActivity(activity: SocialActivity): SocialActivity = SocialActivityLocalServiceUtil.updateSocialActivity(activity)

  def toOption(liferayOptionalValue: Long) = {
    if (liferayOptionalValue == 0) None
    else Some(liferayOptionalValue)
  }

  def toOption(liferayOptionalValue: String) = {
    if (liferayOptionalValue == "") None
    else Some(liferayOptionalValue)
  }

  def deleteActivities(className: String, classPK: Long): Unit = {
    SocialActivityLocalServiceUtil.deleteActivities(className, classPK)
  }

  def deleteActivity(activityId: Long): Unit = {
    SocialActivityLocalServiceUtil.deleteActivity(activityId)
  }

  def getBy(companyId: Long)(filter: SocialActivity => Boolean): Seq[Activity] = {
    val dq = SocialActivityLocalServiceUtil.dynamicQuery()
    dq.add(RestrictionsFactoryUtil.eq("companyId", companyId))
    SocialActivityLocalServiceUtil
      .dynamicQuery(dq)
      .collect {
        case item if filter(item.asInstanceOf[SocialActivity]) =>
          toModel(item.asInstanceOf[SocialActivity])
      }
  }

  def getById(activityId: Long): Activity = {
    val socialActivity = SocialActivityLocalServiceUtil.getActivity(activityId)
    toModel(socialActivity)
  }

  def getActivities(className: String,
                    start: Int,
                    end: Int): Seq[SocialActivity] =
    SocialActivityLocalServiceUtil.getActivities(className, start, end)

  def addActivity(userId: Long,
                  groupId: Long,
                  className: String,
                  classPK: Long,
                  activityType: Int,
                  extraData: String,
                  receiverUserId: Long): Unit = {
    SocialActivityLocalServiceUtil.addActivity(userId, groupId, className, classPK, activityType, extraData, receiverUserId)
  }

  def getActivities(userId: Long, afterDate: DateTime): Seq[SocialActivity] =
    getUserActivities(userId, QueryUtil.ALL_POS, QueryUtil.ALL_POS)
      .filter(sa => new DateTime(sa.getCreateDate).isAfter(afterDate))

  def getCountActivities(userId: Long, startDate: DateTime, endDate: DateTime, className: String): Int = {
    //TODO: avoid all data reading
    getUserActivities(userId, QueryUtil.ALL_POS, QueryUtil.ALL_POS)
      .count(sa => new DateTime(sa.getCreateDate).isAfter(startDate) &&
      new DateTime(sa.getCreateDate).isBefore(endDate)
      && sa.getClassName == className)
  }

  def getUserActivities(userId: Long,
                        start: Int,
                        end: Int): Seq[SocialActivity] =
    SocialActivityLocalServiceUtil.getUserActivities(userId, start, end)

  def getSocialActivities(start: Int, end: Int): Seq[SocialActivity] =
    SocialActivityLocalServiceUtil.getSocialActivities(start, end)

  def createSocialActivity(id: Long): SocialActivity = SocialActivityLocalServiceUtil.createSocialActivity(id)

  def addActivity(socialActivity: SocialActivity, mirrorSocialActivity: SocialActivity): Unit =
    SocialActivityLocalServiceUtil.addActivity(socialActivity, mirrorSocialActivity)
}

trait ActivityConverter {

  import SocialActivityLocalServiceHelper.toOption

  private def getLiferayFeedEntry(activity: SocialActivity) = {
    if (activity.getClassName.contains("com.liferay")) {
      val ctx = ServiceContextThreadLocal.getServiceContext
      if (ctx.getThemeDisplay != null) {
        Option(SocialActivityInterpreterLocalServiceUtil.interpret(StringPool.BLANK, activity, ctx))
      } else {
        None
      }
    } else {
      None
    }
  }

  protected def toModel(from: SocialActivity): Activity = {
    Activity(
      id = from.getActivityId,
      userId = from.getUserId,
      className = from.getClassName,
      companyId = from.getCompanyId,
      createDate = new DateTime(from.getCreateDate),
      activityType = from.getType,
      classPK = toOption(from.getClassPK),
      groupId = toOption(from.getGroupId),
      extraData = toOption(from.getExtraData),
      liferayFeedEntry = getLiferayFeedEntry(from)
    )
  }
}