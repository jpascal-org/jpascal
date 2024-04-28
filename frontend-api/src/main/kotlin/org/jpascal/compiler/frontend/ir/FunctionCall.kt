package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.resolve.JvmMethod
import org.jpascal.compiler.frontend.ir.types.Type

class FunctionCall(
    val identifier: String,
    val arguments: List<Expression>,
    override val position: SourcePosition? = null,
    var resolved: JvmMethod? = null,
    override var type: Type? = null
) : Expression {
    init {
        arguments.forEach { it.parent = this }
    }
    override var parent: PositionedElement? = null
}