package com.arcusys.valamis.lesson.tincan.service

import com.arcusys.valamis.lesson.tincan.model.LessonCategoryGoal
import com.arcusys.valamis.lesson.tincan.storage.LessonCategoryGoalTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class LessonCategoryGoalServiceImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LessonCategoryGoalService
  with SlickProfile
  with LessonCategoryGoalTableComponent{

  import driver.simple._

  override def add(goals: Seq[LessonCategoryGoal]): Unit = {
    if (goals.nonEmpty) db.withTransaction{ implicit session =>
      lessonCategoryGoals ++= goals
    }
  }

  override def get(lessonId: Long): Seq[LessonCategoryGoal] = db.withSession { implicit session =>
    lessonCategoryGoals.filter(_.lessonId === lessonId).list
  }

  override def delete(lessonId: Long): Unit = db.withSession { implicit session =>
    lessonCategoryGoals.filter(_.lessonId === lessonId).delete
  }
}
