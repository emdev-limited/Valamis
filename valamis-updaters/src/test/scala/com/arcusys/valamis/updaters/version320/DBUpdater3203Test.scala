package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.version320.schema3203._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater3203Test
  extends FunSuite
    with BeforeAndAfter
    with ActorsSchema
    with AccountsSchema
    with AgentProfileSchema
    with ContextSchema
    with StatementObjectSchema
    with StatementSchema
    with StateProfileSchema
    with SubStatementSchema
    with SlickProfile
    with SlickDbTestBase
    with DatabaseLayer {

  import driver.api._

  before {
    createDB()
    Await.result(db.run((statementObjects.schema ++ statements.schema ++ stateProfiles.schema
      ++ agentProfiles.schema ++ subStatements.schema ++ contexts.schema
      ++ actors.schema ++ accounts.schema).create), Duration.Inf)
  }
  after {
    dropDB()
  }

  val companyId = 234
  val user1Id = 324
  val user2Id = 54
  val driver2 = driver
  val db2 = db
  val updater = new DBUpdater3203 {
    override lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
      override def slickDriver: JdbcDriver = driver2

      override def slickProfile: JdbcProfile = slickDriver

      override def databaseDef: JdbcBackend#DatabaseDef = db2
    }
    override lazy val driver = driver2

  }

  test("test") {
    val accId = execSync(accounts.returning(accounts.map(_.key)) += (0L, Some("home"), Some("sdfsdf")))
    val actorId = execSync(actors.returning(actors.map(_.key)) += (0L, None, None, None, None, Option(accId), Option("dfgdfg")))
    val actor2Id = execSync(actors.returning(actors.map(_.key)) += (0L, None, None, None, None, Option(accId), Option("dfgdfg2")))
    val actor3Id = execSync(actors.returning(actors.map(_.key)) += (0L, None, None, None, None, Option(accId), Option("dfgdfg3")))

    execSync(stateProfiles += ("123", actorId, 12L, Option("sdsd"), "sffsf"))
    execSync(stateProfiles += ("1234", actor2Id, 12L, Option("sdsd2"), "sffsf2"))

    execSync(agentProfiles += ("123", actorId, "sffsf"))
    execSync(agentProfiles += ("1234", actor2Id, "sffsf2"))
    execSync(agentProfiles += ("1234", actor3Id, "sffsf2"))

    execSync(statementObjects += (actorId, "agent"))
    execSync(statementObjects += (actor2Id, "agent"))
    execSync(statementObjects += (actor3Id, "agent"))

    execSync(statements += ("1", actorId, "Sdfsd", "Sdfsdf", actorId, None, None, DateTime.now, DateTime.now, None, None))
    execSync(statements += ("2", actor2Id, "Sdfsd", "Sdfsdf", actor2Id, None, None, DateTime.now, DateTime.now, None, None))
    execSync(statements += ("3", actor3Id, "Sdfsd", "Sdfsdf", actor3Id, None, None, DateTime.now, DateTime.now, None, None))

    execSync(contexts += (Some("1"), Some(actorId), Some(actorId), None, None, None, None, None))
    execSync(contexts += (Some("2"), Some(actor2Id), Some(actor2Id), None, None, None, None, None))
    execSync(contexts += (Some("3"), Some(actor3Id), Some(actor3Id), None, None, None, None, None))

    execSync(subStatements += (1L, actorId, actorId, "Asf", "Asd"))
    execSync(subStatements += (2L, actor2Id, actor2Id, "Asf", "Asd"))
    execSync(subStatements += (3L, actor3Id, actor3Id, "Asf", "Asd"))

    updater.clearDuplicatedActors

    assert(1 == execSync(actors.filter(_.accountKey === accId).length.result))

    assert(1 == execSync(statementObjects.filter(_.key === actorId).length.result))
    assert(0 == execSync(statementObjects.filter(_.key === actor2Id).length.result))
    assert(0 == execSync(statementObjects.filter(_.key === actor3Id).length.result))

    assert(2 == execSync(stateProfiles.filter(_.agentKey === actorId).length.result))
    assert(0 == execSync(stateProfiles.filter(_.agentKey === actor2Id).length.result))
    assert(0 == execSync(stateProfiles.filter(_.agentKey === actor3Id).length.result))

    assert(3 == execSync(agentProfiles.filter(_.agentKey === actorId).length.result))
    assert(0 == execSync(agentProfiles.filter(_.agentKey === actor2Id).length.result))
    assert(0 == execSync(agentProfiles.filter(_.agentKey === actor3Id).length.result))

    assert(3 == execSync(statements.filter(_.actorKey === actorId).length.result))
    assert(0 == execSync(statements.filter(_.actorKey === actor2Id).length.result))
    assert(0 == execSync(statements.filter(_.actorKey === actor3Id).length.result))

    assert(3 == execSync(statements.filter(_.objectKey === actorId).length.result))
    assert(0 == execSync(statements.filter(_.objectKey === actor2Id).length.result))
    assert(0 == execSync(statements.filter(_.objectKey === actor3Id).length.result))

    assert(3 == execSync(subStatements.filter(_.actorKey === actorId).length.result))
    assert(0 == execSync(subStatements.filter(_.actorKey === actor2Id).length.result))
    assert(0 == execSync(subStatements.filter(_.actorKey === actor3Id).length.result))

    assert(3 == execSync(subStatements.filter(_.statementObjectKey === actorId).length.result))
    assert(0 == execSync(subStatements.filter(_.statementObjectKey === actor2Id).length.result))
    assert(0 == execSync(subStatements.filter(_.statementObjectKey === actor3Id).length.result))

    assert(3 == execSync(contexts.filter(_.instructor === actorId).length.result))
    assert(0 == execSync(contexts.filter(_.instructor === actor2Id).length.result))
    assert(0 == execSync(contexts.filter(_.instructor === actor3Id).length.result))

    assert(3 == execSync(contexts.filter(_.team === actorId).length.result))
    assert(0 == execSync(contexts.filter(_.team === actor2Id).length.result))
    assert(0 == execSync(contexts.filter(_.team === actor3Id).length.result))
  }

}

