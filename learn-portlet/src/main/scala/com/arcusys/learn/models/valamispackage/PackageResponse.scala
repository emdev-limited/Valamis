package com.arcusys.learn.models.valamispackage

import com.arcusys.valamis.lesson.model.{LessonType, PackageUploadModel, ValamisTag}
import com.arcusys.valamis.ratings.model.Rating
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}

case class PlayerPackageResponse(id: Long,
  title: String,
  description: Option[String],
  version: Option[String],
  visibility: Boolean,
  isDefault: Boolean,
  packageType: String,
  logo: Option[String],
  suspendedID: Option[String],
  passingLimit: Int,
  rerunInterval: Int,
  rerunIntervalType: String,
  attemptsCount: Int,
  status: String,
  tags: Seq[ValamisTag],
  beginDate: String,
  endDate: String,
  rating: Rating)

case class PackageResponse(id: Long,
  title: String,
  description: Option[String],
  visibility: Boolean,
  isDefault: Boolean,
  packageType: String,
  logo: Option[String],
  passingLimit: Int,
  rerunInterval: Int,
  rerunIntervalType: String,
  tags: Seq[ValamisTag],
  beginDate: String,
  endDate: String,
  rating: Rating)

class PackageSerializer extends CustomSerializer[PackageUploadModel](implicit format => ({
  case jValue: JValue =>
    PackageUploadModel(
      jValue.\("id").extract[Int],
      jValue.\("title").extract[String],
      jValue.\("description").extract[String],
      jValue.\("packageType").extract[String] match {
        case "scorm" => LessonType.Scorm
        case "tincan" => LessonType.Tincan
        case s => LessonType.withName(s)
      },
      jValue.\("logo").extract[String]
    )
}, {
  case pack: PackageUploadModel => render(Extraction.decompose(pack)(DefaultFormats))
}))