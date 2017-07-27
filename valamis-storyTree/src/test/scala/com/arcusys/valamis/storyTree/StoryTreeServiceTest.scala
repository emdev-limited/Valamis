package com.arcusys.valamis.storyTree

import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.service.LessonCategoryGoalService
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.storyTree.model.{StoryPackageItem, StoryNode, Story}
import com.arcusys.valamis.storyTree.service.impl.{StoryTreeNodeServiceImpl, StoryTreeServiceImpl}
import com.arcusys.valamis.storyTree.storage.StoryTreeTableComponent
import org.scalatest.{BeforeAndAfter, FunSuite}
import scala.concurrent.ExecutionContext.Implicits.global

class StoryTreeServiceTest extends FunSuite
  with StoryTreeTableComponent
  with SlickProfile
  with BeforeAndAfter
  with SlickDbTestBase {

  val service = new StoryTreeServiceImpl(db, driver) {
    override def lessonService: LessonService = ???
    override def fileService: FileService = ???
    override def lessonCategoryGoalService: LessonCategoryGoalService = ???
  }
  val nodeService = new StoryTreeNodeServiceImpl(db, driver) {
    override def lessonService: LessonService = ???
    override def lessonCategoryGoalService: LessonCategoryGoalService = ???
  }

  before {
    createDB()
  }
  after {
    dropDB()
  }

  def createSchema() {
      import driver.api._
      await(db.run((trees.schema ++ nodes.schema ++ packages.schema).create))
  }

  test("insert and get") {
    createSchema()

    val sourceTree = new Story(None, 2, "titleD", "descriptionD", Some("1.png"), true)

    service.create(1, "titleA", "descriptionA", logo = None, isDefault = false)

    val treeId = service.create(
      sourceTree.courseId,
      sourceTree.title,
      sourceTree.description,
      sourceTree.logo,
      sourceTree.isDefault).id

    service.create(1, "titleE", "descriptionE", logo = None, isDefault = false)

    val storedTree = treeId.map(service.get)

    assert(storedTree.isDefined)
    assert(storedTree.get.courseId == sourceTree.courseId)
    assert(storedTree.get.title == sourceTree.title)
    assert(storedTree.get.description == sourceTree.description)
    assert(!storedTree.get.published)
    assert(storedTree.get.logo.isEmpty)
  }

  test("update") {
    createSchema()

    service.create(1, "titleA", "descriptionA", logo = None, isDefault = false)
    val tree = service.create(2, "titleD", "descriptionD", logo = Some("1.png"), isDefault = false)
    service.create(1, "titleA", "descriptionA", logo = None, isDefault = false)

    service.update(tree.id.get, tree.title, tree.description, tree.isDefault)
    service.publish(tree.id.get)

    val storedTree = service.get(tree.id.get)

    assert(storedTree.published)
  }

  test("get root nodes") {
    createSchema()

    val tree = service.create(2, "titleD", "descriptionD", logo = Some("1.png"), isDefault = false)

    nodeService.createEmptyNode(tree.id.get, parentId = None, "t", "d", comment = None)

    val nodes = nodeService.getByParent(tree.id.get, None)

    assert(nodes.size == 1)
  }

  test("delete story") {
    import driver.api._
    createSchema()

    val newStory = Story(id = None, 25199L, "new title", "new description", None, published = false, isDefault = false)

    val createStoryIdAction =
      for {
        treeId <- (trees returning trees.map(_.id)) += newStory
        nodeParentId <- (nodes returning nodes.map(_.id)) +=
          StoryNode(id = None, None, treeId, "node title", "node description", None)
        nodeId <- nodes += StoryNode(id = None, Some(nodeParentId), treeId, "node title", "node description", None)
        _ <- packages += StoryPackageItem(id = None, nodeId, treeId, 123L, None)
        _ <- packages += StoryPackageItem(id = None, nodeId, treeId, 1234L, None)
      } yield treeId

    val treeId = await(db.run(createStoryIdAction))

    service.delete(treeId)

  }
}