package com.arcusys.valamis.competence.repository

import com.arcusys.valamis.competence.model.{CategoryFilter, CategoryEntity}

import org.scalatest.{BeforeAndAfterEach, FunSpec}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

trait CategoryTestData{
  val companyId = 10157
  val categoryWithDefinedOrder = CategoryEntity(
    title = "titleDefinedOrder",
    companyId = companyId,
    order = Some(3),
    parentCategoryId = None
  )
  val categoryWithUndefinedOrder = CategoryEntity(
    title = "titleUndefinedOrder",
    companyId = companyId,
    order = None,
    parentCategoryId = None
  )
}

class CategoryRepositoryTest extends FunSpec with H2Configuration with CategoryTestData with BeforeAndAfterEach {
  override def beforeEach(): Unit = {
    new CompetenceTableCreator().reCreateTables()
  }

  val categoryRepository = new CategoryRepositoryImpl(
    h2Configuration.inject[JdbcBackend#DatabaseDef](None),
    h2Configuration.inject[JdbcProfile](None)
  )
  describe("CategoryRepository"){
    it("First category order == 1"){
      val created = categoryRepository.create(categoryWithUndefinedOrder)
      assert(created.order == Some(1))
    }

    it("Undefined order should be set as maxOrder in directory"){
      categoryRepository.create(categoryWithUndefinedOrder)
      val created = categoryRepository.create(categoryWithUndefinedOrder)

      assert(created.order == Some(2))
    }

    it("Defined order should be preserved"){
      val created = categoryRepository.create(categoryWithDefinedOrder)

      assert(categoryWithDefinedOrder.order == created.order)
    }

    it("first subcategory order == 1"){
      val firstCategory = categoryRepository.create(categoryWithUndefinedOrder)
      val created = categoryRepository.create(categoryWithUndefinedOrder.copy(parentCategoryId = firstCategory.id))

      assert(created.order == Some(1))
    }

    it("should filter by title"){
      val firstCategory = categoryRepository.create(categoryWithUndefinedOrder)
      categoryRepository.create(categoryWithDefinedOrder)
      categoryRepository.create(categoryWithUndefinedOrder.copy(parentCategoryId = firstCategory.id))

      val titleUndefined = categoryRepository.getBy(CategoryFilter(companyId, titlePattern = Some(categoryWithDefinedOrder.title)))
      assert(titleUndefined.length == 1)

      val titleDefined = categoryRepository.getBy(CategoryFilter(companyId, titlePattern = Some(categoryWithUndefinedOrder.title)))
      assert(titleDefined.length == 2)
    }

    it("should get toplevel categories"){
      val firstCategory = categoryRepository.create(categoryWithUndefinedOrder)
      categoryRepository.create(categoryWithDefinedOrder)
      categoryRepository.create(categoryWithUndefinedOrder.copy(parentCategoryId = firstCategory.id))

      assert(categoryRepository.getBy(CategoryFilter(companyId, parentCategoryId = Some(None))).length == 2)
    }

    it("should retrieve CategoryChildern"){
      val firstCategory = categoryRepository.create(categoryWithUndefinedOrder)
      val secondCategory = categoryRepository.create(categoryWithDefinedOrder)
      val subCategory = categoryRepository.create(categoryWithUndefinedOrder.copy(parentCategoryId = firstCategory.id))

      val categoryChildren = categoryRepository.getBy(CategoryFilter(companyId, parentCategoryId = Some(firstCategory.id)))
      assert(categoryChildren.length == 1)
      assert(categoryChildren.head == subCategory)

      assert(categoryRepository.getBy(CategoryFilter(companyId, parentCategoryId = Some(secondCategory.id))).length == 0)
      assert(categoryRepository.getBy(CategoryFilter(companyId, parentCategoryId = Some(subCategory.id))).length == 0)
    }
  }
}
