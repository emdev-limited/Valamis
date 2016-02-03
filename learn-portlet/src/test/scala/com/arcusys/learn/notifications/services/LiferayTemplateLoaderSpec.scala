package com.arcusys.learn.notifications.services

import com.arcusys.learn.notifications.MessageTemplateLoader.MessageTemplate
import com.arcusys.learn.notifications.{ CourseMessageService, MessageType }
import org.joda.time.DateTime
import org.scalatest.{ Matchers, FlatSpec }

class LiferayTemplateLoaderSpec extends FlatSpec with Matchers {

  behavior of "LiferayTemplateLoader"

  it should """translate [$DATE$] to {{date}}""" in {
    val body = <html><body>[$DATE$]</body></html>.toString()
    val now = (new DateTime).toString("EEEE, MMMM d")
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.EnrolledStudent, body), Map("date" -> now))
    rendered shouldEqual s"""<html><body>$now</body></html>"""
  }
  it should """translate one-lined [$REPEAT$]...[$/REPEAT$] to {{#data}}...{{/data}}""" in {
    val body = """<html><body>[$REPEAT$]{{.}} [$/REPEAT$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.EnrolledStudent, body), Map("data" -> Seq("data1", "data2")))
    rendered shouldEqual """<html><body>data1 data2 </body></html>"""
  }
  it should """translate multi-lined [$REPEAT$]...[$/REPEAT$] to {{#data}}...{{/data}}""" in {
    val body =
      """<html>
        |<body>[$REPEAT$]
        |{{.}}
        |[$/REPEAT$]</body>
        |</html>""".stripMargin
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.EnrolledStudent, body), Map("data" -> Seq("data1", "data2")))
    rendered shouldEqual """<html><body>data1data2</body></html>"""
  }

  it should """translate [$COURSE_NAME$] to {{courseName}}""" in {
    val body = """<html><body>[$COURSE_NAME$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.EnrolledStudent, body), Map("courseName" -> "course1"))
    rendered shouldEqual """<html><body>course1</body></html>"""
  }
  it should """translate [$ENROLLED_STUDENTS$] to <br/><table>{{#enrolledStudents}}<tr><td valign="top">{{.}}</td></tr>{{/enrolledStudents}}</table>""" in {
    val enroledStudents = Seq("Student1", "Student2", "Student3")
    val body = """<html><body>[$ENROLLED_STUDENTS$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.EnrolledStudent, body), Map("enrolledStudents" -> enroledStudents))
    rendered shouldEqual """<html><body><br/><table><tr><td valign="top">Student1</td></tr><tr><td valign="top">Student2</td></tr><tr><td valign="top">Student3</td></tr></table></body></html>"""
  }

  it should """translate [$STUDENT_NAME$] to {{studentName}}""" in {
    val body = """<html><body>[$STUDENT_NAME$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.FinishedLearningModule, body), Map("studentName" -> "Student1"))
    rendered shouldEqual """<html><body>Student1</body></html>"""
  }
  it should """translate [$FINISHED_PACKAGES$] to <br/><table>{{#finishedPackages}}<tr><td valign="top">{{.}}</td></tr>{{/finishedPackages}}</table>""" in {
    val finishedPackages = Seq("pkg1", "pkg2")
    val body = """<html><body>[$FINISHED_PACKAGES$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.FinishedLearningModule, body), Map("finishedPackages" -> finishedPackages))
    rendered shouldEqual """<html><body><br/><table><tr><td valign="top">pkg1</td></tr><tr><td valign="top">pkg2</td></tr></table></body></html>"""
  }

  it should """translate [$CERTIFICATE_NAME$] to {{title}}""" in {
    val body = """<html><body>[$CERTIFICATE_NAME$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateExpiration, body), Map("title" -> "certificate1"))
    rendered shouldEqual """<html><body>certificate1</body></html>"""
  }
  it should """translate [$DAYS$] to {{days}}""" in {
    val body = """<html><body>[$DAYS$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateExpiration, body), Map("days" -> "14"))
    rendered shouldEqual """<html><body>14</body></html>"""
  }
  it should """translate [$EXPIRATION_DATE$] to {{date}}""" in {
    val now = new DateTime().toString("EEEE, MMMM d")
    val body = """<html><body>[$EXPIRATION_DATE$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateExpiration, body), Map("date" -> now))
    rendered shouldEqual s"""<html><body>$now</body></html>"""
  }

  import CourseMessageService._
  val now = new DateTime

  it should "translate [$REPEAT_COURSE$]...[$/REPEAT_COURSE$]" in {
    val data = Seq(
      DeadlineRenderView(
        "cert1",
        Seq(),
        Seq(Deadline("course1", 7, new DateTime().plusDays(7))),
        Seq()
      )
    )
    val body = """<html><body>[$REPEAT$][$REPEAT_COURSE$]Course [$TITLE$] has deadline in [$DAYS$] days, at [$DATE$][$/REPEAT_COURSE$][$/REPEAT$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateDeadline, body), Map("data" -> data))
    rendered shouldEqual s"""<html><body><table><tr><td valign="top">Course course1 has deadline in 7 days, at ${now.plusDays(7).toString("EEEE, MMMM d")}</td></tr></table></body></html>"""
  }
  it should "translate [$REPEAT_STATEMENT$]...[$/REPEAT_STATEMENT$]" in {
    val data = Seq(
      DeadlineRenderView(
        "cert1",
        Seq(),
        Seq(),
        Seq(Deadline("st1", 14, new DateTime().plusDays(14)))
      )
    )
    val body = """<html><body>[$REPEAT$][$REPEAT_STATEMENT$]Statement [$TITLE$] has deadline in [$DAYS$] days, at [$DATE$][$/REPEAT_STATEMENT$][$/REPEAT$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateDeadline, body), Map("data" -> data))
    rendered shouldEqual s"""<html><body><table><tr><td valign="top">Statement st1 has deadline in 14 days, at ${now.plusDays(14).toString("EEEE, MMMM d")}</td></tr></table></body></html>"""
  }
  it should "translate [$REPEAT_ACTIVITY$]...[$/REPEAT_ACTIVITY$]" in {
    val data = Seq(
      DeadlineRenderView(
        "cert1",
        Seq(Deadline("activity1", 0, new DateTime())),
        Seq(),
        Seq()
      )
    )
    val body = """<html><body>[$REPEAT$][$REPEAT_ACTIVITY$]Activity [$TITLE$] has deadline in [$DAYS$] days, at [$DATE$][$/REPEAT_ACTIVITY$][$/REPEAT$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateDeadline, body), Map("data" -> data))
    rendered shouldEqual s"""<html><body><table><tr><td valign="top">Activity activity1 has deadline in 0 days, at ${now.toString("EEEE, MMMM d")}</td></tr></table></body></html>"""
  }
  it should "translate [$CERTIFICATE_NAME$] to {{title}} for deadline case" in {
    val data = Seq(
      DeadlineRenderView(
        "cert1",
        Seq(),
        Seq(),
        Seq()
      )
    )
    val body = """<html><body>[$REPEAT$][$CERTIFICATE_NAME$][$/REPEAT$]</body></html>"""
    val rendered = LiferayTemplateLoader.render(MessageTemplate("", MessageType.CourseCertificateDeadline, body), Map("data" -> data))
    rendered shouldEqual "<html><body>cert1</body></html>"
  }

}
