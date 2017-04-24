package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.model.SlideSet
import org.joda.time.DateTime

trait SlideSetRepository {
  def getById(id: Long): Option[SlideSet]
  def getByActivityId(activityId: String): Seq[SlideSet]
  def getByCourseId(courseId: Long, titleFilter: Option[String]): Seq[SlideSet]
  def getTemplates: Seq[SlideSet]
  def getByVersion(activityId: String, version: Double): Seq[SlideSet]
  def delete(id: Long): Unit
  def update(id: Long, title: String, description: String): Unit
  def update(id: Long,
             isSelectedContinuity: Boolean,
             themeId: Option[Long],
             duration: Option[Long],
             scoreLimit: Option[Double],
             playerTitle: String,
             topDownNavigation: Boolean,
             status: String,
             version: Double,
             oneAnswerAttempt: Boolean,
             requiredReview: Boolean): Unit
  def updateLockUser(slideSetId: Long, userId: Option[Long], date: Option[DateTime]): Unit
  def updateLogo(id: Long, name: Option[String]): Unit
  def updateStatus(id: Long, status: String): Unit
  def updateStatusWithDate(id: Long, status: String, date: DateTime): Unit
  def create(slideSet: SlideSet): SlideSet
}