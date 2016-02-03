package com.arcusys.valamis.hook

import java.util.Locale

import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.language.{LanguageUtil, Language}
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.util.{FileUtil, LocaleUtil}
import com.liferay.portal.model.Group
import com.liferay.portal.service.{ClassNameLocalServiceUtil, ServiceContext, GroupLocalServiceUtil, UserLocalServiceUtil}
import com.liferay.portlet.dynamicdatamapping.model.{DDMTemplate, DDMTemplateConstants, DDMStructure, DDMStructureConstants}
import com.liferay.portlet.dynamicdatamapping.service.{DDMTemplateLocalServiceUtil, DDMStructureLocalServiceUtil}
import com.liferay.portlet.journal.model.JournalArticle
import scala.collection.JavaConverters._
import com.liferay.portal.kernel.template.TemplateConstants._


class UpgradeTemplates extends SimpleAction {
  private val _log: Log = LogFactoryUtil.getLog(classOf[UpgradeTemplates])

  override def run(companyIds: Array[String]): Unit = {
    _log.info("Upgrade valamis web content template")

    companyIds.foreach(companyId => {
      val groupId = GroupLocalServiceUtil.getCompanyGroup(companyId.toLong).getGroupId
      val userId = UserLocalServiceUtil.getDefaultUserId(companyId.toLong)

      upgrade(groupId, userId)
    })
  }

  private def upgrade(groupId: Long, userId: Long) {
    val defaultLocale = LocaleUtil.getDefault

    val structureId = addStructure(
      groupId,
      userId,
      "ValamisWebContent",
      Map(defaultLocale -> LanguageUtil.get(defaultLocale, "valamisWebContent")),
      Map(defaultLocale -> LanguageUtil.get(defaultLocale, "valamisWebContent")),
      getFileAsString("structures/ValamisWebContent.xml")
    )

    addTemplate(
      groupId,
      userId,
      structureId,
      "ValamisWebContent",
      Map(defaultLocale -> LanguageUtil.get(defaultLocale, "valamisWebContent")),
      Map(defaultLocale -> LanguageUtil.get(defaultLocale, "valamisWebContent")),
      getFileAsString("templates/ValamisWebContent.ftl"),
      LANG_TYPE_FTL
    )
  }
  
  private def getFileAsString(path: String): String = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val is = classLoader.getResourceAsStream(path)
    new String(FileUtil.getBytes(is))
  }

  private def addStructure(groupId: Long,
                           userId: Long,
                           structureKey: String,
                           nameMap: Map[Locale, String],
                           descriptionMap: Map[Locale, String],
                           xsd: String): Long = {

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val structureClassNameId = ClassNameLocalServiceUtil.getClassNameId(classOf[JournalArticle])

    val structure = DDMStructureLocalServiceUtil.fetchStructure(groupId, structureClassNameId, structureKey) match {
      case structure: DDMStructure =>
        _log.info("Existing structure found with id: " + structure.getStructureId)

        structure.setXsd(xsd)

        DDMStructureLocalServiceUtil.updateStructure(
          structure.getStructureId,
          DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID,
          nameMap.asJava,
          descriptionMap.asJava,
          xsd,
          serviceContext)

        _log.info("Structure " + structure.getStructureId + " updated successfully.")

        structure

      case _ =>
        _log.info("Could not find an existing structure. Adding a new structure with id: " + structureKey)

        DDMStructureLocalServiceUtil.addStructure(
          userId,
          groupId,
          DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID,
          structureClassNameId,
          structureKey,
          nameMap.asJava,
          descriptionMap.asJava,
          xsd,
          "xml",
          DDMStructureConstants.TYPE_DEFAULT,
          serviceContext)
    }

    structure.getStructureId
  }

  private def addTemplate(groupId: Long,
                          userId: Long,
                          structureId: Long,
                          templateKey: String,
                          nameMap: Map[Locale, String],
                          descriptionMap: Map[Locale, String],
                          body: String,
                          langType: String) {

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val templateClassNameId = ClassNameLocalServiceUtil.getClassNameId(classOf[DDMStructure])

    DDMTemplateLocalServiceUtil.fetchTemplate(groupId, templateClassNameId, templateKey, true) match {
      case template: DDMTemplate =>
        _log.info("Existing template found with id: " + template.getTemplateId)

        DDMTemplateLocalServiceUtil.updateTemplate(
          template.getTemplateId,
          structureId,
          nameMap.asJava,
          descriptionMap.asJava,
          DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY,
          null,
          langType,
          body,
          false,
          false,
          null,
          null,
          serviceContext)

        _log.info("Template " + template.getTemplateId + " updated successfully.")

      case _ =>
        _log.info("Could not find an existing template. Adding a new template with id: " + templateKey + " for structure with id: " + structureId)

        DDMTemplateLocalServiceUtil.addTemplate(
          userId,
          groupId,
          templateClassNameId,
          structureId,
          templateKey,
          nameMap.asJava,
          descriptionMap.asJava,
          DDMTemplateConstants.TEMPLATE_TYPE_DISPLAY,
          null,
          langType,
          body,
          false,
          false,
          null,
          null,
          serviceContext)
    }
  }
}