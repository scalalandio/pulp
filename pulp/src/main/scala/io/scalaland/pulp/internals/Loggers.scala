package io.scalaland.pulp.internals

private[pulp] trait Loggers {

  protected def withDebugLog[T](msg: String)(thunk: => T): T = withLog("pulp.debug", "DEBUG")(msg)(thunk)

  protected def withTraceLog[T](msg: String)(thunk: => T): T = withLog("pulp.trace", "TRACE")(msg)(thunk)

  private def withLog[T](property: String, logLevel: String)(msg: String)(thunk: => T): T = {
    val value = thunk
    if (System.getProperty(property) != null) { println(s"[$logLevel]$msg\n$value") }
    value
  }
}
