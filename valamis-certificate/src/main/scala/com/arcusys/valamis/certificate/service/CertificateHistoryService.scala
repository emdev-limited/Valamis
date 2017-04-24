package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{Certificate, CertificateHistory}
import org.joda.time.DateTime

import scala.concurrent.Future

trait CertificateHistoryService {

  def add(certificate: Certificate, isDelete: Boolean = false): Unit

  def get(companyId: Long, date: DateTime): Future[Seq[CertificateHistory]]
}
