package proptics

import cats.syntax.eq._
import cats.syntax.option._
import cats.{Applicative, Eq, Id, Monoid}
import proptics.internal.Grating

import scala.Function.const

/**
 * An Optic with fixed type [[Grating]] [[cats.arrow.Profunctor]]
 *
 * @tparam S the source of an [[AGrate]]
 * @tparam T the modified source of an [[AGrate]]
 * @tparam A the target of an [[AGrate]]
 * @tparam B the modified target of an [[AGrate]]
 */
abstract class AGrate[S, T, A, B] { self =>
  def apply(grating: Grating[A, B, A, B]): Grating[A, B, S, T]

  def view(s: S)(implicit ev: Monoid[A]): A = asGrate.view(s)

  def set(b: B)(s: S)(implicit ev: Monoid[A]): T = over(const(b))(s)

  def over(f: A => B)(s: S)(implicit ev: Monoid[A]): T = overF[Id](f)(s)

  def overF[F[_]: Applicative](f: A => F[B])(s: S)(implicit ev: Monoid[A]): F[T] = traverse(s)(f)

  def traverse[F[_]](s: S)(f: A => F[B])(implicit ev0: Applicative[F], ev1: Monoid[A]): F[T] =
    ev0.map(f(self.view(s)))(self.set(_)(s))

  def filter(f: A => Boolean)(s: S)(implicit ev: Monoid[A]): Option[A] = view(s).some.filter(f)

  def exists(f: A => Boolean)(s: S)(implicit ev: Monoid[A]): Boolean = f(view(s))

  def noExists(f: A => Boolean)(s: S)(implicit ev: Monoid[A]): Boolean = !exists(f)(s)

  def contains(s: S)(a: A)(implicit ev0: Eq[A], ev1: Monoid[A]): Boolean = exists(_ === a)(s)

  def notContains(s: S)(a: A)(implicit ev0: Eq[A], ev1: Monoid[A]): Boolean = !contains(s)(a)

  def asGrate: Grate[S, T, A, B] = Grate(withGrate _)

  def withGrate(f: (S => A) => B): T = self(Grating(_.apply(identity))).runGrating(f)
}

object AGrate {
  private[proptics] def apply[S, T, A, B](f: Grating[A, B, A, B] => Grating[A, B, S, T]): AGrate[S, T, A, B] = new AGrate[S, T, A, B] {
    override def apply(grating: Grating[A, B, A, B]): Grating[A, B, S, T] = f(grating)
  }

  def apply[S, T, A, B](to: ((S => A) => B) => T)(implicit ev: DummyImplicit): AGrate[S, T, A, B] = AGrate((_: Grating[A, B, A, B]) => Grating(to))
}

object AGrate_ {
  def apply[S, A](to: ((S => A) => A) => S): AGrate_[S, A] = AGrate(to)
}
