package com.arcusys.valamis.lrs

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import scala.slick.driver.JdbcProfile

trait LrsEndpointTableComponent {
  protected val driver: JdbcProfile
  import driver.simple._

  lazy val authTypeMapper = MappedColumnType.base[AuthType.AuthType, String](
    s => s.toString,
    s => AuthType.withName(s)
  )

  class LrsEndpointTable(tag: Tag) extends Table[LrsEndpoint](tag, tblName("LRS_ENDPOINT")) {
    implicit val mapper = authTypeMapper
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def endpoint = column[String]("END_POINT", O.Length(2000, varying = true))
    def authType = column[AuthType.AuthType]("AUTH_TYPE", O.Length(2000, varying = true))
    def key = column[String]("KEY", O.Length(2000, varying = true))
    def secret = column[String]("SECRET", O.Length(2000, varying = true))
    def customHost = column[Option[String]]("CUSTOM_HOST")

    def * = (endpoint, authType, key, secret, customHost, id.?) <>(LrsEndpoint.tupled, LrsEndpoint.unapply)
  }
  val lrsEndpoint = TableQuery[LrsEndpointTable]
}



