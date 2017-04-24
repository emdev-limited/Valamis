package com.arcusys.valamis.hook.utils

import java.util.Locale

import com.liferay.portal.kernel.language.LanguageUtil
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.kernel.template.TemplateConstants._
import com.liferay.portal.kernel.util.{FileUtil, LocaleUtil}
import com.liferay.portal.kernel.xml.SAXReaderUtil
import com.liferay.portal.model._
import com.liferay.portal.service._
import com.liferay.portal.service.permission.PortletPermissionUtil
import com.liferay.portlet.dynamicdatamapping.model.{DDMStructure, DDMStructureConstants, DDMTemplate, DDMTemplateConstants}
import com.liferay.portlet.dynamicdatamapping.service.{DDMStructureLocalServiceUtil, DDMTemplateLocalServiceUtil}
import com.liferay.portlet.journal.model.JournalArticle

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

/**
  * Created by Igor Borisov on 12.05.16.
  */
object Utils {
  private val log = LogFactoryUtil.getLog(this.getClass)

  def addStructureWithTemplate(
                                groupId: Long,
                                userId: Long,
                                structureInfo: StructureInfo,
                                templateInfo: TemplateInfo): Unit = {
    addStructureWithTemplates(groupId, userId, structureInfo, Array(templateInfo))
  }

  def addStructureWithTemplates(groupId: Long,
                                userId: Long,
                                structureInfo: StructureInfo,
                                templates: Array[TemplateInfo]): Unit = {

    val xsd = getFileAsString(s"structures/${structureInfo.key}.xml")

    // locale from xsd should be in the name/description map
    val locales = Seq(LocaleUtil.getDefault, getDefaultLocale(xsd)).distinct

    val structureId = addStructure(
      groupId,
      userId,
      structureInfo.key,
      locales.map(l => (l, LanguageUtil.get(l, structureInfo.name))).toMap,
      locales.map(l => (l, LanguageUtil.get(l, structureInfo.description))).toMap,
      xsd
    )

    templates.foreach { template =>
      addStructureTemplate(
        groupId,
        userId,
        structureId,
        template.key,
        locales.map(l => (l, LanguageUtil.get(l, template.name))).toMap,
        locales.map(l => (l, LanguageUtil.get(l, template.description))).toMap,
        getFileAsString(s"templates/${template.key}.ftl"),
        LANG_TYPE_FTL
      )
    }
  }

  private def addStructureTemplate(
                                    groupId: Long,
                                    userId: Long,
                                    structureId: Long,
                                    templateKey: String,
                                    nameMap: Map[Locale, String],
                                    descriptionMap: Map[Locale, String],
                                    body: String,
                                    langType: String) {

    val templateClassNameId = ClassNameLocalServiceUtil.getClassNameId(classOf[DDMStructure])

    addTemplate(
      groupId,
      userId,
      Some(structureId),
      templateKey,
      nameMap,
      descriptionMap,
      body,
      langType,
      templateClassNameId)
  }

  private def getDefaultLocale(xsd: String) = {
    val document = SAXReaderUtil.read(xsd)

    val rootElement = document.getRootElement

    LocaleUtil.fromLanguageId(rootElement.attributeValue("default-locale"))
  }

  def addTemplate(
                   groupId: Long,
                   userId: Long,
                   classPK: Option[Long],
                   templateKey: String,
                   nameMap: Map[Locale, String],
                   descriptionMap: Map[Locale, String],
                   body: String,
                   langType: String,
                   templateClassNameId: Long
                 ) = {

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    DDMTemplateLocalServiceUtil.fetchTemplate(groupId, templateClassNameId, templateKey, true) match {
      case template: DDMTemplate =>
        log.info("Existing template found with id: " + template.getTemplateId)

        DDMTemplateLocalServiceUtil.updateTemplate(
          template.getTemplateId,
          classPK getOrElse 0,
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

        log.info("Template " + template.getTemplateId + " updated successfully.")

      case _ =>
        log.info("Could not find an existing template. Adding a new template with id: " + templateKey + " for structure with id: " + classPK)

        DDMTemplateLocalServiceUtil.addTemplate(
          userId,
          groupId,
          templateClassNameId,
          classPK getOrElse 0,
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

  def addStructure(groupId: Long,
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
        log.info("Existing structure found with id: " + structure.getStructureId)

        structure.setXsd(xsd)

        DDMStructureLocalServiceUtil.updateStructure(
          structure.getStructureId,
          DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID,
          nameMap.asJava,
          descriptionMap.asJava,
          xsd,
          serviceContext)

        log.info("Structure " + structure.getStructureId + " updated successfully.")

        structure

      case _ =>
        log.info("Could not find an existing structure. Adding a new structure with id: " + structureKey)

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

  def getFileAsString(path: String): String = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val is = classLoader.getResourceAsStream(path)
    new String(FileUtil.getBytes(is))
  }

  def getLayout(siteGroupId: Long, isPrivate: Boolean, friendlyUrl: String): Option[Layout] = {
    Option(LayoutLocalServiceUtil.fetchLayoutByFriendlyURL(siteGroupId, isPrivate, friendlyUrl))
  }

  def removeLayout(siteGroupId: Long, isPrivate: Boolean, friendlyUrl: String) {
    val layout = getLayout(siteGroupId, isPrivate, friendlyUrl)
    layout match {
      case Some(l) => LayoutLocalServiceUtil.deleteLayout(l)
      case None => log.debug("Cannot remove layout - layout does not exist")
                   log.debug(s"groupId: $siteGroupId private: $isPrivate friendlyUrl: $friendlyUrl")
    }

  }

  def addLayout(siteGroupId: Long, userId: Long, layoutName: String, isPrivate: Boolean, friendlyURL: String, parentId: Long): Layout = {

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val title = ""
    val description = ""
    val layoutType = LayoutConstants.TYPE_PORTLET
    val isHidden = false

    LayoutLocalServiceUtil.addLayout(
      userId,
      siteGroupId,
      isPrivate,
      parentId,
      layoutName,
      title,
      description,
      layoutType,
      isHidden,
      friendlyURL,
      serviceContext
    )
  }

  def setupPages(siteId: Long, userId: Long, isSitePrivate: Boolean, layouts: Seq[PageLayout]) {
    log.info("Create site pages")

    layouts foreach { layout =>
      addLayout(siteId, userId, isSitePrivate, layout)
    }
  }

  def addLayout(
                         siteId: Long,
                         userId: Long,
                         isSitePrivate: Boolean,
                         pageLayout: PageLayout,
                         parentId: Long = LayoutConstants.DEFAULT_PARENT_LAYOUT_ID): Unit = {

    try {

      log.info(s"Update layout ${pageLayout.name}")
      val serviceContext = new ServiceContext
      serviceContext.setAddGuestPermissions(true)

      val layout = getLayout(siteId, pageLayout.isPrivate, pageLayout.friendlyUrl) match {
        case Some(l) =>
          log.info(s"Found existed layout ${pageLayout.name}. Existed layout will be updated")
          Option(l)
        case None =>
          log.info(s"Layout ${pageLayout.name} does not exist. New layout will be created")
          Option(addLayout(siteId, userId, pageLayout.name, isSitePrivate, pageLayout.friendlyUrl, parentId))
      }

      layout foreach { l =>
        val layoutType = l.getLayoutType.asInstanceOf[LayoutTypePortlet]
        layoutType.setLayoutTemplateId(userId, pageLayout.templateId)

        Utils.updateLayout(l)

        pageLayout.children
          .foreach(childLayout => addLayout(siteId, userId, isSitePrivate, childLayout, l.getLayoutId))
      }

    } catch {
      case NonFatal(e) => log.warn(s"Failed to add layout ${pageLayout.name} to group $siteId")
    }
  }

  def addSite(userId: Long, name: String, friendlyURL: String): Group = {
    val groupType = GroupConstants.TYPE_SITE_OPEN
    val parentGroupId = GroupConstants.DEFAULT_PARENT_GROUP_ID
    val liveGroupId = GroupConstants.DEFAULT_LIVE_GROUP_ID
    val membershipRestriction = GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION
    val description = ""
    val manualMembership = true
    val isSite = true
    val isActive = true

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    GroupLocalServiceUtil.addGroup(
      userId,
      parentGroupId,
      classOf[Group].getName,
      0, //classPK
      liveGroupId,
      name,
      description,
      groupType,
      manualMembership,
      membershipRestriction,
      friendlyURL,
      isSite,
      isActive,
      serviceContext)
  }

  def setupTheme(siteGroupId: Long, themeId: String): LayoutSet = {

    LayoutSetLocalServiceUtil
      .updateLookAndFeel(siteGroupId, false, themeId, "", "", false)

    LayoutSetLocalServiceUtil
      .updateLookAndFeel(siteGroupId, true, themeId, "", "", false)
  }

  //portlets: Map[String, String] -> ( portletId, columnId )
  def addPortletsToLayout(layout: Layout, portlets: Map[String, String]):Unit = {
    portlets.foreach {
      case (portletId, columnId) => Utils.addPortletById(layout, portletId, columnId)
    }
  }

  def addPortletById(layout: Layout, portletId: String, columnId: String): Unit = {
    log.info(s"Add portlet $portletId to ${layout.getNameCurrentValue} at $columnId")
    val layoutTypePortlet = layout.getLayoutType.asInstanceOf[LayoutTypePortlet]

    if (!layoutTypePortlet.hasPortletId(portletId)) {
      val newPortletId = layoutTypePortlet.addPortletId(0, portletId, columnId, -1, false)

      addResources(layout, newPortletId)
      updateLayout(layout)
    }
  }

  def updateLayout(layout: Layout) {
    LayoutLocalServiceUtil.updateLayout(layout.getGroupId, layout.isPrivateLayout, layout.getLayoutId, layout.getTypeSettings)
  }

  def addResources(layout: Layout, portletId: String) {
    val rootPortletId = PortletConstants.getRootPortletId(portletId)
    val portletPrimaryKey = PortletPermissionUtil.getPrimaryKey(layout.getPlid, portletId)

    ResourceLocalServiceUtil.addResources(
      layout.getCompanyId,
      layout.getGroupId,
      0, //userId
      rootPortletId,
      portletPrimaryKey,
      true,
      true,
      true)
  }

  def hasPage(groupId: Long, isPrivate: Boolean, friendlyUrl: String): Boolean = {
    getLayout(groupId, isPrivate, friendlyUrl).isDefined
  }

  def updatePortletsForLayout(siteId: Long, pageLayout: PageLayout, portlets: Map[String, String]):Unit = {
    val layout = Utils.getLayout(siteId, pageLayout.isPrivate, pageLayout.friendlyUrl)
    layout match {
      case Some(l) =>
        Utils.addPortletsToLayout(l, portlets)
      case None =>
        log.info(s"Cannot add portlets to layout - layout ${pageLayout.name} does not exist")
    }
  }
}
