package com.arcusys.valamis.competence.repository

import com.arcusys.valamis.competence.model.{SkillFilter, Skill}
import org.scalatest.{BeforeAndAfterEach, FunSpec}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SkillRepositoryTest extends FunSpec with H2Configuration with CategoryTestData with BeforeAndAfterEach {
  override def beforeEach(): Unit = {
    new CompetenceTableCreator().reCreateTables()
  }

  val categoryRepository = new CategoryRepositoryImpl(
    h2Configuration.inject[JdbcBackend#DatabaseDef](None),
    h2Configuration.inject[JdbcProfile](None)
  )
  val skillRepository = new SkillRepositoryImpl(
    h2Configuration.inject[JdbcBackend#DatabaseDef](None),
    h2Configuration.inject[JdbcProfile](None)
  )
  describe("SkillRepository"){
    it("getByTitle"){
      val category1 = categoryRepository.create(categoryWithUndefinedOrder)
      val category2 = categoryRepository.create(categoryWithUndefinedOrder)

      val skill1Cat1 = Skill(
        title = "skill1title",
        description = "skill1description",
        companyId = companyId,
        parentCategoryId = category1.id.get
      )

      val skill1Cat2 = Skill(
        title = "skill1title",
        description = "skill1description",
        companyId = companyId,
        parentCategoryId = category2.id.get
      )

      val skill2Cat1 = Skill(
        title = "skill2title",
        description = "",
        companyId = companyId,
        parentCategoryId = category1.id.get
      )

      skillRepository.create(skill1Cat1)
      skillRepository.create(skill1Cat2)
      skillRepository.create(skill2Cat1)

      assert(skillRepository.getBy(SkillFilter(companyId, titlePattern = Some(skill1Cat1.title))).length == 2)
      assert(skillRepository.getBy(SkillFilter(companyId, titlePattern = Some(skill2Cat1.title))).length == 1)
    }

    it("getCategoryChildren"){
      val category1 = categoryRepository.create(categoryWithUndefinedOrder)
      val category2 = categoryRepository.create(categoryWithUndefinedOrder)

      val skill1Cat1 = Skill(
        title = "skill1title",
        description = "skill1description",
        companyId = companyId,
        parentCategoryId = category1.id.get
      )

      val skill1Cat2 = Skill(
        title = "skill1title",
        description = "skill1description",
        companyId = companyId,
        parentCategoryId = category2.id.get
      )

      val skill2Cat1 = Skill(
        title = "skill2title",
        description = "",
        companyId = companyId,
        parentCategoryId = category1.id.get
      )

      skillRepository.create(skill1Cat1)
      skillRepository.create(skill1Cat2)
      skillRepository.create(skill2Cat1)

      assert(skillRepository.getBy(SkillFilter(companyId, parentCategoryId = category1.id)).length == 2)
      assert(skillRepository.getBy(SkillFilter(companyId, parentCategoryId = category2.id)).length == 1)
    }
  }
}
