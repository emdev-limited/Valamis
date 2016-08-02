package com.arcusys.learn.liferay.update.version270.migrations

import org.joda.time.DateTime
import slick.jdbc.GetResult

import scala.slick.jdbc.{ JdbcBackend, StaticQuery}

/**
  * Created by mminin on 13.02.16.
  */
trait ScormPackageReader {

  def getScormPackages(implicit s: JdbcBackend#Session): Seq[ScormPackage] = {
    implicit val reader = GetResult[ScormPackage](r => ScormPackage(
      r.nextLong(), //id_ LONG not null primary key,
      r.nextStringOption(), //defaultOrganizationID TEXT null,
      r.nextStringOption(), //title VARCHAR(2000) null,
      r.nextStringOption(), //base TEXT null,
      r.nextStringOption(), //resourcesBase TEXT null,
      r.nextStringOption(), //summary VARCHAR(2000) null,
      r.nextLongOption(), //assetRefID LONG null,
      r.nextLongOption(), //courseID INTEGER null,
      r.nextStringOption(), //logo TEXT null,
      r.nextDateOption() map (new DateTime(_)), //beginDate DATE null,
      r.nextDateOption() map (new DateTime(_)) //endDate DATE null
    ))

    StaticQuery.queryNA[ScormPackage]("select * from Learn_LFPackage").list
  }

  case class ScormPackage(id: Long, //id_ LONG not null primary key,
                          defaultOrganizationID: Option[String], //defaultOrganizationID TEXT null,
                          title: Option[String], //title VARCHAR(2000) null,
                          base: Option[String], //base TEXT null,
                          resourcesBase: Option[String], //resourcesBase TEXT null,
                          summary: Option[String], //summary VARCHAR(2000) null,
                          assetRefId: Option[Long], //assetRefID LONG null,
                          courseId: Option[Long], //courseID INTEGER null,
                          logo: Option[String], //logo TEXT null,
                          beginDate: Option[DateTime], //beginDate DATE null,
                          endDate: Option[DateTime] //endDate DATE null
                         )

}