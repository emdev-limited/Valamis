package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.LCompany
import com.liferay.portal.kernel.dao.shard.ShardUtil
import com.liferay.portal.service.ShardLocalServiceUtil

object ShardUtilHelper {
  def initShardUtil(companyId:Long): Unit = {
    if (ShardUtil.isEnabled) {
      CompanyHelper.setCompanyId(companyId)

      val shard = ShardLocalServiceUtil.getShard(classOf[LCompany].getName, companyId)

      ShardUtil.setTargetSource(shard.getName)
    }
  }

}
