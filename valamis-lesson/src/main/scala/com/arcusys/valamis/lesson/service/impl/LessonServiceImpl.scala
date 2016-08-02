package com.arcusys.valamis.lesson.service.impl

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.exception.NoLessonException
import com.arcusys.valamis.lesson.model.LessonType._
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service.{CustomLessonService, LessonAssetHelper, LessonService}
import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonTableComponent}
import com.arcusys.valamis.lesson.storage.query._
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.user.model.User
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 21.01.16.
  */
abstract class LessonServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LessonService
    with LessonTableComponent
    with LessonQueries
    with LessonPlayerQueries
    with LessonLimitQueries
    with LessonViewerQueries
    with LessonAttemptsQueries
    with LessonAttemptsTableComponent
    with SlickProfile {

  import driver.simple._

  def ratingService: RatingService[Lesson]
  def tagService: TagService[Lesson]
  def assetHelper: LessonAssetHelper
  def socialActivityHelper: SocialActivityHelper[Lesson]
  def userService: UserLocalServiceHelper
  def fileService: FileService
  def fileStorage: FileStorage
  def customLessonServices: Map[LessonType, CustomLessonService]
  private def logoPathPrefix(packageId: Long) = s"package_logo_$packageId/"
  private def getFullLogoPath(packageId: Long, logo: String) = "files/" + logoPathPrefix(packageId) + logo


  override def create(lessonType: LessonType,
                      courseId: Long,
                      title: String,
                      description: String,
                      ownerId: Long,
                      scoreLimit: Option[Double] = None): Lesson = {
    val logo = None
    val isVisible = Some(true)

    val lessonId = db.withTransaction { implicit s =>
      lessons
        .map(l => (
          l.lessonType,
          l.title,
          l.description,
          l.logo,
          l.courseId,
          l.isVisible,
          l.ownerId,
          l.creationDate,
          l.scoreLimit,
          l.requiredReview))
        .returning(lessons.map(_.id))
        .insert(
          lessonType,
          title,
          description,
          logo,
          courseId,
          isVisible,
          ownerId,
          DateTime.now,
          scoreLimit.getOrElse(0.7),
          false)
    }

    getLessonRequired(lessonId)
  }

  override def getLesson(id: Long): Option[Lesson] = {
    db.withSession { implicit s =>
      lessons.filterById(id).firstOption
    }
  }

  override def getRootActivityId(id: Long): String = {
    db.withSession { implicit s =>
      lessons.filterById(id).selectType.firstOption
    } match {
      case Some(lessonType) => customLessonServices(lessonType).getRootActivityId(id)
      case None => throw new NoLessonException(id)
    }
  }

  def getRootActivityId(lesson: Lesson): String = {
    customLessonServices(lesson.lessonType).getRootActivityId(lesson.id)
  }

  override def getByRootActivityId(activityId: String): Option[Long] = {
    customLessonServices.toStream
      .flatMap { case (lessonType, service) => service.getLessonIdByRootActivityId(activityId) }
      .headOption
  }

  override def getLessonRequired(id: Long): Lesson = {
    getLesson(id) getOrElse (throw new EntityNotFoundException(s"Lesson not found, id $id"))
  }

  override def getCount(courseId: Long): Int = {
    //visible ?
    db.withSession { implicit s =>
      lessons.filterByCourseId(courseId).length.run
    }
  }

  override def getCountByCourses(courseIds: Seq[Long]): Int = {
    //visible ?
    db.withSession { implicit s =>
      lessons.filterByCourseIds(courseIds).length.run
    }
  }

  def getLogo(id: Long): Option[Array[Byte]] = {
    val lesson = getLesson(id)

    val logoPath = lesson.flatMap(_.logo).map(getFullLogoPath(id, _))

    logoPath.flatMap(fileService.getFileContentOption)
  }

  def setLogo(id: Long, name: String, content: Array[Byte]): Unit = {
    val lesson = getLessonRequired(id)

    fileService.setFileContent(
      folder = logoPathPrefix(lesson.id),
      name = name,
      content = content,
      deleteFolder = true
    )

    db.withSession { implicit s =>
      lessons.filterById(id).map(_.logo).update(Some(name))
    }
  }

  override def delete(id: Long): Unit = {
    getLesson(id) match {
      case None =>
      case Some(lesson) =>

        val customService = customLessonServices(lesson.lessonType)

        if (lesson.logo.isDefined) {
          fileService.deleteByPrefix(logoPathPrefix(lesson.id))
        }

        ratingService.deleteRatings(id)

        db.withTransaction(implicit s => {
          playerLessons.filterBy(id).delete
          lessonLimits.filterByLessonId(id).delete
          lessonViewers.filterByLessonId(id).delete

          lessonAttempts.filterByLessonId(id).delete

          customService.deleteResources(id)

          assetHelper.deleteAssetEntry(id)
          socialActivityHelper.deleteActivities(id)

          lessons.filterById(id).delete
        })
    }
  }

  override def update(id: Long,
                      title: String,
                      description: String,
                      isVisible: Option[Boolean],
                      beginDate: Option[DateTime],
                      endDate: Option[DateTime],
                      tagIds: Seq[Long],
                      requiredReview: Boolean,
                      scoreLimit: Double): Unit = {
    val lesson = db.withTransaction { implicit s =>
      lessons
        .filterById(id)
        .map(l => (
          l.title,
          l.description,
          l.isVisible,
          l.beginDate,
          l.endDate,
          l.requiredReview,
          l.scoreLimit))
        .update((
          title,
          description,
          isVisible,
          beginDate,
          endDate,
          requiredReview,
          scoreLimit))

      lessons.filterById(id).first
    }

    val assetId = assetHelper.updatePackageAssetEntry(lesson)
    tagService.setTags(assetId, tagIds)
  }

  override def update(lesson: Lesson): Unit = {
    db.withTransaction { implicit s =>
      lessons
        .filterById(lesson.id)
        .map(l => (
          l.title,
          l.description,
          l.isVisible,
          l.beginDate,
          l.endDate,
          l.ownerId,
          l.creationDate,
          l.scoreLimit))
        .update((lesson.title, lesson.description, lesson.isVisible, lesson.beginDate,
          lesson.endDate, lesson.ownerId, lesson.creationDate, lesson.scoreLimit))
    }

    assetHelper.updatePackageAssetEntry(lesson)
  }

  override def updateLessonsInfo(lessonsInfo: Seq[LessonInfo]): Unit = {
    val updatedLessons = db.withTransaction { implicit s =>
      lessonsInfo.map { info =>
        lessons.filterById(info.id)
          .map(l => (l.title, l.description))
          .update((info.title, info.description))

        lessons.filterById(info.id).first
      }
    }

    updatedLessons.foreach(assetHelper.updatePackageAssetEntry)
  }

  override def updateVisibility(lessonId: Long, isVisible: Option[Boolean]): Unit = {
    val lesson = db.withTransaction { implicit s =>
      lessons.filterById(lessonId)
        .map(_.isVisible)
        .update(isVisible)

      lessons.filterById(lessonId).first
    }

    assetHelper.updatePackageAssetEntry(lesson)
  }

  override def getAll(courseId: Long): Seq[Lesson] = {
    db.withSession { implicit s =>
      lessons
        .filterByCourseId(courseId)
        .list
    }
  }

  def getByCourses(courseIds: Seq[Long]): Seq[Lesson] = {
    db.withSession { implicit s =>
      lessons
        .filterByCourseIds(courseIds)
        .list
    }
  }

  override def getAllSorted(courseId: Long,
                            ascending: Boolean = true,
                            skipTake: Option[SkipTake] = None): Seq[Lesson] = {
    db.withSession { implicit s =>
      lessons
        .filterByCourseId(courseId)
        .sortByTitle(ascending)
        .slice(skipTake)
        .list
    }
  }

  override def getSortedByCourses(courseIds: Seq[Long],
                                  ascending: Boolean = true,
                                  skipTake: Option[SkipTake] = None): Seq[Lesson] = {

    db.withSession { implicit s =>
      lessons
        .filterByCourseIds(courseIds)
        .sortByTitle(ascending)
        .slice(skipTake)
        .list
    }
  }

  def getInReview(courseId: Long): Seq[Lesson] = {
    db.withSession { implicit s =>
      lessons
        .filterByCourseId(courseId)
        .filterByInReview
        .list
    }
  }

  def getInReviewByCourses(courseIds: Seq[Long]): Seq[Lesson]= {
    db.withSession { implicit s =>
      lessons
        .filterByCourseIds(courseIds)
        .filterByInReview
        .list
    }
  }

  def getWithLimit(lessonId: Long): (Lesson, Option[LessonLimit]) = {
    db.withSession { implicit s =>
      val lesson = lessons.filterById(lessonId).first
      val limit = lessonLimits.filterByLessonId(lessonId).firstOption

      (lesson, limit)
    }
  }

  def getAllWithLimits(courseId: Long): Seq[(Lesson, Option[LessonLimit])] = {
    val lessonsQ = lessons.filterByCourseId(courseId)

    db.withSession { implicit s =>
      val courseLessons = lessonsQ.list

      val limits = lessonsQ
        .join(lessonLimits).on((l, lim) => l.id === lim.lessonId)
        .map { case (l, lim) => lim }
        .list

      courseLessons.map(l => (l, limits.find(lim => lim.lessonId == l.id)))
    }
  }

  def getAllVisible(courseId: Long): Seq[Lesson] = {
    db.withSession { implicit s =>
      lessons
        .filterByCourseId(courseId)
        .filterVisible(true)
        .list
    }
  }

  override def getAll(criterion: LessonFilter,
                      ascending: Boolean,
                      skipTake: Option[SkipTake]
                     ): RangeResult[LessonFull] = {

    val lessonIdsByTag = criterion.tagId.map(tagService.getItemIds)

    if (lessonIdsByTag.exists(_.isEmpty)) {
      RangeResult(0, Nil)
    } else {
      var query = lessons
        .filterByCourseIds(criterion.courseIds)
        .filterByType(criterion.lessonType)
        .filterByTitle(criterion.title)
        .filterVisible(criterion.onlyVisible)

      for (ids <- lessonIdsByTag) {
        query = query.filterByIds(ids)
      }

      val result = db.withSession { implicit s =>
        //we can't use query.length
        //select count() ... where ID in (...) - falls on hypersonic (liferay version)
        val count = query.map(_.id).run.size

        val resultLessons = query.sortByTitle(ascending).slice(skipTake).list

        RangeResult(count, resultLessons)
      }

      if (result.records.isEmpty) {
        RangeResult(result.total, Nil)
      } else {
        fillLessonInfo(result)
      }
    }
  }

  private def fillLessonInfo(result: RangeResult[Lesson]): RangeResult[LessonFull] = {
    val ids = result.records.map(_.id)

    val tags = ids.map(id => (id, tagService.getByItemId(id))).toMap
    val limits = db.withSession { implicit s =>
      lessonLimits.filterByLessonIds(ids).list.map(limit => (limit.lessonId, limit)).toMap
    }

    val userIds = result.records.map(_.ownerId).distinct
    val users = userService.getUsers(userIds).map(u => new User(u))

    result.map { lesson => LessonFull(
      lesson,
      limits.get(lesson.id),
      tags.getOrElse(lesson.id, Nil),
      users.find(_.id == lesson.ownerId)
    )
    }
  }

  override def getTagsFromCourse(courseId: Long): Seq[ValamisTag] = {
    val lessonIds = db.withSession { implicit s =>
      lessons.filterByCourseId(courseId).selectId.run
    }

    tagService.getByItemIds(lessonIds)
  }

  override def getTagsFromCourses(courseIds: Seq[Long]): Seq[ValamisTag] = {
    val lessonIds = db.withSession { implicit s =>
      lessons.filterByCourseIds(courseIds).selectId.run
    }

    tagService.getByItemIds(lessonIds)
  }
}
