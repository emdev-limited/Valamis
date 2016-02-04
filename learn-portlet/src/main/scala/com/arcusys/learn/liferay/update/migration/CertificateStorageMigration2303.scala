package com.arcusys.learn.liferay.update.migration

import java.net.URLDecoder

import com.arcusys.learn.liferay.update.version240.certificate._
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.core.SlickProfile
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.learn.liferay.update.version240.file.FileTableComponent
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery, GetResult, JdbcBackend}

class CertificateStorageMigration2303(
    val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateTableComponent
  with ActivityGoalTableComponent
  with CourseGoalTableComponent
  with PackageGoalTableComponent
  with StatementGoalTableComponent
  with CertificateStateTableComponent
  with SlickProfile {

  import driver.simple._

  private def certificateLogoDir(id: Long) = s"files/$id/"

  val fileNameRegexp = "files/\\d+/(.+)".r
  private def getFileName(path: String) = fileNameRegexp.findFirstMatchIn(path).map(_.group(1))

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      implicit val getCertificate = GetResult[Certificate] { r => (
        r.nextInt(),                             //        id_
        r.nextString(),                          //        title
        r.nextString(),                          //        description
        r.nextString(),                          //        logo
        r.nextBoolean(),                         //        isPermanent
        r.nextBoolean(),                         //        publishBadge
        r.nextString(),                          //        shortDescription
        r.nextInt(),                             //        companyID,
        PeriodTypes.withName(r.nextString()),    //        validPeriodType
        r.nextInt(),                             //        validPeriod
        new DateTime(r.nextDate()),              //        createdDate
        r.nextBoolean(),                         //        isPublished
        r.nextLongOption()                       //        scope
      )}

      implicit val getActivityGoal = GetResult[ActivityGoal] { r => (
        r.nextInt(),                           //        certificateID
        r.nextString(),                        //        activityName
        r.nextInt(),                           //        datacount
        r.nextInt(),                           //        period
        PeriodTypes.withName(r.nextString())   //        periodType
      )}

      implicit val getCourseGoal = GetResult[CourseGoal] { r => (
        r.nextInt(),                             //        certificateID
        r.nextLong,                              //        courseID
        r.nextInt(),                             //        period
        PeriodTypes.withName(r.nextString()),    //        periodType
        r.nextInt()                              //        arrangementIndex
      )}

      implicit val getPackageGoal = GetResult[PackageGoal] { r => (
        r.nextInt(),                           //      certificateID
        r.nextLong(),                          //      packageID
        r.nextInt(),                           //      period
        PeriodTypes.withName(r.nextString())   //      periodType
      )}

      implicit val getStatementGoal = GetResult[StatementGoal] { r => (
        r.nextLong(),                        //      certificateID
        r.nextString(),                      //      verb
        r.nextString(),                      //      object
        r.nextInt(),                         //      period
        PeriodTypes.withName(r.nextString()) //      periodType
      )}

      implicit val getCertificateToUser = GetResult[CertificateState] { r =>
        val certificateId = r.nextInt()
        val userId = r.nextInt()
        val userJoinedDate = new DateTime(r.nextDate())
        (userId, CertificateStatuses.InProgress, userJoinedDate, userJoinedDate, certificateId)
      }

      val columns = "id_, title, description, logo, isPermanent, publishBadge, shortDescription, companyID, validPeriodType, validPeriod, createdDate, isPublished, scope"
      StaticQuery.queryNA[Certificate](s"SELECT $columns FROM Learn_LFCertificate")
        .list
        .foreach { certificate =>
        val newDescription = URLDecoder.decode(certificate._3, "UTF-8")
        val newCertificateId = (certificates returning certificates.map(_.id)).insert(certificate.copy(_3 = newDescription))

        StaticQuery.queryNA[ActivityGoal]("SELECT certificateID, activityName, datacount, periodType, period FROM Learn_LFCertificateActivity WHERE certificateID=${certificate.id}")
          .list
          .map { aG => activityGoals.insert(aG.copy(_1 = newCertificateId)) }

        StaticQuery.queryNA[CourseGoal]("SELECT certificateID, courseID, period, periodType, arrangementIndex FROM Learn_LFCertificateCourse  WHERE certificateID=${certificate.id}")
          .list
          .map { cG => courseGoals.insert(cG.copy(_1 = newCertificateId)) }

        StaticQuery.queryNA[PackageGoal]("SELECT certificateID, packageID, period, periodType FROM Learn_LFCertificatePackageGoal WHERE certificateID=${certificate.id}")
          .list
          .map { pG => packageGoals.insert(pG.copy(_1 = newCertificateId)) }

        StaticQuery.queryNA[StatementGoal]("SELECT certificateID, verb, object, period, periodType FROM Learn_LFCertTCStmnt WHERE certificateID=${certificate.id}")
          .list
          .map { sG => statementGoals.insert(sG.copy(_1 = newCertificateId)) }

        StaticQuery.queryNA[CertificateState]("SELECT certificateID, userID, attachedDate FROM Learn_LFCertificateUser WHERE certificateID=${certificate.id}")
          .list
          .map { cS => certificateStates.insert(cS.copy(_1 = newCertificateId)) }

        val files = new FileTableComponent {
          override protected val driver = CertificateStorageMigration2303.this.driver
        }.files

        val oldLogoDir = certificateLogoDir(certificate._1)

        val certificatePaths = files
          .filter(_.filename.startsWith(oldLogoDir))
          .map(_.filename)
          .run

        certificatePaths.foreach { path =>
          val fileName =
            getFileName(path)
              .getOrElse(throw new IllegalStateException(s"""$path can't be regexped"""))

          files
            .filter(_.filename === path)
            .map(_.filename)
            .update(certificateLogoDir(newCertificateId) + fileName)
        }
      }
    }
  }
}
