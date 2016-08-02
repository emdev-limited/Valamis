package com.arcusys.learn.liferay.update.version300.migrations

import com.arcusys.learn.liferay.update.version270.lesson.LessonGradeTableComponent
import com.arcusys.learn.liferay.update.version270.lesson.LessonTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime
import slick.jdbc.GetResult

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

/**
  * Created by mminin on 04.04.16.
  */
class PackageGradeMigration(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends LessonGradeTableComponent
    with LessonTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(): Unit = {

    val grades = db.withSession{implicit s =>
      getOldGrades
        .map(convert)
        .filter(g => lessons.filter(_.id === g.lessonId).map(_.id).firstOption.isDefined)
    }
    db.withTransaction{ implicit s =>



      lessonGrades.ddl.create

      if (grades.nonEmpty) {
        lessonGrades ++= grades
      }
    }
  }

  private def convert(packageGrade: PackageGrade): LessonGrade = {
    LessonGrade(
      packageGrade.packageId,
      packageGrade.userId,
      packageGrade.grade,
      packageGrade.date getOrElse DateTime.now,
      Some(packageGrade.comment).filter(_.nonEmpty)
    )
  }

  private def getOldGrades(implicit s: JdbcBackend#Session): Seq[PackageGrade] = {
    implicit val reader = GetResult[PackageGrade](r => PackageGrade(
      r.nextLong(), //userId LONG not null,
      r.nextLong(), //packageId LONG not null,
      r.nextFloatOption(), //grade VARCHAR(512) null,
      r.nextString(), //comment_ VARCHAR(512) null,
      r.nextDateOption().map(d => new DateTime(d.getTime)) //date_ DATE null,
    ))

    StaticQuery.queryNA[PackageGrade]("select * from Learn_LFPackageGradeStorage").list
  }

  case class PackageGrade(userId: Long,
                          packageId: Long,
                          grade: Option[Float],
                          comment: String,
                          date: Option[DateTime] = None)
}
