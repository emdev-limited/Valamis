package com.arcusys.learn.liferay.service.utils

import com.arcusys.learn.liferay.constants.PortletConstantsHelper

object PortletKeys {
  final val ValamisPackage: String = "SCORMApplication_WAR_learnportlet"
  final val ValamisPackageAdmin: String = "PackageManager_WAR_learnportlet"
  final val ValamisPackageDefaultInstance: String = ValamisPackage + PortletConstantsHelper.INSTANCE_SEPARATOR + "0000"
  final val ValamisCertificate: String = "CurriculumUser_WAR_learnportlet"
  final val ValamisCertificateAdmin: String = "Curriculum_WAR_learnportlet"
  final val ValamisCertificateDefaultInstance: String = ValamisCertificate + PortletConstantsHelper.INSTANCE_SEPARATOR + "0000"
}
