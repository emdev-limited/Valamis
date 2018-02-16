package com.arcusys.valamis.statements

import com.arcusys.valamis.lrs.tincan.Statement

/**
  * Created by eboytsova on 02/05/2017.
  */
  trait StatementChecker {

    def checkStatements(statements: Seq[Statement], companyIdOpt: Option[Long] = None)
  }
