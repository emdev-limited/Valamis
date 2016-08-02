package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.LiferayClasses.LAddress
import com.arcusys.learn.liferay.util.CountryUtilHelper
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.gradebook.service._
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service._
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.StatementResult
import com.arcusys.valamis.tag.TagService
import com.arcusys.valamis.tag.model.ValamisTag
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.user.util.UserExtension
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.base.exceptions.AccessDeniedException
import com.arcusys.valamis.web.servlet.grade.response.{PackageGradeResponse, PieData, StudentResponse}
import com.arcusys.valamis.web.servlet.user.UserFacadeContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import scala.collection.JavaConverters._

@deprecated
class GradebookFacade(implicit val bindingModule: BindingModule)
  extends GradebookFacadeContract
  with Injectable {

  private lazy val userService = inject[UserService]
  protected lazy val gradeService = inject[TeacherLessonGradeService]
  protected lazy val packageChecker = inject[LessonGradeService]
  private lazy val courseGradeService = inject[TeacherCourseGradeService]
  private lazy val courseService = inject[CourseService]
  private lazy val gradeBookService = inject[GradeBookService]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val tagService = inject[TagService[Lesson]]
  private lazy val lrsClient = inject[LrsClientManager]
  private lazy val statementReader = inject[LessonStatementReader]
  private lazy val  lessonResultService = inject[UserLessonResultService]

  private lazy val lessonService = inject[LessonService]

  private def getTotalGrade(courseId: Long, valamisUserId: Long): Float = {
    courseGradeService.get(courseId, valamisUserId).flatMap(_.grade).getOrElse(0)
  }

  private def getTotalComment(courseId: Long, valamisUserId: Long): String = {
    courseGradeService.get(courseId, valamisUserId).flatMap(_.comment).getOrElse("")
  }

  private def getPackageGrades(courseId: Long,
                                        valamisUserId: Long,
                                        packageIds: Option[Seq[Long]],
                                        skip: Int,
                                        count: Int,
                                        sortAsc: Boolean = false,
                                        withStatements: Boolean = true): Seq[PackageGradeResponse] = {
    val user = userService.getById(valamisUserId)

    var packages = lessonService.getAll(courseId)
      .filter(p => packageIds.isEmpty || packageIds.get.contains(p.id))
      .sortBy(p => p.title)

    if (!sortAsc)
      packages = packages.reverse

    if (skip != -1 && count != -1)
      packages = packages.slice(skip, skip + count)

    packages.map(lesson => {
      val lessonResult = lessonResultService.get(lesson, user)
      val gradeAuto = lessonResult.score.map(_.toString).getOrElse("")

      if (withStatements)
        getPackageGradeWithStatements(valamisUserId, lesson.id, Some(gradeAuto))
      else {
        val result = gradeService.get(valamisUserId, lesson.id)

        PackageGradeResponse(
          id = lesson.id,
          packageLogo = lesson.logo.getOrElse(""),
          packageName = lesson.title,
          description = lesson.description,
          finished = result.flatMap(_.grade).isDefined,
          grade = result.flatMap(_.grade).map(_.toString).getOrElse(""),
          gradeAuto = gradeAuto,
          activityId = lessonService.getRootActivityId(lesson.id),
          statements = "",
          comment = result.flatMap(_.comment) getOrElse ""
        )
      }
    })
  }

  def getPackageGradeWithStatements(valamisUserId: Long,
                                    packageId: Long,
                                    gradeAuto: Option[String] = None): PackageGradeResponse = {
    val pack = lessonService.getLessonRequired(packageId)

    val result = gradeService.get(valamisUserId, pack.id)

    val statements = gradeBookService.getStatementGrades(pack.id, valamisUserId, sortAsc = false, shortMode = true)

    PackageGradeResponse(
      id = pack.id,
      packageLogo = pack.logo.getOrElse(""),
      packageName = pack.title,
      description = pack.description,
      finished = result.flatMap(_.grade).isDefined,
      grade = result.flatMap(_.grade).map(_.toString).getOrElse(""),
      gradeAuto = gradeAuto.getOrElse(""),
      activityId = lessonService.getRootActivityId(pack.id),
      statements = JsonHelper.toJson(StatementResult(statements, ""), new StatementSerializer),
      comment = result.flatMap(_.comment) getOrElse ""
    )
  }


  private def getLocation(adr: LAddress): String =
    if (adr.getCity.isEmpty)
      adr.getCountry.getName
    else
      adr.getCity + ", " + CountryUtilHelper.getName(adr.getCountry)


  def getGradesForStudent(studentId: Long,
                          courseId: Long,
                          skip: Int,
                          count: Int,
                          sortAsc: Boolean = false,
                          withStatements: Boolean = true): StudentResponse = {
    val student = userService.getById(studentId)
    if (!userFacade.canView(PermissionUtil.getCourseId, PermissionUtil.getUserId, viewAll = false))
      throw AccessDeniedException()

    StudentResponse(
      id = student.getUserId,
      fullname = student.getFullName,
      avatarUrl = student.getPortraitUrl,
      address = student.getAddresses.asScala.map(adr => getLocation(adr)),
      organizationNames = student.getOrganizations.asScala.map(org => org.getName),
      lastModified = "last modified",
      gradeTotal = getTotalGrade(courseId, student.getUserId.toInt),
      commentTotal = getTotalComment(courseId, student.getUserId.toInt),
      completedPackagesCount = packageChecker.getCompletedLessonsCount(courseId, student.getUserId),
      packagesCount = lessonService.getCount(courseId),
      packageGrades = getPackageGrades(courseId, student.getUserId.toInt, None, skip, count, sortAsc, withStatements))
  }


  //TODO: Move to Dashboard Service (Dashboard module)
  def getPieDataWithCompletedPackages(userId: Long): (Seq[PieData], Int) = {

    val completedPackages = courseService.getByUserId(userId)
      .flatMap { lGroup => lessonService.getAll(lGroup.getGroupId) }
      .filter(lesson => {
        val teacherGrade = gradeService.get(userId, lesson.id).flatMap(_.grade)
        packageChecker.isLessonFinished(teacherGrade, userId, lesson)
      })

    val tags = completedPackages.flatMap(pack => {
      val packTags = tagService.getByItemId(pack.id)

      packTags match {
        case Seq() => Seq(ValamisTag(id = 0, text = ""))
        case seq => seq
      }
    })

    val total = tags.size
    val all = tags.groupBy(t => t.text).map {
      case (name, amounts) => PieData(name, amounts.size * 100 / total)
    } .toSeq

    if(total < 5) {
      (all, completedPackages.size)
    }
    else {
      val ShowedTakeNumber = 4
      val orderedWithoutOther = all.filter(x => !x.label.isEmpty).sortBy(x => x.value).reverse
      val showed = orderedWithoutOther.take(ShowedTakeNumber)
      val toOther = all.filter(p => !showed.contains(p))
      val other = PieData("", toOther.map(p => p.value).sum )

      (showed :+ other, completedPackages.size)
    }
  }

}
