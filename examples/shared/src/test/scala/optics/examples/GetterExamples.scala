package optics.examples

import proptics.Getter
import proptics.specs.PropticsSuite

class GetterExamples extends PropticsSuite {
  test("focus into nested data") {
    val composed =
      Getter[Person, Address](_.address) andThen
        Getter[Address, Street](_.street)

    assertResult(Street("Negra Arroyo Lane", 308))(composed.view(mrWhite))
  }

  test("using to in order to focus into nested data") {
    val composed =
      Getter[Person, Address](_.address) to (_.street)

    assertResult(Street("Negra Arroyo Lane", 308))(composed.view(mrWhite))
  }
}
