package com.arcusys.valamis.web.init

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.lrssupport.lrs.service.UserCredentialsStorage
import com.arcusys.valamis.lrssupport.services.UserCredentialsStorageImpl
import com.arcusys.valamis.slide.convert.PDFProcessor
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.init.util.BundleResourceReader
import com.arcusys.valamis.web.service.{PDFProcessorImpl, Sanitizer}
import com.escalatesoft.subcut.inject.NewBindingModule
import com.liferay.portal.kernel.service.ServiceContextThreadLocal

/**
  * Created by mminin on 01.06.16.
  */
class BundleConfiguration extends NewBindingModule(fn = implicit module => {
  module.bind[ResourceReader].toSingle(new BundleResourceReader)

  module.bind[UserCredentialsStorage].toSingle(new UserCredentialsStorageImpl(getRequest))

  //TODO: use antisamy or other library
  module.bind[Sanitizer].toSingle(new Sanitizer {
    override def sanitize(text: String): String = text
  })

  module.bind[PDFProcessor] toSingle new PDFProcessorImpl

  def getRequest: (Unit => Option[HttpServletRequest]) = _ => {
    Option(ServiceContextThreadLocal.getServiceContext).map { context =>
      context.getRequest
    }
  }
})
