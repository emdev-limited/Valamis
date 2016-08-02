package com.arcusys.valamis.lesson.tincan.model

/**
  * A Tincan activity
  * http://tincanapi.com/wp-content/assets/tincan.xsd
  * @param activityId type="xs:anyURI"
  * @param name type="langstring"
  * @param description type="langstring"
  * @param launch type="langURI"
  * @param resource type="langURI"
  */
case class TincanActivity(lessonId: Long,
                          activityId: String,
                          activityType: String,
                          name: String,
                          description: String,
                          launch: Option[String],
                          resource: Option[String],
                          id: Option[Long] = None
                         )
