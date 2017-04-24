package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.valamis.file.service.FileService
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.learn.liferay.update.version260.update2511.CertificateTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.service.ImageProcessor
import com.escalatesoft.subcut.inject.BindingModule

import scala.slick.jdbc.StaticQuery

class DBUpdater2511(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlideTableComponent
    with StoryTreeTableComponent
    with CertificateTableComponent
    with SlickDBContext {

  def this() = this(Configuration)

  private lazy val imageProcessor = inject[ImageProcessor]
  //TODO: remove FileService using, it can be changed in new versions (after 2.5)
  private lazy val fileService = inject[FileService]

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
        fileService.getFileContentOption(s"files/${c.id}/${c.logo}")
          .map { image => imageProcessor.resizeImage(image, LogoWidth, LogoHeight)}
          .foreach { image => fileService.setFileContent( s"${c.id}/", c.logo, image, deleteFolder = true ) }
      }
  }

  private def resizePackagesLogo() = {
    val packagesInfos = dbInfo.databaseDef.withTransaction{ implicit s =>
      val scorm = StaticQuery.queryNA[(Long, Option[String])]("select id_, logo from learn_lfpackage").list
      val tincan = StaticQuery.queryNA[(Long, Option[String])]("select id_, logo from learn_lftincanpackage").list
      scorm ++ tincan
    }

    packagesInfos.foreach { case (id, logo) =>
      logo
        .map("files/" + s"package_logo_id/" + _)
        .flatMap(fileService.getFileContentOption)
        .map(imageProcessor.resizeImage(_, LogoWidth, LogoHeight))
        .foreach(setPackagesLogo(id, logo.get, _))
    }
  }

  private def setPackagesLogo(packageId: Long, logo: String, content: Array[Byte]) =
    dbInfo.databaseDef.withTransaction { implicit session =>
      fileService.setFileContent(
        folder = s"package_logo_$packageId/",
        name = logo,
        content = content,
        deleteFolder = true
      )
    }
}
