package com.arcusys.valamis.gradebook.service.impl

import com.arcusys.learn.liferay.services.MessageBusHelper
import com.arcusys.valamis.gradebook.model.{CourseActivityType, CourseGrade}
import com.arcusys.valamis.gradebook.service._
import com.arcusys.valamis.gradebook.storage.CourseGradeTableComponent
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

class TeacherCourseGradeServiceImpl(val db: JdbcBackend#DatabaseDef,
                                    val driver: JdbcProfile,
                                    implicit val executionContext: ExecutionContext)
  extends TeacherCourseGradeService
    with CourseGradeTableComponent
    with LogSupport
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  val socialActivityHelper = new SocialActivityHelper(CourseActivityType)

  def get(courseId: Long, userId: Long): Option[CourseGrade] = execSync {
    courseGrades
      .filter(x => x.courseId === courseId && x.userId === userId)
      .result
      .headOption
  }

  def get(courseIds: Seq[Long], userId: Long): Seq[CourseGrade] = execSync {
    courseGrades
      .filter(x => x.userId === userId)
      .filter(x => x.courseId inSet courseIds)
      .result
  }

  def set(courseId: Long, userId: Long, grade: Float, comment: Option[String],
          companyId: Long): Unit = execSyncInTransaction {

    (for {
      updatedCount <- courseGrades
        .filter(x => x.courseId === courseId && x.userId === userId)
        .map(x => (x.grade, x.comment))
        .update((Some(grade), comment))

      _ <- if (updatedCount == 0) {
        courseGrades += CourseGrade(
          courseId,
          userId,
          Some(grade),
          DateTime.now(),
          comment)
      } else {
        DBIO.successful()
      }
    } yield ()) map { _ =>
      if (grade > LessonSuccessLimit) {
        onCourseCompleted(companyId, courseId, userId)
      }
    }

  }

  def setComment(courseId: Long, userId: Long, comment: String,
                 companyId: Long): Unit = execSyncInTransaction {
    for {
      updatedCount <- courseGrades
        .filter(x => x.courseId === courseId && x.userId === userId)
        .map(x => x.comment)
        .update(Some(comment))

      _ <- if (updatedCount == 0) {
        courseGrades += CourseGrade(
          courseId,
          userId,
          None,
          DateTime.now(),
          Some(comment)
        )
      } else {
        DBIO.successful()
      }
    } yield ()
  }

  private def onCourseCompleted(companyId: Long, courseId: Long, userId: Long): Unit = {
    sendCourseCompleted(courseId, userId)
    socialActivityHelper.addWithSet(
      companyId,
      userId,
      courseId = Option(courseId.toLong),
      `type` = Some(CourseActivityType.Completed.id),
      classPK = Option(courseId),
      createDate = DateTime.now
    )
  }

  private def sendCourseCompleted(courseId: Long, userId: Long): Unit = {
    try {
      val messageValues = new java.util.HashMap[String, AnyRef]()
      messageValues.put("state", "completed")
      messageValues.put("courseId", courseId.toString)
      messageValues.put("userId", userId.toString)
      MessageBusHelper.sendAsynchronousMessage("valamis/courses/completed", messageValues)
    } catch {
      case ex: Throwable =>
        log.error(s"Failed to send course completed event via MessageBus for " +
          s"courseId: $courseId; userId: $userId ", ex)
    }
  }
}
