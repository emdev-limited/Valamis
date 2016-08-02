package com.arcusys.valamis.web.service

trait Sanitizer {
  def sanitize(text: String): String
}
