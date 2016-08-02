package com.arcusys.valamis.web.servlet.lesson.serializer

import com.arcusys.valamis.lesson.model.LessonInfo
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}

class LessonInfoSerializer extends CustomSerializer[LessonInfo](implicit format => ({
  case jValue: JValue =>
    LessonInfo(
      jValue.\("id").extract[Int],
      jValue.\("title").extract[String],
      jValue.\("description").extract[String]
    )
}, {
  case pack: LessonInfo => render(Extraction.decompose(pack)(DefaultFormats))
}))
