package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.DbNameUtils._

trait ContextSchema  {
  self: SlickProfile =>

  import driver.api._

  type ContextRow = (Option[String],
    Option[Long],
    Option[Long],
    Option[String],
    Option[String],
    Option[String],
    Option[Long],
    Option[String])


  class ContextsTable(tag: Tag) extends Table[ContextRow](tag: Tag, "lrs_contexts") {
    override def * = (key.?, instructor, team, revision, platform, language, statementRefId, extensions)

    def key = column[String]("key", O.PrimaryKey, O.SqlType(uuidKeyLength))
    def instructor = column[Option[Long]]("instructor")
    def team = column[Option[Long]]("team")
    def revision = column[Option[String]]("revision", O.SqlType(varCharMax))
    def platform = column[Option[String]]("platform", O.SqlType(varCharMax))
    def language = column[Option[String]]("language", O.SqlType(varCharMax))
    def statementRefId = column[Option[Long]]("statementRefId")
    def extensions = column[Option[String]]("extensions", O.SqlType(varCharMax))

//    def statementRef = foreignKey(fkName("cntxt2stmntRef"), statementRefId, TableQuery[StatementReferenceTable])(x => x.key)

//    def instructorIndx = index("idx_instructor", instructor)
//    def teamIndx = index("idx_team", team)
  }

  lazy val contexts = TableQuery[ContextsTable]

}
