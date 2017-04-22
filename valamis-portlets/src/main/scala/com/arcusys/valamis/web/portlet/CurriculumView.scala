package com.arcusys.valamis.web.portlet

import java.io.PrintWriter
import javax.portlet._

import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletURLUtilHelper}
import com.arcusys.valamis.web.portlet.base._

/**
 * User: Yulia.Glushonkova
 * Date: 07.06.13
 */
abstract class CurriculumAbstract extends OAuthPortlet with PortletBase {

  protected def sendResponse(data: Map[String, Any], templateName: String)(implicit out: PrintWriter) = {
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/user_select_templates.html")
    sendTextFile("/templates/file_uploader.html")
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

    sendTextFile("/templates/curriculum_admin_templates.html")
    sendTextFile("/templates/file_uploader.html")
    sendMustacheFile(data, "curriculum_settings.html")
  }

  protected def doViewHelper(request: RenderRequest, response: RenderResponse): SecurityData = {
    val httpServletRequest = PortalUtilHelper.getHttpServletRequest(request)
    val url = getRootUrl(request, response)

    val securityScope = getSecurityData(request)
    httpServletRequest.getSession.setAttribute("userID", securityScope.userId)
    val permission = new PermissionUtil(request, this)
    val activatePermission = permission.hasPermission(PublishPermission.name)

    securityScope.data = securityScope.data ++
      Map(
        "root" -> url,
        "isAdmin" -> securityScope.permissionToModify,
        "permissionToActivate" -> activatePermission
      )

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

    sendTextFile("/templates/curriculum_admin_templates.html")
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

    sendTextFile("/templates/curriculum_user_templates.html")
    sendResponse(scope.data, "curriculum_user.html")
  }
}
