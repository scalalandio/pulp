package io.scalaland.pulp

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros

private[pulp] class WiredImpl(val c: Context)(annottees: Any*) {

  import c.universe._

  private def buildProviderMethod(classDef: ClassDef): DefDef = classDef match {
    case q"""$_ class $name[..${params: Seq[TypeDef]}] $_(..${ctorParams: Seq[ValDef]})
                  extends { ..$_ }
                  with ..$_ { $_ => ..$_ }""" =>

      val providerArgs = ctorParams.map(p => q"${p.name}: io.scalaland.pulp.Provider[${p.tpt}]")
      val ctorArgs = ctorParams.map(p => q"${p.name}.get()")

      q"""implicit def implicitProvider[..$params](implicit ..$providerArgs)
              : io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
            new io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] {
              lazy val get(): $name[..${params.map(_.name)}] = new $name[..${params.map(_.name)}](..$ctorArgs)
            }""": DefDef
  }

  private def extendCompanion(objectDef: ModuleDef, classDef: ClassDef): ModuleDef = objectDef match {
    case q"$mods object $tname extends { ..$earlydefns } with ..$parents { $self => ..$body }" =>
      val provider = buildProviderMethod(classDef)
      //println(provider)
      q"""$mods object $tname extends { ..$earlydefns } with ..$parents { $self =>
            $body
            $provider
          }""": ModuleDef
  }

  private def createCompanion(classDef: ClassDef): ModuleDef = {
    val provider = buildProviderMethod(classDef)
    //println(provider)
    q"""object ${TermName(classDef.name.toString)} {
          $provider
        }""": ModuleDef
  }

  private def expanded: c.Expr[Any] = annottees.toList match {
    case List(Expr(classDef: ClassDef) :: Expr(objectDef: ModuleDef) :: Nil) =>
      c.Expr(q"""$classDef
                 ${extendCompanion(objectDef, classDef)}""")
    case List(Expr(objectDef: ModuleDef) :: Expr(classDef: ClassDef) :: Nil) =>
      c.Expr(q"""${extendCompanion(objectDef, classDef)}
                 $classDef""")
    case List(Expr(classDef: ClassDef) :: Nil) =>
      c.Expr(q"""$classDef
                 ${createCompanion(classDef)}""")
    case got => c.abort(c.enclosingPosition, s"Only class can be annotated with @Wired, got: $got")
  }

  def wire(): c.Expr[Any] = {
    val result = expanded
    //println(result)
    expanded
  }
}
