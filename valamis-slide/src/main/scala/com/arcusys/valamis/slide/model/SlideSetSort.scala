package com.arcusys.valamis.slide.model

import com.arcusys.valamis.model.{Order, SortBy}

case class SlideSetSort(sortBy: SlideSetSortBy.SlideSetSortBy, order: Order.Value) extends SortBy(sortBy, order)