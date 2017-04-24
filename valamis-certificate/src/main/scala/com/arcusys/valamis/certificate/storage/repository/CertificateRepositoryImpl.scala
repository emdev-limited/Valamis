package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.certificate.storage.schema._
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.model.{PeriodTypes, RangeResult, SkipTake}
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class CertificateRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateRepository
    with SlickProfile
    with DatabaseLayer
    with CertificateTableComponent
    with CertificateStateTableComponent
    with CertificateGoalGroupTableComponent
    with CertificateGoalTableComponent
    with Queries {

  import driver.api._

  private type CertificateQuery = Query[CertificateTable, Certificate, Seq]

  override def create(certificate: Certificate): Certificate = {
    val insertQ = (certificates returning certificates.map(_.id)) += certificate

    val action = insertQ flatMap { id =>
      certificates.filter(_.id === id).result.head
    }

    execSync(action.transactionally)
  }

  override def update(certificate: Certificate): Certificate = {
    val certificateQ = certificates.filter(_.id === certificate.id)

    val updateQ = certificateQ.map(_.update).update(certificate)

    val action = updateQ andThen certificateQ.result.head
    execSync(action.transactionally)
  }

  override def updateLogo(certificateId: Long, logo: String): Unit = {
    val action = certificates
      .filter(_.id === certificateId)
      .map(_.logo)
      .update(logo)

    execSync(action)
  }

  override def updateIsActive(certificateId: Long, isActive: Boolean): Unit = {
    val query = certificates.filter(_.id === certificateId)
    val action = if(isActive) {
      query
        .map(c => (c.isActive, c.activationDate))
        .update((isActive, Some(DateTime.now)))
    } else {
      query.map(_.isActive).update(isActive)
    }

    execSync(action)
  }

  override def delete(id: Long): Unit = {
    val deleteGoalsQ = certificateGoals.filterByCertificateId(id).delete
    val deleteCertificateQ = certificates.filter(_.id === id).delete

    val action = DBIO.seq(deleteGoalsQ, deleteCertificateQ)
    execSync(action.transactionally)
  }

  override def getById(id: Long): Certificate = {
    getByIdOpt(id) match {
      case Some(c) => c
      case None => throw new EntityNotFoundException(s"no certificate with id: $id")
    }
  }

  override def getByIdWithItemsCount(id: Long,
                                     isDeleted: Option[Boolean]): Option[(Certificate, CertificateItemsCount)] = {
    val action = certificates.filter(_.id === id)
      .addItemsCounts(isDeleted)
      .result
      .headOption

    execSync(action)
      .map(x => (x._1, CertificateItemsCount(x._2, x._3, x._4, x._5, x._6, x._7, x._8)))
  }

  override def getByIdOpt(id: Long): Option[Certificate] = {
    val action = certificates.filter(_.id === id).result.headOption

    execSync(action)
  }

  override def getByIds(ids: Seq[Long]): Seq[Certificate] = {
    if (ids.isEmpty) {
      Seq()
    }
    else {
      val action = certificates.filter(_.id inSet ids).result
      execSync(action)
    }
  }

  override def getBy(filter: CertificateFilter,
                     skipTake: Option[SkipTake]): Seq[Certificate] = {

    val action = certificates
      .filterBy(filter)
      .skipTake(skipTake)
      .result

    execSync(action)
  }

  override def getAvailable(filter: CertificateFilter,
                            skipTake: Option[SkipTake],
                            userId: Long): RangeResult[Certificate] = {

    val statesQ = certificateStates.filterByUserId(userId)

    val certificateIds = certificates
      .join(statesQ).on(_.id === _.certificateId)
      .groupBy(_._1)
      .map(_._1.id)

    val action = certificates
      .filterNot(_.id in certificateIds)
      .filterBy(filter)

    getRangeResult(action, skipTake)
  }

  override def getByUser(filter: CertificateFilter,
                         userId: Long,
                         isAchieved: Option[Boolean],
                         skipTake: Option[SkipTake]): RangeResult[Certificate] = {

    val stateFilter = CertificateStateFilter(userId = Some(userId))

    val query = getQueryByUser(filter, stateFilter, isAchieved, skipTake)

    getRangeResult(query, skipTake)
  }

  override def getByUser(filter: CertificateFilter,
                         stateFilter: CertificateStateFilter,
                         isAchieved: Option[Boolean],
                         skipTake: Option[SkipTake]): RangeResult[Certificate] = {

    val query = getQueryByUser(filter, stateFilter, isAchieved, skipTake)

    getRangeResult(query, skipTake)
  }

  private def getQueryByUser(filter: CertificateFilter,
                             stateFilter: CertificateStateFilter,
                             isAchieved: Option[Boolean],
                             skipTake: Option[SkipTake]): CertificateQuery = {

    val stateFiltered = certificates.getByStates(stateFilter, scopeId = filter.scope)

    // Combine active certificates that user is joined to with the ones achieved by the user
    // Need to move sortBy to the end of the query, because otherwise it breaks "union"
    val query = (isAchieved map { x =>
      val achievedStateFilter = stateFilter.copy(statuses = Set(CertificateStatuses.Success), containsStatuses = x)
      val achievedFiltered = certificates.getByStates(achievedStateFilter, scopeId = filter.scope)

      achievedFiltered union stateFiltered.filterBy(filter.copy(sortBy = None))
    } getOrElse stateFiltered).filterBy(filter)

    query
  }

  override def getByState(filter: CertificateFilter, stateFilter: CertificateStateFilter): Seq[Certificate] = {
    val action = certificates
      .getByStates(stateFilter)
      .filterBy(filter)
      .result

    execSync(action)
  }

  private def getRangeResult(query: CertificateQuery, skipTake: Option[SkipTake]): RangeResult[Certificate] = {
    val countF = db.run(query.length.result)
    val itemsF = db.run(query.skipTake(skipTake).result)

    val resultF = for {
      count <- countF
      items <- itemsF
    } yield {
      RangeResult(count, items)
    }

    Await.result(resultF, dbTimeout)
  }

  override def getWithUserState(companyId: Long,
                                userId: Long,
                                certificateState: CertificateStatuses.Value): Seq[(Certificate, CertificateState)] = {
    val stateQuery = certificateStates
      .filterByUserId(userId)
      .filter(_.status === certificateState)

    val action =
      certificates
        .filter(_.companyId === companyId)
        .join(stateQuery)
        .on((certificate, state) => certificate.id === state.certificateId)


    execSync(action.result)
  }

  override def getWithStatBy(filter: CertificateFilter,
                             skipTake: Option[SkipTake]
                            ): Seq[(Certificate, CertificateUsersStatistic)] = {

    val action = certificates
      .filterBy(filter)
      .skipTake(skipTake)
      .addStatusesCounts
      .result

    execSync(action)
      .map(x => (x._1, CertificateUsersStatistic(x._2, x._3, x._4, x._5)))
  }

  override def getWithItemsCountBy(filter: CertificateFilter,
                                   skipTake: Option[SkipTake],
                                   isDeleted: Option[Boolean]
                                  ): Seq[(Certificate, CertificateItemsCount)] = {
    val action = certificates
      .filterBy(filter)
      .skipTake(skipTake)
      .addItemsCounts(isDeleted)
      .result

      execSync(action)
        .map(x => (x._1, CertificateItemsCount(x._2, x._3, x._4, x._5, x._6, x._7, x._8)))
  }

  override def getCountBy(filter: CertificateFilter): Int = {
    val action = certificates
      .filterBy(filter)
      .length
      .result

    execSync(action)
  }

  def getGoalsMaxArrangementIndex(certificateId: Long): Int = {
    val goalsQ = certificateGoals.filterByCertificateId(certificateId).map(_.arrangementIndex)
    val groupQ = certificateGoalGroups.filterByCertificateId(certificateId).map(_.arrangementIndex)

    val action = (goalsQ union groupQ).max.result

    execSync(action) getOrElse 0
  }

  override def getNotTypeWithStatus(periodType: PeriodTypes.Value,
                                    status: Seq[CertificateStatuses.Value],
                                    isActive: Boolean = true): Seq[(Certificate, CertificateState)] = {
    val stateQuery = certificateStates
      .filter(_.status inSet status)

    val query =
      certificates
        .filterNot(_.validPeriodType === periodType)
        .filter(_.isActive === isActive)
        .join(stateQuery)
        .on((certificate, state) => certificate.id === state.certificateId)

    execSync(query.result)
  }
}
