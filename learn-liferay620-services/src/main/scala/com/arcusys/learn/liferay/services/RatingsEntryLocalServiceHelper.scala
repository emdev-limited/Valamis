package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.dao.orm.DynamicQuery
import com.liferay.portal.service.ServiceContextThreadLocal
import com.liferay.portlet.ratings.model.RatingsEntry
import com.liferay.portlet.ratings.service.RatingsEntryLocalServiceUtil

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Created by Igor Borisov on 16.10.15.
 */
object RatingsEntryLocalServiceHelper {

  def updateRatingsEntry(rating: RatingsEntry): RatingsEntry = RatingsEntryLocalServiceUtil.updateRatingsEntry(rating)

  def dynamicQuery(query: DynamicQuery): Seq[RatingsEntry] = RatingsEntryLocalServiceUtil.dynamicQuery(query).asScala.map(_.asInstanceOf[RatingsEntry])

  def dynamicQuery(): DynamicQuery = RatingsEntryLocalServiceUtil.dynamicQuery()

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
