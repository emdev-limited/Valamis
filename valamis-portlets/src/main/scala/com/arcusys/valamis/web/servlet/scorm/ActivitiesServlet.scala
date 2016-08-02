package com.arcusys.valamis.web.servlet.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.{Activity, LeafActivity}
import com.arcusys.valamis.lesson.scorm.model.tracking.ActivityStateNode
import com.arcusys.valamis.lesson.scorm.service.ActivityServiceContract
import com.arcusys.valamis.util.TreeNode
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.BaseApiController
import com.arcusys.valamis.web.servlet.request.Parameter

class ActivitiesServlet extends BaseApiController {

  lazy val activityManager = inject[ActivityServiceContract]

  private def transform(activity: Activity) =
    Map(
      "id" -> activity.id,
      "title" -> activity.title,
      "visible" -> activity.visible,
      "childActivities" -> Nil,
      "leafData" -> (activity match {
        case leaf: LeafActivity => Map("resourceID" -> leaf.resourceIdentifier, "resourceParam" -> leaf.resourceParameters.getOrElse(""))
        case _                  => Map()
      })
    )


  private def transform(node: TreeNode[Activity]) = {

    def mapNode(node: TreeNode[Activity]): Map[String, Any] = Map(
      "id" -> node.item.id,
      "title" -> node.item.title,
      "visible" -> node.item.visible,
      "childActivities" -> node.children.map(mapNode),
      "leafData" -> (node.item match {
        case leaf: LeafActivity => Map("resourceID" -> leaf.resourceIdentifier, "resourceParam" -> leaf.resourceParameters.getOrElse(""))
        case _                  => Map()
      })
    )

    mapNode(node)
  }

  private def transform(node: ActivityStateNode) = {

    def mapNode(node: ActivityStateNode): Map[String, Any] = Map(
      "id" -> node.item.activity.id,
      "title" -> node.item.activity.title,
      "visible" -> node.item.activity.visible,
      "childActivities" -> node.availableChildren.map(mapNode),
      "leafData" -> (node.item.activity match {
        case leaf: LeafActivity => Map("resourceID" -> leaf.resourceIdentifier, "resourceParam" -> leaf.resourceParameters.getOrElse(""))
        case _                  => Map()
      })
    )

    mapNode(node)
  }

  get("/manifestactivities(/)") {
    val userID = getUserId.toInt

    val packageID = Parameter("packageID")(this).intRequired
    val organizationID = Parameter("organizationID")(this).required

    val currentAttempt = activityManager.getActiveAttempt(userID, packageID, organizationID)
    val tree = activityManager.getActivityStateTreeForAttemptOrCreate(currentAttempt)

    JsonHelper.toJson(tree.availableChildren.map(transform))
  }

  /* get("/manifestactivities/package/:packageID/organization/:organizationID/activity/:id") {
    val packageID = parameter("packageID").intRequired
    val id = parameter("id").required
    jsonModel(activityStorage.get(packageID, id))
  }*/
}