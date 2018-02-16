package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.util.{LanguageHelper => translations}

case class LiferayActivity(activityId: String, title: String)

object LiferayActivity {

  def socialActivities: Seq[LiferayActivity] = Seq (
    LiferayActivity(classOf[LBlogsEntry].getCanonicalName, translations.get("blogs")),
    LiferayActivity(classOf[LDLFileEntry].getCanonicalName, translations.get("file-upload")),
    LiferayActivity(classOf[LWikiPage].getCanonicalName, translations.get("wiki")),
    LiferayActivity(classOf[LMBMessage].getCanonicalName, translations.get("message-board-messages")),
    LiferayActivity(classOf[LCalendarBooking].getCanonicalName, translations.get("calendar-event")),
    LiferayActivity(classOf[LBookmarksEntry].getCanonicalName, translations.get("social-bookmarks"))
  )

  def activities: Seq[LiferayActivity] = socialActivities ++ Seq(
    LiferayActivity("participation", translations.get("participation-value")),
    LiferayActivity("contribution", translations.get("group.statistics.title.contribution"))
  )

}
