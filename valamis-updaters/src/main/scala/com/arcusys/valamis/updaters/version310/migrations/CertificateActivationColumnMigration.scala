package com.arcusys.valamis.updaters.version310.migrations

import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.{schema => newSchema}
import com.arcusys.valamis.updaters.version310.certificate.{CertificateStateTableComponent, CertificateTableComponent}
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CertificateActivationColumnMigration(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
                                          (implicit dialect: Dialect[_])
    extends CertificateTableComponent
    with CertificateStateTableComponent
    with SlickProfile {

  val log: Log = LogFactoryUtil.getLog(this.getClass)

  class NewTables(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
    extends newSchema.CertificateTableComponent
      with SlickProfile

  def migrateColumns(): Unit = {
    db.withSession { implicit session =>
      val migration = TableMigration(certificates)

      migration.renameColumn(_.isPublished, "IS_ACTIVE").apply()
      migration.addColumns(_.column[Option[DateTime]]("ACTIVATION_DATE")).apply()
    }
  }

  def fillActivationDates(): Unit= {
    import scala.concurrent.ExecutionContext.Implicits.global
    import driver.api._
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

    val newTables = new NewTables(driver, db)

    val action = newTables.certificates.map(_.id).result.flatMap { certIds =>
      DBIO.sequence(
        certIds.map ( id =>
          certificateStates.filter(_.certificateId === id)
            .sorted(_.userJoinedDate)
            .map(_.userJoinedDate)
            .result
            .headOption flatMap (date =>
              newTables.certificates
                .filter(_.id === id)
                .map(_.activationDate)
                .update(date)
              )
        )
      )
    }

    Await.result(db.run(action), Duration.Inf)
  }
}