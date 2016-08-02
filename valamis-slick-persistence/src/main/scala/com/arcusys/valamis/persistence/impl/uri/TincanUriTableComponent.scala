package com.arcusys.valamis.persistence.impl.uri

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.uri.model.TincanURI
import com.arcusys.valamis.uri.model.TincanURIType

trait TincanUriTableComponent extends TypeMapper {self: SlickProfile =>
  import driver.simple._

  implicit val tincanUriTypeMapper = enumerationMapper(TincanURIType)

  class TincanUriTable(tag: Tag) extends Table[TincanURI](tag, tblName("TINCAN_URI")) {
    def uri = column[String]("URI", O.PrimaryKey, O.Length(200, true))
    def objectId = column[String]("OBJECT_ID", O.Length(500, true))
    def objectType = column[TincanURIType.TincanURIType]("OBJECT_TYPE", O.Length(500, true))
    def content = column[String]("CONTENT", O.Length(2000, true))

    def * = (uri, objectId, objectType, content) <>(TincanURI.tupled, TincanURI.unapply)
  }

  val tincanUris = TableQuery[TincanUriTable]
}
