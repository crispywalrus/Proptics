---
id: stall
title: Stall
---

`Stall[A, B, S, T]` is a data type shaped like a `Profunctor`, which characterizes the construction of an <a href="/Proptics/docs/optics/affine-traversal" target="_blank">AffineTraversal</a> and <a href="/Proptics/docs/an-optics/an-affine-traversal" target="_blank">AnAffineTraversal</a>.</br>
`AffineTraversal_[S, T, A, B]` and `AnAffineTraversal_[S, T, A, B]` both take two functions as arguments, `viewOrModify: S => Either[T, A]`, which is a matching function that produces an `Either[T, A]` given an `S`,
and `set: S => B => T` function which takes a structure `S` and a new focus `B` and returns a structure of `T`.
`Stall[A, B, S, T]` also takes these two function, thus making it a data type that embeds the way to construct an `AffineTraversal` or an `AffineTraversal`.

```scala
case class Stall[A, B, S, T](viewOrModify: S => Either[T, A], set: S => B => T)
```

While `AffineTraversal` and `AnAffineTraversal` construction is the same, their internal encodings is different.


#### AffineTraversal

```scala
object AffineTraversal_ {
  def apply[S, T, A, B](viewOrModify: S => Either[T, A])(set: S => B => T): AffineTraversal_[S, T, A, B]
}
```

`AffineTraversal_[S, T, A, B]` is a function `P[A, B] => P[S, T]` that takes a <a href="/Proptics/docs/profunctors/choice" target="_blank">Choice</a>, and
 <a href="/Proptics/docs/profunctors/strong" target="_blank">Strong</a> of P[_, _].

```scala
abstract class AffineTraversal_[S, T, A, B] {
  private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev0: Choice[P], ev1: Strong[P]): P[S, T]
}
```

#### AnAffineTraversal

```scala
object AnAffineTraversal_ {
  def apply[S, T, A, B](viewOrModify: S => Either[T, A])(set: S => B => T): AnAffineTraversal_[S, T, A, B]
}
```

`AnAffineTraversal_[S, T, A, B]` is a function `P[A, B] => P[S, T]` Where's the `P[_, _]` is a data </br> type of `Stall`

```scala
abstract class AnAffineTraversal_[S, T, A, B] {
  private[proptics] def apply(pab: Stall[A, B, A, B]): Stall[A, B, S, T]
}
```

In order for `AnAffineTraversal_[S, T, A, B]` to be compatible with `AffineTraversal_[S, T, A, B]`, an instance of `Profunctor` of `Stall` has been
introduced.

<a href="/Proptics/docs/profunctors/profunctor" target="_blank">Profunctor[_, _]</a> is a type constructor that takes 2 type parameters. `Stall[A, B, S, T]` is a type that has 4 type parameters, so we need
to fix two of the type parameters of `Stall` in order to create an instance of `Profunctor` of `Market`. We can use Scala's type lambda syntax:

```scala
implicit def profunctorStall[E, F]: Profunctor[({ type P[S, T] = Stall[E, F, S, T] })#P] =
  new Profunctor[({ type P[S, T] = Stall[E, F, S, T] })#P] {
    override def dimap[A, B, C, D](fab: Stall[E, F, A, B])
                                  (f: C => A)
                                  (g: B => D): Stall[E, F, C, D] =
      Stall(c => fab.viewOrModify(f(c)).leftMap(g), c => ff => g(fab.set(f(c))(ff)))
  }
```

or we can use the <a href="https://github.com/typelevel/kind-projector" target="_blank">kind projector</a> compiler plugin:

```scala
implicit def profunctorStall[E, F]: Profunctor[Stall[E, F, *, *]] =
  new Profunctor[Stall[E, F, *, *]] {
    override def dimap[A, B, C, D](fab: Stall[E, F, A, B])
                                  (f: C => A)
                                  (g: B => D): Stall[E, F, C, D] =
      Stall(c => fab.viewOrModify(f(c)).leftMap(g), c => ff => g(fab.set(f(c))(ff)))
  }
```

`AnAffineTraversal` allows us to export its internal construction logic to a `Stall` using the `toStall` method.

```scala
import proptics.AnAffineTraversal
// import proptics.AnAffineTraversal

sealed trait Json
// defined trait Json

case object JNull extends Json
// defined object JNull

case class JNumber(value: Int) extends Json
// defined class JNumber

val jsonAffineTraversal =
  AnAffineTraversal.fromPartial[Json, Int] { case JNumber(i) => i } { json => i =>
    json match {
      case JNumber(_) => JNumber(i)
      case _          => json
    }
  }
// jsonAffineTraversal: AnAffineTraversal[Json,Int] = AnAffineTraversal_$$anon$6@27ce826e

val stall = jsonAffineTraversal.toStall
//  stall: proptics.internal.Stall[Int,Int,Json,Json] = 
//    Stall(proptics.AnAffineTraversal_$$$Lambda$6037/0x0000000801cb3040@1adf1c6a,
//          proptics.AnAffineTraversal_$$$Lambda$6038/0x0000000801cb3840@1a9cda87)

stall.viewOrModify(JNumber(9))
// res0: Either[Json,Int] = Right(9)

stall.set(JNumber(1))(9)
// res1: Json = JNumber(9)
```

We can later on create a new instance of an `AffineTraversal` or an `AnAffineTraversal` from the stall instance

```scala
import proptics.AffineTraversal
// import proptics.AffineTraversal

import proptics.AnAffineTraversal
// import proptics.AnAffineTraversal

val anAffineTraversalFromStall: AnAffineTraversal[Json, Int] = 
  AnAffineTraversal[Json, Int](stall.viewOrModify)(stall.set)
// anAffineTraversalFromStall: proptics.AnAffineTraversal[Json,Int] = 
//   proptics.AnAffineTraversal_$$anon$6@77d28f9

val affineTraversalFromStall: AffineTraversal[Json, Int] = 
  AffineTraversal[Json, Int](stall.viewOrModify)(stall.set)
// affineTraversalFromStall: proptics.AffineTraversal[Json,Int] = 
//   proptics.AffineTraversal_$$anon$10@7995e246
```
