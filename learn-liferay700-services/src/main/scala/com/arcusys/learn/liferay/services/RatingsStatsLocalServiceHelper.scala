package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LDynamicQuery, LRatingsStats}
import com.liferay.ratings.kernel.service.RatingsStatsLocalServiceUtil

import scala.collection.JavaConverters._

object RatingsStatsLocalServiceHelper {

  // Used to convert rating values for Liferay 7.
  // Liferay 7 uses 0.0 - 1.0 values.
  // Liferay 6 uses constants of MIN (1) and MAX (5) rating values (not in 0.0 - 1.0 interval).
  // Valamis uses 1-5 rating
  private val maxScore = 5

  def deleteRatingsStats(statsId: Long): Unit = {
    RatingsStatsLocalServiceUtil.deleteRatingsStats(statsId)
  }

  def getStats(className: String, classPK: Long): LRatingsStats = {
    toValamis(RatingsStatsLocalServiceUtil.getStats(className, classPK))
  }

  def updateRatingsStats(ratingsStats: LRatingsStats): LRatingsStats = {
    RatingsStatsLocalServiceUtil.updateRatingsStats(toLiferay(ratingsStats))
  }

  def dynamicQuery(query: LDynamicQuery): Seq[LRatingsStats] = {
    RatingsStatsLocalServiceUtil.dynamicQuery[LRatingsStats](query).asScala
      .map(toValamis)
  }

  def dynamicQuery(): LDynamicQuery = {
    RatingsStatsLocalServiceUtil.dynamicQuery()
  }
  
  def getRatingStats(className: String, classPK: Long): LRatingsStats = {
    val entity = RatingsStatsLocalServiceUtil.getStats(className, classPK)

    toValamis(entity)
  }

  def deleteRatingStats(className: String, classPK: Long): Unit = {
    RatingsStatsLocalServiceUtil.deleteStats(className, classPK)
  }

  private def toLiferay(entity: LRatingsStats): LRatingsStats = {
    entity.setAverageScore(entity.getAverageScore / maxScore)
    entity.setTotalScore(entity.getTotalScore / maxScore)
    entity
  }

  private def toValamis(entity: LRatingsStats): LRatingsStats = {
    entity.setAverageScore(entity.getAverageScore * maxScore)
    entity.setTotalScore(entity.getTotalScore * maxScore)
    entity
  }
}
