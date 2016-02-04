package com.arcusys.learn.liferay.update.version260

import java.sql.Connection
import com.arcusys.learn.liferay.update.version260.migrations.PackageScopeRuleMigration
import com.arcusys.valamis.lesson.PackageScopeRuleTableComponent
import com.arcusys.valamis.lesson.model.PackageScopeRule
import com.arcusys.valamis.model.ScopeType
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import org.scalatest.{BeforeAndAfter, FunSuite}
import scala.slick.jdbc.StaticQuery
import scala.util.Try

class PackageScopeRuleMigrationTest extends FunSuite with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:packagescoperule", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.createConnection()
  }
  after {
    connection.close()
  }

  val table = new PackageScopeRuleTableComponent {
    override protected val driver = H2Driver
  }

  private val scopeInstance = "instance"
  private val scopeSite = "site"
  private val scopePage = "page"
  private val scopePlayer = "player"

  private def createOldTable(implicit s: scala.slick.jdbc.JdbcBackend#SessionDef) : Unit = {
    StaticQuery.updateNA(
      """
        create table learn_lfpackagescoperule(
        id bigint NOT NULL,
        packageid integer,
        scope character varying(3000),
        scopeid character varying(3000),
        visibility boolean,
        isdefault boolean)"""
    ).execute
  }

  private def insertToOldTable(id: Long,
                               packageId: Long,
                               scope: String,
                               scopeId: Option[Long],
                               visibility: Option[Boolean],
                               isDefault: Option[Boolean])
                              (implicit s: scala.slick.jdbc.JdbcBackend#SessionDef) : Unit = {
    val scopeIdValue = scopeId.map(_.toString).getOrElse("null")
    val visibilityValue = visibility.map(_.toString).getOrElse("null")
    val isDefaultValue = isDefault.map(_.toString).getOrElse("null")
    StaticQuery.updateNA(
      s"""
        insert into learn_lfpackagescoperule
        ( id, packageid, scope, scopeid, visibility, isdefault)
        values ( $id, $packageId, '$scope', $scopeIdValue, $visibilityValue, $isDefaultValue)"""
    ).execute
  }

  test("migrate packageScopeRule") {
    db.withSession { implicit s =>
      createOldTable
      insertToOldTable( 1, 2, scopeInstance, None, None, Some(true))
    }

    new PackageScopeRuleMigration(db, H2Driver).migrate()

    db.withSession { implicit s =>
      val stored = table.packageScopeRule.firstOption getOrElse fail("no packageScopeRule created")

      assert(stored == PackageScopeRule(2, ScopeType.Instance, None, false, true, None, stored.id))
    }
  }

  test("migrate list of packageScopeRule") {
    db.withSession { implicit s =>
      createOldTable
      insertToOldTable( 1, 2, scopeSite, Some(13), None, Some(false))
      insertToOldTable( 3, 4, scopePage, None, Some(true), Some(false))
      insertToOldTable( 5, 6, scopePlayer, None, Some(false), Some(true))
    }

    new PackageScopeRuleMigration(db, H2Driver).migrate()

    db.withSession { implicit s =>
      val newRules = table.packageScopeRule.list

      assert(newRules.size == 3)

      val r1 = newRules.find(_.packageId == 2) getOrElse fail("no packageScopeRule created")
      assert(r1 == PackageScopeRule(2, ScopeType.Site, Some("13"), false, false, None, r1.id))

      val r2 = newRules.find(_.packageId == 4) getOrElse fail("no packageScopeRule created")
      assert(r2 == PackageScopeRule(4, ScopeType.Page, None, true, false, None, r2.id))

      val r3 = newRules.find(_.packageId == 6) getOrElse fail("no packageScopeRule created")
      assert(r3 == PackageScopeRule(6, ScopeType.Player, None, false, true, None, r3.id))
    }
  }

  test("migrate packageScopeRule with incorrect scope") {
    db.withSession { implicit s =>
      createOldTable
      insertToOldTable(3, 3, "wrong scope", None, None, None)
    }

    Try(new PackageScopeRuleMigration(db, H2Driver).migrate())

    db.withSession { implicit s =>
      assert(table.packageScopeRule.length.run == 0)
    }
  }
}
