package com.arcusys.valamis.lesson.scorm.model.tracking

import com.arcusys.valamis.lesson.scorm.model.ScormUser

case class Attempt(
  id: Long,
  user: ScormUser,
  packageID: Long,
  organizationID: String,
  isComplete: Boolean)
