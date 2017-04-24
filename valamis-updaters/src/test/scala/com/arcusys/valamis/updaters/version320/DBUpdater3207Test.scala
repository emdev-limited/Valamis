package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.version320.schema3207.{CertificateTableComponent, CourseCertificateTableComponent}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc.JdbcBackend

import scala.util.{Failure, Success, Try}

class DBUpdater3207Test
  extends FunSuite
    with CertificateTableComponent
    with CourseCertificateTableComponent
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase
    with DatabaseLayer {

  import driver.api._

  val updater = new DBUpdater3207 {
    override lazy val dbInfo: SlickDBInfo = new SlickDBInfo {
      override def slickDriver: JdbcDriver = DBUpdater3207Test.this.driver

      override def slickProfile: JdbcProfile = slickDriver

      override def databaseDef: JdbcBackend#DatabaseDef = DBUpdater3207Test.this.db
    }
    override lazy val driver = DBUpdater3207Test.this.driver
  }

  before {
    createDB()
    execSync(certificates.schema.create)
    updater.doUpgrade()
  }
  after {
    dropDB()
  }

  test("add row to table") {
    val cert = Certificate(1, "certificate 1", "description", companyId = 999, createdAt = DateTime.now)
    execSync(certificates += cert)

    val row = CourseCertificate(123, 1, DateTime.now)
    execSync(courseCertificates += (row))

    assert(execSync(courseCertificates.length.result) == 1)
  }

  test("add rows with same courseId and certificateId") {
    val cert = Certificate(1, "certificate 1", "description", companyId = 999, createdAt = DateTime.now)
    execSync(certificates += cert)

    val row = CourseCertificate(123, 1, DateTime.now)
    execSync(courseCertificates += (row))

    Try {
      execSync(courseCertificates += (row))
    } match {
      case Failure(e: Exception) =>
        assert(execSync(courseCertificates.length.result) == 1)
      case Success(_) =>
        fail("exception expected")
    }
  }
}