package com.arcusys.valamis.lrs.service

import com.arcusys.valamis.lrs.api.valamis._
import com.arcusys.valamis.lrs.api.{ActivityApi, ActivityProfileApi, StatementApi}

trait LrsClientManager {

  def statementApi[T](action: StatementApi => T, authInfo: Option[String] = None): T

  def verbApi[T](action: VerbApi => T, authInfo: Option[String] = None): T

  def scaleApi[T](action: ScaleApi => T, authInfo: Option[String] = None): T

  def activityProfileApi[T](action: ActivityProfileApi => T, authInfo: Option[String] = None): T

  def activityApi[T](action: ActivityApi => T, authInfo: Option[String] = None): T
}
