package com.arcusys.valamis.updaters.version330.schema3301

import com.arcusys.valamis.persistence.common.DbNameUtils.{idName, pkName}
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}

trait OldTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>
  import driver.simple._

  implicit val authTypeMapper1 = enumerationMapper(AuthType)

  class OldLrsEndpointTable(tag: Tag) extends Table[LrsEndpoint](tag, "LEARN_LRS_ENDPOINT") {
    def id = column[Long](idName, O.PrimaryKey, O.AutoInc)

    def endpoint = column[String]("END_POINT", O.Length(2000, varying = true))
    def authType = column[AuthType.AuthType]("AUTH_TYPE", O.Length(255, varying = true))
    def key = column[String]("KEY", O.Length(2000, varying = true))
    def secret = column[String]("SECRET", O.Length(2000, varying = true))
    def customHost = column[Option[String]]("CUSTOM_HOST")

    def * = (endpoint, authType, key, secret, customHost, id.?) <> (LrsEndpoint.tupled, LrsEndpoint.unapply)

  }
  val oldLrsEndpoint = TableQuery[OldLrsEndpointTable]
}



