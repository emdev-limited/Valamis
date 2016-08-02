package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.slide.SlideTableComponent
import slick.jdbc.JdbcBackend

import scala.slick.driver._

class CreateDefaultValues(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
  extends SlideTableComponent
    with SlickProfile {

  import driver.simple._

  def create(): Unit = {
    db.withTransaction { implicit session =>

      val defaultSlideSet = slideSets.filter { e =>
        e.title === defaultSlideSetTemplate.title &&
          e.description === defaultSlideSetTemplate.description &&
          e.courseId === defaultSlideSetTemplate.courseId &&
          e.logo.isEmpty &&
          e.isTemplate === defaultSlideSetTemplate.isTemplate &&
          e.isSelectedContinuity === defaultSlideSetTemplate.isSelectedContinuity
      }.firstOption

      if (defaultSlideSet.isEmpty)
        slideSets += defaultSlideSetTemplate
    }

    db.withTransaction { implicit session =>
      val defaultThemes = slideThemes.filter(_.isDefault === true).firstOption

      if (defaultThemes.isEmpty)
        slideThemes ++= defaultSlideThemes
    }
  }
}
