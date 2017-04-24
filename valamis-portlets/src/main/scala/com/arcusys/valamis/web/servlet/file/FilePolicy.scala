package com.arcusys.valamis.web.servlet.file

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{PermissionUtil => _, _}
import com.arcusys.valamis.web.servlet.base._
import com.arcusys.valamis.web.servlet.file.request.{FileActionType, FileExportRequest, FileRequest, UploadContentType}
import org.scalatra.ScalatraBase

/**
  * Created by Yuriy Gatilin on 03.08.15.
  */
trait FilePolicy {
  self: ScalatraBase =>

  before("/files/export(/)", request.getParameter(FileExportRequest.ContentType) == FileExportRequest.Package)(
    PermissionUtil.requirePermissionApi(ExportPermission, PortletName.LessonManager)
  )

  before("/files(/)", FileActionType.withName(request.getParameter(FileRequest.Action)) == FileActionType.Delete)(
    PermissionUtil.requirePermissionApi(ModifyPermission,
      PortletName.CertificateManager,
      PortletName.LessonManager,
      PortletName.ContentManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.Base64Icon)(
    PermissionUtil.requirePermissionApi(
      ModifyPermission, PortletName.CertificateManager, PortletName.LessonManager)
  )

  before("/files(/)", request.getMethod == "POST",
    List(UploadContentType.Icon, UploadContentType.DocLibrary)
      .contains(UploadContentType.withName(request.getParameter(FileRequest.ContentType)))) {
    PermissionHelper.preparePermissionChecker(PermissionUtil.getUserId)
    PermissionUtil.requirePermissionApi(
      Permission(ModifyPermission, List(PortletName.CertificateManager, PortletName.LessonManager)),
      Permission(ViewPermission, List(PortletName.LessonStudio))
    )
  }

  before("/files(/)", request.getMethod == "POST",
    List(UploadContentType.RevealJs, UploadContentType.Pdf,
      UploadContentType.Pptx, UploadContentType.ImportLesson, UploadContentType.ImportPackage)
      .contains(UploadContentType.withName(request.getParameter(FileRequest.ContentType))))(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonStudio)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.Package)(
    PermissionUtil.requirePermissionApi(UploadPermission, PortletName.LessonManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.ImportQuestion)(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.ContentManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.ImportCertificate)(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.ImportSlideSet)(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonStudio)
  )

  before("/files/package/:id/logo", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonManager)
  )

  before("/files/certificate/:id/logo", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager)
  )

  before("/files/slideset/:id/logo", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonStudio)
  )
}
