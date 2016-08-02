package com.arcusys.valamis.lesson.service.impl

import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.lesson.model.{LessonGrade, UserLessonResult}
import com.arcusys.valamis.lesson.service.{LessonService, TeacherLessonGradeService}
import com.arcusys.valamis.lesson.storage.{LessonGradeTableComponent, LessonTableComponent}
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 04.04.16.
  */
abstract class TeacherLessonGradeServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends TeacherLessonGradeService
    with LessonGradeTableComponent
    with SlickProfile
    with LessonTableComponent {

  import driver.simple._

  def lrsClient: LrsClientManager
  def courseService: CourseService
  def lessonService: LessonService

  def get(userId: Long, lessonId: Long): Option[LessonGrade] = {
    db.withSession { implicit s =>
      lessonGrades
        .filter(x => x.lessonId === lessonId && x.userId === userId)
        .firstOption
    }
  }

  def get(userId: Long, lessonIds: Seq[Long]): Seq[LessonGrade] = {
    if (lessonIds.isEmpty) {
      Nil
    } else {
      db.withSession { implicit s =>
        lessonGrades
          .filter(x => x.lessonId.inSet(lessonIds) && x.userId === userId)
          .list
      }
    }
  }

  //TODO: link to table with cascade delete
  def deleteByLesson(lessonId: Long): Unit = {
    db.withSession { implicit s =>
      lessonGrades
        .filter(_.lessonId === lessonId)
        .delete
    }
  }

  def set(userId: Long, lessonId: Long, grade: Option[Float], comment: Option[String]): Unit = {

    db.withTransaction { implicit s =>
      val updatedCount = lessonGrades.filter(x => x.lessonId === lessonId && x.userId === userId)
        .map(x => (x.grade, x.comment))
        .update((grade, comment))

      if (updatedCount == 0) {
        lessonGrades.insert(new LessonGrade(lessonId, userId, grade, DateTime.now, comment))
      }
    }
    onLessonGraded(userId, lessonId, grade)
  }

  def get(userIds: Seq[Long], lessonIds: Seq[Long]): Seq[LessonGrade] = {
    if (userIds.isEmpty || lessonIds.isEmpty) {
      Nil
    }
    else {
      db.withSession { implicit s =>
        lessonGrades
          .filter(_.lessonId inSet lessonIds)
          .filter(_.userId inSet userIds)
          .list
      }
    }
  }

  def get(userIds: Seq[Long], lessonId: Long): Seq[LessonGrade] = {
    if (userIds.isEmpty) {
      Nil
    }
    else {
      db.withSession { implicit s =>
        lessonGrades
          .filter(_.lessonId === lessonId)
          .filter(_.userId inSet userIds)
          .list
      }
    }
  }

  def onLessonGraded(userId: Long, lessonId: Long, grade: Option[Float]): Unit = {}
}
