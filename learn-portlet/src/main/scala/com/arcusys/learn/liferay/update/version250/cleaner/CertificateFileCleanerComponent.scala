package com.arcusys.learn.liferay.update.version250.cleaner

import com.arcusys.learn.liferay.update.version240.certificate.CertificateTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.file.FileTableComponent

import scala.slick.jdbc.JdbcBackend

trait CertificateFileCleanerComponent
  extends FileTableComponent
    with CertificateTableComponent
    with SlickProfile {

  import driver.simple._

  private val fileNameRegexp = "files/(\\d+)/.+".r
  private val certificatePattern = "files/%"
  private def getId(path: String) =
    fileNameRegexp
      .findFirstMatchIn(path)
      .map(m => m.group(1).toLong)

  protected def cleanCertificateLogos()(implicit session: JdbcBackend#Session): Unit = {
    val certificateLogos =
      files
        .map(_.filename)
        .filter(_.like(certificatePattern))
        .run
        .filter(fileNameRegexp.findFirstIn(_).isDefined)

    certificateLogos.foreach { path =>
      val id = getId(path).get

      val hasCertificate = certificates.filter(_.id === id).map(_.id).firstOption.isDefined

      if(!hasCertificate) {
        files
          .filter(_.filename === path)
          .delete
      }
    }
  }
}