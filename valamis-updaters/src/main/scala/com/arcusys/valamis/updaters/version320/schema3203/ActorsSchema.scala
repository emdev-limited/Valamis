package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile}

trait ActorsSchema {
  self: SlickProfile =>

  import driver.api._

  type ActorsEntity = (Long, Option[String], Option[String], Option[String], Option[Long], Option[Long], Option[String])

  class ActorsTable(tag: Tag) extends Table[ActorsEntity](tag, "lrs_actors") {

    def key = column[Long]("key", O.PrimaryKey, O.AutoInc)

    def openId = column[Option[String]]("openId", O.DBType(varCharMax))

    def mBoxSha1Sum = column[Option[String]]("mBoxSha1Sum", O.DBType(varCharMax))

    def mBox = column[Option[String]]("mBox", O.DBType(varCharMax))

    def groupKey = column[Option[Long]]("groupKey")

    def accountKey = column[Option[Long]]("accountKey")

    def name = column[Option[String]]("name")

    def * = (key, openId, mBoxSha1Sum, mBox, groupKey, accountKey, name)

  }

  val actors = TableQuery[ActorsTable]
}
