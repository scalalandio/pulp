package io.scalaland.pulp

import io.scalaland.pulp.internals._

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class ImplementedAs[T](implClass: Class[T]) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro ImplementedAs.impl
}

private object ImplementedAs {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = ImplementedAsImpl.impl(c)(annottees)
}
