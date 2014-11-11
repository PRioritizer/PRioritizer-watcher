package task

import events.Event

trait TaskRunner {
  def run(event: Event): Boolean

  def canRun(event: Event): Boolean = canRunInfo(event)._1

  def canRunInfo(event: Event): (Boolean, String)
}
