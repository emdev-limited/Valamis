package com.arcusys.valamis.web.servlet.scorm

import java.net.URLDecoder

import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.lesson.scorm.model.manifest.{LeafActivity, ResourceUrl}
import com.arcusys.valamis.lesson.scorm.model.sequencing.{ProcessorResponseDelivery, ProcessorResponseEndSession}
import com.arcusys.valamis.lesson.scorm.service.sequencing.SequencingProcessor
import com.arcusys.valamis.lesson.scorm.service.{ActivityServiceContract, ScormPackageService}
import com.arcusys.valamis.lesson.service.{LessonLimitService, LessonPlayerService, LessonService}
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.slide.model.SlideSetStatus
import com.arcusys.valamis.slide.storage.SlideSetRepository
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.request.Parameter
import org.apache.http.HttpStatus
import org.scalatra.{ScalatraBase, SinatraRouteMatcher}

class SequencingServlet extends BaseApiController {

  lazy val activityManager = inject[ActivityServiceContract]
  lazy val tincanLessonService = inject[TincanPackageService]
  lazy val scormLessonService = inject[ScormPackageService]
  lazy val lessonService = inject[LessonService]
  lazy val lessonLimitService = inject[LessonLimitService]
  lazy val slideSetRepository = inject[SlideSetRepository]
  lazy val lessonPlayerService = inject[LessonPlayerService]

  implicit val scalatra: ScalatraBase = this

  implicit override def string2RouteMatcher(path: String) = new SinatraRouteMatcher(path)

  // get possible navigation types, check which navigation controls should be hidden
  get("/sequencing/NavigationRules/:packageID/:currentScormActivityID") {
    val packageID = Parameter("packageID").intRequired
    val activityID = Parameter("currentScormActivityID").required
    val activity = activityManager.getActivity(packageID, activityID)
    JsonHelper.toJson("hiddenUI" -> activity.hiddenNavigationControls.map(_.toString))
  }

  post("/sequencing/Tincan/:lessonId") {
    val lessonId = Parameter("lessonId").intRequired

    val lesson = lessonPlayerService.getLessonIfAvailable(lessonId, PermissionUtil.getLiferayUser)
      .getOrElse {
        halt(HttpStatus.SC_FORBIDDEN, reason = "Lesson is not available", body = "unavailablePackageException")
      }

    assert(lesson.lessonType == LessonType.Tincan)

    val mainFileName = tincanLessonService.getTincanLaunch(lessonId)

    val activityId = lessonService.getRootActivityId(lesson)

    val slideSetList = slideSetRepository.getByActivityId(activityId)
      .filter(_.status == SlideSetStatus.Published)

    val versionNumber =
      if (slideSetList.isEmpty) 0
      else slideSetList.map(_.version).max

    JsonHelper.toJson(Map("launchURL" -> mainFileName, "versionNumber" -> versionNumber, "activityId" -> activityId))
  }

  get("/sequencing/NavigationRequest/:currentScormPackageID/:currentOrganizationID/:sequencingRequest") {

    val userID = getUserId.toInt
    val packageID = Parameter("currentScormPackageID").intRequired
    val organizationID = Parameter("currentOrganizationID").required

    val lesson = lessonPlayerService.getLessonIfAvailable(packageID, PermissionUtil.getLiferayUser)

    if (lesson.isEmpty) {
      "The lesson you are trying to open seems to be unavailable."
    } else {
      assert(lesson.map(_.lessonType).contains(LessonType.Scorm))

      val currentAttempt = activityManager.getActiveAttempt(userID, packageID, organizationID)
      val tree = activityManager.getActivityStateTreeForAttemptOrCreate(currentAttempt)

      val processor = new SequencingProcessor(currentAttempt, tree)

      val sequencingRequest = URLDecoder.decode(Parameter("sequencingRequest").required, "UTF-8")

      val jsonData = JsonHelper.toJson(processor.process(sequencingRequest) match {
        case ProcessorResponseDelivery(tree) => {
          activityManager.updateActivityStateTree(currentAttempt.id.toInt, tree)
          val currentActivityID = tree.currentActivity.map(_.item.activity.id).getOrElse("")
          Map("currentActivity" -> currentActivityID, "endSession" -> false) ++ getActivityData(packageID, currentActivityID)
        }
        case ProcessorResponseEndSession(tree) => {
          activityManager.updateActivityStateTree(currentAttempt.id.toInt, tree)
          activityManager.markAsComplete(currentAttempt.id.toInt)
          val currentActivityID = tree.currentActivity.map(_.item.activity.id).getOrElse("")
          Map("currentActivity" -> currentActivityID, "endSession" -> true) ++ getActivityData(packageID, currentActivityID)
        }
      })

      contentType = "text/html"
      val headScriptData = scala.xml.Unparsed(
        """
        function findPlayerView(win) {
          var findPlayerTries = 0;
          while ( !win.lessonViewer && (win.parent != null) && (win.parent != win)) {
            findPlayerTries++;
            if (findPlayerTries > 20) return null;
            win = win.parent;
          }
          return win.lessonViewer.playerLayoutView;
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

  //TODO: obsolete ?
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
        val manifest = scormLessonService.getManifest(packageID).get

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
