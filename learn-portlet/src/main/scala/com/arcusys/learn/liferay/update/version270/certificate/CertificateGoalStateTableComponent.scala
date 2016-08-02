package com.arcusys.learn.liferay.update.version270.certificate

import com.arcusys.valamis.certificate.model.goal.{GoalStatuses, GoalType}
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateGoalStateTableComponent
  extends CertificateTableComponent
      with TypeMapper { self: SlickProfile =>
    import driver.simple._

  type CertificateGoalState = (Option[Long], Long, Long, Long, GoalType.Value, GoalStatuses.Value, DateTime)

    implicit lazy val certificateGoalStatusTypeMapper = enumerationMapper(GoalStatuses)
    implicit lazy val certificateGoalTypeMapper = enumerationMapper(GoalType)


    class CertificateGoalStateTable(tag: Tag) extends Table[CertificateGoalState](tag, tblName("CERT_GOAL_STATE")) {
      def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
      def userId = column[Long]("USER_ID")
      def certificateId = column[Long]("CERTIFICATE_ID")
      def goalId = column[Long]("GOAL_ID")
      def goalType = column[GoalType.Value]("GOAL_TYPE")
      def status = column[GoalStatuses.Value]("STATUS")
      def modifiedDate = column[DateTime]("MODIFIED_DATE")

      def * = (id.?, userId, certificateId, goalId, goalType, status, modifiedDate)

    }

    val certificateGoalStates = TableQuery[CertificateGoalStateTable]
  }
