package com.arcusys.valamis.gradebook.model

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.lesson.model.Lesson

case class LessonAverageGrade(lesson: Lesson,
                              users: Seq[LUser],
                              grade: Float)
