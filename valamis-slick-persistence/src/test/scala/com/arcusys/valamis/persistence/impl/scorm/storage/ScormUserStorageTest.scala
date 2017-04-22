package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptTableComponent, ScormUserComponent}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}


/**
  * Created by eboystova on 10.05.16.
  */
class ScormUserStorageTest extends FunSuite
  with ScormUserComponent
  with AttemptTableComponent
  with SlickProfile
  with BeforeAndAfter
  with SlickDbTestBase {

  val storages = new StorageFactory(db, driver)

  val scormUserStorage = new UserStorageImpl(db, driver)
  val attemptStorage = new AttemptStorageImpl(db, driver)

  before {
    createDB()
    createSchema()
  }
  after {
    dropDB()
  }

  def createSchema() {
    import driver.simple._
    db.withSession { implicit session => scormUsersTQ.ddl.create
      attemptTQ.ddl.create
    }

  }

  test("execute 'create' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    import driver.simple._
    db.withSession { implicit session =>
      val isScormUser = scormUsersTQ.filter(_.userId === 123L).exists.run
      assert(isScormUser)
    }
  }

  test("execute 'getById' none without errors") {
    val scormUser = ScormUser(124, "Name", 1, "language", 2, 0)
    scormUserStorage.add(scormUser)
    val user = scormUserStorage.getById(124)
    assert(user.isDefined)
    assert(user.exists(_.name == "Name"))
    assert(user.exists(_.preferredAudioLevel == 1F))
    assert(user.exists(_.preferredDeliverySpeed == 2F))
    assert(user.exists(_.preferredAudioCaptioning == 0))
    assert(user.exists(_.preferredLanguage == "language"))
  }

  test("execute 'getAll' none without errors") {
    val scormUser = ScormUser(125, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val scormUser2 = ScormUser(126, "Name2", 2, "language2", 2, 2)
    scormUserStorage.add(scormUser2)
    val users = scormUserStorage.getAll
    assert(users.nonEmpty)
    assert(users.size == 2)
    assert(users.exists(_.id == 125))
    assert(users.exists(_.name == "Name"))
    assert(users.exists(_.preferredAudioLevel == 1F))
    assert(users.exists(_.preferredDeliverySpeed == 1F))
    assert(users.exists(_.preferredAudioCaptioning == 0))
    assert(users.exists(_.preferredLanguage == "language"))


  }


  test("execute 'getByName' none without errors") {
    val scormUser = ScormUser(126, "Name_1", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)
    val user = scormUserStorage.getByName("Name_1")
    assert(user.nonEmpty && user.exists(_.name == "Name_1"))
  }


  test("execute 'modify' none without errors") {
    val scormUser = ScormUser(127, "Name_1", 1, "language_1", 1, 1)
    scormUserStorage.add(scormUser)
    val scormUserModify = ScormUser(127, "Name_2", 2, "language_2", 2, 2)
    scormUserStorage.modify(scormUserModify)

    val user = scormUserStorage.getById(127)

    assert(user.nonEmpty)
    assert(user.exists(_.name == "Name_2"))
    assert(user.exists(_.preferredAudioCaptioning == 2))
    assert(user.exists(_.preferredAudioLevel == 2F))
    assert(user.exists(_.preferredDeliverySpeed == 2F))
    assert(user.exists(_.preferredLanguage == "language_2"))
  }


  test("execute 'delete' without errors") {
    val scormUser = ScormUser(127, "Name_1", 1, "language_1", 1, 1)
    scormUserStorage.add(scormUser)

    scormUserStorage.delete(127)
    val user = scormUserStorage.getById(127)
    assert(user.isEmpty)
  }


  test("execute 'getUserWithAttempts' without errors") {

    val scormUser = ScormUser(129, "Name_1", 1, "language_1", 1, 1)
    scormUserStorage.add(scormUser)

    val attemptId = attemptStorage.createAndGetID(129, 1, "Organization id")
    val attempt = attemptStorage.getByID(attemptId)

    assert(attempt.isDefined)


    val users = scormUserStorage.getUsersWithAttempts
    assert(users.nonEmpty)
  }
}

