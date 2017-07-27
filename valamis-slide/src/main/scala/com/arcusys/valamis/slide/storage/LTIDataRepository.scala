package com.arcusys.valamis.slide.storage

import com.arcusys.valamis.slide.service.lti.model.LTIData
import slick.dbio.DBIO

trait LTIDataRepository {
  def get(uuid: String): DBIO[Option[LTIData]]
  def create(ltiData: LTIData): DBIO[Int]
  def delete(uuid: String): DBIO[Int]
}
