package com.arcusys.valamis.lesson.service.impl


import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.util.CourseUtilHelper
import com.arcusys.valamis.lesson.model.{LessonUser, Lesson, LessonViewer}
import com.arcusys.valamis.member.model.{Member, MemberTypes}
import com.arcusys.valamis.lesson.service.LessonMembersService
import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.lesson.storage.query.{LessonQueries, LessonViewerQueries}
import com.arcusys.valamis.member.service.MemberService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 19.02.16.
  */
abstract class LessonMembersServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LessonMembersService
    with LessonTableComponent
    with LessonViewerQueries
    with LessonQueries
    with SlickProfile {

  import driver.simple._

  def memberService: MemberService

  def removeMembers(lessonId: Long, viewerIds: Seq[Long], viewerType: MemberTypes.Value): Unit = {
    db.withTransaction { implicit s =>
      lessonViewers
        .filterByTypeAndLessonId(viewerType, lessonId)
        .filterByViewerIds(viewerIds)
        .delete
    }
  }

  def addMembers(lessonId: Long, viewerIds: Seq[Long], viewerType: MemberTypes.Value): Unit = {
    val viewers = viewerIds.map(LessonViewer(lessonId, _, viewerType))

    db.withTransaction { implicit s =>
      lessonViewers
        .filterByTypeAndLessonId(viewerType, lessonId)
        .filterByViewerIds(viewerIds)
        .delete

      lessonViewers ++= viewers
    }
  }

  def getMembers(lessonId: Long,
                 viewerType: MemberTypes.Value,
                 nameFilter: Option[String],
                 ascending: Boolean,
                 skipTake: Option[SkipTake]): RangeResult[Member] = {

    val (courseId, viewerIds) = db.withSession { implicit s =>
      val courseId = lessons.filterById(lessonId).selectCourseId.first

      val viewerIds = lessonViewers
        .filterByTypeAndLessonId(viewerType, lessonId)
        .map(_.viewerId)
        .list
      (courseId, viewerIds)
    }

    if (viewerIds.isEmpty) {
      RangeResult(0, Nil)
    } else {
      val companyId = CourseUtilHelper.getCompanyId(courseId)

      memberService.getMembers(viewerIds, true, viewerType, companyId, nameFilter, ascending, skipTake)
    }
  }

  def getUserMembers(lessonId: Long,
                     nameFilter: Option[String],
                     ascending: Boolean,
                     skipTake: Option[SkipTake],
                     organizationId: Option[Long]): RangeResult[LUser] = {

    val (courseId, viewerIds) = db.withSession { implicit s =>
      val courseId = lessons.filterById(lessonId).selectCourseId.first

      val viewerIds = lessonViewers
        .filterByTypeAndLessonId(MemberTypes.User, lessonId)
        .map(_.viewerId)
        .list
      (courseId, viewerIds)
    }

    if (viewerIds.isEmpty) {
      RangeResult(0, Nil)
    } else {
      val companyId = CourseUtilHelper.getCompanyId(courseId)

      memberService.getUserMembers(viewerIds, true, companyId, nameFilter, ascending, skipTake, organizationId)
    }
  }

  def getAvailableMembers(lessonId: Long,
                          viewerType: MemberTypes.Value,
                          nameFilter: Option[String],
                          ascending: Boolean,
                          skipTake: Option[SkipTake]): RangeResult[Member] = {

    val (courseId, viewerIds) = db.withSession { implicit s =>
      val courseId = lessons.filterById(lessonId).selectCourseId.first

      val viewerIds = lessonViewers
        .filterByTypeAndLessonId(viewerType, lessonId)
        .map(_.viewerId)
        .list
      (courseId, viewerIds)
    }

    val companyId = CourseUtilHelper.getCompanyId(courseId)

    memberService.getMembers(viewerIds, false, viewerType, companyId, nameFilter, ascending, skipTake)
  }

  def getAvailableUserMembers(lessonId: Long,
                              nameFilter: Option[String],
                              ascending: Boolean,
                              skipTake: Option[SkipTake],
                              organizationId: Option[Long]): RangeResult[LUser] = {

    val (courseId, viewerIds) = db.withSession { implicit s =>
      val courseId = lessons.filterById(lessonId).selectCourseId.first

      val viewerIds = lessonViewers
        .filterByTypeAndLessonId(MemberTypes.User, lessonId)
        .map(_.viewerId)
        .list
      (courseId, viewerIds)
    }

    val companyId = CourseUtilHelper.getCompanyId(courseId)

    memberService.getUserMembers(viewerIds, false, companyId, nameFilter, ascending, skipTake, organizationId)
  }

  def getLessonMembers(lessonIds: Seq[Long]): Seq[LessonViewer] = {
    db.withSession { implicit s =>
      lessonViewers.filter(_.lessonId inSet lessonIds)
        .list
    }
  }

  def getLessonsUsers(lessons: Seq[Lesson], allUsers: Seq[LUser]): Seq[LessonUser] = {
    if (allUsers.isEmpty) {
      Seq()
    }
    else {
      val lessonIds = lessons.map(_.id)
      lazy val members = getLessonMembers(lessonIds)
      lessons.flatMap { lesson =>
        lesson.isVisible match {
          case Some(true) => allUsers.map(LessonUser(lesson, _))
          case Some(false) => Seq()
          case None =>
            val users = members
              .filter(_.lessonId == lesson.id)
              .flatMap(v => memberService.getMembers(v.viewerId, v.viewerType))
            allUsers.intersect(users).map(LessonUser(lesson, _))
        }
      }
    }
  }
}
