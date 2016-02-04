package com.arcusys.learn.policies.api

import com.arcusys.learn.liferay.permission.{ModifyPermission, PermissionUtil, PortletName, ViewPermission}

trait PackagePolicy extends BasePolicy {

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

  before("/packages(/)", request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonManager)
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
