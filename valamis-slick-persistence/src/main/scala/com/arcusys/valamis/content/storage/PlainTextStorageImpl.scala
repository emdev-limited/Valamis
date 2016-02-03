package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model.PlainText
import com.arcusys.valamis.content.schema.ContentTableComponent
import com.arcusys.valamis.core.OptionFilterSupport

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 **  Created by pkornilov on 23.10.15.
 */

class PlainTextStorageImpl (val db:     JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends PlainTextStorage with ContentTableComponent with OptionFilterSupport {

  import driver.simple._

  override def getById(id: Long): Option[PlainText] =  db.withSession { implicit s =>
    plainTexts.filter(_.id===id).firstOption
  }

  override def update(plainText: PlainText): Unit = db.withSession { implicit  s=>
    plainTexts.filter(_.id === plainText.id).update(plainText)
  }

  override def delete(id: Long): Unit = db.withSession {implicit  s=>
    plainTexts.filter(_.id===id).delete
  }

  override def create(plainText: PlainText): PlainText = db.withSession{implicit  s=>
    val id = (plainTexts returning plainTexts.map(_.id)).insert(plainText)
    plainTexts.filter(_.id===id).first
  }

  override def getByCourse(courseId: Long): Seq[PlainText] = db.withSession{implicit s =>
    plainTexts.filter(_.courseId === courseId).list
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[PlainText] = db.withSession{implicit s =>
    plainTexts.filter(pt => optionFilter(pt.categoryId,categoryId) &&  pt.courseId === courseId).list
  }

  override def getCountByCourse(courseId: Long): Int = db.withSession{implicit s =>
    plainTexts.filter(_.courseId === courseId).length.run
  }

  override def getCountByCategory(categoryId: Option[Long], courseId: Long): Int = db.withSession { implicit s =>
    plainTexts.filter(pt => optionFilter(pt.categoryId,categoryId) &&  pt.courseId === courseId).length.run
  }

  override def moveToCategory(id:Long, newCategoryId:Option[Long],courseId:Long) = db.withSession {implicit s =>
    val query = for { q <- plainTexts if q.id === id } yield (q.categoryId,q.courseId)
    query.update(newCategoryId,courseId).run
  }

  override def moveToCourse(id:Long, courseId:Long,moveToRoot:Boolean) = db.withSession { implicit s =>
    if (moveToRoot) {
      val query = for {q <- plainTexts if q.id === id} yield (q.courseId,q.categoryId)
      query.update(courseId,None).run
    } else {
      val query = for {q <- plainTexts if q.id === id} yield q.courseId
      query.update(courseId).run
    }
  }

}
