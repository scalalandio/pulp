package io.scalaland.pulp.internals

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

private[pulp] class ImplementedAsImpl(val c: Context)(annottees: Seq[Any]) extends Loggers {

  import c.universe._

  private def buildProviderMethod(classDef: ClassDef): DefDef = classDef match {
    case q"""$_ class $name[..${params: Seq[TypeDef]}] $_(..${ctorParams: Seq[ValDef]})
                  extends { ..$_ }
                  with ..$_ { $_ => ..$_ }""" =>
      val implClass = c.prefix.tree match { case q"new $_[$implClass]" => implClass }
      val providerArg = q"provider: _root_.io.scalaland.pulp.Provider[$implClass]"

      withTraceLog("Provider implicit expanded") {
        q"""implicit def implicitProvider[..$params](implicit $providerArg)
                : _root_.io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
              _root_.io.scalaland.pulp.Provider.upcast[$implClass, $name[..${params.map(_.name)}]]""": DefDef
      }

    case q"""$_ trait $name[..${params: Seq[TypeDef]}]
                  extends { ..$_ }
                  with ..$_ { $_ => ..$_ }""" =>
      val implClass = c.prefix.tree match { case q"new $_[$implClass]" => implClass }
      val providerArg = q"provider: _root_.io.scalaland.pulp.Provider[$implClass]"

      withTraceLog("Provider implicit expanded") {
        q"""implicit def implicitProvider[..$params](implicit $providerArg)
                : _root_.io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
              _root_.io.scalaland.pulp.Provider.upcast[$implClass, $name[..${params.map(_.name)}]]""": DefDef
      }
  }

  private def extendCompanion(objectDef: ModuleDef, classDef: ClassDef): ModuleDef = objectDef match {
    case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" =>
      q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
            ..$body
            ${buildProviderMethod(classDef)}
          }""": ModuleDef
  }

  private def createCompanion(classDef: ClassDef): ModuleDef = {
    q"""object ${TermName(classDef.name.toString)} {
          ${buildProviderMethod(classDef)}
        }""": ModuleDef
  }

  def wire(): c.Expr[Any] = withDebugLog("Provider injection result") {
    annottees.toList match {
      case Expr(classDef: ClassDef) :: Expr(objectDef: ModuleDef) :: Nil =>
        c.Expr(q"""$classDef
                   ${extendCompanion(objectDef, classDef)}""")
      case Expr(objectDef: ModuleDef) :: Expr(classDef: ClassDef) :: Nil =>
        c.Expr(q"""${extendCompanion(objectDef, classDef)}
                   $classDef""")
      case Expr(classDef: ClassDef) :: Nil =>
        c.Expr(q"""$classDef
                   ${createCompanion(classDef)}""")
      case got => c.abort(c.enclosingPosition, s"@ImplementedAs can only annotate class, got: $got")
    }
  }
}

private[pulp] object ImplementedAsImpl {

  def impl(c: Context)(annottees: Seq[c.Expr[Any]]): c.Expr[Any] =
    new ImplementedAsImpl(c)(annottees).wire().asInstanceOf[c.Expr[Any]]
}
