package com.arcusys.valamis.lrsEndpoint.storage

import com.arcusys.valamis.lrsEndpoint.model.AuthType.AuthType
import com.arcusys.valamis.lrsEndpoint.model.LrsEndpoint

trait LrsEndpointStorage {
  def getAll(companyId: Long): Seq[LrsEndpoint]
  def get(auth: AuthType, companyId: Long): Option[LrsEndpoint]
  def deleteAll(companyId: Long): Int
  def deleteExternal(companyId: Long): Int
  def create(entity: LrsEndpoint, companyId: Long): Unit
}
