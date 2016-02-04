package com.arcusys.learn.liferay.service

import com.arcusys.valamis.lesson.tincan.model.TincanManifest

object TincanPackageAssetRendererFactory {
  final val CLASS_NAME: String = classOf[TincanManifest].getName
  final val TYPE: String = "tincanpackage"
}

class TincanPackageAssetRendererFactory extends BasePackageAssetRendererFactory {
  def getClassName: String = TincanPackageAssetRendererFactory.CLASS_NAME

  def getType: String = TincanPackageAssetRendererFactory.TYPE

}