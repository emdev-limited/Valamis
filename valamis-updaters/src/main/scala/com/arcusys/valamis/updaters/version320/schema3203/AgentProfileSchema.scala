package com.arcusys.valamis.updaters.version320.schema3203

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait AgentProfileSchema {
  self: SlickProfile =>

  import driver.api._

  type AgentProfileRow = (String, Long, String)

  class AgentProfilesTable(tag: Tag) extends Table[AgentProfileRow](tag, "lrs_agentProfiles") {

    def * = (profileId, agentKey, documentKey)

    def profileId   = column[String]("profileId", O.SqlType(varCharMax))
    def agentKey    = column[Long]("agentKey")
    def documentKey = column[String]("documentKey", O.SqlType(uuidKeyLength))

  }

  lazy val agentProfiles = TableQuery[AgentProfilesTable]

}
