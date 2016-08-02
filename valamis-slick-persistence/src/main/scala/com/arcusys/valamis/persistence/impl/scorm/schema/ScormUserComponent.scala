package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ScormUserModel

trait ScormUserComponent extends TypeMapper { self: SlickProfile =>


  import driver.simple._

  class ScormUserTable(tag: Tag) extends Table[ScormUserModel](tag, tblName("SCO_SCORM_USER")) {

    def userId = column[Long]("USER_ID")

    def name = column[String]("NAME", O.DBType(varCharMax))

    def preferredAudioLevel = column[Option[Double]]("PREFERRED_AUDIO_LEVEL")

    def preferredLanguage = column[Option[String]]("PREFERRED_LANGUAGE", O.DBType(varCharMax))

    def preferredDeliverySpeed = column[Option[Double]]("PREFERRD_DELIVERY_SPEED")

    def preferredAudioCaptioning = column[Option[Int]]("PREFERRED_AUDIO_CAPTIONING")

    def * = (
      userId,
      name,
      preferredAudioLevel,
      preferredLanguage,
      preferredDeliverySpeed,
      preferredAudioCaptioning) <>(ScormUserModel.tupled, ScormUserModel.unapply)

    def PK = primaryKey(pkName("LF_USER_PRIMARY_KEY"), userId)
  }

  val scormUsersTQ = TableQuery[ScormUserTable]
}

