package com.arcusys.valamis.web.configuration.database

import java.sql.SQLException

import com.arcusys.slick.drivers.{DB2Driver, OracleDriver, SQLServerDriver}
import com.arcusys.valamis.certificate.storage.schema._
import com.arcusys.valamis.gradebook.storage.{CourseGradeTableComponent, CourseTableComponent}
import com.arcusys.valamis.lesson.scorm.storage.ScormManifestTableComponent
import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonGradeTableComponent, LessonTableComponent}
import com.arcusys.valamis.lesson.tincan.storage.{LessonCategoryGoalTableComponent, TincanActivityTableComponent}
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickDBInfo, SlickProfile}
import com.arcusys.valamis.persistence.impl.file.FileTableComponent
import com.arcusys.valamis.persistence.impl.scorm.schema._
import com.arcusys.valamis.persistence.impl.settings.{ActivityToStatementTableComponent, StatementToActivityTableComponent}
import com.arcusys.valamis.persistence.impl.social.schema.{CommentTableComponent, LikeTableComponent}
import com.arcusys.valamis.persistence.impl.uri.TincanUriTableComponent
import com.arcusys.valamis.settings._
import com.arcusys.valamis.lrssupport.tables._
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent
import slick.driver.{HsqldbDriver, JdbcProfile}
import slick.jdbc.meta._

import scala.concurrent.ExecutionContext.Implicits.global

class CreateTables(dbInfo: SlickDBInfo)
  extends SlickProfile
    with LogSupport
    with DatabaseLayer
    with StoryTreeTableComponent
    with LikeTableComponent
    with CommentTableComponent
    with CertificateTableComponent
    with CertificateStateTableComponent
    with FileTableComponent
    with LessonCategoryGoalTableComponent
    with CourseTableComponent
    with SettingTableComponent
    with StatementToActivityTableComponent
    with TincanUriTableComponent
    with LessonTableComponent
    with TincanActivityTableComponent
    with ScormManifestTableComponent
    with LessonAttemptsTableComponent
    with ActivityToStatementTableComponent
    with LessonGradeTableComponent
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

  import driver.api._

  val tables = Seq(activityToStatement,
    certificates, certificateStates,
    files,
    lessonCategoryGoals,
    trees, nodes, packages,
    likes, comments,
    completedCourses,
    statementToActivity,
    tincanUris,
    lessons, lessonLimits, playerLessons, lessonViewers, lessonAttempts, invisibleLessonViewers,
    tincanActivitiesTQ, scormManifestsTQ,
    lessonGrades, courseGrades,
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
          try {
            execSync(sql"""SELECT COUNT(*) FROM #$tableName WHERE 1 = 0""".as[Int].headOption)
            true
          } catch {
            case _: SQLException => false
          }
      case driver: HsqldbDriver =>
        val action = MTable.getTables(Some("PUBLIC"), Some("PUBLIC"), Some(tableName), Some(Seq("TABLE"))).headOption
        execSync(action).isDefined
      case DB2Driver =>
        execSync(driver.defaultTables).map(_.name.name).contains(tableName)
      case _ => execSync(MTable.getTables(tableName).headOption).isDefined
    }
  }

  def create() {
    if (!hasTables) {
      execSyncInTransaction(
        DBIO.sequence(
          createSettingsSchema(db) +: tables.map(_.schema.create)
        ).transactionally
      )
    }
  }

  override val jdbcProfile: JdbcProfile = dbInfo.slickProfile
}
