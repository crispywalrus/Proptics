package proptics

import scala.Function.const

import cats.{Applicative, Distributive, Functor}

import proptics.internal.{Indexed, Tagged, Zipping}
import proptics.profunctor.Costar._
import proptics.profunctor.{Closed, Costar}
import proptics.rank2types.Rank2TypeGrateLike
import proptics.syntax.costar._
import proptics.syntax.function._

/** A [[Grate_]] allows a generalized zipWith operation.
  *
  * @tparam S the source of a [[Grate_]]
  * @tparam T the modified source of a [[Grate_]]
  * @tparam A the focus of a [[Grate_]]
  * @tparam B the modified focus of a [[Grate_]]
  */
abstract class Grate_[S, T, A, B] { self =>
  private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Closed[P]): P[S, T]

  /** view the modified source of a [[Grate_]] */
  final def review(b: B): T = self(Tagged[A, B](b)).runTag

  /** set the modified focus of a [[Grate_]] */
  final def set(b: B): S => T = over(const(b))

  /** modify the focus type of a [[Grate_]] using a function, resulting in a change of type to the full structure */
  final def over(f: A => B): S => T = self(f)

  /** zip two sources of a [[Grate_]] together provided a binary operation which modify the focus of a [[Grate_]] */
  final def zipWith(s1: S, s2: S)(f: (A, A) => B): T = self(Zipping(f.curried)).runZipping(s1)(s2)

  /** modify an effectful focus of a [[Grate_]] to the type of the modified focus, resulting in a change of type to the full structure */
  final def cotraverse[F[_]: Applicative](fs: F[S])(f: F[A] => B): T = self(Costar(f)).runCostar(fs)

  /** synonym for [[cotraverse]], flipped */
  final def zipWithF[F[_]: Applicative](f: F[A] => B)(fs: F[S]): T = cotraverse(fs)(f)

  /** compose this [[Grate_]] with an [[Iso_]], having this [[Grate_]] applied first */
  final def andThen[C, D](other: Iso_[A, B, C, D]): Grate_[S, T, C, D] = new Grate_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Closed[P]): P[S, T] = self(other(pab))
  }

  /** compose this [[Grate_]] with an [[Iso_]], having this [[Grate_]] applied last */
  final def compose[C, D](other: Iso_[C, D, S, T]): Grate_[C, D, A, B] = new Grate_[C, D, A, B] {
    override private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Closed[P]): P[C, D] =
      other(self(pab))
  }

  /** compose this [[Grate_]] with an [[AnIso_]], having this [[Grate_]] applied first */
  final def andThen[C, D](other: AnIso_[A, B, C, D]): Grate_[S, T, C, D] = new Grate_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Closed[P]): P[S, T] =
      self(ev.dimap(pab)(other.view)(other.review))
  }

  /** compose this [[Grate_]] with an [[AnIso_]], having this [[Grate_]] applied last */
  final def compose[C, D](other: AnIso_[C, D, S, T]): Grate_[C, D, A, B] = new Grate_[C, D, A, B] {
    override private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Closed[P]): P[C, D] =
      ev.dimap(self(pab))(other.view)(other.review)
  }

  /** compose this [[Grate_]] with a [[Setter_]], having this [[Grate_]] applied first */
  final def andThen[C, D](other: Setter_[A, B, C, D]): Setter_[S, T, C, D] = new Setter_[S, T, C, D] {
    override private[proptics] def apply(pab: C => D): S => T = self(other(pab))
  }

  /** compose this [[Grate_]] with a [[Setter_]], having this [[Grate_]] applied last */
  final def compose[C, D](other: Setter_[C, D, S, T]): Setter_[C, D, A, B] = new Setter_[C, D, A, B] {
    override private[proptics] def apply(pab: A => B): C => D = other.over(self.over(pab))
  }

  /** compose this [[Grate_]] with a [[Grate_]], having this [[Grate_]] applied first */
  final def andThen[C, D](other: Grate_[A, B, C, D]): Grate_[S, T, C, D] = new Grate_[S, T, C, D] {
    override def apply[P[_, _]](pab: P[C, D])(implicit ev: Closed[P]): P[S, T] = self(other(pab))
  }

  /** compose this [[Grate_]] with a [[Grate_]], having this [[Grate_]] applied last */
  final def compose[C, D](other: Grate_[C, D, S, T]): Grate_[C, D, A, B] = new Grate_[C, D, A, B] {
    override private[proptics] def apply[P[_, _]](pab: P[A, B])(implicit ev: Closed[P]): P[C, D] = other(self(pab))
  }

  /** compose this [[Grate_]] with a [[Review_]], having this [[Grate_]] applied first */
  final def andThen[C, D](other: Review_[A, B, C, D]): Review_[S, T, C, D] = new Review_[S, T, C, D] {
    override private[proptics] def apply(tagged: Tagged[C, D]): Tagged[S, T] = self(other(tagged))
  }

  /** compose this [[Grate_]] with a [[Review_]], having this [[Grate_]] applied last */
  final def compose[C, D](other: Review_[C, D, S, T]): Review_[C, D, A, B] = new Review_[C, D, A, B] {
    override private[proptics] def apply(tagged: Tagged[A, B]): Tagged[C, D] =
      Tagged(other.review(self.review(tagged.runTag)))
  }

  /** compose this [[Grate_]] with an [[IndexedSetter_]], having this [[Grate_]] applied first */
  final def andThen[I, C, D](other: IndexedSetter_[I, A, B, C, D]): IndexedSetter_[I, S, T, C, D] = new IndexedSetter_[I, S, T, C, D] {
    override private[proptics] def apply(indexed: Indexed[* => *, I, C, D]): S => T =
      self.over(other.over(indexed.runIndex))
  }

  /** compose this [[Grate_]] with an [[IndexedSetter_]], having this [[Grate_]] applied last */
  final def compose[I, C, D](other: IndexedSetter_[I, C, D, S, T]): IndexedSetter_[I, C, D, A, B] = new IndexedSetter_[I, C, D, A, B] {
    override private[proptics] def apply(indexed: Indexed[* => *, I, A, B]): C => D =
      other.over { case (s, i) => self.over(a => indexed.runIndex((a, i)))(s) }
  }
}

object Grate_ {
  /** create a polymorphic [[Grate_]] from Rank2TypeGrateLike encoding */
  private[proptics] def apply[S, T, A, B](f: Rank2TypeGrateLike[S, T, A, B]): Grate_[S, T, A, B] = new Grate_[S, T, A, B] {
    override def apply[P[_, _]](pab: P[A, B])(implicit ev: Closed[P]): P[S, T] = f(pab)
  }

  /** create a polymorphic [[Grate_]] from a nested continuation function */
  final def apply[S, T, A, B](grate: ((S => A) => B) => T): Grate_[S, T, A, B] = Grate_(new Rank2TypeGrateLike[S, T, A, B] {
    override def apply[P[_, _]](pab: P[A, B])(implicit ev: Closed[P]): P[S, T] =
      ev.dimap[(S => A) => A, (S => A) => B, S, T](ev.closed(pab))(_.&)(grate)
  })

  /** create a polymorphic [[Grate_]] from a [[cats.Distributive]] */
  final def fromDistributive[F[_], A, B](implicit ev: Distributive[F]): Grate_[F[A], F[B], A, B] = {
    def cotraverse[G[_]: Functor](f: G[A] => B)(gfa: G[F[A]]): F[B] =
      ev.map(ev.cosequence(gfa))(f)

    Grate_[F[A], F[B], A, B](cotraverse(_: (F[A] => A) => B)(identity[F[A]])(Functor[F[A] => *]))
  }

  /** polymorphic identity of a [[Grate_]] */
  final def id[S, T]: Grate_[S, T, S, T] = Grate_[S, T, S, T]((s2s: (S => S) => T) => s2s(identity[S]))
}

object Grate {
  /** create a monomorphic [[Grate]] from a nested continuation function */
  final def apply[S, A](to: ((S => A) => A) => S): Grate[S, A] = Grate_(to)

  /** create a monomorphic [[Grate]] from a [[cats.Distributive]] */
  final def fromDistributive[F[_]: Distributive, A]: Grate[F[A], A] = Grate_.fromDistributive

  /** monomorphic identity of a [[Grate]] */
  final def id[S]: Grate[S, S] = Grate_.id[S, S]
}
