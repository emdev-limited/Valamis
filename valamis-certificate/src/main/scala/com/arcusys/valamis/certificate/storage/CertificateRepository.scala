package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.model.SkipTake

trait CertificateRepository {
  private[certificate] def create(certificate: Certificate): Certificate
  private[certificate] def update(certificate: Certificate): Certificate
  private[certificate] def delete(id: Long)

  def getById(id: Long): Certificate
  def getByIdOpt(id: Long): Option[Certificate]
  def getByIds(ids: Set[Long]): Seq[Certificate]
  def getByIdWithItemsCount(id: Long): Option[(Certificate, CertificateItemsCount)]

  def getBy(filter: CertificateFilter, skipTake: Option[SkipTake] = None): Seq[Certificate]
  def getByState(filter: CertificateFilter, stateFilter: CertificateStateFilter, skipTake: Option[SkipTake] = None): Seq[Certificate]
  def getWithStatBy(filter: CertificateFilter, skipTake: Option[SkipTake] = None): Seq[(Certificate, CertificateUsersStatistic)]
  def getWithItemsCountBy(filter: CertificateFilter, skipTake: Option[SkipTake]): Seq[(Certificate, CertificateItemsCount)]
  def getCountBy(filter: CertificateFilter): Int

  def getGoalsMaxArrangementIndex(certificateId: Long): Int
}