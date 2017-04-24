package com.arcusys.valamis.updaters.version310

import com.arcusys.valamis.updaters.version310.slide.SlideTableComponent

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.arcusys.valamis.persistence.common.DatabaseLayer._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.common.BaseDBUpdater

import scala.concurrent.ExecutionContext.Implicits.global


class DBUpdater3105()
  extends BaseDBUpdater
    with SlideTableComponent
    with SlickProfile {

  override def getThreshold = 3105

  import driver.api._

  override def doUpgrade(): Unit = {
    val zIndexToContent = Map(
      "1" -> "<div id=\"lesson-summary-header\"><span>Lesson summary</span></div>",
      "3" -> "<span id=\"lesson-summary-table\"></span>"
    )

    val slideIds = slides.filter(_.isLessonSummary === true)
      .map(_.id)
      .result

    val action = slideIds flatMap { ids =>
      DBIO.sequence(
        ids flatMap { id =>
          zIndexToContent.map { case (zIndex, content) =>
            slideElements
              .filter(x => x.slideId === id && x.zIndex === zIndex)
              .map(_.content)
              .update(content)
          }
        }
      )
    }
    Await.result(dbInfo.databaseDef.run(action), Duration.Inf)
  }

}
