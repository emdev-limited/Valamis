package com.arcusys.learn.models.request

import java.util.UUID

import com.arcusys.learn.models.request.GradebookActionType._
import com.arcusys.learn.service.AntiSamyHelper
import com.arcusys.valamis.gradebook.model.GradebookUserSortBy
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.scalatra.ScalatraBase

import scala.util.Try

object GradebookRequest extends BaseCollectionFilteredRequest with BaseRequest {

  val StudentId = "studentId"
  val StudentNameFilter = "studentName"
  val OrganisationNameFilter = "organizationName"
  val ResultAs = "resultAs"
  val SelectedPackages = "selectedPackages"
  val Grade = "totalGrade"
  val GradeComment = "gradeComment"
  val StatementId = "statementId"
  val StatementGrade = "statementGrade"
  val PackageId = "packageId"
  val StudyCourseId = "studyCourseId"
  val WithStatements = "withStatements"
  val Completed = "completed"
  val PackagesCount  = "packagesCount"

  val SHORT_RESULT_VALUE = "short"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase)
    extends BaseSortableCollectionFilteredRequestModel(scalatra, GradebookUserSortBy.apply) {

    def actionType: GradebookActionType = GradebookActionType.withName(Parameter(Action).required.toUpperCase)

    def studentId = Parameter(StudentId).intRequired

    def userIdServer = PermissionUtil.getUserId

    def courseId = Parameter(CourseId).intRequired

    def studyCourseId = Parameter(StudyCourseId).intRequired

    def withStatements = Parameter(WithStatements).booleanOption.getOrElse(true)

    def organizationName = Parameter(OrganisationNameFilter).option match {
      case Some(value) => value
      case None        => ""
    }

    def studentNameFilter = Parameter(StudentNameFilter).option.filter(_.nonEmpty)

    def isShortResult = Parameter(ResultAs).option match {
      case Some(value) => value != "detailed"
      case None        => true
    }

    def selectedPackages = Parameter(SelectedPackages).multiWithEmpty.map(x => x.toLong)

    def gradeComment = Parameter(GradeComment).option.map(AntiSamyHelper.sanitize)

    // TODO: check what will be send from browser, remove AntiSamyHelper
    def grade = Try(AntiSamyHelper.sanitize(Parameter(Grade).required).toFloat).toOption

    def statementId = UUID.fromString(Parameter(StatementId).required)

    def packageId = Parameter(PackageId).intRequired

    def statementGrade = Parameter(StatementGrade).intRequired

    def isCompleted =  Parameter(Completed).booleanRequired

    def packagesCount = Parameter(PackagesCount).intRequired
  }
}

