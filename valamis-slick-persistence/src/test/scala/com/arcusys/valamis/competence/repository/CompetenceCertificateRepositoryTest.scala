package com.arcusys.valamis.competence.repository

import com.arcusys.valamis.competence.model.{CertificateFilter, CompetenceCertificateTypes, CompetenceCertificate}
import org.scalatest.{BeforeAndAfterEach, FunSpec}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

trait CertificateTestData{
  val companyId = 10157
  val certificate1 = CompetenceCertificate(
    id = None,
    year = Some("2012-2013"),
    title = "Functional programming in Scala",
    description = "Description1",
    tpe = CompetenceCertificateTypes.Certificate,
    userId = 12345L,
    companyId = companyId
  )
  val certificate2 = CompetenceCertificate(
    id = None,
    year = Some("2012-2013"),
    title = "certificate2title",
    description = "Description1",
    tpe = CompetenceCertificateTypes.Certificate,
    userId = 54321L,
    companyId = companyId
  )
}

class CompetenceCertificateRepositoryTest extends FunSpec with H2Configuration with CertificateTestData with BeforeAndAfterEach {
  override def beforeEach(): Unit = {
    new CompetenceTableCreator().reCreateTables()
  }

  val certificateRepository = new CompetenceCertificateRepositoryImpl(
    h2Configuration.inject[JdbcBackend#DatabaseDef](None),
    h2Configuration.inject[JdbcProfile](None)
  )
  describe("CompetenceCertificateRepository"){
    it("should getCertificates by title"){
      certificateRepository.create(certificate1)
      certificateRepository.create(certificate1)
      certificateRepository.create(certificate2)

      assert(certificateRepository.getBy(CertificateFilter(companyId, titlePattern = Some(certificate1.title))).length == 2)
      assert(certificateRepository.getBy(CertificateFilter(companyId, titlePattern = Some(certificate2.title))).length == 1)
    }

    it("should getCertificates by userId"){
      certificateRepository.create(certificate1)
      certificateRepository.create(certificate1)
      certificateRepository.create(certificate2)

      assert(certificateRepository.getBy(CertificateFilter(companyId, userId = Some(certificate1.userId))).length == 2)
      assert(certificateRepository.getBy(CertificateFilter(companyId, userId = Some(certificate2.userId))).length == 1)
    }
  }

}
