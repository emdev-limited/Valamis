package com.arcusys.learn.liferay.update.version250.cleaner

import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

object FileCleaner
  extends Injectable
  with CertificateFileCleanerComponent
  with PackageFileCleanerComponent
  with SlideFileCleanerComponent {
  implicit val bindingModule = Configuration

  private val slickDBInfo = inject[SlickDBInfo]
  protected val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  protected val certificateRepository = inject[CertificateRepository]

  def clean(): Unit = db.withTransaction { implicit session =>
    cleanCertificateLogos()
    cleanPackageLogos()
    cleanPackageContent()
    cleanSlideSetLogos()
    cleanSlideBGImages()
//    cleanSlideElementFiles() FIXME: collision with Lesson designer
  }
}