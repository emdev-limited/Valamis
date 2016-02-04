package com.arcusys.valamis.storyTree

import java.sql.Connection

import com.arcusys.valamis.storyTree.model.{StoryNode, Story}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._

class StoryTreeStorageTest extends FunSuite with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
  val storage = new StoryTreeStorageImpl(db, H2Driver)
  val nodeStorage = new StoryTreeNodeStorageImpl(db, H2Driver)

  var connection: Connection = _

  // db data will be released after connection close
  before {
    connection = db.createConnection()
  }
  after {
    connection.close()
  }

  def createSchema() {
    new StoryTreeTableComponent {
      override protected val driver = H2Driver
      import driver.simple._
      db.withSession { implicit session => (trees.ddl ++ nodes.ddl).create }
    }
  }

  test("insert and get") {
    createSchema()

    val sourceTree = new Story(None, 2, "titleD", "descriptionD", Some("1.png"), true)

    storage.create(new Story(None, 1, "titleA", "descriptionA", None, false))

    val treeId = storage.create(sourceTree).id

    storage.create(new Story(None, 1, "titleE", "descriptionE", None, false))

    val storedTree = storage.get(treeId.get)

    assert(storedTree.isDefined)
    assert(storedTree.get.courseId == sourceTree.courseId)
    assert(storedTree.get.title == sourceTree.title)
    assert(storedTree.get.description == sourceTree.description)
    assert(storedTree.get.published == sourceTree.published)
    assert(storedTree.get.logo == sourceTree.logo)
  }

  test("update") {
    createSchema()

    storage.create(new Story(None, 1, "titleA", "descriptionA", None, false))
    val tree = storage.create(new Story(None, 2, "titleD", "descriptionD", Some("1.png"), false))
    storage.create(new Story(None, 1, "titleA", "descriptionA", None, false))

    storage.update(tree.copy(published = true))

    val storedTree = storage.get(tree.id.get).get

    assert(storedTree.published)
  }

  test("get root nodes") {
    createSchema()

    val tree = storage.create(new Story(None, 2, "titleD", "descriptionD", Some("1.png"), false))

    nodeStorage.create(new StoryNode(None, None, tree.id.get, "t", "d", None))

    val nodes = nodeStorage.getByParent(tree.id.get, None)

    assert(nodes.size == 1)
  }
}
