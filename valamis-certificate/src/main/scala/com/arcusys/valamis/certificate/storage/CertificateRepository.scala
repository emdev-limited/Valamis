package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.model.{PeriodTypes, RangeResult, SkipTake}

trait CertificateRepository {
  private[certificate] def create(certificate: Certificate): Certificate
  private[certificate] def update(certificate: Certificate): Certificate
  private[certificate] def updateLogo(certificateId: Long, logo: String): Unit
  private[certificate] def updateIsActive(certificateId: Long, isActive: Boolean): Unit
  private[certificate] def delete(id: Long): Unit

  def getById(id: Long): Certificate
  def getByIdOpt(id: Long): Option[Certificate]
  def getByIds(ids: Seq[Long]): Seq[Certificate]
  def getByIdWithItemsCount(id: Long,
                            isDeleted: Option[Boolean] = Some(false)): Option[(Certificate, CertificateItemsCount)]
  def getBy(filter: CertificateFilter,
            skipTake: Option[SkipTake] = None): Seq[Certificate]
  def getAvailable(filter: CertificateFilter,
                   skipTake: Option[SkipTake],
                   userId: Long): RangeResult[Certificate]
  def getByUser(filter: CertificateFilter,
                userId: Long,
                isAchieved: Option[Boolean] = None,
                skipTake: Option[SkipTake]): RangeResult[Certificate]
  def getByUser(filter: CertificateFilter,
                stateFilter: CertificateStateFilter,
                isAchieved: Option[Boolean],
                skipTake: Option[SkipTake]): RangeResult[Certificate]
  def getByState(filter: CertificateFilter, stateFilter: CertificateStateFilter): Seq[Certificate]
  def getWithStatBy(filter: CertificateFilter,
                    skipTake: Option[SkipTake] = None): Seq[(Certificate, CertificateUsersStatistic)]
  def getWithItemsCountBy(filter: CertificateFilter,
                          skipTake: Option[SkipTake],
                          isDeleted: Option[Boolean] = Some(false)): Seq[(Certificate, CertificateItemsCount)]
  def getCountBy(filter: CertificateFilter): Int
  def getWithUserState(companyId: Long,
                       userId: Long,
                       certificateState: CertificateStatuses.Value): Seq[(Certificate, CertificateState)]
  def getGoalsMaxArrangementIndex(certificateId: Long): Int

  def getNotTypeWithStatus(periodType: PeriodTypes.Value,
                           status: Seq[CertificateStatuses.Value], isActive: Boolean = true): Seq[(Certificate, CertificateState)]
}