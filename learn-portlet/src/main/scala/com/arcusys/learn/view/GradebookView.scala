package com.arcusys.learn.view

import javax.portlet._

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.permission.{PermissionUtil, ViewAllPermission}
import com.arcusys.learn.liferay.util.EncryptorUtilHelper
import com.arcusys.learn.view.extensions._
import com.arcusys.learn.view.liferay.LiferayHelpers
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.tincan.Agent
import com.arcusys.valamis.lrs.util.TincanHelper
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.arcusys.valamis.util.serialization.JsonHelper

class GradebookView extends OAuthPortlet with BaseView {

  lazy val packageService = inject[ValamisPackageService]

  override def doView(request: RenderRequest, response: RenderResponse) {

    val scope = getSecurityData(request)

    val language = LiferayHelpers.getLanguage(request)

    val user = LiferayHelpers.getUser(request)

    // for poller auth we encrypt company key + userID
    val encryptUserID = EncryptorUtilHelper.encrypt(scope.company.getKeyObj, "" + scope.userId)
    val isAdmin = PermissionUtil.hasPermission(scope.courseId, scope.portletId, scope.primaryKey, ViewAllPermission)

    val translations = getTranslation("gradebook", language)
    val packages = packageService.getByCourse(scope.courseId.toInt)

    def StringToNone(str: String): Option[String] = {
      if (str == null || str.isEmpty)
        None
      else
        Some(str)
    }

    val tincanActor = if (user != null)
      JsonHelper.toJson(user.getAgentByUuid, new AgentSerializer)
    else
      JsonHelper.toJson(Agent(name = StringToNone("Anonymous"), mBox = StringToNone("mailto:anonymous@liferay.com")), new AgentSerializer)

    val endpoint = JsonHelper.toJson(getEndpointInfo(request))

    val data = Map(
      "encryptUserID" -> encryptUserID,
      "isAdmin" -> isAdmin,
      "packages" -> packages,
      "tincanActor" -> tincanActor,
      "permissionToViewAll" -> isAdmin,
      "endpointData" -> endpoint
    ) ++ translations ++ scope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/gradebook_templates.html")
    sendTextFile("templates/2.0/paginator.html")
    sendMustacheFile(data, "gradebook.html")
  }
}