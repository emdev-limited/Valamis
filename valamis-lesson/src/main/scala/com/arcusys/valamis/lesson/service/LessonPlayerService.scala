package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.tag.model.ValamisTag

/**
  * Created by mminin on 11.02.16.
  */
trait LessonPlayerService {
  def setLessonVisibilityFromPlayer(playerId: Long, lessonId: Long, hidden: Boolean): Unit


  def getAll(playerId: Long, courseId: Long): Seq[Lesson]

  def getAvailableToAdd(playerId: Long,
                        courseId: Long,
                        sourceCourseIds: Seq[Long],
                        criterion: LessonFilter,
                        ascending: Boolean,
                        skipTake: Option[SkipTake]): RangeResult[Lesson]

  def addLessonsToPlayer(playerId: Long, lessonIds: Seq[Long]): Unit

  def deleteLessonFromPlayer(playerId: Long, lessonId: Long): Unit

  def getForPlayer(courseId: Long,
                   playerId: Long,
                   user: LUser,
                   title: Option[String],
                   tagId: Option[Long],
                   ascending: Boolean,
                   sortBy: LessonSortBy.Value,
                   skipTake: Option[SkipTake],
                   getSuspendedId: (Long, Lesson) => Option[String]
                  ): RangeResult[LessonWithPlayerState]

  def getAllVisible(courseId: Long, playerId: Long): Seq[Lesson]

  def updateOrder(playerId: Long, ids: Seq[Long]): Unit

  def getTagsFromPlayer(playerId: Long, courseId: Long): Seq[ValamisTag]

  def getLessonIfAvailable(lessonId: Long, user: LUser): Option[Lesson]

  def isLessonVisible(user: LUser, lesson: Lesson): Boolean
}
