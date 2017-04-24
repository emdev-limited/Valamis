package com.arcusys.valamis.lrsEndpoint.service

import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.storage.LrsEndpointStorage

// TODO refactor with lesson package
abstract class LrsEndpointServiceImpl extends LrsEndpointService {
  def endpointStorage: LrsEndpointStorage

  override def setEndpoint(newEndpoint: LrsEndpoint)(implicit companyId: Long): Unit = {
    if (newEndpoint.auth == AuthType.INTERNAL) endpointStorage.deleteAll(companyId)
    else endpointStorage.deleteExternal(companyId)

    endpointStorage.create(newEndpoint, companyId)
  }

  override def getEndpoint(implicit companyId: Long): Option[LrsEndpoint] = {
    val all = endpointStorage.getAll(companyId)

    lazy val external = all.find(_.auth != AuthType.INTERNAL)
    lazy val internal = all.find(_.auth == AuthType.INTERNAL)

    external orElse internal
  }

  override def switchToInternal(customHost: Option[String])(implicit companyId: Long): Unit = {
    endpointStorage.get(AuthType.INTERNAL, companyId) match {
      case Some(internal) => setEndpoint(internal.copy(customHost = customHost))
      case None =>
    }
  }
}
