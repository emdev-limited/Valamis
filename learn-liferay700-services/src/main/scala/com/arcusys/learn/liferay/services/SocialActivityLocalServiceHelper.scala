package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.LSocialActivity
import com.arcusys.learn.liferay.model.Activity
import com.liferay.portal.kernel.dao.orm.{QueryUtil, RestrictionsFactoryUtil}
import com.liferay.portal.kernel.service.{ServiceContext, ServiceContextThreadLocal}
import com.liferay.portal.kernel.util.StringPool
import com.liferay.social.kernel.model.{SocialActivity, SocialActivityFeedEntry}
import com.liferay.social.kernel.service.{SocialActivityInterpreterLocalServiceUtil, SocialActivityLocalServiceUtil}
import org.joda.time.DateTime

import scala.collection.JavaConversions._

object SocialActivityLocalServiceHelper extends ActivityConverter {

  def get(className: String,
          start: Int = QueryUtil.ALL_POS,
          end: Int = QueryUtil.ALL_POS) : Seq[LSocialActivity] = {
    SocialActivityLocalServiceUtil.getActivities(className, start, end)
  }

  def interpret(selector: String, activity: LSocialActivity, ctx: ServiceContext): SocialActivityFeedEntry =
    SocialActivityInterpreterLocalServiceUtil.interpret(selector, activity, ctx)

  def updateSocialActivity(activity: SocialActivity): SocialActivity = SocialActivityLocalServiceUtil.updateSocialActivity(activity)

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
      .dynamicQuery[SocialActivity](dq)
      .collect {
        case item if filter(item) =>
          toModel(item)
      }
  }

  def getById(activityId: Long): Activity = {
    val socialActivity = SocialActivityLocalServiceUtil.getActivity(activityId)
    toModel(socialActivity)
  }

  def getActivities(className: String,
                    start: Int,
                    end: Int): Seq[LSocialActivity] =
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

  private def toOption(liferayOptionalValue: Long) = {
    Some(liferayOptionalValue).filterNot(_ == 0)
  }

  private def toOption(liferayOptionalValue: String) = {
    Some(liferayOptionalValue).filterNot(_.isEmpty)
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