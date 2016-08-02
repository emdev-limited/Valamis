package com.arcusys.valamis.web.servlet.request

import com.arcusys.valamis.model.SkipTake
import org.apache.http.ParseException
import org.scalatra.ScalatraBase

object BaseCollectionRequest extends BaseCollectionRequest

trait BaseCollectionRequest {
  final val Page = "page"
  final val Count = "count"
  final val SortBy = "sortBy"
  final val SortAscDirection = "sortAscDirection"
  final val sortBy = "sortBy"
}

abstract class BaseCollectionRequestModel(scalatra: ScalatraBase) {
  implicit val _scalatra = scalatra

  def page = Parameter(BaseCollectionRequest.Page).intOption.getOrElse(1)
  def count = Parameter(BaseCollectionRequest.Count).intRequired
  def skip = (page - 1) * count
  def isSortDirectionAsc = Parameter(BaseCollectionRequest.SortAscDirection).booleanOption match {
    case Some(value) => value
    case None        => true
  }
  def ascending = Parameter(BaseCollectionRequest.SortAscDirection).booleanOption match {
    case Some(value) => value
    case None        => true
  }

  def pageOpt = Parameter(BaseCollectionRequest.Page).intOption
  def countOpt = Parameter(BaseCollectionRequest.Count).intOption

  def skipTake = {
    if (pageOpt.isEmpty || countOpt.isEmpty) None
    else if (pageOpt.nonEmpty && countOpt.nonEmpty) Some(SkipTake(skip, count))
    else throw new ParseException("page or count parameter couldn't be parsed")
  }
}

abstract class BaseSortableCollectionRequestModel[T](scalatra: ScalatraBase, toEnum: String => T) extends BaseCollectionRequestModel(scalatra) {
  def sortBy: T = {
    toEnum(Parameter(BaseCollectionRequest.SortBy).required)
  }
}