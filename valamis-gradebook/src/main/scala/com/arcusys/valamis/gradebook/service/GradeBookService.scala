package com.arcusys.valamis.gradebook.service

import com.arcusys.valamis.gradebook.model.AttemptInfo
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.model.{RangeResult, SkipTake}

trait GradeBookService {

  def getStatementGrades(packageId: Long,
                         valamisUserId: Long,
                         sortAsc: Boolean = false,
                         shortMode: Boolean = false): Seq[Statement]

  def getAttemptsAndStatements(userId: Long,
                               lessonId: Long,
                               companyId: Long,
                               skipTake: Option[SkipTake]): RangeResult[AttemptInfo]
}
