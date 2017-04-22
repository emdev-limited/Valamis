package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.updaters.version310.slide.SlideTableComponent

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.arcusys.valamis.persistence.common.DatabaseLayer._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.common.BaseDBUpdater

import scala.concurrent.ExecutionContext.Implicits.global


class DBUpdater3210()
  extends BaseDBUpdater
    with SlideTableComponent
    with SlickProfile {

  override def getThreshold = 3210

  import driver.api._

  override def doUpgrade(): Unit = {
    val slideIds = slides.filter(_.isLessonSummary === true)
      .map(_.id)
      .result

    val action = slideIds flatMap { ids =>
      DBIO.sequence(
        ids map { id =>
            slideElements
              .filter(x => x.slideId === id && x.zIndex === "1")
              .map(_.content)
              .update("""<h1><span style="font-size:2em">Lesson summary</span></h1>""")
          }
      )
    }
    Await.result(dbInfo.databaseDef.run(action), Duration.Inf)
  }

}
