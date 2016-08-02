package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model.PlainText
import slick.dbio.DBIO

trait PlainTextStorage extends ContentStorageBase[PlainText]{
  def getById(id: Long): DBIO[Option[PlainText]]
  def create(plainText: PlainText): DBIO[PlainText]
  def update(plainText: PlainText): DBIO[Int]
  def delete(id: Long): DBIO[Int]

}