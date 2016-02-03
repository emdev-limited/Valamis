package com.arcusys.learn.scorm.rte.service

import java.net.URLDecoder
import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.web.ServletBase
import com.arcusys.valamis.lesson.scorm.model.manifest.{LeafActivity, ResourceUrl}
import com.arcusys.valamis.lesson.scorm.model.sequencing.{ProcessorResponseDelivery, ProcessorResponseEndSession}
import com.arcusys.valamis.lesson.scorm.service.sequencing.SequencingProcessor
import com.arcusys.valamis.lesson.service.{ActivityServiceContract, LessonLimitChecker, ValamisPackageService}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.scalatra.SinatraRouteMatcher

class SequencingService extends BaseApiController with ServletBase {

  lazy val passingLimitChecker = inject[LessonLimitChecker]

  lazy val packageManager = inject[ValamisPackageService]
  lazy val activityManager = inject[ActivityServiceContract]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  implicit override def string2RouteMatcher(path: String) = new SinatraRouteMatcher(path)

  // get possible navigation types, check which navigation controls should be hidden
  get("/sequencing/NavigationRules/:packageID/:currentScormActivityID") {
    val packageID = parameter("packageID").intRequired
    val activityID = parameter("currentScormActivityID").required
    val activity = activityManager.getActivity(packageID, activityID)
    JsonHelper.toJson("hiddenUI" -> activity.hiddenNavigationControls.map(_.toString))
  }

  post("/sequencing/Tincan/:packageID") {
    val packageID = parameter("packageID").intRequired

    val mainFileName = packageManager.getTincanLaunchWithLimitTest(packageID, getLiferayUser)

    JsonHelper.toJson(Map("launchURL" -> mainFileName))
  }

  get("/sequencing/NavigationRequest/:currentScormPackageID/:currentOrganizationID/:sequencingRequest") {
    val userID = getUserId.toInt
    val packageID = parameter("currentScormPackageID").intRequired
    val organizationID = parameter("currentOrganizationID").required

    val isAvaliable = passingLimitChecker.checkScormPackage(getLiferayUser, packageID)

    if (!isAvaliable) ""
    else {
      val currentAttempt = activityManager.getActiveAttempt(userID, packageID, organizationID)
      val tree = activityManager.getActivityStateTreeForAttemptOrCreate(currentAttempt)

      val processor = new SequencingProcessor(currentAttempt, tree)

      val sequencingRequest = URLDecoder.decode(parameter("sequencingRequest").required, "UTF-8")

      val jsonData = JsonHelper.toJson(processor.process(sequencingRequest) match {
        case ProcessorResponseDelivery(tree) => {
          activityManager.updateActivityStateTree(currentAttempt.id, tree)
          val currentActivityID = tree.currentActivity.map(_.item.activity.id).getOrElse("")
          Map("currentActivity" -> currentActivityID, "endSession" -> false) ++ getActivityData(packageID, currentActivityID)
        }
        case ProcessorResponseEndSession(tree) => {
          activityManager.updateActivityStateTree(currentAttempt.id, tree)
          activityManager.markAsComplete(currentAttempt.id)
          val currentActivityID = tree.currentActivity.map(_.item.activity.id).getOrElse("")
          Map("currentActivity" -> currentActivityID, "endSession" -> true) ++ getActivityData(packageID, currentActivityID)
        }
      })

      contentType = "text/html"
      val headScriptData = scala.xml.Unparsed(
        """
        function findPlayerView(win) {
          var findPlayerTries = 0;
          while ((win.scormPlayerView == null) && (win.parent != null) && (win.parent != win)) {
            findPlayerTries++;
            if (findPlayerTries > 20) return null;
            win = win.parent;
          }
          return win.scormPlayerView;
        }

        function getPlayerView() {
          var thePlayer = findPlayerView(window);
          if ((thePlayer == null)) {
            if ((window.opener != null) && (typeof(window.opener) != "undefined"))
              thePlayer = thePlayer(window.opener);
            }
          return thePlayer;
        }
        function init(){
          getPlayerView().loadView(""" + jsonData + """);
        }""")
      <html>
        <head>
          <script language="javascript">
            { headScriptData }
          </script>
        </head>
        <body onload="init()"></body>
      </html>
    }
  }

  post("/sequencing/setSession") {
    request.getSession.setAttribute("packageId", params("id"))
    request.getSession.setAttribute("packageType", params("type"))
    request.getSession.setAttribute("packageTitle", params("title"))
    request.getSession.setAttribute("playerID", params("playerID"))
  }
  post("/sequencing/clearSession") {
    request.getSession.removeAttribute("packageId")
    request.getSession.removeAttribute("packageType")
    request.getSession.removeAttribute("packageTitle")
    request.getSession.removeAttribute("playerID")
  }

  // private methods
  private def getActivityData(packageID: Int, id: String): Map[String, Any] = {
    val activityOption = activityManager.getActivityOption(packageID, id)
    if (activityOption.isDefined) {
      val activity = activityOption.get
      if (activity.isInstanceOf[LeafActivity]) {
        val leafActivity = activity.asInstanceOf[LeafActivity]
        val resource = activityManager.getResource(packageID, leafActivity.resourceIdentifier)
        val manifest = packageManager.getScormManifest(packageID)

        val resultedURL = if (resource.href.get.startsWith("http://") || resource.href.get.startsWith("https://")) {
          resource.href.get
        } else {
          val manifestRelativeResourceUrl = ResourceUrl(manifest.base, manifest.resourcesBase, resource.base, resource.href.get, leafActivity.resourceParameters)
          servletContext.getContextPath + "/" + contextRelativeResourceURL(packageID, manifestRelativeResourceUrl)
        }
        Map("activityURL" -> resultedURL,
          "activityTitle" -> leafActivity.title,
          "activityDesc" -> leafActivity.title,
          "hiddenUI" -> leafActivity.hiddenNavigationControls.map(_.toString))
      } else Map()
    } else Map()
  }

  //todo: is it deprecate
  private def contextRelativeResourceURL(packageID: Int, manifestRelativeResourceUrl: String): String =
    "SCORMData/data/" + packageID.toString + "/" + manifestRelativeResourceUrl
}
