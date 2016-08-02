package com.arcusys.valamis.util

package object enumeration {

  implicit class EnumerationExtensions(val e: Enumeration) extends AnyVal {
    def isValid(s: String): Boolean = {
      e.values.exists(_.toString == s)
    }
  }

}
