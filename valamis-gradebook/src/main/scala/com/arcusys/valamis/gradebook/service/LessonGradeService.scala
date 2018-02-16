package com.arcusys.valamis.gradebook.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.gradebook.model.{LessonWithGrades, UserCoursesWithGrade}
import com.arcusys.valamis.lesson.model.{Lesson, LessonSort}
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.user.model.UserSort

trait LessonGradeService {

  def getCompletedLessonsCount(courseId: Long, userId: Long): Int

  def isLessonFinished(teacherGrade: Option[Float], userId: Long, lesson: Lesson): Boolean

  def isCourseCompleted(courseId: Long, userId: Long): Boolean

  def getCoursesCompletedWithGrade(userId: Long): Seq[UserCoursesWithGrade]

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

  def getLessonAverageGradesForReport(lesson: Lesson,
                                      users: Seq[LUser]): Option[Float]

  def getLessonGradesByCourse(courseId: Long,
                              lessonId: Long,
                              companyId: Long,
                              organizationId: Option[Long],
                              userNameFilter: Option[String],
                              sortBy: Option[UserSort],
                              skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getLessonGradesByCourses(courses: Seq[LGroup],
                               lessonId: Long,
                               companyId: Long,
                               organizationId: Option[Long],
                               userNameFilter: Option[String],
                               sortBy: Option[UserSort],
                               skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getUserGradesByCourse(courseId: Long,
                            user: LUser,
                            lessonTitleFilter: Option[String],
                            sortBy: Option[LessonSort],
                            skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getUserGradesByCourses(courses: Seq[LGroup],
                             user: LUser,
                             lessonTitleFilter: Option[String],
                             sortBy: Option[LessonSort],
                             skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getInReviewByCourse(courseId: Long,
                          users: Seq[LUser],
                          nameFilter: Option[String],
                          sortBy: Option[UserSort],
                          skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]

  def getInReviewByCourses(courses: Seq[LGroup],
                           companyId: Long,
                           organizationId: Option[Long],
                           nameFilter: Option[String],
                           sortBy: Option[UserSort],
                           skipTake: Option[SkipTake]): RangeResult[LessonWithGrades]


}