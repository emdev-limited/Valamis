package com.arcusys.valamis.updaters.version330

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.version330.schema3301._
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

class DBUpdater3301Test
  extends FunSuite
    with TableComponent
    with OldTableComponent
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase
    with DatabaseLayer {

  import driver.api._

  val updater = new DBUpdater3301 {
    override lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
      override def slickDriver: JdbcDriver = DBUpdater3301Test.this.driver

      override def slickProfile: JdbcProfile = slickDriver

      override def databaseDef: JdbcBackend#DatabaseDef = DBUpdater3301Test.this.db
    }
    override lazy val driver = DBUpdater3301Test.this.driver

    override def getCompanyId: Long = 1L
  }

  before {
    createDB()
    execSync(
      DBIO.seq(oldSettings.schema.create,
        oldLrsEndpoint.schema.create)
    )
  }
  after {
    dropDB()
  }

  test("check updater for settings table") {

    execSync(oldSettings += OldSetting("Setting1", "key"))
    execSync(oldSettings += OldSetting("Setting2", "key2"))

    updater.doUpgrade()

    val entries = execSync(settings.sortBy(_.id).result)
    assert(entries.size == 2)
    assert(entries.head.key == "Setting1")
    assert(entries.head.value == "key")

    val companyIdSome = execSync(settings.filter(_.dataKey === "Setting2").map(_.companyId).result)
    assert(companyIdSome.head.contains(1L))

    execSync(settings.map(s => (s.dataKey, s.dataValue, s.companyId)) += ("Setting3", "val", Some(2L)))
    execSync(settings.map(s => (s.dataKey, s.dataValue, s.companyId)) += ("Setting4", "val", Some(1L)))
    assert(execSync(settings.length.result) == 4)

  }

  test("check updater for lrs endpoint settings table") {

    execSync(oldLrsEndpoint += LrsEndpoint("endpoint", AuthType.Internal, "key", "secret"))

    updater.doUpgrade()

    val entries = execSync(lrsEndpoint.result)
    assert(entries.size == 1)
    assert(entries.head.endpoint == "endpoint")
    assert(entries.head.key == "key")
    assert(entries.head.secret == "secret")
    assert(entries.head.auth == AuthType.Internal)

    val companyIds = execSync(lrsEndpoint.map(_.companyId).result)
    assert(companyIds.head == 1L)

  }
}