package com.arcusys.valamis.gradebook.model

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.lesson.model.{LessonGrade, LessonStates, Lesson}
import org.joda.time.DateTime

case class LessonWithGrades(lesson: Lesson,
                            user: LUser,
                            lastAttemptedDate: Option[DateTime],
                            autoGrade: Option[Float],
                            teacherGrade: Option[LessonGrade],
                            state: Option[LessonStates.Value])