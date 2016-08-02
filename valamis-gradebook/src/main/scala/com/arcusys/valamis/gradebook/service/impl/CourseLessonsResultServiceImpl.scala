package com.arcusys.valamis.gradebook.service.impl

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.gradebook.model.LessonAverageGrade
import com.arcusys.valamis.gradebook.service.{StatisticBuilder, LessonGradeService, CourseLessonsResultService}
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service._

import scala.slick.driver._
import scala.slick.jdbc._

abstract class CourseLessonsResultServiceImpl(val db: JdbcBackend#DatabaseDef,
                                              val driver: JdbcProfile)
  extends CourseLessonsResultService {

  def lessonGradeService: LessonGradeService
  def memberService: LessonMembersService
  def statisticBuilder: StatisticBuilder

  override def getLessonsAverageGrade(lessons: Seq[Lesson], users: Seq[LUser]): Seq[LessonAverageGrade] = {
    lazy val lessonUsers = memberService.getLessonsUsers(lessons, users)
    lessons.map {lesson =>
      if (users.isEmpty) {
        LessonAverageGrade(
          lesson,
          Seq(),
          0F
        )
      }
      else {
        val users = lessonUsers.filter(_.lesson == lesson).map(_.user)
        val grade = lessonGradeService.getLessonAverageGrades(lesson, users)
        LessonAverageGrade(
          lesson,
          users,
          grade
        )
      }
    }
  }
}
