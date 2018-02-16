package com.arcusys.valamis.web.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.{CompanyHelper, MessageBusHelper, ServiceContextHelper, UserLocalServiceHelper}
import com.arcusys.valamis.lesson.service.UserLessonResultService
import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.lrs.tincan.{Account, Activity, Statement}
import com.arcusys.valamis.statements.StatementChecker
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.format.ISODateTimeFormat

/**
  * Created by pkornilov on 04.03.16.
  */
abstract class StatementCheckerImpl(implicit val bindingModule: BindingModule)
  extends StatementChecker
    with Injectable
    with LogSupport {

  lazy val statementActivityCreator = inject[StatementActivityCreator]
  def gradeChecker: GradeChecker
  lazy val lessonResult = inject[UserLessonResultService]

  def checkStatements(statements: Seq[Statement], companyIdOpt: Option[Long] = None): Unit = {
    if (statements.nonEmpty) {
      val companyId = companyIdOpt.getOrElse(CompanyHelper.getCompanyId.longValue())

      val userIdOption = Option(ServiceContextHelper.getServiceContext).map(_.getUserId)
        .filter(_ > 0)
        .orElse{
          val user = statements.head.actor.account match {
            case Some(account: Account) =>
              Option(UserLocalServiceHelper().fetchUserByUuidAndCompanyId(account.name, companyId))
            case _ =>
              statements.head.actor.mBox flatMap {
                email => Option(UserLocalServiceHelper().fetchUserByEmailAddress(companyId, email.replace("mailto:", "")))
              }
          }
          user.filterNot(_.isDefaultUser).map(_.getUserId)
        }
      for (userId <- userIdOption) {
        val user = UserLocalServiceHelper().getUser(userId)
        statementActivityCreator.create(companyId, statements, userId)

        lessonResult.update(user, statements)

        sendNewStatementsMessage(statements, user)
      }
    }
  }

  private def sendNewStatementsMessage(statements: Seq[Statement], user: LUser) = {
    statements
      .filter(_.obj.isInstanceOf[Activity])
      .foreach { s =>
        try {
          val messageValues = new java.util.HashMap[String, AnyRef]()
          messageValues.put("verbId", s.verb.id)
          messageValues.put("objectId", s.obj.asInstanceOf[Activity].id)
          messageValues.put("timestamp", ISODateTimeFormat.dateTime().print(s.timestamp))
          messageValues.put("userId", user.getUserId.toString)
          messageValues.put("companyId", user.getCompanyId.toString)


          MessageBusHelper.sendAsynchronousMessage("valamis/lrs/statement/stored", messageValues)
        } catch {
          case ex: Throwable =>
            log.error(s"Failed to send message to valamis/lrs/statement/stored", ex)
        }
      }
  }
}
