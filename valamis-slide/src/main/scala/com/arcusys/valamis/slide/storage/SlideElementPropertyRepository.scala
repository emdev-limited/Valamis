package com.arcusys.valamis.slide.storage

/**
 * Created by Igor Borisov on 02.11.15.
 */
trait SlideElementPropertyRepository {
  def createFromOldValues(deviceId: Long,
                          slideElementId: Long,
                          top: String,
                          left: String,
                          width: String,
                          height: String): Unit
}
