package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.slide.SlideTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2704(val bindingModule: BindingModule)
  extends LUpgradeProcess
  with SlideTableComponent
  with SlickDBContext {

  override def getThreshold = 2704

  def this() = this(Configuration)

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      slideSets.list.foreach { slideSet =>
        val isVertical = slides
          .filter(_.slideSetId === slideSet.id)
          .list
          .exists(_.topSlideId.isDefined)
        slideSets
          .filter(_.id === slideSet.id)
          .map(_.topDownNavigation)
          .update(isVertical)
      }
    }
  }
}
