package com.arcusys.valamis.content.exceptions

/**
  * Created by pkornilov on 09.03.16.
  */
class NoContentException(val id: Long, val contentType:String)
  extends Exception(s"no $contentType with id: $id")
