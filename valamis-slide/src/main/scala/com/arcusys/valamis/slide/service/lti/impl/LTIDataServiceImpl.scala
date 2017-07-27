package com.arcusys.valamis.slide.service.lti.impl

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slide.service.lti.LTIDataService
import com.arcusys.valamis.slide.service.lti.model.LTIData
import com.arcusys.valamis.slide.storage.LTIDataRepository
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

abstract class LTIDataServiceImpl(slickDBInfo: SlickDBInfo) extends LTIDataService
  with SlickProfile with DatabaseLayer {

  implicit val executionContext:ExecutionContext

  override val driver: JdbcProfile = slickDBInfo.slickDriver

  override def db: JdbcBackend#DatabaseDef = slickDBInfo.databaseDef

  def ltiDataRepository: LTIDataRepository

  override def add(data: LTIData): Future[Unit] =
    db.run(ltiDataRepository.create(data)).map(_ => Unit)

  override def get(uuid: String): Future[Option[LTIData]] = db.run(ltiDataRepository.get(uuid))

  override def delete(uuid: String): Future[Int] = db.run(ltiDataRepository.delete(uuid))
}
