package com.arcusys.valamis.grade.model

import org.joda.time._

case class PackageGrade(userId: Long,
  packageId: Long,
  grade: Option[Float],
  comment: String,
  date: Option[DateTime] = None)
