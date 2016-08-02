package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.dao.orm.DynamicQuery
import com.liferay.portlet.ratings.model.RatingsStats
import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil

import scala.collection.JavaConverters._

object RatingsStatsLocalServiceHelper {

  def deleteRatingsStats(statsId: Long) = RatingsStatsLocalServiceUtil.deleteRatingsStats(statsId)

  def getStats(className: String, classPK: Long) = RatingsStatsLocalServiceUtil.getStats(className, classPK)


  def updateRatingsStats(ratingsStats: RatingsStats): RatingsStats = RatingsStatsLocalServiceUtil.updateRatingsStats(ratingsStats)

  def dynamicQuery(query: DynamicQuery): Seq[RatingsStats] = RatingsStatsLocalServiceUtil.dynamicQuery(query).asScala.map(_.asInstanceOf[RatingsStats])

  def dynamicQuery(): DynamicQuery = RatingsStatsLocalServiceUtil.dynamicQuery()
  
  def getRatingStats(className: String, classPK: Long) = {
    RatingsStatsLocalServiceUtil.getStats(className, classPK)
  }

  def getRatingStats(statsId: Long) = {
    RatingsStatsLocalServiceUtil.getStats(statsId)
  }

  def deleteRatingStats(className: String, classPK: Long) = {
    RatingsStatsLocalServiceUtil.deleteStats(className, classPK)
  }
}
