package com.arcusys.learn.liferay.services

import com.liferay.portal.service.ServiceContextThreadLocal
import com.liferay.portlet.ratings.model.RatingsEntry
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil
import scala.collection.JavaConverters._

/**
 * Created by Igor Borisov on 16.10.15.
 */
object RatingsEntryLocalServiceHelper {

  def getRatingEntry(userId: Long, className: String, classPK: Long) = {
    RatingsEntryLocalServiceUtil.getEntry(userId, className, classPK)
  }

  def getRatingEntry(entryId: Long) = {
    RatingsEntryLocalServiceUtil.getRatingsEntry(entryId)
  }

  def getEntries(className: String, classPK: Long):Seq[RatingsEntry] = {
    RatingsEntryLocalServiceUtil.getEntries(className, classPK).asScala
  }

  def deleteEntry(userId: Long, className: String, classPK: Long) =
    RatingsEntryLocalServiceUtil.deleteEntry(userId, className, classPK)


  def updateEntry(userId:Long, className: String, classPK: Long, score: Double): RatingsEntry = {
    val serviceContext= ServiceContextThreadLocal.getServiceContext
    RatingsEntryLocalServiceUtil.updateEntry(userId, className, classPK, score, serviceContext)
  }
}
