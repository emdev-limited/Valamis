package com.arcusys.valamis.persistence.impl.contentProviders.schema

import com.arcusys.valamis.slide.service.contentProvider.model.ContentProvider
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.util.TupleHelpers._

/**
  * Created By:
  * User: zsoltberki
  * Date: 29.9.2016
  */
trait ContentProviderTableComponent extends TypeMapper { self: SlickProfile =>
  import driver.api._

  val contentProviders = TableQuery[ContentProviderTable]

  class ContentProviderTable(tag : Tag) extends Table[ContentProvider](tag, tblName("CONTENT_PROVIDERS")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME", O.Length(20, varying = true))
    def image = column[String]("IMAGE_URL", O.Length(2000, varying = true))
    def description = column[String]("DESCRIPTION")
    def url = column[String]("URL", O.Length(2000, varying = true))
    def width = column[Int]("WIDTH")
    def height = column[Int]("HEIGHT")
    def isPrivate = column[Boolean]("IS_PRIVATE")
    def customerKey = column[String]("CUSTOMER_KEY", O.Length(254, varying = true))
    def customerSecret = column[String]("CUSTOMER_SECRET", O.Length(254, varying = true))

    def * =
      (
        id,
        name,
        description,
        image,
        url,
        width,
        height,
        isPrivate,
        customerKey,
        customerSecret
      ) <> (ContentProvider.tupled, ContentProvider.unapply)
  }
}