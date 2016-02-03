package com.arcusys.learn.liferay.update.version250.cleaner

import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.file.FileTableComponent

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

trait CertificateFileCleanerComponent extends FileTableComponent {
  protected val driver: JdbcProfile
  protected val certificateRepository: CertificateRepository

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
      val cert = certificateRepository.getByIdOpt(id)
      if(cert.isEmpty)
        files
          .filter(_.filename === path)
          .delete
    }
  }
}