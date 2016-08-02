package com.arcusys.learn.liferay.update.version300.migrations

import com.arcusys.learn.liferay.update.version270.slide.SlideSetTableComponent
import com.arcusys.learn.liferay.update.version300.lesson.{LessonTableComponent, TincanActivityTableComponent}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slide.model.SlideSetStatus

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class LessonScoreMigration(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends LessonTableComponent
    with SlideSetTableComponent
    with TincanActivityTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(): Unit = {
    db.withTransaction { implicit session =>
      tincanActivitiesTQ.list.foreach { activity =>
        slideSets
          .filter(x => x.activityId === activity.activityId && x.status === SlideSetStatus.Published)
          .filter(_.scoreLimit.isDefined)
          .sortBy(_.version.desc)
          .map(_.scoreLimit)
          .firstOption
          .map { case Some(limit) =>
            lessons.filter(_.id === activity.lessonId)
              .map(_.scoreLimit)
              .update(limit)
          }
      }
    }
  }
}
