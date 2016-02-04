package com.arcusys.valamis.gradebook.service

import com.arcusys.valamis.lrs.tincan.Statement

trait GradeBookService {

  def getStatementGrades(packageId: Long,
                         valamisUserId: Long,
                         sortAsc: Boolean = false,
                         shortMode: Boolean = false): Seq[Statement]
}
