package com.arcusys.learn.scorm.manifest.storage.impl.liferay

import com.arcusys.valamis.lesson.model.{ PackageScopeRule, LessonType }
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.valamis.lesson.storage.PackageScopeRuleStorage
import com.arcusys.valamis.model.{ ScopeType, PeriodTypes }
import org.joda.time.DateTime
import com.arcusys.learn.persistence.liferay.model.LFPackage
import com.arcusys.learn.persistence.liferay.service.persistence.LFLessonLimitPK
import com.arcusys.learn.persistence.liferay.service.{ LFAttemptLocalServiceUtil, LFLessonLimitLocalServiceUtil, LFPackageLocalServiceUtil }
import com.liferay.portal.kernel.dao.orm.{ PropertyFactoryUtil, RestrictionsFactoryUtil }

import scala.collection.JavaConverters._
import scala.util.Try

trait ScormPackageRepositoryImpl extends ScormPackagesStorage {

  def packageScopeRuleRepository: PackageScopeRuleStorage

  override def renew(): Unit = {
    LFPackageLocalServiceUtil.removeAll()
  }

  override def createAndGetID(entity: Manifest, courseID: Option[Int]): Long = {
    val manifest = Manifest(0, entity.version, entity.base, entity.scormVersion, entity.defaultOrganizationID,
      entity.resourcesBase, entity.title, entity.summary, entity.metadata,
      courseID, logo = entity.logo, isDefault = false, beginDate = None, endDate = None)

    import com.arcusys.learn.storage.impl.liferay.LiferayCommon._
    val newEntity = LFPackageLocalServiceUtil.createLFPackage()

    newEntity.setDefaultOrganizationID(manifest.defaultOrganizationID.orNull)
    newEntity.setTitle(manifest.title)
    newEntity.setBase(manifest.base.orNull)
    newEntity.setResourcesBase(manifest.resourcesBase.orNull)
    newEntity.setSummary(manifest.summary.orNull)
    newEntity.setCourseID(manifest.courseId)
    newEntity.setBeginDate(null)
    newEntity.setEndDate(null)
    manifest.logo.foreach(newEntity.setLogo)

    val id = LFPackageLocalServiceUtil.addLFPackage(newEntity).getId

    val limitEntity = LFLessonLimitLocalServiceUtil.createLFLessonLimit(new LFLessonLimitPK(id.toLong, LessonType.Scorm.toString))
    limitEntity.setPassingLimit(entity.passingLimit)
    limitEntity.setRerunInterval(entity.rerunInterval)
    limitEntity.setRerunIntervalType(entity.rerunIntervalType.toString)
    LFLessonLimitLocalServiceUtil.addLFLessonLimit(limitEntity)
    id
  }

  override def delete(id: Long): Unit = {
    val limitEntity = LFLessonLimitLocalServiceUtil.findByID(id, LessonType.Scorm.toString)
    LFLessonLimitLocalServiceUtil.deleteLFLessonLimit(limitEntity)

    LFPackageLocalServiceUtil.deleteLFPackage(id)

    packageScopeRuleRepository.delete(id)
  }

  override def getById(id: Long): Option[ScormPackage] = {
    Option(LFPackageLocalServiceUtil.fetchLFPackage(id)).map(extractPackage)
  }

  override def getById(id: Long, courseID: Int, scope: ScopeType.Value, scopeID: String): Option[Manifest] = {
    if (scope == ScopeType.Instance) {
      Option(LFPackageLocalServiceUtil.getLFPackage(id))
        .map(extract)
        .map(fillManifestWithScopeValues()(_).head)
    } else {
      Option(LFPackageLocalServiceUtil.getLFPackage(id))
        .map(extract)
        .filter(_.courseId == Option(courseID))
        .map(fillManifestWithScopeValues(scope, Option(scopeID))(_).head)
    }
  }

  override def getAll: Seq[ScormPackage] = {
    LFPackageLocalServiceUtil.getLFPackages(-1, -1).asScala map extractPackage
  }

  override def getByTitleAndCourseId(titlePattern: Option[String], courseIds: Seq[Long]): Seq[ScormPackage] = {
    val ids = courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer)
    val result = titlePattern match {
      case Some(title) =>
        LFPackageLocalServiceUtil.findByTitleAndCourseID(title + "%", ids)
      case None =>
        LFPackageLocalServiceUtil.findByInstance(ids)
    }

    result.asScala.map(extractPackage)
  }

  override def getCountByTitleAndCourseId(titlePattern: String, courseIds: Seq[Int]): Int = {
    LFPackageLocalServiceUtil.countByTitleAndCourseID(titlePattern, courseIds.toArray.map(i => i: java.lang.Integer))
  }

  // get all in course with visibility
  override def getManifestByCourseId(courseId: Long): Seq[Manifest] = {
    getByScope(courseId.toInt, ScopeType.Site, courseId.toString)
  }

  override def getByCourseId(courseId: Long): Seq[ScormPackage] = {
    LFPackageLocalServiceUtil.findByCourseID(courseId.toInt).asScala.map(extractPackage)
  }

  // get all in instance with visibility
  override def getAllForInstance(courseIds: List[Long]): Seq[Manifest] = {
    if (courseIds.isEmpty) {
      Seq()
    }
    else {
      LFPackageLocalServiceUtil.findByInstance(courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer)).asScala
        .map(extract)
        .flatMap(fillManifestWithScopeValues())
    }
  }

  // get all in current course (liferay site) by scope with visibility
  override def getByScope(courseID: Int, scope: ScopeType.Value, scopeID: String): Seq[Manifest] = {
    LFPackageLocalServiceUtil.findByCourseID(courseID).asScala
      .map(extract)
      .flatMap(fillManifestWithScopeValues(scope, Option(scopeID)))
  }

  override def getByExactScope(courseIds: List[Long], scope: ScopeType.Value, scopeID: String): Seq[Manifest] = {
    LFPackageLocalServiceUtil.findByInstance(courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer)).asScala
      .map(extract)
      .flatMap(fillManifestWithScopeValuesWithFilter(scope, Option(scopeID)))
  }

  // for Player show only visible in current scope
  override def getOnlyVisible(scope: ScopeType.Value, scopeID: String, titlePattern: Option[String], date: DateTime): Seq[ScormPackage] = {
    val visiblePackageIds = packageScopeRuleRepository.getPackageIdVisible(scope, Option(scopeID))

    getPackages(visiblePackageIds, titlePattern, date)
  }

  override def getInstanceScopeOnlyVisible(courseIds: List[Long], titlePattern: Option[String], date: DateTime): Seq[ScormPackage] = {
    if (courseIds.isEmpty) {
      Seq()
    }
    else {
      val visiblePackageIds = packageScopeRuleRepository.getPackageIdVisible(ScopeType.Instance, None)
      getPackages(visiblePackageIds, titlePattern, date, Some(courseIds))
    }
  }

  private def getPackages(packageIds: Seq[Long],
                          titlePattern: Option[String],
                          date: DateTime,
                          courseIds: Option[Seq[Long]] = None) = {
    if (packageIds.isEmpty) {
      Seq()
    }
    else {
      val packageQuery = LFPackageLocalServiceUtil.dynamicQuery()
        .add(PropertyFactoryUtil.forName("id").in(packageIds.toArray))
        .add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.isNull("beginDate"),
        RestrictionsFactoryUtil.le("beginDate", date.toDate))
        )
        .add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.isNull("endDate"),
        RestrictionsFactoryUtil.ge("endDate", date.toDate))
        )

      for (ids <- courseIds)
        packageQuery.add(RestrictionsFactoryUtil.in("courseID", ids.map(_.toInt).asJava))

      var packages = LFPackageLocalServiceUtil.dynamicQuery(packageQuery).asScala.map(_.asInstanceOf[LFPackage])

      packages = titlePattern.map(_.toLowerCase) match {
        case Some(title) => packages.filter(_.getTitle.toLowerCase.contains(title))
        case None => packages
      }

      packages.map(extractPackage)
    }
  }

  override def getPackagesWithUserAttempts(userID: Int): Seq[Manifest] = {
    val packageIDs = LFAttemptLocalServiceUtil.findByUserID(userID).asScala.map(_.getPackageID.toLong.asInstanceOf[java.lang.Long]).toSet
    LFPackageLocalServiceUtil.findByPackageID(packageIDs.toArray).asScala.map(extract)
  }

  // These 2 methods is only for SCORM packages
  override def getPackagesWithAttempts: Seq[Manifest] = {
    val packageIDs = LFAttemptLocalServiceUtil.getLFAttempts(-1, -1).asScala.map(_.getPackageID.toLong.asInstanceOf[java.lang.Long]).toSet
    LFPackageLocalServiceUtil.findByPackageID(packageIDs.toArray).asScala.map(extract)
  }

  override def modify(id: Long, title: String, description: String, beginDate: Option[DateTime], endDate: Option[DateTime]) = {
    val entity = LFPackageLocalServiceUtil.getLFPackage(id)
    entity.setTitle(title)
    entity.setSummary(description)
    entity.setBeginDate(beginDate.map(_.toDate).orNull)
    entity.setEndDate(endDate.map(_.toDate).orNull)
    val updatedEntity = LFPackageLocalServiceUtil.updateLFPackage(entity)

    extractPackage(updatedEntity)
  }

  override def setLogo(id: Long, logo: Option[String]): Unit = {
    val entity = LFPackageLocalServiceUtil.getLFPackage(id)
    logo.foreach { l =>
      entity.setLogo(l)
      LFPackageLocalServiceUtil.updateLFPackage(entity)
    }
  }

  private def extractPackage(lfEntity: LFPackage): ScormPackage = {
    import com.arcusys.learn.storage.impl.liferay.LiferayCommon._

    new ScormPackage(
      lfEntity.getId.toInt,
      None,
      lfEntity.getBase.toOption,
      "",
      lfEntity.getDefaultOrganizationID.toOption,
      lfEntity.getResourcesBase.toOption,
      lfEntity.getTitle,
      Option(lfEntity.getSummary),
      None,
      lfEntity.getCourseID.toOption,
      Option(lfEntity.getLogo),
      Option(lfEntity.getBeginDate).map(new DateTime(_)),
      Option(lfEntity.getEndDate).map(new DateTime(_))
    )
  }

  private def extract(lfEntity: LFPackage) = {
    import com.arcusys.learn.storage.impl.liferay.LiferayCommon._
    val lessonLimit = Try({
      val limit = LFLessonLimitLocalServiceUtil.findByID(lfEntity.getId, LessonType.Scorm.toString)
      (limit.getPassingLimit.toInt, limit.getRerunInterval.toInt, limit.getRerunIntervalType)
    }
    ).getOrElse((0, 0, ""))

    new Manifest(lfEntity.getId.toInt,
      None,
      lfEntity.getBase.toOption,
      "",
      lfEntity.getDefaultOrganizationID.toOption,
      lfEntity.getResourcesBase.toOption,
      lfEntity.getTitle,
      Option(lfEntity.getSummary),
      None,
      lfEntity.getCourseID.toOption,
      None,
      Option(lfEntity.getLogo),
      false,
      lessonLimit._1,
      lessonLimit._2,
      PeriodTypes(lessonLimit._3),
      Option(lfEntity.getBeginDate).map(new DateTime(_)),
      Option(lfEntity.getEndDate).map(new DateTime(_))
    )
  }

  private def fillManifestWithScopeValues(scope: ScopeType.Value = ScopeType.Instance, scopeID: Option[String] = None): (Manifest) => Seq[Manifest] = {
    manifest =>
      {
        val scopeRules = packageScopeRuleRepository.getAll(manifest.id.toInt, scope, scopeID)
        if (scopeRules.isEmpty) {
          Seq(manifest)
        } else {
          scopeRules.map(fillByScopeRule(manifest))
        }
      }
  }

  private def fillManifestWithScopeValuesWithFilter(scope: ScopeType.Value = ScopeType.Instance, scopeID: Option[String] = None): (Manifest) => Seq[Manifest] = {
    manifest =>
      {
        val scopeRules = packageScopeRuleRepository.getAll(manifest.id.toInt, scope, scopeID)
        if (scopeRules.isEmpty) {
          Seq()
        } else {
          scopeRules.map(fillByScopeRule(manifest))
        }
      }
  }

  private def fillByScopeRule(manifest: Manifest): (PackageScopeRule) => Manifest = {
    scopeRule =>
      manifest.copy(
        visibility = Option(scopeRule.visibility),
        isDefault = scopeRule.isDefault)
  }
}
