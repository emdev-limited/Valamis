package com.arcusys.valamis.web.servlet.report.response.learningReport

import com.arcusys.learn.liferay.LiferayClasses.{LOrganization, LUser}
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lrssupport.lrs.service.util.TinCanActivityType
import com.arcusys.valamis.reports.model.{ActivitiesStatuses, ActivityStatus, DateTime, LessonStatus, PathReportResult, PatternReportStatus}
import com.arcusys.valamis.web.servlet.user.UserResponse
import org.joda.time.DateTime

case class LessonResponse(id: Long, title: String, creationDate: DateTime, hasQuestion: Boolean) {
  def this(lesson: Lesson, hasQuestion: Boolean) = {
    this(lesson.id, lesson.title, lesson.creationDate, hasQuestion)
  }
}

case class UserLessonsResponse(id: Long,
                               user: UserResponse,
                               grade: Option[Float],
                               organizations: Seq[String],
                               lessons: Seq[LessonStatus]
                              ) {
  def this(user: LUser,
           grade: Option[Float],
           organizations: Seq[LOrganization],
           lessons: Seq[LessonStatus]) {
    this(user.getUserId, new UserResponse(user), grade, organizations.map(_.getName), lessons)
  }
}

case class SlideStatusResponse(id: Long,
                               pageId: String,
                               title: String,
                               isQuestion: Boolean,
                               date: Option[DateTime],
                               status: PatternReportStatus.Value) {
  def this(e: ActivityStatus) = this(
    e.activity.id.get,
    e.activity.activityId,
    e.activity.name,
    e.activity.activityType == TinCanActivityType.cmiInteraction.toString,
    e.date,
    e.status
  )
}

case class UserLessonSlidesResponse(userId: Long,
                                    lessonId: Long,
                                    version: Option[String],
                                    date: Option[DateTime],
                                    pages: Seq[SlideStatusResponse]) {
  def this(user: LUser, lesson: Lesson, attemptInfo: ActivitiesStatuses) = {
    this(
      user.getUserId,
      lesson.id,
      attemptInfo.revision,
      attemptInfo.attemptDate,
      attemptInfo.statuses.map(new SlideStatusResponse(_))
    )
  }
}

case class ActivityGoalPathsResponse(id: Long,
                                     goalType: PathsGoalType.Value,
                                     isOptional: Boolean,
                                     title: String,
                                     activityName: String
                                    )

case class CourseGoalPathsResponse(id: Long,
                                   goalType: PathsGoalType.Value,
                                   isOptional: Boolean,
                                   title: String,
                                   courseId: Long
                                  )

case class StatementGoalPathsResponse(id: Long,
                                      goalType: PathsGoalType.Value,
                                      isOptional: Boolean,
                                      title: String,
                                      obj: String,
                                      verb: String
                                     )

case class LessonGoalPathsResponse(id: Long,
                                   goalType: PathsGoalType.Value,
                                   isOptional: Boolean,
                                   title: String,
                                   lessonId: Long
                                  )

case class EventGoalPathsResponse(id: Long,
                                  goalType: PathsGoalType.Value,
                                  isOptional: Boolean,
                                  title: String,
                                  eventId: Long,
                                  startTime: DateTime,
                                  endTime: DateTime
                                 )

case class CertificatePathsResponse(id: Long,
                                    title: String,
                                    creationDate: DateTime,
                                    goals: Seq[_]
                                   )

case class UserCertificateResponse(id: Long,
                                   user: UserResponse,
                                   organizations: Seq[String],
                                   certificates: Seq[PathReportResult]
                                  )

object PathsGoalType extends Enumeration {
  val Empty = Value(0)
  val Activity = Value(1)
  val Course = Value(2)
  val Statement = Value(3)
  val Package = Value(4)
  val Event = Value(6)
  val WebContent = Value(7)
}

case class TotalResponse[T](id: Long, total: Map[T, Int])