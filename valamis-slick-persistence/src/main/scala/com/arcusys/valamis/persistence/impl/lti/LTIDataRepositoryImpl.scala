package com.arcusys.valamis.persistence.impl.lti

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.lti.schema.LTIDataTableComponent
import com.arcusys.valamis.slide.service.lti.model.LTIData
import com.arcusys.valamis.slide.storage.LTIDataRepository
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
  * Created by Igor Borisov on 27.03.17.
  */
class LTIDataRepositoryImpl(val driver: JdbcProfile)
  extends LTIDataRepository
    with SlickProfile
    with LTIDataTableComponent {

  import driver.api._

  override def get(uuid: String): DBIO[Option[LTIData]] = {
    val query = ltiDatas.filter(_.uuid.toLowerCase === uuid.toLowerCase)
    query.result.headOption
  }

  override def create(ltiData: LTIData): DBIO[Int] =
    ltiDatas += ltiData

  override def delete(uuid: String): DBIO[Int] =
    ltiDatas.filter(_.uuid.toLowerCase === uuid.toLowerCase).delete

}
