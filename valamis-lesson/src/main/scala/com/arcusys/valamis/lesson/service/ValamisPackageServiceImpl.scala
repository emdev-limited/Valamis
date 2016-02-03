package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.{AssetEntryLocalServiceHelper, SocialActivityLocalServiceHelper}
import com.arcusys.valamis.course.UserCourseResultService
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.exception.{NoPackageException, PassingLimitExceededException}
import com.arcusys.valamis.lesson.model.LessonType._
import com.arcusys.valamis.lesson.model.{LessonType, _}
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.valamis.lesson.scorm.storage.tracking.{ActivityStateTreeStorage, AttemptStorage}
import com.arcusys.valamis.lesson.storage.{LessonLimitStorage, PackageScopeRuleStorage, PlayerScopeRuleStorage}
import com.arcusys.valamis.lesson.tincan.model.{TincanManifest, TincanPackage}
import com.arcusys.valamis.lesson.tincan.storage.{PackageCategoryGoalStorage, TincanManifestActivityStorage, TincanPackageStorage}
import com.arcusys.valamis.lrs.util.TinCanActivityType
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.model.ScopeType.ScopeType
import com.arcusys.valamis.model.{PeriodTypes, ScopeType}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.ratings.model.Rating
import com.arcusys.valamis.uri.model.ValamisURIType
import com.arcusys.valamis.uri.service.URIServiceContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

// TODO refactor, split on parts, tincan statement part, scores, import/export ...
class ValamisPackageServiceImpl(implicit val bindingModule: BindingModule) extends ValamisPackageService with Injectable with PackageSelect {

  val packageRepository = inject[ScormPackagesStorage]
  val tcpackageRepository = inject[TincanPackageStorage]
  val tincanManifestActivityStorage = inject[TincanManifestActivityStorage]
  val uriService = inject[URIServiceContract]
  val attemptStorage = inject[AttemptStorage]
  val activityStateTreeStorage = inject[ActivityStateTreeStorage]
  val passingLimitChecker = inject[LessonLimitChecker]
  val fileStorage = inject[FileStorage]  // FIXME: Used because /data/$packageId/$fileName breaks /files prefix encapsulation.
  val fileService = inject[FileService]
  val tagService = inject[TagServiceContract]
  val packageScopeRuleStorage = inject[PackageScopeRuleStorage]
  val playerScopeRuleRepository = inject[PlayerScopeRuleStorage]
  val lessonLimitStorage = inject[LessonLimitStorage]
  val packageGoalStorage = inject[PackageCategoryGoalStorage]
  val statementReader = inject[LessonStatementReader]
  private lazy val ratingService = new RatingService[BaseManifest]

  lazy val courseResults = inject[UserCourseResultService]
  val scormClassName = classOf[Manifest].getName
  val tincanClassName = classOf[TincanManifest].getName

  protected lazy val scopePackageService = inject[ScopePackageService]
  private lazy val assetHelper = new PackageAssetHelper()

  private def contentPathPrefix(packageId: Long) = s"data/$packageId/"
  private def logoPathPrefix(packageId: Long) = s"package_logo_$packageId/"
  private def logoPath(packageId: Long, logo: String) = "files/" + logoPathPrefix(packageId) + logo

  def getTincanRootActivityId(packageId: Long): String = {
    val targetType = TinCanActivityType.getURI(TinCanActivityType.course)
    lazy val hasPackage = getTincanPackageById(packageId).isDefined

    tincanManifestActivityStorage.getByPackageId(packageId)
      .find(_.activityType equals targetType)
      .map(_.tincanId)
      .getOrElse{
        if (hasPackage) throw new Exception(s"no course activity for tincan package $packageId")
        else throw new NoPackageException(packageId)
      }
  }

  def getScormRootActivityId(packageId: Long): String = {
    uriService.getById(packageId.toString, ValamisURIType.Package).map(_.uri).getOrElse(packageId.toString)
  }

  def getRootActivityId(packageId: Long): String = {
    getById(packageId).map(_.packageType) match {
      case Some(LessonType.Scorm) => getScormRootActivityId(packageId)
      case Some(LessonType.Tincan) => getTincanRootActivityId(packageId)
      case None => throw new NoPackageException(packageId)
    }
  }

  def getPackagesCount(courseId: Long): Int = getPackagesByCourse(courseId).length

  def getByCourse(courseId: Long): Seq[BaseManifest] = {
    packageRepository.getManifestByCourseId(courseId) ++
      tcpackageRepository.getManifestByCourseId(courseId)
  }

  def getPackagesByCourse(courseId: Long): Seq[PackageBase] = {
    packageRepository.getByCourseId(courseId) ++ tcpackageRepository.getByCourseId(courseId)
  }

  def getTincanPackagesByCourse(courseId: Int, onlyVisible: Boolean): Seq[BaseManifest] =
    tcpackageRepository.getManifestByCourseId(courseId, onlyVisible)

  def getPackage(packageId: Long): BaseManifest =
    tcpackageRepository.getById(packageId).map(toTincanManifest).getOrElse(
      packageRepository.getById(packageId).map(toScormManifest).getOrElse(
        throw new EntityNotFoundException("Package not found"))
    )

  def getPackage(className: String, packageId: Long): BaseManifest = {
    val pkg = className match {
      case `tincanClassName` =>
        tcpackageRepository.getById(packageId).map(toTincanManifest)
      case `scormClassName` =>
        packageRepository.getById(packageId).map(toScormManifest)
      case _ => throw new NotImplementedError("Getting package of " + className)
    }
    pkg.getOrElse(throw new EntityNotFoundException("Package not found"))
  }

  def getPackage(lessonType: LessonType, packageId: Long): BaseManifest = {
    val className = lessonType match {
      case LessonType.Tincan => classOf[TincanManifest].getName
      case LessonType.Scorm => classOf[Manifest].getName
    }
    getPackage(className, packageId)
  }

  override def getLogo(packageId: Long): Option[Array[Byte]] = {
    getById(packageId)
      .flatMap(_.logo)
      .map(logoPath(packageId, _))
      .flatMap(fileService.getFileContentOption)
  }

  override def setLogo(packageId: Long, name: String, content: Array[Byte]): Unit = {
    val pack = getPackage(packageId)
    fileService.setFileContent(
      folder = logoPathPrefix(packageId),
      name = name,
      content = content,
      deleteFolder = true
    )

    pack match {
      case tincan: TincanManifest => tcpackageRepository.setLogo(tincan.id, Some(name))
      case scorm: Manifest => packageRepository.setLogo(scorm.id, Some(name))
    }
  }

  private def updatePackageSettings(id: Long, visibility: Boolean, isDefault: Boolean, scope: ScopeType, courseId: Int, pageId: Option[String], playerId: Option[String]): PackageScopeRule = {

    //TODO check places with scope = instanceScope, siteScope, pageScope, playerScope
    scope match {
      case ScopeType.Instance => scopePackageService.setInstanceScopeSettings(id, visibility, isDefault)
      case ScopeType.Site => scopePackageService.setSiteScopeSettings(id, courseId, visibility, isDefault)
      case ScopeType.Page => scopePackageService.setPageScopeSettings(id, pageId.get, visibility, isDefault)
      case ScopeType.Player => scopePackageService.setPlayerScopeSettings(id, playerId.get, visibility, isDefault)
    }
  }

  def updatePackage(tags: Seq[Long], passingLimit: Int, rerunInterval: Int, rerunIntervalType: PeriodType,
                    beginDate: Option[DateTime], endDate: Option[DateTime], scope: ScopeType, packageId: Long,
                    visibility: Boolean, isDefault: Boolean, courseId: Int, title: String, description: String,
                    packageType: LessonType, pageId: Option[String], playerId: Option[String], userId: Int): BaseManifest = {

    val scopeRule = updatePackageSettings(packageId, visibility, isDefault, scope, courseId, pageId, playerId)
    val limits = lessonLimitStorage.setLimit(packageId, packageType, passingLimit, rerunInterval, rerunIntervalType)
    val pkg = updatePackage(packageType, passingLimit, rerunInterval, rerunIntervalType, packageId, title, description, beginDate, endDate)

    val manifest: BaseManifest = packageType match {
      case LessonType.Tincan =>
        val tincanPackage = tcpackageRepository.modify(packageId, title, description, beginDate, endDate)
        toTincanManifest(tincanPackage, Option(scopeRule), limits)
      case LessonType.Scorm =>
        val scormPackage = packageRepository.modify(packageId, title, description, beginDate, endDate)
        toScormManifest(scormPackage, Option(scopeRule))
      case _ => throw new NotImplementedError()
    }

    val assetRefId = assetHelper.updatePackageAssetEntry(userId, courseId, manifest, visibility)
    tagService.assignTags(assetRefId, tags)

    manifest
  }

  def uploadPackages(packages: Seq[PackageUploadModel], scope: ScopeType, courseId: Int, pageId: Option[String], playerId: Option[String]) {

    packages.foreach(pack => {

      val packageId = pack.id
      val visibility = true
      val isDefault = false
      val title = pack.title
      val description = pack.description

      val packageType = pack.packageType
      val packageLogo = Option(pack.logo)
      val limit = 0
      val rerunInterval = 0
      val period = PeriodTypes.UNLIMITED

      updatePackageSettings(packageId, visibility, isDefault, scope, courseId, pageId, playerId)
      updatePackage(packageType, limit, rerunInterval, period, packageId, title, description, None, None)
      updatePackageLogo(packageType, packageId, packageLogo)
    })
  }

  def updatePackageLogo(lessonType: LessonType, packageId: Long, packageLogo: Option[String]): Unit = {
    lessonType match {
      case LessonType.Tincan =>
        tcpackageRepository.setLogo(packageId, packageLogo)
      case LessonType.Scorm =>
        packageRepository.setLogo(packageId, packageLogo)
    }
  }

  private def updatePackage(lessonType: LessonType, passingLimit: Int, rerunInterval: Int, rerunIntervalType: PeriodType, packageId: Long, title: String, description: String, beginDate: Option[DateTime], endDate: Option[DateTime]) = {
    val pkg: PackageBase = lessonType match {
      case LessonType.Tincan =>
        tcpackageRepository.modify(packageId, title, description, beginDate, endDate)
      case LessonType.Scorm =>
        packageRepository.modify(packageId, title, description, beginDate, endDate)
      case _ => throw new NotImplementedError()
    }
    lessonLimitStorage.setLimit(packageId, lessonType, passingLimit, rerunInterval, rerunIntervalType)
    pkg
  }

  def updatePackageScopeVisibility(id: Long, scope: ScopeType, courseId: Int, visibility: Boolean, isDefault: Boolean, pageId: Option[String], playerId: Option[String], userId: Long) {
    updatePackageSettings(id, visibility, isDefault, scope, courseId, pageId, playerId)
  }

  def removePackage(packageId: Long): Unit =
    getById(packageId) foreach { p =>
      removePackage(p.id, p.packageType)
    }

  def removePackage(packageId: Long, packageType: LessonType) {
    val courseId = packageType match {
      case LessonType.Scorm =>
        val pkg = packageRepository.getById(packageId).map(toScormManifest).get
        for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(pkg.getClass.getName, pkg.id))
          assetHelper.deleteAssetEntry(asset.getPrimaryKey, pkg)
        packageRepository.delete(packageId)
        SocialActivityLocalServiceHelper.deleteActivities(classOf[ScormPackage].getName, packageId)
        pkg.courseId.get
      case LessonType.Tincan =>
        val pkg = tcpackageRepository.getById(packageId).map(toTincanManifest).get
        for (asset <- AssetEntryLocalServiceHelper.fetchAssetEntry(pkg.getClass.getName, pkg.id))
          assetHelper.deleteAssetEntry(asset.getPrimaryKey, pkg)
        tcpackageRepository.delete(packageId)
        tincanManifestActivityStorage.deleteByPackageId(packageId)
        SocialActivityLocalServiceHelper.deleteActivities(classOf[TincanPackage].getName, packageId)
        pkg.courseId.get
    }

    fileStorage.delete(contentPathPrefix(packageId), asDirectory = true)
    fileService.deleteByPrefix(logoPathPrefix(packageId))
    packageGoalStorage.delete(packageId)

    ratingService.deleteRatings(packageId)

    courseResults.resetCourseResults(courseId)
  }

  def removePackages(packageIds: Seq[Long]) =
    packageIds.foreach(removePackage)

  def addPackageToPlayer(playerId: String, packageId: Long) = {
    val isDefault = false
    val visibility = true
    scopePackageService.setPlayerScopeSettings(packageId, playerId, visibility, isDefault)
  }

  def updatePlayerScope(scope: ScopeType, playerId: String) {
    playerScopeRuleRepository.get(playerId) match {
      case Some(rule) => playerScopeRuleRepository.update(playerId, scope)
      case _ => playerScopeRuleRepository.create(playerId, scope)
    }
  }

  override def getScormManifest(packageId: Int) = {
    packageRepository.getById(packageId).map(toScormManifest).get
  }

  override def getTincanLaunchWithLimitTest(packageId: Int, user: LUser): String = {
    val tincanPackage = tcpackageRepository.getById(packageId)
    if (tincanPackage.isEmpty)
      throw new UnsupportedOperationException()

    if (!passingLimitChecker.checkTincanPackage(user, packageId))
      throw new PassingLimitExceededException

    val activities = tincanManifestActivityStorage.getByPackageId(packageId)
    val firstActivity = activities.find(a => a.launch != null && a.launch.isDefined).getOrElse(throw new UnsupportedOperationException("tincan package without launch not supported"))

    val mainFileName = "data/" + tincanPackage.get.id + "/" + firstActivity.launch.get
    mainFileName
  }

  override def getAll: Seq[BaseManifest] = {
    tcpackageRepository.getAll.map(toTincanManifest) ++
      packageRepository.getAll.map(toScormManifest)
  }

  override def ratePackage(packageId:Long,  userId: Long, score:Double) = {
    ratingService.updateRating(userId, score, packageId)
    getRating(packageId, userId)
  }

  override def deletePackageRating(packageId:Long,  userId: Long) = {
    ratingService.deleteRating(userId, packageId)
    getRating(packageId, userId)
  }

  override def getRating(packageId: Long, userId: Long): Rating = {
    ratingService.getRating(userId, packageId)
  }

  override def updateOrder(playerId: String, packageIds: Seq[Long]): Unit ={
    val rule = playerScopeRuleRepository.get(playerId)
    val scope = rule.map(_.scope).getOrElse(ScopeType.Site)

    packageIds.zipWithIndex.foreach {
      case(packageId, i) => packageScopeRuleStorage.updatePackageIndex(packageId, scope, i)
    }
  }
}
