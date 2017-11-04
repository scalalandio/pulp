package io.scalaland.pulp

import io.scalaland.pulp.internals._

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Factory extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Factory.impl
}

private object Factory {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = WiredImpl.impl(WiredImpl.Type.Factory)(c)(annottees)
}
