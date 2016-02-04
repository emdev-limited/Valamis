package com.arcusys.valamis.lesson.scorm.model.tracking

import com.arcusys.valamis.user.model.ScormUser

case class Attempt(
  id: Int,
  user: ScormUser,
  packageID: Int,
  organizationID: String,
  isComplete: Boolean)
