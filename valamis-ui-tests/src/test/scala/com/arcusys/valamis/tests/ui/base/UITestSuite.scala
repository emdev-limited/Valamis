package com.arcusys.valamis.tests.ui.base

import org.scalatest.{BeforeAndAfter, Suite, FlatSpec, Matchers}

/**
 * Created by Igor Borisov on 13.07.15.
 */
abstract class UITestSuite extends FlatSpec with Suite with Matchers with BeforeAndAfter with UITestBase