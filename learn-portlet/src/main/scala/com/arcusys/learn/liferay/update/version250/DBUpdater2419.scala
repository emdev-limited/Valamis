package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2419 extends LUpgradeProcess with Injectable with SlideTableComponent{

  implicit val bindingModule = Configuration

  override def getThreshold = 2419

  lazy val dbInfo = inject[SlickDBInfo]
  lazy val driver = dbInfo.slickDriver
  lazy val db = dbInfo.databaseDef
  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withSession { implicit session =>
        var themes = Seq[SlideTheme]()
        themes = themes :+ createSlideTheme("Black and White",
          "#000000",
          "ubuntu$20px$#ffffff") :+
          createSlideTheme("Blue",
            "#48aadf",
            "roboto$20px$#ffffff") :+
          createSlideTheme("Default White",
            "#ffffff",
            "ubuntu$20px$#000000") :+
          createSlideTheme("Green",
            "#ffffff",
            "roboto$20px$#9cc83d")

        themes.foreach(theme =>{
          slideThemes returning slideThemes.map(_.id) insert theme
        })
    }
  }

  private def createSlideTheme(title: String, bgColor: String, font: String): SlideTheme = {
    new SlideTheme(
      title = title,
      bgColor = Some(bgColor),
      font = Some(font),
      isDefault = true
    )
  }
}
