package com.arcusys.learn.liferay.servlet

import com.liferay.portal.kernel.servlet.PortalDelegateServlet

class CustomPortalDelegateServlet extends PortalDelegateServlet {
  //we have to override this method as it called by Equinox OSGI framework
  //at such moment, that it causes NPE due to uncompleted initialization
  //of delegate servlet (field, storing reference to wrapped servlet, i.e., our servlet,
  //is not yet initialized)

  //TODO: return correct ServletInfo
  override def getServletInfo: String = {
    Option(servlet)
      .map(_.getServletInfo)
      .getOrElse("initialization in progress")
  }
}
