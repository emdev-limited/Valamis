package com.arcusys.valamis.web.configuration.database

import java.sql.SQLException

import com.arcusys.slick.drivers.{OracleDriver, SQLServerDriver}
import com.arcusys.valamis.certificate.storage.schema.{CertificateGoalGroupTableComponent, _}
import com.arcusys.valamis.gradebook.storage.{CourseGradeTableComponent, CourseTableComponent}
import com.arcusys.valamis.lesson.scorm.storage.ScormManifestTableComponent
import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonGradeTableComponent, LessonTableComponent}
import com.arcusys.valamis.lesson.tincan.storage.{LessonCategoryGoalTableComponent, TincanActivityTableComponent}
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.persistence.impl.file.FileTableComponent
import com.arcusys.valamis.persistence.impl.lrs.{LrsEndpointTableComponent, TokenTableComponent}
import com.arcusys.valamis.persistence.impl.scorm.schema._
import com.arcusys.valamis.persistence.impl.settings.{ActivityToStatementTableComponent, SettingTableComponent, StatementToActivityTableComponent}
import com.arcusys.valamis.persistence.impl.slide.SlideTableComponent
import com.arcusys.valamis.persistence.impl.social.schema.{CommentTableComponent, LikeTableComponent}
import com.arcusys.valamis.persistence.impl.uri.TincanUriTableComponent
import slick.driver.HsqldbDriver
import slick.jdbc._
import slick.jdbc.meta._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CreateTables(dbInfo: SlickDBInfo)
  extends SlickProfile
    with LikeTableComponent
    with CommentTableComponent
    with CertificateTableComponent
    with ActivityGoalTableComponent
    with CourseGoalTableComponent
    with PackageGoalTableComponent
    with StatementGoalTableComponent
    with AssignmentGoalTableComponent
    with CertificateStateTableComponent
    with FileTableComponent
    with TokenTableComponent
    with LessonCategoryGoalTableComponent
    with SlideTableComponent
    with CourseTableComponent
    with SettingTableComponent
    with StatementToActivityTableComponent
    with LrsEndpointTableComponent
    with TincanUriTableComponent
    with LessonTableComponent
    with TincanActivityTableComponent
    with ScormManifestTableComponent
    with LessonAttemptsTableComponent
    with CertificateGoalStateTableComponent
    with ActivityToStatementTableComponent
    with CertificateMemberTableComponent
    with LessonGradeTableComponent
    with CertificateGoalTableComponent
    with CertificateGoalGroupTableComponent
    with ActivityDataMapTableComponent
    with ActivityStateNodeTableComponent
    with AttemptDataTableComponent
    with ActivityStateTreeTableComponent
    with ActivityStateTableComponent
    with ActivityTableComponent
    with AttemptTableComponent
    with ChildrenSelectionTableComponent
    with ConditionRuleItemTableComponent
    with ConditionRuleTableComponent
    with GlblObjectiveStateTableComponent
    with ObjectiveMapTableComponent
    with ObjectiveStateTableComponent
    with ObjectiveTableComponent
    with ResourceTableComponent
    with RollupContributionTableComponent
    with RollupRuleTableComponent
    with ScormUserComponent
    with SeqPermissionsTableComponent
    with SequencingTableComponent
    with SequencingTrackingTableComponent
    with CourseGradeTableComponent {

  val db = dbInfo.databaseDef
  val driver = dbInfo.slickProfile

  import driver.simple._

  val dbTimeout = Duration.Inf
  val tables = Seq(
    certificates, certificateGoalGroups, certificateGoals, certificateStates,
    activityGoals, courseGoals, packageGoals, statementGoals, assignmentGoals,
    files,
    tokens,
    lessonCategoryGoals,
    likes, comments,
    slideThemes, slideSets, slides, slideElements, devices, slideElementProperties, slideProperties,
    completedCourses,
    settings, statementToActivity, lrsEndpoint,
    tincanUris,
    lessons, lessonLimits, playerLessons, lessonViewers, lessonAttempts,
    tincanActivitiesTQ, scormManifestsTQ,
    certificateGoalStates, certificateMembers,
    activityToStatement, lessonGrades, courseGrades,
    activityDataMapTQ,
    activityStateNodeTQ,
    scormUsersTQ,
    attemptTQ,
    activityStateTreeTQ,
    activityStateTQ,
    activityTQ,
    attemptDataTQ,
    childrenSelectionTQ,
    sequencingTQ,
    rollupRuleTQ,
    conditionRuleTQ,
    conditionRuleItemTQ,
    glblObjectiveStateTQ,
    objectiveTQ,
    objectiveStateTQ,
    objectiveMapTQ,
    resourceTQ,
    rollupContributionTQ,
    seqPermissionsTQ,
    sequencingTrackingTQ)

  private def hasTables: Boolean = {
    tables.headOption.fold(true)(t => hasTable(t.baseTableRow.tableName))
  }

  private def hasTable(tableName: String): Boolean = {
    driver match {
      case SQLServerDriver | OracleDriver =>
        db.withSession { implicit s =>
          try {
            StaticQuery.queryNA[String](s"SELECT * FROM $tableName WHERE 1 = 0").list
            true
          } catch {
            case e: SQLException => false
          }
        }
      case driver: HsqldbDriver =>
        val action = MTable.getTables(Some("PUBLIC"), Some("PUBLIC"), Some(tableName), Some(Seq("TABLE"))).headOption
        Await.result(db.run(action), Duration.Inf).isDefined
      case _ => Await.result(db.run(MTable.getTables(tableName).headOption), Duration.Inf).isDefined
    }
  }

  def create() {
    if (!hasTables) {
      // TODO: combine ddl to single query
      db.withTransaction { implicit s =>
        tables.foreach(_.ddl.create)
      }
    }
  }
}
