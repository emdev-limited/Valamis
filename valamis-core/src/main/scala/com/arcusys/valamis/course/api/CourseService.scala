package com.arcusys.valamis.course.api

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.model.{RangeResult, SkipTake}

/**
  * Created by amikhailov on 02.12.16.
  */
trait CourseService {

  def getById(courseId: Long): Option[LGroup]

  def getByUserId(userId: Long): Seq[LGroup]

  def getByUserId(userId: Long,
                  skipTake: Option[SkipTake],
                  sortAsc: Boolean = true): RangeResult[LGroup]

  def getSitesByUserId(userId: Long): Seq[LGroup]

  def getByCompanyId(companyId: Long, skipCheckActive: Boolean = false): Seq[LGroup]
}
