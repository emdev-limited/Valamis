package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model._

trait QuestionStorage extends ContentStorageBase[Question] {

  def getById(id: Long): Option[Question]

  def create(question: Question): Question

  def createWithCategory(question: Question, categoryId: Option[Long]): Question

  def update(question: Question): Unit

  def delete(id: Long): Unit

}