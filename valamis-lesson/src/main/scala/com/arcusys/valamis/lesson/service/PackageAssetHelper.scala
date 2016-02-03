package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.valamis.lesson.model.BaseManifest
import com.arcusys.valamis.util.AssetHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class PackageAssetHelper(implicit val bindingModule: BindingModule) extends AssetHelper with Injectable {

  lazy val packageService = inject[ValamisPackageService]

  def updatePackageAssetEntry(userId: Long, groupId: Long, pkg: BaseManifest,
                              isVisible: Boolean = true) = {
    AssetEntryLocalServiceHelper.fetchAssetEntry(pkg.getClass.getName, pkg.id) match {
      case Some(asset) =>
        updateAssetEntry(Some(asset.getPrimaryKey), pkg.id, Some(userId), Some(groupId), Some(pkg.title), pkg.summary, pkg, isVisible = isVisible)
      case None =>
        updateAssetEntry(None, pkg.id, Some(userId), Some(groupId), Some(pkg.title), pkg.summary, pkg, isVisible = isVisible)
    }
  }

}
