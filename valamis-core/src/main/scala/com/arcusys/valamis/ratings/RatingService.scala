package com.arcusys.valamis.ratings

import com.arcusys.valamis.ratings.model.Rating
import com.arcusys.valamis.util.RatingHelper

/**
 * Created by Igor Borisov on 19.10.15.
 */
class RatingService[T: Manifest] extends RatingHelper {

  protected val classname = manifest[T].runtimeClass.getName

  def updateRating(userId: Long, score: Double, objId: Long) =
    updateRatingEntry(userId, classname, objId, score)

  def getRating(userId: Long, objId: Long): Rating = {
    val (average, total) = getAverageScore(objId)
    Rating(score = getScore(userId, objId),
           average = average,
           total = total)
  }

  def deleteRatings(objId: Long) = {
    deleteRatingEntries(classname, objId)
  }

  def deleteRating(userId: Long, objId: Long) = {
    deleteRatingEntry(userId, classname, objId)
  }

  private def getScore(userId: Long, objId: Long): Double =
    getRatingEntry(userId, classname, objId) match {
      case Some(rating) => rating.getScore
      case None => 0
    }

  private def getAverageScore(objId: Long): (Double, Int) =
    getRatingStats(classname, objId) match {
      case Some(stats) => (stats.getAverageScore, stats.getTotalEntries)
      case None => (0, 0)
    }
}
