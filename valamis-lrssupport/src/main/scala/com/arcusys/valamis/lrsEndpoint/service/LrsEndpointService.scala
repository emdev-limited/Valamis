package com.arcusys.valamis.lrsEndpoint.service

import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}

/**
 * Created by igorborisov on 17.10.14.
 */
trait LrsEndpointService {

  def setEndpoint(endpointSettings: LrsEndpoint): Unit

  def getEndpoint: Option[LrsEndpoint]
  
  def switchToInternal(customHost: Option[String]): Unit
}
