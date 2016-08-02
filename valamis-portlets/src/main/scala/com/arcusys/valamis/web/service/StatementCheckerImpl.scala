package com.arcusys.valamis.web.service

import com.arcusys.learn.liferay.services.{CompanyHelper, ServiceContextHelper, UserLocalServiceHelper}
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.{CertificateService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
import com.arcusys.valamis.lesson.service.UserLessonResultService
import com.arcusys.valamis.lrs.service.util.StatementChecker
import com.arcusys.valamis.lrs.tincan.{Activity, Account, Statement}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

/**
  * Created by pkornilov on 04.03.16.
  */
abstract class StatementCheckerImpl(implicit val bindingModule: BindingModule) extends StatementChecker with Injectable {

  lazy val statementActivityCreator = inject[StatementActivityCreator]
  lazy val certificateCompletionChecker = inject[CertificateStatusChecker]
  def gradeChecker: GradeChecker
  lazy val certificateStateRepository = inject[CertificateStateRepository]
  lazy val lessonResult = inject[UserLessonResultService]
  lazy val certificateService = inject[CertificateService]

  def checkStatements(statements: Seq[Statement], companyIdOpt: Option[Long] = None) = {
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

        statements
          .filter(_.obj.isInstanceOf[Activity])
          .foreach { statement =>
          certificateStateRepository.getBy(userId, CertificateStatuses.InProgress) foreach { state =>
            certificateCompletionChecker.updateStatementGoalState(state, statement, userId)
          }
        }

        certificateService.getAffectedCertificateIds(statements).foreach { certId =>
          if (certificateService.isUserJoined(certId, userId)) {
            certificateCompletionChecker.checkAndGetStatus(certId, userId)
          }
        }

        lessonResult.update(user, statements)
      }
    }
  }


}
