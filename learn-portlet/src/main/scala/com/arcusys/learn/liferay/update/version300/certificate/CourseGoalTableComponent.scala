package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait CourseGoalTableComponent extends CertificateTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class CourseGoal(certificateId: Long,
                        courseId: Long,
                        periodValue: Int,
                        periodType: PeriodTypes.Value,
                        arrangementIndex: Int,
                        id: Option[Long] = None,
                        isOptional: Boolean = false)

  class CourseGoalTable(tag: Tag) extends Table[CourseGoal](tag, tblName("CERT_COURSE_GOAL")) {
    implicit val ValidPeriodTypeMapper = MappedColumnType.base[PeriodTypes.PeriodType, String](
      s => s.toString,
      s => PeriodTypes.withName(s)
    )

    def id = column[Long]("ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def courseId = column[Long]("COURSE_ID")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (certificateId, courseId, periodValue, periodType, arrangementIndex, id.?, isOptional) <> (CourseGoal.tupled, CourseGoal.unapply)
  }

  val courseGoals = TableQuery[CourseGoalTable]
}
