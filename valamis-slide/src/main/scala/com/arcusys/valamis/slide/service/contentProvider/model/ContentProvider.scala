package com.arcusys.valamis.slide.service.contentProvider.model

/**
  * Created By:
  * User: zsoltberki
  * Date: 28.9.2016
  */
case class ContentProvider(id: Long = 0L,
                           name: String,
                           description: String,
                           image: String,
                           url: String,
                           width: Int,
                           height: Int,
                           isPrivate: Boolean,
                           customerKey: String,
                           customerSecret: String)
