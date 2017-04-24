package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LDynamicQuery, LRatingsStats}
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil

import scala.collection.JavaConverters._

object RatingsStatsLocalServiceHelper {

  def deleteRatingsStats(statsId: Long): Unit = {
    RatingsStatsLocalServiceUtil.deleteRatingsStats(statsId)
  }

  def getStats(className: String, classPK: Long): LRatingsStats = {
    RatingsStatsLocalServiceUtil.getStats(className, classPK)
  }

  def updateRatingsStats(ratingsStats: LRatingsStats): LRatingsStats = {
    RatingsStatsLocalServiceUtil.updateRatingsStats(ratingsStats)
  }

  def dynamicQuery(query: LDynamicQuery): Seq[LRatingsStats] = {
    RatingsStatsLocalServiceUtil.dynamicQuery(query).asScala
      .map(_.asInstanceOf[LRatingsStats])
  }

  def dynamicQuery(): LDynamicQuery = {
    RatingsStatsLocalServiceUtil.dynamicQuery()
  }
  
  def getRatingStats(className: String, classPK: Long): LRatingsStats = {
    RatingsStatsLocalServiceUtil.getStats(className, classPK)
  }
  
  def deleteRatingStats(className: String, classPK: Long): Unit = {
    RatingsStatsLocalServiceUtil.deleteStats(className, classPK)
  }
}
