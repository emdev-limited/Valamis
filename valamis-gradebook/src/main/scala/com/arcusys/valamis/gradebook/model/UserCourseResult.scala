package com.arcusys.valamis.gradebook.model

case class UserCourseResult(courseId: Long,
                            userId: Long,
                            isCompleted: Boolean)

case class UserCourseResultInfo(courseId: Long,
                                userId: Long,
                                courseGrade: Option[CourseGrade],
                                lessonsCount: Int,
                                completedLessonsCount: Int,
                                lessonGrades: Seq[LessonWithGrades]
                                 )