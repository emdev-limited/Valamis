package com.arcusys.valamis.tests.ui.contentManager

import com.arcusys.valamis.tests.ui.base.{UITestSuite, WebDriverArcusys}
import com.arcusys.valamis.tests.ui.contentManager.page.ContentManagerPage

/**
 * Created by Igor Borisov on 13.07.15.
 */
class QuestionsTests(val driver: WebDriverArcusys) extends UITestSuite {

  it should "be able to create choice question" in {

    val page = new ContentManagerPage(driver)
    page.open()

    val title = "Title for choice question"
    val text = "Text for choice question"
    val typeQuestion = "choiceType"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createQuestion(title, text, typeQuestion)

    assert(!tree.existItem(title), "Choice question should not be in tree")

    val list = page.contentList

    assert(list.existsQuestion(title), "Choice question should be in list")

    val question = list.getQuestionItem(title)

    question.title shouldEqual title

    page.deleteItem(question)
  }

  it should "be able to create short question" in {

    val page = new ContentManagerPage(driver)
    page.open()

    val title = "Title for short question"
    val text = "Text for short question"
    val typeQuestion = "shortType"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createQuestion(title, text, typeQuestion)

    assert(!tree.existItem(title), "Short question should not be in tree")

    val list = page.contentList

    assert(list.existsQuestion(title), "Short question should be in list")

    val question = list.getQuestionItem(title)

    question.title shouldEqual title
  }

  it should "be able to create positioning question" in {

    val page = new ContentManagerPage(driver)
    page.open()

    val title = "Title for positioning question"
    val text = "Text for positioning question"
    val typeQuestion = "positioningType"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createQuestion(title, text, typeQuestion)

    assert(!tree.existItem(title), "Positioning question should not be in tree")

    val list = page.contentList

    assert(list.existsQuestion(title), "Positioning question should be in list")

    val question = list.getQuestionItem(title)

    question.title shouldEqual title
  }

  it should "be able to create matching question" in {

    val page = new ContentManagerPage(driver)
    page.open()

    val title = "Title for matching question"
    val text = "Text for matching question"
    val typeQuestion = "matchingType"

    val tree = page.contentTree
    tree.activateRootItem()

    page.createQuestion(title, text, typeQuestion)

    assert(!tree.existItem(title), "Matching question should not be in tree")

    val list = page.contentList

    assert(list.existsQuestion(title), "Matching question should be in list")

    val question = list.getQuestionItem(title)

    question.title shouldEqual title
  }
}
