package com.arcusys.valamis.content.model

case class ContentTree(contentAmount: Int,
                       nodes: Seq[ContentTreeNode])

trait ContentTreeNode{
  def item: Content
}

case class CategoryTreeNode(item: Category,
                            contentAmount: Int,
                            nodes: Seq[ContentTreeNode]) extends ContentTreeNode

case class PlainTextNode(item: PlainText) extends ContentTreeNode

case class QuestionNode(item: Question,
                        answer: Seq[Answer]) extends ContentTreeNode
