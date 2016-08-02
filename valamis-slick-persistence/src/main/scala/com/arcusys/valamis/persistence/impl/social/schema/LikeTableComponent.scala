package com.arcusys.valamis.persistence.impl.social.schema

import com.arcusys.valamis.social.model.Like
import org.joda.time.DateTime
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait LikeTableComponent extends LongKeyTableComponent with TypeMapper {self: SlickProfile =>
  import driver.simple._

  class LikeTable(tag: Tag) extends LongKeyTable[Like](tag, "LIKE") {
    def companyId = column[Long]("COMPANY_ID")
    def userId = column[Long]("USER_ID")
    def creationDate = column[DateTime]("CREATION_DATE")
    def activityId = column[Long]("ACTIVITY_ID")

    def userActivityIndex = index(idxName("LIKE_UID_AID"), (userId, activityId), unique = true)
    def * = (companyId, userId, activityId, id.?, creationDate) <> (Like.tupled, Like.unapply)

    def update = (companyId, userId, activityId, creationDate) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

  }

  val likes = TableQuery[LikeTable]
}