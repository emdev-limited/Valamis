package com.arcusys.learn.controllers.api

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.facades.PackageFacadeContract
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.models.TagResponse
import com.arcusys.learn.models.request.PackageRequest
import com.arcusys.learn.models.response.CollectionResponseHelper._
import com.arcusys.learn.models.valamispackage.PackageSerializer
import com.arcusys.learn.policies.api.PackagePolicy
import com.arcusys.learn.web.ServletBase
import com.arcusys.valamis.lesson.model.{LessonType, PackageUploadModel}
import com.arcusys.valamis.lesson.service.{TagServiceContract, ValamisPackageService}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

class PackageApiController
  extends BaseJsonApiController
  with PackagePolicy
  with ServletBase {

  private lazy val packageFacade = inject[PackageFacadeContract]
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val req = PackageRequest(this)
  private lazy val tagService = inject[TagServiceContract]

  implicit override val jsonFormats: Formats =
    DefaultFormats + new PackageSerializer + new EnumNameSerializer(LessonType)

  get("/packages(/)", request.getParameter("action") == "VISIBLE"){
    val companyId = req.companyId
    val courseId = req.courseIdRequired
    val pageId = req.pageIdRequired
    val playerId = req.playerIdRequired
    val user = getLiferayUser
    val tagId = req.tagId

    val filter = req.textFilter
    packageFacade.getForPlayer(companyId, courseId, pageId, filter, tagId, playerId, user,
      req.isSortDirectionAsc, req.sortBy, req.skipTake
    )
      .toCollectionResponse(req.page)
  }

  get("/packages(/)", request.getParameter("action") == "ALL"){
    val courseId = req.courseIdRequired
    val companyId = req.companyId
    val user = getLiferayUser
    val scope = req.scope

    val filter = req.textFilter
    val tagId = req.tagId
    val isSortDirectionAsc = req.isSortDirectionAsc
    val packageType = req.packageType

    packageFacade.getAllPackages(packageType, Some(courseId), scope, filter, tagId, isSortDirectionAsc, req.skipTake, companyId, user)
      .toCollectionResponse(req.page)
  }


  get("/packages/:id/logo"){

    val content = packageService.getLogo(req.packageId)
      .getOrElse(halt(HttpServletResponse.SC_NOT_FOUND, s"Package with id: ${req.packageId} doesn't exist"))

    response.reset()
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("image/png")
    content
  }

  get("/packages/getPersonalForPlayer"){

    val playerId = req.playerIdRequired
    val companyId = req.companyId
    val groupId = getLiferayUser.getGroupId
    val user = getLiferayUser

    packageFacade.getForPlayerConfig(playerId, companyId, groupId, user)
  }

  get("/packages/getByScope"){
    val courseId = req.courseIdRequired
    val pageId = req.pageId
    val playerId = req.playerId
    val companyId = req.companyId
    val user = getLiferayUser

    val scope = req.scope
    val courseIds = List(getLiferayUser.getGroupId)

    packageFacade.getByScopeType(courseId.toInt, scope, pageId, playerId, companyId, courseIds, user)
  }

  post("/packages(/)", request.getParameter("action") == "UPDATE"){
    val packageId = req.packageId

    val courseId = req.courseIdRequired
    val companyId = req.companyId
    val pageId = req.pageId
    val playerId = req.playerId
    val user = getLiferayUser
    val scope = req.scope

    val visibility = req.visibility
    val isDefault = req.isDefault
    val title = req.title.get
    val description = req.description.getOrElse("")
    val packageType = req.packageTypeRequired

    val passingLimit = req.passingLimit
    val rerunInterval = req.rerunInterval
    val rerunIntervalType = req.rerunIntervalType

    val tags = req.tags

    val beginDate = req.beginDate
    val endDate = req.endDate

    packageFacade.updatePackage(packageId, tags, passingLimit, rerunInterval, rerunIntervalType, beginDate, endDate, scope, visibility, isDefault, companyId, courseId.toInt, title, description, packageType, pageId, playerId, user)
  }

  post("/packages(/)", request.getParameter("action") == "UPDATELOGO"){
    val packageId = req.packageId
    val packageLogo = req.packageLogo
    val packageType = req.packageTypeRequired

    packageService.updatePackageLogo(packageType, packageId, packageLogo)
  }

  post("/packages(/)", request.getParameter("action") == "UPDATEPACKAGES"){
    val packages = JsonHelper.fromJson[Seq[PackageUploadModel]](req.packages)

    val scope = req.scope
    val courseId = req.courseIdRequired
    val pageId = req.pageId
    val playerId = req.playerId

    packageFacade.uploadPackages(packages, scope, courseId.toInt, pageId, playerId)
  }

  delete("/packages/:packageType/:id(/)"){
    val packageId = req.packageId
    val packageType = req.packageTypeRequired

    packageService.removePackage(packageId, packageType)
  }

  post("/packages(/)", request.getParameter("action") == "REMOVEPACKAGES"){
    val packageIds = req.packageIds

    packageService.removePackages(packageIds)
  }

  post("/packages/updatePackageScopeVisibility/:id") {

    val courseId = req.courseIdRequired
    val pageId = req.pageId
    val playerId = req.playerId
    val userId = getLiferayUser.getUserId
    val scope = req.scope
    val id = req.packageId
    val visibility = req.visibility
    val isDefault = req.isDefault

    packageService.updatePackageScopeVisibility(id, scope, courseId.toInt, visibility, isDefault, pageId, playerId, userId)
  }

  post("/packages/addPackageToPlayer/:playerID") {
    val playerId = req.playerIdRequired
    val packageId = req.packageId

    packageService.addPackageToPlayer(playerId, packageId)
  }

  post("/packages/updatePlayerScope") {
    val scope = req.scope
    val playerId = req.playerIdRequired

    packageService.updatePlayerScope(scope, playerId)
  }

  get("/packages/tags(/)") {
    val tags = req.playerId match {
      case Some(playerId) =>
        tagService.getPackagesTagsByPlayerId(playerId, req.companyId, req.courseIdRequired, req.pageId.get)

      case None =>
        req.courseId match {
          case Some(courseId) => tagService.getPackagesTagsByCourse(courseId)
          case None => tagService.getPackagesTagsByCompany(getCompanyId)
        }
    }

    tags.map(t => TagResponse(t.id, t.text))
  }

  post("/packages/rate(/)", request.getParameter("action") == "UPDATERATING") {

    val packageId = req.packageId
    val ratingScore = req.ratingScore
    val userId = getUserId

    packageService.ratePackage(packageId, userId, ratingScore)
  }

  post("/packages/rate(/)", request.getParameter("action") == "DELETERATING") {

    val packageId = req.packageId
    val userId = getUserId

    packageService.deletePackageRating(packageId, userId)
  }

  post("/packages/order(/)") {
    packageService.updateOrder(
      req.playerIdRequired,
      req.packageIdsRequired
    )
  }
}
