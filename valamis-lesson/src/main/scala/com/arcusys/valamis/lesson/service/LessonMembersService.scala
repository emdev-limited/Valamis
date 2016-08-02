package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.lesson.model.{LessonUser, Lesson, LessonViewer}
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.member.model.{Member, MemberTypes}

/**
  * Created by mminin on 26.02.16.
  */
trait LessonMembersService {
  def addMembers(lessonId: Long, viewerIds: Seq[Long], viewerType: MemberTypes.Value): Unit

  def removeMembers(lessonId: Long, viewerIds: Seq[Long], viewerType: MemberTypes.Value): Unit

  def getMembers(lessonId: Long,
                 viewerType: MemberTypes.Value,
                 nameFilter: Option[String],
                 ascending: Boolean,
                 skipTake: Option[SkipTake]): RangeResult[Member]

  def getUserMembers(lessonId: Long,
                     nameFilter: Option[String],
                     ascending: Boolean,
                     skipTake: Option[SkipTake],
                     organizationId: Option[Long]): RangeResult[LUser]

  def getAvailableMembers(lessonId: Long,
                          viewerType: MemberTypes.Value,
                          nameFilter: Option[String],
                          ascending: Boolean,
                          skipTake: Option[SkipTake]): RangeResult[Member]

  def getAvailableUserMembers(lessonId: Long,
                              nameFilter: Option[String],
                              ascending: Boolean,
                              skipTake: Option[SkipTake],
                              organizationId: Option[Long]): RangeResult[LUser]

  def getLessonMembers(lessonIds: Seq[Long]): Seq[LessonViewer]

  def getLessonsUsers(lessons: Seq[Lesson], allUsers: Seq[LUser]): Seq[LessonUser]
}
