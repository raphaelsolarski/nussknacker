package pl.touk.nussknacker.engine.definition

import pl.touk.nussknacker.engine.{ExpressionEvaluator, Interpreter, compiledgraph}
import pl.touk.nussknacker.engine.api._
import pl.touk.nussknacker.engine.compile.PartSubGraphCompiler
import pl.touk.nussknacker.engine.definition.DefinitionExtractor.ObjectWithMethodDef
import pl.touk.nussknacker.engine.graph.node.CustomNode
import pl.touk.nussknacker.engine.splittedgraph.splittednode.SplittedNode
import pl.touk.nussknacker.engine.types.EspTypeUtils
import pl.touk.nussknacker.engine.util.SynchronousExecutionContext

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}

trait CustomNodeInvoker[T] {
  def run(lazyDeps: () => CustomNodeInvokerDeps): T
}

private[definition] class CustomNodeInvokerImpl[T](executor: ObjectWithMethodDef, metaData: MetaData, node: SplittedNode[CustomNode])
    extends CustomNodeInvoker[T] {

  override def run(lazyDeps: () => CustomNodeInvokerDeps) : T = {
    executor.invokeMethod(prepareParam(lazyDeps), Seq()).asInstanceOf[T]
  }

  private def prepareParam(lazyDeps: () => CustomNodeInvokerDeps)(param: String) : Option[AnyRef] = {
    val interpreter = CompilerLazyInterpreter[AnyRef](lazyDeps, metaData, node, param)
    val methodParam = EspTypeUtils.findParameterByParameterName(executor.methodDef.method, param)
    if (methodParam.exists(_.getType == classOf[LazyInterpreter[_]])) {
      Some(interpreter)
    } else {
      val emptyResult = InterpretationResult(NextPartReference(node.id), null, Context(""))
      Some(interpreter.syncInterpretationFunction(emptyResult))
    }
  }

}



private[definition] case class CompilerLazyInterpreter[T](lazyDeps: () => CustomNodeInvokerDeps,
                                   metaData: MetaData,
                                   node: SplittedNode[CustomNode], param: String) extends LazyInterpreter[T] {

  override def createInterpreter = (ec: ExecutionContext, context: Context) =>
    createInterpreter(ec, lazyDeps())(context)

  private[definition] def createInterpreter(ec: ExecutionContext, deps: CustomNodeInvokerDeps): (Context) => Future[T] = {

    val compiledExpression = deps.subPartCompiler
      .compileWithoutContextValidation(node)
      .getOrElse(throw new IllegalArgumentException("Cannot compile"))
      //FIXME: two lines below are quite nasty :|
      .node.asInstanceOf[compiledgraph.node.CustomNode]
      .params.find(_.name == param).getOrElse(throw new IllegalArgumentException("Cannot find param"))
      .expression

    val evaluator = deps.expressionEvaluator
        
    (context: Context) => evaluator.evaluate[T](compiledExpression, param, node.id, context)(ec, metaData).map(_.value)(ec)
  }

  //lazy val is used, interpreter creation is expensive
  @transient override lazy val syncInterpretationFunction = new SyncFunction

  class SyncFunction extends (InterpretationResult => T) with Serializable {

    lazy implicit val ec = SynchronousExecutionContext.ctx
    lazy val deps = lazyDeps()
    lazy val interpreter = createInterpreter(ec, deps)

    override def apply(v1: InterpretationResult) = {
      Await.result(interpreter(v1.finalContext), deps.processTimeout)
    }

  }


}

object CustomNodeInvoker {

  def apply[T](executor: ObjectWithMethodDef, metaData: MetaData, node: SplittedNode[CustomNode]) =
    new CustomNodeInvokerImpl[T](executor, metaData, node)

}


trait CustomNodeInvokerDeps {
  def expressionEvaluator: ExpressionEvaluator
  def subPartCompiler: PartSubGraphCompiler
  def processTimeout: FiniteDuration
}

object CustomStreamTransformerExtractor extends AbstractMethodDefinitionExtractor[CustomStreamTransformer] {

  override protected val expectedReturnType: Option[Class[_]] = None

  override protected val additionalParameters = Set[Class[_]]()

  override protected def extractParameterType(p: java.lang.reflect.Parameter) =
    EspTypeUtils.extractParameterType(p, classOf[LazyInterpreter[_]])


}


