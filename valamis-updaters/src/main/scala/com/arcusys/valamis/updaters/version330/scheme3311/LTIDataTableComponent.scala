package com.arcusys.valamis.updaters.version330.scheme3311

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}

trait LTIDataTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.api._

  val ltiDatas = TableQuery[LTIDataTable]

  case class LTIData(uuid: String,
                     title: Option[String],
                     text: Option[String],
                     url: String,
                     width: Option[Int],
                     height: Option[Int],
                     returnType: String,
                     ltiStatus: Option[String] = None)


  class LTIDataTable(tag: Tag) extends Table[LTIData](tag, tblName("LTI_DATA")) {

    def uuid = column[String]("UUID", O.PrimaryKey, O.SqlType(uuidKeyLength))

    def title = column[Option[String]]("TITLE", O.Length(254, varying = true))

    def text = column[Option[String]]("TEXT", O.Length(254, varying = true))

    def url = column[String]("URL", O.Length(2000, varying = true))

    def width = column[Option[Int]]("WIDTH")

    def height = column[Option[Int]]("HEIGHT")

    def returnType = column[String]("RETURNTYPE", O.Length(10, varying = true))

    def ltiStatus = column[Option[String]]("STATUS", O.Length(254, varying = true))

    def * =
      (
        uuid,
        title,
        text,
        url,
        width,
        height,
        returnType,
        ltiStatus
      ) <> (LTIData.tupled, LTIData.unapply)

  }

}