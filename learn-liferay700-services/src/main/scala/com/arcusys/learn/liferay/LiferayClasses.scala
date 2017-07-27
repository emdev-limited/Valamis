package com.arcusys.learn.liferay

import com.liferay.asset.kernel.exception.NoSuchVocabularyException
import com.liferay.asset.kernel.model.{AssetEntry, BaseAssetRenderer, BaseAssetRendererFactory, _}
import com.liferay.blogs.kernel.model.BlogsEntry
import com.liferay.bookmarks.model.BookmarksEntry
import com.liferay.calendar.model.CalendarBooking
import com.liferay.document.library.kernel.model.DLFileEntry
import com.liferay.journal.model.JournalArticle
import com.liferay.mail.kernel.model.MailMessage
import com.liferay.portal.kernel.bean.BeanLocator
import com.liferay.portal.kernel.dao.orm.DynamicQuery
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.exception.{NoSuchGroupException, NoSuchLayoutException, NoSuchRoleException, _}
import com.liferay.portal.kernel.language.UTF8Control
import com.liferay.portal.kernel.messaging.{Message, MessageListener}
import com.liferay.portal.kernel.model.{User, _}
import com.liferay.portal.kernel.notifications.BaseUserNotificationHandler
import com.liferay.portal.kernel.portlet.{LiferayPortletRequest, LiferayPortletResponse, LiferayPortletSession}
import com.liferay.portal.kernel.search.{Hits, HitsOpenSearchImpl, SearchContext, Summary, _}
import com.liferay.portal.kernel.security.permission.PermissionChecker
import com.liferay.portal.kernel.service.{BaseLocalService, ServiceContext}
import com.liferay.portal.kernel.struts.{BaseStrutsAction, StrutsAction}
import com.liferay.portal.kernel.theme.ThemeDisplay
import com.liferay.portal.kernel.upgrade.UpgradeProcess
import com.liferay.portal.kernel.util.{UnicodeProperties, WebKeys}
import com.liferay.ratings.kernel.model.{RatingsEntry, RatingsStats}
import com.liferay.social.kernel.model.{SocialActivity, SocialActivityFeedEntry}
import com.liferay.message.boards.kernel.model.MBMessage
import com.liferay.portal.kernel.security.auth.PrincipalException
import com.liferay.wiki.model.WikiPage

object LiferayClasses {
  type LAssetEntry = AssetEntry
  type LBeanLocator = BeanLocator
  type LAssetRenderer = AssetRenderer[Any]
  type LBaseAssetRenderer = BaseAssetRenderer[Any]
  type LBaseAssetRendererFactory = BaseAssetRendererFactory[Any]
  type LBaseIndexer[T] = BaseIndexer[T]
  type LIndexer[T] = Indexer[T]
  type LBaseLocalService = BaseLocalService
  type LBaseModel[T] = BaseModel[T]
  type LBaseModelListener[T <: LBaseModel[T]] = BaseModelListener[T]
  type LBaseStrutsAction = BaseStrutsAction
  type LBooleanQuery = BooleanQuery
  type LDocument = Document
  type LDocumentImpl = DocumentImpl
  type LDynamicQuery = DynamicQuery
  type LGroup = com.arcusys.learn.liferay.model.LGroup
  type LAssetVocabulary = AssetVocabulary
  type LHits = Hits
  type LHitsOpenSearchImpl = HitsOpenSearchImpl
  type LLayout = Layout
  type LLayoutSetPrototype = LayoutSetPrototype
  type LLayoutTypePortlet = LayoutTypePortlet
  type LLiferayPortletRequest = LiferayPortletRequest
  type LLiferayPortletResponse = LiferayPortletResponse
  type LPermissionChecker = PermissionChecker
  type LSearchContext = SearchContext
  type LServiceContext = ServiceContext
  type LSocialActivity = SocialActivity
  type LSocialActivityFeedEntry = SocialActivityFeedEntry
  type LStrutsAction = StrutsAction
  type LSummary = Summary
  type LThemeDisplay = ThemeDisplay
  type LTheme = Theme
  type LUnicodeProperties = UnicodeProperties
  type LUpgradeProcess = UpgradeProcess
  type LUser = User
  type LSimpleAction = SimpleAction
  type LMessageListener = MessageListener
  type LMessage = Message
  type LBaseUserNotificationHandler = BaseUserNotificationHandler
  type LUserNotificationEvent = UserNotificationEvent
  type LJournalArticle = JournalArticle
  type LRatingsStats = RatingsStats
  type LRatingsEntry = RatingsEntry
  type LAssetCategory = AssetCategory
  type LOrganization = Organization

  //Liferay Activities
  type LBlogsEntry = BlogsEntry
  type LDLFileEntry = DLFileEntry
  type LWikiPage = WikiPage
  type LMBMessage = MBMessage
  type LCalendarBooking = CalendarBooking
  type LBookmarksEntry = BookmarksEntry

  type LAddress = Address
  type LCompany = Company
  type LMailMessage = MailMessage
  type LMembershipRequest = MembershipRequest
  type LRole = Role

  // Exceptions
  type LNoSuchRoleException = NoSuchRoleException
  type LNoSuchGroupException = NoSuchGroupException
  type LNoSuchLayoutException = NoSuchLayoutException
  type LNoSuchUserException = NoSuchUserException
  type LNoSuchResourceActionException = NoSuchResourceActionException
  type LNoSuchVocabularyException = NoSuchVocabularyException
  type LNoSuchCompanyException = NoSuchCompanyException
  type LGroupFriendlyURLException = GroupFriendlyURLException
  type LDuplicateGroupException = DuplicateGroupException
  type LLayoutFriendlyURLException = LayoutFriendlyURLException
  type LNoSuchResourcePermissionException = NoSuchResourcePermissionException
  type LMustBeAuthenticatedException = PrincipalException.MustBeAuthenticated

  type LUTF8Control = UTF8Control
  object LUTF8Control {
    val instance: LUTF8Control = UTF8Control.INSTANCE
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
