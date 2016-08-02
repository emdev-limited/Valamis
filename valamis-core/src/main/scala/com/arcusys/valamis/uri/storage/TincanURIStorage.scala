package com.arcusys.valamis.uri.storage

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.uri.model.TincanURI
import com.arcusys.valamis.uri.model.TincanURIType._

trait TincanURIStorage {
  def get(uri: String): Option[TincanURI]
  def getById(objId: String, objType: TincanURIType): Option[TincanURI]
  def getAll(skipTake: Option[SkipTake], filter: String): Seq[TincanURI]
  def create(uri: String, objId: String, objType: TincanURIType, content: String): TincanURI
  def delete(uri: String)
}
