package com.arcusys.learn.utils

import com.arcusys.valamis.web.servlet.base.HttpUtilsHelper
import org.scalatest.FunSuite

class HttpUtilsHelperTests extends FunSuite {

  val enc = "UTF8"

  test("parseQueryString parse empty") {
    val line = ""
    val result = HttpUtilsHelper.parseQueryString(line, enc)

    assert(result.isEmpty)
  }

  test("parseQueryString single parameter") {
    val line = "a=34"
    val result = HttpUtilsHelper.parseQueryString(line, enc)

    assert(result.size() == 1)
    assert(Array("34") sameElements result.get("a"))
  }

  test("parseQueryString multi parameter") {
    val line = "a[]=34&a[]=qwe"
    val result = HttpUtilsHelper.parseQueryString(line, enc)

    assert(result.size() == 1)
    assert(Array("34", "qwe") sameElements result.get("a[]"))
  }

  test("parseQueryString multi parameter 2") {
    val line = "a[]=34&a[]=45&a[]=qwe"
    val result = HttpUtilsHelper.parseQueryString(line, enc)

    assert(result.size() == 1)
    assert(Array("34", "45", "qwe") sameElements result.get("a[]"))
  }
}
