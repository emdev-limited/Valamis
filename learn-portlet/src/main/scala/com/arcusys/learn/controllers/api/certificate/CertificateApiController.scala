package com.arcusys.learn.controllers.api.certificate

import javax.servlet.http.HttpServletResponse
import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.exceptions.BadRequestException
import com.arcusys.learn.facades.certificate.CertificateResponseFactory
import com.arcusys.learn.liferay.permission.PermissionUtil
import com.arcusys.learn.models.CertificateStatementGoalRequest
import com.arcusys.learn.models.request.CertificateRequest
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.policies.api.CertificatePolicy
import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.certificate.model.CertificateFilter
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.util.PortalUtil
import org.json4s.Formats

class CertificateApiController
  extends BaseJsonApiController
  with CertificatePolicy
  with CertificateResponseFactory {

  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val req = CertificateRequest(this)

  override implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  get("/certificates(/)") {
    val filter = CertificateFilter(PermissionUtil.getCompanyId,
      Some(req.filter),
      req.scope,
      req.isPublished,
      Some(CertificateSort(req.sortBy, Order(req.isSortDirectionAsc)))
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
    val isJoined = certificateService.hasUser(c.id, PortalUtil.getUserId(request))
    toCertificateResponse(c, Some(isJoined))
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

      case 'move_course => certificateService.reorderCourseGoals(req.id, req.courseGoalIds)

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

      case 'user => certificateService.addUser(req.id, req.userId, true, Some(req.courseId.toLong))

      case 'users => req.userIds.foreach(userId => certificateService.addUser(req.id, userId))

      case 'activity => certificateService.addActivityGoal(req.id, req.activityId, 1)

      case 'activities => req.activityNames.foreach(activity =>
        certificateService.addActivityGoal(req.id, activity, 1))

      case 'statement => certificateService.addStatementGoal(req.id, req.tincanVerb, req.tincanObject)

      case 'statements =>
        val items = JsonHelper.fromJson[Seq[CertificateStatementGoalRequest]](req.tincanStatements)
        items.foreach(st => certificateService.addStatementGoal(req.id, st.verb, st.obj))

      case 'package => certificateService.addPackageGoal(req.id, req.packageId)

      case 'packages => req.packageIds.foreach(certificateService.addPackageGoal(req.id, _))

      case _ => throw new BadRequestException
    }
  }

  put("/certificates/:id(/)"){
    certificateService.update(
      req.id,
      req.title,
      req.description,
      req.validPeriod.valueType,
      req.validPeriod.value,
      req.isPublishBadge,
      req.shortDescription,
      PermissionUtil.getCompanyId,
      PermissionUtil.getUserId,
      req.scope)
  }

  patch("/certificates/:id/:resource(/)"){
    Symbol(params("resource")) match {
      case 'logo => certificateService.changeLogo(req.id, req.logo)

      case 'course => certificateService.changeCourseGoalPeriod(
        req.id,
        req.courseGoalId,
        req.periodValue,
        req.periodType)

      case 'activity => certificateService.changeActivityGoalPeriod(
        req.id,
        req.activityId,
        req.activityCount,
        req.periodValue,
        req.periodType)

      case 'statement => certificateService.changeStatementGoalPeriod(
        req.id,
        req.tincanVerb,
        req.tincanObject,
        req.periodValue,
        req.periodType)

      case 'package => certificateService.changePackageGoalPeriod(
        req.id,
        req.packageId,
        req.periodValue,
        req.periodType)

      case _ => throw new BadRequestException
    }
  }

  delete("/certificates/:id(/)")(
    certificateService.delete(params("id").toLong)
  )

  delete("/certificates/:id/:resource(/)") {
    Symbol(params("resource")) match {
      case 'course => certificateService.deleteCourseGoal(req.id, req.courseGoalId)

      case 'user => certificateService.deleteUser(req.id, req.userId)

      case 'users => for (userId <- req.userIds)
        certificateService.deleteUser(req.id, userId)

      case 'activity =>
        certificateService.deleteActivityGoal(req.id, req.activityId)

      case 'activities => for (activityName <- req.activityNames)
        certificateService.deleteActivityGoal(req.id, activityName)

      case 'statement =>
        certificateService.deleteStatementGoal(req.id, req.tincanVerb, req.tincanObject)

      case 'package =>
        certificateService.deletePackageGoal(req.id, req.packageId)

      case _ => throw new BadRequestException
    }
  }
}
