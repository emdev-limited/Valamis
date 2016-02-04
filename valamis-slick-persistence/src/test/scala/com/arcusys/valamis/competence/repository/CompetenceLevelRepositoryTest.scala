package com.arcusys.valamis.competence.repository

import com.arcusys.valamis.competence.model.CompetenceLevel
import org.scalatest.{BeforeAndAfterEach, FunSpec}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

trait CompetenceLevelTestData{
  val companyId = 10157
  val competenceLevelWithDefinedValue = CompetenceLevel(
    title = "title",
    description = "description",
    value = Some(3),
    companyId = companyId
  )
}

class CompetenceLevelRepositoryTest extends FunSpec with H2Configuration with CompetenceLevelTestData with BeforeAndAfterEach {
  override def beforeEach(): Unit = {
    new CompetenceTableCreator().reCreateTables()
  }

  val competenceLevelRepository = new CompetenceLevelRepositoryImpl(
    h2Configuration.inject[JdbcBackend#DatabaseDef](None),
    h2Configuration.inject[JdbcProfile](None)
  )

  describe("CompetenceLevelRepository"){
    it("max value of empty table == 0"){
      assert(competenceLevelRepository.getValueMaxValue == 0)
    }

    it("should get maxValue of value"){
      competenceLevelRepository.create(competenceLevelWithDefinedValue)

      assert(competenceLevelRepository.getValueMaxValue == competenceLevelWithDefinedValue.value.get)
    }
  }
}
