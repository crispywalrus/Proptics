---
id: optic
title: Optic
---

Optic is a type that can be used to focus on a particular element in a deeply nested data structure. <br/>
It describes a relationship between a structure `S` and zero, one, or many values of type `A`, called the focus (or foci) of the optic.

## Definition

This is a general definition of an optic:

```scala
/**
  * @tparam S the source of an Optic_
  * @tparam T the modified source of an Optic_
  * @tparam A the focus of an Optic_
  * @tparam B the modified focus of an Optic_
  */
trait Optic_[S, T, A, B] {
  def apply[P[_, _]](pab: P[A, B]): P[S, T]   
}
```

So basically an optic is a function `P[A, B] => P[S, T]`. So how can we convert `P[A, B]` into `P[S, T]`?<br/>
We need two functions, for the left side a function that can extract an A out of S, and for the right side a conversion function from `B` into a `T`,
and this is equivalent to the `dimap` function of a [Profunctor](../profunctors/profunctor)

```scala
trait Profunctor[F[_, _]] {
  def dimap[A, B, C, D](fab: F[A, B])(f: C => A)(g: B => D): F[C, D]
}
```

If we replace the type parameters to be aligned with the type parameters of an optic:<br/>
`F -> P`, `C -> S` and `D -> T`, then we would end up with a function from `P[A, B] => P[S, T]` 

```scala
trait Profunctor[P[_, _]] {
  def dimap[A, B, S, T](fab: P[A, B])(f: S => A)(g: B => T): P[S, T]
}
```

#### Optics and Profunctors

Some optics take in addition to `P[A, B]` some kind of implicit instance of `Profunctor`.

```scala
trait Optic_[S, T, A, B] {
  def apply[P[_, _]](pab: P[A, B])(implicit ev: Profunctor[P]): P[S, T]   
}
``` 

The ones that don't require a `Profunctor` are using a concrete type that is shaped like a profunctor, for example the `Function` type
that takes two type parameters like the `Profunctor`.

```scala
type Function[-A, +B] = Function1[A, B]
```

## Understanding the types of an Optic

Let's try to understand these types using an example.  
A simple example would be focusing on a specific element of a Tuple. 

```scala
val tuple: (String, Int) = ("One", 1)    
``` 

Let's assume that we want to change the second element of the tuple to be `String`

```scala
(String, Int) => (String, String)
```

the concrete types for this optic would be:

```scala
/**
  * (String, Int) the source of an [[Optic_]]
  * (String, String) the modified source of an [[Optic_]]
  * Int the focus of an [[Optic_]]
  * String the modified focus of an [[Optic_]]
  */
trait Optic_ {
  def apply[P[_, _]](pab: P[Int, String])
                    (implicit ev: Profunctor[P]): P[(String, Int), (String, String)]   
} 
```

An optic that changes its focus/structure, is called `Polymorphic Optic`.

Often times you will just change the value of the focus, which means to the type of the structure will remain the same, for this specific scenario
instead of using an `Optic_[S, S, A, A]` which has repeating type parameters, there is a short notation of:

```scala
type Optic[S, A] = Optic_[S, S, A, A]
```

An optic that does not change its focus/structure, is called `Monomorphic Optic`.

## Optic internal encoding

While `Optic_[S, T, A, B]` is not really used for the encoding of optics in `Proptics` (does not serve as a base class for all optics, and it is only shown for explanation purposes), 
all optics are functions from `P[A, B]` to `P[S, T]`, where's the `P[_, _]` is a typeclass derived from profunctor.<br/>
`AnOptic_[S, T, A, B]` is an optic, that takes a data typed shaped liked a profunctor, which has an instance of the same `Profunctor` as the one taken by `Optic_[S, T, A, B]`,
thus making the data type compatible with `Optic_[S, T, A, B]`.<br/>
For a more detailed explanation go to [AnOptic](../an-optics/an-optic).
   
## List of all Optics

This table shows all pairs of optics and their profunctor:

|                                                          |  Profunctor                                                                               | Data Type                                       |
| -------------------------------------------------------- |:-----------------------------------------------------------------------:|:--------------------------------------:|
| [Iso](iso.md)                                            | [Profunctor](../profunctors/profunctor.md)                              |                                        |
| [AnIso](../an-optics/an-iso.md)                          |                                                                         | [Exchange](../data-types/exchange.md)  |
| [Lens](lens.md)                                          | [Strong](../profunctors/strong.md)                                      |                                        |
| [ALens](../an-optics/a-lens.md)                          |                                                                         | [Shop](../data-types/shop.md)          |           
| [Prism](prism.md)                                        | [Choice](../profunctors/choice.md)                                      |                                        |
| [APrism](../an-optics/a-prism.md)                        |                                                                         | [Market](../data-types/market.md)      |
| [AffineTraversal](affine-traversal.md)                   | [Choice](../profunctors/choice.md), [Strong](../profunctors/strong.md)  |                                        |
| [AnAffineTraversal](../an-optics/an-affine-traversal.md) |                                                                         | [Stall](../data-types/stall.md)        |
| [Traversal](traversal.md)                                | [Wander](../profunctors/wander.md)                                      |                                        |
| [ATraversal](../an-optics/a-traversal.md)                |                                                                         | [Bazaar](../data-types/bazaar.md)      |
| [Fold](fold.md)                                          |                                                                         | [Forget](../data-types/forget.md)      |
| [Getter](getter.md)                                      |                                                                         | [Forget](../data-types/forget.md)      |
| [Setter](setter.md)                                      |                                                                         | Function                               |
| [Grate](grate.md)                                        | [Closed](../profunctors/closed.md)                                      |                                        |
| [Review](review.md)                                      |                                                                         | [Tagged](../data-types/tagged.md)      |


