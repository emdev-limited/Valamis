package com.arcusys.valamis.settings.service

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.settings.model.StatementToActivity
import com.arcusys.valamis.settings.storage.StatementToActivityStorage
import com.escalatesoft.subcut.inject.{ BindingModule, Injectable }

class LRSToActivitySettingServiceImpl(implicit val bindingModule: BindingModule) extends LRSToActivitySettingService with Injectable {

  val lrsToActivitySettingStorage = inject[StatementToActivityStorage]

  def getByCourseId(courseId: Int): Seq[StatementToActivity] = {
    lrsToActivitySettingStorage.getByCourseId(courseId)
  }

  def create(courseId: Int, title: String, mappedActivity: Option[String], mappedVerb: Option[String]): StatementToActivity = {
    lrsToActivitySettingStorage.create(StatementToActivity(courseId, title, mappedActivity, mappedVerb))
  }

  def modify(id: Int, courseId: Int, title: String, mappedActivity: Option[String], mappedVerb: Option[String]): StatementToActivity = {
    val entity = lrsToActivitySettingStorage.getById(id).getOrElse(throw new EntityNotFoundException)
      .copy(courseId = courseId, title = title, mappedActivity = mappedActivity, mappedVerb = mappedVerb)

    lrsToActivitySettingStorage.modify(entity)

    entity
  }

  def delete(id: Int) {
    lrsToActivitySettingStorage.delete(id)
  }
}
