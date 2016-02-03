package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.services.{PermissionHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.valamis.file.FileTableComponent
import com.arcusys.valamis.file.model.FileRecord
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.util.StreamUtil
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portlet.documentlibrary.service.DLFileEntryServiceUtil

import scala.slick.driver.JdbcProfile

class DBUpdater2424 extends LUpgradeProcess with SlideTableComponent with FileTableComponent with Injectable {

  implicit val bindingModule = Configuration

  val dbInfo = inject[SlickDBInfo]

  override val driver: JdbcProfile = dbInfo.slickProfile

  override def getThreshold = 2424

  override def doUpgrade(): Unit = dbInfo.databaseDef.withTransaction { implicit session =>
    import driver.simple._

    val defaultUserId = UserLocalServiceHelper().getDefaultUserId(PortalUtilHelper.getDefaultCompanyId)
    PermissionHelper.preparePermissionChecker(defaultUserId)

    val fileNames = files.map(_.filename).list
    val pdfRegex = """.*viewer.html\?file=.*/((((quiz|slide)Data)|slide_item_)\d+)/(.+)$"""
    val docLibRegex = """(url\(\")?.*documents/.+/.+/(.+)/.*version=([^&]+).*entryId=([^&]+)(.*ext=([^&\"]+))?(\"\))?(\s+[^&]+)?"""

    def filePath(folderName: String, fileName: String) = s"files/$folderName/$fileName"
    def fileExists(fileName: String) = files
      .filter(_.filename === fileName)
      .firstOption
      .isDefined

    slideElements
      .filter(_.content.like("%viewer.html%"))
      .foreach { slideElement =>
        val slideElementFolderPrefix = pdfRegex.r
          .findFirstMatchIn(slideElement.content)
          .map(_.group(2))
        val pdfFileName = slideElement.content.reverse.takeWhile(_ != '/').reverse

        slideElementFolderPrefix.foreach { folderPrefix =>
          fileNames
            .filter(_.matches(s".*$folderPrefix\\d+/$pdfFileName.*"))
            .foreach { filename =>
              val newFileName = filename.replaceFirst("(quizData|slide_item_)\\d+", s"slideData${slideElement.id.get}")
              if(!fileExists(newFileName)) {
                val fileContent = files.filter(_.filename === filename).map(_.content).run
                fileContent.foreach { file =>
                  files.insert(new FileRecord(newFileName, file))
                }
                slideElements
                  .filter(_.id === slideElement.id)
                  .map(_.content)
                  .update(slideElement.content.replaceFirst(pdfRegex, "$5"))
              }
            }
        }
      }

    slideElements
      .filter(_.content.like("%/documents/%version%entryId%"))
      .foreach { slideElement =>
        val version =
          docLibRegex.r
            .findFirstMatchIn(slideElement.content)
            .map(_.group(3))
        val fileEntryId =
          docLibRegex.r
            .findFirstMatchIn(slideElement.content)
            .map(_.group(4))
        val fileName =
          docLibRegex.r
            .findFirstMatchIn(slideElement.content)
            .map(x => s"${x.group(2)}.${x.group(6)}")

        if(version.isDefined && fileEntryId.isDefined && fileName.isDefined) {
          val stream = DLFileEntryServiceUtil.getFileAsStream(fileEntryId.get.toLong, version.get)
          val bytes = StreamUtil.toByteArray(stream)
          if(!bytes.isEmpty) {
            val newFileName = filePath(s"slide_item_${slideElement.id.get}", fileName.get)
            if(!fileExists(newFileName)) {
              files.insert(new FileRecord(newFileName, Some(bytes)))
              slideElements
                .filter(_.id === slideElement.id)
                .map(_.content)
                .update(slideElement.content.replaceFirst(docLibRegex, "$2.$6$8"))
            }
          }
        }
      }

    slides
      .filter(_.bgImage.nonEmpty)
      .foreach { slide =>
        slide.bgImage.foreach { bgImage =>
          fileNames
            .filter(x => x.matches(s".*quizData\\d+/${bgImage.takeWhile(_ != ' ')}.*"))
            .foreach { filename =>
              val newFileName = filename.replaceFirst("(quizData|slide_)\\d+", s"slide_${slide.id.get}")
              if(!fileExists(newFileName)) {
                val fileContent = files
                  .filter(_.filename === filename)
                  .map(_.content)
                  .run
                fileContent.foreach { file =>
                  files.insert(new FileRecord(newFileName, file))
                }
              }
            }
        }
      }

    slides
      .filter(_.bgImage.like("%/documents/%version%entryId%"))
      .foreach { slide =>
        slide.bgImage.foreach { bgImage =>
          val version =
            docLibRegex.r
              .findFirstMatchIn(bgImage)
              .map(_.group(3))
          val fileEntryId =
            docLibRegex.r
              .findFirstMatchIn(bgImage)
              .map(_.group(4))
          val fileName =
            docLibRegex.r
              .findFirstMatchIn(bgImage)
              .map(x => s"${x.group(2)}.${x.group(6)}")

          if(version.isDefined && fileEntryId.isDefined) {
            val stream = DLFileEntryServiceUtil.getFileAsStream(fileEntryId.get.toLong, version.get)
            val bytes = StreamUtil.toByteArray(stream)
            if(!bytes.isEmpty) {
              val newFileName = filePath(s"slide_${slide.id.get}", fileName.get)
              files.insert(new FileRecord(newFileName, Some(bytes)))
              slides
                .filter(_.id === slide.id)
                .map(_.bgImage)
                .update(Some(bgImage.replaceFirst(docLibRegex, "$2.$6$8")))
            }
          }
        }
      }
  }
}