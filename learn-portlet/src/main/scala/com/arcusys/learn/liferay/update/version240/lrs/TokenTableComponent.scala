package com.arcusys.learn.liferay.update.version240.lrs

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait TokenTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  type LrsToken = (String, String, String, DateTime)
  class TokenTable(tag : Tag) extends Table[LrsToken](tag, tblName("TOKEN")) {

    def token = column[String]("TOKEN", O.PrimaryKey, O.DBType("varchar(255)"))
    def authInfo = column[String]("AUTH", O.NotNull, O.DBType("varchar(512)"))
    def authType = column[String]("TYPE", O.NotNull, O.DBType("varchar(512)"))
    def created = column[DateTime]("CREATED")

    def * = (token, authInfo, authType, created)
  }

  val tokens = TableQuery[TokenTable]
}
