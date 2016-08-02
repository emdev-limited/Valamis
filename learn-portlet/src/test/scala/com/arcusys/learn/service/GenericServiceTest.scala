package com.arcusys.learn.service

import com.arcusys.valamis.web.servlet.admin.AdminServlet
import com.arcusys.valamis.web.servlet.content.CategoryServlet
import com.arcusys.valamis.web.servlet.file.FileServlet
import com.arcusys.valamis.web.servlet.grade.GradebookServlet
import com.arcusys.valamis.web.servlet.scorm.{ActivitiesServlet, OrganizationsServlet, RteServlet, SequencingServlet}
import org.junit._

class GenericServiceTest {
  @Test
  def allServicesHaveNoArgsConstructor() {
    new AdminServlet
    new FileServlet
    new GradebookServlet
    new CategoryServlet

    new ActivitiesServlet
    new OrganizationsServlet

    new RteServlet
    new SequencingServlet
  }
}