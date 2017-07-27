package com.arcusys.learn.liferay.util

import com.liferay.portal.kernel.dao.db.{DB, DBManagerUtil}

object DBFactoryUtilHelper {
  def getDB: DB = DBManagerUtil.getDB
}
