package com.arcusys.learn.liferay.update.version310.certificate3102

import com.arcusys.valamis.updaters.version310.certificate.CertificateTableComponent
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

trait CertificateGoalGroupTableComponent
  extends CertificateTableComponent { self: SlickProfile =>

  import driver.simple._

  trait CertificateGoalHistoryColumns { self: Table[_] =>
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def userId = column[Option[Long]]("USER_ID")
    def isDeleted = column[Boolean]("IS_DELETED")
  }

  case class GoalGroup(id: Long,
                       count: Int,
                       certificateId: Long,
                       periodValue: Int,
                       periodType: PeriodType,
                       arrangementIndex: Int,
                       modifiedDate: DateTime,
                       userId: Option[Long],
                       isDeleted: Boolean = false)

  implicit val periodTypesMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalGroupTable(tag: Tag)
    extends Table[GoalGroup](tag, tblName("CERT_GOALS_GROUP"))
      with CertificateGoalHistoryColumns {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def count = column[Int]("COUNT")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")

    def * = (id,
      count,
      certificateId,
      periodValue,
      periodType,
      arrangementIndex,
      modifiedDate,
      userId,
      isDeleted) <> (GoalGroup.tupled, GoalGroup.unapply)

    def certificateFK = foreignKey(fkName("GOALS_GROUP_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val certificateGoalGroups = TableQuery[CertificateGoalGroupTable]
}