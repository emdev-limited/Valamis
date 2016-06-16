package com.arcusys.learn.facades

import java.net.URI

import com.arcusys.learn.exceptions.AccessDeniedException
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.CountryUtilHelper
import com.arcusys.learn.models.Gradebook._
import com.arcusys.learn.models.response.users.UserResponseUtils
import com.arcusys.learn.models.response.{CollectionResponse, PieData}
import com.arcusys.learn.utils.LiferayGroupExtensions._
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.grade.service.{CourseGradeService, PackageGradeService}
import com.arcusys.valamis.gradebook.model.GradebookUserSortBy.GradebookUserSortBy
import com.arcusys.valamis.gradebook.model._
import com.arcusys.valamis.gradebook.service.GradeBookService
import com.arcusys.valamis.lesson._
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service.{LessonStatementReader, TagServiceContract, ValamisPackageService}
import com.arcusys.valamis.lrs.serializer.{AgentSerializer, StatementSerializer}
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.{Statement, StatementResult}
import com.arcusys.valamis.lrs.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.Joda._
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class GradebookFacade(implicit val bindingModule: BindingModule)
  extends GradebookFacadeContract
  with Injectable
  with GradedPackageConverter{

  private lazy val userService = inject[UserService]
  private lazy val packageService = inject[ValamisPackageService]
  protected lazy val gradeService = inject[PackageGradeService]
  protected lazy val packageChecker = inject[PackageChecker]
  private lazy val courseGradeService = inject[CourseGradeService]
  private lazy val courseService = inject[CourseService]
  private lazy val gradeBookService = inject[GradeBookService]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val tagService = inject[TagServiceContract]
  private lazy val lrsClient = inject[LrsClientManager]
  private lazy val statementReader = inject[LessonStatementReader]

  private[facades] def getTotalGrade(courseId: Long, valamisUserId: Long): Float = {
    courseGradeService.get(courseId, valamisUserId).flatMap(_.grade).getOrElse(0)
  }

  private def getTotalComment(courseId: Long, valamisUserId: Long): String = {
    courseGradeService.get(courseId, valamisUserId).map(_.comment).getOrElse("")
  }

  private[facades] def getPackageGrades(courseId: Long,
                                        valamisUserId: Long,
                                        packageIds: Option[Seq[Long]],
                                        skip: Int,
                                        count: Int,
                                        sortAsc: Boolean = false,
                                        withStatements: Boolean = true): Seq[PackageGradeResponse] = {
    val agent = UserLocalServiceHelper().getUser(valamisUserId).getAgentByUuid

    val autoGradePackage = lrsClient.scaleApi{ api =>
      api.getMaxActivityScale(
        JsonHelper.toJson(agent, new AgentSerializer),
        new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))
      )
    } get


    val autoGradePackagePassed = lrsClient.scaleApi{ api =>
      api.getMaxActivityScale(
        JsonHelper.toJson(agent, new AgentSerializer),
        new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Passed))
      )
    } get
    
    //val autoGradePackage = Seq();
    //val autoGradePackagePassed = Seq();

    var packages = packageService
      .getPackagesByCourse(courseId)
      .filter(p => packageIds.isEmpty || packageIds.get.contains(p.id))
      .sortBy(_.title)

    if (!sortAsc)
      packages = packages.reverse

    if (skip != -1 && count != -1)
      packages = packages.slice(skip, skip + count)

    packages.map(pack => {
      val gradeCompleted = packageChecker.getPackageAutoGrade(pack, valamisUserId, autoGradePackage)
      val gradeAuto = gradeCompleted match {
        case Some(x) => x.toString
        case _ => packageChecker.getPackageAutoGrade(pack, valamisUserId, autoGradePackagePassed).getOrElse("").toString
      }

        if (withStatements)
        getPackageGradeWithStatements(valamisUserId, pack.id, Some(gradeAuto))
      else {
        val result = gradeService.getPackageGrade(valamisUserId, pack.id)

        PackageGradeResponse(
          id = pack.id,
          packageLogo = pack.logo.getOrElse(""),
          packageName = pack.title,
          description = pack.summary.getOrElse(""),
          finished = result.flatMap(_.grade).isDefined,
          grade = result.flatMap(_.grade).map(_.toString).getOrElse(""),
          gradeAuto = gradeAuto,
          activityId = packageService.getRootActivityId(pack.id),
          statements = "",
          comment = if (result.isDefined) Try(result.get.comment).get else ""
        )
      }
    })
  }

  def getPackageGradeWithStatements(valamisUserId: Long,
                                    packageId: Long,
                                    gradeAuto: Option[String] = None): PackageGradeResponse = {
    val pack = packageService
      .getPackage(packageId)

    val result = gradeService.getPackageGrade(valamisUserId, pack.id)

    val statements = gradeBookService.getStatementGrades(pack.id, valamisUserId, sortAsc = false, shortMode = true)

    PackageGradeResponse(
      id = pack.id,
      packageLogo = pack.logo.getOrElse(""),
      packageName = pack.title,
      description = pack.summary.getOrElse(""),
      finished = result.flatMap(_.grade).isDefined,
      grade = result.flatMap(_.grade).map(_.toString).getOrElse(""),
      gradeAuto = gradeAuto.getOrElse(""),
      activityId = packageService.getRootActivityId(pack.id),
      statements = JsonHelper.toJson(StatementResult(statements, ""), new StatementSerializer),
      comment = if (result.isDefined) Try(result.get.comment).get else ""
    )
  }


  private def getLocation(adr: com.liferay.portal.model.Address): String =
    if (adr.getCity.isEmpty)
      adr.getCountry.getName
    else
      adr.getCity + ", " + CountryUtilHelper.getName(adr.getCountry)

  def getLastModified(courseId: Long, userId: Long): String = {

    val result = packageService
      .getPackagesByCourse(courseId)
      .flatMap(p => statementReader.getLast(userId, p))
      .distinct
      .sortBy(s => s.stored)
      .lastOption
      .map(s => s.timestamp)

    if (result.isDefined)
      new DateTime(result.get).toString
    else
      ""

  }

  def getStudents(courseId: Long,
                  skip: Int,
                  count: Int,
                  nameFilter: String,
                  orgNameFilter: String,
                  sortBy: GradebookUserSortBy,
                  sortAZ: Boolean,
                  detailed: Boolean = false,
                  packageIds: Seq[Long] = Seq()): Seq[StudentResponse] = {
    val lastModifiedCache = mutable.HashMap[Long, String]()

    def getOrganizationNames(user: LUser): String = user.getOrganizations.asScala.map(_.getName).mkString(", ")
    def getLastModifiedField(user: LUser): DateTime = {
      val date = getLastModified(courseId, user.getUserId)
      lastModifiedCache.put(user.getUserId, date)
      if (date.nonEmpty) DateTime.parse(date) else new DateTime(0)
    }
    val sorting = sortBy match {
      case GradebookUserSortBy.org => Ordering.by[LUser, String](getOrganizationNames)
      case GradebookUserSortBy.last_modified => Ordering.by[LUser, DateTime](getLastModifiedField)
      case _ => Ordering.by[LUser, String](_.getFullName)
    }

    var students = userService
      .all(courseId, nameFilter, sortAZ = true)
      .filter(user =>
      if (orgNameFilter != "")
        user.getOrganizations.asScala.exists(org => org.getName.toLowerCase.contains(orgNameFilter.toLowerCase))
      else true)
      .filter(user => userFacade.canView(courseId, user, viewAll = false))
      .sorted(sorting)

    if (!sortAZ)
      students = students.reverse

    if (skip != -1 && count != -1)
      students.slice(skip, skip + count)
      
    students
      .map(student => StudentResponse(
      id = student.getUserId,
      fullname = student.getFullName,
      avatarUrl = UserResponseUtils.getPortraitUrl(student),
      address = student.getAddresses.asScala.map(adr => getLocation(adr)),
      organizationNames = student.getOrganizations.asScala.map(org => org.getName),
      lastModified = lastModifiedCache.getOrElse(student.getUserId, ""), //getLastModified(courseId, student.getUserId),
      gradeTotal = getTotalGrade(courseId, student.getUserId.toInt),
      commentTotal = getTotalComment(courseId, student.getUserId.toInt),
      completedPackagesCount = if (detailed) 0 else packageChecker.getCompletedPackagesCount(courseId, student.getUserId.toInt),
      packagesCount = packageService.getPackagesCount(courseId),
      packageGrades = if (detailed) getPackageGrades(courseId, student.getUserId.toInt, Option(packageIds), -1, -1, sortAsc = true, withStatements = false) else null)
      )

  }

  def getGradesForStudent(studentId: Int,
                          courseId: Int,
                          skip: Int,
                          count: Int,
                          sortAsc: Boolean = false,
                          withStatements: Boolean = true): StudentResponse = {
    val student = userService.getById(studentId)
    if (!userFacade.canView(getCourseId, getUserId, viewAll = false))
      throw AccessDeniedException()

    StudentResponse(
      id = student.getUserId,
      fullname = student.getFullName,
      avatarUrl = UserResponseUtils.getPortraitUrl(student),
      address = student.getAddresses.asScala.map(adr => getLocation(adr)),
      organizationNames = student.getOrganizations.asScala.map(org => org.getName),
      lastModified = "last modified",
      gradeTotal = getTotalGrade(courseId, student.getUserId.toInt),
      commentTotal = getTotalComment(courseId, student.getUserId.toInt),
      completedPackagesCount = packageChecker.getCompletedPackagesCount(courseId, student.getUserId.toInt),
      packagesCount = packageService.getPackagesCount(courseId),
      packageGrades = getPackageGrades(courseId, student.getUserId.toInt, None, skip, count, sortAsc, withStatements))
  }

  protected def lastCompleted(pack: BaseManifest, userId: Long): Option[Statement] = {
    val statements = gradeBookService.getStatementGrades(pack.id, userId.toInt, true)
    statements
      .filter(st => TincanHelper.isVerbType(st.verb, TinCanVerbs.Completed))
      .lastOption
  }

  def getLastPackages(userId: Long, count: Int): Seq[RecentLesson] = {
    var recentLesson = Seq[RecentLesson]()
    courseService.getByUserId(userId).foreach(lGroup => {
      packageService
        .getByCourse(lGroup.getGroupId).foreach(pack => {
        if (pack.visibility.contains(true)) {
          for (statement <- statementReader.getLastAttempted(userId, pack))
            recentLesson = recentLesson :+ RecentLesson(
              pack.id,
              pack.title,
              statement.timestamp.toString,
              lGroup.getDescriptiveName,
              lGroup.getCourseFriendlyUrl
            )
        }
      })
    })
    recentLesson.sortBy(_.throughDate).reverse.take(count)
  }

  def getBy(userId: Long, isCompleted: Boolean, skipTake: Option[SkipTake]): CollectionResponse[GradedPackageResponse] = {
    lazy val agent = UserLocalServiceHelper().getUser(userId).getAgentByUuid
    
    
    lazy val autoGradePackage = lrsClient.scaleApi{ api =>
      api.getMaxActivityScale(
        JsonHelper.toJson(agent, new AgentSerializer),
        new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))
      )
    }.get

    lazy val autoGradePackagePassed = lrsClient.scaleApi{ api =>
      api.getMaxActivityScale(
        JsonHelper.toJson(agent, new AgentSerializer),
        new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Passed))
      )
    }.get
    
//    val autoGradePackage = Seq();
//    val autoGradePackagePassed = Seq();
    
    val gradedPackageResponses = courseService.getByUserId(userId).flatMap { lGroup =>
      val unfinishedPackages =
        packageService
          .getByCourse(lGroup.getGroupId)
          .filter { pack =>
            val isVisible = pack.visibility.getOrElse(false)
            if (isVisible) {
              lazy val isAutograded = packageChecker.isPackageComplete(pack, userId, autoGradePackage) match{
                case true => true
                case _ => packageChecker.isPackageComplete(pack, userId, autoGradePackagePassed)
              }
              lazy val isGradedByTeacher = gradeService.getPackageGrade(userId, pack.id).flatMap(_.grade).exists(_ > LessonSuccessLimit)

              if (isCompleted) isGradedByTeacher || isAutograded
              else !isGradedByTeacher && !isAutograded
            }
            else {
              false
            }
          }

      unfinishedPackages.map(toResponse(lGroup, userId)(_, autoGradePackage ++ autoGradePackagePassed)).sortBy(_.sortGrade).reverse
    }

    val total = gradedPackageResponses.length
    val records = skipTake match {
      case None => gradedPackageResponses
      case Some(SkipTake(skip, take)) =>
        gradedPackageResponses.slice(skip, skip + take)
    }

    CollectionResponse(
      0,
      records,
      total
    )
  }


  def getPieDataWithCompletedPackages(userId: Long): (Seq[PieData], Int) = {
    val agent = UserLocalServiceHelper().getUser(userId).getAgentByUuid
    
    val autoGradePackage = lrsClient.scaleApi{ api =>
      api.getMaxActivityScale(
        JsonHelper.toJson(agent, new AgentSerializer),
        new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))
      )
    } get
    
    //val autoGradePackage = Seq();

    val completedPackages = courseService.getByUserId(userId)
      .flatMap { lGroup => packageService.getPackagesByCourse(lGroup.getGroupId) }
      .filter { lesson => packageChecker.isPackageComplete(lesson, userId, autoGradePackage) }

    val tags = completedPackages.flatMap(pack => {
      val packTags = tagService.getEntryTags(pack)

      packTags match {
        case Seq() => Seq(ValamisTag(id = 0, text = ""))
        case seq => seq
      }
    })

    val total = tags.size
    val all = tags.groupBy(t => t.text).map {
      case (name, amounts) => PieData(name, amounts.size * 100 / total)
    } .toSeq

    if(total < 5) (all, autoGradePackage.length)
    else {
      val orderedWithoutOther = all.filter(x => !x.label.isEmpty).sortBy(x => x.value).reverse
      val showed = orderedWithoutOther.take(4)
      val toOther = all.filter(p => !showed.contains(p))
      val other = PieData("", toOther.map(p => p.value).sum )

      (showed :+ other, autoGradePackage.length)
    }
  }


  def getTotalGradeForStudent(studentId: Int,
                              courseId: Int): TotalGradeResponse = TotalGradeResponse(
    getTotalGrade(courseId, studentId),
    getTotalComment(courseId, studentId), None, None)

  override def getStudentsCount(courseId: Int,
                                nameFilter: String,
                                orgNameFilter: String): Int = {

    val users =
      UserLocalServiceHelper() //.getUsers(0, -1)
        .getGroupUsers(courseId)
        .asScala
        .filter(u => u.isActive && u.getFullName != "")
        .filter(user => userFacade.canView(courseId, user, viewAll = false))

    val nameFiltered = if (nameFilter != "")
      users.filter(u => u.getFullName.contains(nameFilter))
    else
      users

    val orgFiltered = if (orgNameFilter != "")
      nameFiltered.filter(u => u.getOrganizations.asScala.exists(org => org.getName.toLowerCase.contains(orgNameFilter.toLowerCase)))
    else
      nameFiltered

    orgFiltered.length
  }

}
