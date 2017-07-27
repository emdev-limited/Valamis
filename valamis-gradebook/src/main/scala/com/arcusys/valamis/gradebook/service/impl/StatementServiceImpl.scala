package com.arcusys.valamis.gradebook.service.impl

import java.util.UUID

import com.arcusys.learn.liferay.LiferayClasses.{LCompany, LUser}
import com.arcusys.learn.liferay.services.{CompanyLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.gradebook.service.StatementService
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.lrssupport.lrs.service.{LrsClientManager, LrsRegistration}
import org.joda.time.DateTime

/**
  * Created by iantonov on 09.06.17.
  */
abstract class StatementServiceImpl extends StatementService {

  def lrsRegistration: LrsRegistration

  def userLocalServiceHelper: UserLocalServiceHelper

  def lrsClientManager: LrsClientManager

  val LanguageEn = "en-US"
  val MinGrade = 0
  val MaxGrade = 100

  override def sendStatementUserReceivesGrade(studentId: Long,
                                              teacherId: Long,
                                              lesson: Lesson,
                                              grade: Float,
                                              comment: Option[String]): Unit = {

    for (
      student <- userLocalServiceHelper.fetchUser(studentId);
      teacher <- userLocalServiceHelper.fetchUser(teacherId)
    ) {
      val companyId = student.getCompanyId
      val company = CompanyLocalServiceHelper.getCompany(companyId)
      val studentAgent = createAgent(student, company)

      val verb = Verb("http://adlnet.gov/expapi/verbs/scored", Map(LanguageEn -> "scored"))

      val objectActivity = Activity(
        id = createActivityId(lesson.id, companyId),
        name = Option(Map(LanguageEn -> lesson.title)),
        theType = Option("http://adlnet.gov/expapi/activities/course"),
        description = Option(Map(LanguageEn -> lesson.description)))


      val score = Option(Score(
        scaled = Some(grade),
        raw = Some(grade * 100),
        min = Some(MinGrade),
        max = Some(MaxGrade)))

      val success = Option(grade > lesson.scoreLimit)
      val result = Option(Result(
        score = score,
        success = success,
        completion = None,
        response = comment,
        duration = None,
        extensions = None))


      val teacherAgent = createAgent(teacher, company)
      val statement = Statement(
        Option(UUID.randomUUID),
        studentAgent,
        verb,
        objectActivity,
        result,
        context = Some(Context(
          instructor = Some(teacherAgent)
        )),
        timestamp = DateTime.now,
        stored = DateTime.now)

      val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All,
        host = PortalUtilHelper.getHostWithPort(companyId))(companyId).auth

      lrsClientManager.statementApi(_.addStatement(statement), Some(lrsAuth))(companyId)

    }

  }

  private def createActivityId(id: Long, companyId: Long): String = {
    val uriValamis = "valamis"
    val uriType = "lessons"
    val prefix = PortalUtilHelper.getHostWithPort(companyId)
    s"$prefix/$uriValamis/$uriType/$id"
  }

  def createAgent(user: LUser, company: LCompany): Agent = {
    val homePage = "http://" + company.getVirtualHostname
    val account = Account(homePage, user.getUserUuid)
    Agent(name = Option(user.getFullName), account = Option(account))
  }

}
