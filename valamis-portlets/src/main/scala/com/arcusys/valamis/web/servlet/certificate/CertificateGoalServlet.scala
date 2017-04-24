package com.arcusys.valamis.web.servlet.certificate

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.goal.{CertificateGoalGroupWithUser, CertificateGoalsWithGroups, GoalType}
import com.arcusys.valamis.certificate.service.{CertificateGoalService, CertificateUserService}
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.certificate.facade.{CertificateFacadeContract, CertificateResponseFactory}
import com.arcusys.valamis.web.servlet.certificate.response.LiferayActivity
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer}
import org.json4s.{DefaultFormats, Formats}

class CertificateGoalServlet
  extends BaseJsonApiController
    with CertificateResponseFactory{

  private implicit def locale = UserLocalServiceHelper().getUser(PermissionUtil.getUserId).getLocale

  override implicit val jsonFormats: Formats =
    DefaultFormats + new EnumNameSerializer(GoalType) + DateTimeSerializer

  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val certificateGoalService = inject[CertificateGoalService]
  private lazy val userService = inject[UserService]

  get("/certificate-goals/activities") {
    LiferayActivity.activities
  }

  get("/certificate-goals/certificates/:certificateId/users/:userId") {
    val certificateId = params.as[Long]("certificateId")
    val userId = params.as[Long]("userId")
    if (certificateUserService.hasUser(certificateId, userId)) {
      certificateFacade.getGoalsStatuses(certificateId, userId)
    }
  }

  get("/certificate-goals/certificates/:certificateId") {
    val certificateId = params.as[Long]("certificateId")
    val goals = certificateGoalService.getGoals(certificateId).flatMap(toCertificateGoalsData)
    val groups = certificateGoalService.getGroups(certificateId).map { group =>
      val user = group.userId.map(id => new UserInfo(userService.getById(id)))
      CertificateGoalGroupWithUser(group, user)
    }
    CertificateGoalsWithGroups(goals, groups)
  }

}
