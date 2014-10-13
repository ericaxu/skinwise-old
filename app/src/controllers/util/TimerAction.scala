package src.controllers.util

import play.api.mvc._
import src.util.Logger

import scala.concurrent.Future

object TimerAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    val start = System.nanoTime()
    val retVal = block(request)
    val end = System.nanoTime()
    val milliseconds = (end - start) / 1000 / 1000
    Logger.info("Timer", "Executed request " + request.uri + " in " + milliseconds + "ms.")
    retVal
  }
}
