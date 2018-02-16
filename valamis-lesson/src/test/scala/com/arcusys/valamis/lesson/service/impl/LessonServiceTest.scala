package com.arcusys.valamis.lesson.service.impl

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.model.{Lesson, LessonFilter, LessonType}
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.service.{CustomLessonService, LessonAssetHelper}
import com.arcusys.valamis.lesson.storage.{LessonAttemptsTableComponent, LessonTableComponent}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import org.scalatest.{BeforeAndAfter, FunSuite}
//TODO: add hsql test dependency
//class LessonServiceHSQLTest extends LessonServiceTestBase(
//  HsqldbDriver,
//  HsqldbDriver.simple.Database.forURL("jdbc:hsqldb:mem:LessonServiceTest;shutdown=true", driver = "org.hsqldb.jdbcDriver")
//)

class LessonServiceTest
  extends FunSuite
    with BeforeAndAfter
    with LessonTableComponent
    with LessonAttemptsTableComponent
    with SlickProfile
    with SlickDbTestBase {

  import driver.simple._

  before {
    createDB()
  }
  after {
    dropDB()
  }

  val courseId = 345
  val userId = 546
  val tagId = 565

  test("lessonService_getAll") {
    val lessonService = new LessonServiceImpl(db, driver) {
      lazy val tagService: TagService[Lesson] = new TagService[Lesson] {
        override def getItemIds(tagId: Long): Seq[Long] = Seq(1)
        override def getByItemId(itemId: Long): Seq[ValamisTag] = Nil
      }
      lazy val userService: UserLocalServiceHelper = new UserLocalServiceHelper{
        override def getUsers(userIds: Seq[Long]) = Nil
      }
      lazy val ratingService: RatingService[Lesson] = ???
      lazy val assetHelper: LessonAssetHelper = ???
      lazy val socialActivityHelper: SocialActivityHelper[Lesson] = ???
      lazy val fileService: FileService = ???
      lazy val customLessonServices: Map[LessonType, CustomLessonService] = ???
      lazy val fileStorage: FileStorage = ???
      lazy val lessonNotificationService = ???
    }

    db.withTransaction { implicit s =>
      (lessons.ddl ++ playerLessons.ddl ++ lessonLimits.ddl).create
    }

    lessonService.create(LessonType.Tincan, courseId, "t1", "d1", userId)

    val items = lessonService.getLessonsWithData(new LessonFilter(Seq(courseId), None, tagId = Some(tagId)))

    assert(items.total == 1)
  }
}
