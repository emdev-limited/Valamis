package com.arcusys.learn.liferay.services

import com.liferay.portlet.ratings.service.RatingsStatsLocalServiceUtil

/**
 * Created by Igor Borisov on 16.10.15.
 */
object RatingsStatsLocalServiceHelper {
  
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
