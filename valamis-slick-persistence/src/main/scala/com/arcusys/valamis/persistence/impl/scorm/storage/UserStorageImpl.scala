package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.storage.ScormUserStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ScormUserModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptTableComponent, ScormUserComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class UserStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ScormUserStorage
  with ScormUserComponent
  with AttemptTableComponent
  with SlickProfile {

  import driver.simple._

  override def getAll: Seq[ScormUser] = db.withSession { implicit s =>
    scormUsersTQ.list.map(_.convert)
  }

  override def getById(userId: Long): Option[ScormUser] = db.withSession { implicit s =>
    val users = scormUsersTQ.filter(_.userId === userId.toLong).firstOption
    users.map(_.convert)
  }

  override def getByName(name: String): Seq[ScormUser] = db.withSession { implicit s =>
    val users = scormUsersTQ.filter(_.name.toLowerCase like name.toLowerCase).run
    users.map(_.convert)
  }

  override def add(user: ScormUser): Long = db.withSession { implicit s =>
    val scormUser = new ScormUserModel(
      user.id,
      user.name,
      Some(user.preferredAudioLevel),
      Some(user.preferredLanguage),
      Some(user.preferredDeliverySpeed),
      Some(user.preferredAudioCaptioning)
    )

   scormUsersTQ += scormUser
   user.id
  }

  override def modify(user: ScormUser)  = db.withSession { implicit s =>
    scormUsersTQ.filter(_.userId === user.id).map {
      u => (u.name,
        u.preferredAudioCaptioning,
        u.preferredAudioLevel,
        u.preferredDeliverySpeed,
        u.preferredLanguage)}
      .update(user.name,
      Some(user.preferredAudioCaptioning),
      Some(user.preferredAudioLevel),
      Some(user.preferredDeliverySpeed),
      Some(user.preferredLanguage))
  }

  override def delete(userId: Long) = db.withSession { implicit s =>
    scormUsersTQ.filter(_.userId === userId).delete
  }

  override def getUsersWithAttempts: Seq[ScormUser] = db.withSession { implicit s =>
    val usersIds = attemptTQ.map(_.userId).run
    val users = scormUsersTQ.filter(_.userId inSet usersIds).run
    users.map(_.convert)
  }

  implicit class ScormUserToTincan(s: ScormUserModel) {
    def convert = {
      new ScormUser(
        s.userId,
        s.name,
        s.preferredAudioLevel.getOrElse(1D).toFloat,
        s.preferredLanguage.getOrElse(""),
        s.preferredDeliverySpeed.getOrElse(1D).toFloat,
        s.preferredAudioCaptioning.getOrElse(0)
      )
    }
  }
}