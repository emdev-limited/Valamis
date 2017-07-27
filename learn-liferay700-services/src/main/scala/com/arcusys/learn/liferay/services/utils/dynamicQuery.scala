package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.dao.orm.{Criterion, DynamicQuery, RestrictionsFactoryUtil}

import scala.collection.JavaConversions._

/**
  * Created by mminin on 04.03.16.
  */
package object dynamicQuery {

  /**
    * dynamic query with preliminary false restriction
    */
  class UselessRestrictionException extends Exception

  private val InLimit = 1000

  implicit class DynamicQueryExtensions(val query: DynamicQuery) extends AnyVal {

    def addFilterByValues[T](propertyKey: String, values: Seq[T], contains: Boolean): DynamicQuery = {
      if (values.isEmpty) {
        if (contains) throw new UselessRestrictionException
        query
      }
      else {
        val inIdsCriterion = makeInSetCriterion(propertyKey, values)
        query.add(if (contains) inIdsCriterion else RestrictionsFactoryUtil.not(inIdsCriterion))
      }
    }

    private def makeInSetCriterion[T](propertyKey: String, values: Seq[T]): Criterion = {
      val criterions = for (offset <- 0 until(values.length, InLimit)) yield {
        RestrictionsFactoryUtil.in(propertyKey, values.slice(offset, offset + InLimit))
      }

      criterions.reduceLeft((c1, c2) => RestrictionsFactoryUtil.or(c1, c2))
    }

    def addLikeRestriction(propertyKey: String, pattern: Option[String]): DynamicQuery = {
      pattern match {
        case None => query
        case Some(v) => query.add(RestrictionsFactoryUtil.ilike(propertyKey, v))
      }
    }
  }

}
