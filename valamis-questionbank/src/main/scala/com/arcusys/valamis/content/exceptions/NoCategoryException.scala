package com.arcusys.valamis.content.exceptions

/**
 * Created by mromanova on 13.10.15.
 */
class NoCategoryException(val id: Long) extends Exception(s"no category with id: $id")
