package com.arcusys.valamis.slide.service.export

import com.arcusys.valamis.slide.model._
import org.json4s.JsonAST._
import org.json4s.Extraction._
import org.json4s.{DefaultFormats, CustomSerializer}


//FIXME: we lose other custom serializers from format
class SlidePropertiesSerializer extends CustomSerializer[SlideModel](implicit format => ( {
  case jValue: JValue => ???
}, {
  case i: SlideModel =>
    decompose(i)(DefaultFormats + new SlideElementsPropertiesSerializer)
      .replace("properties" :: Nil, JObject(i.properties.map(x =>
      x.deviceId.toString -> JObject(x.properties.map(p =>
        p.key -> JString(p.value)
      ): _*)
    ): _*))
}
  ))

class SlideElementsPropertiesSerializer extends CustomSerializer[SlideElementModel](implicit format => ( {
  case jValue: JValue => ???
}, {
  case i: SlideElementModel =>
    decompose(i)(DefaultFormats)
      .replace("properties" :: Nil, JObject(i.properties.map(x =>
      x.deviceId.toString -> JObject(x.properties.map(p =>
        p.key -> JString(p.value)
      ): _*)
    ): _*))
}
  ))
