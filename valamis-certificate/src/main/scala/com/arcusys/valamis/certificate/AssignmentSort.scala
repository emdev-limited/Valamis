package com.arcusys.valamis.certificate

import com.arcusys.valamis.certificate.model.AssignmentSortBy
import com.arcusys.valamis.model.{Order, SortBy}

case class AssignmentSort(sortBy: AssignmentSortBy.AssignmentSortBy, order: Order.Value) extends SortBy(sortBy, order)