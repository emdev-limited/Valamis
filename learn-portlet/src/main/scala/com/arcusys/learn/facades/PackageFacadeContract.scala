package com.arcusys.learn.facades

import java.io.InputStream

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.models.valamispackage.{PackageResponse, PlayerPackageResponse}
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model.{PackageSortBy, PackageUploadModel}
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.model.ScopeType.ScopeType
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import org.joda.time.DateTime

trait PackageFacadeContract {

  def exportAllPackages(courseID: Int): InputStream

  def exportPackages(packagesIds: Seq[Long]): InputStream

  def exportPackagesForMobile(packagesIds: Seq[Long]): InputStream

  def getForPlayerConfig(playerID: String, companyID: Long, groupId: Long, user: LUser): Seq[PackageResponse]

  def getAllPackages(lessonType: Option[LessonType], courseId: Option[Long], scope: ScopeType, filter: Option[String], tagId: Option[Long],
                     isSortDirectionAsc: Boolean, skipTake: Option[SkipTake],
                     companyID: Long, user: LUser): RangeResult[PackageResponse]

  def getForPlayer(companyId: Long, courseId: Long, pageId: String, filter: Option[String], tagId: Option[Long],
                   playerID: String, user: LUser, isSortDirectionAsc: Boolean, sortBy: PackageSortBy.PackageSortBy,
                   skipTake: Option[SkipTake]): RangeResult[PlayerPackageResponse]

  def getByScopeType(courseID: Int, scope: ScopeType, pageId: Option[String], playerID: Option[String],
                     companyID: Long, courseIds: List[Long], user: LUser): Seq[PackageResponse]

  def updatePackage(packageId: Long, tags: Seq[String], passingLimit: Int, rerunInterval: Int, rerunIntervalType: PeriodType,
                    beginDate: Option[DateTime], endDate: Option[DateTime], scope: ScopeType, visibility: Boolean, isDefault: Boolean,
                    companyId: Long, courseId: Int, title: String, description: String, packageType: LessonType, pageId: Option[String],
                    playerId: Option[String], user: LUser): PackageResponse

  def uploadPackages(packages: Seq[PackageUploadModel], scope: ScopeType, courseId: Int, pageID: Option[String], playerID: Option[String])
}
