package com.arcusys.valamis.lesson.scorm.model

/**
  * A SCORM manifest
  * @param version               Version of manifest, if specified
  * @param base                  Common base for all files the manifest references
  * @param defaultOrganizationId ID of default organization. Should be specified if at least one organization is present
  * @param resourcesBase         Common base for resource files, relative to common base
  */
case class ScormManifest(lessonId: Long,
                         version: Option[String],
                         base: Option[String],
                         scormVersion: String,
                         defaultOrganizationId: Option[String],
                         resourcesBase: Option[String])
