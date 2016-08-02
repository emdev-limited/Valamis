package com.arcusys.learn.liferay.update.version270.migrations

import org.joda.time.DateTime
import slick.jdbc.GetResult

import scala.slick.jdbc.{ JdbcBackend, StaticQuery}

/**
  * Created by mminin on 13.02.16.
  */
trait TincanPackageReader {

  def getTincanPackages(implicit s: JdbcBackend#Session): Seq[TincanPackage] = {
    implicit val reader = GetResult[TincanPackage](r => TincanPackage(
      r.nextLong(), //id_ LONG not null primary key,
      r.nextStringOption(), //title VARCHAR(2000) null,
      r.nextStringOption(), //summary VARCHAR(2000) null,
      r.nextLongOption(), //assetRefID LONG null,
      r.nextLongOption(), //courseID INTEGER null,
      r.nextStringOption(), //logo TEXT null,
      r.nextDateOption() map (new DateTime(_)), //beginDate DATE null,
      r.nextDateOption() map (new DateTime(_)) //endDate DATE null
    ))

    StaticQuery.queryNA[TincanPackage]("select * from Learn_LFTincanPackage").list
  }

  def getActivities(implicit s: JdbcBackend#Session): Seq[TincanManifestAct] = {
    implicit val reader = GetResult[TincanManifestAct](r => TincanManifestAct(
      r.nextLong(), // id_ LONG not null primary key,
      r.nextStringOption(), // tincanID VARCHAR(2000) null,
      r.nextLongOption(), //packageID LONG null,
      r.nextStringOption(), //activityType VARCHAR(2000) null,
      r.nextStringOption(), //name VARCHAR(2000) null,
      r.nextStringOption(), //description VARCHAR(2000) null,
      r.nextStringOption(), //launch VARCHAR(2000) null,
      r.nextStringOption() //resourceID VARCHAR(2000) null
    ))

    StaticQuery.queryNA[TincanManifestAct](s"select * from Learn_LFTincanManifestAct").list
  }

  case class TincanPackage(id: Long, //id_ LONG not null primary key,
                           title: Option[String], //title VARCHAR(2000) null,
                           summary: Option[String], //summary VARCHAR(2000) null,
                           assetRefId: Option[Long], //assetRefID LONG null,
                           courseId: Option[Long], //courseID INTEGER null,
                           logo: Option[String], //logo TEXT null,
                           beginDate: Option[DateTime], //beginDate DATE null,
                           endDate: Option[DateTime] //endDate DATE null
                          )

  case class TincanManifestAct(id: Long, // id_ LONG not null primary key,
                               activityId: Option[String], // tincanID VARCHAR(2000) null,
                               packageId: Option[Long], //packageID LONG null,
                               activityType: Option[String], //activityType VARCHAR(2000) null,
                               name: Option[String], //name VARCHAR(2000) null,
                               description: Option[String], //description VARCHAR(2000) null,
                               launch: Option[String], //]launch VARCHAR(2000) null,
                               resourceId: Option[String] //resourceID VARCHAR(2000) null
                              )

}