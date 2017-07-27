package com.arcusys.valamis.web.servlet.public.parameters

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.web.servlet.base.ServletBase

/**
  * Created by pkornilov on 1/30/17.
  */
trait BaseParameters { self: ServletBase =>

  def id = params.as[Long]("id")

  def courseId = params.as[Long]("courseId")
  def size = params.getAs[Int]("size")
  def start = params.getAs[Int]("start")

  def skipTake: Option[SkipTake] = (size, start) match {
    case (None, None) => None
    case (Some(size), Some(start)) =>
      if (start < 1 || size < 1) {
        haltWithBadRequest("start and size parameters should be >= 1")
      } else {
        Some(SkipTake(start-1, size))
      }
      //TODO VALAMIS_API start and size should be used together or not?
    case _ => haltWithBadRequest("start and size should be used together")
  }
}
