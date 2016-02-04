package com.arcusys.learn.liferay.update.version260.lrs

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.core.{LongKeyTableComponent, SlickProfile}

trait ActorsSchema extends LongKeyTableComponent {
  self: SlickProfile =>

  import driver.simple._

  type ActorsEntity = (Option[String], Option[String], Option[String], Option[Long], Option[Long], Option[String])

  class ActorsTable(tag: Tag) extends Table[ActorsEntity](tag, "lrs_actors") {

    def key = column[Long]("key", O.PrimaryKey, O.AutoInc)

    def openId = column[Option[String]]("openId", O.DBType(varCharMax))

    def mBoxSha1Sum = column[Option[String]]("mBoxSha1Sum", O.DBType(varCharMax))

    def mBox = column[Option[String]]("mBox", O.DBType(varCharMax))

    def groupKey = column[Option[Long]]("groupKey")

    def accountKey = column[Option[Long]]("accountKey")

    def name = column[Option[String]]("name")

    def * = (openId, mBoxSha1Sum, mBox, groupKey, accountKey, name)

  }

  val actors = TableQuery[ActorsTable]
}
