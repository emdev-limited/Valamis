package com.arcusys.valamis.gradebook.service

import com.arcusys.valamis.lesson.service.LessonStatementReader
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.util.Joda._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class GradeBookServiceImpl(implicit val bindingModule: BindingModule) extends Injectable with GradeBookService {
  private lazy val statementReader = inject[LessonStatementReader]

  def getStatementGrades(packageId: Long,
                         valamisUserId: Long,
                         sortAsc: Boolean = false,
                         shortMode: Boolean = false): Seq[Statement] = {

    val statements = if (!shortMode) {
      statementReader.getAll(valamisUserId, packageId)
    }
    else {
      val root = statementReader.getRoot(valamisUserId, packageId)
      val answered = statementReader.getAnsweredByPackageId(valamisUserId, packageId)

      (root ++ answered).sortBy(_.timestamp).reverse
    }


    if (!sortAsc)
      statements.reverse
    else
      statements
  }

}
