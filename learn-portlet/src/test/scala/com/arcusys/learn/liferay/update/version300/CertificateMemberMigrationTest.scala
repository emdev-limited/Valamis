package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version240.certificate.CertificateTableComponent
import com.arcusys.learn.liferay.update.version300.certificate.{CertificateMemberTableComponent, CertificateStateTableComponent}
import com.arcusys.learn.liferay.update.version300.migrations.CertificateMemberMigration
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class CertificateMemberMigrationTest (val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with SlickProfile
    with CertificateMemberTableComponent {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:certificatemember", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    certificateTable.createSchema()
    table.createSchema()
  }
  after {
    connection.close()
  }

  val certificateTable = new CertificateTableComponent with SlickProfile {
    val driver: JdbcProfile = H2Driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificates.ddl.create
    }
  }

  val table = new CertificateStateTableComponent with SlickProfile {
    val driver: JdbcProfile = H2Driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificateStates.ddl.create
    }
  }


  test("migrate members from certificateState") {

    val certificateId = db.withSession { implicit s =>
      certificateTable.certificates returning certificateTable.certificates.map(_.id) +=
        (1L, "title", "description", "logo", false, false, "short", 21L, PeriodTypes.DAYS, 1, new DateTime(), false, None)
    }

    val migrator = new CertificateMemberMigration(db, driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List(
        new CertificateState(13L, CertificateStatuses.InProgress, new DateTime, new DateTime, certificateId)
      )
    }

    migrator.migrate()

    val data = db.withSession { implicit s => certificateMembers.list }

    assert(data.head.certificateId == certificateId)
    assert(data.head.memberId == 13L)
    assert(data.head.memberType == MemberTypes.User)
  }

  test("migrate members from empty table"){
    val certificateId = db.withSession { implicit s =>
      certificateTable.certificates returning certificateTable.certificates.map(_.id) +=
        (1L, "title", "description", "logo", false, false, "short", 21L, PeriodTypes.DAYS, 1, new DateTime(), false, None)
    }

    val migrator = new CertificateMemberMigration(db, driver) {
      override def getOldData(implicit session: JdbcBackend#Session) = List()
    }

    migrator.migrate()

    val data = db.withSession { implicit s => certificateMembers.list }

    assert(data.size == 0)
  }
}
