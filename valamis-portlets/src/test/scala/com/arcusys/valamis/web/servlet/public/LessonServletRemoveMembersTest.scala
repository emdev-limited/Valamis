package com.arcusys.valamis.web.servlet.public

/**
  * Created by pkornilov on 2/17/17.
  */
class LessonServletRemoveMembersTest extends LessonServletBaseTest
with MemberBase {

  test("should fail for empty body") {
    createSampleLesson()
    checkBadJson(bodyStr = "")
  }

  test("should fail for bad json") {
    createSampleLesson()
    checkBadJson(bodyStr = "null")
    checkBadJson(bodyStr = "0")
    checkBadJson(bodyStr = "{}")
  }

  test("should fail for non existing lesson") {
    val url = "/lessons/1/members"
    deleteWithBody(url, body = "") {
      statusAssert("DELETE", url, 404)
      parse(body) shouldBe noLessonResponse(1)
    }
  }

  test("should remove members") {
    initMocks()
    createSampleLesson()
    val url = "/lessons/1/members"
    addMembers(url)

    //delete user
    deleteWithBody(url, body =
      """[
        |{
        |  "id": 3,
        |  "memberType": "user"
        |}
        |]
      """.stripMargin) {
      statusAssert("DELETE", url, 204)
      body shouldBe empty
    }
    getWithStatusAssert(url + "?memberType=user", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 1,
        |      "name": "Vanya",
        |      "memberType": "user"
        |    }
        |  ]
      """.stripMargin
    )

    //delete user group
    deleteWithBody(url, body =
      """[
        | {
        |    "id": 1,
        |    "memberType": "userGroup"
        |  },
        |  {
        |    "id": 2,
        |    "memberType": "userGroup"
        |  }
        |]
      """.stripMargin) {
      statusAssert("DELETE", url, 204)
      body shouldBe empty
    }
    getWithStatusAssert(url + "?memberType=userGroup", 200)(body) shouldBe "[]"

    //delete roles
    deleteWithBody(url, body =
      """[
        |{
        |    "id": 3,
        |    "memberType": "role"
        |}
        |]
      """.stripMargin) {
      statusAssert("DELETE", url, 204)
      body shouldBe empty
    }
    getWithStatusAssert(url + "?memberType=role", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 2,
        |      "name": "Role 2",
        |      "memberType": "role"
        |    }
        |  ]
      """.stripMargin
    )

    //delete organizations
    deleteWithBody(url, body =
      """[
        |{
        |    "id": 1,
        |    "memberType": "organization"
        |}
        |]
      """.stripMargin) {
      statusAssert("DELETE", url, 204)
      body shouldBe empty
    }
    getWithStatusAssert(url + "?memberType=organization", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 2,
        |      "name": "Organization 2",
        |      "memberType": "organization"
        |    }
        |]
      """.stripMargin
    )
  }

  private def checkBadJson(bodyStr: String): Unit = {
    val url = "/lessons/1/members"
    deleteWithBody(url, body = bodyStr) {
      statusAssert("DELETE", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

  protected def addMembers(url: String): Unit = {
    post(url, body =
      """
        |[
        |  {
        |    "id": 1,
        |    "memberType": "user"
        |  },
        |  {
        |    "id": 3,
        |    "memberType": "user"
        |  },
        |
        |  {
        |    "id": 1,
        |    "memberType": "userGroup"
        |  },
        |  {
        |    "id": 2,
        |    "memberType": "userGroup"
        |  },
        |
        |  {
        |    "id": 2,
        |    "memberType": "role"
        |  },
        |  {
        |    "id": 3,
        |    "memberType": "role"
        |  },
        |
        |  {
        |    "id": 1,
        |    "memberType": "organization"
        |  },
        |  {
        |    "id": 2,
        |    "memberType": "organization"
        |  }
        |]
      """.stripMargin) {
      statusAssert("POST", url, 204)
      body shouldBe empty
    }
  }

}
