package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.ResourceMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema.ResourceTableComponent
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ResourceMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with ResourceTableComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:migrationTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFResource (
          	id_ LONG not null primary key,
          	packageID INTEGER null,
          	scormType TEXT null,
          	resourceID VARCHAR(3000) null,
          	href TEXT null,
          	base TEXT null
          );"""
      ).execute

      resourceTQ.ddl.create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFResource;"""
      ).execute

      resourceTQ.ddl.drop
    }
    connection.close()
  }

  val courseId = 245
  val lessonOwnerId = 354

  test("empty source table") {
    db.withSession { implicit s =>
      val migration = new ResourceMigration(db, driver)
      migration.migrate()

      val size =
        resourceTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate") {
    val id = 1
    val packageId = 2
    val scormType = "type"
    val resourceId = "3"
    val href = "href"
    val base = "base"
    db.withSession { implicit s =>
      addResource(id, packageId, scormType, resourceId, href, base)


      val migration = new ResourceMigration(db, driver)
      migration.migrate()

      val rows = resourceTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(Some(id) == g.id)
      assert(Some(packageId) == g.packageId)
      assert(scormType == g.scormType)
      assert(Some(resourceId) == g.resourceId)
      assert(Some(href) == g.href)
      assert(Some(base) == g.base)
    }
  }

  private def addResource(  id: Int,
                            packageId: Long,
                            scormType: String,
                            resourceId: String,
                            href: String,
                            base: String
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFResource
           (id_, packageId, scormType, resourceId, href, base )
          	values ($id, $packageId, '$scormType', '$resourceId', '$href', '$base');"""
    ).execute
  }

}
