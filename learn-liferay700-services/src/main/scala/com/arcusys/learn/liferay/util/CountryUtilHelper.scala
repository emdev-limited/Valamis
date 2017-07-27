package com.arcusys.learn.liferay.util

import java.util.Locale

import com.liferay.portal.kernel.model.Country


/**
 * User: Yulia.Glushonkova
 * Date: 18.08.14
 */
object CountryUtilHelper {
  def getName(country: Country) = country.getName(Locale.getDefault)
}
