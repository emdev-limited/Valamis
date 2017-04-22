package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.lesson.model.LessonSortBy
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

/**
  * Created by mminin on 21.01.16.
  */
trait LessonQueries {
  self: SlickProfile with LessonTableComponent =>

  import driver.simple._

  type LessonQuery = Query[LessonTable, LessonTable#TableElementType, Seq]

  implicit class LessonExtensions(q: LessonQuery) {
    def filterById(id: Long): LessonQuery = {
      q.filter(_.id === id)
    }

    def filterByIds(ids: Seq[Long]): LessonQuery = {
      q.filter(_.id inSet ids)
    }

    def filterByCourseId(courseId: Long): LessonQuery = {
      q.filter(_.courseId === courseId)
    }

    def filterByCourseIds(courseIds: Seq[Long]): LessonQuery = {
      q.filter(_.courseId inSet courseIds)
    }

    def filterByType(lessonType: Option[LessonType]): LessonQuery = {
      lessonType match {
        case Some(t) => q.filter(_.lessonType === t)
        case None => q
      }
    }

    def filterByTitle(titlePattern: Option[String]): LessonQuery = {
      titlePattern match {
        case Some(t) =>
          val pattern = "%" + t.toLowerCase + "%"
          q.filter(_.title.toLowerCase like pattern)
        case None => q
      }
    }

    def filterByPlayerId(playerId: Long): LessonQuery = {
      playerLessons
        .filter(_.playerId === playerId)
        .join(q).on((p, l) => p.lessonId === l.id)
        .map(_._2)
    }

    def filterVisible(onlyVisible: Boolean): LessonQuery = {
      if (!onlyVisible) q else q filter (_.isVisible === true)
    }

    def filterExtraVisible: LessonQuery = {
      q filter (_.isVisible isEmpty)
    }

    def filterByViewer(viewerId: Long,
                       viewerType: MemberTypes.Value): LessonQuery = {
      val viewersQ = lessonViewers
        .filter(v => v.viewerId === viewerId && v.viewerType === viewerType)

      q.join(viewersQ).on((l,r) => r.lessonId === l.id)
        .map(_._1)
    }

    def filterByViewers(viewerIds: Seq[Long],
                        viewerType: MemberTypes.Value): LessonQuery = {
      val viewersQ = lessonViewers
        .filter(v => (v.viewerId inSet viewerIds) && v.viewerType === viewerType)

      q.join(viewersQ).on((l, r) => r.lessonId === l.id)
        .map(_._1)
    }

    def filterByBeginEndDates(now: DateTime): LessonQuery = {
      q.filter(l => l.beginDate.isEmpty || l.beginDate <= now)
        .filter(l => l.endDate.isEmpty || l.endDate >= now)
    }

    def filterPlayerVisible(playerId: Long): LessonQuery = {
      val invisIds = invisibleLessonViewers.filter(_.playerId === playerId).map(_.lessonId)
      q.filterNot(l => l.id.in(invisIds))
    }
    def sortByDate(ascending: Boolean): LessonQuery = {
      if (ascending) {
        q.sortBy(_.creationDate)
      }
      else {
        q.sortBy(_.creationDate.desc)
      }
    }

    def sortByTitle(ascending: Boolean): LessonQuery = {
      if (ascending) {
        q.sortBy(x => x.title)
      }
      else {
        q.sortBy(_.title.desc)
      }
    }

    def sort(sortBy: LessonSortBy.Value, ascending: Boolean, playerId: Long): LessonQuery = {
      sortBy match {
        case LessonSortBy.Name => q.sortByTitle(ascending)
        case LessonSortBy.Date => q.sortByDate(ascending)
        case LessonSortBy.Default =>
          val playerSettings = playerLessons.filter(_.playerId === playerId)
          q.leftJoin(playerSettings).on((l,p) => l.id === p.lessonId)
            .sortBy{case (l,p) => p.index}
            .map{case (l,p) => l}
      }
    }

    def slice(skipTake: Option[SkipTake]): LessonQuery = {
      skipTake match {
        case Some(SkipTake(skip, take)) => q.drop(skip).take(take)
        case None => q
      }
    }

    def selectId: Query[Column[Long], Long, Seq] = {
      q.map(_.id)
    }

    def selectType: Query[Column[LessonType], LessonType, Seq] = {
      q.map(_.lessonType)
    }

    def selectCourseId: Query[Column[Long], Long, Seq] = {
      q.map(_.courseId)
    }

    def filterByInReview: LessonQuery = {
      q.filter(_.requiredReview)
    }
  }

}
