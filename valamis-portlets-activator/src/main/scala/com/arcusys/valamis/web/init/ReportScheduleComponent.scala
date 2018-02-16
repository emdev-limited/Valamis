package com.arcusys.valamis.web.init

import com.arcusys.valamis.log.LogSupport
import com.arcusys.valamis.reports.service.{ReportService}
import com.arcusys.valamis.web.configuration.InjectableSupport
import com.liferay.portal.kernel.messaging.{BaseMessageListener, BaseSchedulerEntryMessageListener, DestinationNames, Message}
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle
import com.liferay.portal.kernel.scheduler.{SchedulerEngineHelper, TimeUnit, TriggerFactory, TriggerFactoryUtil}
import org.osgi.service.component.annotations._

@Component(immediate = true, service = Array(classOf[BaseMessageListener]))
class ReportSchedulerComponent
  extends BaseSchedulerEntryMessageListener
    with LogSupport
    with InjectableSupport {

  private var _schedulerEngineHelper: SchedulerEngineHelper = _ //to be injected by Service Component Runtime

  lazy val reportService = inject[ReportService]
  val ONE_DAY = 3600 * 24

  override def doReceive(message: Message): Unit = {
    try {
      reportService.cleanReportDir(ONE_DAY)
    } catch {
      case e: Throwable => log.error(e.getMessage, e)
    }
  }

  @Activate
  @Modified
  protected def activate(properties: java.util.Map[String, Object]) {
    val timeUnit = TimeUnit.DAY

    schedulerEntryImpl.setTrigger(
      TriggerFactoryUtil.createTrigger(getEventListenerClass, getEventListenerClass, 1, timeUnit))

    _schedulerEngineHelper.register(
      this, schedulerEntryImpl, DestinationNames.SCHEDULER_DISPATCH)
  }

  @Deactivate
  protected def deactivate() {
    _schedulerEngineHelper.unregister(this)
  }

  @Reference(unbind = "-")
  protected def setSchedulerEngineHelper(schedulerEngineHelper: SchedulerEngineHelper) {
    _schedulerEngineHelper = schedulerEngineHelper
  }

  //These methods are needed to make sure,
  //that needed Liferay services have been initialized before activating our component
  @Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED, unbind = "-")
  protected def setModuleServiceLifecycle(moduleServiceLifecycle: ModuleServiceLifecycle) {}

  @Reference(unbind = "-")
  protected def setTriggerFactory(triggerFactory: TriggerFactory) {}
}
