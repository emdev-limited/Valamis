package com.arcusys.valamis.reports


package object model {

  type DateTime = org.joda.time.DateTime


  implicit class Optional[A](val a: A) extends AnyVal {
    def optional: Option[A] = Option(a)
    def some: Option[A] = Some(a)
  }
}
