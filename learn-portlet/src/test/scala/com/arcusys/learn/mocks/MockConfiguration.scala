package com.arcusys.learn.test.mocks

import com.arcusys.learn.facades.{ CertificateFacadeContract, FileFacadeContract }
import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.mocks.Mocks
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import com.arcusys.valamis.lesson.scorm.service.sequencing._
import com.arcusys.valamis.settings.storage.SettingStorage
import com.escalatesoft.subcut.inject.NewBindingModule

object MockConfiguration extends NewBindingModule(implicit module => {
  import module._

  bind[UserLocalServiceHelper] toSingle Mocks.userLocalServiceHelper
  bind[CourseGradeStorage] toSingle Mocks.courseStorage
  bind[SettingStorage] toSingle Mocks.settingStorage
  module.bind[FileStorage] toSingle Mocks.fileStorage
  bind[CertificateFacadeContract].toSingle(Mocks.certificateFacadeContract)
  bind[FileFacadeContract].toSingle(Mocks.fileFacadeContract)

  bind[NavigationRequestServiceContract] toSingle new NavigationRequestService
  bind[TerminationRequestServiceContract] toSingle new TerminationRequestService
  bind[SequencingRequestServiceContract] toSingle new SequencingRequestService
  bind[DeliveryRequestServiceContract] toSingle new DeliveryRequestService
  bind[RollupServiceContract] toSingle new RollupService
  bind[EndAttemptServiceContract] toSingle new EndAttemptService
})