package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.LessonGrade

trait TeacherLessonGradeService {
  def get(userId: Long, lessonId: Long): Option[LessonGrade]

  def get(userId: Long, lessonIds: Seq[Long]): Seq[LessonGrade]

  def set(userId: Long, lessonId: Long, grade: Option[Float], comment: Option[String]): Unit

  def get(userIds: Seq[Long], lessonIds: Seq[Long]): Seq[LessonGrade]

  def get(userIds: Seq[Long], lessonId: Long): Seq[LessonGrade]

  def deleteByLesson(lessonId: Long): Unit
}


