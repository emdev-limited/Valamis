package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile

/**
  * Created by mminin on 21.01.16.
  */
trait LessonViewerQueries {
  self: SlickProfile with LessonTableComponent =>

  import driver.simple._

  private type LessonViewerQuery = Query[LessonViewerTable, LessonViewerTable#TableElementType, Seq]

  implicit class LessonViewerExtensions(q: LessonViewerQuery) {
    def filterByLessonId(lessonId: Long): LessonViewerQuery = {
      q.filter(_.lessonId === lessonId)
    }

    def filterByTypeAndLessonId(viewerType: MemberTypes.Value, lessonId: Long): LessonViewerQuery = {
      q.filter(v => v.lessonId === lessonId && v.viewerType === viewerType)
    }

    def filterByViewer(viewerId: Long, viewerType: MemberTypes.Value): LessonViewerQuery = {
      q.filter(v => v.viewerId === viewerId && v.viewerType === viewerType)
    }

    def filterByViewers(viewerIds: Seq[Long], viewerType: MemberTypes.Value): LessonViewerQuery = {
      q.filter(v => (v.viewerId inSet viewerIds) && v.viewerType === viewerType)
    }

    def filterByViewerIds(viewerIds: Seq[Long]): LessonViewerQuery = {
      q.filter(_.viewerId inSet viewerIds)
    }
  }
}
