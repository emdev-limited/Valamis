package com.arcusys.valamis.web.listener

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.gradebook.model.CourseActivityType
import com.arcusys.valamis.gradebook.service.{LessonGradeService, UserCourseResultService}
import com.arcusys.valamis.lesson.model.{Lesson, PackageActivityType}
import com.arcusys.valamis.lesson.service.{LessonService, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.liferay.SocialActivityHelper
import org.joda.time.DateTime

abstract class LessonListener {
  def lessonService: LessonService
  def lessonResultService: UserLessonResultService
  def gradeService: LessonGradeService
  def teacherGradeService: TeacherLessonGradeService
  def lessonGradeService: LessonGradeService
  def userCourseService: UserCourseResultService
  def certificateChecker: CertificateStatusChecker
  lazy val lessonSocialActivityHelper = new SocialActivityHelper[Lesson]
  lazy val courseSocialActivityHelper = new SocialActivityHelper(CourseActivityType)
  lazy val activityTimeout = 1000*60*60*24 //24 hours

  def lessonGraded(userId: Long, lessonId: Long, grade: Option[Float]): Unit = {
    lessonService.getLesson(lessonId).foreach { lesson =>
      if (gradeService.isLessonFinished(grade, userId, lesson)) {
        onLessonCompleted(lesson, userId, new DateTime)
      }
    }
  }

  def lessonFinished(userId: Long, lessonId: Long, attemptDate: DateTime): Unit = {
    lessonService.getLesson(lessonId).foreach { lesson =>
      val grade = teacherGradeService.get(userId, lessonId).flatMap(_.grade)
      if (gradeService.isLessonFinished(grade, userId, lesson)) {
        onLessonCompleted(lesson, userId, attemptDate)
      }
    }
  }

  private def onLessonCompleted(lesson: Lesson, userId: Long, attemptDate: DateTime): Unit = {
    createLessonActivity(userId, lesson.id, lesson.courseId, attemptDate)
    certificateChecker.updatePackageGoalState(userId, lesson.id, attemptDate)
    if (lessonGradeService.isCourseCompleted(lesson.courseId, userId)) {
      createCourseActivity(userId, lesson.courseId, attemptDate)
      userCourseService.set(lesson.courseId, userId, isCompleted = true)
    }

  }

  private def createLessonActivity(userId: Long, lessonId: Long, courseId: Long, attemptDate: DateTime): Unit = {
    val delay = System.currentTimeMillis() - attemptDate.getMillis
    if (delay <= activityTimeout && delay >= 0) {
      val companyId = CompanyHelper.getCompanyId
      lessonSocialActivityHelper.addWithSet(
        companyId,
        userId,
        courseId = Some(courseId),
        `type` = Some(PackageActivityType.Completed.id),
        classPK = Some(lessonId),
        createDate = attemptDate)
    }
  }

  private def createCourseActivity(userId: Long, courseId: Long, attemptDate: DateTime): Unit = {
    val delay = System.currentTimeMillis() - attemptDate.getMillis
    if (delay <= activityTimeout && delay >= 0) {
      val companyId = CompanyHelper.getCompanyId
      courseSocialActivityHelper.addWithSet(
        companyId,
        userId,
        courseId = Some(courseId),
        `type` = Some(CourseActivityType.Completed.id),
        classPK = Some(courseId),
        createDate = attemptDate)
    }
  }
}
