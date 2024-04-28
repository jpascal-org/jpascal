package org.jpascal.compiler.frontend.ir

class FunctionStatement(
    val functionCall: FunctionCall,
    override var label: Label? = null,
    override val position: SourcePosition?
) : Statement {
    init {
        functionCall.parent = this
    }

    override var parent: PositionedElement? = null
}