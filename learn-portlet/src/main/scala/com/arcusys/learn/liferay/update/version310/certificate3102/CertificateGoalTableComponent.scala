package com.arcusys.learn.liferay.update.version310.certificate3102

import com.arcusys.learn.liferay.update.migration.GoalType
import com.arcusys.valamis.updaters.version310.certificate.CertificateTableComponent
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

trait CertificateGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalGroupTableComponent { self: SlickProfile =>

  import driver.simple._

  case class CertificateGoal(id: Long,
                             certificateId: Long,
                             goalType: GoalType.Value,
                             periodValue: Int,
                             periodType: PeriodType,
                             arrangementIndex: Int,
                             isOptional: Boolean = false,
                             groupId: Option[Long],
                             oldGroupId: Option[Long],
                             modifiedDate: DateTime,
                             userId: Option[Long],
                             isDeleted: Boolean = false)

  implicit lazy val certificateGoalTypeMapper = enumerationIdMapper(GoalType)
  implicit lazy val validPeriodTypeMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalTable(tag: Tag)
    extends Table[CertificateGoal](tag, tblName("CERT_GOALS"))
      with CertificateGoalHistoryColumns {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalType = column[GoalType.Value]("GOAL_TYPE")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")
    def groupId = column[Option[Long]]("GROUP_ID")
    def oldGroupId = column[Option[Long]]("OLD_GROUP_ID")

    def * = (id,
      certificateId,
      goalType,
      periodValue,
      periodType,
      arrangementIndex,
      isOptional,
      groupId,
      oldGroupId,
      modifiedDate,
      userId,
      isDeleted) <> (CertificateGoal.tupled, CertificateGoal.unapply)

    def certificateFK = foreignKey(fkName("GOALS_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def groupFK = foreignKey(fkName("GOALS_TO_GROUP"), groupId, certificateGoalGroups)(x => x.id)
    def oldGroupFK = foreignKey(fkName("GOALS_TO_OLD_GROUP"), oldGroupId, certificateGoalGroups)(x => x.id)
  }

  val certificateGoals = TableQuery[CertificateGoalTable]
}