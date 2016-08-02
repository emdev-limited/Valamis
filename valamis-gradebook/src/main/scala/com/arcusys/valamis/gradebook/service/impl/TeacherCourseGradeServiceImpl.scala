package com.arcusys.valamis.gradebook.service.impl

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.gradebook.model.{UserCourseResultInfo, LessonWithGrades, CourseActivityType, CourseGrade}
import com.arcusys.valamis.gradebook.service._
import com.arcusys.valamis.gradebook.storage.CourseGradeTableComponent
import com.arcusys.valamis.lesson.model.LessonStates
import com.arcusys.valamis.lesson.service.{TeacherLessonGradeService, UserLessonResultService, LessonService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class TeacherCourseGradeServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends TeacherCourseGradeService
    with CourseGradeTableComponent
    with SlickProfile {

  import driver.simple._

  val socialActivityHelper = new SocialActivityHelper(CourseActivityType)

  def get(courseId: Long, userId: Long): Option[CourseGrade] = {
    db.withSession { implicit s =>
      courseGrades
        .filter(x => x.courseId === courseId && x.userId === userId)
        .firstOption
    }
  }

  def set(courseId: Long, userId: Long, grade: Option[Float], comment: Option[String], companyId: Long) {
    db.withTransaction { implicit s =>
      val updatedCount = courseGrades
        .filter(x => x.courseId === courseId && x.userId === userId)
        .map(x => (x.grade, x.comment))
        .update((grade, comment))

      if (updatedCount == 0) {
        courseGrades += CourseGrade(
          courseId,
          userId,
          grade,
          DateTime.now(),
          comment)
      }
    }

    if (grade.exists(_ > LessonSuccessLimit)) {
      socialActivityHelper.addWithSet(
        companyId,
        userId,
        courseId = Option(courseId.toLong),
        `type` = Some(CourseActivityType.Completed.id),
        classPK = Option(courseId),
        createDate = DateTime.now
      )
    }
  }
}
