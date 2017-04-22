package com.arcusys.valamis.web.servlet.certificate

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.services.{CompanyHelper, ServiceContextHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName, ServiceContextFactoryHelper, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model.CertificateFilter
import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{Context, Order, Period, RangeResult}
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateResponseFactory
import com.arcusys.valamis.web.servlet.certificate.request.{CertificateRequest, CertificateStatementGoalRequest}
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.arcusys.valamis.web.servlet.user.UserFacadeContract
import org.joda.time.DateTime
import org.json4s.Formats

class CertificateServlet
  extends BaseJsonApiController
  with CertificatePolicy
  with CertificateResponseFactory {

  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateGoalService = inject[CertificateGoalService]
  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val certificateBadgeService = inject[CertificateBadgeService]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateMemberService = inject[CertificateMemberService]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val req = CertificateRequest(this)

  override implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  implicit def context = Context(CompanyHelper.getCompanyId, PermissionUtil.getCourseIdFromRequest, PermissionUtil.getUserId)
  private def sortBy = Some(CertificateSort(req.sortBy, Order(req.ascending)))

  private implicit def locale = UserLocalServiceHelper().getUser(PermissionUtil.getUserId).getLocale

  get("/certificates(/)") {
    val filter = CertificateFilter(
      PermissionUtil.getCompanyId,
      Some(req.filter),
      req.scopeId,
      req.isActive,
      sortBy
    )

    val certificates = req.additionalData match {
      case Some("usersStatistics") =>
        certificateRepository
          .getWithStatBy(filter, req.skipTake)
          .map(c => toCertificateWithUserStatisticsResponse(c._1,c._2))
      case Some("itemsCount") =>
        certificateRepository
          .getWithItemsCountBy(filter, req.skipTake, isDeleted = None)
          .map(toCertificateShortResponse)
      case None =>
        certificateRepository.getBy(filter, req.skipTake)
      case _ => throw new BadRequestException
    }

    val count = certificateRepository.getCountBy(filter)

    CollectionResponse(req.page, certificates, count)
  }

  get("/certificates/users/:userId") {
    val result = if (req.available) {
      certificateUserService.getAvailableCertificates(
        req.userId,
        CertificateFilter(
          getCompanyId,
          req.textFilter,
          req.scopeId,
          isActive = Some(true),
          sortBy = sortBy
        ),
        req.skipTake
      ).map(toCertificateResponse(req.isShortResult))
    } else {
      if (req.withOpenBadges)
        certificateUserService.getCertificatesByUserWithOpenBadges(
          req.userId,
          getCompanyId.toInt,
          req.ascending,
          req.skipTake,
          req.textFilter
        ).map(toCertificateResponse(req.isShortResult))
      else
        certificateUserService.getByUser(
          req.userId,
          CertificateFilter(
            getCompanyId,
            req.textFilter,
            req.scopeId,
            isActive = Some(true),
            sortBy = sortBy
          ),
          req.isAchieved,
          req.skipTake
        ).map(toCertificateWithUserStatusResponse(req.userId))
    }
    RangeResult(result.total, result.records)
  }

  get("/certificates/:id(/)") {
    val c = certificateRepository.getById(req.id)
    val userId = PortalUtilHelper.getUserId(request)
    toCertificateWithUserStatusResponse(userId)(c)
  }

  get("/certificates/:id/logo") {
    val content = certificateService.getLogo(req.id)
      .getOrElse(halt(HttpServletResponse.SC_NOT_FOUND, s"Certificate with id: ${req.id} doesn't exist"))

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    response.getOutputStream.write(content)
    content
  }

  get("/certificates/:id/issue_badge(/:model)"){
    Symbol(params.getOrElse("model", "none")) match {
      case 'none => certificateBadgeService.getIssuerBadge(req.id, req.userId, req.rootUrl)

      case 'badge => certificateBadgeService.getBadgeModel(req.id, req.rootUrl)

      case 'issuer => certificateBadgeService.getIssuerModel(req.rootUrl)

      case _ => throw new BadRequestException
    }
  }

  get("/certificates/:id/member(/)", request.getParameter("action") == "MEMBERS") {
    req.memberType match {
      case MemberTypes.User =>
        certificateMemberService.getUserMembers(
          req.id,
          req.textFilter,
          req.ascending,
          req.skipTake,
          req.orgIdOption
        ).map { cs =>
          userFacade.getUserResponseWithCertificateStatus(cs.user,
            cs.status.map(_.userJoinedDate),
            cs.status.map(_.status))
        }

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

  post("/certificates/:id/do/:action(/)"){
    Symbol(params("action")) match {
      case 'clone =>
        val c = certificateService.clone(req.id)
        certificateRepository.getByIdWithItemsCount(c.id) map toCertificateShortResponse

      case 'activate =>
        certificateService.activate(req.id)
        certificateRepository.getByIdWithItemsCount(req.id) map toCertificateShortResponse

      case 'deactivate =>
        certificateService.deactivate(req.id)
        certificateRepository.getByIdWithItemsCount(req.id) map toCertificateShortResponse

      case _ => throw new BadRequestException
    }
  }

  post("/certificates(/)"){
    val certificate = certificateService.create(
      req.title,
      req.description
    )
    certificateRepository.getByIdWithItemsCount(certificate.id) map toCertificateShortResponse
  }

  //TODO: move all goal related APIs to the GoalServlet
  post("/certificates/:id/:resource(/)"){

    Symbol(params("resource")) match {
      case 'courses => req.courseGoalIds.foreach(courseId =>
        certificateGoalService.addCourseGoal(req.id, courseId))

      case 'members =>
        certificateUserService.addMembers(req.id, req.memberIds, req.memberType)

      case Symbol("current-user") =>
        certificateUserService.addUserMember(req.id, PermissionUtil.getLiferayUser.getUserId, req.courseId)

      case 'activity => certificateGoalService.addActivityGoal(req.id, req.activityName, 1)

      case 'activities => req.activityNames.foreach(activity =>
        certificateGoalService.addActivityGoal(req.id, activity, 1))

      case 'statement => certificateGoalService.addStatementGoal(req.id, req.tincanVerb, req.tincanObject)

      case 'statements =>
        val items = JsonHelper.fromJson[Seq[CertificateStatementGoalRequest]](req.tincanStatements)
        items.foreach(st => certificateGoalService.addStatementGoal(req.id, st.verb, st.obj))

      case 'package => certificateGoalService.addPackageGoal(req.id, req.packageId)

      case 'packages => req.packageIds.foreach(certificateGoalService.addPackageGoal(req.id, _))

      case 'assignment => certificateGoalService.addAssignmentGoal(req.id, req.assignmentId)

      case 'assignments => req.assignmentIds.foreach(certificateGoalService.addAssignmentGoal(req.id, _))

      case _ => throw new BadRequestException
    }
  }

  post("/certificates/:id/goals/indexes(/)"){
    certificateGoalService.updateGoalIndexes(req.goalIndexes)
  }

  post("/certificates/:id/group(/)"){
    certificateGoalService.createGoalGroup(req.id, req.userIdOption, req.goalCount, req.goalIds)
  }

  put("/certificates/:id(/)"){
    certificateGoalService.restoreGoals(req.id, req.restoreGoalIds)
    certificateService.update(
      req.id,
      req.title,
      req.description,
      Period(req.periodType, req.periodValue),
      req.isPublishBadge,
      req.shortDescription,
      req.scopeId)

    certificateRepository.getByIdWithItemsCount(req.id) map toCertificateShortResponse
  }

  put("/certificates/:id/goal/:goalId(/)") (
    toCertificateGoalsData(certificateGoalService.updateGoal(
      req.goalId,
      req.periodValue,
      req.periodType,
      req.arrangementIndex,
      req.isOptional,
      req.activityCount,
      req.goalGroupId,
      req.goalOldGroupId,
      Some(req.userId),
      req.isDeleted))
  )

  put("/certificates/:id/group/:groupId(/)") (
    certificateGoalService.updateGoalGroup(
      GoalGroup(
        req.groupId,
        req.goalCount,
        req.id,
        req.periodValue,
        req.periodType,
        req.arrangementIndex,
        DateTime.now,
        Some(req.userId),
        req.isDeleted),
      req.deleteContent).map(toGoalGroupResponse)
  )

  put("/certificates/:id/group/:groupId/goals(/)") (
    certificateGoalService.updateGoalsInGroup(req.groupId, req.goalOldGroupId, req.goalIds)
  )

  delete("/certificates/:id(/)")(
    certificateService.delete(params("id").toLong)
  )

  delete("/certificates/:id/members(/)") (
    certificateUserService.deleteMembers(req.id, req.memberIds, req.memberType)
  )

  delete("/certificates/:id/current-user(/)") (
    certificateUserService.deleteMembers(req.id, Seq(PermissionUtil.getLiferayUser.getUserId), MemberTypes.User)
  )
}