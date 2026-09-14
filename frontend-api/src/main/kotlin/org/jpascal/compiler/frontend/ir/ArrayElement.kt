package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

class ArrayElement(
    val name: String,
    val indices: List<Expression>,
    override val position: SourcePosition? = null,
    override var type: Type? = null,
) : Expression, Lvalue {
    override var parent: PositionedElement? = null
}