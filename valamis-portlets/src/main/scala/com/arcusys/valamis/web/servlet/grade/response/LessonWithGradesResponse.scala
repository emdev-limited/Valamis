package com.arcusys.valamis.web.servlet.grade.response

import com.arcusys.valamis.lesson.model.{Lesson, LessonGrade, LessonStates}
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.web.servlet.course.CourseResponse
import org.joda.time.DateTime

case class LessonWithGradesResponse(lesson: Lesson,
                                    user: UserInfo,
                                    course: Option[CourseResponse],
                                    lastAttemptedDate: Option[DateTime],
                                    teacherGrade: Option[LessonGrade],
                                    autoGrade: Option[Float],
                                    state: Option[LessonStates.Value])