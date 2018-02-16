package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LDynamicQuery, LRatingsEntry}
import com.liferay.portal.kernel.service.ServiceContextThreadLocal
import com.liferay.ratings.kernel.service.RatingsEntryLocalServiceUtil

import scala.collection.JavaConverters._

object RatingsEntryLocalServiceHelper {

  // Used to convert rating values for Liferay 7.
  // Liferay 7 uses 0.0 - 1.0 values.
  // Liferay 6 uses constants of MIN (1) and MAX (5) rating values (not in 0.0 - 1.0 interval).
  // Valamis uses 1-5 rating
  private val RatingMaxScore = 5

  def updateRatingsEntry(rating: LRatingsEntry): LRatingsEntry = {
    RatingsEntryLocalServiceUtil.updateRatingsEntry(toLiferay(rating))
  }

  def dynamicQuery(query: LDynamicQuery): Seq[LRatingsEntry] = {
    RatingsEntryLocalServiceUtil.dynamicQuery[LRatingsEntry](query).asScala
      .map(toValamis)
  }

  def dynamicQuery(): LDynamicQuery = {
    RatingsEntryLocalServiceUtil.dynamicQuery()
  }

  def getRatingEntry(userId: Long, className: String, classPK: Long): LRatingsEntry = {
    val entity = RatingsEntryLocalServiceUtil.getEntry(userId, className, classPK)
    toValamis(entity)
  }

  def getEntries(className: String, classPK: Long):Seq[LRatingsEntry] = {
    RatingsEntryLocalServiceUtil.getEntries(className, classPK).asScala
      .map(toValamis)
  }

  def deleteEntry(userId: Long, className: String, classPK: Long): Unit = {
    RatingsEntryLocalServiceUtil.deleteEntry(userId, className, classPK)
  }

  def updateEntry(userId: Long, className: String, classPK: Long, score: Double): LRatingsEntry = {
    val serviceContext = ServiceContextThreadLocal.getServiceContext
    RatingsEntryLocalServiceUtil.updateEntry(userId, className, classPK, toLiferay(score), serviceContext)
  }

  private def toLiferay(value: Double): Double = {
    value / RatingMaxScore
  }

  private def toLiferay(rating: LRatingsEntry): LRatingsEntry = {
    rating.setScore(toLiferay(rating.getScore))
    rating
  }

  private def toValamis(rating: LRatingsEntry): LRatingsEntry = {
    rating.setScore(rating.getScore * RatingMaxScore)
    rating
  }
}
