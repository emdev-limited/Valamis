package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile

/**
  * Created by mminin on 21.01.16.
  */
trait LessonPlayerQueries {
  self: SlickProfile with LessonTableComponent =>

  import driver.simple._

  private type PlayerLessonQuery = Query[PlayerLessonTable, PlayerLessonTable#TableElementType, Seq]

  implicit class PlayerLessonExtensions(q: PlayerLessonQuery) {
    def filterBy(lessonId: Long): PlayerLessonQuery = {
      q.filter(_.lessonId === lessonId)
    }

    def filterBy(lessonId: Long, playerId: Long): PlayerLessonQuery = {
      q.filter(p => p.lessonId === lessonId && p.playerId === playerId)
    }

    def filterByLessonIds(lessonIds: Seq[Long], playerId: Long): PlayerLessonQuery = {
      q.filter(p => (p.lessonId inSet lessonIds) && p.playerId === playerId)
    }

    def filterByPlayerId(playerId: Long): PlayerLessonQuery = {
      q.filter(_.playerId === playerId)
    }
  }

}
