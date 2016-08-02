package com.arcusys.learn.liferay.update.version270.migrations

import com.arcusys.learn.liferay.services.{AssetEntryLocalServiceHelper, GroupLocalServiceHelper, SocialActivityLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.update.version240.file.FileTableComponent
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lesson.model.{LessonType, PackageActivityType}
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime
import java.util.Date

import com.arcusys.learn.liferay.LiferayClasses.LSocialActivity
import com.arcusys.learn.liferay.update.version270.lesson.LessonTableComponent
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import slick.jdbc.GetResult

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}
import com.arcusys.valamis.persistence.common.DbNameUtils._

/**
  * Created by mminin on 26.02.16.
  */
trait PackageMigrationBase
  extends LessonTableComponent
    with FileTableComponent
    with SlickProfile
    with TypeMapper {

  val db: JdbcBackend#DatabaseDef
  val driver: JdbcProfile

  import driver.simple._

  class MigrationLessonTable(tag: Tag) extends Table[Lesson](tag, tblName("LESSON")) {
    def id = column[Long]("ID", O.PrimaryKey)
    def lessonType = column[LessonType.Value]("LESSON_TYPE")
    def title = column[String]("TITLE", O.Length(2000, varying = true))
    def description = column[String]("DESCRIPTION", O.Length(2000, varying = true))
    def courseId = column[Long]("COURSE_ID")
    def logo = column[Option[String]]("LOGO")
    def isVisible = column[Option[Boolean]]("IS_VISIBLE")
    def beginDate = column[Option[DateTime]]("BEGIN_DATE")
    def endDate = column[Option[DateTime]]("END_DATE")
    def ownerId = column[Long]("OWNER_ID")
    def creationDate = column[DateTime]("CREATION_DATE")

    def * = (id, lessonType, title, description, logo, courseId, isVisible, beginDate, endDate, ownerId, creationDate) <> (Lesson.tupled, Lesson.unapply)
  }

  val newAssetClassName = "com.arcusys.valamis.lesson.model.Lesson"
  val oldAssetClassNames = Set(
    "com.arcusys.valamis.lesson.tincan.model.TincanPackage",
    "com.arcusys.valamis.lesson.tincan.model.TincanManifest",
    "com.arcusys.valamis.lesson.scorm.model.manifest.Manifest",
    "com.arcusys.valamis.lesson.model.BaseManifest"
  )
  val oldActivityClassNames = Set(
    "com.arcusys.valamis.lesson.tincan.model.TincanPackage",
    "com.arcusys.valamis.lesson.scorm.model.ScormPackage"
  )

  val migrationLessons = TableQuery[MigrationLessonTable]

  def createMigrationLessonTables()(implicit s: JdbcBackend#SessionDef): Unit = {
    migrationLessons.ddl.create
  }

  case class Limit(lessonId: Long, // itemID LONG not null,
                   lessonType: LessonType.LessonType, //itemType VARCHAR(75) not null,
                   passingLimit: Option[Int], //passingLimit INTEGER null,
                   rerunInterval: Option[Int], //rerunInterval INTEGER null,
                   rerunIntervalType: PeriodTypes.PeriodType //rerunIntervalType VARCHAR(512) null,
                  )

  protected def getLimits(implicit s: JdbcBackend#Session): Seq[Limit] = {
    implicit val reader = GetResult(r => Limit(
      r.nextLong(), // itemID LONG not null,
      LessonType.withName(r.nextString()), //itemType VARCHAR(75) not null,
      r.nextIntOption(), //passingLimit INTEGER null,
      r.nextIntOption(), //rerunInterval INTEGER null,
      r.nextStringOption().map(PeriodTypes.withName)
        .getOrElse(PeriodTypes.UNLIMITED) //rerunIntervalType VARCHAR(512) null,
    ))

    StaticQuery.queryNA[Limit](s"select * from Learn_LFLessonLimit").list
  }

  protected def convertLessonLimit(lessonId: Long, limit: Limit): LessonLimit = {
    LessonLimit(
      lessonId,
      limit.passingLimit,
      limit.rerunInterval,
      limit.rerunIntervalType
    )
  }

  protected def getUserIdFromPackageAsset(packageId: Long): Option[Long] = {
    val asset = oldAssetClassNames.toStream
      .flatMap(className => AssetEntryLocalServiceHelper.fetchAssetEntry(className, packageId))
      .headOption

    asset.map(_.getUserId)
  }

  protected def getDefaultUserIdFromCourse(courseId: Long): Option[Long] = {
    Option(GroupLocalServiceHelper.fetchGroup(courseId))
      .map(g => UserLocalServiceHelper().getDefaultUserId(g.getCompanyId))
  }

  protected def getCreationDateFromAsset(packageId: Long): Option[DateTime] = {
    val asset = oldAssetClassNames.toStream
      .flatMap(className => AssetEntryLocalServiceHelper.fetchAssetEntry(className, packageId))
      .headOption

    asset.map(_.getCreateDate).collect {
      case date: Date if date != null => new DateTime(date)
    }
  }

  protected def getCreationDateFromActivity(packageId: Long): Option[DateTime] = {
    getLastPublishedPackageActivity(packageId).map(_.getCreateDate).collect{
        case data: Long => new DateTime(data)
    }
  }

  protected def getDefaultUserId: Long = {
    UserLocalServiceHelper().getDefaultUserId(PortalUtilHelper.getDefaultCompanyId)
  }

  protected def getUserIdFromActivity (packageId: Long): Option[Long] = {
    getLastPublishedPackageActivity(packageId).map(_.getUserId)
  }

  private def getLastPublishedPackageActivity(packageId: Long) : Option[LSocialActivity] = {
    oldActivityClassNames.toStream
      .flatMap(className => SocialActivityLocalServiceHelper.getActivities(className, -1, -1))
      .filter(x => x.getClassPK == packageId && x.getType == PackageActivityType.Published.id)
      .sortBy(_.getCreateDate)
      .lastOption

  }
}
