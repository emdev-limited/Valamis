package com.arcusys.valamis.web.init

import com.liferay.portal.kernel.upgrade.UpgradeProcess
import com.arcusys.valamis.updaters.version310._
import com.arcusys.valamis.updaters.version320._
import com.arcusys.valamis.updaters.version330._
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator.Registry
import org.osgi.service.component.annotations.Component

/**
  * Created by pkornilov on 8/26/16.
  */
@Component(immediate = true, service = Array(classOf[com.liferay.portal.upgrade.registry.UpgradeStepRegistrator]))
class ValamisUpgradeStepRegistrator extends UpgradeStepRegistrator {

  private val bundleName = "com.arcusys.valamis.web.bundle"//TODO read it from bundle headers

  override def register(registry: Registry): Unit = {
    //!!!Updaters, added here also have to be added to portal-ext.properties
    implicit val reg = registry

    "3102" -> "3103" by new DBUpdater3103
    "3103" -> "3104" by new DBUpdater3104
    "3104" -> "3105" by new DBUpdater3105
    "3105" -> "3106" by new DBUpdater3106
    "3106" -> "3107" by new DBUpdater3107
    "3107" -> "3201" by new DBUpdater3201
    "3201" -> "3202" by new DBUpdater3202
    "3202" -> "3203" by new DBUpdater3203
    "3203" -> "3205" by new DBUpdater3205
    "3205" -> "3206" by new DBUpdater3206
    "3206" -> "3207" by new DBUpdater3207
    "3207" -> "3208" by new DBUpdater3208
    "3208" -> "3209" by new DBUpdater3209
    "3209" -> "3210" by new DBUpdater3210
    "3210" -> "3301" by new DBUpdater3301
    "3301" -> "3302" by new DBUpdater3302
    "3302" -> "3303" by new DBUpdater3303
    "3303" -> "3311" by new DBUpdater3311
    "3311" -> "3312" by new DBUpdater3312
  }

  implicit class Registrator(versions: (String, String)) {
    def by(updater: UpgradeProcess)(implicit registry: Registry) = {
      val (fromVersion, toVersion) = versions
      registry.register(bundleName, fromVersion, toVersion, updater)
    }

  }

}
