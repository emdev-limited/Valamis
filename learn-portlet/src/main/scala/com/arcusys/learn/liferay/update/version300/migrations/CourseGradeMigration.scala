package com.arcusys.learn.liferay.update.version300.migrations

import com.arcusys.learn.liferay.update.version300.course.CourseGradeTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}

import util.Try

class CourseGradeMigration(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends CourseGradeTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(): Unit = {

    val grades = db.withSession { implicit s =>
      getOldGrades
    }

    db.withTransaction { implicit s =>
      courseGrades.ddl.create

      if (grades.nonEmpty) {
        courseGrades ++= grades
      }
    }
  }

  private def getOldGrades(implicit s: JdbcBackend#Session): Seq[CourseGrade] = {
    val reader = GetResult[CourseGrade](r => {
      val id = r.nextLong() //id_ LONG not null primary key
      val courseId = r.nextLong() //courseID INTEGER null,
      val userId = r.nextLong() //userID INTEGER null,
      val gradeRaw = r.nextString() //grade VARCHAR(3000) null,
      val comment = r.nextStringOption() //comment_ TEXT null,
      val date = r.nextDateOption() //date_ DATE null,

      val grade = Try(gradeRaw.toFloat).map(_ / 100).toOption

      CourseGrade(
        courseId,
        userId,
        grade,
        date.map(new DateTime(_)) getOrElse DateTime.now,
        comment.filter(_.nonEmpty)
      )
    })

    StaticQuery.queryNA[CourseGrade]("select * from Learn_LFCourse")(reader).list
  }
}