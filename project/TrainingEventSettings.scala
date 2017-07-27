import Settings.LiferayPluginProperties

object TrainingEventPluginProperties extends LiferayPluginProperties {
  val longDescription = "Valamis is a eLearning portlet family based on the SCORM and Tincan standards, including, package manager, player, Quiz editor, gradebook and curriculum portlets. This brings elements of Learning Management Systems (LMS) to the Liferay Portal Platform and expands them with more flexible options for eLearning."
  val pageUrl = "http://valamis.arcusys.com/"
  val tags = "valamis,eLearning,scorm,quiz"
  val author="Arcusys Oy."
  val supportedLiferayVersion = Settings.liferay.supportVersion
  val appVersion = Version.trainingEvents
}

object TrainingEventSettings {
  val commonResourceDirs =
    List("css", "fonts", "html", "i18n", "img", "js", "templates", "valamis", "vendor")
}
