package com.arcusys.valamis.web.servlet.request

import javax.servlet.http.HttpServletResponse
import com.arcusys.valamis.web.util.DateTimeUtil
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.apache.http.ParseException
import org.joda.time.DateTime
import org.scalatra.ScalatraBase
import scala.util.{Failure, Success, Try}


object Parameter {
  def apply(name: String)(implicit kernel: ScalatraBase): Parameter = new Parameter(name, kernel)
}

class Parameter(name: String, kernel: ScalatraBase) {
  import kernel._

  def withDefault(default: String): String = kernel.params.get(name) match {
    case Some(value) => value
    case None        => default
  }

  def required: String = kernel.params.get(name) match {
    case Some(value) => value
    case None =>
      throw new ParameterNamesNotFoundException(s"Required parameter '$name' is not specified")
  }

  def longRequired: Long = Try(required.toLong) match {
    case Success(value) => value
    case Failure(_) =>
      throw new ParseException(s"Long parameter '$name' could not be parsed")
  }

  def intRequired: Int = Try(required.toInt) match {
    case Success(value) => value
    case Failure(_) =>
      throw new ParseException(s"Integer parameter '$name' could not be parsed")
  }

  def floatRequired: Float = Try(required.toFloat) match {
    case Success(value) => value
    case Failure(_) =>
      throw new ParseException(s"Float parameter '$name' could not be parsed")
  }

  def floatOption: Option[Float] = Try(required.toFloat) match {
    case Success(value) => Option(value)
    case Failure(_) =>
      throw new ParseException(s"Float parameter '$name' could not be parsed")
  }

  def doubleRequired: Double = Try(required.toDouble) match {
    case Success(value) => value
    case Failure(_) =>
      throw new ParseException(s"Double parameter '$name' could not be parsed")
  }

  def doubleOption: Option[Double] = Try(required.toDouble) match {
    case Success(value) => Option(value)
    case Failure(_)     => None
  }

  def booleanRequired: Boolean = required.toLowerCase match {
    case "1" | "on" | "true"   => true
    case "0" | "off" | "false" => false
    case _ =>
      throw new ParseException(s"Boolean parameter '$name' could not be parsed")
  }
  def booleanOption: Option[Boolean] = Try(booleanRequired).toOption
  def booleanOption(none: String): Option[Boolean] = {
    val value = params.getOrElse(name, none)
    if (value == none) None
    else Some(try {
      value.toBoolean
    } catch {
      case _: Throwable => halt(HttpServletResponse.SC_BAD_REQUEST, s"Integer parameter '$name' could not be parsed")
    })
  }

  def multiRequired: Seq[String] = kernel.multiParams.get(name) match {
    case Some(value) => value
    case None =>
      throw new ParameterNamesNotFoundException(s"Required parameter '$name' is not specified")
  }

  def multiWithEmpty: Seq[String] = kernel.multiParams.get(name) match {
    case Some(value) => value
    case None        => Seq[String]()
  }

  def multiLongRequired: Seq[Long] = {
    val result = multiLong
    if (result.isEmpty) throw new ParameterNamesNotFoundException(s"Required parameter '$name' is not specified")

    result
  }

  def multiLong: Seq[Long] = {
    kernel.multiParams.get(name) match {
      case Some(values) => Try(values.map(_.toLong))
        .getOrElse(throw new ParseException(s"Multi long parameter '$name' could not be parsed"))
      case None => Seq()
    }
  }

  def contains: Boolean = kernel.multiParams.contains(name)

  def option: Option[String] = kernel.params.get(name)
  def option(none: String): Option[String] = {
    kernel.params.get(name).filterNot(_ == none)
  }

  def intOption: Option[Int] = Try(intRequired).toOption
  def intOption(none: String): Option[Int] = {
    val value = params.getOrElse(name, none)
    if (value == none) None
    else Some(value.toInt)
  }
  def intOption(none: Int): Option[Int] = intOption(none.toString)

  def longOption: Option[Long] = Try(longRequired).toOption
  def longOption(none: String): Option[Long] = {
    val value = params.getOrElse(name, none)
    if (value == none) None else Some(value.toLong)
  }
  def longOption(none: Long): Option[Long] = {
    longOption.filterNot(_ == none)
  }

  def bigDecimalOption(none: String): Option[BigDecimal] = {
    val value = params.getOrElse(name, none)
    if (value == none) None
    else Some(BigDecimal(value.toDouble))
  }
  def bigDecimalOption: Option[BigDecimal] = option flatMap { str => Try(BigDecimal(str)).toOption }

  def bigDecimalRequired: BigDecimal = Try(BigDecimal(required)) match {
    case Success(value) => value
    case Failure(_) =>
      throw new ParseException(s"BigDecimal parameter '$name' could not be parsed")
  }

  def dateTimeOption: Option[DateTime] = {
    params.get(name) match {
      case None => None
      case Some("") => None
      case Some(str) => Some(DateTimeUtil.parseDateWithTime(str))
    }
  }

  def dateOption: Option[DateTime] = {
    params.get(name) match {
      case None => None
      case Some("") => None
      case Some(str) => Some(DateTimeUtil.parseDate(str))
    }
  }
}