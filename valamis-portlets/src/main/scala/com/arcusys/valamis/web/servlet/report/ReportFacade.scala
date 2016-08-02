package com.arcusys.valamis.web.servlet.report

import java.util.{Calendar, Date}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.{CompanyLocalServiceHelper, GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.gradebook.model.CourseGrade
import com.arcusys.valamis.gradebook.service.TeacherCourseGradeService
import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lesson.tincan.service.TincanPackageService
import com.arcusys.valamis.lrs.model.StatementFilter
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.service.util.StatementApiHelpers._
import com.arcusys.valamis.lrs.service.util.TinCanVerbs
import com.arcusys.valamis.lrs.service.util.TincanHelper._
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.user.util.UserExtension
import com.arcusys.valamis.util.Joda._
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.report.response._
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.arcusys.valamis.web.servlet.user.UserFacadeContract
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.{DateTime, Period}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class ReportFacade(implicit val bindingModule: BindingModule) extends ReportFacadeContract with Injectable {

  // TODO: TO Refactor this
  private lazy val uriService = inject[TincanURIService]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val userService = inject[UserLocalServiceHelper]
  private lazy val lrsClient = inject[LrsClientManager]
  private lazy val courseService = inject[CourseService]
  private lazy val courseGradeService = inject[TeacherCourseGradeService]

  private lazy val lesonService = inject[LessonService]
  private lazy val tincanLessonService = inject[TincanPackageService]

  object ReportingPeriods extends Enumeration {
    type ReportingPeriods = Value
    val hour, day, week, month, year, interval = Value
  }

  // overrides
  override def getStudentsLeaderboard(period: String, offset: Int, amount: Int): CollectionResponse[StudentMostActiveResponse] = {
    val ALL_TIME = "all_time"

    val dateSince: Option[Date] =
      try {
        period match {
          case ALL_TIME  => None
          case s: String => Option(getStartOf(new Date(), ReportingPeriods.withName(s.toLowerCase)))
          case _         => throw new BadRequestException // value of period is not valid
        }
      } catch {
        case _: Throwable => throw new BadRequestException // value of period is not valid
      }

    val companies = CompanyLocalServiceHelper.getCompanies

    val courseIds = companies.asScala.map(company => courseService.getByCompanyId(company.getCompanyId).map(_.getGroupId))
      .flatMap(c => c)
      .toList

    val students = courseIds.map(cId => getStudentsInCourse(cId))
      .flatMap(s => s)
      .distinct
      .sortBy(x => x.getFullName)

    val packages = courseIds.map(cID => getPackages(cID)).flatMap(p => p)

    val result = students.map(st => {
      val packagesStatements = getStatements(packages, Option(st.getUserId), dateSince.map(new DateTime(_)))

      StudentMostActiveResponse(st.getUserId,
        st.getFullName,
        st.getPortraitUrl,
        packagesStatements.size)
    }).sortBy(s => s.stmtCount).reverse

    if (amount <= 0) throw new BadRequestException //(400, "Amount should be greater than zero")
    if (offset < 0) throw new BadRequestException //(400, "Offset cannot be less than zero")

    CollectionResponse(math.ceil((offset + 0.1) / amount.toFloat).toInt, result.slice(offset, offset + amount), result.length)
  }

  override def getMostActive(currentUserID: Int, offset: Int, amount: Int): CollectionResponse[StudentMostActiveResponse] = {
    val companies = CompanyLocalServiceHelper.getCompanies
    val userID = currentUserID

    val courseIds = companies.asScala.map(company => courseService.getByCompanyId(company.getCompanyId).map(_.getGroupId)
      .filter(c => userFacade.canView(PermissionUtil.getCourseId, userID, viewAll = false)))
      .flatMap(c => c).toList

    val students = courseIds.map(cId => getStudentsInCourse(cId))
      .flatMap(s => s)
      .distinct
      .sortBy(x => x.getFullName)

    val packages = courseIds.map(cID => getPackages(cID)).flatMap(p => p)

    val result = students.map(st => {
      val packagesStatements = getStatements(packages, Option(st.getUserId))

      StudentMostActiveResponse(st.getUserId,
        st.getFullName,
        st.getPortraitUrl,
        packagesStatements.size)
    }).sortBy(s => s.stmtCount).reverse

    if (amount <= 0) throw new BadRequestException //(400, "Amount should be greater than zero")
    if (offset < 0) throw new BadRequestException //(400, "Offset cannot be less than zero")

    CollectionResponse(math.ceil((offset + 0.1) / amount.toFloat).toInt, result.slice(offset, offset + amount), result.length)
  }

  override def getUserLatestStatements(currentUserID: Int, offset: Int, amount: Int): CollectionResponse[Statement] = {
    val user = try {
      UserLocalServiceHelper().getUser(currentUserID)
    } catch {
      case _: Throwable => throw new LNoSuchUserException("Cannot find user for current session") //(500, "Cannot find user for current session")
    }

    if (amount < 0) throw new BadRequestException("Amount should be greater than zero or zero for all statements")
    if (offset < 0) throw new BadRequestException("Offset cannot be less than zero")

    var statements = Seq[Statement]()
    var total = 0

    // If needed to get ALL statements without pagination
    if (amount == 0) {
      // Show only statements containing these verbs
      val verbs: Array[Map[String, String]] = Array(
        Map("http://adlnet.gov/expapi/verbs/answered" -> "answered"),
        Map("http://adlnet.gov/expapi/verbs/completed" -> "completed"),
        Map("http://adlnet.gov/expapi/verbs/attempted" -> "attempted"))

      lrsClient.statementApi { statementApi =>
        for (verb <- verbs) {
          val filter = StatementFilter(
            agent = Option(user.getAgentByUuid),
            verb = Option(verb.keys.head)
          )

          // Join statements, filtered by each listed verb
          statements = statements ++ statementApi.getByFilter(filter) //.sortBy(st => st.timestamp).reverse
          //      val statements = /*if (amount > 0)*/ statementLRS.getStatements(filter).statements.sortBy(st => st.timestamp).reverse
          //    else statementLRS.getStatements().statements.sortBy(st => st.timestamp).reverse
          total = statements.length
        }
      }
      statements = statements.sortBy(st => st.timestamp).reverse
    } else {
      val filter = StatementFilter(
        agent = Option(user.getAgentByUuid),
        limit  = Option(amount),
        offset = Option(offset)
      )
      statements = lrsClient.statementApi(_.getByFilter(filter))
      total = Try(lrsClient.verbApi(_.getAmount()).getOrElse("0").toInt).getOrElse(0)
    }

    CollectionResponse(math.ceil((offset + 0.1) / amount.toFloat).toInt, statements, total)
  }

  override def getStudentsLatestStatements(currentUserID: Int, offset: Int, amount: Int): CollectionResponse[Statement] = {

    if (amount <= 0) throw new BadRequestException("Amount should be greater than zero")
    if (offset < 0) throw new BadRequestException("Offset cannot be less than zero")
    val verbCompleted = Option(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))

    val filter = StatementFilter(
      limit  = Option(amount),
      offset = Option(offset),
      verb = verbCompleted
    )
    val statements = lrsClient.statementApi(_.getByFilter(filter))
    val total = Try(lrsClient.verbApi(_.getAmount(None, verbCompleted)).getOrElse("0").toInt).getOrElse(0)

    CollectionResponse(math.ceil((offset + 0.1) / amount.toFloat).toInt, statements, total)
  }

  override def getStatementVerbs: VerbResponse = {
    val sinceDate = Option(getSinceDate(10))
    val statements = lrsClient.statementApi(_.getByFilter(StatementFilter(since = sinceDate)))

    val statementVerbs = statements
      .map(_.verb.id.split("/").last) // http://adlnet.gov/expapi/verbs/answered -> answered
      .groupBy(verb => verb)
      .mapValues(_.size)

    VerbResponse(statementVerbs)
  }

  override def getOverallByPeriod(period: String, from: Long, to: Long): OverallByPeriodResponse = {
    try {
      val periodVal = ReportingPeriods.withName(period.toLowerCase)
      val fromDate = getStartOf(new Date(from), periodVal)
      val toDate = getEndOf(new Date(to), periodVal)
      // TODO: rewrite read all statements
      val data = lrsClient.statementApi(_.getByFilter(StatementFilter()))
        .filter(d => d.timestamp.toDate.compareTo(fromDate) >= 0 && d.timestamp.toDate.compareTo(toDate) <= 0)
        .map(s => getStartOf(s.timestamp.toDate, periodVal))
        .sortBy(d => d.getTime)
        .groupBy(date => date)
        .map(k => Map("date" -> k._1.getTime, "amount" -> k._2.size))
      OverallByPeriodResponse(data, periodVal.toString)
    } catch {
      case _: Throwable => throw new BadRequestException
    }
  }

  override def getCourseEvent(group: String, groupPeriod: Option[String], period: String, from: Long, to: Long): Seq[CourseEventResponse] = {
    object ReportingGroup extends Enumeration {
      type ReportingGroup = Value
      val duration, teacher, organization, group = Value
    }

    def isInDateRange(date: Date, fromDate: Date, toDate: Date) =
      date.compareTo(fromDate) < 0 || (date.compareTo(fromDate) >= 0 && date.compareTo(toDate) <= 0)

    def getMapElement(mapList: mutable.HashMap[String, CourseEventResponse], key: String) = {
      var res = mapList.get(key)
      if (res.isEmpty) {
        mapList.put(key, CourseEventResponse(0, 0, key))
        res = mapList.get(key)
      }
      res.get
    }

    val groupBy = try {
      ReportingGroup.withName(group.toLowerCase)
    } catch {
      case _: Throwable => throw new BadRequestException
    }

    val groupByPeriod = if (groupBy == ReportingGroup.duration)
      try {
        ReportingPeriods.withName(groupPeriod.get.toLowerCase)
      } catch {
        case _: Throwable => throw new BadRequestException
      }
    else null

    //    try {
    val periodVal = try {
      ReportingPeriods.withName(period.toLowerCase)
    } catch {
      case _: Throwable => throw new BadRequestException
    }

    val fromDate = try {
      if (periodVal == ReportingPeriods.interval) new Date(from) else getStartOf(new Date(), periodVal)
    } catch {
      case _: Throwable => throw new BadRequestException
    }
    val toDate = try {
      if (periodVal == ReportingPeriods.interval) new Date(to) else getEndOf(new Date(), periodVal)
    } catch {
      case _: Throwable => throw new BadRequestException
    }
    val companies = CompanyLocalServiceHelper.getCompanies
    val courseIds = companies.asScala.map(company => courseService.getByCompanyId(company.getCompanyId).map(_.getGroupId))
      .flatMap(c => c).toList

    val result: mutable.HashMap[String, CourseEventResponse] = mutable.HashMap()

    groupBy match {
      case ReportingGroup.duration =>
        def dateRange(from: DateTime, to: DateTime, step: Period): List[DateTime] =
          Iterator.iterate(from)(_.plus(step)).takeWhile(!_.isAfter(to)).toList

        groupByPeriod match {
          case ReportingPeriods.year =>
            dateRange(
              new DateTime(fromDate),
              new DateTime(toDate),
              Period.years(1))
              .map(d => {
                getMapElement(result, d.toString("YYYY"))
              })
          case ReportingPeriods.month =>
            dateRange(
              new DateTime(fromDate),
              new DateTime(toDate),
              Period.months(1))
              .map(d => {
                getMapElement(result, d.toString("YYYY/MM"))
              })
          case ReportingPeriods.week =>
            dateRange(
              new DateTime(fromDate),
              new DateTime(toDate),
              Period.weeks(1))
              .map(d => {
                getMapElement(result, d.toString("YYYY/ww"))
              })
          case ReportingPeriods.day =>
            dateRange(
              new DateTime(fromDate),
              new DateTime(toDate),
              Period.days(1))
              .map(d => {
                getMapElement(result, d.toString("YYYY/MM/dd"))
              })
        }

        var allStudents: Seq[LUser] = Seq()
        courseIds.foreach(cID => {
          val students = getStudentsInCourse(cID)
          allStudents = allStudents ++ students

          students.foreach(st => {
            val grade = getCourseGrade(cID, st.getUserId.toInt)
            if (grade.isDefined && isInDateRange(grade.get.date.toDate, fromDate, toDate)) {
              groupByPeriod match {
                case ReportingPeriods.year =>
                  dateRange(
                    new DateTime(fromDate),
                    new DateTime(toDate),
                    Period.years(1))
                    .foreach(d => {
                      val res = getMapElement(result, d.toString("YYYY"))
                      if (d.compareTo(grade.get.date) > 0)
                        res.completionsCount += 1
                    })
                case ReportingPeriods.month =>
                  dateRange(
                    new DateTime(fromDate),
                    new DateTime(toDate),
                    Period.months(1))
                    .foreach(d => {
                      val res = getMapElement(result, d.toString("YYYY/MM"))
                      if (d.compareTo(grade.get.date) > 0)
                        res.completionsCount += 1
                    })
                case ReportingPeriods.week =>
                  dateRange(
                    new DateTime(fromDate),
                    new DateTime(toDate),
                    Period.weeks(1))
                    .foreach(d => {
                      val res = getMapElement(result, d.toString("YYYY/ww"))
                      if (d.compareTo(grade.get.date) > 0)
                        res.completionsCount += 1
                    })
                case ReportingPeriods.day =>
                  dateRange(
                    new DateTime(fromDate),
                    new DateTime(toDate),
                    Period.days(1))
                    .foreach(d => {
                      val res = getMapElement(result, d.toString("YYYY/MM/dd"))
                      if (d.compareTo(grade.get.date) > 0)
                        res.completionsCount += 1
                    })
              }
            }
          })
        })
        result.foreach(r => r._2.enrollmentsCount = allStudents.size - r._2.completionsCount)
      case ReportingGroup.teacher =>
        courseIds.foreach(cID => {
          val students = getStudentsInCourse(cID)

          val teachers = getTeachersInCourse(cID)
          teachers.foreach(t => {
            val res = getMapElement(result, t.getFullName)

            students.foreach(st => {
              val grade = getCourseGrade(cID, st.getUserId.toInt)
              if (grade.isDefined && isInDateRange(grade.get.date.toDate, fromDate, toDate)) {
                res.completionsCount += 1
              } else
                res.enrollmentsCount += 1

            })
          })
        })
      case ReportingGroup.organization =>
        courseIds.foreach(cID => {
          val students = getStudentsInCourse(cID)

          students.foreach(st => {
            st.getOrganizations.asScala.foreach(org => {
              val res = getMapElement(result, org.getName)
              val grade = getCourseGrade(cID, st.getUserId.toInt)
              if (grade.isDefined && isInDateRange(grade.get.date.toDate, fromDate, toDate)) {
                res.completionsCount += 1
              } else
                res.enrollmentsCount += 1

            })
          })
        })
      case ReportingGroup.group =>
        courseIds.foreach(cID => {
          val students = getStudentsInCourse(cID)

          students.foreach(st => {
            st.getUserGroups.asScala.foreach(group => {
              val res = getMapElement(result, group.getName)
              val grade = getCourseGrade(cID, st.getUserId.toInt)
              if (grade.isDefined && isInDateRange(grade.get.date.toDate, fromDate, toDate)) {
                res.completionsCount += 1
              } else
                res.enrollmentsCount += 1

            })
          })
        })
    }

    result.values.toSeq.seq.sortBy(s => s.groupName)
  }

  override def getParticipantReport(group: String): Seq[ParticipantResponse] = {
    object ReportingGroup extends Enumeration {
      type ReportingGroup = Value
      val course, teacher, organization, group = Value
    }

    def getMapElement(mapList: mutable.HashMap[String, ParticipantResponse], key: String) = {
      var res = mapList.get(key)
      if (res.isEmpty) {
        mapList.put(key, ParticipantResponse(0, key))
        res = mapList.get(key)
      }
      res.get
    }

    val groupBy = try {
      ReportingGroup.withName(group.toLowerCase)
    } catch {
      case _: Throwable => throw new BadRequestException
    }

    val companies = CompanyLocalServiceHelper.getCompanies
    val courseIds = companies.asScala
      .map(company => courseService.getByCompanyId(company.getCompanyId))
      .flatMap(c => c).toList

    val result: mutable.HashMap[String, ParticipantResponse] = mutable.HashMap()

    groupBy match {
      case ReportingGroup.course =>
        courseIds.foreach(course => {
          val students = getStudentsInCourse(course.getGroupId)

          if (students.nonEmpty) {
            val res = getMapElement(result, course.getDescriptiveName)
            res.amount = students.size
          }
        })
      case ReportingGroup.teacher =>
        courseIds.foreach(course => {
          val students = getStudentsInCourse(course.getGroupId)

          val teachers = getTeachersInCourse(course.getGroupId)
          teachers.foreach(t => {
            val res = getMapElement(result, t.getFullName)
            res.amount += students.size
          })
        })
      case ReportingGroup.organization =>
        courseIds.foreach(course => {
          val students = getStudentsInCourse(course.getGroupId)

          students.foreach(st => {
            st.getOrganizations.asScala.foreach(org => {
              val res = getMapElement(result, org.getName)
              res.amount += 1
            })
          })
        })
      case ReportingGroup.group =>
        courseIds.foreach(course => {
          val students = getStudentsInCourse(course.getGroupId)

          students.foreach(st => {
            st.getUserGroups.asScala.foreach(group => {
              val res = getMapElement(result, group.getName)
              res.amount += 1

            })
          })
        })
    }

    result.values.toSeq.seq.sortBy(s => s.groupName)

  }


  override def getOverallByTime: OverallByTimeResponse = {
    val sinceDate = Option(getSinceDate(10))
    val verbs = lrsClient.verbApi(_.getStatistics(sinceDate)).toOption.toSeq
      .flatMap(k => k.withDatetime)
      .sortBy(_._2)
      .map(d => (d._1, d._2.withTimeAtStartOfDay().getMillis))
      .groupBy(identity)
      .map{case ((verbId, date),(items)) => (verbId, date, items.size)}

    val startedData = verbs
      .filter{ case (verbId, date, amount) => verbId ==  TinCanVerbs.getVerbURI(TinCanVerbs.Attempted) }
      .map{ case (verbId, date, amount) => Map("date" -> date, "amount" -> amount) }

    val completedData  = verbs
      .filter{ case (verbId, date, amount) => verbId ==  TinCanVerbs.getVerbURI(TinCanVerbs.Completed) }
      .map{ case (verbId, date, amount) => Map("date" -> date, "amount" -> amount) }

    val experiencedData = verbs
      .filter{ case (verbId, date, amount) => verbId == TinCanVerbs.getVerbURI(TinCanVerbs.Experienced) }
      .map{ case (verbId, date, amount) => Map("date" -> date, "amount" -> amount) }

    OverallByTimeResponse(startedData, completedData, experiencedData)
  }


  override def getCourseReport(isInstanceScope: Boolean, courseID: Option[Int]): CourseReportResponse = {

    val courseIds = {
      if (isInstanceScope) {
        courseService.getGroupIdsForAllCoursesFromAllCompanies
      } else {
        if (courseID.isEmpty) throw new BadRequestException
        else List(courseID.get.toLong)
      }
    }

    val result =
      if (isInstanceScope)
        CourseReportResponse(0, "Instance", courseIds.size, 0, 0, 0, 0, 0, 0, 0)
      else
        CourseReportResponse(courseIds.head, GroupLocalServiceHelper.getGroup(courseIds.head).getDescriptiveName, 1, 0, 0, 0, 0, 0, 0, 0)

    var allStudents: Seq[Long] = Seq()
    courseIds.foreach(cID => {
      val students = getStudentIdsInCourse(cID)

      allStudents = allStudents ++ students

      val packages = getPackages(cID)

      students.foreach(stId => {

        val started = getStatements(packages, Option(stId)).nonEmpty

        if (started)
          result.studentsStartedCount += 1

        val grade = getCourseGrade(cID, stId).flatMap(_.grade)
        if (grade.isDefined) {
          result.studentsCompletedCount += 1
          if (grade.get > 0)
            result.studentsPassedCount += 1
          else
            result.studentsFailedCount += 1
        } else if (started)
          result.studentsIncompletedCount += 1
        else
          result.studentsUnknownCount += 1
      })
    })

    result.studentsCount = allStudents.size
    result
  }

  private def getStudentsInCourse(courseId: Long): Seq[LUser] = {
    UserLocalServiceHelper().getGroupUsers(courseId)
      .filter(u => u.isActive && u.getFullName != "")
      .filter(user => userFacade.canView(courseId.toInt, user, viewAll = false))
      .sortBy(x => x.getFullName)
      .toSeq
  }

  private def getStudentIdsInCourse(courseId: Long): Seq[Long] = {
    UserLocalServiceHelper().getGroupUsers(courseId).map(_.getUserId)
      .filter(userId => userFacade.canView(courseId.toInt, userId, viewAll = false))
      //.sortBy(x => x.getFullName)
      .toSeq
  }

  private def getTeachersInCourse(courseId: Long): Seq[LUser] = {
    UserLocalServiceHelper()
      .getGroupUsers(courseId)
      .filter(u => u.isActive && u.getFullName != "")
      .filter(user => userFacade.canView(courseId.toInt, user, viewAll = true))
      .sortBy(x => x.getFullName)
      .toSeq
  }

  private def getPackages(courseId: Long) = {
    lesonService.getAll(courseId)
  }

  private def getStatements(packages: Seq[Lesson],
                                     userId: Option[Long] = None, dateSince: Option[DateTime] = None) = {
    packages.flatMap(p => {
      p.lessonType match {
        case LessonType.Scorm =>
          val packageUri = uriService.getById(p.id.toString, TincanURIType.Package)
          val filter = StatementFilter(
            agent = if (userId.isEmpty) None else Option(userService.getUser(userId.get).getAgentByUuid),
            activity = if (packageUri.isDefined) Option(packageUri.get.uri) else Option(p.id.toString),
            relatedActivities = Option(true),
            since = if (dateSince.isDefined) Option(dateSince.get) else None)
          lrsClient.statementApi(_.getByFilter(filter))
        case LessonType.Tincan =>
          val activity = tincanLessonService.getRootActivityId(p.id)

          val email = if (userId.isEmpty) "" else UserLocalServiceHelper().getUser(userId.get).getEmailAddress
          val filter = StatementFilter(
            agent = if (userId.isEmpty) None else Option(userService.getUser(userId.get).getAgentByUuid),
            activity = Option(activity),
            relatedActivities = Option(true),
            since = if (dateSince.isDefined) Option(dateSince.get) else None
          )
          lrsClient.statementApi(_.getByFilter(filter))
      }
    })
      .distinct

  }

  private def getSinceDate(day:Int) : DateTime = DateTime.now.minusDays(day)

  private def getCourseGrade(courseId: Long, valamisUserId: Long): Option[CourseGrade] = {
    courseGradeService.get(courseId, valamisUserId)
  }

  private def getStartOf(date: Date, period: ReportingPeriods.Value) = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    if (period == ReportingPeriods.day) {
      calendar.set(Calendar.HOUR_OF_DAY, 0)
    }
    if (period == ReportingPeriods.week) {
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
      calendar.set(Calendar.HOUR_OF_DAY, 0)
    }
    if (period == ReportingPeriods.month) {
      calendar.set(Calendar.DAY_OF_MONTH, 1)
      calendar.set(Calendar.HOUR_OF_DAY, 0)
    }
    if (period == ReportingPeriods.year) {
      calendar.set(Calendar.MONTH, 0)
      calendar.set(Calendar.DAY_OF_MONTH, 1)
      calendar.set(Calendar.HOUR_OF_DAY, 0)
    }
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.getTime
  }

  private def getEndOf(date: Date, period: ReportingPeriods.Value) = {
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    if (period == ReportingPeriods.day) {
      calendar.set(Calendar.HOUR_OF_DAY, 23)
    }
    if (period == ReportingPeriods.week) {
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
      //calendar.add(Calendar.DATE, 7)
      calendar.set(Calendar.HOUR_OF_DAY, 23)
    }
    if (period == ReportingPeriods.month) {
      calendar.add(Calendar.MONTH, 1)
      calendar.set(Calendar.DAY_OF_MONTH, 1)
      calendar.add(Calendar.DATE, -1)
      calendar.set(Calendar.HOUR_OF_DAY, 23)
    }
    if (period == ReportingPeriods.year) {
      calendar.set(Calendar.MONTH, 0)
      calendar.set(Calendar.DAY_OF_MONTH, 1)
      calendar.add(Calendar.YEAR, 1)
      calendar.add(Calendar.DATE, -1)
      calendar.set(Calendar.HOUR_OF_DAY, 23)
    }
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    calendar.getTime
  }

}
