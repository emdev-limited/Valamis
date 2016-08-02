package com.arcusys.valamis.web.servlet.social.response

import com.arcusys.valamis.web.servlet.course.CourseResponse

object Activities extends Enumeration{
  val Lesson, Course, Certificate, UserStatus, LiferayEntry = Value
}

sealed trait ActivityObjectResponse {
  def tpe: Activities.Value
  val withImage: Boolean
}

case class ActivityPackageResponse(
  id: Long,
  title: String,
  logo: Option[String],
  course: Option[CourseResponse],
  comment: Option[String],
  tpe: Activities.Value = Activities.Lesson,
  override val withImage: Boolean = true,
  url: Option[String]
) extends ActivityObjectResponse

case class ActivityCourseResponse(
  id: Long,
  title: String,
  logoCourse: Option[String],
  tpe: Activities.Value = Activities.Course,
  override val withImage: Boolean = true
) extends ActivityObjectResponse

case class ActivityCertificateResponse(
  id: Long,
  title: String,
  logo: Option[String],
  tpe: Activities.Value = Activities.Certificate,
  override val withImage: Boolean = true,
  url: Option[String]
) extends ActivityObjectResponse

case class ActivityUserStatusResponse(
  comment: String,
  tpe: Activities.Value = Activities.UserStatus,
  override val withImage:Boolean = false
) extends ActivityObjectResponse

case class LActivityEntryResponse(
  id: Long,
  title: String,
  body: String,
  tpe: Activities.Value = Activities.LiferayEntry,
  liferayEntry: Boolean = true,
  override val withImage:Boolean = false
) extends ActivityObjectResponse