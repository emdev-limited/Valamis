package com.arcusys.valamis.web.servlet.liferay

import com.arcusys.learn.liferay.LiferayClasses.LDLFileEntry
import com.arcusys.valamis.file.service.FileEntryService
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.liferay.request.LiferayRequest
import com.arcusys.valamis.web.servlet.liferay.response.FileEntryModel
import com.arcusys.valamis.web.servlet.response.CollectionResponse

class LiferayServlet extends BaseJsonApiController {

  private lazy val fileService = inject[FileEntryService]

  get("/liferay/images(/)") {
    val req = LiferayRequest(this)

    val result = fileService.getImages(
      PermissionUtil.getLiferayUser,
      req.courseId,
      req.filter,
      req.skip,
      req.count,
      req.ascending
    )

    CollectionResponse(
      req.page,
      result.records map toResponse,
      result.total
    )
  }
  get("/liferay/video(/)") {
    val req = LiferayRequest(this)

    val result = fileService.getVideo(
      PermissionUtil.getLiferayUser,
      req.courseId,
      req.skip,
      req.count
    )

    CollectionResponse(
      req.page,
      result.records map toResponse,
      result.total
    )
  }

  private def toResponse(x: LDLFileEntry) = FileEntryModel(
      x.getFileEntryId,
      x.getTitle,
      x.getFolderId,
      x.getVersion,
      x.getMimeType,
      x.getGroupId,
      x.getUuid
  )
}
