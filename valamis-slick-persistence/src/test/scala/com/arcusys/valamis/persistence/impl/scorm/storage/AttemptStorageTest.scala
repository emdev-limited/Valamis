package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._


/**
* Created by eboystova on 10.05.16.
*/
class AttemptStorageTest extends FunSuite
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile
  with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:AttemptStorageTest", driver = "org.h2.Driver")
  override val driver = H2Driver
  val storages = new StorageFactory(db, driver)

  val attemptStorage = storages.getAttemptStorage
  val scormUserStorage = storages.getScormUserStorage

  var connection: Connection = _

  // db data will be released after connection close
  before {
    connection = db.source.createConnection()
    createSchema()
  }
  after {
    connection.close()
  }

  def createSchema() {
    import driver.simple._
    db.withSession { implicit session =>
      scormUsersTQ.ddl.create
      attemptTQ.ddl.create
    }
  }

  test("execute 'create' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")
    import driver.simple._
    db.withSession { implicit session =>
      val isAttempt = attemptTQ.filter(_.id === attemptId).exists.run
      assert(isAttempt)
    }
  }

  test("execute 'getActive' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    attemptStorage.createAndGetID(123, 1, "organizationId")
    val attempt = attemptStorage.getActive(123, 1)
    assert(attempt.isDefined)
  }

  test("execute 'getLast' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    attemptStorage.createAndGetID(123, 1, "organizationId")
    val attempt = attemptStorage.getLast(123, 1, false)
    assert(attempt.isDefined)
  }


  test("execute 'markAsComplete' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")
    attemptStorage.markAsComplete(attemptId)
    import driver.simple._
    db.withSession { implicit session =>
      val isAttempt = attemptTQ.filter(a => a.id === attemptId && a.packageId === 1L && a.isComplete).exists.run
      assert(isAttempt)
    }
  }

  test("execute 'getAllComplete' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")
    attemptStorage.markAsComplete(attemptId)
    val attempts = attemptStorage.getAllComplete(123, 1)
    assert(attempts.nonEmpty)
  }


  test("execute 'getByID' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")
    val attempts = attemptStorage.getByID(attemptId)
    assert(attempts.isDefined)
  }

  test("execute 'checkIfComplete' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")
    attemptStorage.markAsComplete(attemptId)
    val attempts = attemptStorage.checkIfComplete(123, 1)
    assert(attempts)
  }
}
