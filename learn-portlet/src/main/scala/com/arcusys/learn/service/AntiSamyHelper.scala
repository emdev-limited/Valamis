package com.arcusys.learn.service

import com.arcusys.valamis.web.service.Sanitizer
import org.owasp.validator.html._

object AntiSamyHelper extends Sanitizer {
  private val policy = Policy.getInstance(Thread.currentThread().getContextClassLoader.getResourceAsStream("antisamy-tinymce.xml"))
  private val as = new AntiSamy()

  def sanitize(text: String) = {
    val cr = as.scan(text, policy)
    cr.getCleanHTML.replaceAll("\n", "").replaceAll("\"", "\\\"") // strip newlines
  }
}
