package com.arcusys.valamis.web.service

import java.net.URLEncoder
import javax.portlet.PortletURL
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.service.LessonService


class OpenPackageAction extends BaseOpenAction {
  override val portletId = PortletName.LessonViewer.key

  lazy val lessonService = inject[LessonService]

  override def getById(id: Long): Option[LAssetEntry] = {
    lessonService.getLesson(id) map { lesson =>
      val className = classOf[Lesson].getName
      val classPK = lesson.id

      AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)
    }
  }

  override def sendResponse(response: HttpServletResponse,
                            portletURL: PortletURL,
                            assetEntry: Option[LAssetEntry]): Unit = {
    val hash = assetEntry map { i =>
      s"""/lesson/${i.getClassPK}/${getType(i.getClassPK)}/${URLEncoder.encode(i.getTitle.replace(" ", "%20"), "UTF-8")}/false"""
    } getOrElse ""
    response.sendRedirect(portletURL.toString + "#" + hash)
  }

  private def getType(lessonId: Long): String = {
    val lessonType = lessonService.getLesson(lessonId).map(_.lessonType).getOrElse(LessonType.Tincan)

    lessonType.toString
  }
}
