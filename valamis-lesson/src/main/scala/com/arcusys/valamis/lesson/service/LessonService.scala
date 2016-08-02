package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.LessonType._
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.tag.model.ValamisTag
import org.joda.time.DateTime

/**
  * Created by mminin on 19.01.16.
  */
trait LessonService {

  def create(lessonType: LessonType,
             courseId: Long,
             title: String,
             description: String,
             ownerId: Long,
             scoreLimit: Option[Double] = None): Lesson

  def getLesson(id: Long): Option[Lesson]

  def getLessonRequired(id: Long): Lesson

  def getAll(courseId: Long): Seq[Lesson]

  def getByCourses(courseIds: Seq[Long]): Seq[Lesson]

  def getAllSorted(courseId: Long,
                   ascending: Boolean = true,
                   skipTake: Option[SkipTake] = None): Seq[Lesson]

  def getSortedByCourses(courseIds: Seq[Long],
                         ascending: Boolean = true,
                         skipTake: Option[SkipTake] = None): Seq[Lesson]

  def getInReview(courseId: Long): Seq[Lesson]

  def getInReviewByCourses(courseIds: Seq[Long]): Seq[Lesson]

  def getAllWithLimits(courseId: Long): Seq[(Lesson, Option[LessonLimit])]

  def getWithLimit(lessonId: Long): (Lesson, Option[LessonLimit])

  def getAllVisible(courseId: Long): Seq[Lesson]

  def getCount(courseId: Long): Int

  def getCountByCourses(courseIds: Seq[Long]): Int

  def getAll(criterion: LessonFilter,
             ascending: Boolean = true,
             skipTake: Option[SkipTake] = None
            ): RangeResult[LessonFull]

  def getLogo(id: Long): Option[Array[Byte]]

  def setLogo(id: Long, name: String, content: Array[Byte]): Unit

  def getRootActivityId(id: Long): String

  def getRootActivityId(lesson: Lesson): String

  def getByRootActivityId(activityId: String): Option[Long]

  def delete(id: Long): Unit

  def update(lesson: Lesson): Unit

  def update(id: Long,
             title: String,
             description: String,
             isVisible: Option[Boolean],
             beginDate: Option[DateTime],
             endDate: Option[DateTime],
             tagIds: Seq[Long],
             requiredReview: Boolean,
             scoreLimit: Double): Unit

  def updateLessonsInfo(lessonsInfo: Seq[LessonInfo]): Unit

  def updateVisibility(lessonId: Long, isVisible: Option[Boolean]): Unit

  def getTagsFromCourse(courseId: Long): Seq[ValamisTag]

  def getTagsFromCourses(courseIds: Seq[Long]): Seq[ValamisTag]
}


