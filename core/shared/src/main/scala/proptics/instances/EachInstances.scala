package proptics.instances

import scala.collection.immutable.{ListMap, SortedMap}

import cats.data.{Chain, NonEmptyChain, NonEmptyList, NonEmptyVector, OneAnd}
import cats.{Applicative, Order}

import proptics.rank2types.LensLike
import proptics.std.string._
import proptics.typeclass.Each
import proptics.{Traversal, Traversal2, Traversal3, Traversal4, Traversal5}

trait EachInstances extends ScalaVersionSpecificEachInstances { self =>
  def each[S, A](implicit ev: Each[S, A]): Traversal[S, A] = ev.each

  implicit def eachTuple2[A]: Each[(A, A), A] = new Each[(A, A), A] {
    override def each: Traversal[(A, A), A] =
      Traversal2[(A, A), (A, A), A, A](_._1, _._2)((b1, b2, _) => (b1, b2))
  }

  implicit def eachTuple3[A]: Each[(A, A, A), A] = new Each[(A, A, A), A] {
    override def each: Traversal[(A, A, A), A] =
      Traversal3[(A, A, A), (A, A, A), A, A](_._1, _._2, _._3)((b1, b2, b3, _) => (b1, b2, b3))
  }

  implicit def eachTuple4[A]: Each[(A, A, A, A), A] = new Each[(A, A, A, A), A] {
    override def each: Traversal[(A, A, A, A), A] =
      Traversal4[(A, A, A, A), (A, A, A, A), A, A](_._1, _._2, _._3, _._4)((b1, b2, b3, b4, _) => (b1, b2, b3, b4))
  }

  implicit def eachTuple5[A]: Each[(A, A, A, A, A), A] = new Each[(A, A, A, A, A), A] {
    override def each: Traversal[(A, A, A, A, A), A] =
      Traversal5[(A, A, A, A, A), (A, A, A, A, A), A, A](_._1, _._2, _._3, _._4, _._5)((b1, b2, b3, b4, b5, _) => (b1, b2, b3, b4, b5))
  }

  implicit final def eachString: Each[String, Char] = new Each[String, Char] {
    override def each: Traversal[String, Char] =
      stringToChars andThen self.each[List[Char], Char]
  }

  implicit final def eachVector[A]: Each[Vector[A], A] = new Each[Vector[A], A] {
    override def each: Traversal[Vector[A], A] = Traversal.fromTraverse[Vector, A]
  }

  implicit final def eachList[A]: Each[List[A], A] = new Each[List[A], A] {
    override def each: Traversal[List[A], A] = Traversal.fromTraverse[List, A]
  }

  implicit final def eachListMap[K, V]: Each[ListMap[K, V], V] = new Each[ListMap[K, V], V] {
    override def each: Traversal[ListMap[K, V], V] =
      Traversal.wander[ListMap[K, V], V](new LensLike[ListMap[K, V], ListMap[K, V], V, V] {
        override def apply[F[_]](f: V => F[V])(implicit ev: Applicative[F]): ListMap[K, V] => F[ListMap[K, V]] = s =>
          s.foldLeft(ev.pure(ListMap.empty[K, V])) { case (acc, (k, v)) =>
            ev.map2(f(v), acc)((head, tail) => tail + (k -> head))
          }
      })
  }

  implicit def eachSortedMap[K: Order, V]: Each[SortedMap[K, V], V] = new Each[SortedMap[K, V], V] {
    override def each: Traversal[SortedMap[K, V], V] = Traversal.fromTraverse[SortedMap[K, *], V]
  }

  implicit def eachChain[A]: Each[Chain[A], A] = new Each[Chain[A], A] {
    override def each: Traversal[Chain[A], A] = Traversal.fromTraverse[Chain, A]
  }

  implicit def eachOneAnd[G[_], A](implicit ev0: Each[G[A], A]): Each[OneAnd[G, A], A] = new Each[OneAnd[G, A], A] {
    override def each: Traversal[OneAnd[G, A], A] =
      Traversal.wander[OneAnd[G, A], A](new LensLike[OneAnd[G, A], OneAnd[G, A], A, A] {
        override def apply[F[_]](f: A => F[A])(implicit ev1: Applicative[F]): OneAnd[G, A] => F[OneAnd[G, A]] = s => ev1.map2(f(s.head), ev0.each.traverse(s.tail)(f))(OneAnd.apply)
      })
  }

  implicit def eachNonEmptyVector[A]: Each[NonEmptyVector[A], A] = new Each[NonEmptyVector[A], A] {
    override def each: Traversal[NonEmptyVector[A], A] = Traversal.fromTraverse[NonEmptyVector, A]
  }

  implicit def eachNonEmptyList[A]: Each[NonEmptyList[A], A] = new Each[NonEmptyList[A], A] {
    override def each: Traversal[NonEmptyList[A], A] = Traversal.fromTraverse[NonEmptyList, A]
  }

  implicit def eachNonEmptyChain[A]: Each[NonEmptyChain[A], A] = new Each[NonEmptyChain[A], A] {
    override def each: Traversal[NonEmptyChain[A], A] = Traversal.fromTraverse[NonEmptyChain, A]
  }
}
