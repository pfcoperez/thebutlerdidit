package org.pfcoperez.thebutlerdidit

import fastparse._
import NoWhitespace._
import org.scalatest.Inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class BasicParsersSpec extends AnyFunSpec with Matchers with Inside {

  describe("BasicParsers") {
    import ThreadDumpParsers.BasicParsers._

    it("should parse integer numbers") {
      inside(parse("42", intNumber(_))) {
        case Parsed.Success(x, _) => assert(x === 42)
      }

      inside(parse("-42", intNumber(_))) {
        case Parsed.Success(x, _) => assert(x === -42)
      }

      inside(parse("+42", intNumber(_))) {
        case Parsed.Success(x, _) => assert(x === 42)
      }

      inside(parse("a32", intNumber(_))) {
        case _: Parsed.Failure =>
      }
    }

    it("should parse float numbers") {
      inside(parse("42.3", floatNumber(_))) {
        case Parsed.Success(x, _) => assert(x === 42.3F)
      }

      inside(parse("-42.1", floatNumber(_))) {
        case Parsed.Success(x, _) => assert(x === -42.1F)
      }

      inside(parse("+42.42", floatNumber(_))) {
        case Parsed.Success(x, _) => assert(x === 42.42F)
      }

      inside(parse("42", floatNumber(_))) {
        case _: Parsed.Failure =>
      }
    }

    it("should parse float numbers with units") {
      inside(parse("42.3ms", floatWithUnits("ms")(_))) {
        case Parsed.Success(WithUnits(x: Float, "ms"), _) => assert(x === 42.3F)
      }

      inside(parse("-42.1ms", floatWithUnits("ms")(_))) {
        case Parsed.Success(WithUnits(x: Float, "ms"), _) => assert(x === -42.1F)
      }

      inside(parse("+42.42ms", floatWithUnits("ms")(_))) {
        case Parsed.Success(WithUnits(x: Float, "ms"), _) => assert(x === 42.42F)
      }

      inside(parse("42.3m/s", floatWithUnits("ms")(_))) {
        case _: Parsed.Failure =>
      }

      inside(parse("42.3", floatWithUnits("ms")(_))) {
        case _: Parsed.Failure =>
      }
    }

    it("should parse hex values") {

      inside(parse("0xBADF00D", hexDec(_))) {
        case Parsed.Success(x: BigInt, _) => assert(x.toLong.toHexString === "badf00d")
      }

      inside(parse("0XBADF00D", hexDec(_))) {
        case Parsed.Success(x: BigInt, _) => assert(x.toLong.toHexString === "badf00d")
      }

      inside(parse("0xBADF00D2", hexDec(_))) {
        case Parsed.Success(x: BigInt, _) => assert(x.toLong.toHexString === "badf00d2")
      }

      def parserWithEnd[_ : P] = P(hexDec ~ End)

      assert(!parse("0x", parserWithEnd(_)).isSuccess)
      assert(!parse("BADF00D", parserWithEnd(_)).isSuccess)
      assert(!parse("0xbadfood", parserWithEnd(_)).isSuccess)

    }

  }

}
