package com.arcusys.learn.liferay.service

import java.net.URLEncoder
import javax.portlet.PortletURL
import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.service.utils.PortletKeys
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lesson.tincan.model.TincanManifest


class OpenPackageAction extends BaseOpenAction {
  override val portletId = PortletKeys.ValamisPackage

  private lazy val packageService = inject[ValamisPackageService]

  override def getById(id: Long) = {
    packageService.getById(id) map { pkg =>
      val className = pkg.packageType match {
        case LessonType.Tincan => classOf[TincanManifest].getName
        case LessonType.Scorm => classOf[Manifest].getName
      }

      AssetEntryLocalServiceHelper.getAssetEntry(className, pkg.id)
    }
  }

  override def sendResponse(response: HttpServletResponse, portletURL: PortletURL, assetEntry: Option[LAssetEntry]) = {
    val hash = assetEntry map { i =>
      s"""/lesson/${i.getClassPK}/${getType(i.getClassName)}/${i.getTitle.replace(" ", "%20")}/false"""
    } getOrElse ""
    response.sendRedirect(portletURL.toString + "&hash=" + URLEncoder.encode(hash, "UTF-8"))
  }

  private def getType(className: String): String = {
    val Tincan = classOf[TincanManifest].getName
    className match {
      case Tincan => "tincan"
      case _ => "scorm"
    }
  }
}
