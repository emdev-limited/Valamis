package com.arcusys.valamis.slide.service.lti

import com.arcusys.valamis.slide.service.lti.model.LTIData

import scala.concurrent.Future

trait LTIDataService {
  def get(uuid: String): Future[Option[LTIData]]

  def add(data: LTIData): Future[Unit]

  def delete(uuid: String): Future[Int]
}


