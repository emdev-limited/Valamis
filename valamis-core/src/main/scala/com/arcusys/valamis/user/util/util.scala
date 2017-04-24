package com.arcusys.valamis.user

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.WebServerServletTokenHelper
import com.liferay.portal.kernel.util.{DigesterUtil, HttpUtil}
import scala.collection.JavaConverters._
import scala.util.Try

package object util {

  implicit class UserExtension(val user: LUser) extends AnyVal {
    def getPortraitUrl: String = {
      val gender = if (user.isMale) "male" else "female"
      val portraitId = user.getPortraitId
      val token = HttpUtil.encodeURL(DigesterUtil.digest(user.getUserUuid))
      val stamp = WebServerServletTokenHelper.getToken(portraitId)

      s"/image/user_${gender}_portrait?img_id=$portraitId&img_id_token=$token&t=$stamp"
    }

    def getPublicUrl: String = {
      Try(if (new LGroup(user.getGroup).getPublicLayoutsPageCount > 0) "/web/" + user.getScreenName else "").getOrElse("")
    }

    def getOrganizationNames: Set[String] = {
      user.getOrganizations.asScala.map(org => org.getName).toSet
    }
  }
}
