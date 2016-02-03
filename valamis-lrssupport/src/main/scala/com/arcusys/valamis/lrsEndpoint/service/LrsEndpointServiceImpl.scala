package com.arcusys.valamis.lrsEndpoint.service

import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.storage.LrsEndpointStorage
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

// TODO refactor with lesson package
class LrsEndpointServiceImpl(implicit val bindingModule: BindingModule) extends LrsEndpointService with Injectable {
  private val settings = inject[LrsEndpointStorage]

  override def setEndpoint(newEndpoint: LrsEndpoint): Unit = {
    if (newEndpoint.auth == AuthType.INTERNAL) settings.deleteAll()
    else settings.deleteExternal()

    settings.create(newEndpoint)
  }

  override def getEndpoint: Option[LrsEndpoint] = {
    val all = settings.getAll

    lazy val external = all.find(_.auth != AuthType.INTERNAL)
    lazy val internal = all.find(_.auth == AuthType.INTERNAL)

    external orElse internal
  }

  override def switchToInternal(customHost: Option[String]): Unit = {
    settings.get(AuthType.INTERNAL) match {
      case Some(internal) => setEndpoint(internal.copy(customHost = customHost))
      case None =>
    }
  }
}
