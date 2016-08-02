package com.arcusys.learn.liferay.util

import java.sql.Connection
import javax.sql.DataSource

import com.liferay.portal.kernel.dao.jdbc.DataAccess
import com.liferay.portal.kernel.util.InfrastructureUtil

/**
  * Created by mminin on 31.05.16.
  */
object DataAccessUtil {
  def getDataSource: DataSource = InfrastructureUtil.getDataSource

  def cleanUp(connection: Connection): Unit = {
    DataAccess.cleanUp(connection)
  }
}
