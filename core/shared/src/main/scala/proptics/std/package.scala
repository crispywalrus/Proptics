package proptics

package object std {
  object all extends AllStdOptics
  object eitherK extends CoproductOptics
  object either extends EitherOptics
  object function extends FunctionOptics
  object list extends ListOptics
  object option extends OptionOptics
  object tuple2K extends ProductOptics
  object string extends StringOptics
  object tuple extends TuplesOptics
  object unit extends UnitOptics
}
