package com.arcusys.valamis.web.servlet.slides.request

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.slide.model.{SlideElementProperty, SlideElementsProperties, SlideProperties, SlideProperty}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.request.{BaseRequest, Parameter}
import org.scalatra.ScalatraServlet

object SlideRequest extends BaseRequest{
  val Id = "id"
  val Page = "page"
  val ItemsOnPage = "itemsOnPage"
  val TitleFilter = "titleFilter"
  val SortTitleAsc = "sortTitleAsc"
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
  val Properties = "properties"
  val PlayerTitle = "playerTitle"
  val TopDownNavigation = "topDownNavigation"
  val ActivityId = "activityId"
  val Status = "status"
  val NewVersion = "newVersion"
  val Version = "version"
  val OneAnswerAttempt = "oneAnswerAttempt"
  val Tags = "tags"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(controller: ScalatraServlet) {
    implicit val _controller = controller

    def action = Parameter(Action).required
    def id = Parameter(Id).longOption
    def idRequired = Parameter(Id).longRequired
    def page = Parameter(Page).intRequired
    def itemsOnPage = Parameter(ItemsOnPage).intRequired
    def titleFilter = Parameter(TitleFilter).option
    def sortTitleAsc = Parameter(SortTitleAsc).booleanRequired
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
    def statementCategoryId = Parameter(StatementCategoryId).option
    def font = Parameter(Font).option
    def questionFont = Parameter(QuestionFont).option
    def answerFont = Parameter(AnswerFont).option
    def answerBg = Parameter(AnswerBg).option
    def isTemplate = Parameter(IsTemplate).booleanOption
    def fromTemplate = Parameter(FromTemplate).booleanOption
    def cloneElements = Parameter(CloneElements).booleanOption
    def isSelectedContinuity = Parameter(IsSelectedContinuity).booleanOption
    def isLessonSummary = Parameter(IsLessonSummary).booleanOption
    def userId = PermissionUtil.getUserId
    def isDefault = Parameter(IsDefault).booleanOption.getOrElse(false)
    def isMyThemes = Parameter(IsMyThemes).booleanOption.getOrElse(false)
    def themeId = Parameter(ThemeId).longOption
    def scoreLimit = Parameter(ScoreLimit).doubleOption
    def properties = deserializeProperties(Parameter(Properties).required)
    def playerTitleOption = Parameter(PlayerTitle).option("lessonSetting")
    def playerTitle = Parameter(PlayerTitle).required
    def slideProperties = deserializeSlideProperties(Parameter(Properties).required)
    def topDownNavigation = Parameter(TopDownNavigation).booleanOption.getOrElse(false)
    def activityId = Parameter(ActivityId).required
    def status = Parameter(Status).required
    def newVersion = Parameter(NewVersion).booleanOption
    def version = Parameter(Version).doubleOption.getOrElse(1.0)
    def tags = Parameter(Tags).multiWithEmpty.filter(!_.isEmpty)
    def skipTake = SkipTake((page - 1) * itemsOnPage, itemsOnPage)
    def oneAnswerAttempt = Parameter(OneAnswerAttempt).booleanOption.getOrElse(false)

    def deserializeProperties(models: String): Seq[SlideElementsProperties] = {
      JsonHelper.fromJson[Map[Long, Map[String,String]]](models)
        .map( property =>
          SlideElementsProperties(
            property._1,
            property._2.map( elementProperty =>
              SlideElementProperty(
                elementProperty._1,
                elementProperty._2)
            ).toSeq
          )
        ).toSeq
    }

    def deserializeSlideProperties(models: String): Seq[SlideProperties] = {
      JsonHelper.fromJson[Map[Long, Map[String,String]]](models)
        .map( property =>
          SlideProperties(
            property._1,
            property._2.map( slideSetProperty =>
              SlideProperty(
                slideSetProperty._1,
                slideSetProperty._2)
            ).toSeq
          )
        ).toSeq
    }
  }
}
