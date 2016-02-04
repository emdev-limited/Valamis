package com.arcusys.valamis.content.exceptions

/**
 * Created by mromanova on 13.10.15.
 */
class NoQuestionException(val id: Long) extends Exception(s"no question with id: $id")
