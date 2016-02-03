package com.arcusys.valamis.ratings

import com.arcusys.valamis.lesson.model.BaseManifest
import org.scalatest.FunSuite

/**
 * Created by Igor Borisov on 30.10.15.
 */
class RatingServiceTest extends FunSuite {

  test("Valamis package rating test") {
    val service = new RatingService[BaseManifest]{
      def classNamePublic = classname
    }

    assert(
      service.classNamePublic === "com.arcusys.valamis.lesson.model.BaseManifest",
      "Class name for base manifest must be equal values of class name already existed in database")
  }
}
