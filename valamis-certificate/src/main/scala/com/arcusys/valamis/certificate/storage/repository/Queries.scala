package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage.schema._
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.jdbc.JdbcBackend

private[repository] trait Queries extends SlickProfile
  with CertificateTableComponent
  with CertificateStateTableComponent
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

    def filterBy(filter: CertificateStateFilter)(implicit session: JdbcBackend#Session) = {
      val collection = q
      val userIdFiltered =
        if (filter.userId.isDefined) collection.filter(_.userId === filter.userId.get)
        else collection
      val certificateIdFiltered =
        if (filter.certificateId.isDefined) userIdFiltered.filter(_.certificateId === filter.certificateId.get)
        else userIdFiltered
      val statusFiltered =
        if (filter.statuses.nonEmpty) certificateIdFiltered.filter(_.status inSet filter.statuses)
        else certificateIdFiltered
      statusFiltered
    }
  }

  implicit class CertificateQueryExtensions(q: Query[CertificateTable, CertificateTable#TableElementType, Seq]) {

    def skipTake(skipTake: Option[SkipTake])(implicit session: JdbcBackend#Session) = {
      skipTake match {
        case Some(SkipTake(skip, take)) => q.drop(skip).take(take)
        case _ => q
      }
    }

    def filterBy(filter: CertificateFilter)(implicit session: JdbcBackend#Session) = {

      val companyIdFiltered = q.filter(_.companyId === filter.companyId)

      val titleFiltered = if (filter.titlePattern.isDefined && filter.titlePattern.get.nonEmpty)
        companyIdFiltered.filter(_.title.toLowerCase.like(likePattern(filter.titlePattern.get.toLowerCase)))
      else companyIdFiltered

      val publishedFiltered =
        if (filter.isPublished.isDefined)
          titleFiltered.filter(_.isPublished === filter.isPublished.get)
        else
          titleFiltered

      val scopeFiltered = filter.scope match {
        case None => publishedFiltered
        case Some(scopeValue) => publishedFiltered.filter(_.scope === scopeValue)
      }

      if (filter.sortBy.isDefined) filter.sortBy.get match {
        case CertificateSort(CertificateSortBy.CreationDate, Order.Asc) => scopeFiltered.sortBy(_.createdAt)
        case CertificateSort(CertificateSortBy.CreationDate, Order.Desc) => scopeFiltered.sortBy(_.createdAt.desc)
        case CertificateSort(CertificateSortBy.Name, Order.Asc) => scopeFiltered.sortBy(_.title)
        case CertificateSort(CertificateSortBy.Name, Order.Desc) => scopeFiltered.sortBy(_.title.desc)
        case _ => throw new NotImplementedError("certificate sort")
      } else scopeFiltered
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

    def addItemsCounts = {
      q.map(x => (
        x,
        certificateStates.filter(_.certificateId === x.id).length,
        courseGoals.filter(_.certificateId === x.id).length,
        statementGoals.filter(_.certificateId === x.id).length,
        activityGoals.filter(_.certificateId === x.id).length,
        packageGoals.filter(_.certificateId === x.id).length,
        assignmentGoals.filter(_.certificateId === x.id).length
        ))
    }
  }
}