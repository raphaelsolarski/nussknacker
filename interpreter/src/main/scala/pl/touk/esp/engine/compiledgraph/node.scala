package pl.touk.esp.engine.compiledgraph

import pl.touk.esp.engine.compiledgraph.expression.Expression
import pl.touk.esp.engine.compiledgraph.service.ServiceRef
import pl.touk.esp.engine.compiledgraph.variable.Field

object node {

  sealed trait Node {
    def id: String
  }

  case class Source(id: String, next: Next) extends Node

  case class Sink(id: String, endResult: Option[Expression]) extends Node

  case class VariableBuilder(id: String, varName: String, fields: List[Field], next: Next) extends Node

  case class Processor(id: String, service: ServiceRef, next: Next) extends Node

  case class Enricher(id: String, service: ServiceRef, output: String, next: Next) extends Node

  case class Filter(id: String, expression: Expression, nextTrue: Next,
                    nextFalse: Option[Next]) extends Node

  case class Switch(id: String, expression: Expression, exprVal: String,
                    nexts: List[Case], defaultNext: Option[Next]) extends Node

  case class Case(expression: Expression, node: Next)

  case class AggregateDefinition(id: String, keyExpression: Expression, next: PartRef) extends Node

  case class AggregateTrigger(id: String, triggerExpression:Option[Expression], next: PartRef) extends Node


  sealed trait Next
  case class NextNode(node: Node) extends Next
  case class PartRef(id: String) extends Next

}