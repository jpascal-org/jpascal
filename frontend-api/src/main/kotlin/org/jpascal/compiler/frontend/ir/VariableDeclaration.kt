package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

class VariableDeclaration(
    val name: String,
    override val type: Type,
    val expr: Expression?,
    override val position: SourcePosition?
) : TypedDeclaration {
    init {
        expr?.parent = this
    }

    override var parent: PositionedElement? = null
}