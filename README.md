# Pulp

Too much fructose is not good for your health, so you should remove
Guice from your diet. This small experimental project allows you to have
your fruity projects with no Guice.

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
object B { implicit def provide(a: Provider[A]): Provider[B] = () => new B(a.get()) }
class C (b: B)
object B { implicit def provide(b: Provider[B]): Provider[C] = () => new C(b.get()) }
class D (b: B, c: C)
object D { implicit def provide(b: Provider[B], c: Provider[C]): Provider[D] = () => new D(b.get(), c.get()) }

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

## Limitations

Pulp uses implicits for passing objects around. It means that
`Provider[T]` must be in scope of initialization for each dependency
required by our class. We might pass it manually, write implicit by hand
or take from companion object - remember however that only classes
annotated with `@Wired` will have implicit `Provider`s generated.

Additionally whether something will have one or more instances is kind
of a undefined behavior now, so one should not rely on either classes
being singletons or having each instance being different object.
