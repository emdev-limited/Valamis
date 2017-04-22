package com.arcusys.valamis.web.util

import com.arcusys.learn.liferay.services.CompanyHelper

import scala.concurrent.forkjoin.ForkJoinPool

object ForkJoinPoolWithLRCompany extends ForkJoinPool {

  implicit val ExecutionContext = concurrent.ExecutionContext.fromExecutorService(this)

  override def execute(task: Runnable) {
    super.execute(new Runnable {
      // this runs in current thread
      val companyId = CompanyHelper.getCompanyId

      override def run() = {
        // this runs in sub thread
        CompanyHelper.setCompanyId(companyId)

        task.run()
      }
    })
  }
}
