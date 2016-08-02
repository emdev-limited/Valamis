package com.arcusys.valamis.web.servlet

import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.slide.service.SlideServiceContract
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.web.servlet.base.BaseApiController
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.request.uri.{URIActionType, URIRequest}

/**
 * Create and provide URI for TinCan Objects
 */
class URIServlet extends BaseApiController {

  lazy val uriService = inject[TincanURIService]
  lazy val slideService = inject[SlideServiceContract]

  get("/uri(/)")(jsonAction {
    val uriRequest = URIRequest(this)
    uriRequest.action match {
      case None =>
        val result = uriService.getOrCreate(
          uriRequest.prefix,
          uriRequest.id,
          TincanURIType.withName(uriRequest.objectType.toLowerCase),
          uriRequest.content)

        result
      case Some(URIActionType.GetAll) =>
        uriService.getById(
          uriRequest.skipTake,
          uriRequest.filter)
      case _ => throw new BadRequestException
    }
  })

  get("/uri/verbs(/)")(jsonAction {
    slideService.getTinCanVerbs
  })

  get("/uri/:objType/:objName")(action {
    val uri = request.getRequestURL.toString
    val result = uriService.getByURI(uri)
    if (result.isDefined)
      halt(200, result.get.content)
    else
      throw new EntityNotFoundException("Object not found")
  })

}
