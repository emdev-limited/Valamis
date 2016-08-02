package com.arcusys.valamis.uri.service

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.uri.model.TincanURIType.TincanURIType
import com.arcusys.valamis.uri.model.TincanURI

trait TincanURIService {
  def getByURI(uri: String): Option[TincanURI]

  def getOrCreate(prefix: String, objId: String, objType: TincanURIType, content: Option[String]): TincanURI

  def createRandom(objType: TincanURIType, content: Option[String]): TincanURI

  def getById(objId: String, objType: TincanURIType): Option[TincanURI]

  def getById(skipTake: Option[SkipTake], filter: String): Seq[TincanURI]

  def getLocalURL(suffix: String = "delegate/", companyId:Option[Long]=None): String
}
