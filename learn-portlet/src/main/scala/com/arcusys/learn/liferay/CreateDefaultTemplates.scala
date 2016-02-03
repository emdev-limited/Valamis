package com.arcusys.learn.liferay

import com.arcusys.learn.liferay.update.version250.{DBUpdater2415, DBUpdater2420, DBUpdater2423}
import com.arcusys.learn.liferay.update.version260.DBUpdater2517
import com.arcusys.valamis.core.SlickDBInfo

class CreateDefaultTemplates(dbInfo: SlickDBInfo){
  private lazy val templatesUpdater = new DBUpdater2415(dbInfo)
  private lazy val lessonSummaryUpdater = new DBUpdater2420(dbInfo)
  private lazy val elementsLessonSummaryUpdater = new DBUpdater2423(dbInfo)
  private lazy val templatesFontsUpdater = new DBUpdater2517(dbInfo)

  def create() {
    templatesUpdater.doUpgrade()
    lessonSummaryUpdater.doUpgrade()
    elementsLessonSummaryUpdater.doUpgrade()
    templatesFontsUpdater.doUpgrade()
  }
}
