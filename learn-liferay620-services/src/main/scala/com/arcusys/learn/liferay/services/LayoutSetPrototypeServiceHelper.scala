package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.LLayoutSetPrototype
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.service.LayoutSetPrototypeLocalServiceUtil
import scala.collection.JavaConverters._

/**
  * Created by amikhailov on 23.11.16.
  */
object LayoutSetPrototypeServiceHelper {

  def search(companyId: Long, active: Boolean): Seq[LLayoutSetPrototype] =
    LayoutSetPrototypeLocalServiceUtil.search(companyId, active, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null).asScala

  def getLayoutSetPrototype(layoutSetPrototypeId: Long): LLayoutSetPrototype =
    LayoutSetPrototypeLocalServiceUtil.getLayoutSetPrototype(layoutSetPrototypeId)

  def fetchLayoutSetPrototype(layoutSetPrototypeId: Long): Option[LLayoutSetPrototype] =
    Option(LayoutSetPrototypeLocalServiceUtil.fetchLayoutSetPrototype(layoutSetPrototypeId))
}