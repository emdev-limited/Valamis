package com.arcusys.learn.liferay.update

/**
 * Created by aklimov on 05.02.15.
 */

import com.arcusys.learn.liferay.services.{GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.valamis.lesson.service.{PackageAssetHelper, ScopePackageService}
import com.arcusys.valamis.lesson.tincan.storage.TincanPackageStorage
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}

class CreatePackageAssets(implicit val bindingModule: BindingModule) extends Injectable {
  private val _log: Log = LogFactoryUtil.getLog(classOf[CreatePackageAssets])
  val packageRepository = inject[ScormPackagesStorage]
  val tincanRepository = inject[TincanPackageStorage]
  private val scopePackageService = inject[ScopePackageService]

  def run(companyIds: Seq[Long]): Unit = {
    companyIds.foreach(companyId => {
      val defaultUserId = UserLocalServiceHelper().getDefaultUserId(companyId)
      val groupId = GroupLocalServiceHelper.getCompanyGroup(companyId).getGroupId
      createAssetRefs(companyId, groupId, defaultUserId)
    })
  }

  def createAssetRefs(companyId: Long, groupId: Long, userId: Long) {
    val courseIds = scopePackageService.getAllCourseIds(companyId)
    val scormPackages = packageRepository.getAllForInstance(courseIds)
    val tincanPackages = tincanRepository.getAllForInstance(courseIds)
    val assetHelper = new PackageAssetHelper()

    scormPackages.foreach { pkg =>
      assetHelper.updatePackageAssetEntry(userId, groupId, pkg)
    }
    tincanPackages.foreach { pkg =>
      assetHelper.updatePackageAssetEntry(userId, groupId, pkg)
    }
  }
}