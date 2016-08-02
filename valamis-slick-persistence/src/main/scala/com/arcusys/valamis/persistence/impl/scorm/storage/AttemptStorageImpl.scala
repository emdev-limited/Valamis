package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.model.tracking.Attempt
import com.arcusys.valamis.lesson.scorm.storage.tracking.AttemptStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.{AttemptModel, ScormUserModel}
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptTableComponent, ScormUserComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class AttemptStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends AttemptStorage
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile {

  import driver.simple._

  override def getActive(userId: Long, packageId: Long): Option[Attempt] =
    db.withSession { implicit session =>
      val attempt = attemptTQ
        .filter(a => a.userId === userId && a.packageId === packageId && a.isComplete === false)
        .firstOption

      attempt.map(convert)
    }

  override def getLast(userId: Long, packageId: Long, complete: Boolean): Option[Attempt] =
    db.withSession { implicit session =>
      val attempt = attemptTQ
        .filter(a => a.userId === userId && a.packageId === packageId && a.isComplete === complete)
        .firstOption

      attempt.map(convert)
    }

  override def markAsComplete(id: Long): Unit = db.withSession { implicit session =>
    attemptTQ.filter(_.id === id).map(_.isComplete).update(true)
  }

  override def getAllComplete(userId: Long, packageId: Long): Seq[Attempt] = db.withSession { implicit session =>
    val attempts = attemptTQ.filter(a => a.userId === userId && a.packageId === packageId && a.isComplete === true).run
    attempts.map(convert)
  }

  override def getByID(id: Long): Option[Attempt] = db.withSession { implicit session =>
    val attempt = attemptTQ
      .filter(_.id === id)
      .firstOption
    attempt.map(convert)
  }


  override def createAndGetID(userId: Long, packageId: Long, organizationId: String): Long =
    db.withSession { implicit session =>

      val attempt = AttemptModel(
        None,
        userId,
        packageId,
        organizationId,
        isComplete = false)

      (attemptTQ returning attemptTQ.map(_.id)) += attempt
    }

  override def checkIfComplete(userId: Long, packageId: Long): Boolean = db.withSession { implicit session =>
     attemptTQ.filter(a => a.userId === userId && a.packageId === packageId && a.isComplete).exists.run
  }


  private def convert(attempt: AttemptModel)(implicit session: JdbcBackend#SessionDef): Attempt = {
    val user = scormUsersTQ.filter(_.userId === attempt.userId).first
    convert(attempt, user.convert)
  }

  private def convert(attempt: AttemptModel, scormUser: ScormUser): Attempt = {
    Attempt(
      attempt.id.get.toInt,
      scormUser,
      attempt.packageId,
      attempt.organizationId,
      attempt.isComplete)
  }

  implicit class ScormUserToTincan(s: ScormUserModel) {
    def convert: ScormUser = {
      ScormUser(
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
