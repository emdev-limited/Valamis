package com.arcusys.learn.liferay.update.version270.migrations

import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.scorm.model.ScormManifest
import com.arcusys.valamis.lesson.scorm.storage.ScormManifestTableComponent
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 13.02.16.
  */
class ScormPackagesMigration(val db: JdbcBackend#DatabaseDef,
                             val driver: JdbcProfile)
  extends PackageMigrationBase
    with ScormPackageReader
    with ScormManifestTableComponent {

  import driver.simple._

  def migrate(): Unit = {

    val (packages, limits) = db.withSession(implicit s => (
      getScormPackages.filter(_.courseId.isDefined),
      getLimits
      ))

    db.withTransaction { implicit s =>

      packages.foreach(p => {
        val lesson = convertPackage(p)
        migrationLessons  += lesson

        val manifest = convertManifest(lesson.id, p)
        scormManifestsTQ += manifest

        for(limit <- limits.find(l => l.lessonId == p.id && l.lessonType == LessonType.Tincan)) {
          val lessonLimit = convertLessonLimit(lesson.id, limit)
          lessonLimits += lessonLimit
        }
      })
    }
  }

  private def convertPackage(pkg: ScormPackage): Lesson = {

    val courseId = pkg.courseId
      .getOrElse(throw new IllegalStateException(s"package with id: ${pkg.id} has no courseId"))

    val userId = getUserIdFromActivity(pkg.id)
      .orElse(getUserIdFromPackageAsset(pkg.id))
      .orElse(getDefaultUserIdFromCourse(courseId))
      .getOrElse(getDefaultUserId)

    val creationDate = getCreationDateFromActivity(pkg.id)
      .orElse(getCreationDateFromAsset(pkg.id))
      .getOrElse(DateTime.now)

    Lesson(
      pkg.id,
      LessonType.Scorm,
      pkg.title.getOrElse("xApi lesson"),
      pkg.summary.getOrElse(""),
      pkg.logo,
      courseId,
      isVisible = Some(true),
      pkg.beginDate,
      pkg.endDate,
      userId,
      creationDate
    )
  }

  private def convertManifest(lessonId: Long, pkg: ScormPackage): ScormManifest = {
    ScormManifest(
      lessonId,
      version = None,
      pkg.base,
      scormVersion = "",
      pkg.defaultOrganizationID,
      pkg.resourcesBase
    )
  }
}
