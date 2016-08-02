package com.arcusys.valamis.web.portlet

import java.io.PrintWriter
import javax.portlet._

import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletURLUtilHelper}
import com.arcusys.valamis.certificate.service.AssignmentService
import com.arcusys.valamis.web.portlet.base._

/**
 * User: Yulia.Glushonkova
 * Date: 07.06.13
 */
abstract class CurriculumAbstract extends OAuthPortlet with PortletBase {

  lazy val assignmentService = inject[AssignmentService]

  protected def sendResponse(data: Map[String, Any], templateName: String)(implicit out: PrintWriter) = {
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/paginator.html")
    sendTextFile("/templates/2.0/image_gallery_templates.html")
    sendTextFile("/templates/2.0/user_select_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendMustacheFile(data, templateName)
  }

  protected def doEditViewHelper(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)

    val data = Map(
      "language" -> language,
      "certificateActionURL" -> response.createResourceURL(),
      "companyID" -> PortalUtilHelper.getCompanyId(request),
      "portletID" -> request.getAttribute("PORTLET_ID"),
      "contextPath" -> getContextPath(request)
    ) ++ getTranslation("curriculum", language)

    sendTextFile("/templates/2.0/curriculum_admin_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendMustacheFile(data, "curriculum_settings.html")
  }

  protected def doViewHelper(request: RenderRequest, response: RenderResponse): SecurityData = {

    val language = LiferayHelpers.getLanguage(request)

    val httpServletRequest = PortalUtilHelper.getHttpServletRequest(request)
    val url = getRootUrl(request, response)

    val translations = getTranslation("curriculum", language)

    val securityScope = getSecurityData(request)
    httpServletRequest.getSession.setAttribute("userID", securityScope.userId)
    val permission = new PermissionUtil(request, this)
    val publishPermission = permission.hasPermission(PublishPermission.name)

    securityScope.data = securityScope.data ++
      Map(
        "root" -> url,
        "isAdmin" -> securityScope.permissionToModify,
        "permissionToPublish" -> publishPermission,
        "assignmentDeployed" -> assignmentService.isAssignmentDeployed
      ) ++ translations

    securityScope
  }

  private def getRootUrl(request: RenderRequest, response: RenderResponse) = {
    val url = PortletURLUtilHelper.getCurrent(request, response)
    val parts = url.toString.split("/")
    if (parts.length > 2) parts.tail.tail.head else ""
  }
}

class CurriculumAdmin extends CurriculumAbstract {
  override def doView(request: RenderRequest, response: RenderResponse) {
    val scope = super.doViewHelper(request: RenderRequest, response: RenderResponse)

    implicit val out = response.getWriter

    sendTextFile("/templates/2.0/curriculum_admin_templates.html")
    sendResponse(scope.data, "curriculum_admin.html")
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    doEditViewHelper(request, response)
  }

}

class CurriculumUser extends CurriculumAbstract {
  override def doView(request: RenderRequest, response: RenderResponse) {
    val scope = super.doViewHelper(request: RenderRequest, response: RenderResponse)

    implicit val out = response.getWriter

    sendTextFile("/templates/2.0/curriculum_user_templates.html")
    sendResponse(scope.data, "curriculum_user.html")
  }
}
