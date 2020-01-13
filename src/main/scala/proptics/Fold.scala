package proptics

import cats.Foldable
import cats.syntax.monoid._
import cats.kernel.Monoid
import cats.syntax.either._
import proptics.internal.Forget
import proptics.profunctor.Choice

/**
 * A [[Fold]] is an [[Optic]] with fixed type [[Forget]] [[cats.arrow.Profunctor]]
 *
 * @tparam R the return type of a [[Fold]]
 * @tparam S the source of a [[Fold]]
 * @tparam T the modified source of a [[Fold]]
 * @tparam A the target of a [[Fold]]
 * @tparam B the modified target of a [[Fold]]
 */
abstract class Fold[R, S, T, A, B] extends Optic[Forget[R, *, *], S, T, A, B] {
}

object Fold {
  private[proptics] def liftForget[R, S, T, A, B](f: S => A): Forget[R, A, B] => Forget[R, S, T] =
    forget => Forget[R, S, T](forget.runForget compose f)

  private[proptics] def apply[R, S, T, A, B](f: Forget[R, A, B] => Forget[R, S, T]): Fold[R, S, T, A, B] = new Fold[R, S, T, A, B] {
    override def apply(pab: Forget[R, A, B]): Forget[R, S, T] = f(pab)
  }

  def apply[R, S, T, A, B](f: S => A)(implicit ev: DummyImplicit): Fold[R, S, T, A, B] =
    Fold(liftForget[R, S, T, A, B](f))

  def filtered[P[_, _], A](predicate: A => Boolean)(implicit ev: Choice[P]): Optic_[P, A, A] = {
    Optic_[P, A, A](pab => ev.dimap[Either[A, A], Either[A, A], A, A](ev.right(pab))
      (x => if (predicate(x)) x.asRight[A] else x.asLeft[A])(_.fold(identity[A], identity[A])))
  }

  def replicated[R, A, B, T](i: Int)(implicit ev: Monoid[R]): Fold[R, A, B, A, T] = {
    def go(i: Int, r: R): R = (i, r) match {
      case (0, _) => ev.empty
      case (n, x) => x |+| go(n - 1, x)
    }

    Fold((forget: Forget[R, A, T]) => Forget[R, A, B](a => go(i, forget.runForget(a))))
  }

  def folded[G[_], A, B, T, R](implicit ev1: Monoid[R], ev2: Foldable[G]): Fold[R, G[A], B, A, T] =
    Fold((forget: Forget[R, A, T]) => Forget[R, G[A], B](ev2.foldMap(_)(forget.runForget)))

  def unfolded[R, S, T, A, B](f: S => Option[(A, S)])(implicit ev: Monoid[R]): Fold[R, S, T, A, B] = {
    def go(s: S, forget: Forget[R, A, B]): R = {
      f(s).fold(ev.empty) { case (a, sn) => forget.runForget(a) |+| go(sn, forget) }
    }

    Fold((forget: Forget[R, A, B]) => Forget[R, S, T](s => go(s, forget)))
  }
}