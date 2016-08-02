package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ResourceModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ResourceTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ResourceMigration(val db: JdbcBackend#DatabaseDef,
                        val driver: JdbcProfile)
  extends ResourceTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate()(implicit s: JdbcBackend#Session): Unit = {

    val grades = getOldGrades


    if (grades.nonEmpty) {
      resourceTQ ++= grades
    }
  }

  private def getOldGrades(implicit s: JdbcBackend#Session): Seq[ResourceModel] = {
    implicit val reader = GetResult[ResourceModel](r => ResourceModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLongOption(), // packageID INTEGER null,
      r.nextString(), // scormType TEXT null,
      r.nextStringOption(), // resourceID VARCHAR(3000) null,
      r.nextStringOption(), // href TEXT null,
      r.nextStringOption() // base TEXT null
    ))

    StaticQuery.queryNA[ResourceModel]("select * from Learn_LFResource").list
  }
}
