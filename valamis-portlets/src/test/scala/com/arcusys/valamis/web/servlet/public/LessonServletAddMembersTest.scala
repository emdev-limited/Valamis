package com.arcusys.valamis.web.servlet.public

/**
  * Created by pkornilov on 2/17/17.
  */
class LessonServletAddMembersTest extends LessonServletBaseTest
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
    post(url, body = "") {
      statusAssert("POST", url, 404)
      parse(body) shouldBe noLessonResponse(1)
    }
  }

  test("should add members") {
    initMocks()

    createSampleLesson()
    val url = "/lessons/1/members"
    addMembers(url)

    getWithStatusAssert(url + "?memberType=user", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 3,
        |      "name": "Anatoliy",
        |      "memberType": "user"
        |    },
        |    {
        |      "id": 1,
        |      "name": "Vanya",
        |      "memberType": "user"
        |    }
        |  ]
      """.stripMargin
    )

    getWithStatusAssert(url + "?memberType=userGroup", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 1,
        |      "name": "Group 1",
        |      "memberType": "userGroup"
        |    },
        |    {
        |      "id": 2,
        |      "name": "Group 2",
        |      "memberType": "userGroup"
        |    }
        |  ]
      """.stripMargin
    )


    getWithStatusAssert(url + "?memberType=role", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 2,
        |      "name": "Role 2",
        |      "memberType": "role"
        |    },
        |    {
        |      "id": 3,
        |      "name": "Role 3",
        |      "memberType": "role"
        |    }
        |  ]
      """.stripMargin
    )


    getWithStatusAssert(url + "?memberType=organization", 200)(parse(body)) shouldEqual parse(
      """
        |[
        |    {
        |      "id": 1,
        |      "name": "Organization 1",
        |      "memberType": "organization"
        |    },
        |    {
        |      "id": 2,
        |      "name": "Organization 2",
        |      "memberType": "organization"
        |    }
        |  ]
      """.stripMargin
    )

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

  private def checkBadJson(bodyStr: String): Unit = {
    val url = "/lessons/1/members"
    post(url, body = bodyStr) {
      statusAssert("POST", url, 400)
      parse(body) shouldBe parse(
        """{
          | "code":-1,
          | "message":"Bad JSON value"
          | }
        """.stripMargin)
    }
  }

}
