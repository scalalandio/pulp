package io.scalaland.pulp

private[pulp] sealed trait WiredType
private[pulp] object WiredType {
  case object Default extends WiredType
  case object Factory extends WiredType
  case object Singleton extends WiredType
}
