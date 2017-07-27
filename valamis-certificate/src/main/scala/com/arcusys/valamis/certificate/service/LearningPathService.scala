package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{LPInfo, LPInfoWithUserStatus, LearningPathWithVersion}
import org.joda.time.DateTime

import scala.concurrent.Future

trait LearningPathService {
  def isLearningPathDeployed: Boolean

  def getUsersToCertificateCount(since: DateTime,
                                 until: DateTime,
                                 companyId: Long): Future[Map[Long, Int]]

  def getLearningPathById(id: Long, companyId: Long): Future[Option[LearningPathWithVersion]]

  def getLearningPathsByIds(ids: Seq[Long]): Future[Seq[LPInfo]]

  def getLearningPathsWithUserStatusByIds(ids: Seq[Long], userId: Long): Future[Seq[LPInfoWithUserStatus]]

  def getPassedLearningPaths(userId: Long, companyId: Long): Future[Seq[LPInfoWithUserStatus]]
}