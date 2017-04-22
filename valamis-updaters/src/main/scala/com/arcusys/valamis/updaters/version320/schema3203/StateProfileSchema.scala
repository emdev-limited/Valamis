package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.DbNameUtils._


trait StateProfileSchema {
  self: SlickProfile =>

  import driver.api._

  type StateProfileRow = (String, Long, Long, Option[String], String)

  class StateProfilesTable(tag: Tag) extends Table[StateProfileRow](tag, "lrs_stateProfiles") {
    override def * = (stateId, agentKey, activityKey, registration, documentKey)

    def agentKey = column[Long]("agentKey")
    def activityKey = column[Long]("activityKey")
    def stateId = column[String]("stateId", O.SqlType(varCharPk))
    def registration = column[Option[String]]("registration", O.SqlType(varCharPk))
    def documentKey = column[String]("documentKey" , O.SqlType(uuidKeyLength))

//    def activity = foreignKey(fkName("stateProfile2activity"), activityKey, TableQuery[ActivitiesTable])(x => x.key, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
//    def document = foreignKey(fkName("stateProfiles2document"), documentKey, TableQuery[DocumentsTable])(x => x.key, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
//    def agent = foreignKey(fkName("stateProfile2agent"), agentKey, TableQuery[ActorsTable])(_.key)

//    def indx = index(idxName("stateProfile"), (agentKey, activityKey, stateId, registration), unique = true)
  }

  lazy val stateProfiles = TableQuery[StateProfilesTable]

}
