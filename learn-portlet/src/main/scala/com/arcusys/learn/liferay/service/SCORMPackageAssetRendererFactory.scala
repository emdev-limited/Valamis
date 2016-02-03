package com.arcusys.learn.liferay.service

import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest

object SCORMPackageAssetRendererFactory {
  final val CLASS_NAME: String = classOf[Manifest].getName
  final val TYPE: String = "scormpackage"
}

class SCORMPackageAssetRendererFactory extends BasePackageAssetRendererFactory {
  def getClassName: String = SCORMPackageAssetRendererFactory.CLASS_NAME

  def getType: String = SCORMPackageAssetRendererFactory.TYPE
}