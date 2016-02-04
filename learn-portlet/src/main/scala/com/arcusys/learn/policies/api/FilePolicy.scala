package com.arcusys.learn.policies.api

import com.arcusys.learn.liferay.permission._
import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.learn.models.request._

/**
 * Created by Yuriy Gatilin on 03.08.15.
 */
trait FilePolicy extends BasePolicy {
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
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.Base64Icon) (
    PermissionUtil.requirePermissionApi(
      ModifyPermission, PortletName.CertificateManager, PortletName.LessonManager)
  )

  before("/files(/)", request.getMethod == "POST",
    List(UploadContentType.Icon,UploadContentType.DocLibrary)
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
      .contains(UploadContentType.withName(request.getParameter(FileRequest.ContentType)))) (
      PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonStudio)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.Package) (
    PermissionUtil.requirePermissionApi(UploadPermission, PortletName.LessonManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.ImportQuestion) (
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.ContentManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.ImportCertificate) (
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager)
  )

  before("/files(/)", request.getMethod == "POST",
    UploadContentType.withName(request.getParameter(FileRequest.ContentType)) == UploadContentType.ImportSlideSet) (
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LessonStudio)
  )

  before("/files/package/:id/logo", request.getMethod == "POST") (
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonManager)
  )

  before("/files/certificate/:id/logo", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.CertificateManager)
  )

  before("/files/story_tree/:id/logo", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.PhenomenizerStudio)
  )

  before("/files/slideset/:id/logo", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonStudio)
  )
}
