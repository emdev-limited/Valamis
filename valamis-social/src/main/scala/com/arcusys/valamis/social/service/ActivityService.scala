package com.arcusys.valamis.social.service

import com.arcusys.learn.liferay.model.Activity
import com.arcusys.valamis.model.SkipTake

trait ActivityService {
  def create(companyId: Long, userId: Long, content: String): Activity
  def share(companyId: Long, userId: Long, packageId: Long, comment: Option[String]): Option[Activity]
  def getBy(companyId: Long, userId: Option[Long], skipTake: Option[SkipTake], showAll: Boolean): Seq[Activity]
  def getById(activityId: Long): Activity
  def delete(activityId: Long): Unit
}