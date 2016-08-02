package com.arcusys.valamis.web.servlet.scorm

import com.arcusys.valamis.lesson.scorm.model.tracking.{ActivityState, ActivityStateNode, ObjectiveState}
import com.arcusys.valamis.lesson.scorm.service.ActivityServiceContract
import com.arcusys.valamis.lesson.scorm.service.lms.DataModelService
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.service.Sanitizer
import com.arcusys.valamis.web.servlet.base.BaseApiController
import com.arcusys.valamis.web.servlet.request.Parameter
import org.scalatra.{ScalatraBase, SinatraRouteMatcher}

class RteServlet extends BaseApiController {

  lazy val sanitizer = inject[Sanitizer]

  //next line fixes 404
  implicit override def string2RouteMatcher(path: String) = new SinatraRouteMatcher(path)

  private lazy val activityManager = inject[ActivityServiceContract]

  implicit val scalatra: ScalatraBase = this

  post("/rte/Initialize") {
    val userID = getUserId.toInt
    val packageID = Parameter("packageID").intRequired
    val organizationID = Parameter("organizationID").required
    val currentAttempt = activityManager.getActiveAttempt(userID, packageID, organizationID)

    val stateTree = activityManager.getActivityStateTreeForAttemptOption(currentAttempt)
    if (stateTree.isEmpty) {
      activityManager.createActivityStateTreeForAttempt(currentAttempt)
      JsonHelper.toJson("status" -> false)
    } else {
      JsonHelper.toJson("status" -> true)
    }
  }

  get("/rte/GetValue/:key") {
    val userID = getUserId.toInt
    val packageID = Parameter("packageID").intRequired
    val activityID = Parameter("activityID").required
    val currentAttempt = activityManager.getLastAttempltOption(userID, packageID).getOrElse(halt(404, "Attempt not found for this SCO and user"))

    val dataModel = new DataModelService(currentAttempt, activityID)
    JsonHelper.toJson(dataModel.getValue(Parameter("key").required))
  }

  get("/rte/GetValues") {
    val userID = getUserId.toInt
    val packageID = Parameter("packageID").intRequired
    val activityID = Parameter("activityID").required
    val currentAttempt = activityManager.getLastAttempltOption(userID, packageID).getOrElse(halt(404, "Attempt not found for this SCO and user"))

    val dataModel = new DataModelService(currentAttempt, activityID)
    JsonHelper.toJson(dataModel.getValues)
  }

  post("/rte/SetValue") {
    val userID = getUserId.toInt
    val value = sanitizer.sanitize(Parameter("value").required)
    val packageID = Parameter("packageID").intRequired
    val activityID = Parameter("activityID").required
    val currentAttempt = activityManager.getLastAttempltOption(userID, packageID).getOrElse(halt(404, "Attempt not found for this SCO and user"))

    val dataModel = new DataModelService(currentAttempt, activityID)
    dataModel.setValue(Parameter("key").required, value)
  }

  post("/rte/SetValues") {
    val userID = getUserId.toInt
    val packageID = Parameter("packageID").intRequired
    val activityID = Parameter("activityID").required
    val currentAttempt = activityManager.getLastAttempltOption(userID, packageID).getOrElse(halt(404, "Attempt not found for this SCO and user"))

    val amount = Parameter("amount").intRequired
    val dataModel = new DataModelService(currentAttempt, activityID)
    (0 until amount).foreach(index => {
      dataModel.setValue(Parameter("dataKey" + index).required, Parameter("dataValue" + index).required)
    })
  }

  get("/rte/ActivityInformation/:activityID") {
    def serializeObjective(id: Option[String], state: ObjectiveState) = {
      Some(
        Map("identifier" -> id,
          "objectiveProgressStatus" -> state.getSatisfiedStatus.isDefined,
          "objectiveSatisfiedStatus" -> state.getSatisfiedStatus,
          "objectiveMeasureStatus" -> state.getNormalizedMeasure.isDefined,
          "objectiveNormalizedMeasure" -> state.getNormalizedMeasure
        )
      )
    }

    def serializePrimaryObjective(activityState: ActivityState) = {
      val primaryObjective = activityState.activity.sequencing.primaryObjective
      if (primaryObjective.isDefined) {
        val primaryObjectiveState = activityState.objectiveStates(None)
        serializeObjective(primaryObjective.get.id, primaryObjectiveState)
      } else {
        None
      }
    }

    val userID = getUserId.toInt
    val packageID = Parameter("packageID").intRequired
    val attempt = activityManager.getLastAttempltOption(userID, packageID).getOrElse(halt(404, "Attempt not found for this SCO and user"))
    val tree = activityManager.getActivityStateTreeForAttemptOption(attempt).get //OrElse(throw new Exception("Activity tree should exist!"))
    val activity = tree(Parameter("activityID").required)
    JsonHelper.toJson(Map("attemptProgressStatus" -> activity.get.item.getCompletionStatus().isEmpty,
      "attemptCompletionStatus" -> activity.get.item.getCompletionStatus(),
      "attemptCompletionAmount" -> activity.get.item.attemptCompletionAmount,
      "isActivitySuspended" -> activity.get.item.suspended,
      "primaryObjective" -> serializePrimaryObjective(activity.get.item),
      "activityObjectives" -> activity.get.item.activity.sequencing.nonPrimaryObjectives.map(objective =>
        serializeObjective(objective.id, activity.get.item.objectiveStates(objective.id)))
    ))
  }

  post("/rte/ActivityInformation/:activityID") {
    def deserializeObjective(base: String, state: ObjectiveState) {
      state.setSatisfiedStatus(Parameter(base + "[objectiveProgressStatus]").booleanOption("null"))
      state.setNormalizedMeasure(Parameter(base + "[objectiveNormalizedMeasure]").bigDecimalOption("null"))
    }

    def deserializePrimaryObjective(activity: ActivityStateNode) {
      val activityState = activity.item
      val primaryObjective = activityState.activity.sequencing.primaryObjective
      if (primaryObjective.isDefined) {
        val primaryObjectiveState = activityState.objectiveStates(None)
        deserializeObjective("primaryObjective", primaryObjectiveState)
      }
    }

    def deserializeNonPrimaryObjective(activity: ActivityStateNode, index: String) {
      val activityState = activity.item
      val id = Parameter("activityObjectives[" + index + "][identifier]").required
      val objectiveState = activityState.objectiveStates(Some(id))
      deserializeObjective("activityObjectives[" + index + "]", objectiveState)
    }

    val userID = getUserId.toInt
    val packageID = Parameter("packageID").intRequired
    val attempt = activityManager.getLastAttempltOption(userID, packageID).getOrElse(halt(404, "Attempt not found for this SCO and user"))
    val tree = activityManager.getActivityStateTreeForAttemptOption(attempt).getOrElse(throw new Exception("Activity tree should exist!"))
    val activity = tree(Parameter("activityID").required)

    activity.get.item.setCompletionStatus(Parameter("attemptCompletionStatus").booleanOption("null"))
    activity.get.item.attemptCompletionAmount = Parameter("attemptCompletionAmount").bigDecimalOption("null")
    activity.get.item.suspended = Parameter("isActivitySuspended").booleanRequired
    val activityObjectivesCount = Parameter("activityObjectivesCount").intRequired
    deserializePrimaryObjective(activity.get)
    (0 until activityObjectivesCount).foreach(index => deserializeNonPrimaryObjective(activity.get, index.toString))
    activityManager.updateActivityStateTree(attempt.id.toInt, tree)
  }

  post("/rte/Commit") {

  }
}
