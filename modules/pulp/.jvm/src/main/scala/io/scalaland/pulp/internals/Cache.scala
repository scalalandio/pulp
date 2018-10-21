package io.scalaland.pulp.internals

import scala.collection.mutable
import scala.reflect.runtime.universe._

object Cache extends Loggers {

  type Id[A] = WeakTypeTag[A]

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val cache: mutable.Map[String, Any] = mutable.Map.empty[String, Any]

  private[pulp] def query[A](tag: Id[A], thunk: => A): A =
    cache.synchronized {
      val searchKey = tag.tpe.dealias.toString
      cache.get(searchKey) match {
        case Some(value) =>
          withDebugLog(s"Obtained from cache by $tag") {
            value.asInstanceOf[A]
          }
        case None =>
          withDebugLog(s"Cache empty, created for $tag") {
            val value = thunk
            cache.put(searchKey, value)
            value
          }
      }
    }
}
