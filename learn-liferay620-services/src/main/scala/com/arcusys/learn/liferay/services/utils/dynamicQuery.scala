package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.dao.orm.{DynamicQuery, RestrictionsFactoryUtil}

import scala.collection.JavaConverters.asJavaCollectionConverter

/**
  * Created by mminin on 04.03.16.
  */
package object dynamicQuery {

  implicit class DynamicQueryExtensions(val query: DynamicQuery) extends AnyVal {

    def addInSetRestriction[T](propertyKey: String, values: Seq[T], contains: Boolean): DynamicQuery = {
      if (values.isEmpty) {
        if (contains) throw new IllegalArgumentException("values")
        query
      }
      else {
        val inIdsCriterion = RestrictionsFactoryUtil.in(propertyKey, values.asJavaCollection)
        query.add(if (contains) inIdsCriterion else RestrictionsFactoryUtil.not(inIdsCriterion))
      }
    }

    def addLikeRestriction(propertyKey: String, pattern: Option[String]): DynamicQuery = {
      pattern match {
        case None => query
        case Some(v) => query.add(RestrictionsFactoryUtil.ilike(propertyKey, v))
      }
    }
  }

}
