package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

class TreeExpression(
    val op: Operation,
    val left: Expression,
    val right: Expression,
    override val position: SourcePosition? = null,
    override var type: Type? = null
) : Expression {
    init {
        left.parent = this
        right.parent = this
    }

    operator fun component1(): Operation = op
    operator fun component2(): Expression = left
    operator fun component3(): Expression = right

    override var parent: PositionedElement? = null
}