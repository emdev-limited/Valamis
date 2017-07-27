package com.arcusys.valamis.web.servlet.public.model.response

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.user.model.User
import com.arcusys.valamis.web.servlet.public.model.response.VisibilityType.VisibilityType
import org.joda.time.{DateTime, Period}
import LessonResponse._
/**
  * Created by pkornilov on 1/31/17.
  */
object LessonResponse {
  def toVisibilityType(isVisible: Option[Boolean]) =
    isVisible.fold(VisibilityType.Custom) { isPublic =>
      if (isPublic) VisibilityType.Public else VisibilityType.Hidden
    }

  def toLessonTypeString(lessonType: LessonType) = lessonType match {
    case LessonType.Scorm => "scorm"
    case LessonType.Tincan => "tincan"
    case _ => "unknown type"
  }
}

case class LessonResponse(
   id: Long,
   lessonType: String,
   title: String,
   description: String,
   courseId: Long,
   visibility: VisibilityType,
   creationDate: DateTime,
   beginDate: Option[DateTime],
   endDate: Option[DateTime],
   requiredReview: Boolean,
   scoreLimit: Double,
   rerunLimits: Option[RerunLimits],
   link: String
) {
  def this(lesson: Lesson, link: String, limits: Option[RerunLimits]) = this(
      id = lesson.id,
      lessonType = toLessonTypeString(lesson.lessonType),
      title = lesson.title,
      description = lesson.description,
      courseId = lesson.courseId,
      visibility = toVisibilityType(lesson.isVisible),
      creationDate = lesson.creationDate,
      beginDate = lesson.beginDate,
      endDate = lesson.endDate,
      requiredReview = lesson.requiredReview,
      scoreLimit = lesson.scoreLimit,
      rerunLimits = limits,
      link = link
    )
}

case class RerunLimits(
  maxAttempts: Option[Int],
  period: Option[Period]
)

object VisibilityType extends Enumeration {

  type VisibilityType = Value

  val Hidden = Value(1, "hidden")
  val Public = Value(2, "public")
  val Custom = Value(3, "custom")
}

case class MemberResponse(
  id: Long,
  name: String,
  memberType: MemberTypes.Value
) {

  def this(lUser: LUser) = this(
    id = lUser.getUserId,
    name = lUser.getFullName,
    memberType = MemberTypes.User
  )

  def this(user: User) = this(
    id = user.id,
    name = user.name,
    memberType = MemberTypes.User
  )

  def this(member: Member, viewerType: MemberTypes.Value) = this(
    id = member.id,
    name = member.name,
    memberType = viewerType

  )


}
