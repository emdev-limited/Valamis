package com.arcusys.learn.liferay.services


import scala.collection.JavaConverters._
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil
import com.liferay.social.kernel.model.SocialActivityCounter
import com.liferay.social.kernel.service.SocialActivityCounterLocalServiceUtil

/**
 * Created by mminin on 29.08.14.
 */
object SocialActivityCounterLocalServiceHelper {
  def getUserValue(userId: Long, counterNames: String): Option[Int] = {

    SocialActivityCounterLocalServiceUtil.dynamicQuery[SocialActivityCounter](SocialActivityCounterLocalServiceUtil.dynamicQuery
      .add(RestrictionsFactoryUtil.eq("classPK", userId)) // user Id in classpk column
      .add(RestrictionsFactoryUtil.eq("name", counterNames))
    )
      .asScala
      .map(_.getTotalValue).toSeq match {
        case Nil              => None
        case values: Seq[Int] => Some(values.sum)
      }
  }
}
