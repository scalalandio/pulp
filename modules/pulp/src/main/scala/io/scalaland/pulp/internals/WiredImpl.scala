package io.scalaland.pulp.internals

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

private[pulp] class WiredImpl(wiredType: WiredImpl.Type)(val c: Context)(annottees: Seq[Any]) extends Loggers {

  import c.universe._

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  private def buildProviderMethod(classDef: ClassDef): ValOrDefDef = classDef match {
    case q"""$_ class $name[..${params: Seq[TypeDef]}] $_(...${ctorParams: Seq[Seq[ValDef]]})
                  extends { ..$_ }
                  with ..$_ { $_ => ..$_ }""" =>
      val providerArgs = ctorParams.flatten.map(p => q"${p.name}: _root_.io.scalaland.pulp.Provider[${p.tpt}]")
      val ctorArgsPreFix =
        if (wiredType != WiredImpl.Type.Singleton) ctorParams.map(_.map(p => q"${p.name}.get"))
        else ctorParams.map(_.map(p => q"_root_.io.scalaland.pulp.Provider.get[${p.tpt}]"))
      val startWithImplicit = ctorParams.flatten.headOption.exists { case ValDef(mods, _, _, _) =>
        mods.hasFlag(Flag.IMPLICIT)
      }
      val ctorArgs = if (startWithImplicit) Nil +: ctorArgsPreFix else ctorArgsPreFix

      withTraceLog("Provider implicit expanded") {
        wiredType match {
          case WiredImpl.Type.Default =>
            q"""implicit def implicitProvider[..$params](implicit ..$providerArgs)
                    : _root_.io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
                 _root_.io.scalaland.pulp.Provider.const(new $name[..${params.map(_.name)}](...$ctorArgs))""": DefDef

          case WiredImpl.Type.Factory =>
            q"""implicit def implicitProvider[..$params](implicit ..$providerArgs)
                    : _root_.io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
                  _root_.io.scalaland.pulp.Provider.factory(new $name[..${params.map(_.name)}](...$ctorArgs))""": DefDef

          case WiredImpl.Type.Singleton if params.isEmpty =>
            q"""implicit lazy val implicitProvider
                    : _root_.io.scalaland.pulp.Provider[$name[..${params.map(_.name)}]] =
                  _root_.io.scalaland.pulp.Provider.const(new $name[..${params.map(_.name)}](...$ctorArgs))""": ValDef

          case WiredImpl.Type.Singleton if params.nonEmpty =>
            c.abort(c.enclosingPosition, "@Singleton cannot be used on parametric types")
        }
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
      case got => c.abort(c.enclosingPosition, s"@Wired, @Singleton or @Factory can only annotate class, got: $got")
    }
  }
}

private[pulp] object WiredImpl {

  sealed trait Type
  object Type {
    case object Default extends Type
    case object Factory extends Type
    case object Singleton extends Type
  }

  def impl(wiredType: WiredImpl.Type)(c: Context)(annottees: Seq[c.Expr[Any]]): c.Expr[Any] =
    new WiredImpl(wiredType)(c)(annottees).wire().asInstanceOf[c.Expr[Any]]
}
