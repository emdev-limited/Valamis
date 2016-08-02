package com.arcusys.learn.liferay.update.version240.social

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.common.joda.JodaDateTimeMapper
import org.joda.time.DateTime

import scala.slick.driver.{JdbcDriver, JdbcProfile}

trait LikeTableComponent extends TypeMapper { self: SlickProfile =>
  import driver.simple._

  type Like = (Long, Long, Long, Option[Long], DateTime)
  class LikeTable(tag: Tag) extends Table[Like](tag, tblName("LIKE")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def companyId = column[Long]("COMPANY_ID")
    def userId = column[Long]("USER_ID")
    def creationDate = column[DateTime]("CREATION_DATE")
    def activityId = column[Long]("ACTIVITY_ID")

    def userActivityIndex = index(idxName("LIKE_UID_AID"), (userId, activityId), unique = true)
    def * = (companyId, userId, activityId, id.?, creationDate)
  }

  val likes = TableQuery[LikeTable]
}