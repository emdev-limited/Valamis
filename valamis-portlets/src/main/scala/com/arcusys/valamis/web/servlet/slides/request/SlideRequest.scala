package com.arcusys.valamis.web.servlet.slides.request

import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.scalatra.{ScalatraBase, ScalatraServlet}

object SlideRequest extends BaseCollectionFilteredRequest with BaseRequest{
  val Id = "id"
  val TitleFilter = "titleFilter"
  val Title = "title"
  val Description = "description"
  val Logo = "logo"
  val SlideSetId = "slideSetId"
  val SlideId = "slideId"
  val CorrectLinkedSlideId = "correctLinkedSlideId"
  val IncorrectLinkedSlideId = "incorrectLinkedSlideId"
  val NotifyCorrectAnswer = "notifyCorrectAnswer"
  val BgColor = "bgColor"
  val BgImage = "bgImage"
  val Duration = "duration"
  val LeftSlideId = "leftSlideId"
  val TopSlideId = "topSlideId"
  val SlideEntityType = "slideEntityType"
  val Top = "top"
  val Left = "left"
  val Width = "width"
  val Height = "height"
  val ZIndex = "zIndex"
  val Content = "content"
  val GroupId = "groupId"
  val StatementVerb = "statementVerb"
  val StatementObject = "statementObject"
  val StatementCategoryId = "statementCategoryId"
  val Font = "font"
  val QuestionFont = "questionFont"
  val AnswerFont = "answerFont"
  val AnswerBg = "answerBg"
  val IsTemplate = "isTemplate"
  val FromTemplate = "fromTemplate"
  val CloneElements = "cloneElements"
  val IsSelectedContinuity = "isSelectedContinuity"
  val IsLessonSummary = "isLessonSummary"
  val IsDefault = "isDefault"
  val IsMyThemes = "isMyThemes"
  val ThemeId = "themeId"
  val ScoreLimit = "scoreLimit"
  val PlayerTitle = "playerTitle"
  val TopDownNavigation = "topDownNavigation"
  val ActivityId = "activityId"
  val Status = "status"
  val NewVersion = "newVersion"
  val Version = "version"
  val OneAnswerAttempt = "oneAnswerAttempt"
  val Tags = "tags"
  val LockUserId = "lockUserId"
  val RequiredReview = "requiredReview"
  val LockDate = "lockDate"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, SlideSetSortBy.apply) {
    implicit val _controller = scalatra
    def id = Parameter(Id).longRequired
    def titleFilter = Parameter(TitleFilter).option
    def title = Parameter(Title).option.getOrElse("title")
    def description = Parameter(Description).option.getOrElse("description")
    def logo = Parameter(Logo).option
    def slideSetId = Parameter(SlideSetId).longRequired
    def slideSetIdOption = Parameter(SlideSetId).longOption(0)
    def slideId = Parameter(SlideId).longRequired
    def correctLinkedSlideId = Parameter(CorrectLinkedSlideId).longOption
    def incorrectLinkedSlideId = Parameter(IncorrectLinkedSlideId).longOption
    def notifyCorrectAnswer = Parameter(NotifyCorrectAnswer).booleanOption
    def bgColor = Parameter(BgColor).option
    def bgImage = Parameter(BgImage).option
    def duration = Parameter(Duration).option
    def slideSetDuration = Parameter(Duration).longOption
    def leftSlideId = Parameter(LeftSlideId).longOption
    def topSlideId = Parameter(TopSlideId).longOption
    def linkSlideId = Parameter(LeftSlideId).longOption orElse Parameter(TopSlideId).longOption
    def slideEntityType = Parameter(SlideEntityType).required
    def top = Parameter(Top).required
    def left = Parameter(Left).required
    def width = Parameter(Width).required
    def height = Parameter(Height).required
    def zIndex = Parameter(ZIndex).required
    def content = Parameter(Content).required
    def courseId = Parameter(CourseId).longRequired
    def courseIdOption = Parameter(CourseId).longOption
    def statementVerb = Parameter(StatementVerb).option
    def statementObject = Parameter(StatementObject).option
    def statementCategoryId = Parameter(StatementCategoryId).option.filter(_.nonEmpty)
    def font = Parameter(Font).option
    def questionFont = Parameter(QuestionFont).option
    def answerFont = Parameter(AnswerFont).option
    def answerBg = Parameter(AnswerBg).option
    def isTemplate = Parameter(IsTemplate).booleanOption.getOrElse(false)
    def fromTemplate = Parameter(FromTemplate).booleanOption
    def cloneElements = Parameter(CloneElements).booleanOption
    def isSelectedContinuity = Parameter(IsSelectedContinuity).booleanOption.getOrElse(false)
    def isLessonSummary = Parameter(IsLessonSummary).booleanOption.getOrElse(false)
    def userId = PermissionUtil.getUserId
    def isDefault = Parameter(IsDefault).booleanOption.getOrElse(false)
    def isMyThemes = Parameter(IsMyThemes).booleanOption.getOrElse(false)
    def themeId = Parameter(ThemeId).longOption
    def scoreLimit = Parameter(ScoreLimit).doubleOption
    def properties = deserializeProperties(Parameter("properties").required)
    def playerTitleOption = Parameter(PlayerTitle).option("lessonSetting")
    def playerTitle = Parameter(PlayerTitle).required
    def slideProperties = deserializeProperties(Parameter("properties").required)
    def topDownNavigation = Parameter(TopDownNavigation).booleanOption.getOrElse(false)
    def activityId = Parameter(ActivityId).required
    def status = Parameter(Status).required
    def newVersion = Parameter(NewVersion).booleanOption
    def version = Parameter(Version).doubleOption.getOrElse(1.0)
    def tags = Parameter(Tags).multiWithEmpty.filter(!_.isEmpty)
    def oneAnswerAttempt = Parameter(OneAnswerAttempt).booleanOption.getOrElse(false)
    def lockUserId = Parameter(LockUserId).longOption
    def lockDate = Parameter(LockDate).option
    def requiredReview= Parameter(RequiredReview).booleanOption.getOrElse(false)

    def deserializeProperties(models: String): Seq[Properties] = {
      JsonHelper.fromJson[Map[Long, Map[String,String]]](models)
        .map( property =>
          Properties(
            property._1,
            property._2.map( elementProperty =>
              Property(
                elementProperty._1,
                elementProperty._2)
            ).toSeq
          )
        ).toSeq
    }
  }
}
