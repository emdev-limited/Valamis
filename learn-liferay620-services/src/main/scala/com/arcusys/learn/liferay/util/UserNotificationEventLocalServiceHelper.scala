package com.arcusys.learn.liferay.util

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.ServiceContextHelper
import com.liferay.portal.kernel.notifications.UserNotificationManagerUtil
import com.liferay.portal.model.{MembershipRequestConstants, UserNotificationDeliveryConstants}
import com.liferay.portal.service.UserNotificationEventLocalServiceUtil
import org.joda.time.DateTime

object UserNotificationEventLocalServiceHelper {

  def addUserNotificationEvent(userId: Long,
                               activityType: String,
                               timestamp: Long,
                               deliverBy: Long,
                               payload: String,
                               archived: Boolean,
                               serviceContext: LServiceContext
                              ): Unit = {
    val classNameId = 0
    if (UserNotificationManagerUtil.isDeliver(userId,
      activityType,
      classNameId,
      MembershipRequestConstants.STATUS_PENDING,
      UserNotificationDeliveryConstants.TYPE_WEBSITE)) {
      UserNotificationEventLocalServiceUtil.addUserNotificationEvent(
        userId,
        activityType,
        timestamp,
        deliverBy,
        payload,
        archived,
        serviceContext
      )
    }
  }

  def sendNotification(model: String, userId: Long, achivedType: String): Unit = {
    val serviceContext = Option(ServiceContextHelper.getServiceContext)
      .getOrElse(new LServiceContext())

    addUserNotificationEvent(userId,
      achivedType,
      DateTime.now().getMillis,
      userId,
      model,
      false,
      serviceContext
    )
  }
}
