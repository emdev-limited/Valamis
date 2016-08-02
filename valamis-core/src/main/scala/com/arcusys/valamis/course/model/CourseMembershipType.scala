package com.arcusys.valamis.course.model

import com.arcusys.learn.liferay.services.GroupLocalServiceHelper

import scala.util.{Failure, Success, Try}

/**
  * Created By:
  * User: zsoltberki
  * Date: 29.4.2016
  */
object CourseMembershipType extends Enumeration {
  type CourseMembershipType = Value
  val OPEN = Value(GroupLocalServiceHelper.TYPE_SITE_OPEN)
  val ON_REQUEST = Value(GroupLocalServiceHelper.TYPE_SITE_RESTRICTED)
  val CLOSED = Value(GroupLocalServiceHelper.TYPE_SITE_PRIVATE)

  def toValidString(v: Int): String = Try(this.apply(v)) match {
    case Success(s) => s.toString
    case Failure(f) => ""
  }
}
