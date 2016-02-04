package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.liferay.permission.PermissionUtil
import com.arcusys.learn.models.FileEntryModel
import com.arcusys.learn.models.request.LiferayRequest
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.valamis.file.service.FileEntryService
import com.liferay.portlet.documentlibrary.model.DLFileEntry

class LiferayApiController extends BaseJsonApiController {

  private val fileService = inject[FileEntryService]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/liferay/images(/)") {
    val req = LiferayRequest(this)

    val result = fileService.getImages(
      PermissionUtil.getLiferayUser,
      req.courseId,
      req.filter,
      req.skip,
      req.count,
      req.isSortDirectionAsc
    )

    CollectionResponse(
      req.page,
      result.items map toResponse,
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
      result.items map toResponse,
      result.total
    )
  }

  private def toResponse(x: DLFileEntry) = FileEntryModel(
      x.getFileEntryId,
      x.getTitle,
      x.getFolderId,
      x.getVersion,
      x.getMimeType,
      x.getGroupId,
      x.getUuid
  )
}
