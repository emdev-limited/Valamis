package com.arcusys.valamis.web.servlet.report

import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.web.servlet.report.response._
import com.arcusys.valamis.web.servlet.response.CollectionResponse

trait ReportFacadeContract {
  def getParticipantReport(groupBy: String): Seq[ParticipantResponse]

  def getStudentsLeaderboard(period: String, offset: Int, amount: Int): CollectionResponse[StudentMostActiveResponse]

  def getStatementVerbs: VerbResponse

  def getOverallByTime: OverallByTimeResponse

  def getOverallByPeriod(period: String, from: Long, to: Long): OverallByPeriodResponse

  def getCourseEvent(group: String, groupPeriod: Option[String], period: String, from: Long, to: Long): Seq[CourseEventResponse]

  def getUserLatestStatements(currentUserID: Int, offset: Int, amount: Int): CollectionResponse[Statement]

  def getStudentsLatestStatements(currentUserID: Int, offset: Int, amount: Int): CollectionResponse[Statement]

  def getMostActive(currentUserID: Int, offset: Int, amount: Int): CollectionResponse[StudentMostActiveResponse]

  def getCourseReport(isInstanceScope: Boolean, courseID: Option[Int]): CourseReportResponse

}
