package com.arcusys.valamis.social.service

import com.arcusys.learn.liferay.model.Activity
import com.arcusys.learn.liferay.services.SocialActivityLocalServiceHelper
import com.arcusys.valamis.certificate.model.{CertificateStateType, Certificate}
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.lesson.model.{CertificateActivityType, CourseActivityType, PackageActivityType, LessonType}
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lesson.tincan.model.TincanPackage
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.social.model.UserStatus
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.service.{ServiceContextThreadLocal, UserLocalServiceUtil}
import org.joda.time.DateTime

class ActivityServiceImpl(
    implicit val bindingModule: BindingModule)
  extends ActivityService
  with Injectable {
  val supportedActivities = Set(classOf[ScormPackage].getName,
                                classOf[TincanPackage].getName,
                                classOf[Certificate].getName,
                                CertificateStateType.getClass.getName,
                                CertificateActivityType.getClass.getName,
                                classOf[CourseGrade].getName,
                                CourseActivityType.getClass.getName,
                                classOf[UserStatus].getName)

  val packageService = inject[ValamisPackageService]


  def create(companyId: Long, userId: Long, content: String): Activity = {
    SocialActivityLocalServiceHelper.addWithSet(
      companyId,
      userId,
      classOf[UserStatus].getName,
      extraData = Some(content)
    )
  }

  def share(companyId: Long, userId: Long, packageId: Long, comment: Option[String]): Option[Activity] = {
    packageService.getById(packageId).map(_.packageType).map {
      case LessonType.Scorm => SocialActivityLocalServiceHelper.addWithSet(
        companyId,
        userId,
        classOf[ScormPackage].getName,
        `type` = Some(PackageActivityType.Shared.id),
        classPK = Some(packageId),
        extraData = comment)
      case LessonType.Tincan => SocialActivityLocalServiceHelper.addWithSet(
        companyId,
        userId,
        classOf[TincanPackage].getName,
        `type` = Some(PackageActivityType.Shared.id),
        classPK = Some(packageId),
        extraData = comment)
    }
  }

  def getBy(companyId: Long, userId: Option[Long], skipTake: Option[SkipTake], showAll: Boolean): Seq[Activity] = {
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val currentUserId = ServiceContextThreadLocal.getServiceContext.getUserId;
    //todo add activities that should be hidden and add this condition in filter
    val hideActivities = Set(CertificateActivityType.getClass.getName);
      val result = SocialActivityLocalServiceHelper
        .getBy(companyId = companyId)
        .filter{ activity =>
          supportedActivities.contains(activity.className) && (if (userId.isDefined) activity.userId == userId.get else true) &&
            (if (activity.groupId.isDefined) UserLocalServiceUtil.getGroupUsers(activity.groupId.get).contains(UserLocalServiceUtil.getUser(currentUserId)) else true) &&
            (if (showAll) true else (!hideActivities.contains(activity.className)))
        }
        .sorted(Ordering.by((_: Activity).createDate).reverse)

      skipTake match {
        case None => result.take(6)
        case Some(SkipTake(skip, take)) =>
          result.slice(skip, skip + take)
      }
    }

  def getById(activityId: Long): Activity = {
    SocialActivityLocalServiceHelper.getById(activityId)
  }

  def delete(activityId: Long): Unit ={
    SocialActivityLocalServiceHelper.deleteActivity(activityId)
  }
}
