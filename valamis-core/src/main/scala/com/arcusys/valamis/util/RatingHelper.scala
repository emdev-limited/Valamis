package com.arcusys.valamis.util

import com.arcusys.learn.liferay.services._

import scala.util.Try

class RatingHelper {

  protected def getRatingEntry(userId: Long, className: String, classPK: Long) =
    Try(RatingsEntryLocalServiceHelper.getRatingEntry(userId, className, classPK)).toOption

  protected def deleteRatingEntry(userId: Long, className: String, classPK: Long) =
    RatingsEntryLocalServiceHelper.deleteEntry(userId, className, classPK)

  protected def deleteRatingEntries(className: String, classPK: Long) = {
    val ratings = RatingsEntryLocalServiceHelper.getEntries(className, classPK)
    for(rating <- ratings) {
      RatingsEntryLocalServiceHelper.deleteEntry(rating.getUserId, className, classPK)
    }
    RatingsStatsLocalServiceHelper.deleteRatingStats(className, classPK)
  }

  protected def updateRatingEntry(userId: Long, className: String, classPK: Long, score: Double) = {
    val ratingEntry = RatingsEntryLocalServiceHelper.updateEntry(userId,className, classPK, score)
    ratingEntry
  }

  protected def getRatingStats(className: String, classPK: Long) =
    Try(RatingsStatsLocalServiceHelper.getRatingStats(className, classPK)).toOption
}
