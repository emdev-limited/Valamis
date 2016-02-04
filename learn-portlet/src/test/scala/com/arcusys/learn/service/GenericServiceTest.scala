package com.arcusys.learn.service

import com.arcusys.learn.controllers.api.content.CategoryController
import com.arcusys.learn.scorm.rte.service._
import com.arcusys.learn.scorm.manifest.service._
import org.junit._
import com.arcusys.learn.controllers.api.{ GradebookApiController, FileApiController, AdminApiController }
import com.arcusys.learn.facades.GradebookFacade

class GenericServiceTest {
  @Test
  def allServicesHaveNoArgsConstructor() {
    new AdminApiController
    new FileApiController
    new GradebookApiController
    new CategoryController

    new ActivitiesService
    new OrganizationsService

    new RunTimeEnvironment
    new SequencingService
  }
}