package com.arcusys.valamis.web.listener

import com.arcusys.valamis.certificate.model.goal.GoalStatuses
import com.arcusys.valamis.certificate.storage.{AssignmentGoalStorage, CertificateGoalStateRepository}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import org.joda.time.DateTime

class AssignmentMessageListener extends MessageListener with Injectable {
  val bindingModule: BindingModule = Configuration
  private lazy val certificateGoalStorage = inject[CertificateGoalStateRepository]
  private lazy val assignmentGoalStorage = inject[AssignmentGoalStorage]

  override def receive(message: Message): Unit = {
    if(message.getString("state") == "completed") {
      val assignmentId = Option(message.getLong("assignmentId")).filter(_ > 0).getOrElse(0L)
      val userIdOption = Option(message.getLong("userId")).filter(_ > 0)
      val assignmentGoals = assignmentGoalStorage.getByAssignmentId(assignmentId)

      userIdOption.foreach { userId =>
        assignmentGoals.foreach { goal =>
          certificateGoalStorage.modify(goal.goalId, userId, GoalStatuses.Success, new DateTime)
        }
      }
    }
  }
}