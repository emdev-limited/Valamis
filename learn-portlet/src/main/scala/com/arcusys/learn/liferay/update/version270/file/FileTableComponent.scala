package com.arcusys.learn.liferay.update.version270.file

import com.arcusys.valamis.file.model.FileRecord
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}

trait FileTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  class FileTable(tag : Tag) extends Table[FileRecord](tag, tblName("FILE")) {
    def filename = column[String]("FILENAME", O.PrimaryKey, O.DBType("varchar(255)"))
    def content = column[Option[Array[Byte]]]("CONTENT", binaryOptions: _*)

    def * = (filename, content) <> (FileRecord.tupled, FileRecord.unapply)
  }

  val files = TableQuery[FileTable]
}

