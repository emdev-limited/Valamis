package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.lesson.{LessonGradeTableComponent, LessonTableComponent}
import com.arcusys.valamis.web.configuration.ioc.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import com.escalatesoft.subcut.inject.BindingModule

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater3011(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlickDBContext
    with LessonGradeTableComponent
    with LessonTableComponent {

  override def getThreshold = 3011

  def this() = this(Configuration)

  import driver.api._

  override def doUpgrade(): Unit = {
    Await.result(migration, Duration.Inf)
  }

  private def migration = {
    db.run {
      lessonGrades
        .filter(_.grade.isDefined)
        .result.flatMap { items =>
        DBIO.sequence(items.map(migrateRow))
      }
    }
  }

  private def migrateRow(lessonGrade: LessonGrade) = {
    val grade = lessonGrade.grade.map(_ / 100)
    lessonGrades.filter(x=> x.lessonId === lessonGrade.lessonId && x.userId === lessonGrade.userId)
      .map(_.grade)
      .update(grade)
  }
}
