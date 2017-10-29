package io.scalaland.pulp

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros

private[pulp] class WiredImpl(val c: Context)(annottees: Any*)(wiredType: WiredType) {

  import c.universe._

  private def buildProviderMethod(classDef: ClassDef): ValOrDefDef = classDef match {
    case q"""$_ class $name[..${params: Seq[TypeDef]}] $_(..${ctorParams: Seq[ValDef]})
                  extends { ..$_ }
                  with ..$_ { $_ => ..$_ }""" =>

      val providerArgs = ctorParams.map(p => q"${p.name}: io.scalaland.pulp.Provider[${p.tpt}]")
      val ctorArgs = if (wiredType != WiredType.Singleton) ctorParams.map(p => q"${p.name}.get")
      else ctorParams.map(p => q"io.scalaland.pulp.Provider.get[${p.tpt}]")

      wiredType match {
        case WiredType.Default =>
          q"""implicit def implicitProvider[..$params](implicit ..$providerArgs)
                  : io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
                io.scalaland.pulp.Provider.value(new $name[..${params.map(_.name)}](..$ctorArgs))""": DefDef

        case WiredType.Factory =>
          q"""implicit def implicitProvider[..$params](implicit ..$providerArgs)
                  : io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
                io.scalaland.pulp.Provider.factory(new $name[..${params.map(_.name)}](..$ctorArgs))""": DefDef

        case WiredType.Singleton if params.isEmpty =>
          q"""implicit lazy val implicitProvider
                  : io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
                io.scalaland.pulp.Provider.value(new $name[..${params.map(_.name)}](..$ctorArgs))""": ValDef

        case WiredType.Singleton if params.nonEmpty =>
          c.abort(c.enclosingPosition, "@Singleton cannot be used on parametric types")
      }
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
    case got => c.abort(c.enclosingPosition, s"@Wired, @Singleton or @Factory can only annotate class, got: $got")
  }

  def wire(): c.Expr[Any] = {
    val result = expanded
    //println(result)
    expanded
  }
}
