package com.arcusys.valamis.updaters.version330

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.schema.{ContentProviderTableComponent => OldContentProviderTable}
import com.arcusys.valamis.updaters.version330.scheme3302.{ContentProviderTableComponent => NewContentProviderTable}
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

import scala.util.{Failure, Success, Try}

class DBUpdater3302Test
    extends FunSuite
    with OldContentProviderTable
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase
    with DatabaseLayer {

  import driver.api._

  val updater = new DBUpdater3302 {
    override lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
      override def slickDriver: JdbcDriver = DBUpdater3302Test.this.driver

      override def slickProfile: JdbcProfile = slickDriver

      override def databaseDef: JdbcBackend#DatabaseDef = DBUpdater3302Test.this.db

    }
    override lazy val driver = DBUpdater3302Test.this.driver

    override def getCompanyId = 0L

  }

  val newTable = new NewContentProviderTable
    with SlickProfile {
    override val driver: JdbcProfile = slickDriver
  }

  before {
    createDB()
    execSync(contentProviders.schema.create)
  }
  after {
    dropDB()
  }

  test("check updater for LEARN_CONTENT_PROVIDERS") {

    execSync(contentProviders += ContentProvider(1,
      "title",
      "description",
      "",
      "http://localhost",
      20,
      10,
      false,
      "customerKey",
      "customerSecret"))


    updater.doUpgrade()

    execSync(newTable.contentProviders.map(_.name).update("title with more than 20 chars"))
    val entries = execSync(newTable.contentProviders.result).headOption
    assert(!entries.map(_.isSelective).get)
    assert(entries.map(_.companyId).get == 0L)
    assert(entries.map(_.name).get == "title with more than 20 chars")
  }
}