package com.arcusys.learn.tincan.manifest.storage.impl.liferay

import com.arcusys.valamis.lesson.model.{PackageScopeRule, LessonType}
import com.arcusys.valamis.lesson.storage.PackageScopeRuleStorage
import com.arcusys.valamis.lesson.tincan.model.{TincanPackage, TincanManifest}
import com.arcusys.valamis.lesson.tincan.storage.TincanPackageStorage
import com.arcusys.valamis.model.{ScopeType, PeriodTypes}
import org.joda.time.DateTime
import com.arcusys.learn.persistence.liferay.model.LFTincanPackage
import com.arcusys.learn.persistence.liferay.service.{LFLessonLimitLocalServiceUtil, LFTincanPackageLocalServiceUtil}
import com.arcusys.learn.persistence.liferay.service.persistence.LFLessonLimitPK
import com.liferay.portal.kernel.dao.orm.{PropertyFactoryUtil, RestrictionsFactoryUtil}

import scala.collection.JavaConverters._
import scala.util.Try

trait TincanPackageRepositoryImpl extends TincanPackageStorage {

  def packageScopeRuleRepository: PackageScopeRuleStorage

  override def createAndGetID(title: String, summary: String, courseID: Option[Int]): Long = {
    import com.arcusys.learn.storage.impl.liferay.LiferayCommon._
    val newEntity = LFTincanPackageLocalServiceUtil.createLFTincanPackage()

    newEntity.setCourseID(courseID)
    newEntity.setSummary(summary)
    newEntity.setTitle(title)

    LFTincanPackageLocalServiceUtil.addLFTincanPackage(newEntity).getId
  }

  override def delete(id: Long): Unit = {
    packageScopeRuleRepository.delete(id)

    LFTincanPackageLocalServiceUtil.deleteLFTincanPackage(id)
    Try(LFLessonLimitLocalServiceUtil.findByID(id, LessonType.Tincan.toString)).map { passingLimitEntity =>
      LFLessonLimitLocalServiceUtil.deleteLFLessonLimit(passingLimitEntity)
    }
  }

  override def getAll: Seq[TincanPackage] = {
    LFTincanPackageLocalServiceUtil.getLFTincanPackages(-1, -1).asScala.map(extractPackage)
  }

  override def getByTitleAndCourseId(titlePattern: Option[String], courseIds: Seq[Long]): Seq[TincanPackage] = {
    val ids = courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer)
    val entities = titlePattern match {
      case Some(title) =>
        LFTincanPackageLocalServiceUtil.findByTitleAndCourseID(title + "%", ids)
      case None =>
        LFTincanPackageLocalServiceUtil.findByInstance(ids)
    }
    entities.asScala.map(extractPackage)
  }

  override def getCountByTitleAndCourseId(titlePattern: String, courseIds: List[Long]): Int = {
    LFTincanPackageLocalServiceUtil.countByTitleAndCourseID(titlePattern, courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer))
  }

  override def getById(id: Long): Option[TincanPackage] = {
    Option(LFTincanPackageLocalServiceUtil.fetchLFTincanPackage(id)).map(extractPackage)
  }

  override def getManifestByCourseId(courseId: Long, onlyVisible: Boolean): Seq[TincanManifest] = {
    if (!onlyVisible) {
      getByScope(courseId.toInt, ScopeType.Site, courseId.toString)
    }
    else {
      val visiblePackageIds = packageScopeRuleRepository.getPackageIdVisible(ScopeType.Site, Some(courseId.toString))

      if (visiblePackageIds.isEmpty) Seq()
      else {

        val packageQuery = LFTincanPackageLocalServiceUtil.dynamicQuery()
          .add(PropertyFactoryUtil.forName("id").in(visiblePackageIds.toArray))

        val packages = LFTincanPackageLocalServiceUtil.dynamicQuery(packageQuery).asScala.map(_.asInstanceOf[LFTincanPackage])
        packages.map(extract)
      }
    }
  }

  def getByCourseId(courseId: Long): Seq[TincanPackage] = {
    LFTincanPackageLocalServiceUtil.findByCourseID(courseId.toInt).asScala.map(extractPackage)
  }

  override def getByScope(courseID: Int, scope: ScopeType.Value, scopeID: String): Seq[TincanManifest] = {
    LFTincanPackageLocalServiceUtil.findByCourseID(courseID).asScala
      .map(extract)
      .flatMap(fillManifestWithScopeValues(scope, Option(scopeID)))
  }

  override def getByExactScope(courseIds: List[Long], scope: ScopeType.Value, scopeID: String): Seq[TincanManifest] = {
    LFTincanPackageLocalServiceUtil.findByInstance(courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer)).asScala
      .map(extract)
      .flatMap(fillManifestWithScopeValuesWithFilter(scope, Option(scopeID)))
  }

  override def getAllForInstance(courseIds: List[Long]): Seq[TincanManifest] = {
    LFTincanPackageLocalServiceUtil.findByInstance(courseIds.map(_.toInt).toArray.map(i => i: java.lang.Integer)).asScala
      .filter(_ != null).map(extract)
      .flatMap(fillManifestWithScopeValues())
  }

  override def getOnlyVisible(scope: ScopeType.Value, scopeID: String, titlePattern: Option[String], date: DateTime): Seq[TincanPackage] = {
    val visiblePackageIds = packageScopeRuleRepository.getPackageIdVisible(scope, Option(scopeID))
    getPackages(visiblePackageIds, titlePattern, date)
  }

  override def getInstanceScopeOnlyVisible(courseIds: List[Long], titlePattern: Option[String], date: DateTime): Seq[TincanPackage] = {
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
      val packageQuery = LFTincanPackageLocalServiceUtil.dynamicQuery()
        .add(PropertyFactoryUtil.forName("id").in(packageIds.toArray))
        .add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.isNull("beginDate"),
        RestrictionsFactoryUtil.lt("beginDate", date.toDate))
        )
        .add(RestrictionsFactoryUtil.or(
        RestrictionsFactoryUtil.isNull("endDate"),
        RestrictionsFactoryUtil.gt("endDate", date.toDate))
        )

      for (ids <- courseIds)
        packageQuery.add(RestrictionsFactoryUtil.in("courseID", ids.map(_.toInt).asJava))

      var packages = LFTincanPackageLocalServiceUtil.dynamicQuery(packageQuery).asScala.map(_.asInstanceOf[LFTincanPackage])

      packages = titlePattern.map(_.toLowerCase) match {
        case Some(title) => packages.filter(_.getTitle.toLowerCase.contains(title))
        case None => packages
      }

      packages.map(extractPackage)
    }
  }

  override def modify(id: Long, title: String, summary: String, beginDate: Option[DateTime], endDate: Option[DateTime]) = {
    val lfEntity = LFTincanPackageLocalServiceUtil.getLFTincanPackage(id)
    lfEntity.setTitle(title)
    lfEntity.setSummary(summary)
    lfEntity.setBeginDate(beginDate.map(_.toDate).orNull)
    lfEntity.setEndDate(endDate.map(_.toDate).orNull)
    val updatedEntity = LFTincanPackageLocalServiceUtil.updateLFTincanPackage(lfEntity)
    extractPackage(updatedEntity)
  }

  override def setLogo(id: Long, logo: Option[String]): Unit = {
    val lfEntity = LFTincanPackageLocalServiceUtil.getLFTincanPackage(id)
    logo.foreach(l => {
      lfEntity.setLogo(l)
      LFTincanPackageLocalServiceUtil.updateLFTincanPackage(lfEntity)
    })
  }

  private def fillManifestWithScopeValues(scope: ScopeType.Value = ScopeType.Instance, scopeID: Option[String] = None): (TincanManifest) => Seq[TincanManifest] = {
    manifest => {
      val scopeRules = packageScopeRuleRepository.getAll(manifest.id.toInt, scope, scopeID)
      if (scopeRules.isEmpty) {
        Seq(manifest)
      } else {
        scopeRules.map(fillByScopeRule(manifest))
      }
    }
  }

  private def fillByScopeRule(manifest: TincanManifest): (PackageScopeRule) => TincanManifest = {
    scopeRule =>
      manifest.copy(
        visibility = Option(scopeRule.visibility),
        isDefault = scopeRule.isDefault)
  }

  private def extractPackage(lfEntity: LFTincanPackage): TincanPackage = {
    import com.arcusys.learn.storage.impl.liferay.LiferayCommon._

    TincanPackage(
      lfEntity.getId,
      lfEntity.getTitle,
      Option(lfEntity.getSummary),
      lfEntity.getCourseID.toOption,
      Option(lfEntity.getLogo),
      Option(lfEntity.getBeginDate).map(new DateTime(_)),
      Option(lfEntity.getEndDate).map(new DateTime(_))
    )
  }

  private def extract(lfEntity: LFTincanPackage) = {
    import com.arcusys.learn.storage.impl.liferay.LiferayCommon._
    val lfLimitPK = new LFLessonLimitPK(lfEntity.getId, LessonType.Tincan.toString)
    val lessonLimit = Option(LFLessonLimitLocalServiceUtil.fetchLFLessonLimit(lfLimitPK))

    TincanManifest(
      lfEntity.getId.toInt,
      lfEntity.getTitle,
      Option(lfEntity.getSummary),
      lfEntity.getCourseID.toOption,
      None, Option(lfEntity.getLogo),
      isDefault = false,
      lessonLimit.flatMap(_.getPassingLimit.toOption).getOrElse(0),
      lessonLimit.flatMap(_.getRerunInterval.toOption).getOrElse(0),
      PeriodTypes(lessonLimit.flatMap(_.getRerunIntervalType.toOption).getOrElse("")),
      Option(lfEntity.getBeginDate).map(new DateTime(_)),
      Option(lfEntity.getEndDate).map(new DateTime(_))
    )
  }

  private def fillManifestWithScopeValuesWithFilter(scope: ScopeType.Value = ScopeType.Instance, scopeID: Option[String] = None): (TincanManifest) => Seq[TincanManifest] = {
    manifest => {
      val scopeRules = packageScopeRuleRepository.getAll(manifest.id.toInt, scope, scopeID)
      if (scopeRules.isEmpty) {
        Seq()
      } else {
        scopeRules.map(fillByScopeRule(manifest))
      }
    }
  }

  private def getInt(value: Any): Int = {
    value match {
      case i: Int => i
      case l: Long => l.toInt
      case _ => 0
    }
  }
}
