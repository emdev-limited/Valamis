package com.arcusys.valamis.gradebook

import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.user.model.{UserSort, UserSortBy}
import com.arcusys.valamis.util.Joda.dateTimeOrdering

package object utils {

  implicit class LessonWithGradesSortExtension(val items: Seq[LessonWithGrades]) extends AnyVal {

    def sort(sortBy: UserSort): Seq[LessonWithGrades] = {

      sortBy match {
        case UserSort(UserSortBy.LastAttempted, Order.Asc) => items.sortBy(_.lastAttemptedDate)
        case UserSort(UserSortBy.LastAttempted, Order.Desc) => items.sortBy(_.lastAttemptedDate).reverse
        case UserSort(_, Order.Asc) => items.sortBy(_.user.getFullName)
        case UserSort(_, Order.Desc) => items.sortBy(_.user.getFullName).reverse
      }
    }

    def sort(sortBy: Option[UserSort]): Seq[LessonWithGrades] = {
      sortBy match {
        case Some(criterion) => items.sort(criterion)
        case None => items
      }
    }
  }

}
