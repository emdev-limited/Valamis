package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import slick.jdbc.JdbcBackend
import slick.driver._
import scala.concurrent.ExecutionContext.Implicits.global

class CreateDefaultValues(val driver: JdbcProfile, val db: JdbcBackend#DatabaseDef)
  extends SlideTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  def create(): Unit = execSync {

    val insertSlideSet =
      for {
        count <- slideSets
          .filter(set => set.courseId === defaultSlideSetTemplate.courseId
            && set.isTemplate === defaultSlideSetTemplate.isTemplate)
          .size
          .result
        _ <- if (count == 0) {
          slideSets += defaultSlideSetTemplate
        }
        else {
          DBIO.successful()
        }
      } yield ()

    val insertThemes =
      for {
        count <- slideThemes.size.result
        _ <- if (count == 0) {
          slideThemes ++= defaultSlideThemes
        }
        else {
          DBIO.successful()
        }
      } yield ()

    insertSlideSet >> insertThemes
  }
}
