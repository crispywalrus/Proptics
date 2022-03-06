package proptics.specs

import scala.Function.const

import cats.syntax.either._

import proptics._

private[proptics] object compose {
  val fold: Fold[Int, Int] = Fold[Int, Int](identity)
  val getter: Getter[Int, Int] = Getter[Int](identity)
  val setter: Setter[Int, Int] = Setter[Int, Int](f => f)
  val grate: Grate[Int, Int] = Grate[Int, Int](_(identity))
  val review: Review[Int, Int] = Review[Int, Int](identity)
  val anIso: AnIso[Int, Int] = AnIso[Int, Int](identity)(identity)
  val iso: Iso[Int, Int] = Iso[Int, Int](identity[Int] _)(identity)
  val lens: Lens[Int, Int] = Lens[Int, Int](identity)(const(identity))
  val prism: Prism[Int, Int] = Prism[Int, Int](_.asRight[Int])(identity)
  val aLens: ALens[Int, Int] = ALens[Int, Int](identity)(const(identity))
  val aPrism: APrism[Int, Int] = APrism[Int, Int](_.asRight[Int])(identity)
  val indexedFold: IndexedFold[Int, Int, Int] = IndexedFold[Int, Int, Int]((_, 0))
  val traversal: Traversal[Int, Int] = Traversal[Int, Int](identity)(const(identity))
  val aTraversal: ATraversal[Int, Int] = ATraversal[Int, Int](identity)(const(identity))
  val indexedGetter: IndexedGetter[Int, Int, Int] = IndexedGetter[Int, Int, Int]((_, 0))
  val indexedSetter: IndexedSetter[Int, Int, Int] = IndexedSetter[Int, Int, Int](f => f(_, 0))
  val indexedLens: IndexedLens[Int, Int, Int] = IndexedLens[Int, Int, Int]((_, 0))(const(identity))
  val anIndexedLens: AnIndexedLens[Int, Int, Int] = AnIndexedLens[Int, Int, Int]((_, 0))(const(identity))
  val affineTraversal: AffineTraversal[Int, Int] = AffineTraversal[Int, Int](_.asRight[Int])(const(identity))
  val anAffineTraversal: AnAffineTraversal[Int, Int] = AnAffineTraversal[Int, Int](_.asRight[Int])(const(identity))
  val indexedTraversal: IndexedTraversal[Int, Int, Int] = IndexedTraversal[Int, Int, Int]((_, 0))(const(identity))
}
