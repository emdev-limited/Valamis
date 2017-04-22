package com.arcusys.valamis.lrs.service.util

object TinCanActivityType extends Enumeration {
  type TinCanActivityType = Value
  val assessment, course, file, interaction, lesson, link, media, meeting, module, objective, performance, question, simulation = Value
  val cmiInteraction = Value("cmi.interaction")

  def getURI(activityType: TinCanActivityType) = {
    "http://adlnet.gov/expapi/activities/" + activityType.toString
  }

}
