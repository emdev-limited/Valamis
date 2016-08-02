package com.arcusys.valamis.web.servlet.certificate

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model.CertificateFilter
import com.arcusys.valamis.certificate.model.goal.{CertificateGoalsWithGroups, GoalGroup}
import com.arcusys.valamis.certificate.service.{CertificateMemberService, CertificateService}
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateResponseFactory
import com.arcusys.valamis.web.servlet.certificate.request.{CertificateRequest, CertificateStatementGoalRequest}
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.arcusys.valamis.web.servlet.user.UserFacadeContract
import org.json4s.Formats

class CertificateServlet
  extends BaseJsonApiController
  with CertificatePolicy
  with CertificateResponseFactory {

  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateMemberService = inject[CertificateMemberService]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val req = CertificateRequest(this)

  override implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  get("/certificates(/)") {
    val filter = CertificateFilter(PermissionUtil.getCompanyId,
      Some(req.filter),
      req.scopeId,
      req.isPublished,
      Some(CertificateSort(req.sortBy, Order(req.ascending)))
    )

    val certificates = req.additionalData match {
      case Some("usersStatistics") =>
        certificateRepository.getWithStatBy(filter, req.skipTake).map(c => toCertificateWithUserStatisticsResponse(c._1,c._2))
      case Some("itemsCount") =>
        certificateRepository.getWithItemsCountBy(filter, req.skipTake).map(toCertificateShortResponse)
      case None =>
        certificateRepository.getBy(filter, req.skipTake)
      case _ => throw new BadRequestException
    }

    val count = certificateRepository.getCountBy(filter)

    CollectionResponse(req.page, certificates, count)
  }

  get("/certificates/:id(/)") {
    val c = certificateRepository.getById(req.id)
    val userId = PortalUtilHelper.getUserId(request)
    toCertificateWithUserStatusResponse(userId)(c)
  }

  get("/certificates/:id/logo")(action {

    val content = certificateService.getLogo(req.id)
      .getOrElse(halt(HttpServletResponse.SC_NOT_FOUND, s"Certificate with id: ${req.id} doesn't exist"))

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
    content
  })

  get("/certificates/:id/issue_badge(/:model)"){
    Symbol(params.getOrElse("model", "none")) match {
      case 'none => certificateService.getIssuerBadge(req.id, req.userId, req.rootUrl)

      case 'badge => certificateService.getBadgeModel(req.id, req.rootUrl)

      case 'issuer => certificateService.getIssuerModel(req.rootUrl)

      case _ => throw new BadRequestException
    }
  }

  get("/certificates/:id/member(/)", request.getParameter("action") == "MEMBERS") {
    req.memberType match  {
      case MemberTypes.User =>
        certificateMemberService.getUserMembers(req.id, req.textFilter, req.ascending, req.skipTake, req.orgIdOption)
          .map { case (u, cs) => userFacade.getUserResponseWithCertificateStatus(u, cs.userJoinedDate, cs.status)}
      case _ =>
        certificateMemberService.getMembers(req.id, req.memberType, req.textFilter, req.ascending, req.skipTake)
    }
  }

  get("/certificates/:id/member(/)", request.getParameter("action") == "AVAILABLE_MEMBERS") {
    req.memberType match  {
      case MemberTypes.User =>
        certificateMemberService.getAvailableUserMembers(req.id, req.textFilter, req.ascending, req.skipTake, req.orgIdOption)
          .map(u => new UserInfo(u))
      case _ =>
        certificateMemberService.getAvailableMembers(req.id, req.memberType, req.textFilter, req.ascending, req.skipTake)
    }
  }

  get("/certificates/:id/goals")(action {
    val goals = certificateService.getGoals(req.id).flatMap(toCertificateGoalsData)
    val groups = certificateService.getGroups(req.id)
    CertificateGoalsWithGroups(goals, groups)
  })

  post("/certificates/:id/do/:action(/)"){
    Symbol(params("action")) match {
      case 'clone =>
        val c = certificateService.clone(req.id)
        certificateRepository.getByIdWithItemsCount(c.id) map toCertificateShortResponse

      case 'publish =>
        certificateService.publish(req.id, PermissionUtil.getUserId, PermissionUtil.getCourseId)
        certificateRepository.getByIdWithItemsCount(req.id) map toCertificateShortResponse

      case 'unpublish =>
        certificateService.unpublish(req.id)
        certificateRepository.getByIdWithItemsCount(req.id) map toCertificateShortResponse

      case _ => throw new BadRequestException
    }
  }

  post("/certificates(/)"){
    val certificate = certificateService.create(
      PermissionUtil.getCompanyId,
      req.title,
      req.description
    )
    certificateRepository.getByIdWithItemsCount(certificate.id) map toCertificateShortResponse
  }

  post("/certificates/:id/:resource(/)"){

    Symbol(params("resource")) match {
      case 'courses => req.courseGoalIds
        .foreach(courseId => certificateService.addCourseGoal(req.id, courseId))

      case 'member =>
        certificateService.addMembers(req.id, req.memberIds, req.memberType)

      case 'user =>
        certificateService.addUserMember(req.id, req.userId, req.courseId)

      case 'activity => certificateService.addActivityGoal(req.id, req.activityName, 1)

      case 'activities => req.activityNames.foreach(activity =>
        certificateService.addActivityGoal(req.id, activity, 1))

      case 'statement => certificateService.addStatementGoal(req.id, req.tincanVerb, req.tincanObject)

      case 'statements =>
        val items = JsonHelper.fromJson[Seq[CertificateStatementGoalRequest]](req.tincanStatements)
        items.foreach(st => certificateService.addStatementGoal(req.id, st.verb, st.obj))

      case 'package => certificateService.addPackageGoal(req.id, req.packageId)

      case 'packages => req.packageIds.foreach(certificateService.addPackageGoal(req.id, _))

      case 'assignment => certificateService.addAssignmentGoal(req.id, req.assignmentId)

      case 'assignments => req.assignmentIds.foreach(certificateService.addAssignmentGoal(req.id, _))

      case _ => throw new BadRequestException
    }
  }

  post("/certificates/:id/goals/indexes(/)"){
    certificateService.updateGoalIndexes(req.goalIndexes)
  }

  post("/certificates/:id/group(/)"){
    certificateService.createGoalGroup(req.id, req.goalCount, req.goalIds)
  }

  put("/certificates/:id(/)"){
    certificateService.update(
      req.id,
      req.title,
      req.description,
      req.periodType,
      req.periodValue,
      req.isPublishBadge,
      req.shortDescription,
      PermissionUtil.getCompanyId,
      PermissionUtil.getUserId,
      req.scopeId,
      req.optionalGoals)

    certificateRepository.getByIdWithItemsCount(req.id) map toCertificateShortResponse
  }

  put("/certificates/:id/logo(/)") (
    certificateService.changeLogo(req.id, req.logo)
  )

  put("/certificates/:id/goal/:goalId(/)") (
    certificateService.updateGoal(
      req.goalId,
      req.periodValue,
      req.periodType,
      req.arrangementIndex,
      req.isOptional,
      req.activityCount,
      req.goalGroupId)
  )

  put("/certificates/:id/group/:groupId(/)") (
    certificateService.updateGoalGroup(
      GoalGroup(
        req.groupId,
        req.goalCount,
        req.id,
        req.periodValue,
        req.periodType,
        req.arrangementIndex)
    )
  )

  put("/certificates/:id/group/:groupId/goals(/)") (
    certificateService.updateGoalsInGroup(req.groupId, req.goalIds)
  )

  delete("/certificates/:id(/)")(
    certificateService.delete(params("id").toLong)
  )

  delete("/certificates/:id/goal/:goalId(/)") (
    certificateService.deleteGoal(req.goalId)
  )

  delete("/certificates/:id/group/:groupId(/)") (
    certificateService.deleteGoalGroup(req.groupId, req.deletedContent)
  )

  delete("/certificates/:id/members(/)") (
    certificateService.deleteMembers(req.id, req.memberIds, req.memberType)
  )
}