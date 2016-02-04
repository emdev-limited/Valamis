package com.arcusys.valamis.certificate.model.goal

object GoalStatistic {
  def empty = GoalStatistic(0,0,0,0)
}

case class GoalStatistic(success: Int, inProgress: Int, failed: Int,  total: Int, notStarted: Int = 0)
{
  def addSuccess(count: Int = 1) =
    this.copy(success = success + count, total = total + count)
  def addFailed(count: Int = 1) =
    this.copy(failed = failed + count, total = total + count)
  def addInProgress(count: Int = 1) =
    this.copy(inProgress = inProgress + count, total = total + count)

  def add(status: GoalStatuses.Value) = {
    status match {
      case GoalStatuses.Failed => this.addFailed()
      case GoalStatuses.Success => this.addSuccess()
      case GoalStatuses.InProgress => this.addInProgress()
    }
  }

  def +(that: GoalStatistic): GoalStatistic = {
    val success = this.success + that.success
    val inProgress = this.inProgress + that.inProgress
    val failed = this.failed + that.failed
    val notStarted = this.notStarted + that.notStarted
    val total = this.total + that.total
    GoalStatistic(success, inProgress, failed, total, notStarted)
  }
}



