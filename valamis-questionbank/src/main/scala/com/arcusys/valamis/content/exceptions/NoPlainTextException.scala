package com.arcusys.valamis.content.exceptions

/**
 * Created by mromanova on 13.10.15.
 */
class NoPlainTextException(val id: Long) extends Exception(s"no plaintext with id: $id")
