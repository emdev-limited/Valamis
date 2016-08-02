package com.arcusys.valamis.hook

import com.arcusys.valamis.hook.utils.{StructureInfo, TemplateInfo, Utils}
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.service.{GroupLocalServiceUtil, UserLocalServiceUtil}

class UpgradeTemplates extends SimpleAction {
  private val log = LogFactoryUtil.getLog(classOf[UpgradeTemplates])

  override def run(companyIds: Array[String]): Unit = {
    log.info("Upgrade valamis web content template")

    companyIds.foreach(companyId => {
      val groupId = GroupLocalServiceUtil.getCompanyGroup(companyId.toLong).getGroupId
      val userId = UserLocalServiceUtil.getDefaultUserId(companyId.toLong)

      upgrade(groupId, userId)
    })
  }

  private def upgrade(groupId: Long, userId: Long) {

    Utils.addStructureWithTemplate(
      groupId,
      userId,
      StructureInfo(key = "ValamisWebContent", name = "valamis-web-content"),
      TemplateInfo(key = "ValamisWebContent", name = "ValamisWebContent")
    )
  }
}
