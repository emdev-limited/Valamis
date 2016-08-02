package com.arcusys.valamis.web.interpreter

import java.util.Date

import com.arcusys.learn.liferay.LBaseSocialActivityInterpreter
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.lesson.model.{Lesson, PackageActivityType}
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable
import org.ocpsoft.prettytime.PrettyTime

object PackageActivityInterpreter {
  val ScormPackageClassName = "com.arcusys.valamis.lesson.scorm.model.ScormPackage"
  val TincanPackageClassName = "com.arcusys.valamis.lesson.tincan.model.TincanPackage"
  val LessonClassName = "com.arcusys.valamis.lesson.model.Lesson"
  val ClassNames = Array[String](LessonClassName, ScormPackageClassName, TincanPackageClassName)
}

class PackageActivityInterpreterLF extends PackageActivityInterpreter

class PackageActivityInterpreterSO extends PackageActivityInterpreter {
  override def getSelector(): String = "SO"
}

abstract class PackageActivityInterpreter extends LBaseSocialActivityInterpreter with Injectable {
  import PackageActivityInterpreter._
  implicit lazy val bindingModule = Configuration

  lazy val lessonService = inject[LessonService]

  def getVerb(value: Int) = PackageActivityType.apply(value) match {
    case PackageActivityType.Published => "published"
    case PackageActivityType.Shared => "shared"
    case PackageActivityType.Completed => "completed"
  }

  override protected def doInterpret(activity: LSocialActivity , context: Context): LSocialActivityFeedEntry = {
    val title = ""
    val body = renderFeedEntryBodyHelper(
      lessonService.getLesson(activity.getClassPK).get,
      activity
    )
    new LSocialActivityFeedEntry(title, body)
  }

  val prettyTime = new PrettyTime()
  private def renderFeedEntryBodyHelper(model: Lesson, activity: LSocialActivity) = {
    val userName = UserLocalServiceHelper().getUser(activity.getUserId).getFullName
    val verb = getVerb(activity.getType)

    val logoSrc = if(model.logo.isDefined && model.logo.get != "") s"""/delegate/files/images?folderId=package_logo_${model.id}&file=${model.logo.get}"""
    else """/learn-portlet/img/imgo.jpg"""

    val displayLogo = """<img style="width: 180px; height: 120px;" src="""" + logoSrc + """"/>"""

    s"""
       |<div><b>${userName}</b> $verb a lesson</div>
       |<div style="font-size: small; color: gray"> ${prettyTime.format(new Date(activity.getCreateDate))} </div>
       |<div style="margin: 10px 5px;"> ${activity.getExtraData} </div>
       |<div style="margin-bottom: 10px;"> ${displayLogo} <b> ${model.title} </b> </div>
    """.stripMargin
  }

  def getClassNames: Array[String] = ClassNames
}