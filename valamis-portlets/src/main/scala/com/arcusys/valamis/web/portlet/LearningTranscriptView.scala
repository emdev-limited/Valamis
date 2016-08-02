package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PermissionUtil, PortletBase, ViewAllPermission}
import org.apache.http.client.RedirectException

class LearningTranscriptView extends OAuthPortlet with PortletBase {

  lazy val userService = inject[UserService]

  override def doView(request: RenderRequest, response: RenderResponse) {

    try {
      val scope = getSecurityData(request)
      val user = new UserInfo(userService.getById(scope.userId))
      val permission = new PermissionUtil(request, this)
      val data = Map(
        "userName" -> user.name,
        "userPicture" -> user.picture,
        "userPageUrl" -> user.pageUrl,
        "viewAllPermission" -> permission.hasPermission(ViewAllPermission.name)) ++
        scope.data

      implicit val out = response.getWriter
      sendTextFile("/templates/2.0/learning_transcript_template.html")
      sendTextFile("/templates/2.0/user_select_templates.html")
      sendTextFile("/templates/2.0/paginator.html")
      sendMustacheFile(data, "learning_transcript.html")
    }
    catch {
      case e: RedirectException =>
        response.getWriter.println(s"""<script type="text/javascript">
            window.location.replace("${e.getMessage}");
          </script>""")
    }
  }
}
