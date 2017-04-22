package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{Permission, ModifyPermission, PreferencesPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

trait PackagePolicy { self: ScalatraBase =>

  before("/packages(/)", request.getMethod == "GET", request.getParameter("action") == "ALL") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonViewer, PortletName.LessonManager)
  }

  before("/packages(/)", request.getMethod == "GET", request.getParameter("action") == "VISIBLE") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonViewer)
  }

  before("/packages/:id/logo", request.getMethod == "GET") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonManager, PortletName.LessonViewer,
      PortletName.MyLessons, PortletName.ValamisActivities, PortletName.RecentLessons)
  }

  before("/packages/getPersonalForPlayer", request.getMethod == "GET") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonViewer)
  }

  before("/packages/getByScope", request.getMethod == "GET") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonManager, PortletName.LessonViewer)
  }

  before("/packages(/)", request.getMethod == "POST",
    request.getParameter("action") != "ADD_LESSONS_TO_PLAYER",
    request.getParameter("action") != "DELETE_LESSON_FROM_PLAYER",
    request.getParameter("action") != "SET_PLAYER_DEFAULT",
    request.getParameter("action") != "UPDATE_VISIBLE") {
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonManager)
  }

  before("/packages(/)", request.getMethod == "POST", request.getParameter("action") == "UPDATE_VISIBLE") {
    PermissionUtil.requirePermissionApi(
      Permission(ModifyPermission, List(PortletName.LessonManager)),
      Permission(ViewPermission, List(PortletName.LessonStudio))
    )
  }

  before("/packages(/)", request.getMethod == "POST", request.getParameter("action") == "ADD_LESSONS_TO_PLAYER") {
    PermissionUtil.requirePermissionApi(PreferencesPermission, PortletName.LessonViewer)
  }

  before("/packages(/)", request.getMethod == "POST", request.getParameter("action") == "DELETE_LESSON_FROM_PLAYER") {
    PermissionUtil.requirePermissionApi(PreferencesPermission, PortletName.LessonViewer)
  }

  before("/packages(/)", request.getMethod == "POST", request.getParameter("action") == "SET_PLAYER_DEFAULT") {
    PermissionUtil.requirePermissionApi(PreferencesPermission, PortletName.LessonViewer)
  }

  before("/packages/:packageType/:id(/)", request.getMethod == "DELETE") {
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonManager)
  }

  before("/packages/updatePackageScopeVisibility/:id", request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonViewer)
  }

  before("/packages/addPackageToPlayer/:playerID", request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonViewer)
  }

  before("/packages/updatePlayerScope", request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonViewer)
  }
}
