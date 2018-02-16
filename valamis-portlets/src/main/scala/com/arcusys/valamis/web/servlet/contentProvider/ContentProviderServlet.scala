package com.arcusys.valamis.web.servlet.contentProvider

import com.arcusys.valamis.slide.service.contentProvider.ContentProviderService
import com.arcusys.valamis.slide.service.contentProvider.model.ContentProvider
import com.arcusys.valamis.slide.service.contentProvider.util.EduAppsHelper
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.response.CollectionResponse

/**
  * Created By:
  * User: zsoltberki
  * Date: 20.9.2016
  */
class ContentProviderServlet extends BaseApiController {

  private implicit lazy val providerService = inject[ContentProviderService]
  private lazy val providerRequest = ContentProviderRequest(this)

  get("/content-providers(/)")(jsonAction {
    val contentProviders = providerService.getAll(providerRequest.skipTake,
      providerRequest.textFilter,
      providerRequest.ascending,
      PermissionUtil.getCompanyId)

    CollectionResponse(providerRequest.page, contentProviders.records, contentProviders.total)
  })

  get("/content-providers/edu-apps(/)") {
    EduAppsHelper.getApps(providerRequest.offset)
  }


  post("/content-providers(/)")(jsonAction {
    providerService.add(
      getContentProvider(true)
    )
  })

  put("/content-providers(/)")(jsonAction {
    providerService.update(
      getContentProvider(false)
    )

  })

  delete("/content-providers(/)")(jsonAction {
    providerService.delete(
      providerRequest.id)
  })

  private def getContentProvider(isNew: Boolean) = {
    ContentProvider(
      if(isNew) 0L else providerRequest.id,
      providerRequest.name,
      providerRequest.description.getOrElse(""),
      providerRequest.imageUrl.getOrElse(""),
      providerRequest.url,
      providerRequest.width.getOrElse(0),
      providerRequest.height.getOrElse(0),
      providerRequest.isPrivate,
      providerRequest.customerKey.getOrElse(""),
      providerRequest.customerSecret.getOrElse(""),
      PermissionUtil.getCompanyId,
      providerRequest.isSelective.getOrElse(false)
    )
  }
}