package proptics.specs

import cats.instances.option.catsStdInstancesForOption
import cats.instances.string._
import cats.syntax.either._
import cats.syntax.option._
import proptics.Prism
import proptics.law.PrismRules
import proptics.specs.Json._
import proptics.instances.boolean._

class PrismSpec extends PropticsSuite {
  val jsonPrism: Prism[Json, String] =
    Prism[Json, String] {
      case JString(value) => value.asRight[Json]
      case json           => json.asLeft[String]
    }(JString)

  val fromOptionJsonPrism: Prism[Json, String] =
    Prism.fromOption[Json, String] {
      case JString(value) => value.some
      case _              => None
    }(JString)

  val partialJsonPrism: Prism[Json, String] =
    Prism.fromPartial[Json, String] {
      case JString(value) => value
    }(JString)

  val emptyStr = ""
  val jNumber: JNumber = JNumber(9d)
  val jsonContent: String = "proptics"
  val jStrEmpty: JString = JString("")
  val jStringContent: JString = JString(jsonContent)
  val jStringContentUppercase: JString = JString(jsonContent.toUpperCase)
  def lengthGreaterThan5(str: String): Boolean = greaterThan5(str.length)
  def lengthGreaterThan10(str: String): Boolean = greaterThan10(str.length)

  checkAll("Prism fromOption", PrismRules(fromOptionJsonPrism))
  checkAll("Prism fromPartial", PrismRules(partialJsonPrism))
  checkAll("Prism apply", PrismRules(jsonPrism))

  test("viewOrModify") {
    jsonPrism.viewOrModify(jStringContent) shouldEqual jsonContent.asRight[Json]
    jsonPrism.viewOrModify(jNumber) shouldEqual jNumber.asLeft[String]
  }

  test("preview") {
    jsonPrism.preview(jStringContent) shouldEqual jsonContent.some
    jsonPrism.preview(jNumber) shouldEqual None
  }

  test("review") {
    jsonPrism.review(jsonContent) shouldEqual jStringContent
  }

  test("set") {
    jsonPrism.set(jsonContent)(jStrEmpty) shouldEqual jStringContent
  }

  test("setOption") {
    jsonPrism.setOption(jsonContent)(jStrEmpty) shouldEqual jStringContent.some
    jsonPrism.setOption(jsonContent)(jNumber) shouldEqual None
  }

  test("over") {
    jsonPrism.over(_.toUpperCase)(jStringContent) shouldEqual jStringContentUppercase
  }

  test("overOption") {
    jsonPrism.overOption(_.toUpperCase)(jStringContent) shouldEqual jStringContentUppercase.some
    jsonPrism.overOption(_.toUpperCase)(jNumber) shouldEqual None
  }

  test("traverse") {
    jsonPrism.traverse(jStringContent)(_.some) shouldEqual Some(jStringContent)
    jsonPrism.traverse(jStringContent)(_.some) shouldEqual jsonPrism.overF(_.some)(jStringContent)
  }

  test("forall") {
    jsonPrism.forall(lengthGreaterThan5 _)(jStringContent) shouldEqual true
    jsonPrism.forall(lengthGreaterThan10 _)(jStringContent) shouldEqual false
    jsonPrism.forall(lengthGreaterThan5 _)(jNumber) shouldEqual true
    jsonPrism.forall(lengthGreaterThan10 _)(jNumber) shouldEqual true
  }

  test("forall using heyting") {
    jsonPrism.forall(jStringContent)(lengthGreaterThan5) shouldEqual true
    jsonPrism.forall(jStringContent)(lengthGreaterThan10) shouldEqual false
    jsonPrism.forall(jNumber)(lengthGreaterThan5) shouldEqual true
    jsonPrism.forall(jNumber)(lengthGreaterThan10) shouldEqual true
  }

  test("exists") {
    jsonPrism.exists(lengthGreaterThan5)(jStringContent) shouldEqual true
    jsonPrism.exists(lengthGreaterThan10)(jStringContent) shouldEqual false
  }

  test("notExists") {
    jsonPrism.notExists(lengthGreaterThan10)(jStringContent) shouldEqual true
    jsonPrism.notExists(lengthGreaterThan5)(jStringContent) shouldEqual false
    jsonPrism.notExists(lengthGreaterThan5)(jStringContent) shouldEqual
      (!jsonPrism.exists(lengthGreaterThan5)(jStringContent))
  }

  test("contains") {
    jsonPrism.contains(jStringContent)(jsonContent) shouldEqual true
    jsonPrism.contains(jStringContent)(emptyStr) shouldEqual false
  }

  test("notContains") {
    jsonPrism.notContains(jStringContent)(emptyStr) shouldEqual true
    jsonPrism.notContains(jStringContent)(jsonContent) shouldEqual false
    jsonPrism.notContains(jStringContent)(jsonContent) shouldEqual (!jsonPrism.contains(jStringContent)(jsonContent))
  }

  test("isEmpty") {
    jsonPrism.isEmpty(jStringContent) shouldEqual false
    jsonPrism.isEmpty(jNumber) shouldEqual true
  }

  test("nonEmpty") {
    jsonPrism.nonEmpty(jStringContent) shouldEqual true
    jsonPrism.nonEmpty(jNumber) shouldEqual false
    jsonPrism.nonEmpty(jStringContent) shouldEqual (!jsonPrism.isEmpty(jStringContent))
  }

  test("find") {
    jsonPrism.find(lengthGreaterThan5)(jStringContent) shouldEqual jsonContent.some
    jsonPrism.find(lengthGreaterThan10)(jStringContent) shouldEqual None
    jsonPrism.find(lengthGreaterThan5)(jNumber) shouldEqual None
    jsonPrism.find(lengthGreaterThan10)(jNumber) shouldEqual None
  }
}
