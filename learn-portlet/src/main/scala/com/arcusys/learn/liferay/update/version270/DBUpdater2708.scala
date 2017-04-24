package com.arcusys.learn.liferay.update.version270

import java.util.UUID

import com.arcusys.learn.liferay.LiferayClasses.{LNoSuchCompanyException, LUpgradeProcess}
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.slide.SlideSetTableComponent
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.persistence.impl.uri.TincanUriTableComponent
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2708(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlideSetTableComponent
    with TincanUriTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 2708

  val uriService = inject[TincanURIService]
  val courseService = inject[CourseService]
  val uriType = TincanURIType.Course

  def this() = this(Configuration)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit s =>
      slideSets
        .filterNot(_.courseId === 0L)
        .foreach(fillActivityId)
    }
  }

  private def fillActivityId(slideSet: SlideSet) = {
    db.withTransaction { implicit s =>
      val companyId = courseService
        .getById(slideSet.courseId)
        .map(_.getCompanyId)

      val activityId = try {
        val url = uriService.getLocalURL(companyId = companyId)
        Some(s"$url$uriType/${uriType}_${UUID.randomUUID}")
      } catch {
        case e: LNoSuchCompanyException => None
      }

      activityId foreach { value =>
        slideSets
          .filter(_.id === slideSet.id)
          .map(_.activityId)
          .update(value)
      }
    }
  }
}