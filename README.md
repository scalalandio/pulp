# Pulp

[![https://travis-ci.org/scalalandio/pulp](https://api.travis-ci.org/scalalandio/pulp.svg?branch=master)](https://travis-ci.org/scalalandio/pulp)
[![Maven Central](https://img.shields.io/maven-central/v/io.scalaland/pulp_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cpulp)
[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Too much fructose is not good for your health, so you should remove
Guice from your diet. This small experimental project allows you to have
your fruity projects with no Guice.

## Getting started

Library is available for Scala 2.11, 2.12, 2.13-M4 and Scala.js 0.6
(Scala.js without 2.13.0-M4 due to a compiler bug in former ).

Add it with (2.11, 2.12):

```scala
libraryDependencies += "io.scalaland" %% "pulp" % pulpVersion
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

or if you cross-build with Scala.js (2.11, 2.12):

```scala
libraryDependencies += "io.scalaland" %%% "pulp" % pulpVersion
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

or with Scala 2.13:

```scala
libraryDependencies += "io.scalaland" %% "pulp" % pulpVersion
scalacOptions += "-Ymacro-annotations"
```

Latest version can be checked on Maven and is displayed on the badge above.

Ammonite users can try it out with:

```scala
import $ivy.`io.scalaland:pulp_2.12:0.0.8`, io.scalaland.pulp._
interp.load.plugin.ivy("org.scalamacros" % "paradise_2.12.4" % "2.1.0")
```

With Ammonite 1.1.0 you can [try out this showoff code](https://gist.github.com/MateuszKubuszok/595b1b6cb409f2ef0cbf5d5c914e1e1b)!

## Usage

See [DOCS](DOCS.md) for specific cases or read further for understanding the general idea.

## Motivation

I wanted to avoid runtime reflection based dependency injection
in my program while still avoiding the need to pass everything manually.
Existing ways of doing DI in Scala that I knew of were:

 * manual dependency injection
 * usage of runtime reflection like [Guice](https://github.com/google/guice)
   or one used by [Spring Framework](https://spring.io)
 * semi-manual DI via something like [MacWire](https://github.com/adamw/macwire)
 * usage of Scala's built-in implicits

All of above have some pros and cons thought it's mostly up to
programmers' taste to decide which trade off they like better (though
they would often defend their own choice as the only reasonable).

I wanted to go with implicits, but that generating a bit of a boilerplate:

```scala
class A
class B (implicit a: A)
class C (implicit b: B)
class D (implicit b: B, c: C)

implicit val a: A = new A
implicit val b: B = new B
implicit val c: C = new C
```

Additionally we pollute the scope with tons of manually written
implicits, including these passed by constructor.

Instead we could move them to companion objects and wrap in a dedicated
type to ensure they won't accidentally mix with other implicits:

```scala
trait Provider[T] { def get(): T }
object Provider { def get[T: Provider]: T = implicitly[Provider[T]].get() }

class A
object A { implicit def provide: Provider[A] = () => new A }
class B (a: A)
object B { implicit def provide(implicit a: Provider[A]): Provider[B] = () => new B(a.get()) }
class C (b: B)
object B { implicit def provide(implicit b: Provider[B]): Provider[C] = () => new C(b.get()) }
class D (b: B, c: C)
object D { implicit def provide(implicit b: Provider[B], c: Provider[C]): Provider[D] = () => new D(b.get(), c.get()) }

Provider.get[D]
```

However, as we can see it brings a lot of boilerplate to the table.
But what if we generated all of that code? E.g. with macro annotations:

```scala
@Wired class A
@Wired class B (a: A)
@Wired class C (b: B)
@Wired class D (b: B, c: C)

Provider.get[D]
```

That's basically what Pulp does.

## Features

 * both monomorphic and polymorphic classes
 * existing companion objects will be extended and missing generated
 * class might have or have not dependencies passed via constructor
 * type-class derivation via `import io.scalaland.pulp.semiauto._`

## Limitations

Pulp uses implicits for passing objects around. It means that
`Provider[T]` must be in scope of initialization for each dependency
required by our class. We might pass it manually, write implicit by hand
or take from companion object - remember however that only classes
annotated with `@Wired` will have implicit `Provider`s generated.

Additionally whether something will have one or more instances is not
guaranteed for `@Wired` - if one need to ensure that there will be only
one Provider or that each Provider of some type will always return new
instance one should use `@Singleton` or `@Factory`. If there might be
arguments available in first usage scopes, Provider needs arguments from
scope, but `@Singleton` doesn't work you might use `@Cached`.

Last but not least such implementation of `Provider`s is invariant - if
we have `trait A` and `@Wired class AImpl extends A` it will not be
resolved for `A` unless we explicitly provide

```scala
implicit val a = Provider.upcast[AImpl, A]
```

or (if implementation is accessible to interface's scope):

```scala
@ImplementedAs[AImpl] class A

@Wired class AImpl extends A
```
