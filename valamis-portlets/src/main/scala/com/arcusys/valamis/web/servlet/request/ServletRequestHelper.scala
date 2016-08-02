package com.arcusys.valamis.web.servlet.request

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.model.SkipTake
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.apache.http.ParseException

import scala.util.{Failure, Success, Try}

/**
 * Created by pkornilov on 19.01.16.
 *
 */
object ServletRequestHelper {

  implicit class ServletRequestExt(val r: HttpServletRequest) {

    def withDefault(name: String, default: String): String = Option(r.getParameter(name)) match {
      case Some(value) => value
      case None => default
    }

    def required(name: String): String = Option(r.getParameter(name)) match {
      case Some(value) => value
      case None =>
        throw new ParameterNamesNotFoundException(s"Required parameter '$name' is not specified")
    }

    def longRequired(name: String): Long = Try(required(name).toLong) match {
      case Success(value) => value
      case Failure(_) =>
        throw new ParseException(s"Long parameter '$name' could not be parsed")
    }

    def intRequired(name: String): Int = Try(required(name).toInt) match {
      case Success(value) => value
      case Failure(_) =>
        throw new ParseException(s"Integer parameter '$name' could not be parsed")
    }
    def intOption(name: String): Option[Int] = Try(intRequired(name)).toOption

    def booleanRequired(name: String): Boolean = required(name).toLowerCase match {
      case "1" | "on" | "true" => true
      case "0" | "off" | "false" => false
      case _ =>
        throw new ParseException(s"Boolean parameter '$name' could not be parsed")
    }

    def booleanOption(name: String): Option[Boolean] = Try(booleanRequired(name)).toOption

    def page = intOption(BaseCollectionRequest.Page).getOrElse(1)
    def count = intRequired(BaseCollectionRequest.Count)
    def skip = (page - 1) * count

    def pageOpt = intOption(BaseCollectionRequest.Page)
    def countOpt = intOption(BaseCollectionRequest.Count)

    def skipTake = {
      if (pageOpt.isEmpty || countOpt.isEmpty) None
      else if (pageOpt.nonEmpty && countOpt.nonEmpty) Some(SkipTake(skip, count))
      else throw new ParseException("page or count parameter couldn't be parsed")
    }

  }

}
