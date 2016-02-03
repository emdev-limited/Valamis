package com.arcusys.learn.view

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.permission.{PermissionUtil, ViewAllPermission}
import com.arcusys.learn.models.response.users._
import com.arcusys.learn.view.extensions._
import com.arcusys.valamis.user.service.UserService
import org.apache.http.client.RedirectException

class LearningTranscriptView extends OAuthPortlet with BaseView{

  val userService = inject[UserService]

  override def doView(request: RenderRequest, response: RenderResponse) {

    try {
      val scope = getSecurityData(request)
      val user = new UserResponse(userService.getById(scope.userId))
      val data = Map(
        "userName" -> user.name,
        "userPicture" -> user.picture,
        "userPageUrl" -> user.pageUrl,
        "viewAllPermission" -> PermissionUtil.hasPermission(scope.courseId, scope.portletId, scope.primaryKey, ViewAllPermission)) ++
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
