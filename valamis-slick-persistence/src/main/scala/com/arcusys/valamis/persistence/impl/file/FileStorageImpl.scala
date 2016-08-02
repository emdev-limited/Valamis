package com.arcusys.valamis.persistence.impl.file

import javax.inject.Inject

import com.arcusys.valamis.file.model.FileRecord
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class FileRepositoryImpl @Inject()(val db: JdbcBackend#DatabaseDef,
                                   val driver: JdbcProfile) extends FileStorage
    with FileTableComponent
    with SlickProfile {

  import driver.simple._

  override def getFile(fileName: String): Option[FileRecord] = {
    db.withSession { implicit s =>
      files.filter(f => f.filename === fileName).firstOption
    }
  }


  override def store(fileName: String, content: Array[Byte]): Unit = {
    db.withSession { implicit s =>
      files.insert(new FileRecord(fileName, Some(content)))
    }
  }

  //TODO: replace to 'remove' and 'remove by name prefix'
  override def delete(fileName: String, byPrefix: Boolean): Unit = {
    db.withSession { implicit s =>
      if (byPrefix)
        files.filter(f => f.filename.startsWith(fileName)).delete
      else
        files.filter(f => f.filename === fileName).delete
    }
  }

  override def getFiles(namePrefix: String): Seq[FileRecord] = {
    db.withSession { implicit s =>
      files.filter(f => f.filename.startsWith(namePrefix)).list
    }
  }
}
