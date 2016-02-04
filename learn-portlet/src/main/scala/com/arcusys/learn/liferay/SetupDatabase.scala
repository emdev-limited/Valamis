package com.arcusys.learn.liferay

import java.sql.SQLException

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.utils.LiferayContext
import com.arcusys.slick.drivers.SQLServerDriver
import com.arcusys.valamis.certificate.schema._
import com.arcusys.valamis.content.schema.ContentTableComponent
import com.arcusys.valamis.core.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.course.CourseTableComponent
import com.arcusys.valamis.file.FileTableComponent
import com.arcusys.valamis.lesson.{PackageScopeRuleTableComponent, PackageCategoryGoalTableComponent}
import com.arcusys.valamis.lrs.{LrsEndpointTableComponent, TokenTableComponent}
import com.arcusys.valamis.settings.{StatementToActivityTableComponent, SettingTableComponent}
import com.arcusys.valamis.slide.SlideTableComponent
import com.arcusys.valamis.social.schema.{CommentTableComponent, LikeTableComponent}
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.LogFactoryUtil
import scala.slick.driver.HsqldbDriver
import scala.slick.jdbc._
import scala.slick.jdbc.meta._

class SetupDatabase extends SimpleAction {
  val logger = LogFactoryUtil.getLog(getClass)

  override def run(companyIds: Array[String]): Unit = {
    LiferayContext.init(companyIds.head.toLong)

    def dbInfo = Configuration.inject[SlickDBInfo](None)

    new SlickDbCreator(dbInfo).create()
  }
}

class SlickDbCreator(dbInfo: SlickDBInfo)
  extends SlickProfile
  with LikeTableComponent
  with CommentTableComponent
  with CertificateTableComponent
  with ActivityGoalTableComponent
  with CourseGoalTableComponent
  with PackageGoalTableComponent
  with StatementGoalTableComponent
  with CertificateStateTableComponent
  with FileTableComponent
  with TokenTableComponent
  with PackageCategoryGoalTableComponent
  with SlideTableComponent
  with CourseTableComponent
  with SettingTableComponent
  with StatementToActivityTableComponent
  with LrsEndpointTableComponent
  with ContentTableComponent
  with PackageScopeRuleTableComponent
{

  val db = dbInfo.databaseDef
  val driver = dbInfo.slickProfile
  lazy val templateCreator = new CreateDefaultTemplates(dbInfo)
  lazy val deviceCreator = new CreateDefaultDevices(dbInfo)

  import driver.simple._

  val tables = Seq(
    certificates, activityGoals, courseGoals, packageGoals, statementGoals, certificateStates,
    files,
    tokens,
    packageCategoryGoals,
    likes, comments,
    slideThemes, slideSets, slides, slideElements, devices, slideElementProperties, slideProperties,
    completedCourses,
    settings, statementToActivity, lrsEndpoint,
    questionCategories,questions,plainTexts,answers,
    packageScopeRule
  )

  private def hasTables: Boolean = {
    hasTable(certificates.baseTableRow.tableName)
  }

  private def hasTable(tableName: String): Boolean = {
    db.withSession { implicit s =>
      driver match {
        case driver: SQLServerDriver =>
          try {
            StaticQuery.queryNA[String](s"SELECT * FROM $tableName WHERE 1 = 0").list
            true
          } catch {
            case e: SQLException => false
          }
        case driver: HsqldbDriver =>
          MTable.getTables(Some("PUBLIC"), Some("PUBLIC"), Some(tableName), Some(Seq("TABLE"))).firstOption.isDefined
        case _ => MTable.getTables(tableName).firstOption.isDefined
      }
    }
  }

  def create() {
    if (!hasTables) {
      // TODO: combine ddl to single query
      db.withTransaction { implicit s =>
        tables.foreach(_.ddl.create)
      }
      addDefaultValues()
    }
  }

  private def addDefaultValues() {
    db.withTransaction { implicit session =>

      val defaultSlideSet = slideSets.filter { e =>
        e.title === defaultSlideSetTemplate.title &&
          e.description === defaultSlideSetTemplate.description &&
          e.courseId === defaultSlideSetTemplate.courseId &&
          e.logo.isEmpty &&
          e.isTemplate === defaultSlideSetTemplate.isTemplate &&
          e.isSelectedContinuity === defaultSlideSetTemplate.isSelectedContinuity
      }.firstOption

      if (defaultSlideSet.isEmpty)
        slideSets += defaultSlideSetTemplate
    }

    db.withTransaction { implicit session =>
      val defaultThemes = slideThemes.filter(_.isDefault === true).firstOption

      if (defaultThemes.isEmpty)
        slideThemes ++= defaultSlideThemes
    }

    val defaultTemplate = db.withSession { implicit s =>
      slides.filter(_.isTemplate === true).firstOption
    }

    if (defaultTemplate.isEmpty) templateCreator.create()

    val defaultDevices = db.withSession { implicit session =>
       devices.firstOption
    }

    if (defaultDevices.isEmpty) deviceCreator.create
  }
}

