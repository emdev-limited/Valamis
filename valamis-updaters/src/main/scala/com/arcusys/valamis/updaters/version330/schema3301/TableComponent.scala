package com.arcusys.valamis.updaters.version330.schema3301

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}

trait TableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>
  import driver.simple._

  implicit val authTypeMapper = enumerationMapper(AuthType)

  class LrsEndpointTable(tag: Tag) extends Table[LrsEndpoint](tag, "LEARN_LRS_ENDPOINT") {
    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)

    def endpoint = column[String]("END_POINT", O.Length(2000, varying = true))
    def authType = column[AuthType.AuthType]("AUTH_TYPE", O.Length(255, varying = true))
    def key = column[String]("KEY", O.Length(2000, varying = true))
    def secret = column[String]("SECRET", O.Length(2000, varying = true))
    def customHost = column[Option[String]]("CUSTOM_HOST")
    def companyId = column[Long]("COMPANY_ID")
    def companyIdOpt = column[Option[Long]]("COMPANY_ID")

    def * = (endpoint, authType, key, secret, customHost, id.?) <> (LrsEndpoint.tupled, LrsEndpoint.unapply)

  }
  class SettingsTable(tag : Tag) extends Table[Setting](tag, "LEARN_SETTINGS") {

    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)

    def dataKey = column[String]("DATAKEY", O.Length(128, varying = true))
    def dataValue = column[String]("DATAVALUE", O.Length(2048, varying = true))
    def companyId = column[Option[Long]]("COMPANY_ID")

    def * = (id, dataKey, dataValue, companyId) <> (Setting.tupled, Setting.unapply)
  }

  class OldSettingsTable(tag : Tag) extends Table[OldSetting](tag, "LEARN_SETTINGS") {

    def dataKey = column[String]("DATAKEY", O.PrimaryKey, O.Length(128, true))
    def dataValue = column[String]("DATAVALUE", O.Length(2048, true))

    def * = (dataKey, dataValue) <> (OldSetting.tupled, OldSetting.unapply)
  }

  val oldSettings = TableQuery[OldSettingsTable]
  val settings = TableQuery[SettingsTable]
  val lrsEndpoint = TableQuery[LrsEndpointTable]
}



