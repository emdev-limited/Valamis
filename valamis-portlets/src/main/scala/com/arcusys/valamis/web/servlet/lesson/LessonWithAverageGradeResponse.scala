package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.valamis.gradebook.model.Statistic
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.web.servlet.course.CourseResponse

case class LessonWithAverageGradeResponse(lesson: Lesson,
                                          course: CourseResponse,
                                          userCount: Integer,
                                          grade: Float,
                                          statistic: Statistic)
