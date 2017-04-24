package com.arcusys.valamis.uri.service

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.uri.model.TincanURIType.TincanURIType
import com.arcusys.valamis.uri.model.{TincanURI, TincanURIType}
import com.arcusys.valamis.uri.storage.TincanURIStorage
import java.util.UUID

import com.arcusys.learn.liferay.services.{CompanyHelper, CompanyLocalServiceHelper, ServiceContextHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper

import scala.util.Try

abstract class TincanURIServiceImpl extends TincanURIService {

  def uriStorage: TincanURIStorage

  override def getByURI(uri: String): Option[TincanURI] = {
    uriStorage.get(uri)
  }

  override def getOrCreate(prefix: String, id: String, uriType: TincanURIType, content: Option[String]): TincanURI = {
    lazy val objId = uriType match {
      case TincanURIType.Package => id
      case _ => Try(UUID.fromString(id)).getOrElse(UUID.randomUUID).toString
    }

    uriStorage.getById(id, uriType) match {
      case Some(uri) => uri
      case None =>
        val uri = createUri(prefix, objId, uriType)
        uriStorage.create(uri, objId, uriType, content.getOrElse(""))
    }
  }

  override def createRandom(uriType: TincanURIType, content: Option[String]): TincanURI = {
    val id = UUID.randomUUID.toString
    val prefix = getLocalURL()

    val uuid = Try(UUID.fromString(id)).getOrElse(UUID.randomUUID).toString
    val uri = createUri(prefix, uuid, uriType)

    uriStorage.create(uri, uuid, uriType, content.getOrElse(""))
  }

  override def getById(id: String, uriType: TincanURIType): Option[TincanURI] = {
    uriStorage.getById(id, uriType)
  }

  override def getById(skipTake: Option[SkipTake], filter: String): Seq[TincanURI] = {
    uriStorage.getAll(skipTake, filter)
  }

  def createUri(prefix: String, id: String, uriType: TincanURIType): String = {
    s"$prefix$uriType/${uriType}_$id"
  }

  override def getLocalURL(suffix: String = "delegate/uri/", companyId: Option[Long] = None): String = {
    val company = CompanyLocalServiceHelper.getCompany(companyId.getOrElse(CompanyHelper.getCompanyId))
    val isSecure = if (ServiceContextHelper.getServiceContext != null)
      ServiceContextHelper.getServiceContext.getRequest.isSecure
    else false //TODO: get somehow isSecure when ServiceContextThreadLocal.getServiceContext == null
    val rootUrl = PortalUtilHelper.getPortalURL(company.getVirtualHostname, PortalUtilHelper.getPortalPort(isSecure), isSecure) // http://localhost:8080
    rootUrl + "/" + suffix
  }
}