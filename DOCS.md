# Documentation

## General mechanism

Pulp uses implicits `Provider` type-class for dependency injection.

```scala
trait Provider[A] {

  def get: A
}
```

For basic cases like

```scala
class A(b: B, c: C) {
  ..
}
```

it provides macro annotations that would create implicit def inside
companion object

```scala
object A {
  implicit def provider(implicit b: Provider[B], c: Provider[C]):
    Provider[A] = ...
}
```

In this example as long as Providers for `B` and `C` will be in scope,
then `Provider[C]` will return generated `Provider[C]` while
`Provider.get[C]` will return `C` value.

As we can see this mechanism relies on propagating implicit Providers
down the dependency hierarchy.

## Macro annotations

There are 4 flavors of macro annotation generating different Providers:

 * `@Wired` - a default one. It generates following implementation:
    ```scala
    def provider: Provider[A] = new Provider[A] { lazy val: A = ... }
    ```
    it reuses computed `A` value in scope where it was generated, but
    across different scopes it might be generate a different instances
    of a type-class,
 * `@Cached` - similar to default but it caches globally first instance
   obtained for a `WeakTypeTag`:
    ```scala
    def provider: Provider[A] = new Provider[A] { def: A = internals.Cache.query(...) }
    ```
    As long as two usages creates the same `WeakTypeTag` it will reuse
    first instance,
 * `@Factory` - for *factories*. It generates following implementation:
    ```scala
    def provider: Provider[A] = new Provider[A] { def: A = ... }
    ```
    it guarantees to return new instance of `A` each time `get` is
    called,
 * `@Singleton` - It generates following implementation:
    ```scala
    lazy val provider: Provider[A] = new Provider[A] { lazy val: A = ... }
    ```
    it guarantees to return the same instance of `A` each time `get` is
    called.

Probably the best would be to default to `@Wired` and change them to
`@Cached`, `@Factory` or `@Singleton` only where needed:

```scala
@Wired class NormalClass
@Cached class Storage
@Singleton class Database
@Factory class AsyncQueryBuilder
```

Macro annotations support more cases than semiauto-generated `Provider`s:

```scala
@Wired class MultipleParamLists(param: String)(param2: Int)
@Factory class WithImplicit(value: Double)(implicit ec: ExecutionContext)
@Singleton class TypeBounded[F: Monad](init: F[String])
```

## Interface-Implementation separation

In case we want to split interface and implementation we can always use

```scala
trait A
@Wired class AImpl extends A
implicit aProvider: Provider[A] = Provider.upcast[AImpl, A]
```

However, in case both are defined in the same scope one would prefer to
use just annotation for this:

```scala
@ImplemetendAs[AImpl] class A
@Wired class AImpl extends A
```

## Provider derivation

In case the class is a case class or the class has all its attributes public:

```scala
case class B (a: A)
class C (val a: A)
```

we might use derivation to generate provider using those available in
scope:

```scala
import io.scalaland.pulp.semiauto._
Provider.get[B]
Provider.get[C]
```

However, we need to remember, that current scope of semiauto is limited. It does not support:

 * multiple parameter lists: explicit (`class A (i: Int)(d: Double)`) and implicit (`class B (implicit ec: ExecutionContext)`, `class C[F: Functor]`) - you need annotate the type to generate the provider,
 * sum types - you need to create an implicit `Provider` yourself, e.g. with `Provider.const` or `Provider.factory`,
 * classes, that cannot be considered product types,
 * overall anything that cannot have `Generic` representation derived by Shapeless.

## Parametric classes

Macro annotations support it out of the box:

```scala
@ImplementedAs[ParametricImpl[A]] trait Parametric[A]
@Wired class ParametricImpl[A] extends Parametric[A]
```

Exception is the `@Singleton`, which currently requires a monomorphic implementation:

```scala
@Singleton class DoubleParametric extends Parametric[Double] // ok
// @Singleton class AnyParametric[A] // doesn't compile
```

## Implicit params

...are being automatically lifted to `Provider`:

```scala
implicit val ec: ExecutionContext = ...
Provider.get[ExecutionContext]
```

## Debugging

Macro-generated `Provider`s can be previewed during compilation with `-Dpulp.debug=debug` or `-Dpulp.debug=trace` SBT JVM flags.
