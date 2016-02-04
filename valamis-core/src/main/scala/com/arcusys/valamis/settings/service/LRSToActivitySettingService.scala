package com.arcusys.valamis.settings.service

import com.arcusys.valamis.settings.model.StatementToActivity

trait LRSToActivitySettingService {
  def getByCourseId(courseId: Int): Seq[StatementToActivity]
  def create(courseId: Int, title: String, mappedActivity: Option[String], mappedVerb: Option[String]): StatementToActivity
  def modify(id: Int, courseID: Int, title: String, mappedActivity: Option[String], mappedVerb: Option[String]): StatementToActivity
  def delete(id: Int)
}
