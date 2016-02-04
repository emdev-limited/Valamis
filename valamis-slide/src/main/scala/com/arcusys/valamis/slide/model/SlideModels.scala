package com.arcusys.valamis.slide.model

case class SlideSetModel(id: Option[Long] = None,
                         title: String = "",
                         description: String = "",
                         courseId: Long,
                         logo: Option[String] = None,
                         slides: List[SlideModel] = List(),
                         isTemplate: Boolean = false,
                         isSelectedContinuity: Boolean = false,
                         themeId: Option[Long] = None,
                         duration: Option[Long] = None,
                         scoreLimit: Option[Double] = None,
                         playerTitle: String = "page",
                         slidesCount: Option[Long] = None)

case class SlideModel(id: Option[Long] = None,
                      title: String = "Page",
                      bgColor: Option[String] = None,
                      bgImage: Option[String] = None,
                      font: Option[String] = None, //3 parameters separated with $ (family, size, color)
                      questionFont: Option[String] = None,
                      answerFont: Option[String] = None,
                      answerBg: Option[String] = None,
                      duration: Option[String] = None,
                      leftSlideId: Option[Long] = None,
                      topSlideId: Option[Long] = None,
                      slideElements: List[SlideElementModel] = List(),
                      slideSetId: Long,
                      statementVerb: Option[String] = None,
                      statementObject: Option[String] = None,
                      statementCategoryId: Option[String] = None,
                      isTemplate: Boolean = false,
                      isLessonSummary: Boolean = false,
                      playerTitle: Option[String] = None,
                      properties: Seq[SlideProperties] = Seq())

object SlideEntityType {
  val Text = "text"
  val Iframe = "iframe"
  val Question = "question"
  val Image = "image"
  val Video = "video"
  val Pdf = "pdf"
  val Math = "math"
  val Webgl = "webgl"
  val PlainText = "plaintext"
  val RandomQuestion = "randomquestion"
  val AvailableTypes = Text :: Iframe :: Question :: Image :: Video :: Pdf :: Math :: Webgl :: PlainText :: RandomQuestion :: Nil
  val AvailableExternalFileTypes = Image :: Pdf :: Webgl :: Nil
}

// We don't construct Slide*Model into hierarchy, because all rendering is done on client-side
// We add extra field slideEntityType, because our serialization erases type information
case class SlideElementModel(id: Option[Long] = None,
                             top: String = "0",
                             left: String = "0",
                             width: String = "800",
                             height: String = "auto",
                             zIndex: String = "1",
                             content: String = "",
                             slideEntityType: String,
                             slideId: Long,
                             correctLinkedSlideId: Option[Long] = None,
                             incorrectLinkedSlideId: Option[Long] = None,
                             notifyCorrectAnswer: Option[Boolean] = None,
                             properties: Seq[SlideElementsProperties] = Seq())

case class SlideElementProperty(key: String, value: String)

case class SlideElementsProperties(deviceId: Long, properties: Seq[SlideElementProperty])

case class SlideProperty(key: String, value: String)

case class SlideProperties(deviceId: Long, properties: Seq[SlideProperty])


case class Device(id: Option[Long] = None,
                  name: String,
                  minWidth: Int,
                  maxWidth: Int,
                  minHeight: Int,
                  margin: Int)

case class SlideThemeModel(id: Option[Long] = None,
                           title: String = "Theme",
                           bgColor: Option[String] = None,
                           bgImage: Option[String] = None, //2 parameters separated with $ (image and its size(mode))
                           font: Option[String] = None, //3 parameters separated with $ (family, size, color)
                           questionFont: Option[String] = None,
                           answerFont: Option[String] = None,
                           answerBg: Option[String] = None,
                           userId: Option[Long] = None,
                           isDefault: Boolean = false)
