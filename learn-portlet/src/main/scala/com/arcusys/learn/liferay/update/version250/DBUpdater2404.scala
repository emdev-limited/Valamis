package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.file.model.FileRecord
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.persistence.impl.file.FileTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.StaticQuery

/**
 * Created by ashikov on 10/21/15.
 */
class DBUpdater2404
  extends LUpgradeProcess
  with Injectable
  with SlickProfile
  with FileTableComponent {

  val dbInfo = inject[SlickDBInfo]

  override val driver: JdbcProfile = dbInfo.slickProfile

  import driver.simple._

  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2404

  private val bgImageRegex = "(files.*slide_\\d+)(_\\d+)(.*)$"

  override def doUpgrade(): Unit = dbInfo.databaseDef.withTransaction { implicit session =>
    val slideIds = StaticQuery.queryNA[Long](s"select id_ from Learn_LFSlide where bgimage is not null").list

    slideIds
      .foreach{id =>
        val bgImages =
          (files
            .map(_.filename)
            .filter(_ like s"files/slide_$id/%")
            ++
          files
            .map(_.filename)
            .filter(_ like s"files/slide_$id\\_%")
          )
            .sortBy(_ asc)
            .list

        val content = if(bgImages.nonEmpty)
            files
              .filter(_.filename === bgImages.last)
              .map(_.content)
              .firstOption
          else None

        if( content.isDefined) {
          files
            .filter(_.filename === bgImages.head)
            .update(
              FileRecord(
                bgImages.head.replaceFirst(bgImageRegex, "$1$3"),
                content.get
              )
            )
        }
      }
  }
}
