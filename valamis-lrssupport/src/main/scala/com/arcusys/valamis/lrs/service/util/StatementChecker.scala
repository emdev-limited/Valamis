package com.arcusys.valamis.lrs.service.util

import com.arcusys.valamis.lrs.tincan.Statement

/**
  * Created by pkornilov on 04.03.16.
  */
trait StatementChecker {

  def checkStatements(statements: Seq[Statement], companyIdOpt: Option[Long] = None)

}
