package com.arcusys.valamis.ratings

import com.arcusys.learn.liferay.LiferayClasses.LRatingsEntry
import com.arcusys.valamis.ratings.model.Rating

/**
  * Created by Igor Borisov on 19.10.15.
  */
class RatingService[T: Manifest] extends RatingHelper {

  protected val classname = manifest[T].runtimeClass.getName

  def updateRating(userId: Long, score: Double, objId: Long): LRatingsEntry = {
    updateRatingEntry(userId, classname, objId, score)
  }

  def getRating(userId: Long, objId: Long): Rating = {
    val (average, total) = getAverageScore(objId)
    Rating(
      score = getScore(userId, objId),
      average = average,
      total = total
    )
  }

  def getRating(objId: Long): Rating = {
    val (average, total) = getAverageScore(objId)
    Rating(
      score = 0, //no user score
      average = average,
      total = total
    )
  }

  def deleteRatings(objId: Long): Unit = {
    deleteRatingEntries(classname, objId)
  }

  def deleteRating(userId: Long, objId: Long): Rating = {
    deleteRatingEntry(userId, classname, objId)
    getRating(objId)
  }

  private def getScore(userId: Long, objId: Long): Double = {
    getRatingEntry(userId, classname, objId) match {
      case Some(rating) => rating.getScore
      case None => 0
    }
  }

  private def getAverageScore(objId: Long): (Double, Int) = {
    getRatingStats(classname, objId) match {
      case Some(stats) => (stats.getAverageScore, stats.getTotalEntries)
      case None => (0, 0)
    }
  }
}
