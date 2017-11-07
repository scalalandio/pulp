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

There are 3 flavors of macro annotation generating different Providers:

 * `@Wired` - a default one. It generates following implementation:
    ```scala
    def provider: Provider[A] = new Provider[A] { lazy val: A = ... }
    ```
    it reuses computed `A` value in scope where it was generated, but
    across different scopes it might be generate a different instances
    of a type-class,
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
`@Factory` or `@Singleton` only where needed:

```scala
@Wired class NormalClass
@Singleton class Database
@Factory class AsyncQueryBuilder
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

In case class is case class or class has all its attributes public:

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
