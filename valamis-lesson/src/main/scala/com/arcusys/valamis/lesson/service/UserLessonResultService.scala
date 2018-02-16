package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.lesson.model.{Lesson, UserLessonResult}
import com.arcusys.valamis.lrs.tincan.Statement
import org.joda.time.DateTime

/**
  * Created by lpotahina on 04.03.16.
  */
trait UserLessonResultService {

  def get(lesson: Lesson, user: LUser): UserLessonResult

  def getAttemptedDate(lessonId: Long, userId: Long): Option[DateTime]

  def update(user: LUser, statements: Seq[Statement]): Unit

  def isLessonAttempted(user: LUser, lesson: Lesson): Boolean

  def isLessonFinished(user: LUser, lesson: Lesson): Boolean

  def getLastLessons(user: LUser, courseIds: Seq[Long], count: Int): Seq[(UserLessonResult, Lesson)]

  def getUserResults(user: LUser, courseId: Long): Seq[UserLessonResult]

  def getLastResultForCourse(userId: Long, courseId: Long): Option[UserLessonResult]

  def getLastResult(userId: Long): Option[UserLessonResult]

  def get(users: Seq[LUser], lessons: Seq[Lesson]): Seq[UserLessonResult]

  def getAttemptedTotal(lessonsIds: Seq[Long],
                        isFinished: Boolean,
                        limit: Int,
                        offset: Int): Seq[(Long, Int)]

  def getAttemptedTotal(lessonsIds: Seq[Long],
                        userIds: Seq[Long],
                        isFinished: Boolean): Seq[(Long, Int)]
}
