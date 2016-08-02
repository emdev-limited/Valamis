package com.arcusys.valamis.gradebook.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.lesson.model.{LessonSort, Lesson}
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.user.model.UserSort

trait LessonGradeService {

  def getCompletedLessonsCount(courseId: Long, userId: Long): Int
  def isLessonFinished(lesson: Lesson, user: LUser): Boolean
  def isLessonFinished(teacherGrade: Option[Float], userId: Long, lesson: Lesson): Boolean
  def isCourseCompleted(courseId: Long, userId: Long): Boolean
  def getFinishedLessonsGradesByUser(user: LUser,
                                     coursesIds: Seq[Long],
                                     isFinished: Boolean,
                                     skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getLastActivityLessonWithGrades(user: LUser,
                                      courseId: Long,
                                      skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getUsersGradesByLessons(users: Seq[LUser],
                              lessons: Seq[Lesson]): Seq[LessonWithGrades]

  def getLessonAverageGrades(lesson: Lesson, users: Seq[LUser]): Float

  def getLessonGradesByCourse(courseId: Long,
                              lessonId: Long,
                              companyId: Long,
                              organizationId: Option[Long],
                              sortBy: Option[UserSort],
                              skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getLessonGradesByCourses(courses: Seq[LGroup],
                               lessonId: Long,
                               companyId: Long,
                               organizationId: Option[Long],
                               sortBy: Option[UserSort],
                               skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getUserGradesByCourse(courseId: Long,
                            user: LUser,
                            sortBy: Option[LessonSort],
                            skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getUserGradesByCourses(courses: Seq[LGroup],
                             user: LUser,
                             sortBy: Option[LessonSort],
                             skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getUsersGradesByCourse(courseId: Long,
                             users: Seq[LUser],
                             sortBy: Option[UserSort],
                             skipTake: Option[SkipTake],
                             inReview: Boolean = false): RangeResult[LessonWithGrades]

  def getUsersGradesByCourses(courses: Seq[LGroup],
                              companyId: Long,
                              organizationId: Option[Long],
                              sortBy: Option[UserSort],
                              skipTake: Option[SkipTake],
                              inReview: Boolean = false): RangeResult[LessonWithGrades]


}