package com.arcusys.valamis.web.servlet.base.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.{CompanyHelper, PermissionHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import scala.util.Try


class LiferayCheckerAuthStrategy(protected val app: ScalatraBase) extends ScentryStrategy[AuthUser] {

  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[AuthUser] = {
    def findUserAndSetupPermissions = {
      val user = getUserByRequest()

      val companyId = PortalUtilHelper.getCompanyId(request)
      CompanyHelper.setCompanyId(companyId)

      PermissionHelper.preparePermissionChecker(user)

      AuthUser(user.getUserId, user)
    }

    Try(findUserAndSetupPermissions).toOption
  }

  def getUserByRequest()(implicit request: HttpServletRequest): LUser = {

    Option(PortalUtilHelper.getUser(request)) getOrElse {
      val companyId = PortalUtilHelper.getCompanyId(request)
      UserLocalServiceHelper().getDefaultUser(companyId)
    }
  }

}

