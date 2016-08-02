package com.arcusys.valamis.lesson.tincan.storage

import com.arcusys.valamis.lesson.tincan.model.LessonCategoryGoal
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait LessonCategoryGoalTableComponent extends LongKeyTableComponent with TypeMapper {self:SlickProfile =>
  import driver.simple._

  class PackageCategoryGoalTable(tag : Tag) extends LongKeyTable[LessonCategoryGoal](tag, "PACKAGE_CATEGORY_GOAL") {
    def lessonId = column[Long]("PACKAGE_ID") //TODO: lesson id
    def name = column[String]("NAME")
    def category = column[String]("CATEGORY")
    def count = column[Int]("COUNT")

    def * = (lessonId, name, category, count, id.?) <> (LessonCategoryGoal.tupled, LessonCategoryGoal.unapply)

    def update = (lessonId, name, category, count) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val lessonCategoryGoals = TableQuery[PackageCategoryGoalTable]
}
