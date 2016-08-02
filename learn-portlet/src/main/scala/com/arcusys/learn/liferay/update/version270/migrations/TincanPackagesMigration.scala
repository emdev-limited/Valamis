package com.arcusys.learn.liferay.update.version270.migrations

import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.lesson.tincan.storage.TincanActivityTableComponent
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 13.02.16.
  */
class TincanPackagesMigration(val db: JdbcBackend#DatabaseDef,
                              val driver: JdbcProfile)
  extends PackageMigrationBase
    with TincanPackageReader
    with TincanActivityTableComponent {

  import driver.simple._

  def migrate(): Unit = {

    val (tincanPackages, tincanActivities, limits) = db.withSession(implicit s => (
      getTincanPackages.filter(_.courseId.isDefined),
      getActivities,
      getLimits
    ))

    db.withTransaction { implicit s =>

      tincanPackages.foreach(p => {
        val lesson = convertPackage(p)
        migrationLessons += lesson

        for(limit <- limits.find(l => l.lessonId == p.id && l.lessonType == LessonType.Tincan)) {
          val lessonLimit = convertLessonLimit(lesson.id, limit)
          lessonLimits.insert(lessonLimit)
        }

        val activities = convertTincanActivities(lesson.id, p, tincanActivities)
        if (activities.isEmpty) throw new Exception("no tincan activity for package " + p.id)

          tincanActivitiesTQ ++= activities

      })
    }
  }

  private def convertPackage(pkg: TincanPackage): Lesson = {

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
      LessonType.Tincan,
      pkg.title.getOrElse("xApi lesson"),
      pkg.summary.getOrElse(""),
      pkg.logo,
      courseId,
      isVisible = Some(true),
      pkg.beginDate,
      pkg.endDate,
      userId,
      creationDate = creationDate
    )
  }

  private def convertTincanActivities(lessonId: Long, pkg: TincanPackage, activities: Seq[TincanManifestAct]): Seq[TincanActivity] = {
    activities
      .filter(_.packageId.contains(pkg.id))
      .map(m => TincanActivity(
        lessonId,
        m.activityId.get, //TODO:  handle
        m.activityType.get, /// ????
        m.name.getOrElse(""),
        m.description.getOrElse(""),
        m.launch,
        m.resourceId
      ))
  }
}
