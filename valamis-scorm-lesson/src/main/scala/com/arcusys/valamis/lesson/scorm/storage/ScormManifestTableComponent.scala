package com.arcusys.valamis.lesson.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.ScormManifest
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}

/**
  * Created by mminin on 19.01.16.
  */
trait ScormManifestTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  class ScormManifestTable(tag: Tag) extends Table[ScormManifest](tag, tblName("SCORM_MANIFEST")) {
    def lessonId = column[Long]("LESSON_ID", O.PrimaryKey)

    def version = column[Option[String]]("VERSION", O.Length(254, true))

    def base = column[Option[String]]("BASE", O.Length(2000, true))

    def scormVersion = column[String]("SCORM_VERSION", O.Length(254, true))

    def defaultOrganisationId = column[Option[String]]("DEFAULT_ORGANISATION_ID", O.Length(2000, true))

    def resourcesBase = column[Option[String]]("RESOURCES_BASE", O.Length(2000, true))

    def * = (lessonId, version, base, scormVersion, defaultOrganisationId, resourcesBase) <>(ScormManifest.tupled, ScormManifest.unapply)
  }

  val scormManifestsTQ = TableQuery[ScormManifestTable]
}
