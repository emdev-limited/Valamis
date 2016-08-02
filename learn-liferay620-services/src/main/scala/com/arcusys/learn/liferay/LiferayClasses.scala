package com.arcusys.learn.liferay

import java.util.ResourceBundle

import com.liferay.portal.kernel.bean.BeanLocator
import com.liferay.portal.kernel.dao.orm.DynamicQuery
import com.liferay.portal.kernel.portlet.{LiferayPortletRequest, LiferayPortletResponse, LiferayPortletSession}
import com.liferay.portal.kernel.search._
import com.liferay.portal.kernel.struts.{BaseStrutsAction, StrutsAction}
import com.liferay.portal.kernel.upgrade.UpgradeProcess
import com.liferay.portal.kernel.util.{UnicodeProperties, WebKeys}
import com.liferay.portal.model._
import com.liferay.portal.security.permission.PermissionChecker
import com.liferay.portal.service.{BaseLocalService, ServiceContext}
import com.liferay.portal.theme.ThemeDisplay
import com.liferay.portal._
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.mail.MailMessage
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler
import com.liferay.portal.util.PortletKeys
import com.liferay.portlet.asset.model._
import com.liferay.portlet.asset.{NoSuchEntryException, NoSuchVocabularyException}
import com.liferay.portlet.blogs.model.BlogsEntry
import com.liferay.portlet.bookmarks.model.BookmarksEntry
import com.liferay.portlet.documentlibrary.model.DLFileEntry
import com.liferay.portlet.journal.model.JournalArticle
import com.liferay.portlet.ratings.model.RatingsEntry
import com.liferay.portlet.social.model.{SocialActivity, SocialActivityFeedEntry}
import com.liferay.portlet.trash.DuplicateEntryException

object LiferayClasses {
  type LAssetEntry = AssetEntry
  type LBeanLocator = BeanLocator
  type LBaseAssetRenderer = BaseAssetRenderer
  type LBaseAssetRendererFactory = BaseAssetRendererFactory
  type LBaseIndexer = BaseIndexer
  type LIndexer[T] = Indexer
  type LBaseLocalService = BaseLocalService
  type LBaseModel[T] = BaseModel[T]
  type LBaseModelListener[T <: com.liferay.portal.model.BaseModel[T]] = BaseModelListener[T]
  type LBaseStrutsAction = BaseStrutsAction
  type LBooleanQuery = BooleanQuery
  type LDocument = Document
  type LDocumentImpl = DocumentImpl
  type LDynamicQuery = DynamicQuery
  type LGroup = Group
  type LHits = Hits
  type LHitsOpenSearchImpl = HitsOpenSearchImpl
  type LJournalArticle = JournalArticle
  type LLayout = Layout
  type LLayoutTypePortlet = LayoutTypePortlet
  type LLiferayPortletRequest = LiferayPortletRequest
  type LLiferayPortletResponse = LiferayPortletResponse
  type LPermissionChecker = PermissionChecker
  type LSearchContext = SearchContext
  type LServiceContext = ServiceContext
  type LSocialActivity = SocialActivity
  type LSocialActivityFeedEntry = SocialActivityFeedEntry
  type LStrutsAction = StrutsAction
  type LSimpleAction = SimpleAction
  type LSummary = Summary
  type LThemeDisplay = ThemeDisplay
  type LUnicodeProperties = UnicodeProperties
  type LUpgradeProcess = UpgradeProcess
  type LUser = User
  type LDLFileEntry = DLFileEntry
  type LRatingsEntry = RatingsEntry
  type LAssetCategory = AssetCategory
  type LOrganization = Organization
  type LBookmarksEntry = BookmarksEntry
  type LBlogsEntry = BlogsEntry
  type LAddress = Address
  type LCompany = Company
  type LMailMessage = MailMessage
  type LPortletKeys = PortletKeys
  type LAssetRenderer = AssetRenderer
  type LBaseUserNotificationHandler = BaseUserNotificationHandler
  type LUserNotificationEvent = UserNotificationEvent
  type LMessage = Message
  type LMessageListener = MessageListener
  type LRole = Role


  // Exceptions
  type LNoSuchRoleException = NoSuchRoleException
  type LNoSuchGroupException = NoSuchGroupException
  type LNoSuchEntryException = NoSuchEntryException
  type LNoSuchLayoutException = NoSuchLayoutException
  type LDuplicateEntryException = DuplicateEntryException
  type LNoSuchVocabularyException = NoSuchVocabularyException
  type LNoSuchUserException = NoSuchUserException
  type LNoSuchResourceActionException = NoSuchResourceActionException
  type LNoSuchCompanyException = NoSuchCompanyException
  type LLayoutFriendlyURLException = LayoutFriendlyURLException
  type LDuplicateGroupException = DuplicateGroupException
  type LGroupNameException = GroupNameException
  type LGroupFriendlyURLException = GroupFriendlyURLException
  type LNoSuchResourcePermissionException = NoSuchResourcePermissionException

  /// mock impl, UTF8Control not implemented in LR620
  type LUTF8Control = ResourceBundle.Control
  object LUTF8Control {
    val instance: ResourceBundle.Control = ???
  }

  object LWebKeys {
    val ThemeDisplay = WebKeys.THEME_DISPLAY
  }

  object LLiferayPortletSession {
    val LayoutSeparator = LiferayPortletSession.LAYOUT_SEPARATOR
  }

  object LLayoutFriendlyURLExceptionHelper {
    val ADJACENT_SLASHES = LayoutFriendlyURLException.ADJACENT_SLASHES
    val DOES_NOT_START_WITH_SLASH = LayoutFriendlyURLException.DOES_NOT_START_WITH_SLASH
    val DUPLICATE = LayoutFriendlyURLException.DUPLICATE
    val ENDS_WITH_SLASH = LayoutFriendlyURLException.ENDS_WITH_SLASH
    val INVALID_CHARACTERS = LayoutFriendlyURLException.INVALID_CHARACTERS
    val KEYWORD_CONFLICT = LayoutFriendlyURLException.KEYWORD_CONFLICT
    val POSSIBLE_DUPLICATE = LayoutFriendlyURLException.POSSIBLE_DUPLICATE
    val TOO_DEEP = LayoutFriendlyURLException.TOO_DEEP
    val TOO_LONG = LayoutFriendlyURLException.TOO_LONG
    val TOO_SHORT = LayoutFriendlyURLException.TOO_SHORT
  }
}
