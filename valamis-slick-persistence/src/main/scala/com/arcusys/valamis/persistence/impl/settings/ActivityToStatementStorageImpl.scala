package com.arcusys.valamis.persistence.impl.settings

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.settings.model.ActivityToStatement
import com.arcusys.valamis.settings.storage.ActivityToStatementStorage

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class ActivityToStatementStorageImpl(val db: JdbcBackend#DatabaseDef,
                                     val driver: JdbcProfile)
  extends ActivityToStatementStorage
    with ActivityToStatementTableComponent
    with SlickProfile {

  import driver.simple._

  override def getBy(courseId: Long): Seq[ActivityToStatement] = {
    db.withSession { implicit s =>
      filterBy(courseId).list
    }
  }

  override def getBy(courseId: Long, activityClassId: Long): Option[ActivityToStatement] = {
    db.withSession { implicit s =>
      filterBy(courseId, activityClassId).firstOption
    }
  }

  override def set(courseId: Long, activityClassId: Long, verbValue: Option[String]): Unit = {

    db.withSession { implicit s =>
      verbValue match {
        case None =>
          filterBy(courseId, activityClassId).delete
        case Some(verb) =>
          val updatedCount = filterBy(courseId, activityClassId).map(_.verb).update(verb)

          if (updatedCount == 0) {
            activityToStatement.insert(ActivityToStatement(courseId, activityClassId, verb))
          }
      }
    }
  }

  private def filterBy(courseId: Long): Query[ActivityToStatementTable, ActivityToStatement, Seq] = {
    activityToStatement.filter(_.courseId === courseId)
  }

  private def filterBy(courseId: Long, activityClassId: Long): Query[ActivityToStatementTable, ActivityToStatement, Seq] = {
    filterBy(courseId).filter(_.activityClassId === activityClassId)
  }
}
