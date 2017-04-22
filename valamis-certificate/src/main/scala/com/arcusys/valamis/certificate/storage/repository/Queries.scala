package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal.{CertificateGoal, GoalGroup}
import com.arcusys.valamis.certificate.storage.schema._
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.persistence.common.DbNameUtils.likePattern
import com.arcusys.valamis.persistence.common.SlickProfile

private[repository] trait Queries
  extends SlickProfile
    with CertificateTableComponent
    with CertificateStateTableComponent
    with CertificateGoalStateTableComponent
    with CourseGoalTableComponent
    with StatementGoalTableComponent
    with ActivityGoalTableComponent
    with PackageGoalTableComponent
    with AssignmentGoalTableComponent {

  import driver.simple._

  implicit val CertificateStatusTypeMapper = MappedColumnType.base[CertificateStatuses.Value, String](
    s => s.toString,
    s => CertificateStatuses.withName(s)
  )

  implicit class CertificateStateQueryExtensions(q: Query[CertificateStateTable, CertificateStateTable#TableElementType, Seq]) {

    private type CertificateStateQuery = Query[CertificateStateTable, CertificateState, Seq]

    def filterByCertificateId(certificateId: Long): CertificateStateQuery =
      q.filter(_.certificateId === certificateId)

    def filterByUserId(userId: Long): CertificateStateQuery =
      q.filter(_.userId === userId)

    def filterBy(filter: CertificateStateFilter): CertificateStateQuery = {
      val collection = q
      val userIdFiltered = filter.userId match {
        case Some(userId) => collection.filter(_.userId === userId)
        case None => collection
      }
      val certificateIdFiltered = filter.certificateId match {
        case Some(certificateId) => userIdFiltered.filter(_.certificateId === certificateId)
        case None => userIdFiltered
      }
      if (filter.statuses.nonEmpty) {
        if (filter.containsStatuses) {
          certificateIdFiltered.filter(_.status inSet filter.statuses)
        }
        else {
          certificateIdFiltered.filterNot(_.status inSet filter.statuses)
        }
      }
      else certificateIdFiltered
    }
  }

  implicit class CertificateQueryExtensions(q: Query[CertificateTable, CertificateTable#TableElementType, Seq]) {

    private type CertificateQuery = Query[CertificateTable, Certificate, Seq]

    def skipTake(skipTake: Option[SkipTake]): CertificateQuery = {
      skipTake match {
        case Some(SkipTake(skip, take)) => q.drop(skip).take(take)
        case _ => q
      }
    }

    /**
      * Filters the query to the CertificateTable
      *
      * @param filter       The filter that contains companyId,
      *                     may contain titlePattern, scope, isActive, isAchieved.
      *                     If isAchieved is true, join the query, filtered by isActive
      *                     with the query, filtered by isAchieved
      * @return             The filtered query
      */
    def filterBy(filter: CertificateFilter): CertificateQuery = {

      val companyIdFiltered = q.filter(_.companyId === filter.companyId)

      val titleFiltered = if (filter.titlePattern.isDefined && filter.titlePattern.exists(_.nonEmpty))
        companyIdFiltered.filter(_.title.toLowerCase.like(likePattern(filter.titlePattern.get.toLowerCase)))
      else companyIdFiltered

      val activeFiltered = filter.isActive.fold(titleFiltered){isActive =>
        titleFiltered.filter(_.isActive === isActive)
      }

      val scopeFiltered = activeFiltered.filterByScope(filter.scope)

      if (filter.sortBy.isDefined) filter.sortBy.get match {
        case CertificateSort(CertificateSortBy.CreationDate, Order.Asc) => scopeFiltered.sortBy(_.createdAt)
        case CertificateSort(CertificateSortBy.CreationDate, Order.Desc) => scopeFiltered.sortBy(_.createdAt.desc)
        case CertificateSort(CertificateSortBy.Name, Order.Asc) => scopeFiltered.sortBy(_.title)
        case CertificateSort(CertificateSortBy.Name, Order.Desc) => scopeFiltered.sortBy(_.title.desc)
        case _ => throw new NotImplementedError("certificate sort")
      } else scopeFiltered
    }

    def filterByScope(scope: Option[Long]): CertificateQuery = {
      scope match {
        case None => q
        case Some(scopeValue) => q.filter(_.scope === scopeValue)
      }
    }

    def addStatusesCounts = {
      q.map(x => (
        x,
        certificateStates.filter(_.certificateId === x.id).length,
        certificateStates.filter(_.status === CertificateStatuses.Success).filter(_.certificateId === x.id).length,
        certificateStates.filter(_.status === CertificateStatuses.Failed).filter(_.certificateId === x.id).length,
        certificateStates.filter(_.status === CertificateStatuses.Overdue).filter(_.certificateId === x.id).length
        ))
    }

    def addItemsCounts(isDeleted: Option[Boolean]) = {
      val goalQueries = (courseGoals.filterByDeleted(isDeleted).map(_._1),
          statementGoals.filterByDeleted(isDeleted).map(_._1),
          activityGoals.filterByDeleted(isDeleted).map(_._1),
          packageGoals.filterByDeleted(isDeleted).map(_._1),
          assignmentGoals.filterByDeleted(isDeleted).map(_._1)
          )
      q.map(x => (
        x,
        certificateStates.filter(_.certificateId === x.id).length,
        goalQueries._1.filter(_.certificateId === x.id).length,
        goalQueries._2.filter(_.certificateId === x.id).length,
        goalQueries._3.filter(_.certificateId === x.id).length,
        goalQueries._4.filter(_.certificateId === x.id).length,
        goalQueries._5.filter(_.certificateId === x.id).length,
        certificateGoals.filter(_.certificateId === x.id).filter(_.isDeleted).length
      ))
    }

    def getByStates(stateFilter: CertificateStateFilter, scopeId: Option[Long] = None): CertificateQuery = {

      val statesQ = certificateStates.filterBy(stateFilter)

      q.filterByScope(scopeId)
        .join(statesQ).on(_.id === _.certificateId)
        .groupBy(_._1)
        .map(_._1)
    }
  }

  implicit class CertificateGoalQueryExtensions(q: Query[CertificateGoalTable, CertificateGoalTable#TableElementType, Seq]) {

    private type CertificateGoalQuery = Query[CertificateGoalTable, CertificateGoal, Seq]

    def filterById(id: Long): CertificateGoalQuery =
      q.filter(_.id === id)

    def filterNotDeleted: CertificateGoalQuery =
      q.filterNot(_.isDeleted)

    def filterByCertificateId(certificateId: Long): CertificateGoalQuery =
      q.filter(_.certificateId === certificateId)

    def filterByGroupId(groupId: Long): CertificateGoalQuery =
      q.filter(_.groupId === groupId)

    def filterByDeleted(isDeleted: Option[Boolean]) =
      isDeleted.map(d => q.filter(_.isDeleted === d)).getOrElse(q)
  }

  implicit class CertificateGoalGroupQueryExtensions(q: Query[CertificateGoalGroupTable, CertificateGoalGroupTable#TableElementType, Seq]) {

    private type CertificateGoalGroupQuery = Query[CertificateGoalGroupTable, GoalGroup, Seq]

    def filterById(id: Long): CertificateGoalGroupQuery =
      q.filter(_.id === id)

    def filterByCertificateId(certificateId: Long): CertificateGoalGroupQuery =
      q.filter(_.certificateId === certificateId)

    def filterByDeleted(isDeleted: Option[Boolean]): CertificateGoalGroupQuery =
      isDeleted.map(d => q.filter(_.isDeleted === d)).getOrElse(q)
  }

  implicit class CertificateGoalStateQueryExtensions[E, T <: CertificateGoalStateTable](q: Query[T, E, Seq]) {
    private type CertificateGoalStateQuery = Query[T, E, Seq]

    def filterByCertificateId(certificateId: Long): CertificateGoalStateQuery =
      q.filter(_.certificateId === certificateId)

    def filterByUserId(userId: Long): CertificateGoalStateQuery = {
      q.filter(_.userId === userId)
    }

    def filterByDeleted(isDeleted: Option[Boolean]): Query[(T, CertificateGoalTable), (E, CertificateGoalTable#TableElementType), Seq] = {
      val joined = q.join(certificateGoals)
        .on((state, certificateGoal) => state.goalId === certificateGoal.id)
      isDeleted
        .map(d => joined.filter(_._2.isDeleted === isDeleted))
        .getOrElse(joined)
    }
  }

  implicit class CertificateGoalBaseQueryExtensions[E, T <: CertificateGoalBaseColumns](q: Query[T, E, Seq]) {
    def filterByCertificateId(certificateId: Long): Query[T, E, Seq] =
      q.filter(_.certificateId === certificateId)

    def filterByGoalId(goalId: Long): Query[T, E, Seq] =
      q.filter(_.goalId === goalId)

    def filterByDeleted(isDeleted: Option[Boolean]): Query[(T, CertificateGoalTable), (E, CertificateGoalTable#TableElementType), Seq] = {
      val joined = q.join(certificateGoals)
        .on((goal, certificateGoal) => goal.goalId === certificateGoal.id)

      isDeleted.map(d => joined.filter(_._2.isDeleted === d)).getOrElse(joined)
    }
  }
}