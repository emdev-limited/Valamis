package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.joda.JodaDateTimeMapper
import slick.driver.JdbcDriver


trait StatementSchema {
  self: SlickProfile =>

  import driver.api._

  type StatementRow = (String, Long, String, String, Long, Option[Long], Option[String], DateTime, DateTime, Option[Long], Option[String])

  class StatementsTable (tag: Tag) extends Table [StatementRow] (tag, "lrs_statements") {

    implicit lazy val jodaMapper = new JodaDateTimeMapper(driver.asInstanceOf[JdbcDriver]).typeMapper

    override def * = (
      key         ,
      actorKey    ,
      verbId      ,
      verbDisplay ,
      objectKey   ,
      resultKey   ,
      contextKey  ,
      timestamp   ,
      stored      ,
      authorityKey,
      version
    )

    def key = column[String]("key", O.PrimaryKey, O.SqlType(uuidKeyLength))

    def actorKey    = column [Long]          ("actorId"    )
    def objectKey   = column [Long]("objectKey"  )
    def verbId      = column [String]                 ("verbId"     , O.SqlType(varCharMax)) // TODO: Change type to [[java.net.URI]]
    def verbDisplay = column [String]            ("verbDisplay", O.SqlType(varCharMax))

    def resultKey   = column [Option[Long]]      ("resultId"   )
    def authorityKey= column [Option[Long]]       ("authorityId")
    def contextKey  = column [Option[String]]     ("contextId"  , O.SqlType(uuidKeyLength))
    def version     = column [Option[String]]  ("version"    , O.SqlType(varCharMax))

    def timestamp   = column [DateTime] ("timestamp")
    def stored      = column [DateTime] ("stored"   )

  }

  lazy val statements = TableQuery[StatementsTable]

}
