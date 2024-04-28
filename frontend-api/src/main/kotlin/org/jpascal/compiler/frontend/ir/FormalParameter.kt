package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

class FormalParameter(
    val name: String,
    override val type: Type,
    val pass: Pass,
    override val position: SourcePosition?
) : TypedDeclaration {
    override var parent: PositionedElement? = null
}