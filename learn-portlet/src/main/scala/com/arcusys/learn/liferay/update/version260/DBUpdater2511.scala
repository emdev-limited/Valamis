package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.learn.service.util.ImageProcessor
import com.arcusys.valamis.certificate.schema.CertificateTableComponent
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.model.PackageBase
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.lesson.tincan.storage.TincanPackageStorage
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2511(val bindingModule: BindingModule) extends LUpgradeProcess with SlideTableComponent
with StoryTreeTableComponent with CertificateTableComponent with SlickDBContext {

  def this() = this(Configuration)

  private lazy val imageProcessor     = inject[ImageProcessor]
  private lazy val certificateService = inject[CertificateService]
  private lazy val fileService = inject[FileService]
  private lazy val scormRepository = inject[ScormPackagesStorage]
  private lazy val tincanRepository = inject[TincanPackageStorage]

  private val LogoWidth = 360
  private val LogoHeight = 240

  override def getThreshold = 2511

  import driver.simple._

  override def doUpgrade(): Unit = {
    resizeSlideSetsLogo()
    resizeTreesLogo()
    resizeCertificatesLogo()
    resizePackagesLogo()
  }

  private def resizeSlideSetsLogo() = dbInfo.databaseDef.withTransaction { implicit session =>
    slideSets.filter(_.logo.isDefined).list
      .foreach { s =>
        s.logo.map("files/" + s"slideset_logo_${s.id.get}/" + _)
          .flatMap(fileService.getFileContentOption)
          .map(imageProcessor.resizeImage(_, LogoWidth, LogoHeight))
          .foreach(setSlideSetLogo(s, _))
      }
  }

  private def setSlideSetLogo(slideSet: SlideSet, content: Array[Byte]) = dbInfo.databaseDef.withTransaction { implicit session =>
    fileService.setFileContent(
      folder = s"slideset_logo_${slideSet.id.get}/",
      name = slideSet.logo.get,
      content = content,
      deleteFolder = true
    )
  }

  private def resizeTreesLogo() = dbInfo.databaseDef.withTransaction { implicit session =>
    trees.filter(_.logo.isDefined).list
      .foreach { t =>
        val logoPath = s"files/StoryTree/${t.id.get}/"
        t.logo
          .map(logoPath + _ )
          .flatMap(fileService.getFileContentOption)
          .map(imageProcessor.resizeImage(_, LogoWidth, LogoHeight))
          .foreach(setTreeLogo(t, _))
      }
  }

  private def setTreeLogo(tree: Story, content: Array[Byte]) = dbInfo.databaseDef.withTransaction { implicit session =>
    fileService.setFileContent(
      folder = s"StoryTree/${tree.id.get}/",
      name = tree.logo.get,
      content = content,
      deleteFolder = true
    )
  }

  private def resizeCertificatesLogo() = dbInfo.databaseDef.withTransaction { implicit session =>
    certificates.filterNot(_.logo === "").list
      .foreach { c =>
        certificateService.getLogo(c.id)
          .map(imageProcessor.resizeImage(_, LogoWidth, LogoHeight))
          .foreach(certificateService.setLogo(c.id, c.logo, _))
      }
  }

  private def resizePackagesLogo() = {
    val packages = scormRepository.getAll ++
      tincanRepository.getAll
    packages.foreach { p =>
      p.logo
        .map("files/" + s"package_logo_${p.id}/" + _)
        .flatMap(fileService.getFileContentOption)
        .map(imageProcessor.resizeImage(_, LogoWidth, LogoHeight))
        .foreach(setPackagesLogo(p, _))
    }
  }

  private def setPackagesLogo(pkg: PackageBase, content: Array[Byte]) =
    dbInfo.databaseDef.withTransaction { implicit session =>
      fileService.setFileContent(
        folder = s"package_logo_${pkg.id}/",
        name = pkg.logo.get,
        content = content,
        deleteFolder = true
      )
    }
}
