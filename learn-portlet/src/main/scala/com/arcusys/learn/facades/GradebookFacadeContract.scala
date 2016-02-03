package com.arcusys.learn.facades

import com.arcusys.learn.models.Gradebook._
import com.arcusys.learn.models.response.{CollectionResponse, PieData}
import com.arcusys.valamis.gradebook.model.GradebookUserSortBy
import com.arcusys.valamis.gradebook.model.GradebookUserSortBy.GradebookUserSortBy
import com.arcusys.valamis.lesson.model.RecentLesson
import com.arcusys.valamis.model.SkipTake

trait GradebookFacadeContract {
  def getStudents(courseId: Long,
                  skip: Int,
                  count: Int,
                  nameFilter: String,
                  orgNameFilter: String,
                  sortBy: GradebookUserSortBy,
                  sortAZ: Boolean,
                  detailed: Boolean = false,
                  packageIds: Seq[Long] = Seq()): Seq[StudentResponse]

  def getStudentsCount(courseId: Int,
    nameFilter: String,
    orgNameFilter: String): Int

  def getGradesForStudent(studentId: Int,
                          courseId: Int,
                          skip: Int,
                          count: Int,
                          sortAsc: Boolean = false,
                          withStatements: Boolean = true): StudentResponse

  def getBy(userId: Long, isCompeted: Boolean, skipTake: Option[SkipTake]): CollectionResponse[GradedPackageResponse]


  def getPieDataWithCompletedPackages(userId: Long): (Seq[PieData], Int)

  def getTotalGradeForStudent(studentId: Int,
    courseId: Int): TotalGradeResponse

  def getLastModified(courseId: Long, userId: Long): String

  def getLastPackages(userId: Long, count: Int): Seq[RecentLesson]

  def getPackageGradeWithStatements(valamisUserId: Long,
    packageId: Long, gradeAuto: Option[String] = None): PackageGradeResponse
}
