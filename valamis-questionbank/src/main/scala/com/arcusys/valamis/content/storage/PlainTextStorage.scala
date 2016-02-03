package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model.PlainText

trait PlainTextStorage extends ContentStorageBase[PlainText]{
  def getById(id: Long): Option[PlainText]
  def create(plainText: PlainText): PlainText
  def update(plainText: PlainText): Unit
  def delete(id: Long): Unit

}