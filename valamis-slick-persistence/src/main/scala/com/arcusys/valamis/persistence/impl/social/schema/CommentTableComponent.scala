package com.arcusys.valamis.persistence.impl.social.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.social.model.Comment
import org.joda.time.DateTime
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait CommentTableComponent extends LongKeyTableComponent with TypeMapper { self:SlickProfile =>

  import driver.simple._

  class CommentTable(tag: Tag) extends LongKeyTable[Comment](tag, "COMMENT") {
    def companyId = column[Long]("COMPANY_ID")
    def userId = column[Long]("USER_ID")
    def content = column[String]("CONTENT")
    def activityId = column[Long]("ACTIVITY_ID")
    def creationDate = column[DateTime]("CREATION_DATE")
    def lastUpdateDate = column[Option[DateTime]]("LAST_UPDATE_DATE")

    def * = (companyId, userId, content, activityId, id.?, creationDate, lastUpdateDate) <> (Comment.tupled, Comment.unapply)

    def update = (companyId, userId, content, activityId, creationDate, lastUpdateDate) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

  }

  val comments = TableQuery[CommentTable]
}
