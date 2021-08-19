package proptics.profunctor

import scala.annotation.implicitNotFound

import cats.arrow.Profunctor
import cats.syntax.either._

/** The [[Cochoice]] type class provides the dual operations of the [[Choice]].
  *
  * @tparam P
  *   a type constructor of kind (* -> * -> *)
  */
@implicitNotFound("Could not find an instance of Cochoice[${P}]")
trait Cochoice[P[_, _]] extends Profunctor[P] { self =>
  def unleft[A, B, C](p: P[Either[A, C], Either[B, C]]): P[A, B] =
    unright(self.dimap[Either[A, C], Either[B, C], Either[C, A], Either[C, B]](p)(_.fold(_.asRight[A], _.asLeft[C]))(_.fold(_.asRight[C], _.asLeft[B])))

  def unright[A, B, C](p: P[Either[C, A], Either[C, B]]): P[A, B] =
    unleft(self.dimap[Either[C, A], Either[C, B], Either[A, C], Either[B, C]](p)(_.fold(_.asRight[C], _.asLeft[A]))(_.fold(_.asRight[B], _.asLeft[C])))
}

object Cochoice {
  /** summon an instance of [[Cochoice]] for `P` */
  @inline def apply[P[_, _]](implicit ev: Cochoice[P]): Cochoice[P] = ev
}
