package com.arcusys.valamis.lrsEndpoint.storage

import com.arcusys.valamis.lrsEndpoint.model.AuthType.AuthType
import com.arcusys.valamis.lrsEndpoint.model.LrsEndpoint

trait LrsEndpointStorage {
  def getAll: Seq[LrsEndpoint]
  def get(auth: AuthType): Option[LrsEndpoint]
  def deleteAll(): Unit
  def deleteExternal(): Unit
  def create(entity: LrsEndpoint): LrsEndpoint
}
