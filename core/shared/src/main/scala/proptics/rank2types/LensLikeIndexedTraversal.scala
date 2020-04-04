package proptics.rank2types

import cats.Applicative

private[proptics] trait LensLikeIndexedTraversal[I, S, T, A, B] {
  def apply[F[_]](f: ((I, A)) => F[B])(implicit ev:Applicative[F]): S => F[T]
}