package com.arcusys.valamis.gradebook.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.gradebook.model.LessonAverageGrade
import com.arcusys.valamis.lesson.model.Lesson

trait CourseLessonsResultService {

  def getLessonsAverageGrade(lessons: Seq[Lesson], users: Seq[LUser]): Seq[LessonAverageGrade]

}
