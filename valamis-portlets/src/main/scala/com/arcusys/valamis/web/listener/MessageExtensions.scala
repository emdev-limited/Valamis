package com.arcusys.valamis.web.listener

import com.arcusys.learn.liferay.LiferayClasses.LMessage

/**
  * Created by pkornilov on 3/20/17.
  */
trait MessageExtensions {

  /**
    * we need additional methods to get values
    * because message.get###() returns some defaults if no correct value
    */
  implicit class MessageExt(val msg: LMessage) {

    def getStringRequired(name: String): String = {
      getStringOptions(name).getOrElse {
        throw new NoSuchElementException(s"no $name field")
      }
    }

    def getStringOptions(name: String): Option[String] = {
      Option(msg.get(name)).map(_.toString)
    }

    def getLongRequired(name: String): Long = {
      getStringRequired(name).toLong
    }

    def getDoubleOptional(name: String): Option[Double] = {
      getStringOptions(name).map(_.toDouble)
    }
  }
}
