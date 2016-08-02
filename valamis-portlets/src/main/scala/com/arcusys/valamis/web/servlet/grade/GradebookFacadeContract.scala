package com.arcusys.valamis.web.servlet.grade

import com.arcusys.valamis.web.servlet.grade.response.{PackageGradeResponse, PieData, StudentResponse}

@deprecated
trait GradebookFacadeContract {

  def getGradesForStudent(studentId: Long,
                          courseId: Long,
                          skip: Int,
                          count: Int,
                          sortAsc: Boolean = false,
                          withStatements: Boolean = true): StudentResponse

  def getPieDataWithCompletedPackages(userId: Long): (Seq[PieData], Int)

  def getPackageGradeWithStatements(valamisUserId: Long,
    packageId: Long, gradeAuto: Option[String] = None): PackageGradeResponse
}
