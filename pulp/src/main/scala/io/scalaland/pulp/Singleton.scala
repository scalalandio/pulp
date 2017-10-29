package io.scalaland.pulp

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Singleton extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro Singleton.impl
}

object Singleton {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] =
    new WiredImpl(c)(annottees)(WiredType.Singleton).wire().asInstanceOf[c.Expr[Any]]
}