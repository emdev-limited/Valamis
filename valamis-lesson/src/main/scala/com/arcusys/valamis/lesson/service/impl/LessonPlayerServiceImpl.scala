package com.arcusys.valamis.lesson.service.impl

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.lesson.storage.query.{LessonLimitQueries, LessonPlayerQueries, LessonQueries, LessonViewerQueries}
import com.arcusys.valamis.model.{PeriodTypes, RangeResult, SkipTake}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.user.model.User
import org.joda.time.DateTime
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 11.02.16.
  */
abstract class LessonPlayerServiceImpl(val db: JdbcBackend#DatabaseDef,
                                       val driver: JdbcProfile)
  extends LessonPlayerService
  with LessonTableComponent
  with LessonQueries
  with LessonPlayerQueries
  with LessonViewerQueries
  with LessonLimitQueries
  with SlickProfile {

  import driver.simple._

  def lessonService: LessonService
  def statementReader: LessonStatementReader
  def ratingService: RatingService[Lesson]
  def tagService: TagService[Lesson]
  def lessonResultService: UserLessonResultService
  def teacherGradeService: TeacherLessonGradeService

  override def getAllVisible(courseId: Long, playerId: Long): Seq[Lesson] = {
    val now = DateTime.now

    val courseLessons = lessons.filterByCourseId(courseId)
    val externalLessons = lessons.filterByPlayerId(playerId)

    val lessonsQuery = (courseLessons union externalLessons)
      .filterVisible(true)
      .filterByBeginEndDates(DateTime.now.withTimeAtStartOfDay)

    db.withSession { implicit s =>
      lessonsQuery.list
    }
  }

  override def getForPlayer(courseId: Long,
                            playerId: Long,
                            user: LUser,
                            title: Option[String],
                            tagId: Option[Long],
                            ascending: Boolean,
                            sortBy: LessonSortBy.Value,
                            skipTake: Option[SkipTake],
                            getSuspendedId: (Long, Lesson) => Option[String]): RangeResult[LessonWithPlayerState] = {

    val lessonIds = tagId.map(tagService.getItemIds)

    val lessons = getLessons(courseId, playerId, user, title, tagId, ascending, sortBy, lessonIds)

    if (lessons.isEmpty) {
      RangeResult(0, Seq())
    } else if (user.isDefaultUser) {
      getLessonsForGuest(lessons, skipTake)
    } else {
      getLessonsForStudent(user, lessons, skipTake, getSuspendedId)
    }
  }

  private def getLessonsForGuest(lessons: Seq[Lesson],
                              skipTake: Option[SkipTake]
                             ): RangeResult[LessonWithPlayerState] = {
    val totalCount = lessons.size

    val pageLessons = skipTake.foldLeft(lessons){
      case (items, SkipTake(skip, take)) => items.slice(skip, skip + take)
    }

    val tags = pageLessons.map(l => (l.id, tagService.getByItemId(l.id))).toMap
    val userIds = pageLessons.map(_.ownerId).distinct
    val users = UserLocalServiceHelper().getUsers(userIds).map(u => new User(u))

    val result = pageLessons.map { lesson =>
      val state = None
      val lessonTags = tags.getOrElse(lesson.id, Nil)
      val rating = ratingService.getRating(lesson.id)
      val lessonLimits = None
      val owner = users.find(_.id == lesson.ownerId)
      val attemptsCount = 0

      LessonWithPlayerState(lesson, lessonLimits, lessonTags, owner, rating, state, attemptsCount)
    }

    RangeResult(totalCount, result)
  }

  private def getLessonsForStudent(user: LUser,
                          lessons: Seq[Lesson],
                          skipTake: Option[SkipTake],
                          getSuspendedId: (Long, Lesson) => Option[String]): RangeResult[LessonWithPlayerState] = {
    val limits = getLessonsLimits(lessons.map(_.id))

    val filteredLessons = lessons.toStream.filter { lesson =>
      limits.find(_.lessonId == lesson.id) match {
        case None => true
        case Some(lim) => isLessonAvailable(user, lesson, lim)
      }
    }

    if (filteredLessons.isEmpty) {
      RangeResult(0, Nil)
    } else {
      val totalCount = filteredLessons.size

      val pageLessons = skipTake.foldLeft(filteredLessons){
        case (items, SkipTake(skip, take)) => items.slice(skip, skip + take)
      }

      val tags = pageLessons.map(l => (l.id, tagService.getByItemId(l.id))).toMap
      val userIds = pageLessons.map(_.ownerId).distinct
      val users = UserLocalServiceHelper().getUsers(userIds).map(u => new User(u))

      val result = pageLessons.map { lesson =>
        val lessonResult = lessonResultService.get(lesson, user)
        val teacherGrade = teacherGradeService.get(user.getUserId, lesson.id).flatMap(_.grade)
        val state = lesson.getLessonStatus(lessonResult, teacherGrade)
        val lessonTags = tags.getOrElse(lesson.id, Nil)
        val rating = ratingService.getRating(user.getUserId, lesson.id)
        val owner = users.find(_.id == lesson.ownerId)
        val lessonLimits = limits.find(_.lessonId == lesson.id)

        LessonWithPlayerState(lesson, lessonLimits, lessonTags, owner, rating, state, lessonResult.attemptsCount,
          getSuspendedId(user.getUserId, lesson))
      }

      RangeResult(totalCount, result)
    }
  }

  def isLessonAvailable(user: LUser, lesson: Lesson, limit: LessonLimit): Boolean = {
    val userAttempts = lessonResultService.get(lesson: Lesson, user: LUser)

    val attemptsCount = userAttempts.attemptsCount
    val lastAttemptDate = userAttempts.lastAttemptDate

    // negative limit should be ignored
    val passingLimitSuccess = limit.passingLimit.exists(l => 0 >= l || l > attemptsCount)

    lazy val intervalSuccess = (lastAttemptDate, limit.rerunInterval, limit.rerunIntervalType) match {
      case (None, _, _) => true
      case (_, None, _) => true
      case (_, _, PeriodTypes.UNLIMITED) => true
      case (Some(date), Some(interval), PeriodTypes.DAYS) => date.plusDays(interval).isBeforeNow
      case (Some(date), Some(interval), PeriodTypes.MONTH) => date.plusMonths(interval).isBeforeNow
      case (Some(date), Some(interval), PeriodTypes.WEEKS) => date.plusWeeks(interval).isBeforeNow
      case (Some(date), Some(interval), PeriodTypes.YEAR) => date.plusYears(interval).isBeforeNow
    }

    passingLimitSuccess && intervalSuccess
  }

  def getLessonsLimits(lessonsIds: Seq[Long]): Seq[LessonLimit] = {
    db.withSession { implicit s =>
      lessonLimits
        .filterLimited.filterByLessonIds(lessonsIds).run
    }
  }

  private def getLessons(courseId: Long,
                         playerId: Long,
                         user: LUser,
                         title: Option[String],
                         tagId: Option[Long],
                         ascending: Boolean,
                         sortBy: LessonSortBy.Value,
                         lessonIds: Option[Seq[Long]]): Seq[Lesson] = {

    if (lessonIds.exists(_.isEmpty)) {
      Nil
    }
    else {
      var playerLessonsQ = lessons.filterByCourseId(courseId) union lessons.filterByPlayerId(playerId)

      for (ids <- lessonIds) playerLessonsQ = playerLessonsQ.filterByIds(ids)

      var lessonQ = playerLessonsQ
        .filterVisible(true)
        .filterByBeginEndDates(DateTime.now.withTimeAtStartOfDay)

      val extraVisibleLessonQ = playerLessonsQ.filterExtraVisisble

      lessonQ = lessonQ union extraVisibleLessonQ.filterByViewer(user.getUserId, MemberTypes.User)

      val roleIds = user.getRoleIds
      if (roleIds.nonEmpty) {
        lessonQ = lessonQ union extraVisibleLessonQ.filterByViewers(roleIds, MemberTypes.Role)
      }

      val userGroupIds = user.getUserGroupIds
      if (userGroupIds.nonEmpty) {
        lessonQ = lessonQ union extraVisibleLessonQ.filterByViewers(userGroupIds, MemberTypes.UserGroup)
      }

      val organizationIds = user.getOrganizationIds
      if (organizationIds.nonEmpty) {
        lessonQ = lessonQ union extraVisibleLessonQ.filterByViewers(organizationIds, MemberTypes.Organization)
      }

      lessonQ = lessonQ
        .filterByTitle(title)
        .sort(sortBy, ascending, playerId)

      db.withSession { implicit s => lessonQ.list }
    }
  }

  override def getAll(playerId: Long, courseId: Long): Seq[Lesson] = {
    val courseLessons = lessons.filterByCourseId(courseId)

    val externalLessons = lessons.filterByPlayerId(playerId)

    db.withSession { implicit s =>
      (courseLessons union externalLessons).list
    }
  }

  override def getAvailableToAdd(playerId: Long,
                                 courseId: Long,
                                 sourceCourseIds: Seq[Long],
                                 criterion: LessonFilter,
                                 ascending: Boolean,
                                 skipTake: Option[SkipTake]): RangeResult[Lesson] = {

    val lessonIds = criterion.tagId.map(tagService.getItemIds)

    if (sourceCourseIds.isEmpty || lessonIds.exists(_.isEmpty)) {
      RangeResult(0, Nil)
    } else {
      val attachedLessonIds = playerLessons.filterByPlayerId(playerId).map(_.lessonId)

      val query = (lessonIds match {
        case None => lessons
        case Some(ids) => lessons.filterByIds(lessonIds.get)
      })
        .filterByCourseIds(sourceCourseIds)
        .filterNot(_.id in attachedLessonIds)
        .filterByType(criterion.lessonType)
        .filterByTitle(criterion.title)

      db.withSession { implicit s =>
        val totalCount = query.length.run
        val lessons = query.sortByTitle(ascending).slice(skipTake).list

        RangeResult(totalCount, lessons)
      }
    }
  }

  override def addLessonsToPlayer(playerId: Long, lessonIds: Seq[Long]): Unit = {
    val lastIndex = db.withSession { implicit s =>
      playerLessons.filterByPlayerId(playerId).map(_.index).max.run getOrElse 0
    }

    val startIndex = lastIndex + 1

    val entities = lessonIds
      .zipWithIndex
      .map { case (lessonId, lessonIndex) =>
        PlayerLesson(
          playerId,
          lessonId,
          index = startIndex + lessonIndex
        )
      }

    db.withTransaction { implicit s =>
      playerLessons ++= entities
    }
  }

  def deleteLessonFromPlayer(playerId: Long, lessonId: Long): Unit = {
    db.withSession { implicit s =>
      playerLessons.filterBy(lessonId, playerId).delete
    }
  }

  override def getTagsFromPlayer(playerId: Long, courseId: Long): Seq[ValamisTag] = {
        val courseLessons = lessons.filterByCourseId(courseId)
        val externalLessons = lessons.filterByPlayerId(playerId)

    val lessonIds = db.withSession { implicit s =>
      (courseLessons union externalLessons).map(_.id).list
    }

    tagService.getByItemIds(lessonIds)
  }

  override def updateOrder(playerId: Long, ids: Seq[Long]): Unit = {
    val entities = ids.zipWithIndex.map { case (id, i) => PlayerLesson(playerId, id, i) }

    if (ids.nonEmpty) db.withTransaction { implicit s =>
      playerLessons.filterByLessonIds(ids, playerId).delete
      playerLessons ++= entities
    }
  }

  override def getLessonIfAvailable(lessonId: Long, user: LUser): Option[Lesson] = {
    db.withSession { implicit s =>
      lessons
        .filterById(lessonId)
        .filterByBeginEndDates(DateTime.now.withTimeAtStartOfDay)
        .firstOption
    } flatMap { lesson =>
      val isVisible = isLessonVisible(user, lesson)

      val isPassingLimitCorrect = user.isDefaultUser || {
        db.withSession { implicit s =>
          lessonLimits.filterByLessonId(lessonId).firstOption
        } map { lim =>
          isLessonAvailable(user, lesson, lim)
        } getOrElse {
          true
        }
      }

      if (isVisible && isPassingLimitCorrect) {
        Some(lesson)
      } else {
        None
      }
    }
  }

  private def isLessonVisible(user: LUser, lesson: Lesson): Boolean = {
    lazy val viewers = db.withSession { implicit s =>
      lessonViewers.filterByLessonId(lesson.id).list.toStream
    }

    lazy val isVisibleForUser = viewers
      .filter(_.viewerType == MemberTypes.User)
      .map(_.viewerId)
      .contains(user.getUserId)

    lazy val isVisibleForRole = {
      val validIds = user.getRoleIds
      viewers
        .filter(_.viewerType == MemberTypes.Role)
        .exists(x => validIds.contains(x.viewerId))
    }

    lazy val isVisibleForGroup = {
      val validIds = user.getUserGroupIds
      viewers
        .filter(_.viewerType == MemberTypes.UserGroup)
        .exists(x => validIds.contains(x.viewerId))
    }

    lazy val isVisibleForOrganization = {
      val validIds = user.getOrganizationIds
      viewers
        .filter(_.viewerType == MemberTypes.Organization)
        .exists(x => validIds.contains(x.viewerId))
    }

    lesson.isVisible
      .getOrElse(isVisibleForUser || isVisibleForRole || isVisibleForGroup || isVisibleForOrganization)
  }
}
