package com.arcusys.valamis.persistence.impl.lrs

import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait LrsEndpointTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>
  import driver.simple._

  implicit val authTypeMapper = enumerationMapper(AuthType)

  class LrsEndpointTable(tag: Tag) extends LongKeyTable[LrsEndpoint](tag, "LRS_ENDPOINT") {
    def endpoint = column[String]("END_POINT", O.Length(2000, varying = true))
    def authType = column[AuthType.AuthType]("AUTH_TYPE", O.Length(255, varying = true))
    def key = column[String]("KEY", O.Length(2000, varying = true))
    def secret = column[String]("SECRET", O.Length(2000, varying = true))
    def customHost = column[Option[String]]("CUSTOM_HOST")
    def companyId = column[Long]("COMPANY_ID")

    def * = (endpoint, authType, key, secret, customHost, companyId, id.?)
      .<>[LrsEndpoint, (String, AuthType.AuthType, String, String, Option[String], Long, Option[Long])](
      {
        case (endpoint, authType, key, secret, customHost, _, id) =>
          LrsEndpoint(endpoint, authType, key, secret, customHost, id)
      }, {
        case LrsEndpoint(endpoint, authType, key, secret, customHost, id) =>
          Option((endpoint, authType, key, secret, customHost, 0L, id))
      }
    )

    def update = (endpoint, authType, key, secret, customHost) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

  }
  val lrsEndpoint = TableQuery[LrsEndpointTable]
}



