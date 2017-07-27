package com.arcusys.learn.liferay.util

import java.io.InputStream
import com.liferay.portal.kernel.util.MimeTypesUtil

object MimeTypesHelper {
  def getContentType(inputStream: InputStream, fileName: String): String = MimeTypesUtil.getContentType(inputStream, fileName)
}
