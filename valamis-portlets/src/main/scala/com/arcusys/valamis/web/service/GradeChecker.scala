package com.arcusys.valamis.web.service

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.gradebook.model.CourseActivityType
import com.arcusys.valamis.gradebook.service.{LessonGradeService, LessonSuccessLimit, UserCourseResultService}
import com.arcusys.valamis.lesson.model.{Lesson, PackageActivityType}
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.lrs.service.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.uri.service.TincanURIService
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait GradeChecker {
  def checkCourseComplition(companyId: Long, userId: Long, statements: Seq[Statement]): Unit
}

class GradeCheckerImpl(implicit val bindingModule: BindingModule) extends GradeChecker with Injectable {

  lazy val uriService = inject[TincanURIService]
  lazy val packageChecker = inject[LessonGradeService]
  lazy val userCourseService = inject[UserCourseResultService]
  lazy val lessonService = inject[LessonService]
  lazy val tincanPackageService = inject[TincanPackageService]
  lazy val lessonSocialActivityHelper = new SocialActivityHelper[Lesson]
  lazy val courseSocialActivityHelper = new SocialActivityHelper(CourseActivityType)

  def checkCourseComplition(companyId: Long, userId: Long, statements: Seq[Statement]): Unit = {
    //TODO: refactor. all statements should be for same course and package, 
    // move package reading and part of isCourseComplete out of for

    val user = UserLocalServiceHelper().getUser(userId)
    for {
      statement <- statements if TincanHelper.isVerbType(statement.verb, TinCanVerbs.Completed) ||
      TincanHelper.isVerbType(statement.verb, TinCanVerbs.Passed)
      score <- statement.result.flatMap(_.score).flatMap(_.scaled)
      if score >= LessonSuccessLimit
    } {

      for {
        lessonId <- lessonService.getByRootActivityId(statement.obj.asInstanceOf[Activity].id)
        lesson <- lessonService.getLesson(lessonId)
        courseId = lesson.courseId
      } {

        lessonSocialActivityHelper.addWithSet(
          companyId,
          userId,
          courseId = Some(courseId),
          `type` = Some(PackageActivityType.Completed.id),
          classPK = Some(lessonId),
          createDate = statement.timestamp)

        if (packageChecker.isCourseCompleted(courseId, userId)) {

          courseSocialActivityHelper.addWithSet(
            companyId,
            userId,
            courseId = Some(courseId),
            `type` = Some(CourseActivityType.Completed.id),
            classPK = Some(courseId),
            createDate = statement.timestamp
          )

          userCourseService.set(courseId, userId, isCompleted = true)
        }

      }
    }
  }
}
