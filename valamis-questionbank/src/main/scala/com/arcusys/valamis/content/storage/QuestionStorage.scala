package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model._
import slick.dbio.DBIO

trait QuestionStorage extends ContentStorageBase[Question] {

  def getById(id: Long): DBIO[Option[Question]]

  def create(question: Question): DBIO[Question]

  def createWithCategory(question: Question, categoryId: Option[Long]): DBIO[Question]

  def update(question: Question): DBIO[Int]

  def delete(id: Long): DBIO[Int]

}