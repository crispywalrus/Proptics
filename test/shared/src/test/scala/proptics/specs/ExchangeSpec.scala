package proptics.specs

import cats.Eq
import cats.laws.discipline.{ExhaustiveCheck, FunctorTests, MiniInt, ProfunctorTests}
import org.scalacheck.Arbitrary._
import org.scalacheck.Cogen._
import org.scalacheck.{Arbitrary, Gen}

import proptics.internal.Exchange

class ExchangeSpec extends PropticsSuite {
  implicit def eqExchange0(implicit ev: ExhaustiveCheck[MiniInt]): Eq[Exchange[Int, Int, Int, Int]] = Eq.instance[Exchange[Int, Int, Int, Int]] { (ex1, ex2) =>
    ev.allValues.forall { miniInt =>
      val int = miniInt.toInt

      ex1.view(int) === ex2.view(int) && ex1.review(int) === ex2.review(int)
    }
  }

  implicit def arbExcahnge: Arbitrary[Exchange[Int, Int, Int, Int]] = Arbitrary[Exchange[Int, Int, Int, Int]] {
    for {
      view <- Gen.function1[Int, Int](Arbitrary.arbInt.arbitrary)
      review <- Gen.function1[Int, Int](Arbitrary.arbInt.arbitrary)
    } yield Exchange(view, review)
  }

  checkAll("Functor Exchange[Int, Int, Int, Int]", FunctorTests[Exchange[Int, Int, Int, *]].functor[Int, Int, Int])
  checkAll("Profunctor Exchange[Int, Int, Int, Int]", ProfunctorTests[Exchange[Int, Int, *, *]].profunctor[Int, Int, Int, Int, Int, Int])
}
