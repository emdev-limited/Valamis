package com.arcusys.valamis.settings.storage

import com.arcusys.valamis.settings.model.StatementToActivity

trait StatementToActivityStorage {
  def getAll: Seq[StatementToActivity]
  def getById(id: Long): Option[StatementToActivity]
  def getByCourseId(courseId: Long): Seq[StatementToActivity]
  private[settings] def create(entity: StatementToActivity): StatementToActivity
  private[settings] def modify(entity: StatementToActivity)
  private[settings] def delete(id: Long)
}
